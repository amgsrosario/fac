package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.ClienteCreateDto;
import com.ar2lda.fac.controller.dto.ClienteDto;
import com.ar2lda.fac.controller.dto.ClienteUpdateDto;
import com.ar2lda.fac.model.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class ClienteMapper {

    public ClienteDto toDTO(Cliente e) {
        if (e == null) return null;
        return new ClienteDto(
                e.getId(),
                e.getNome(),
                e.getMorada(),
                e.getMorada1(),
                e.getLocalidade(),
                e.getNif(),
                e.getTel(),
                e.getTm(),
                e.getEmail(),
                e.getEmail1(),
                e.getTspiva(),
                e.getIban(),
                e.isRetencao(),
                e.isInativo(),
                e.getObservacoes(),
                e.getCodPostal() != null ? e.getCodPostal().getId() : null,
                e.getPais() != null ? e.getPais().getId() : null,
                e.getMoeda() != null ? e.getMoeda().getId() : null,
                e.getMPagamento() != null ? e.getMPagamento().getId() : null,
                e.getPPagamento() != null ? e.getPPagamento().getId() : null,
                e.getRiva() != null ? e.getRiva().getId() : null,
                e.getTransporte() != null ? e.getTransporte().getId() : null
        );
    }

    public Cliente fromCreateDTO(ClienteCreateDto dto) {
        if (dto == null) return null;
        Cliente c = new Cliente();
        applyScalars(dto.nome(), dto.morada(), dto.morada1(), dto.localidade(), dto.nif(), dto.tel(), dto.tm(),
                dto.email(), dto.email1(), dto.tspiva(), dto.iban(), dto.retencao(), dto.inativo(), dto.observacoes(), c);
        return c;
    }

    public void applyUpdate(ClienteUpdateDto dto, @MappingTarget Cliente c) {
        if (dto == null || c == null) return;
        applyScalars(dto.nome(), dto.morada(), dto.morada1(), dto.localidade(), dto.nif(), dto.tel(), dto.tm(),
                dto.email(), dto.email1(), dto.tspiva(), dto.iban(), dto.retencao(), dto.inativo(), dto.observacoes(), c);
    }

    private void applyScalars(String nome, String morada, String morada1, String localidade, String nif,
                              String tel, String tm, String email, String email1, String tspiva, String iban,
                              boolean retencao, boolean inativo, String observacoes, Cliente c) {
        c.setNome(nome);
        c.setMorada(morada);
        c.setMorada1(morada1);
        c.setLocalidade(localidade);
        c.setNif(nif);
        c.setTel(tel);
        c.setTm(tm);
        c.setEmail(email);
        c.setEmail1(email1);
        c.setTspiva(tspiva);
        c.setIban(iban);
        c.setRetencao(retencao);
        c.setInativo(inativo);
        c.setObservacoes(observacoes);
    }
}
