package com.ar2lda.fac.fiscal.signingtrial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/** Experimental snapshot only. No production DTO, entity or controller accepts this type. */
public record TrialCanonical(UUID document, String fiscalType, String currency, boolean immediate,
                             LocalDate invoiceDate, LocalDateTime systemEntryDate, BigDecimal grossTotal) {
    public TrialCanonical {
        Objects.requireNonNull(document); Objects.requireNonNull(invoiceDate);
        Objects.requireNonNull(systemEntryDate); Objects.requireNonNull(grossTotal);
        if (!"FT".equals(fiscalType) || !"EUR".equals(currency) || immediate)
            throw new IllegalArgumentException("Trial supports deferred FT EUR only");
        if (systemEntryDate.getNano() != 0 || grossTotal.signum() < 0 || invoiceDate.isAfter(systemEntryDate.toLocalDate()))
            throw new IllegalArgumentException("Invalid trial chronology or amount");
        // Refuse precision loss rather than inventing a rounding rule.
        grossTotal = grossTotal.setScale(2, RoundingMode.UNNECESSARY);
        if (grossTotal.precision() > 18) throw new IllegalArgumentException("Amount too large");
    }
    public String bytesText(String invoiceNumber, String predecessor) {
        if (invoiceNumber == null || !invoiceNumber.matches("FT H207T[A-Z0-9]{1,24}/[1-9][0-9]*"))
            throw new IllegalArgumentException("Not a new trial series");
        if (predecessor == null || (!predecessor.isEmpty() && !predecessor.matches("[A-Za-z0-9+/]{171}=")))
            throw new IllegalArgumentException("Invalid predecessor");
        return invoiceDate + ";" + systemEntryDate.format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"))
                + ";" + invoiceNumber + ";" + grossTotal.toPlainString() + ";" + predecessor;
    }
}
