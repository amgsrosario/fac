package com.ar2lda.fac.fiscal.signingtrial;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** Non-fiscal, ephemeral test material only. No key import/export or production source implementation. */
public final class EphemeralTrialSigner {
    private final KeyPair keys;
    private EphemeralTrialSigner(KeyPair keys) { this.keys = keys; }
    public static EphemeralTrialSigner createForExperiment() {
        try {
            var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(1024);
            return new EphemeralTrialSigner(generator.generateKeyPair());
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Trial RSA unavailable", e); }
    }
    public String publicKey() { return Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()); }
    public String sign(String text) {
        try {
            var signer = Signature.getInstance("SHA1withRSA"); // PKCS#1 v1.5, specimen profile, NOT productive approval.
            signer.initSign(keys.getPrivate()); signer.update(text.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(signer.sign());
            if (result.length() != 172) throw new IllegalStateException("Unexpected trial profile");
            return result;
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Trial signing failed", e); }
    }
    public static boolean verify(String text, String signature, String publicKey) {
        try {
            var key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
            var verifier = Signature.getInstance("SHA1withRSA"); verifier.initVerify(key);
            verifier.update(text.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (GeneralSecurityException | IllegalArgumentException e) { return false; }
    }
}
