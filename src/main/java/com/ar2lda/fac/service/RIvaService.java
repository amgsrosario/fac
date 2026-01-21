package com.ar2lda.fac.service;

import com.ar2lda.fac.model.RIva;
import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.RIvaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RIvaService {
    private final RIvaRepository rivaRepository;

    public RIva salvar (RIva rIva) {
        return rivaRepository.save(rIva);
    }

    public RIva atualizar (RIva rIva) {
        if(rIva.getId() == null) {
            throw new IllegalArgumentException("O registo que pretende atualizar não existe!");
        }
        return rivaRepository.save(rIva);
    }
}
