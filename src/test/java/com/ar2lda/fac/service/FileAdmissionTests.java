package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileAdmissionTests {
    private byte[] zip(String name, byte[] data) throws Exception {
        var out=new ByteArrayOutputStream();
        try(var zip=new ZipOutputStream(out)) {zip.putNextEntry(new ZipEntry(name));zip.write(data);zip.closeEntry();}
        return out.toByteArray();
    }
    @Test void csvLimitsRejectBeforeMalformedTailIsParsed() {
        var parser=mock(DadosMestresTransferService.class,CALLS_REAL_METHODS);
        String manyRows="h\n"+"v\n".repeat(10_001)+"\"unterminated";
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(parser,"parseCsv",(Object)manyRows.getBytes()))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("linhas");
        String columns="h;".repeat(100)+"v\n\"unterminated";
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(parser,"parseCsv",(Object)columns.getBytes()))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("colunas");
        assertThatCode(() -> ReflectionTestUtils.invokeMethod(parser,"parseCsv",(Object)("h\n"+"v\n".repeat(10_000)).getBytes()))
                .doesNotThrowAnyException();
    }
    @Test void worksheetLimitsRejectBeforeMalformedTailAndAcceptBoundary() throws Exception {
        byte[] rows=zip("xl/worksheets/sheet1.xml","<worksheet><row r='10002'/><broken".getBytes());
        assertThatThrownBy(() -> XlsxUploadGuard.check(rows,10000,100)).isInstanceOf(BadRequestException.class);
        byte[] cols=zip("xl/worksheets/sheet1.xml","<worksheet><row r='1'><c r='CW1'/><broken".getBytes());
        assertThatThrownBy(() -> XlsxUploadGuard.check(cols,10000,100)).isInstanceOf(BadRequestException.class);
        byte[] valid=zip("xl/worksheets/sheet1.xml","<worksheet><row r='10001'><c r='CV10001'/></row></worksheet>".getBytes());
        assertThatCode(() -> XlsxUploadGuard.check(valid,10000,100)).doesNotThrowAnyException();
    }
    @Test void nonCanonicalWorksheetNamesAndExtensionsCannotBypassStructuralLimits() throws Exception {
        for(String name : new String[]{"xl/custom.xml","xl/custom.bin"}) {
            byte[] bytes=zip(name,"<worksheet><row r='10002'/><broken".getBytes());
            assertThatThrownBy(() -> XlsxUploadGuard.check(bytes,10000,100)).isInstanceOf(BadRequestException.class);
        }
        // A valid OPC workbook with a relocated worksheet is recognised by POI, but rejected by admission.
        for (String encoding : new String[]{"UTF-8","Cp037"}) {
        var workbookBytes=new ByteArrayOutputStream();
        try(var workbook=new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            workbook.createSheet("dados").createRow(10001).createCell(0).setCellValue("too many rows");
            workbook.write(workbookBytes);
        }
        var rewritten=new ByteArrayOutputStream();
        try(var source=new ZipInputStream(new ByteArrayInputStream(workbookBytes.toByteArray())); var target=new ZipOutputStream(rewritten)) {
            for(var entry=source.getNextEntry();entry!=null;entry=source.getNextEntry()) {
                String name=entry.getName();byte[] content=source.readAllBytes();
                if(name.equals("xl/worksheets/sheet1.xml")) {
                    name="xl/custom.bin";
                    content=new String(content,java.nio.charset.StandardCharsets.UTF_8).replace("UTF-8",encoding)
                            .getBytes(java.nio.charset.Charset.forName(encoding));
                }
                if(name.equals("[Content_Types].xml")) content=new String(content,java.nio.charset.StandardCharsets.UTF_8)
                        .replace("/xl/worksheets/sheet1.xml","/xl/custom.bin").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                if(name.equals("xl/_rels/workbook.xml.rels")) content=new String(content,java.nio.charset.StandardCharsets.UTF_8)
                        .replace("worksheets/sheet1.xml","custom.bin").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                target.putNextEntry(new ZipEntry(name));target.write(content);target.closeEntry();
            }
        }
        byte[] relocated=rewritten.toByteArray();
        try(var workbook=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new ByteArrayInputStream(relocated))) {
            assertThat(workbook.getSheetAt(0).getLastRowNum()).isEqualTo(10001);
        }
        assertThatThrownBy(() -> XlsxUploadGuard.check(relocated,10000,100)).isInstanceOf(BadRequestException.class);
        }
    }

    @Test void expandedEntryIsBoundedWithSmallCompressedFixture() throws Exception {
        var out=new ByteArrayOutputStream();
        try(var zip=new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            byte[] chunk=new byte[8192];
            for(int i=0;i<=XlsxUploadGuard.MAX_ENTRY_BYTES/chunk.length;i++) zip.write(chunk);
            zip.closeEntry();
        }
        assertThat(out.size()).isLessThan(100_000);
        assertThatThrownBy(() -> XlsxUploadGuard.check(out.toByteArray(),10000,100)).isInstanceOf(BadRequestException.class);
    }
    @Test void archiveEntryCountAndXmlExternalEntitiesAreRejected() throws Exception {
        var out=new ByteArrayOutputStream();
        try(var zip=new ZipOutputStream(out)) {
            for(int i=0;i<257;i++) {zip.putNextEntry(new ZipEntry("part"+i));zip.closeEntry();}
        }
        assertThatThrownBy(() -> XlsxUploadGuard.check(out.toByteArray(),10000,100)).isInstanceOf(BadRequestException.class);
        byte[] xml=zip("xl/worksheets/sheet1.xml",("<!DOCTYPE worksheet [<!ENTITY x SYSTEM 'file:///must-not-read'>]><worksheet>&x;</worksheet>").getBytes());
        assertThatThrownBy(() -> XlsxUploadGuard.check(xml,10000,100)).isInstanceOf(BadRequestException.class);
    }
    @Test void oversizedImageHeaderRejectedBeforeMissingPixelsAndActualFormatChecked() throws Exception {
        var out=new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB),"png",out);
        byte[] valid=out.toByteArray();
        assertThatCode(() -> ImageUploadGuard.check(valid,"png",2000)).doesNotThrowAnyException();
        assertThatThrownBy(() -> ImageUploadGuard.check(valid,"jpeg",2000)).isInstanceOf(BadRequestException.class);
        byte[] header=java.util.Arrays.copyOf(valid,33);
        java.nio.ByteBuffer.wrap(header,16,4).putInt(2001);
        var crc=new CRC32();crc.update(header,12,17);java.nio.ByteBuffer.wrap(header,29,4).putInt((int)crc.getValue());
        assertThatThrownBy(() -> ImageUploadGuard.check(header,"png",2000)).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("dimensoes");
        assertThatThrownBy(() -> ImageUploadGuard.check("not an image".getBytes(),"png",2000)).isInstanceOf(BadRequestException.class);
    }
}
