package com.google.zxing.qrcode.encoder;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Mode;
import com.google.zxing.qrcode.decoder.Version;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Application bridge to ZXing 3.5.3's existing encoding primitives.
 * QRCodeWriter has no force-Byte hint; no library classes are replaced or copied.
 * Keep the dependency pinned and verify the encoded stream when upgrading ZXing.
 */
public final class FiscalByteQrEncoder {
    private FiscalByteQrEncoder() { }

    public static QRCode encode(String content) throws WriterException {
        Charset charset = StandardCharsets.ISO_8859_1.newEncoder().canEncode(content)
                ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8;
        if (!charset.newEncoder().canEncode(content)) throw new WriterException("Invalid fiscal QR text encoding");
        byte[] bytes = content.getBytes(charset);
        ErrorCorrectionLevel level = ErrorCorrectionLevel.M;
        for (int number = 9; number <= 40; number++) {
            Version version = Version.getVersionForNumber(number);
            int countBits = Mode.BYTE.getCharacterCountBits(version);
            if (bytes.length >= (1 << countBits)) continue;
            BitArray bits = new BitArray();
            if (charset.equals(StandardCharsets.UTF_8)) {
                bits.appendBits(Mode.ECI.getBits(), 4);
                bits.appendBits(CharacterSetECI.UTF8.getValue(), 8);
            }
            Encoder.appendModeInfo(Mode.BYTE, bits);
            Encoder.appendLengthInfo(bytes.length, version, Mode.BYTE, bits);
            for (byte value : bytes) bits.appendBits(value & 255, 8);
            if (!Encoder.willFit(bits.getSize(), version, level)) continue;
            var blocks = version.getECBlocksForLevel(level);
            int dataBytes = version.getTotalCodewords() - blocks.getTotalECCodewords();
            Encoder.terminateBits(dataBytes, bits);
            var interleaved = Encoder.interleaveWithECBytes(bits, version.getTotalCodewords(), dataBytes, blocks.getNumBlocks());
            int dimension = version.getDimensionForVersion(), bestMask = 0, bestPenalty = Integer.MAX_VALUE;
            ByteMatrix matrix = new ByteMatrix(dimension, dimension);
            for (int mask = 0; mask < QRCode.NUM_MASK_PATTERNS; mask++) {
                MatrixUtil.buildMatrix(interleaved, level, version, mask, matrix);
                int penalty = MaskUtil.applyMaskPenaltyRule1(matrix) + MaskUtil.applyMaskPenaltyRule2(matrix)
                        + MaskUtil.applyMaskPenaltyRule3(matrix) + MaskUtil.applyMaskPenaltyRule4(matrix);
                if (penalty < bestPenalty) { bestPenalty = penalty; bestMask = mask; }
            }
            MatrixUtil.buildMatrix(interleaved, level, version, bestMask, matrix);
            QRCode result = new QRCode();
            result.setECLevel(level); result.setMode(Mode.BYTE); result.setVersion(version);
            result.setMaskPattern(bestMask); result.setMatrix(matrix);
            return result;
        }
        throw new WriterException("Fiscal QR payload exceeds Byte/M capacity");
    }
}
