package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.mapper.SerieMapper;
import com.ar2lda.fac.model.Serie;
import com.ar2lda.fac.model.SerieId;
import com.ar2lda.fac.model.TipoDocumento;
import com.ar2lda.fac.repository.DocumentoComercialRepository;
import com.ar2lda.fac.repository.DocumentoFinanceiroRepository;
import com.ar2lda.fac.repository.SerieRepository;
import com.ar2lda.fac.repository.TipoDocumentoRepository;
import com.ar2lda.fac.security.FunctionalAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SerieServiceTests {

    @Mock
    private SerieRepository repository;
    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock
    private DocumentoComercialRepository documentoComercialRepository;
    @Mock
    private DocumentoFinanceiroRepository documentoFinanceiroRepository;
    @Mock
    private SerieMapper mapper;
    @Mock
    private AuditoriaService auditoriaService;
    @Mock
    private FunctionalAuthorization functionalAuthorization;

    private SerieService service;
    private Serie serie;

    @BeforeEach
    void setup() {
        service = new SerieService(
                repository,
                tipoDocumentoRepository,
                documentoComercialRepository,
                documentoFinanceiroRepository,
                mapper,
                auditoriaService,
                functionalAuthorization
        );
        TipoDocumento tipoDocumento = mock(TipoDocumento.class);
        lenient().when(tipoDocumento.getId()).thenReturn("FT1");
        serie = new Serie(tipoDocumento, "2026", "Faturas 2026", null, null);
        lenient().when(repository.findById(new SerieId("FT1", "2026"))).thenReturn(Optional.of(serie));
    }

    @Test
    void impedeEliminarSerieComDocumentoComercialMesmoSemNumeracao() {
        when(documentoComercialRepository.existsByTipoDocumentoIdAndSerie("FT1", "2026")).thenReturn(true);

        assertThatThrownBy(() -> service.delete(" FT1 ", " 2026 "))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Não é possível eliminar uma série já utilizada");

        verify(repository, never()).delete(serie);
    }

    @Test
    void impedeEliminarSerieComDocumentoFinanceiroMesmoSemNumeracao() {
        when(documentoFinanceiroRepository.existsByTipoDocumentoIdAndSerie("FT1", "2026")).thenReturn(true);

        assertThatThrownBy(() -> service.delete("FT1", "2026"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Não é possível eliminar uma série já utilizada");

        verify(repository, never()).delete(serie);
    }

    @Test
    void rejeitaNumeracaoDeEmissaoSemCodigoAtSemConsumirNumero() {
        when(repository.findForUpdate("FT1", "2026")).thenReturn(Optional.of(serie));

        assertThatThrownBy(() -> service.proximoNumeroParaEmissao("FT1", "2026"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("A série selecionada não possui código de validação atribuído pela AT.");

        assertThat(serie.getNumerador()).isZero();
    }

    @Test
    void devolveNumeroECodigoAtSobBloqueioDeEmissao() {
        serie.setCodigoAt("  ABCD1234  ");
        when(repository.findForUpdate("FT1", "2026")).thenReturn(Optional.of(serie));

        SerieNumeracao numeracao = service.proximoNumeroParaEmissao(" FT1 ", " 2026 ");

        assertThat(numeracao.numeroSequencial()).isEqualTo(1L);
        assertThat(numeracao.codigoValidacaoAt()).isEqualTo("ABCD1234");
        verify(repository).findForUpdate("FT1", "2026");
    }
}
