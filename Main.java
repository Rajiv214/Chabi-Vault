import javax.swing.*;
import Pages.*;

/**
 * Main.java — Single entry point to launch the entire ChabiVault application.
 * 
 * Connects all components:
 *   ├── DatabaseManager   → SQLite local database (chabivault.db)
 *   ├── CryptoUtils       → BCrypt hashing + AES-256 encryption
 *   ├── PasswordGenerator → Secure password generation
 *   ├── RegisterPage      → First-time master account setup
 *   ├── LoginPage         → Master authentication gate
 *   └── VaultPage         → Password management dashboard
 * 
 * Usage:
 *   javac -cp "lib\*" Pages\*.java Main.java
 *   java  -cp "lib\*;Pages;." Main
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║        ChabiVault v1.0                ║");
        System.out.println("║   Secure Password Manager             ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println();

        // ── 1. Set dark Look & Feel for all Swing dialogs ───
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("OptionPane.background",        new java.awt.Color(42, 42, 58));
            UIManager.put("Panel.background",             new java.awt.Color(42, 42, 58));
            UIManager.put("OptionPane.messageForeground", java.awt.Color.WHITE);
            UIManager.put("Button.background",            new java.awt.Color(99, 102, 241));
            UIManager.put("Button.foreground",            java.awt.Color.WHITE);
            UIManager.put("Button.focus",                 new java.awt.Color(99, 102, 241));
            System.out.println("[✓] UI theme loaded");
        } catch (Exception e) {
            System.err.println("[✗] Could not set Look & Feel: " + e.getMessage());
        }

        // ── 2. Initialize the local SQLite database ─────────
        DatabaseManager.initialize();
        System.out.println("[✓] Database ready (chabivault.db)");

        // ── 3. Route to the correct page on the EDT ─────────
        SwingUtilities.invokeLater(() -> {
            if (DatabaseManager.masterUserExists()) {
                System.out.println("[→] Master user found — opening Login Page");
                new LoginPage();
            } else {
                System.out.println("[→] No master user — opening Registration Page");
                new RegisterPage();
            }
        });

        // ── 4. Clean up database on exit ────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.close();
            System.out.println("[✓] Shutdown complete. Vault locked.");
        }));
    }
}
