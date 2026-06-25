package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.EmpresaCreateDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.EmpresaUpdateDto;
import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.exception.ConflictException;
import com.ar2lda.fac.exception.NotFoundException;
import com.ar2lda.fac.mapper.EmpresaMapper;
import com.ar2lda.fac.model.CodPostal;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.Freguesia;
import com.ar2lda.fac.model.Pais;
import com.ar2lda.fac.model.TipoAuditoriaEvento;
import com.ar2lda.fac.repository.CodPostalRepository;
import com.ar2lda.fac.repository.EmpresaRepository;
import com.ar2lda.fac.repository.FreguesiaRepository;
import com.ar2lda.fac.repository.PaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final CodPostalRepository codPostalRepository;
    private final PaisRepository paisRepository;
    private final FreguesiaRepository freguesiaRepository;
    private final EmpresaMapper mapper;
    private final AuditoriaService auditoriaService;
    private final CurrentUserService currentUserService;
    private final Clock clock;

    private static final long MAX_LOGO_BYTES = 1_048_576L;
    private static final int MAX_LOGO_DIMENSION = 2_000;

    @Transactional
    public EmpresaDto create(EmpresaCreateDto dto) {
        if (empresaRepository.existsById(Empresa.EMPRESA_ID)) {
            throw new ConflictException("Ficha da empresa já existe");
        }
        Empresa entity = mapper.fromCreateDTO(dto);
        validarEmpresa(dto.paisId(), dto.nif(), dto.email(), dto.web(), dto.iban(), dto.bicSwift());
        applyRelations(dto.codPostalId(), dto.paisId(), dto.freguesiaId(), entity);
        entity.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        return mapper.toDTO(empresaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public EmpresaDto get() {
        return mapper.toDTO(findEmpresa());
    }

    @Transactional
    public EmpresaDto update(EmpresaUpdateDto dto) {
        Empresa existing = findEmpresa();
        validarEmpresa(dto.paisId(), dto.nif(), dto.email(), dto.web(), dto.iban(), dto.bicSwift());
        mapper.applyUpdate(dto, existing);
        applyRelations(dto.codPostalId(), dto.paisId(), dto.freguesiaId(), existing);
        existing.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        empresaRepository.save(existing);
        auditoriaService.registar(TipoAuditoriaEvento.EMPRESA_ALTERADA, "EMPRESA", Empresa.EMPRESA_ID,
                "Dados da empresa alterados", "{\"versao\":1}");
        return mapper.toDTO(existing);
    }

    @Transactional
    public EmpresaDto guardarLogotipo(MultipartFile ficheiro) {
        Empresa empresa = findEmpresa();
        LogoValidado logo = validarLogotipo(ficheiro);
        empresa.definirLogo(logo.bytes(), logo.mediaType(), logo.nomeInterno());
        empresa.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        auditoriaService.registar(TipoAuditoriaEvento.EMPRESA_LOGOTIPO_ALTERADO, "EMPRESA", Empresa.EMPRESA_ID,
                "Logotipo da empresa alterado", "{\"versao\":1,\"mediaType\":\"" + logo.mediaType() + "\"}");
        return mapper.toDTO(empresa);
    }

    @Transactional
    public void removerLogotipo() {
        Empresa empresa = findEmpresa();
        empresa.removerLogo();
        empresa.marcarAtualizacao(OffsetDateTime.now(clock), currentUserService.currentCodeOrSystem());
        auditoriaService.registar(TipoAuditoriaEvento.EMPRESA_LOGOTIPO_REMOVIDO, "EMPRESA", Empresa.EMPRESA_ID,
                "Logotipo da empresa removido", "{\"versao\":1}");
    }

    @Transactional(readOnly = true)
    public LogoDocumento obterLogotipo() {
        Empresa empresa = findEmpresa();
        byte[] logo = empresa.getLogo();
        if (logo == null || logo.length == 0) {
            throw new NotFoundException("Logotipo não configurado");
        }
        return new LogoDocumento(logo, empresa.getLogoMediaType(), empresa.getLogoNomeInterno());
    }

    private Empresa findEmpresa() {
        return empresaRepository.findById(Empresa.EMPRESA_ID)
                .orElseThrow(() -> new NotFoundException("Ficha da empresa não encontrada"));
    }

    private void applyRelations(String codPostalId, String paisId, String freguesiaId, Empresa entity) {
        entity.setCodPostal(findCodPostal(codPostalId));
        entity.setPais(findPais(paisId));
        entity.setFreguesia(findFreguesia(freguesiaId));
    }

    private CodPostal findCodPostal(String id) {
        return codPostalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Código postal não encontrado: " + id));
    }

    private Pais findPais(String id) {
        return paisRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("País não encontrado: " + id));
    }

    private Freguesia findFreguesia(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return freguesiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Freguesia não encontrada: " + id));
    }

    private void validarEmpresa(String paisId, String nif, String email, String web, String iban, String bicSwift) {
        if ("PT".equalsIgnoreCase(paisId) && !nifPortuguesValido(nif)) {
            throw new BadRequestException("NIF português inválido");
        }
        if (web != null && !web.isBlank()) {
            try {
                URI uri = URI.create(web.trim());
                if (uri.getScheme() == null || !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException exception) {
                throw new BadRequestException("Website deve ser um URL http ou https válido");
            }
        }
        if (iban != null && !iban.isBlank() && !ibanValido(iban)) {
            throw new BadRequestException("IBAN inválido");
        }
        if (bicSwift != null && !bicSwift.isBlank()
                && !bicSwift.trim().toUpperCase(Locale.ROOT).matches("^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$")) {
            throw new BadRequestException("BIC/SWIFT inválido");
        }
    }

    private boolean nifPortuguesValido(String raw) {
        String nif = raw == null ? "" : raw.replaceAll("\\D", "");
        if (!nif.matches("^[123456789]\\d{8}$")) return false;
        int sum = 0;
        for (int i = 0; i < 8; i++) sum += (nif.charAt(i) - '0') * (9 - i);
        int check = 11 - (sum % 11);
        if (check >= 10) check = 0;
        return check == nif.charAt(8) - '0';
    }

    private boolean ibanValido(String raw) {
        String iban = raw.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (!iban.matches("^[A-Z]{2}\\d{2}[A-Z0-9]{1,30}$")) return false;
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;
        for (char c : rearranged.toCharArray()) {
            String value = Character.isDigit(c) ? String.valueOf(c) : String.valueOf(c - 'A' + 10);
            for (char digit : value.toCharArray()) remainder = (remainder * 10 + (digit - '0')) % 97;
        }
        return remainder == 1;
    }

    private LogoValidado validarLogotipo(MultipartFile ficheiro) {
        if (ficheiro == null || ficheiro.isEmpty()) throw new BadRequestException("Logotipo é obrigatório");
        if (ficheiro.getSize() > MAX_LOGO_BYTES) throw new BadRequestException("Logotipo não pode exceder 1 MiB");
        String original = ficheiro.getOriginalFilename() == null ? "logo" : ficheiro.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = ficheiro.getContentType() == null ? "" : ficheiro.getContentType().toLowerCase(Locale.ROOT);
        boolean png = original.endsWith(".png") && "image/png".equals(contentType);
        boolean jpg = (original.endsWith(".jpg") || original.endsWith(".jpeg")) && "image/jpeg".equals(contentType);
        if (!png && !jpg) throw new BadRequestException("Logotipo deve ser PNG ou JPEG");
        try {
            byte[] bytes = ficheiro.getBytes();
            var image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) throw new BadRequestException("Logotipo inválido");
            if (image.getWidth() > MAX_LOGO_DIMENSION || image.getHeight() > MAX_LOGO_DIMENSION) {
                throw new BadRequestException("Logotipo excede as dimensões máximas");
            }
            String mediaType = png ? "image/png" : "image/jpeg";
            String nome = png ? "empresa-logo.png" : "empresa-logo.jpg";
            return new LogoValidado(bytes, mediaType, nome);
        } catch (IOException exception) {
            throw new BadRequestException("Não foi possível ler o logotipo");
        }
    }

    private record LogoValidado(byte[] bytes, String mediaType, String nomeInterno) {}

    public record LogoDocumento(byte[] bytes, String mediaType, String nomeInterno) {}
}
