package Pages;

import org.mindrot.jbcrypt.BCrypt;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

/**
 * CryptoUtils — Security utilities for ChabiVault.
 * 
 * Provides:
 *  - BCrypt password hashing (for master password)
 *  - AES-256-CBC encryption/decryption (for stored platform passwords)
 *  - PBKDF2 key derivation (master password → AES key)
 */
public class CryptoUtils {

    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int AES_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;

    // ── BCrypt Hashing ──────────────────────────────────────

    /**
     * Hash a password using BCrypt with a cost factor of 12.
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Verify a password against a BCrypt hash.
     */
    public static boolean checkPassword(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Salt Generation ─────────────────────────────────────

    /**
     * Generate a cryptographically secure random salt.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // ── AES Key Derivation ──────────────────────────────────

    /**
     * Derive an AES-256 key from the master password using PBKDF2.
     */
    public static SecretKey deriveAESKey(String masterPassword, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
            masterPassword.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(keyBytes, "AES");
    }

    // ── AES Encryption / Decryption ─────────────────────────

    /**
     * Encrypt plaintext using AES-256-CBC.
     * Returns a Base64-encoded string containing [IV + ciphertext].
     */
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Prepend IV to ciphertext so we can extract it during decryption
        byte[] combined = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypt an AES-256-CBC ciphertext (Base64-encoded [IV + ciphertext]).
     */
    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(ciphertext);

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        return new String(cipher.doFinal(encrypted), "UTF-8");
    }

    // ── Hex Utilities ───────────────────────────────────────

    /**
     * Convert a byte array to a hex string.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Convert a hex string to a byte array.
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
