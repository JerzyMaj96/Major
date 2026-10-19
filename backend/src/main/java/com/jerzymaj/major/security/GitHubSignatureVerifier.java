package com.jerzymaj.major.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Verifies that a webhook request really comes from GitHub by checking the
 * {@code X-Hub-Signature-256} header, an HMAC-SHA256 of the request body signed with a
 * shared secret.
 */
@Component
public class GitHubSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Checks the signature header against the HMAC computed from the payload.
     *
     * <p>The comparison uses {@link MessageDigest#isEqual(byte[], byte[])}, which takes the same
     * time regardless of where the values differ, to prevent timing attacks.
     *
     * @param payload         raw request body (must not be re-serialized before verification)
     * @param signatureHeader header value, expected in the form {@code sha256=<hex>}
     * @param secret          shared webhook secret configured in GitHub
     * @return {@code true} only if the header is present, well formed and matches
     */
    public boolean isValidSignature(String payload, String signatureHeader, String secret) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }

        String expectedSignature = signatureHeader.substring("sha256=".length());
        String computedSignature = computeHmac(payload, secret);

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                computedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String computeHmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            for (byte b : hmacBytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to compute HMAC signature", ex);
        }
    }
}
