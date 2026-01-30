package com.ureca.unity.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class OAuthTokenCrypto {

    private static final String ALG = "AES";
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;

    private final byte[] keyBytes;
    private final SecureRandom random = new SecureRandom();

    public OAuthTokenCrypto(@Value("${security.oauth-token.secret}") String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        this.keyBytes = new byte[32];
        for (int i = 0; i < this.keyBytes.length; i++) {
            this.keyBytes[i] = i < raw.length ? raw[i] : 0;
        }
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));

            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);

            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("OAuth token encrypt failed", e);
        }
    }

    public String decrypt(String enc) {
        if (enc == null) return null;
        try {
            byte[] in = Base64.getDecoder().decode(enc);
            if (in.length < IV_LEN + 1) return enc;

            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[in.length - IV_LEN];

            System.arraycopy(in, 0, iv, 0, IV_LEN);
            System.arraycopy(in, IV_LEN, ct, 0, ct.length);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));

            byte[] pt = cipher.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return enc;
        }
    }
}
