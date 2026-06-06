package com.ar2lda.fac.mapper;

import com.ar2lda.fac.controller.dto.EmpresaCreateDto;
import com.ar2lda.fac.controller.dto.EmpresaDto;
import com.ar2lda.fac.controller.dto.EmpresaUpdateDto;
import com.ar2lda.fac.model.Empresa;
import com.ar2lda.fac.model.Freguesia;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    default EmpresaDto toDTO(Empresa entity) {
        if (entity == null) {
            return null;
        }
        Freguesia freguesia = entity.getFreguesia();
        return new EmpresaDto(
                entity.getId(),
                entity.getNome(),
                entity.getNif(),
                entity.getMorada(),
                entity.getMorada1(),
                entity.getCodPostal() != null ? entity.getCodPostal().getId() : null,
                entity.getLocalidade(),
                entity.getPais() != null ? entity.getPais().getId() : null,
                freguesia != null ? freguesia.getCodigo() : null,
                freguesia != null ? freguesia.getCodigoDistrito() : null,
                freguesia != null ? freguesia.getCodigoConcelho() : null,
                freguesia != null ? freguesia.getConcelho() : null,
                freguesia != null ? freguesia.getNome() : null,
                entity.getCapitalSocial(),
                entity.getMatriculaRegistoComercial(),
                entity.getCae(),
                entity.getDescricaoCae(),
                entity.getEmail(),
                entity.getWeb()
        );
    }

    default Empresa fromCreateDTO(EmpresaCreateDto dto) {
        if (dto == null) {
            return null;
        }
        return new Empresa(
                dto.nome(),
                dto.nif(),
                dto.morada(),
                dto.morada1(),
                dto.localidade(),
                dto.capitalSocial(),
                dto.matriculaRegistoComercial(),
                dto.cae(),
                dto.descricaoCae(),
                dto.email(),
                dto.web()
        );
    }

    default void applyUpdate(EmpresaUpdateDto dto, @MappingTarget Empresa entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setNome(dto.nome());
        entity.setNif(dto.nif());
        entity.setMorada(dto.morada());
        entity.setMorada1(dto.morada1());
        entity.setLocalidade(dto.localidade());
        entity.setCapitalSocial(dto.capitalSocial());
        entity.setMatriculaRegistoComercial(dto.matriculaRegistoComercial());
        entity.setCae(dto.cae());
        entity.setDescricaoCae(dto.descricaoCae());
        entity.setEmail(dto.email());
        entity.setWeb(dto.web());
    }
}
