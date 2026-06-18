package com.ar2lda.fac.service;

import com.ar2lda.fac.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class QrFiscalValidator {

    public static final String PAYLOAD_VERSION = "AT-QR-1.1";

    private static final Pattern MONEY = Pattern.compile("-?\\d+\\.\\d{2}");
    private static final Pattern DATE = Pattern.compile("\\d{8}");
    private static final Pattern CERTIFICATE = Pattern.compile("\\d{1,4}");
    private static final Set<String> MONEY_FIELDS = Set.of(
            "I2", "I3", "I4", "I5", "I6", "I7", "I8",
            "J2", "J3", "J4", "J5", "J6", "J7", "J8",
            "K2", "K3", "K4", "K5", "K6", "K7", "K8",
            "L", "M", "N", "O", "P"
    );
    private static final Map<String, Integer> ORDER = buildOrder();

    public void validate(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new BadRequestException("Payload QR fiscal é obrigatório");
        }
        if (payload.contains("**") || payload.endsWith("*") || payload.startsWith("*")) {
            throw new BadRequestException("Payload QR fiscal tem separadores inválidos");
        }

        Map<String, String> fields = parse(payload);
        require(fields, "A");
        require(fields, "B");
        require(fields, "C");
        require(fields, "D");
        require(fields, "E");
        require(fields, "F");
        require(fields, "G");
        require(fields, "H");
        require(fields, "I1");
        require(fields, "N");
        require(fields, "O");
        require(fields, "Q");
        require(fields, "R");

        validateLength(fields, "A", 9);
        validateLength(fields, "B", 30);
        validateLength(fields, "C", 12);
        validateLength(fields, "D", 2);
        validateLength(fields, "E", 1);
        validateLength(fields, "F", 8);
        validateLength(fields, "G", 60);
        validateLength(fields, "H", 70);
        validateLength(fields, "I1", 5);
        validateLength(fields, "Q", 4);
        validateLength(fields, "R", 4);

        if (!DATE.matcher(fields.get("F")).matches()) {
            throw new BadRequestException("Campo F do QR fiscal deve usar o formato YYYYMMDD");
        }
        if (fields.get("Q").length() != 4 || fields.get("Q").contains("*")) {
            throw new BadRequestException("Campo Q do QR fiscal deve conter 4 caracteres do hash");
        }
        if (!CERTIFICATE.matcher(fields.get("R")).matches()) {
            throw new BadRequestException("Campo R do QR fiscal deve conter o número do certificado");
        }

        fields.forEach((code, value) -> {
            if (MONEY_FIELDS.contains(code) && !MONEY.matcher(value).matches()) {
                throw new BadRequestException("Campo " + code + " do QR fiscal deve ter duas casas decimais");
            }
            if ("S".equals(code) && value.contains("*")) {
                throw new BadRequestException("Campo S do QR fiscal não pode conter asterisco");
            }
        });
    }

    private Map<String, String> parse(String payload) {
        Map<String, String> fields = new LinkedHashMap<>();
        int previousOrder = -1;
        for (String token : payload.split("\\*")) {
            int separator = token.indexOf(':');
            if (separator <= 0 || separator == token.length() - 1) {
                throw new BadRequestException("Campo QR fiscal inválido: " + token);
            }
            String code = token.substring(0, separator);
            String value = token.substring(separator + 1);
            Integer currentOrder = ORDER.get(code);
            if (currentOrder == null) {
                throw new BadRequestException("Campo QR fiscal desconhecido: " + code);
            }
            if (currentOrder <= previousOrder) {
                throw new BadRequestException("Campos do QR fiscal fora da ordem oficial");
            }
            if (fields.put(code, value) != null) {
                throw new BadRequestException("Campo QR fiscal repetido: " + code);
            }
            previousOrder = currentOrder;
        }
        return fields;
    }

    private void require(Map<String, String> fields, String code) {
        if (!fields.containsKey(code) || fields.get(code).isBlank()) {
            throw new BadRequestException("Campo " + code + " do QR fiscal é obrigatório");
        }
    }

    private void validateLength(Map<String, String> fields, String code, int maxLength) {
        String value = fields.get(code);
        if (value != null && value.length() > maxLength) {
            throw new BadRequestException("Campo " + code + " do QR fiscal excede o tamanho máximo");
        }
    }

    private static Map<String, Integer> buildOrder() {
        String[] codes = {
                "A", "B", "C", "D", "E", "F", "G", "H",
                "I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8",
                "J1", "J2", "J3", "J4", "J5", "J6", "J7", "J8",
                "K1", "K2", "K3", "K4", "K5", "K6", "K7", "K8",
                "L", "M", "N", "O", "P", "Q", "R", "S"
        };
        Map<String, Integer> order = new LinkedHashMap<>();
        for (int i = 0; i < codes.length; i++) {
            order.put(codes[i], i);
        }
        return order;
    }
}
