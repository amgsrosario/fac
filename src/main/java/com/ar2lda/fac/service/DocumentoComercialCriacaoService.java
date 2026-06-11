package com.ar2lda.fac.service;

import com.ar2lda.fac.controller.dto.DocumentoComercialComLinhaCreateDto;
import com.ar2lda.fac.controller.dto.DocumentoComercialDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentoComercialCriacaoService {

    private final DocumentoComercialService documentoService;
    private final LinhaDocumentoComercialService linhaService;

    @Transactional
    public DocumentoComercialDto createComPrimeiraLinha(DocumentoComercialComLinhaCreateDto dto) {
        DocumentoComercialDto documento = documentoService.create(dto.documento());
        linhaService.create(documento.id(), dto.linha());
        return documentoService.getById(documento.id());
    }
}
