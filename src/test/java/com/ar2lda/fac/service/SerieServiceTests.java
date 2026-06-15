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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private SerieService service;
    private Serie serie;

    @BeforeEach
    void setup() {
        service = new SerieService(
                repository,
                tipoDocumentoRepository,
                documentoComercialRepository,
                documentoFinanceiroRepository,
                mapper
        );
        TipoDocumento tipoDocumento = mock(TipoDocumento.class);
        when(tipoDocumento.getId()).thenReturn("FT1");
        serie = new Serie(tipoDocumento, "2026", "Faturas 2026", null, null);
        when(repository.findById(new SerieId("FT1", "2026"))).thenReturn(Optional.of(serie));
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
}
