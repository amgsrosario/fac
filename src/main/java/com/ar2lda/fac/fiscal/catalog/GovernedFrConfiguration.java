package com.ar2lda.fac.fiscal.catalog;

import com.ar2lda.fac.controller.dto.*;
import com.ar2lda.fac.model.*;
import com.ar2lda.fac.mapper.TipoDocumentoMapper;
import com.ar2lda.fac.security.FunctionalAuthorization;
import com.ar2lda.fac.service.CurrentUserService;
import com.ar2lda.fac.exception.BadRequestException;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

/** Deliberately internal and absent by default. Normal CRUD retains C6, without auto-binding. */
@Service @Profile("fr-inactive-lab")
public class GovernedFrConfiguration {
    private final FiscalCatalog catalog;private final EntityManager entities;private final TipoDocumentoMapper mapper;
    private final FunctionalAuthorization permissions;private final CurrentUserService users;private final FiscalTypeIdentity identity;
    public GovernedFrConfiguration(FiscalCatalog catalog,EntityManager entities,TipoDocumentoMapper mapper,FunctionalAuthorization permissions,CurrentUserService users,FiscalTypeIdentity identity){this.catalog=catalog;this.entities=entities;this.mapper=mapper;this.permissions=permissions;this.users=users;this.identity=identity;}
    @Transactional public TipoDocumentoDto create(TipoDocumentoCreateDto dto) {
        permissions.require(PermissaoFuncional.CONFIGURACAO_GERIR);
        var auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var user=users.currentUserOrNull();
        if(auth==null||!auth.isAuthenticated()||auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken||user==null||user.isInativo())throw new BadRequestException("Authenticated configuration actor required");
        var revision=catalog.frImmediateCreation();var t=mapper.fromCreateDTO(dto);
        if(!FrFiscalSemantics.permits(revision.definition(),t.getCodigoFiscal(),t.getAreaGestao(),t.getEntidade(),t.getSinalContabilistico(),t.isLiquidacaoImediata()))throw new BadRequestException("Incompatible governed FR configuration");
        // The SQL guard claims durable historical identity atomically, including other documentary tables.
        identity.requireNewFrCode(t.getId());
        var bound=TipoDocumento.governed(t,"FT",revision.key().revision());entities.persist(bound);entities.flush();return mapper.toDTO(bound);
    }
}
