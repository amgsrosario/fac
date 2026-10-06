package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import javax.xml.stream.*;
import java.io.*;
import java.util.zip.ZipInputStream;

/** Admission pass with bounded expansion, before POI builds its object model. */
final class XlsxUploadGuard {
    static final long MAX_ENTRY_BYTES = 32L * 1024 * 1024;
    static final long MAX_EXPANDED_BYTES = 64L * 1024 * 1024;
    private XlsxUploadGuard() {}

    static void check(byte[] bytes, int maxRows, int maxColumns) {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setXMLResolver((publicId, systemId, baseUri, namespace) -> { throw new XMLStreamException("Referencia externa recusada"); });
        long[] total = {0};
        int entries = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (++entries > 256) throw invalid();
                var input = new LimitedEntryStream(zip, total);
                var content = new PushbackInputStream(input, 4);
                int first;
                do { first = content.read(); } while (first == ' ' || first == '\t' || first == '\r' || first == '\n');
                if (first != -1) content.unread(first);
                byte[] prefix = content.readNBytes(4);
                content.unread(prefix);
                // OPC part names/extensions are arbitrary; inspect XML content, not conventional paths.
                boolean xml = prefix.length > 0 && (prefix[0] == '<' || (prefix[0] & 255) == 239
                        || (prefix[0] & 255) == 254 || (prefix[0] & 255) == 255
                        || prefix[0] == 0 && prefix.length > 1 && (prefix[1] == 0 || prefix[1] == '<' || Character.isWhitespace(prefix[1])));
                // XML's standard EBCDIC signature (<?xml); no path convention is required.
                xml |= prefix.length == 4 && prefix[0] == 0x4c && prefix[1] == 0x6f
                        && (prefix[2] & 255) == 0xa7 && (prefix[3] & 255) == 0x94;
                if (xml || entry.getName().endsWith(".xml")) {
                    XMLStreamReader reader = factory.createXMLStreamReader(content);
                    try {
                        int rows = 0, columns = 0;
                        boolean rootSeen = false, worksheet = false;
                        while (reader.hasNext()) {
                            int event = reader.next();
                            if (event == XMLStreamConstants.DTD) throw invalid();
                            if (event != XMLStreamConstants.START_ELEMENT) continue;
                            if (!rootSeen) { rootSeen = true; worksheet = "worksheet".equals(reader.getLocalName()); }
                            if (!worksheet) continue;
                            if ("row".equals(reader.getLocalName())) {
                                columns = 0;
                                String index = reader.getAttributeValue(null, "r");
                                if (++rows > maxRows + 1 || index != null && Long.parseLong(index) > maxRows + 1) throw invalid();
                            } else if ("c".equals(reader.getLocalName())) {
                                String ref = reader.getAttributeValue(null, "r");
                                if (++columns > maxColumns || ref != null && column(ref) > maxColumns) throw invalid();
                            }
                        }
                    } finally { reader.close(); }
                }
                byte[] buffer = new byte[8192];
                while (input.read(buffer) != -1) { /* Count every expanded entry, including non-sheet content. */ }
                zip.closeEntry();
            }
            if (entries == 0) throw invalid();
        } catch (IOException | XMLStreamException | NumberFormatException exception) {
            throw invalid();
        }
    }

    private static int column(String ref) {
        if (ref.length() > 32) throw invalid();
        int column = 0;
        for (int i = 0; i < ref.length() && Character.isLetter(ref.charAt(i)); i++) {
            char ch = ref.charAt(i);
            if (ch < 'A' || ch > 'Z' || column > 100) throw invalid();
            column = column * 26 + ch - 'A' + 1;
        }
        return column;
    }
    private static BadRequestException invalid() { return new BadRequestException("XLSX invalido ou excede limites estruturais/descompressao"); }
    private static final class LimitedEntryStream extends FilterInputStream {
        private final long[] total;
        private long size;
        private LimitedEntryStream(InputStream input, long[] total) { super(input); this.total = total; }
        private void count(int n) {
            if (n > 0) { size += n; total[0] += n; if (size > MAX_ENTRY_BYTES || total[0] > MAX_EXPANDED_BYTES) throw invalid(); }
        }
        @Override public int read() throws IOException { int n = in.read(); count(n == -1 ? 0 : 1); return n; }
        @Override public int read(byte[] b, int off, int len) throws IOException { int n = in.read(b, off, len); count(n); return n; }
        @Override public void close() { /* The ZIP owns the stream; XML readers must not close it. */ }
    }
}
