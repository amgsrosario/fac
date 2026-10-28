package com.ar2lda.fac.demo;

import com.ar2lda.fac.service.QrCodeImageService;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.detector.Detector;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Properties;
import javax.imageio.ImageIO;

/** Offline package smoke entry point. Does not start Spring or access a database. */
public final class QrJarSmoke {
    private QrJarSmoke() { }
    public static void main(String[] args) throws Exception {
        for (String artifact : new String[]{"core","javase"}) {
            var properties=new Properties();
            try (var in=QrJarSmoke.class.getResourceAsStream("/META-INF/maven/com.google.zxing/"+artifact+"/pom.properties")) {
                if (in==null) throw new IllegalStateException("ZXing dependency metadata missing");
                properties.load(in);
            }
            if (!"3.5.3".equals(properties.getProperty("version"))) throw new IllegalStateException("ZXing version must remain 3.5.3");
        }
        String payload="TUULI-DEMO-1\nDEMONSTRAÇÃO — SEM VALIDADE FISCAL";
        String uri=new QrCodeImageService().toPngDataUri(payload);
        var image=ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(uri.substring(uri.indexOf(',')+1))));
        var matrix=new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))).getBlackMatrix();
        var detected=new Detector(matrix).detect();var result=new Decoder().decode(detected.getBits());
        if (!payload.equals(result.getText()) || !"M".equals(result.getECLevel())
                || result.getByteSegments()==null || result.getByteSegments().size()!=1
                || (detected.getBits().getWidth()-17)/4<9) throw new IllegalStateException("QR packaged smoke failed");
        System.out.println("H205_QR_JAR_SMOKE_OK ZXing=3.5.3 Byte ECC=M version>=9");
    }
}
