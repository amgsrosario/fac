package com.ar2lda.fac.service;

import com.ar2lda.fac.model.Transporte;
import com.ar2lda.fac.repository.TransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransporteService {
    private final TransporteRepository transporteRepository;

    public Transporte salvar(Transporte transporte) {
        return transporteRepository.save(transporte);
    }

    public Optional<Transporte>porId(Integer id){
        return transporteRepository.findById(id);
    }

    public Transporte atualizar(Transporte transporte) {
        return transporteRepository.save(transporte);
    }

    public List<Transporte> pesquisa(
            Integer id,
            String nome){
        return transporteRepository.findAll();
    };

    public void eliminar(Transporte transporte){
        transporteRepository.delete (transporte);
    }

}
