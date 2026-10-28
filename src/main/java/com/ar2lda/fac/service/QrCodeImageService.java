package com.ar2lda.fac.service;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.encoder.FiscalByteQrEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QrCodeImageService {

    public static final double PRINT_SIZE_MM = 40.0;
    public static final double MIN_QUIET_ZONE_MM = 2.5;
    private static final int PIXELS_PER_MODULE = 2;

    public String toPngDataUri(String payload) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var symbol = FiscalByteQrEncoder.encode(payload).getMatrix();
            int modules = symbol.getWidth();
            // At the shared 40mm PDF size, round the border up to whole modules.
            int border = (int) Math.ceil(MIN_QUIET_ZONE_MM * modules / (PRINT_SIZE_MM - 2 * MIN_QUIET_ZONE_MM));
            int size = (modules + 2 * border) * PIXELS_PER_MODULE;
            BitMatrix matrix = new BitMatrix(size);
            for (int y = 0; y < modules; y++) for (int x = 0; x < modules; x++) {
                if (symbol.get(x, y) == 1) matrix.setRegion((x + border) * PIXELS_PER_MODULE,
                        (y + border) * PIXELS_PER_MODULE, PIXELS_PER_MODULE, PIXELS_PER_MODULE);
            }
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar a imagem do QR Code fiscal", exception);
        }
    }
}
