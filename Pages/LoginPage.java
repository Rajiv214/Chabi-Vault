package Pages;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * LoginPage — Master authentication screen for ChabiVault.
 * 
 * Dark-themed login form that validates credentials against
 * BCrypt-hashed master password stored in the SQLite database.
 */
public class LoginPage {

    // ── Color Palette ───────────────────────────────────────
    private static final Color BG_DARK       = new Color(30, 30, 40);
    private static final Color CARD_BG       = new Color(42, 42, 58);
    private static final Color INPUT_BG      = new Color(55, 55, 75);
    private static final Color INPUT_BORDER  = new Color(80, 80, 110);
    private static final Color ACCENT        = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER  = new Color(79, 82, 221);
    private static final Color TEXT_PRIMARY   = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(150, 150, 170);
    private static final Color TEXT_MUTED     = new Color(120, 120, 140);
    private static final Color ERROR_RED      = new Color(255, 100, 100);
    private static final Color SUCCESS_GREEN  = new Color(100, 220, 100);

    private JFrame frame;

    public LoginPage() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {

        // ── Frame Setup ─────────────────────────────────────
        frame = new JFrame("ChabiVault — Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 680);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // ── Main Panel ──────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 25, 38),
                                                      0, getHeight(), new Color(35, 35, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setOpaque(false);

        // ── Top Title Section ───────────────────────────────
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(55, 0, 20, 0));

        // Lock icon
        JLabel iconLabel = new JLabel("\uD83D\uDD10", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel("ChabiVault", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Unlock your secure vault", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        // ── Login Card ──────────────────────────────────────
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(70, 70, 90, 100));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));
        cardPanel.setPreferredSize(new Dimension(400, 340));
        cardPanel.setMaximumSize(new Dimension(400, 340));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // ── Username ────────────────────────────────────────
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(new Color(200, 200, 220));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        cardPanel.add(userLabel, gbc);

        JTextField usernameField = createStyledTextField("Enter your username");
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 18, 0);
        cardPanel.add(usernameField, gbc);

        // ── Password ────────────────────────────────────────
        JLabel passLabel = new JLabel("Master Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLabel.setForeground(new Color(200, 200, 220));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        cardPanel.add(passLabel, gbc);

        JPasswordField passwordField = createStyledPasswordField();
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        cardPanel.add(passwordField, gbc);

        // ── Show Password ───────────────────────────────────
        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setOpaque(false);
        showPass.setForeground(TEXT_SECONDARY);
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPass.setFocusPainted(false);
        showPass.addActionListener(e -> {
            passwordField.setEchoChar(showPass.isSelected() ? (char) 0 : '\u2022');
        });
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        cardPanel.add(showPass, gbc);

        // ── Error Label ─────────────────────────────────────
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_RED);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        cardPanel.add(errorLabel, gbc);

        // ── Login Button ────────────────────────────────────
        JButton loginBtn = createStyledButton("Unlock Vault", ACCENT, ACCENT_HOVER);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        cardPanel.add(loginBtn, gbc);

        // ── Login Logic ─────────────────────────────────────
        ActionListener loginAction = e -> {
            String enteredUser = usernameField.getText().trim();
            String enteredPass = new String(passwordField.getPassword());

            // Validate fields
            if (enteredUser.isEmpty() || enteredUser.equals("Enter your username")) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Please enter your username.");
                shakeComponent(cardPanel);
                usernameField.requestFocus();
                return;
            }
            if (enteredPass.isEmpty()) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Please enter your password.");
                shakeComponent(cardPanel);
                passwordField.requestFocus();
                return;
            }

            // Validate against database
            String[] master = DatabaseManager.getMasterUser();
            if (master == null) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  No master user found. Please register.");
                return;
            }

            String dbUsername = master[0];
            String dbHash = master[1];
            String dbSaltHex = master[2];

            if (!enteredUser.equals(dbUsername)) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Username not found.");
                shakeComponent(cardPanel);
                usernameField.requestFocus();
                return;
            }

            if (!CryptoUtils.checkPassword(enteredPass, dbHash)) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Incorrect password. Try again.");
                shakeComponent(cardPanel);
                passwordField.setText("");
                passwordField.requestFocus();
                return;
            }

            // ✓ Success
            errorLabel.setForeground(SUCCESS_GREEN);
            errorLabel.setText("\u2713  Welcome back, " + enteredUser + "!");
            loginBtn.setEnabled(false);

            Timer timer = new Timer(600, ev -> {
                frame.dispose();
                // Derive AES key and open vault
                try {
                    byte[] salt = CryptoUtils.hexToBytes(dbSaltHex);
                    javax.crypto.SecretKey aesKey = CryptoUtils.deriveAESKey(enteredPass, salt);
                    new VaultPage(aesKey, enteredUser);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                        "Failed to derive encryption key: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            timer.setRepeats(false);
            timer.start();
        };

        loginBtn.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // ── Center Wrapper ──────────────────────────────────
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(cardPanel);

        // ── Footer ──────────────────────────────────────────
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        JLabel footerLabel = new JLabel("ChabiVault v1.0  \u2022  Secure & Private");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(100, 100, 120));
        footerPanel.add(footerLabel);

        // ── Assemble ────────────────────────────────────────
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
        usernameField.requestFocusInWindow();
    }

    // ── Styled Text Field ───────────────────────────────────
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_MUTED);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 45));
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_MUTED);
                }
            }
        });

        return field;
    }

    // ── Styled Password Field ───────────────────────────────
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 45));
        return field;
    }

    // ── Styled Button ───────────────────────────────────────
    private JButton createStyledButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(300, 48));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });

        return btn;
    }

    // ── Shake Animation ─────────────────────────────────────
    private void shakeComponent(JComponent comp) {
        Point original = comp.getLocation();
        Timer timer = new Timer(30, null);
        final int[] count = {0};
        timer.addActionListener(e -> {
            int offset = (count[0] % 2 == 0) ? 6 : -6;
            comp.setLocation(original.x + offset, original.y);
            count[0]++;
            if (count[0] >= 6) {
                timer.stop();
                comp.setLocation(original);
            }
        });
        timer.start();
    }

    public JFrame getFrame() {
        return frame;
    }
}
