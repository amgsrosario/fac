package com.ar2lda.fac.fiscal.signingtrial;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

/** Explicit experimental library: no Spring component, endpoint, production SignatureSource or PDF writer. */
public final class TrialProofEngine {
    public record Proof(UUID document, String invoiceNumber, TrialCanonical snapshot, String canonical,
                        String signature, String predecessor, String publicKey) {
        public boolean verified() {
            return canonical.equals(snapshot.bytesText(invoiceNumber,predecessor))
                    && EphemeralTrialSigner.verify(canonical,signature,publicKey);
        }
        public String demonstrationQ() {
            if (!verified()) throw new IllegalStateException("Unverifiable trial proof");
            return "NONFISCAL:"+signature.charAt(0)+signature.charAt(10)+signature.charAt(20)+signature.charAt(30);
        }
    }
    private final JdbcTemplate db;
    private final TransactionTemplate tx;
    private final EphemeralTrialSigner signer;
    public TrialProofEngine(JdbcTemplate db, PlatformTransactionManager manager, EphemeralTrialSigner signer) {
        this.db=Objects.requireNonNull(db); this.tx=new TransactionTemplate(Objects.requireNonNull(manager));
        this.signer=Objects.requireNonNull(signer);
        // Actual connection metadata, not an application environment flag. No arbitrary remote test database.
        db.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) c -> {
            URI uri=URI.create(c.getMetaData().getURL().substring(5));
            if (!Set.of("localhost","127.0.0.1").contains(uri.getHost()) || !uri.getPath().endsWith("_test"))
                throw new IllegalStateException("Local disposable _test database required");
            return null;
        });
    }
    public UUID newScope() {
        UUID id=UUID.randomUUID();
        db.update("insert into signature_trial.scope(id,label) values(?,'H2-07-NONFISCAL')",id);
        return id;
    }
    public void newSeries(UUID scope,String name) {
        if (name==null || !name.matches("H207T[A-Z0-9]{1,24}")) throw new IllegalArgumentException("Trial series required");
        db.update("insert into signature_trial.series(scope,name) values(?,?)",scope,name);
    }
    public Proof append(UUID scope,String series,TrialCanonical snapshot) {
        return tx.execute(status -> {
            db.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) c -> {
                if (c.getAutoCommit()) throw new IllegalStateException("Shared transactional JDBC connection required");
                return null;
            });
            // Global scope clock first, then fiscal series identity; deterministic even across multiple series.
            db.queryForObject("select id from signature_trial.scope where id=? for update",UUID.class,scope);
            db.queryForObject("select name from signature_trial.series where scope=? and name=? and fiscal_type='FT' for update",String.class,scope,series);
            var replay=find(scope,snapshot.document());
            if (replay.isPresent()) {
                Proof p=replay.get();
                if (!p.snapshot().equals(snapshot) || !p.invoiceNumber().startsWith("FT "+series+"/"))
                    throw new IllegalStateException("Trial replay payload conflict");
                return p;
            }
            var head=db.query("select sequence,document from signature_trial.proof where scope=? and series=? and fiscal_type='FT' order by sequence desc limit 1",
                    (r,n)->Map.entry(r.getLong(1),r.getObject(2,UUID.class)),scope,series);
            long next=head.isEmpty()?1:head.getFirst().getKey()+1;
            String previous=head.isEmpty()?"":find(scope,head.getFirst().getValue()).orElseThrow().signature();
            String number="FT "+series+"/"+next;
            String canonical=snapshot.bytesText(number,previous),signature=signer.sign(canonical);
            if (!EphemeralTrialSigner.verify(canonical,signature,signer.publicKey())) throw new IllegalStateException("Trial proof invalid");
            db.update("insert into signature_trial.proof(scope,series,fiscal_type,sequence,document,invoice_date,system_at,gross_total,canonical,previous_signature,signature,public_key,key_version) values(?,?,'FT',?,?,?,?,?,?,?,?,?,1)",
                    scope,series,next,snapshot.document(),snapshot.invoiceDate(),Timestamp.valueOf(snapshot.systemEntryDate()),snapshot.grossTotal(),canonical,previous,signature,signer.publicKey());
            return find(scope,snapshot.document()).orElseThrow();
        });
    }
    public Optional<Proof> find(UUID scope,UUID document) {
        var values=db.query("select * from signature_trial.proof where scope=? and document=?",(r,n)-> {
            var snapshot=new TrialCanonical(document,r.getString("fiscal_type"),"EUR",false,r.getDate("invoice_date").toLocalDate(),r.getTimestamp("system_at").toLocalDateTime(),r.getBigDecimal("gross_total"));
            var proof=new Proof(document,"FT "+r.getString("series")+"/"+r.getLong("sequence"),snapshot,r.getString("canonical"),r.getString("signature"),r.getString("previous_signature"),r.getString("public_key"));
            if (!proof.verified()) throw new IllegalStateException("Stored trial proof unverifiable");
            return proof;
        },scope,document);
        return values.stream().findFirst();
    }
}
