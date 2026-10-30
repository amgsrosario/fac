package com.ar2lda.fac.fiscal.signingtrial;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import static org.assertj.core.api.Assertions.*;
class TrialCanonicalTests {
 static TrialCanonical sample() {return new TrialCanonical(UUID.randomUUID(),"FT","EUR",false,LocalDate.of(2026,10,30),LocalDateTime.of(2026,10,30,12,34,56),new BigDecimal("123.40"));}
 @Test void literalOracleIsIndependentOfLocale() {
  Locale old=Locale.getDefault();try {Locale.setDefault(Locale.GERMANY);
   assertThat(sample().bytesText("FT H207TTEST/1","").getBytes(StandardCharsets.UTF_8))
    .isEqualTo("2026-10-30;2026-10-30T12:34:56;FT H207TTEST/1;123.40;".getBytes(StandardCharsets.UTF_8));
  }finally{Locale.setDefault(old);}
 }
 @Test void specimenProfileVerifiedUsingExplicitPkcs1DigestInfoOracle() throws Exception {
  var signer=EphemeralTrialSigner.createForExperiment();String text=sample().bytesText("FT H207TTEST/1","");String signed=signer.sign(text);
  assertThat(signed).hasSize(172).doesNotContain("\n","\r");
  var key=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(signer.publicKey())));
  assertThat(((java.security.interfaces.RSAPublicKey)key).getModulus().bitLength()).isEqualTo(1024);
  var cipher=javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");cipher.init(javax.crypto.Cipher.DECRYPT_MODE,key);
  byte[] digestInfo=cipher.doFinal(Base64.getDecoder().decode(signed));
  byte[] expected=java.util.HexFormat.of().parseHex("3021300906052b0e03021a05000414");
  byte[] digest=MessageDigest.getInstance("SHA-1").digest(text.getBytes(StandardCharsets.UTF_8));
  assertThat(digestInfo).isEqualTo(java.nio.ByteBuffer.allocate(expected.length+digest.length).put(expected).put(digest).array());
  assertThat(EphemeralTrialSigner.verify(text+"x",signed,signer.publicKey())).isFalse();
  assertThat(EphemeralTrialSigner.verify(text,signed,EphemeralTrialSigner.createForExperiment().publicKey())).isFalse();
 }
 @ParameterizedTest @ValueSource(strings={"FR","RC","RG","RL","NC","ND"}) void refusesOtherFamilies(String type) {
  var s=sample();assertThatThrownBy(()->new TrialCanonical(s.document(),type,"EUR",false,s.invoiceDate(),s.systemEntryDate(),s.grossTotal())).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void refusesImmediateCurrencyPrecisionAndFutureDate() {
  var s=sample();
  assertThatThrownBy(()->new TrialCanonical(s.document(),"FT","USD",false,s.invoiceDate(),s.systemEntryDate(),s.grossTotal())).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->new TrialCanonical(s.document(),"FT","EUR",true,s.invoiceDate(),s.systemEntryDate(),s.grossTotal())).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->new TrialCanonical(s.document(),"FT","EUR",false,s.invoiceDate(),s.systemEntryDate(),new BigDecimal("1.001"))).isInstanceOf(ArithmeticException.class);
  assertThatThrownBy(()->new TrialCanonical(s.document(),"FT","EUR",false,s.invoiceDate().plusDays(1),s.systemEntryDate(),s.grossTotal())).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->s.bytesText("FT DEMO26/1","")).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void trialCannotBecomeAProductionSourceAndHasNoSpringRegistration() {
  assertThat(com.ar2lda.fac.service.FiscalDocumentSignatureSource.class.isAssignableFrom(TrialProofEngine.class)).isFalse();
  for(Class<?> type:List.of(TrialProofEngine.class,EphemeralTrialSigner.class,TrialCanonical.class))
   assertThat(org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation(type,org.springframework.stereotype.Component.class)).isFalse();
 }
 @Test void refusesProductionDatabaseWithoutConnectingToIt() throws Exception {
  var connection=org.mockito.Mockito.mock(java.sql.Connection.class);var metadata=org.mockito.Mockito.mock(java.sql.DatabaseMetaData.class);
  org.mockito.Mockito.when(connection.getMetaData()).thenReturn(metadata);
  org.mockito.Mockito.when(metadata.getURL()).thenReturn("jdbc:postgresql://127.0.0.1:5432/fac");
  var db=org.mockito.Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
  org.mockito.Mockito.when(db.execute(org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.ConnectionCallback<Void>>any()))
   .thenAnswer(call->call.<org.springframework.jdbc.core.ConnectionCallback<Void>>getArgument(0).doInConnection(connection));
  assertThatThrownBy(()->new TrialProofEngine(db,org.mockito.Mockito.mock(org.springframework.transaction.PlatformTransactionManager.class),EphemeralTrialSigner.createForExperiment()))
   .hasMessageContaining("disposable");
 }
}
