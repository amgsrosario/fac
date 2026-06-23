package com.ar2lda.fac.demo;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.repository.*;
import com.ar2lda.fac.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoScenarioService {
    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final UtilizadorRepository utilizadorRepository;
    private final DocumentoComercialCriacaoService criacaoService;
    private final DocumentoComercialService documentoService;
    private final LinhaDocumentoComercialService linhaService;
    private final DocumentoFinanceiroService financeiroService;
    private final PendenteRepository pendenteRepository;
    private final DocumentoComercialRepository documentoRepository;
    private final AuditoriaEventoRepository auditoriaRepository;
    private final DocumentoComercialPdfService pdfService;
    private final ExtratoClienteService extratoService;

    @Value("${fac.demo.password-admin:}") private String adminPassword;
    @Value("${fac.demo.password-operador:}") private String operadorPassword;
    @Value("${fac.demo.password-consulta:}") private String consultaPassword;
    @Value("${fac.demo.expected-database:fac_demo}") private String expectedDatabase;
    @Value("${fac.demo.reset-authorized:false}") private boolean resetAuthorized;

    @Transactional
    public DemoValidation seedAndValidate() {
        String actualDatabase = new JdbcTemplate(dataSource).queryForObject("select current_database()", String.class);
        DemoResetSafety.validate(actualDatabase, expectedDatabase, resetAuthorized);
        requirePasswords();
        if (utilizadorRepository.existsById("admin.demo")) return validateScenario();
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("demo/demo-base.sql"));
        populator.execute(dataSource);
        utilizadorRepository.deleteById("DEMO");
        saveUser("admin.demo", "Administrador Demo", "admin@fac.demo", adminPassword, PapelUtilizador.ADMINISTRADOR);
        saveUser("operador.demo", "Operador Demo", "operador@fac.demo", operadorPassword, PapelUtilizador.OPERADOR);
        saveUser("consulta.demo", "Consulta Demo", "consulta@fac.demo", consultaPassword, PapelUtilizador.CONSULTA);

        var authorities = PapelUtilizador.ADMINISTRADOR.permissoes().stream().map(p -> new SimpleGrantedAuthority(p.name())).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin.demo", "N/A", authorities));
        try {
            var d1 = emitir(1104L, "AZ075", 10, "Fatura para extrato");
            var parcial = emitir(1104L, "AZ5L", 4, "Documento parcialmente pago");
            pagar(parcial.id(), new BigDecimal("50.00"));
            var total = emitir(1101L, "CABAZ", 3, "Documento totalmente pago");
            pagar(total.id(), total.valorTotal());
            var anulado = emitir(1105L, "VTRES", 6, "Documento destinado a anulacao");
            documentoService.anular(anulado.id(), new AnularDocumentoRequest("Documento emitido por duplicacao operacional"));
            emitir(1102L, "TRINT", 2, "Documento para cliente espanhol");
            var multi = criar(1103L, "AZ075", 1, "Documento multipagina para validacao MD 15");
            for (int i = 0; i < 34; i++) linhaService.create(multi.id(), line(i % 2 == 0 ? "VTRES" : "VBREG", BigDecimal.ONE));
            documentoService.emitir(multi.id(), new DocumentoComercialEmitirDto("admin.demo"));
        } finally {
            SecurityContextHolder.clearContext();
        }
        return validateScenario();
    }

    public DemoValidation validateScenario() {
        long users = List.of("admin.demo","operador.demo","consulta.demo").stream().filter(utilizadorRepository::existsById).count();
        long documents = documentoRepository.count();
        long annulled = documentoRepository.findAll().stream().filter(DocumentoComercial::isAnulado).count();
        long partial = pendenteRepository.findAll().stream().filter(p -> p.getValorPendente().signum() > 0 && p.getValorPendente().compareTo(p.getValorDocumento()) < 0).count();
        long multi = documentoRepository.findAll().stream().filter(d -> d.getObservacoes() != null && d.getObservacoes().contains("multipagina")).count();
        long audits = auditoriaRepository.count();
        long pdfs = documentoRepository.findAll().stream().filter(d -> d.getEstado() != EstadoDocumentoComercial.RASCUNHO)
                .map(d -> pdfService.gerar(d.getId())).filter(pdf -> pdf.content().length > 1000).count();
        long extratoMovimentos = extratoService.getExtrato(1104L, LocalDate.of(2026,1,1), LocalDate.of(2026,12,31))
                .moedas().stream().mapToLong(m -> m.movimentos().size()).sum();
        DemoValidation result = new DemoValidation(users, documents, annulled, partial, multi, audits, pdfs, extratoMovimentos);
        if (!result.complete()) throw new IllegalStateException("Cenario demo incompleto: " + result);
        return result;
    }

    private DocumentoComercialDto emitir(long cliente, String artigo, int quantidade, String obs) {
        var dto = criar(cliente, artigo, quantidade, obs);
        return documentoService.emitir(dto.id(), new DocumentoComercialEmitirDto("admin.demo"));
    }
    private DocumentoComercialDto criar(long cliente, String artigo, int quantidade, String obs) {
        var header = new DocumentoComercialCreateDto("FT","DEMO26",LocalDate.of(2026,6,23),cliente,null,1001L,"EUR","CON",1001,"P30",1001,null,null,null,null,null,null,obs);
        return criacaoService.createComPrimeiraLinha(new DocumentoComercialComLinhaCreateDto(header, line(artigo, BigDecimal.valueOf(quantidade))));
    }
    private LinhaDocumentoComercialCreateDto line(String artigo, BigDecimal quantidade) {
        BigDecimal price = switch (artigo) { case "AZ075" -> new BigDecimal("12.50"); case "AZ5L" -> new BigDecimal("54.90"); case "VTRES" -> new BigDecimal("18.90"); case "VBREG" -> new BigDecimal("11.90"); case "CABAZ" -> new BigDecimal("39.90"); case "TRINT" -> new BigDecimal("35.00"); default -> new BigDecimal("10.00"); };
        return new LinhaDocumentoComercialCreateDto(artigo,null,quantidade,price,TipoDescontoLinha.PERCENTAGEM,BigDecimal.ZERO,null,null);
    }
    private void pagar(long documentoId, BigDecimal value) {
        var p = pendenteRepository.findByDocumentoComercialId(documentoId).orElseThrow();
        financeiroService.create(new DocumentoFinanceiroCreateDto("RC","DEMO26",LocalDate.of(2026,6,24),p.getCliente().getId(),"EUR",1001,null,"admin.demo","Recebimento demonstrativo",List.of(new LinhaDocumentoFinanceiroCreateDto(p.getId(),value,BigDecimal.ZERO,BigDecimal.ZERO))));
    }
    private void saveUser(String code, String name, String email, String password, PapelUtilizador role) {
        Utilizador u = new Utilizador(code,name,email,passwordEncoder.encode(password),false); u.setPapel(role); utilizadorRepository.save(u);
    }
    private void requirePasswords() {
        if (adminPassword.length() < 8 || operadorPassword.length() < 8 || consultaPassword.length() < 8) throw new IllegalStateException("Defina as tres passwords demo por variaveis de ambiente (minimo 8 caracteres)");
    }
    public record DemoValidation(long users, long documents, long annulled, long partial, long multipage, long audits, long pdfs, long extratoMovimentos) {
        public boolean complete() { return users == 3 && documents >= 6 && annulled >= 1 && partial >= 1 && multipage >= 1 && audits >= 6 && pdfs >= 6 && extratoMovimentos >= 2; }
    }
}
