package Pages;

import javax.swing.*;

/**
 * ChabiVaultApp — Entry point for the ChabiVault Password Manager.
 * 
 * Initializes the SQLite database and routes to either:
 *  - RegisterPage (if no master user exists — first launch)
 *  - LoginPage (if master user is already registered)
 */
public class ChabiVaultApp {

    public static void main(String[] args) {

        // ── Set Look & Feel ─────────────────────────────────
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Dark theme overrides for JOptionPane and dialogs
            UIManager.put("OptionPane.background",        new java.awt.Color(42, 42, 58));
            UIManager.put("Panel.background",             new java.awt.Color(42, 42, 58));
            UIManager.put("OptionPane.messageForeground", java.awt.Color.WHITE);
            UIManager.put("Button.background",            new java.awt.Color(99, 102, 241));
            UIManager.put("Button.foreground",            java.awt.Color.WHITE);
            UIManager.put("Button.focus",                 new java.awt.Color(99, 102, 241));
        } catch (Exception e) {
            // Fallback to system default if cross-platform L&F fails
            System.err.println("[UI] Could not set Look & Feel: " + e.getMessage());
        }

        // ── Initialize Database ─────────────────────────────
        DatabaseManager.initialize();

        // ── Launch on EDT ───────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            if (DatabaseManager.masterUserExists()) {
                System.out.println("[APP] Master user found → Login Page");
                new LoginPage();
            } else {
                System.out.println("[APP] No master user → Register Page");
                new RegisterPage();
            }
        });

        // ── Shutdown Hook ───────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.close();
            System.out.println("[APP] Shutdown complete.");
        }));
    }
}
