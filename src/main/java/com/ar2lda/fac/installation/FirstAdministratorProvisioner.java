package com.ar2lda.fac.installation;

import com.ar2lda.fac.controller.dto.UtilizadorCreateDto;
import com.ar2lda.fac.model.PapelUtilizador;
import com.ar2lda.fac.service.UtilizadorPasswordPolicy;
import jakarta.validation.Validation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/** Not a Spring bean/service/runner. Only the local console entry point uses this class. */
public final class FirstAdministratorProvisioner {
    public record Administrator(String code,String name,String email) { }
    public long provision(Connection db,FirstAdministratorAuthorization auth,Administrator admin,char[] password) throws Exception {
        Objects.requireNonNull(password);
        try {
            if(!db.getAutoCommit()) throw new IllegalArgumentException("Exigida ligacao dedicada sem transaccao externa");
            if(db.getTransactionIsolation()!=Connection.TRANSACTION_READ_COMMITTED)
                throw new IllegalArgumentException("Provisionamento exige READ COMMITTED");
            if(auth.expiresAt().isBefore(java.time.Instant.now())) throw new IllegalArgumentException("Autorizacao expirada");
            String code=admin.code().trim().toUpperCase(Locale.ROOT);
            String name=admin.name().trim();String email=admin.email().trim().toLowerCase(Locale.ROOT);
            String raw=new String(password);
            var dto=new UtilizadorCreateDto(code,name,email,raw,PapelUtilizador.ADMINISTRADOR,false);
            try(var factory=Validation.buildDefaultValidatorFactory()) {
                if(!factory.getValidator().validate(dto).isEmpty()) throw new IllegalArgumentException("Dados do administrador invalidos");
            }
            if(raw.getBytes(StandardCharsets.UTF_8).length>72) throw new IllegalArgumentException("Password excede 72 bytes UTF-8");
            UtilizadorPasswordPolicy.validate(raw,code);
            // No plaintext, hash or personal data in exceptions/logs/results.
            String hash=new BCryptPasswordEncoder().encode(raw);
            try(var sql=db.prepareStatement("select public.provision_first_administrator(?,?,?::uuid,?,?,?,?::timestamptz,?,?,?,?)")) {
                sql.setString(1,auth.database());sql.setString(2,auth.cluster());sql.setObject(3,auth.installation());
                sql.setString(4,auth.operator());sql.setString(5,auth.reference());sql.setString(6,auth.actor());
                sql.setString(7,auth.expiresAt().toString());sql.setString(8,code);sql.setString(9,name);
                sql.setString(10,email);sql.setString(11,hash);
                try(var result=sql.executeQuery()){if(!result.next())throw new IllegalStateException("Sem comprovativo de provisionamento");return result.getLong(1);}
            }
        } finally {Arrays.fill(password,'\0');}
    }
}
