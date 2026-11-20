package com.ar2lda.fac.fiscal.saft;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Internal laboratory only. No operational endpoint or production source for unresolved fiscal facts. */
@Service
@Profile("saft-ft-eur-lab")
public class SaftFtExport {
    public static final String NAMESPACE = "urn:OECD:StandardAuditFile-Tax:PT_1.04_01";

    public record Installation(LocalDate start, LocalDate end, LocalDate generated, ZoneId zone,
                               String taxEntity, String companyId, String producerTaxId, String productId,
                               String version, int certificate, Map<String,String> enabledTypes,
                               Set<String> disabledTypes, boolean supplierRequired) {
        public Installation {
            enabledTypes = Map.copyOf(enabledTypes);
            disabledTypes = Set.copyOf(disabledTypes);
        }
    }

    public record Signature(String hash, String keyVersion) {}
    public record LineFacts(LocalDate taxPointDate, String taxCode, String productType, String productNumberCode, BigDecimal headerDiscountShare) {}

    /** The caller supplies explicit synthetic evidence in this increment. No lookup defaults. */
    public interface Evidence {
        Signature signature(long invoice, String number);
        /** Resolve the documented historical key version independently of the candidate. */
        boolean matchesHistoricalKeyVersion(long invoice, String number, String candidate);
        /** Verify the candidate against the original signed payload and the historical public key. */
        boolean verifiesHash(long invoice, String number, Signature candidate);
        LineFacts line(long invoice, long line);
        boolean isOwnNormalVatInvoice(long invoice);
    }

    /** A caller must supply the pinned XSD 1.1 validator before any bytes are released. */
    public interface Validator {
        void validate(byte[] xml);
    }

    public record Result(byte[] xml, int count, BigDecimal totalCredit) {
        public Result { xml = xml.clone(); }
        @Override public byte[] xml() { return xml.clone(); }
    }

    public static final class Refusal extends IllegalStateException {
        public Refusal(String message) { super(message); }
    }

    private final JdbcTemplate db;

    public SaftFtExport(JdbcTemplate db) { this.db = db; }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public Result export(Installation installation, Evidence evidence, Validator validator) {
        check(installation != null && evidence != null && validator != null, "Installation, evidence and XSD validator required");
        var start = installation.start();
        var end = installation.end();
        check(start != null && end != null && installation.generated() != null && installation.zone() != null
                && !end.isBefore(start) && !end.isAfter(installation.generated())
                && end.isBefore(start.plusYears(1)), "Invalid fiscal period");
        var inventory = inventory(start, end);
        check(!installation.supplierRequired(), "Supplier: applicable family without a legitimate source");
        check(!installation.enabledTypes().isEmpty(), "Enabled documentary types required");
        installation.enabledTypes().forEach((code,type) ->
                check("FT".equals(type), "Enabled type without coverage: " + code + "/" + type));
        var coveredConfiguration = new HashSet<>(installation.enabledTypes().keySet());
        check(Collections.disjoint(coveredConfiguration,installation.disabledTypes()),
                "Type cannot be both enabled and disabled");
        coveredConfiguration.addAll(installation.disabledTypes());
        check(coveredConfiguration.equals(new HashSet<>(db.queryForList("select id from tipodocumento",String.class))),
                "Configured documentary types differ from the installation inventory");
        var configuredFt = db.queryForList("""
                select id from tipodocumento
                where codigo_fiscal='FT' and not liquidacao_imediata and fiscal_catalog_revision is not null
                """, String.class);
        check(configuredFt.containsAll(installation.enabledTypes().keySet()),
                "Enabled type is not a governed deferred FT");
        // Always inspect commercial and fiscal receipts before filtering; internal SETTLE is not a document.
        for (var item : inventory) {
            check("commercial".equals(item.family()) && "FT".equals(item.type()) && "EUR".equals(item.currency())
                    && "FT".equals(installation.enabledTypes().get(item.code()))
                    && Set.of("EMITIDO","ANULADO").contains(item.status()),
                    "Document without coverage: " + item);
            check(evidence.isOwnNormalVatInvoice(item.id()),"Unproved regime or provenance: invoice " + item.id());
        }

        List<Map<String,Object>> invoices = db.queryForList("""
                select * from documento_comercial
                where data_emissao between ? and ? and (estado<>'RASCUNHO' or numero_documento is not null)
                order by id
                """, start, end);
        check(invoices.stream().map(r -> id(r,"id")).toList().equals(inventory.stream().map(Item::id).toList()),
                "Inventory and extraction differ");
        var lines = db.queryForList("""
                select l.* from linha_documento_comercial l
                join documento_comercial d on d.id=l.id_documento_comercial
                where d.data_emissao between ? and ? and (d.estado<>'RASCUNHO' or d.numero_documento is not null)
                order by l.id_documento_comercial,l.numero_linha,l.id
                """, start, end);
        Map<Long,List<Map<String,Object>>> byInvoice = new LinkedHashMap<>();
        lines.forEach(l -> byInvoice.computeIfAbsent(id(l,"id_documento_comercial"), k -> new ArrayList<>()).add(l));

        Document xml = document();
        Element root = xml.createElementNS(NAMESPACE,"AuditFile");
        xml.appendChild(root);
        var issuer = db.queryForMap("select * from empresa where id=1");
        header(root,installation,issuer);
        Element master = node(root,"MasterFiles");
        Element source = node(root,"SourceDocuments");
        Element sales = node(source,"SalesInvoices");
        value(sales,"NumberOfEntries",invoices.size());
        value(sales,"TotalDebit",BigDecimal.ZERO);
        Element totalCredit = value(sales,"TotalCredit",BigDecimal.ZERO);

        Map<String,Customer> customers = new TreeMap<>();
        Map<String,Product> products = new TreeMap<>();
        Map<String,BigDecimal> rates = new TreeMap<>();
        Set<String> numbers = new HashSet<>();
        BigDecimal credit = BigDecimal.ZERO;
        for (var invoice : invoices) {
            long id = id(invoice,"id");
            String number = required(invoice,"numero_documento_completo");
            check(numbers.add(number),"Duplicate InvoiceNo: " + number);
            check("PT".equals(invoice.get("fiscal_jurisdicao"))
                    && "CONTINENTE".equals(invoice.get("fiscal_territorio_fiscal"))
                    && "NORMAL".equals(invoice.get("fiscal_tratamento_liquidacao"))
                    && decimal(invoice,"valor_retencao").signum()==0 && !Boolean.TRUE.equals(invoice.get("liquidado")),
                    number + ": unproved regime, withholding or settlement");
            check(required(issuer,"nif").equals(required(invoice,"emitente_nif")),number + ": issuer mismatch");
            String customerId = required(invoice,"id_cliente");
            var customer = new Customer(required(invoice,"cliente_nif"),required(invoice,"cliente_nome"),
                    address(invoice,"cliente_morada","cliente_morada1"),required(invoice,"cliente_localidade"),
                    required(invoice,"cliente_cod_postal"),required(invoice,"cliente_pais"));
            check("PT".equals(customer.country()),number + ": customer jurisdiction outside the fixture");
            consistent(customers,customerId,customer,"CustomerID");
            var signature = evidence.signature(id,number);
            check(signature != null && present(signature.hash()) && present(signature.keyVersion()),
                    number + ": Hash/HashControl source missing");
            check(!"0".equals(signature.hash()) && !"0".equals(signature.keyVersion()),number + ": placeholder signature");
            check(evidence.matchesHistoricalKeyVersion(id,number,signature.keyVersion()),
                    number + ": HashControl does not match the historical key version");
            check(evidence.verifiesHash(id,number,signature),number + ": Hash does not verify against original evidence");
            List<Map<String,Object>> invoiceLines = byInvoice.getOrDefault(id,List.of());
            check(!invoiceLines.isEmpty(),number + ": no lines");
            Element item = invoice(sales,invoice,installation,signature,number,customerId);
            BigDecimal net = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal gross = BigDecimal.ZERO;
            Set<Long> lineNumbers = new HashSet<>();
            for (var row : invoiceLines) {
                long lineNumber = id(row,"numero_linha");
                check(lineNumbers.add(lineNumber),number + ": duplicate LineNumber " + lineNumber);
                var facts = evidence.line(id,id(row,"id"));
                check(facts != null && facts.taxPointDate()!=null,number + ": TaxPointDate missing on line " + lineNumber);
                BigDecimal amount = decimal(row,"base_tributavel");
                BigDecimal vat = decimal(row,"valor_imposto");
                writeLine(item,row,facts,products,rates);
                net = net.add(amount);
                tax = tax.add(vat);
                discount = discount.add(decimal(row,"valor_desconto")).add(headerShare(facts));
                gross = gross.add(decimal(row,"valor_bruto"));
            }
            same(discount,decimal(invoice,"valor_desconto"),number + ": unapportioned discount");
            same(gross,decimal(invoice,"valor_bruto"),number + ": gross before discounts mismatch");
            same(tax,decimal(invoice,"valor_iva_total"),number + ": TaxPayable mismatch");
            same(tax.add(net),decimal(invoice,"valor_total"),number + ": GrossTotal mismatch");
            var totals = node(item,"DocumentTotals");
            value(totals,"TaxPayable",tax);
            value(totals,"NetTotal",net);
            value(totals,"GrossTotal",net.add(tax));
            if (!"ANULADO".equals(invoice.get("estado"))) credit = credit.add(net);
        }
        totalCredit.setTextContent(credit.toPlainString());
        masters(master,customers,products,rates);
        byte[] bytes = serialize(xml);
        validator.validate(bytes.clone());
        return new Result(bytes,invoices.size(),credit);
    }

    private record Item(String family,long id,String code,String type,String currency,String status) {}

    private List<Item> inventory(LocalDate start, LocalDate end) {
        return db.query("""
                select 'commercial' as family,id,id_tipo_documento as code,
                    coalesce(projection_external_type,tipo_documento_codigo_fiscal) as fiscal_type,
                    moeda_codigo as currency,estado as status
                from documento_comercial where data_emissao between ? and ?
                    and (estado<>'RASCUNHO' or numero_documento is not null)
                union all
                select 'financial',id,id_tipo_documento,null,id_moeda,case when anulado then 'ANULADO' else 'EMITIDO' end
                from documento_financeiro where data_emissao between ? and ?
                order by family,id
                """,(rs,n) -> new Item(rs.getString(1),rs.getLong(2),rs.getString(3),rs.getString(4),
                rs.getString(5),rs.getString(6)),start,end,start,end);
    }

    private static void header(Element root, Installation i, Map<String,Object> issuer) {
        var h = node(root,"Header");
        value(h,"AuditFileVersion","1.04_01");
        value(h,"CompanyID",i.companyId());
        value(h,"TaxRegistrationNumber",required(issuer,"nif"));
        value(h,"TaxAccountingBasis","F");
        value(h,"CompanyName",required(issuer,"nome"));
        address(h,"CompanyAddress",address(issuer,"morada","morada1"),required(issuer,"localidade"),
                required(issuer,"id_codpostal"),required(issuer,"id_pais"));
        value(h,"FiscalYear",i.start().getYear());
        value(h,"StartDate",i.start());
        value(h,"EndDate",i.end());
        value(h,"CurrencyCode","EUR");
        value(h,"DateCreated",i.generated());
        value(h,"TaxEntity",i.taxEntity());
        value(h,"ProductCompanyTaxID",i.producerTaxId());
        check(i.certificate()>0,"Synthetic certificate assertion required");
        value(h,"SoftwareCertificateNumber",i.certificate());
        value(h,"ProductID",i.productId());
        value(h,"ProductVersion",i.version());
        value(h,"HeaderComment","SYNTHETIC TEST ONLY");
    }

    private static Element invoice(Element sales,Map<String,Object> r,Installation i,Signature s,String number,String customerId) {
        boolean voided = "ANULADO".equals(r.get("estado"));
        check(voided == Boolean.TRUE.equals(r.get("anulado")),number + ": inconsistent document status");
        String systemEntry = timestamp(r,"momento_emissao",i,"SystemEntryDate",number);
        var inv = node(sales,"Invoice");
        value(inv,"InvoiceNo",number);
        value(inv,"ATCUD",required(r,"atcud"));
        var status = node(inv,"DocumentStatus");
        value(status,"InvoiceStatus",voided ? "A" : "N");
        value(status,"InvoiceStatusDate",timestamp(r,voided ? "data_hora_anulacao" : "momento_emissao",i,"InvoiceStatusDate",number));
        if (voided) value(status,"Reason",required(r,"motivo_anulacao"));
        value(status,"SourceID",required(r,voided ? "anulado_por_utilizador_id" : "id_emissor"));
        value(status,"SourceBilling","P");
        value(inv,"Hash",s.hash());
        value(inv,"HashControl",s.keyVersion());
        value(inv,"InvoiceDate",r.get("data_emissao"));
        value(inv,"InvoiceType","FT");
        var regimes = node(inv,"SpecialRegimes");
        value(regimes,"SelfBillingIndicator",0);
        value(regimes,"CashVATSchemeIndicator",0);
        value(regimes,"ThirdPartiesBillingIndicator",0);
        value(inv,"SourceID",required(r,"id_emissor"));
        value(inv,"SystemEntryDate",systemEntry);
        value(inv,"CustomerID",customerId);
        return inv;
    }

    private record Customer(String nif,String name,String address,String city,String postal,String country) {}
    private record Product(String type,String description,String number) {}

    private static void writeLine(Element invoice,Map<String,Object> row,LineFacts facts,
                                  Map<String,Product> products,Map<String,BigDecimal> rates) {
        String code = required(row,"artigo_codigo");
        String description = required(row,"descricao");
        check("COMERCIAL".equals(row.get("tipo_linha")) && "NORMAL".equals(row.get("fiscal_tratamento_liquidacao")),
                "Unsupported fiscal line: " + row.get("id"));
        check(Set.of("P","S").contains(facts.productType()) && present(facts.productNumberCode()),
                "Product classification/number missing: " + code);
        String taxCode = switch (required(row,"tipo_taxa_iva_codigo")) {
            case "NORMAL" -> "NOR";
            case "INTERMEDIA" -> "INT";
            case "REDUZIDA" -> "RED";
            default -> throw new Refusal("Unsupported consolidated tax category: " + code);
        };
        check(taxCode.equals(facts.taxCode()),"TaxCode missing or inconsistent: " + code);
        var q = decimal(row,"quantidade");
        var net = decimal(row,"base_tributavel");
        var vat = decimal(row,"valor_imposto");
        var rate = decimal(row,"percentagem_iva");
        var discount = decimal(row,"valor_desconto").add(headerShare(facts));
        check(q.signum()>0 && net.signum()>=0 && rate.signum()>0 && discount.signum()>=0,"Invalid fiscal line amounts");
        same(net,decimal(row,"valor_bruto").subtract(discount),"Discount reconciliation: " + code);
        same(net.add(vat),decimal(row,"total_linha"),"Line gross reconciliation: " + code);
        same(vat,net.multiply(rate).movePointLeft(2).setScale(2,RoundingMode.HALF_UP),"VAT reconciliation: " + code);
        consistent(products,code,new Product(facts.productType(),description,facts.productNumberCode()),"ProductCode");
        consistent(rates,facts.taxCode(),rate.stripTrailingZeros(),"TaxCode");
        var line = node(invoice,"Line");
        value(line,"LineNumber",row.get("numero_linha"));
        value(line,"ProductCode",code);
        value(line,"ProductDescription",description);
        value(line,"Quantity",q);
        value(line,"UnitOfMeasure",required(row,"unidade"));
        value(line,"UnitPrice",unitPrice(net,q));
        value(line,"TaxPointDate",facts.taxPointDate());
        value(line,"Description",description);
        value(line,"CreditAmount",net);
        var tax = node(line,"Tax");
        value(tax,"TaxType","IVA");
        value(tax,"TaxCountryRegion","PT");
        value(tax,"TaxCode",facts.taxCode());
        value(tax,"TaxPercentage",rate);
        if (discount.signum()>0) value(line,"SettlementAmount",discount);
    }

    private static BigDecimal headerShare(LineFacts facts) {
        BigDecimal share = facts.headerDiscountShare();
        check(share == null || share.signum() >= 0,"Invalid header discount allocation");
        return share == null ? BigDecimal.ZERO : share;
    }

    private static BigDecimal unitPrice(BigDecimal net,BigDecimal quantity) {
        try {
            return net.divide(quantity).stripTrailingZeros();
        } catch (ArithmeticException periodic) {
            return net.divide(quantity,12,RoundingMode.HALF_UP).stripTrailingZeros();
        }
    }

    private static void masters(Element master,Map<String,Customer> customers,Map<String,Product> products,Map<String,BigDecimal> rates) {
        customers.forEach((id,c) -> {
            var e = node(master,"Customer");
            value(e,"CustomerID",id);
            value(e,"AccountID","Desconhecido");
            value(e,"CustomerTaxID",c.nif());
            value(e,"CompanyName",c.name());
            address(e,"BillingAddress",c.address(),c.city(),c.postal(),c.country());
            value(e,"SelfBillingIndicator",0);
        });
        products.forEach((code,p) -> {
            var e = node(master,"Product");
            value(e,"ProductType",p.type());
            value(e,"ProductCode",code);
            value(e,"ProductDescription",p.description());
            value(e,"ProductNumberCode",p.number());
        });
        if (!rates.isEmpty()) {
            var taxTable = node(master,"TaxTable");
            rates.forEach((code,rate) -> {
                var e = node(taxTable,"TaxTableEntry");
                value(e,"TaxType","IVA");
                value(e,"TaxCountryRegion","PT");
                value(e,"TaxCode",code);
                value(e,"Description","IVA " + code);
                value(e,"TaxPercentage",rate);
            });
        }
    }

    private static String address(Map<String,Object> row,String first,String second) {
        return required(row,first) + (row.get(second) instanceof String extra && present(extra) ? " " + extra : "");
    }

    private static void address(Element parent,String name,String detail,String city,String postal,String country) {
        var address = node(parent,name);
        value(address,"AddressDetail",detail);
        value(address,"City",city);
        value(address,"PostalCode",postal);
        value(address,"Country",country);
    }

    private static Document document() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create fiscal document",e);
        }
    }

    private static byte[] serialize(Document xml) {
        try {
            var factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
            var output = new ByteArrayOutputStream();
            factory.newTransformer().transform(new DOMSource(xml),new StreamResult(output));
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize fiscal document",e);
        }
    }

    private static Element node(Element parent,String name) {
        var child = parent.getOwnerDocument().createElementNS(NAMESPACE,name);
        parent.appendChild(child);
        return child;
    }

    private static Element value(Element parent,String name,Object v) {
        check(v!=null && present(v.toString()),parent.getTagName() + "/" + name + ": source missing");
        var e=node(parent,name);
        e.setTextContent(v instanceof BigDecimal amount ? amount.toPlainString() : v.toString());
        return e;
    }

    private static long id(Map<String,Object> r,String field) { return ((Number)r.get(field)).longValue(); }
    private static BigDecimal decimal(Map<String,Object> r,String field) {
        if (r.get(field) instanceof BigDecimal d) return d;
        throw new Refusal(field + ": monetary source missing");
    }
    private static String required(Map<String,Object> r,String field) {
        var value = r.get(field);
        check(value!=null && present(value.toString()),field + ": source missing");
        return value.toString();
    }
    private static String timestamp(Map<String,Object> r,String field,Installation i,String xmlField,String number) {
        check(r.get(field) instanceof Timestamp,field + ": timestamp source missing");
        var local = ((Timestamp)r.get(field)).toInstant().atZone(i.zone()).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
        check(!local.toLocalDate().isAfter(i.generated()),number + ": " + xmlField + " after DateCreated");
        return java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss").format(local);
    }
    private static <T> void consistent(Map<String,T> map,String key,T candidate,String field) {
        var previous=map.putIfAbsent(key,candidate);
        check(previous==null || previous.equals(candidate),field + " conflicts: " + key);
    }
    private static boolean present(String value) { return value!=null && !value.isBlank(); }
    private static void same(BigDecimal a,BigDecimal b,String message) { check(a.compareTo(b)==0,message); }
    private static void check(boolean ok,String message) { if (!ok) throw new Refusal(message); }
}
