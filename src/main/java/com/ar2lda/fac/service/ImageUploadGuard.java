package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

final class ImageUploadGuard {
    private ImageUploadGuard() {}
    static void check(byte[] bytes, String expected, int maxDimension) {
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid();
            var reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName();
                if (!(expected.equals("png") ? format.equalsIgnoreCase("png")
                        : format.equalsIgnoreCase("jpeg") || format.equalsIgnoreCase("jpg"))) throw invalid();
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > maxDimension || height > maxDimension)
                    throw new BadRequestException("Logotipo excede as dimensoes maximas");
                if (reader.read(0) == null) throw invalid();
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException exception) { throw invalid(); }
    }
    private static BadRequestException invalid() { return new BadRequestException("Logotipo invalido"); }
}
