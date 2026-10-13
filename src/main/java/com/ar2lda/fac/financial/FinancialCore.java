package com.ar2lda.fac.financial;

import com.ar2lda.fac.exception.BadRequestException;
import com.ar2lda.fac.service.MotorMonetario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/** Internal foundation. No controller or existing documentary service calls this bean. */
@Service
public class FinancialCore {
    public enum Nature { DEBT, CREDIT }
    public record Scope(long empresa, long cliente, String moeda) {}
    public record Position(long id, Scope scope, Nature nature, String origin,
                           BigDecimal original, BigDecimal available, boolean cancelled) {
        public String state() { return cancelled ? "CANCELLED" : available.signum()==0 ? "EXHAUSTED" :
                available.compareTo(original)==0 ? "OPEN" : "PARTIAL"; }
    }
    public record Part(long position, BigDecimal amount) {}
    public record Settlement(List<Part> parts, BigDecimal cashIn, BigDecimal cashOut, BigDecimal discount) {
        public Settlement { parts = List.copyOf(parts); }
    }
    private final JdbcTemplate db;
    public FinancialCore(JdbcTemplate db) { this.db = db; }
    private static void require(boolean ok, String message) { if (!ok) throw new BadRequestException(message); }
    static BigDecimal money(BigDecimal value) {
        require(value!=null && value.signum()>=0 && value.precision()-value.scale()<=17 && value.scale()<=6,
                "Invalid financial amount");
        return MotorMonetario.dinheiro(value);
    }
    private static String text(String value, int max) {
        require(value!=null && !value.isBlank() && value.length()<=max, "Invalid financial reference");
        return value;
    }
    private void scope(Scope s) {
        require(s!=null && s.empresa()>0 && s.cliente()>0 && s.moeda()!=null, "Invalid scope");
        require(Boolean.TRUE.equals(db.queryForObject("select exists(select 1 from empresa where id=?)",Boolean.class,s.empresa())),"Unknown company");
        require(Boolean.TRUE.equals(db.queryForObject("select exists(select 1 from cliente where id=?)",Boolean.class,s.cliente())),"Unknown customer");
        require(Boolean.TRUE.equals(db.queryForObject("select exists(select 1 from moeda where id=?)",Boolean.class,s.moeda())),"Unknown currency");
        // Other currencies need a validated monetary policy before activation.
        require("EUR".equals(s.moeda()),"Financial monetary policy currently supports EUR only");
    }
    private Long replay(Scope s, String key, String payload) {
        text(key,120);
        // Transaction-scoped serialization includes keys not yet inserted. Collision only serializes unrelated requests.
        db.queryForList("select pg_advisory_xact_lock(hashtextextended(?,0))",s.empresa()+":"+key);
        var found=db.queryForList("select id,request_payload,cliente_id,moeda_id from financial_operation where empresa_id=? and request_key=?",s.empresa(),key);
        if(found.isEmpty()) return null;
        var row=found.getFirst();
        require(payload.equals(row.get("request_payload")) && ((Number)row.get("cliente_id")).longValue()==s.cliente() && s.moeda().equals(row.get("moeda_id")),"Idempotency key reused with different request");
        return ((Number)row.get("id")).longValue();
    }
    private long operation(Scope s,String key,String payload,String kind,String actor,String reason,Long reverse) {
        return db.queryForObject("insert into financial_operation(empresa_id,cliente_id,moeda_id,request_key,request_payload,kind,actor,reason,reverse_of) values(?,?,?,?,?,?,?,?,?) returning id",Long.class,s.empresa(),s.cliente(),s.moeda(),key,payload,kind,text(actor,100),reason,reverse);
    }
    @Transactional
    public Position create(Scope s,String key,Nature nature,String origin,String reference,BigDecimal amount,String actor) {
        scope(s); require(nature!=null,"Nature required"); text(origin,120);
        require(reference==null || reference.length()<=200,"Reference too long");
        var value=money(amount); require(value.signum()>0,"Positive original required");
        String payload="CREATE:"+nature+":"+origin.length()+":"+origin+":"+(reference==null ? "NULL" : reference.length()+":"+reference)+":"+value.toPlainString();
        Long replay=replay(s,key,payload);
        if(replay!=null) return positionByCreation(s,replay);
        long op=operation(s,key,payload,"CREATE",actor,"Create position",null);
        long id=db.queryForObject("insert into financial_position(empresa_id,cliente_id,moeda_id,nature,origin,reference,original,available,creation_id) values(?,?,?,?,?,?,?,?,?) returning id",Long.class,s.empresa(),s.cliente(),s.moeda(),nature.name(),origin,reference,value,value,op);
        return position(s,id);
    }
    private Position positionByCreation(Scope s,long operation) {
        return position(s,db.queryForObject("select id from financial_position where creation_id=?",Long.class,operation));
    }
    @Transactional(readOnly=true)
    public Position position(Scope s,long id) {
        var rows=db.query("select * from financial_position where id=? and empresa_id=? and cliente_id=? and moeda_id=?",
            (rs,n)->new Position(rs.getLong("id"),s,Nature.valueOf(rs.getString("nature")),rs.getString("origin"),rs.getBigDecimal("original"),rs.getBigDecimal("available"),rs.getBoolean("cancelled")),id,s.empresa(),s.cliente(),s.moeda());
        require(rows.size()==1,"Position outside scope"); return rows.getFirst();
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> history(Scope s,long id) {
        position(s,id); return db.queryForList("select a.* from financial_allocation a where position_id=? order by operation_id,id",id);
    }
    @Transactional
    public long settle(Scope s,String key,Settlement request,String actor) {
        scope(s); require(request!=null && !request.parts().isEmpty(),"Allocations required");
        var parts=new TreeMap<Long,BigDecimal>();
        for(var part:request.parts()) {
            require(part!=null && part.position()>0,"Invalid position");
            var value=money(part.amount()); require(value.signum()>0,"Positive allocation required");
            require(parts.putIfAbsent(part.position(),value)==null,"Duplicate position");
        }
        var cashIn=money(request.cashIn()); var cashOut=money(request.cashOut()); var discount=money(request.discount());
        String payload="SETTLE:"+parts+":"+cashIn+":"+cashOut+":"+discount;
        Long old=replay(s,key,payload); if(old!=null) return old;
        // Documentary debts share identity locks with both receipt circuits.
        // Do this before position locks; non-documentary core positions are unaffected.
        var placeholders=String.join(",",Collections.nCopies(parts.size(),"?"));
        db.queryForList("select p.id from pendente p join financial_document_origin o "
                + "on o.document_id=p.id_documento_comercial where o.position_id in ("
                + placeholders+") order by p.id for update of p",parts.keySet().toArray());
        BigDecimal debt=BigDecimal.ZERO,credit=BigDecimal.ZERO;
        for(var entry:parts.entrySet()) {
            db.queryForList("select id from financial_position where id=? for update",entry.getKey());
            var p=position(s,entry.getKey()); require(!p.cancelled() && p.available().compareTo(entry.getValue())>=0,"Insufficient available amount");
            if(p.nature()==Nature.DEBT) debt=debt.add(entry.getValue()); else credit=credit.add(entry.getValue());
        }
        require(debt.add(cashOut).compareTo(cashIn.add(credit).add(discount))==0,"Unbalanced settlement");
        require(discount.compareTo(debt)<=0,"Discount requires debt");
        long op=operation(s,key,payload,"SETTLE",actor,"Explicit settlement",null);
        for(var entry:parts.entrySet()) {
            db.update("insert into financial_allocation(operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect) values(?,?,?,?,?,?,'CONSUME')",op,entry.getKey(),s.empresa(),s.cliente(),s.moeda(),entry.getValue());
            db.update("update financial_position set available=available-? where id=?",entry.getValue(),entry.getKey());
        }
        component(op,"CASH_IN",cashIn); component(op,"CASH_OUT",cashOut); component(op,"DISCOUNT",discount);
        return op;
    }
    private void component(long op,String kind,BigDecimal value) {
        if(value.signum()>0) db.update("insert into financial_component(operation_id,kind,amount) values(?,?,?)",op,kind,value);
    }
    @Transactional
    public long reverse(Scope s,String key,long original,String reason,String actor) {
        scope(s); text(reason,500);
        String payload="REVERSE:"+original+":"+reason;
        Long old=replay(s,key,payload); if(old!=null) return old;
        var operations=db.queryForList("select * from financial_operation where id=? and empresa_id=? and cliente_id=? and moeda_id=? for update",original,s.empresa(),s.cliente(),s.moeda());
        require(operations.size()==1,"Operation outside scope"); var row=operations.getFirst();
        require(!"REVERSE".equals(row.get("kind")),"Cannot reverse a reversal");
        require(db.queryForObject("select count(*) from financial_operation where reverse_of=?",Long.class,original)==0,"Already reversed");
        boolean creation="CREATE".equals(row.get("kind"));
        var allocations=creation ? List.<Map<String,Object>>of() : db.queryForList("select * from financial_allocation where operation_id=? order by position_id",original);
        if(creation) {
            long id=db.queryForObject("select id from financial_position where creation_id=?",Long.class,original);
            db.queryForList("select id from financial_position where id=? for update",id);
            require(db.queryForObject("select count(*) from financial_allocation a where position_id=? and effect='CONSUME' and not exists(select 1 from financial_allocation b where b.reverse_of=a.id)",Long.class,id)==0,"Active dependent consumption");
            long op=operation(s,key,payload,"REVERSE",actor,reason,original);
            db.update("update financial_position set available=0,cancelled=true where id=?",id);
            return op;
        }
        for(var a:allocations) db.queryForList("select id from financial_position where id=? for update",a.get("position_id"));
        long op=operation(s,key,payload,"REVERSE",actor,reason,original);
        for(var a:allocations) {
            long id=((Number)a.get("position_id")).longValue(); var amount=(BigDecimal)a.get("amount");
            var p=position(s,id); require(!p.cancelled() && p.available().add(amount).compareTo(p.original())<=0,"Invalid restoration");
            db.update("insert into financial_allocation(operation_id,position_id,empresa_id,cliente_id,moeda_id,amount,effect,reverse_of) values(?,?,?,?,?,?,'RESTORE',?)",op,id,s.empresa(),s.cliente(),s.moeda(),amount,a.get("id"));
            db.update("update financial_position set available=available+? where id=?",amount,id);
        }
        for(var c:db.queryForList("select * from financial_component where operation_id=? order by id",original)) {
            String kind=(String)c.get("kind");
            if(kind.equals("CASH_IN")) kind="CASH_OUT"; else if(kind.equals("CASH_OUT")) kind="CASH_IN";
            db.update("insert into financial_component(operation_id,kind,amount,reversed,reverse_of) values(?,?,?,true,?)",op,kind,c.get("amount"),c.get("id"));
        }
        return op;
    }
}
