package Pages;

import java.security.SecureRandom;

/**
 * PasswordGenerator — Generates cryptographically secure random passwords.
 * 
 * Features:
 *  - Configurable length (4–128 characters)
 *  - Toggle uppercase, lowercase, digits, special characters
 *  - Fisher-Yates shuffle for uniform distribution
 *  - Password strength scoring (0–4)
 */
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String SPECIAL   = "!@#$%^&*()-_=+[]{}|;:',.<>?/~`";

    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a random password with the given configuration.
     * Guarantees at least one character from each enabled category.
     */
    public static String generate(int length, boolean useUpper, boolean useLower,
                                   boolean useDigits, boolean useSpecial) {
        if (length < 4) length = 4;
        if (length > 128) length = 128;

        StringBuilder charPool = new StringBuilder();
        StringBuilder mandatory = new StringBuilder();

        // Add at least one char from each enabled category
        if (useUpper) {
            charPool.append(UPPERCASE);
            mandatory.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (useLower) {
            charPool.append(LOWERCASE);
            mandatory.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (useDigits) {
            charPool.append(DIGITS);
            mandatory.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        if (useSpecial) {
            charPool.append(SPECIAL);
            mandatory.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        // Fallback: if nothing selected, use lowercase
        if (charPool.length() == 0) {
            charPool.append(LOWERCASE);
            mandatory.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }

        // Fill remaining characters from the combined pool
        String pool = charPool.toString();
        StringBuilder password = new StringBuilder(mandatory);
        while (password.length() < length) {
            password.append(pool.charAt(random.nextInt(pool.length())));
        }

        // Fisher-Yates shuffle for uniform distribution
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    /**
     * Calculate password strength (0–4).
     *   0 = Very Weak, 1 = Weak, 2 = Medium, 3 = Strong, 4 = Very Strong
     */
    public static int getStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;
        if (password.length() >= 8)  score++;
        if (password.length() >= 14) score++;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;

        return Math.min(score, 4);
    }

    /**
     * Get a human-readable strength label.
     */
    public static String getStrengthLabel(int strength) {
        switch (strength) {
            case 0:  return "Very Weak";
            case 1:  return "Weak";
            case 2:  return "Medium";
            case 3:  return "Strong";
            case 4:  return "Very Strong";
            default: return "Unknown";
        }
    }

    /**
     * Get a color for the strength indicator (as an RGB int array).
     */
    public static int[] getStrengthColor(int strength) {
        switch (strength) {
            case 0:  return new int[]{220, 50, 50};    // Red
            case 1:  return new int[]{255, 120, 50};   // Orange
            case 2:  return new int[]{245, 180, 30};   // Amber
            case 3:  return new int[]{80, 200, 80};    // Green
            case 4:  return new int[]{50, 220, 150};   // Teal
            default: return new int[]{150, 150, 150};  // Gray
        }
    }
}
