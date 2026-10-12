package com.ar2lda.fac.financial;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
class FinancialCoreMoneyTests {
 @Test void usesExistingHalfUpPolicy(){assertThat(FinancialCore.money(new BigDecimal("0.335"))).isEqualByComparingTo("0.34");}
 @Test void rejectsInvalidBeforeRounding(){assertThatThrownBy(()->FinancialCore.money(new BigDecimal("-0.001"))).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->FinancialCore.money(null)).isInstanceOf(RuntimeException.class);assertThatThrownBy(()->FinancialCore.money(new BigDecimal("0.0000001"))).isInstanceOf(RuntimeException.class);}
}
