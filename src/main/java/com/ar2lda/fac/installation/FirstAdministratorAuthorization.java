package com.ar2lda.fac.installation;

import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.*;

/** An operator-supplied local authorization, not a substitute for administrative approval. */
public record FirstAdministratorAuthorization(String jdbcUrl,String operator,String database,String cluster,
        UUID installation,String reference,String actor,Instant expiresAt) {
    public FirstAdministratorAuthorization {
        if(jdbcUrl==null || !jdbcUrl.matches("jdbc:postgresql://(?:127\\.0\\.0\\.1|localhost):[0-9]{1,5}/[A-Za-z0-9_]+")
           || operator==null || operator.isBlank() || database==null || database.isBlank()
           || cluster==null || !cluster.matches("[0-9]+") || installation==null
           || reference==null || reference.isBlank() || reference.length()>100
           || actor==null || actor.isBlank() || actor.length()>100 || expiresAt==null)
            throw new IllegalArgumentException("Autorizacao local invalida");
        if(!jdbcUrl.endsWith("/"+database)) throw new IllegalArgumentException("Base autorizada incoerente");
    }
    public static FirstAdministratorAuthorization load(Path path) throws Exception {
        if(!path.isAbsolute() || !Files.isRegularFile(path,LinkOption.NOFOLLOW_LINKS)
           || Files.isSymbolicLink(path) || Files.size(path)>8192)
            throw new IllegalArgumentException("Exigido ficheiro local regular absoluto");
        var attrs=Files.readAttributes(path,java.nio.file.attribute.PosixFileAttributes.class,LinkOption.NOFOLLOW_LINKS);
        if(!attrs.owner().getName().equals(System.getProperty("user.name"))
           || !attrs.permissions().equals(Set.of(PosixFilePermission.OWNER_READ,PosixFilePermission.OWNER_WRITE)))
            throw new IllegalArgumentException("Autorizacao exige dono actual e permissoes 0600");
        for(Path parent=path.getParent();parent!=null;parent=parent.getParent()) {
            var a=Files.readAttributes(parent,java.nio.file.attribute.PosixFileAttributes.class,LinkOption.NOFOLLOW_LINKS);
            if(Files.isSymbolicLink(parent) || !(a.owner().getName().equals(System.getProperty("user.name")) || a.owner().getName().equals("root"))
               || a.permissions().contains(PosixFilePermission.GROUP_WRITE) || a.permissions().contains(PosixFilePermission.OTHERS_WRITE))
                throw new IllegalArgumentException("Directorio de autorizacao nao protegido");
        }
        var p=new Properties() {
            @Override public synchronized Object put(Object key,Object value) {
                if(containsKey(key)) throw new IllegalArgumentException("Campo de autorizacao repetido");
                return super.put(key,value);
            }
        };try(var input=Files.newInputStream(path,LinkOption.NOFOLLOW_LINKS)){p.load(input);}
        if(!p.stringPropertyNames().equals(Set.of("jdbcUrl","operator","database","cluster","installation","reference","actor","expiresAt")))
            throw new IllegalArgumentException("Campos de autorizacao invalidos");
        return new FirstAdministratorAuthorization(p.getProperty("jdbcUrl"),p.getProperty("operator"),p.getProperty("database"),
            p.getProperty("cluster"),UUID.fromString(p.getProperty("installation")),p.getProperty("reference"),
            p.getProperty("actor"),Instant.parse(p.getProperty("expiresAt")));
    }
}
