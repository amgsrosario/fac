package com.ar2lda.fac.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class QrCodeImageService {

    private static final int SIZE_PIXELS = 170;

    public String toPngDataUri(String payload) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "ISO-8859-1",
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, SIZE_PIXELS, SIZE_PIXELS, hints);
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar a imagem do QR Code fiscal", exception);
        }
    }
}
