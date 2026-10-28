package com.ar2lda.fac.fiscal.catalog;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
class FtDeferredSemanticsTests {
 final ObjectMapper json=new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
 @Test void tuplesAreNotIndependentAttributeLists()throws Exception {
  var d=json.readTree(GovernedFtControllerTests.definition(1));
  assertThat(FtDeferredSemantics.permits(d,"FT",98,9,2,false)).isTrue();
  assertThat(FtDeferredSemantics.permits(d,"FT",97,8,1,false)).isTrue();
  assertThat(FtDeferredSemantics.permits(d,"FT",98,8,2,false)).isFalse();
  assertThat(FtDeferredSemantics.permits(d,"FT",98,9,2,true)).isFalse();
 }
 @ParameterizedTest @ValueSource(strings={"[]","{}","null","{\"unexpected\":true}","{\"allowedConfigurations\":[]}","{\"allowedConfigurations\":null}","{\"allowedConfigurations\":[null]}"})
 void malformedSemanticsFailClosed(String semantics)throws Exception {
  var d=json.readTree(GovernedFtControllerTests.definition(1));((com.fasterxml.jackson.databind.node.ObjectNode)d).set("semantics",json.readTree(semantics));
  assertThatThrownBy(()->FtDeferredSemantics.configurations(d)).isInstanceOf(IllegalStateException.class);
 }
 @ParameterizedTest @ValueSource(strings={"\"98\"","98.01","null","true","-1","100","123456789012345678901234567890"})
 void integerFieldsRejectCoercionOrLossOfPrecision(String value)throws Exception {
  var d=json.readTree(GovernedFtControllerTests.definition(1).replace("\"areaGestao\":98","\"areaGestao\":"+value));
  assertThatThrownBy(()->FtDeferredSemantics.configurations(d)).isInstanceOf(IllegalStateException.class);
 }
 @Test void duplicatesAndUnknownFieldsAreRefused()throws Exception {
  var d=json.readTree(GovernedFtControllerTests.definition(1));var a=(com.fasterxml.jackson.databind.node.ArrayNode)d.path("semantics").path("allowedConfigurations");a.add(a.get(0));
  assertThatThrownBy(()->FtDeferredSemantics.configurations(d)).isInstanceOf(IllegalStateException.class);
  var x=json.readTree(GovernedFtControllerTests.definition(1));((com.fasterxml.jackson.databind.node.ObjectNode)x.path("semantics")).put("approval",true);
  assertThatThrownBy(()->FtDeferredSemantics.configurations(x)).isInstanceOf(IllegalStateException.class);
 }
}
