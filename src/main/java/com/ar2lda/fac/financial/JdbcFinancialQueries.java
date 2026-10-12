package com.ar2lda.fac.financial;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import static com.ar2lda.fac.financial.FinancialCore.*;

/** Reads persisted cents exactly. Does not reconcile legacy RC/Pendente or perform monetary conversions. */
@Service
@Transactional(readOnly = true)
public class JdbcFinancialQueries implements FinancialQueries {
    private final JdbcTemplate db;
    public JdbcFinancialQueries(JdbcTemplate db) { this.db = db; }

    private static final String POSITION = """
        select p.*, o.document_id, d.data_emissao, d.data_vencimento
        from financial_position p
        left join financial_document_origin o on o.position_id=p.id
        left join documento_comercial d on d.id=o.document_id
        where p.empresa_id=? and p.cliente_id=? and p.moeda_id=?
        """;
    private static void require(boolean condition, String message) {
        if (!condition) throw new BadRequestException(message);
    }
    private static void scope(Scope s) {
        require(s != null && s.empresa()>0 && s.cliente()>0 && s.moeda()!=null && !s.moeda().isBlank(), "Invalid scope");
    }
    private static void page(int offset, int limit) {
        require(offset>=0 && limit>=1 && limit<=500, "Invalid financial page (limit 1..500)");
    }
    private static Optional<CommercialOrigin> document(ResultSet rs) throws SQLException {
        Long id=rs.getObject("document_id", Long.class);
        return id==null ? Optional.empty() : Optional.of(new CommercialOrigin(id, rs.getString("reference"),
                rs.getObject("data_emissao", LocalDate.class), rs.getObject("data_vencimento", LocalDate.class)));
    }
    private static PositionView position(ResultSet rs, Scope s) throws SQLException {
        BigDecimal original=rs.getBigDecimal("original"), residual=rs.getBigDecimal("available");
        State state=rs.getBoolean("cancelled") ? State.CANCELLED : residual.signum()==0 ? State.EXHAUSTED :
                original.compareTo(residual)==0 ? State.OPEN : State.PARTIAL;
        return new PositionView(rs.getLong("id"), s, Nature.valueOf(rs.getString("nature")), rs.getString("origin"),
                rs.getString("reference"), original, residual, state, rs.getLong("creation_id"), document(rs),
                rs.getTimestamp("created_at").toInstant());
    }
    @Override public Page<PositionView> positions(Scope s, Selection selection, Nature nature, int offset, int limit) {
        scope(s); page(offset, limit); require(selection!=null, "Selection required");
        String predicate=switch(selection) {
            case ALL -> "";
            case OPEN -> " and not p.cancelled and p.available>0";
            case CLOSED -> " and (p.cancelled or p.available=0)";
        };
        if (nature!=null) predicate += " and p.nature=?";
        var args=new ArrayList<Object>(); args.add(s.empresa()); args.add(s.cliente()); args.add(s.moeda());
        if (nature!=null) args.add(nature.name()); args.add(limit); args.add(offset);
        // Count and page share a single statement snapshot, including empty/out-of-range pages.
        String sql="with selected as ("+POSITION+predicate+"), total as (select count(*) as total from selected), " +
                "slice as (select * from selected order by id limit ? offset ?) select total.total,slice.* from total left join slice on true order by slice.id";
        return db.query(sql, rs -> {
            var items=new ArrayList<PositionView>(); long total=0;
            while(rs.next()) { total=rs.getLong("total"); if(rs.getObject("id")!=null) items.add(position(rs,s)); }
            return new Page<>(items,total,offset,limit);
        }, args.toArray());
    }
    @Override public PositionView position(Scope s, long id) {
        scope(s); require(id>0,"Invalid position");
        var found=db.query(POSITION+" and p.id=?",(rs,n)->position(rs,s),s.empresa(),s.cliente(),s.moeda(),id);
        require(found.size()==1,"Position outside scope"); return found.getFirst();
    }
    @Override public Optional<PositionView> byCommercialDocument(Scope s, long documentId) {
        scope(s); require(documentId>0,"Invalid document");
        return db.query(POSITION+" and o.document_id=?",(rs,n)->position(rs,s),s.empresa(),s.cliente(),s.moeda(),documentId).stream().findFirst();
    }
    @Override public Balance balance(Scope s) {
        scope(s);
        return db.queryForObject("""
            select coalesce(sum(available) filter(where nature='DEBT' and not cancelled),0) as debt,
                   coalesce(sum(available) filter(where nature='CREDIT' and not cancelled),0) as credit,
                   count(*) filter(where not cancelled and available>0) as opened,
                   count(*) filter(where cancelled or available=0) as closed
            from financial_position where empresa_id=? and cliente_id=? and moeda_id=?
            """,(rs,n)-> { var debt=rs.getBigDecimal("debt"); var credit=rs.getBigDecimal("credit");
                return new Balance(s,debt,credit,debt.subtract(credit),rs.getLong("opened"),rs.getLong("closed"));
            },s.empresa(),s.cliente(),s.moeda());
    }
    @Override public Page<Allocation> allocations(Scope s,long positionId,int offset,int limit) {
        scope(s); page(offset,limit); position(s,positionId);
        return db.query("""
            with selected as (
              select a.*, op.kind, op.created_at, op.reverse_of as reverse_operation
              from financial_allocation a join financial_operation op on op.id=a.operation_id
              where a.empresa_id=? and a.cliente_id=? and a.moeda_id=? and a.position_id=?
            ), total as (select count(*) as total from selected),
            slice as (select * from selected order by operation_id,id limit ? offset ?)
            select total.total,slice.* from total left join slice on true order by slice.operation_id,slice.id
            """,rs -> {
                var items=new ArrayList<Allocation>(); long total=0;
                while(rs.next()) { total=rs.getLong("total"); if(rs.getObject("id")!=null)
                    items.add(new Allocation(rs.getLong("id"),rs.getLong("position_id"),rs.getLong("operation_id"),
                            rs.getBigDecimal("amount"),Effect.valueOf(rs.getString("effect")),rs.getObject("reverse_of",Long.class),
                            OperationKind.valueOf(rs.getString("kind")),rs.getObject("reverse_operation",Long.class),
                            rs.getTimestamp("created_at").toInstant())); }
                return new Page<>(items,total,offset,limit);
            },s.empresa(),s.cliente(),s.moeda(),positionId,limit,offset);
    }
    @Override public OperationView operation(Scope s,long id) {
        scope(s); require(id>0,"Invalid operation");
        return db.query("""
            select op.*, r.id as reversed_by, o.document_id, p.reference, d.data_emissao, d.data_vencimento,
                   c.id as component_id,c.kind as component_kind,c.amount,c.reversed,c.reverse_of as reverse_component
            from financial_operation op
            left join financial_operation r on r.reverse_of=op.id
            left join financial_position p on p.creation_id=op.id
            left join financial_document_origin o on o.position_id=p.id
            left join documento_comercial d on d.id=o.document_id
            left join financial_component c on c.operation_id=op.id
            where op.empresa_id=? and op.cliente_id=? and op.moeda_id=? and op.id=? order by c.id
            """,rs -> {
                require(rs.next(),"Operation outside scope");
                long opId=rs.getLong("id"); var kind=OperationKind.valueOf(rs.getString("kind"));
                String actor=rs.getString("actor"),reason=rs.getString("reason"); var at=rs.getTimestamp("created_at").toInstant();
                Long reverse=rs.getObject("reverse_of",Long.class), reversedBy=rs.getObject("reversed_by",Long.class);
                var origin=document(rs); var components=new ArrayList<Component>();
                do { if(rs.getObject("component_id")!=null) components.add(new Component(rs.getLong("component_id"),
                        ComponentKind.valueOf(rs.getString("component_kind")),rs.getBigDecimal("amount"),
                        rs.getBoolean("reversed"),rs.getObject("reverse_component",Long.class))); } while(rs.next());
                return new OperationView(opId,s,kind,actor,reason,at,reverse,reversedBy,origin,
                        DocumentLinkStatus.NOT_RECORDED,components);
            },s.empresa(),s.cliente(),s.moeda(),id);
    }
    @Override public Balance balanceAt(Scope s, LocalDate financialDate) {
        scope(s); require(financialDate!=null,"Financial date required");
        throw new UnsupportedOperationException("Financial-date balances suspended: settlement documentary links and reversal effect semantics are not defined (TUULI-FIN-TIME-001)");
    }
}
