package com.ar2lda.fac.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.detector.Detector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class QrCodeImageServiceTests {
    private final QrCodeImageService service = new QrCodeImageService();

    @ParameterizedTest
    @ValueSource(strings = {"A", "1234567890", "ABC123:*", "A:500000000*D:FT*G:FT TEST/1", "Texto ç ã € 日本"})
    void byteModeMMinimumVersionAndPhysicalBorder(String payload) throws Exception {
        verify(payload);
    }

    @Test void largePayloadIncreasesVersionWithoutShrinkingPhysicalMargin() throws Exception {
        verify("A:" + "X".repeat(1500));
    }

    private void verify(String payload) throws Exception {
        var image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(service.toPngDataUri(payload).split(",", 2)[1])));
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        var black = bitmap.getBlackMatrix();
        var detected = new Detector(black).detect();
        int modules = detected.getBits().getWidth();
        assertThat((modules - 17) / 4).isGreaterThanOrEqualTo(9);
        var decoded = new Decoder().decode(detected.getBits());
        assertThat(decoded.getText()).isEqualTo(payload);
        assertThat(decoded.getECLevel()).isEqualTo("M");
        assertThat(decoded.getByteSegments()).hasSize(1); // Actual stream, not encoder metadata.
        int[] bounds = black.getEnclosingRectangle();
        double scale = QrCodeImageService.PRINT_SIZE_MM / image.getWidth();
        for (int border : new int[]{bounds[0], bounds[1], image.getWidth()-bounds[0]-bounds[2], image.getHeight()-bounds[1]-bounds[3]})
            assertThat(border * scale).isGreaterThanOrEqualTo(2.5);
        assertThat(bounds[2] * scale).isGreaterThanOrEqualTo(30.0);
        assertThat(bounds[3] * scale).isGreaterThanOrEqualTo(30.0);
    }

    @Test void absenceIsNotAnInventedCode() { assertThat(service.toPngDataUri(null)).isEmpty(); assertThat(service.toPngDataUri(" ")).isEmpty(); }
    @Test void capacityOverflowIsRefused() {
        assertThatThrownBy(() -> service.toPngDataUri("A".repeat(4000))).isInstanceOf(IllegalStateException.class);
    }
}
