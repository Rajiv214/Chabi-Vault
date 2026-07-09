package Pages;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * RegisterPage — First-time master user registration for ChabiVault.
 * 
 * Only shown when no master user exists in the database.
 * Collects username, password (with confirmation), hashes with BCrypt,
 * generates an AES salt, and stores everything in the database.
 */
public class RegisterPage {

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

    public RegisterPage() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {

        // ── Frame ───────────────────────────────────────────
        frame = new JFrame("ChabiVault — Create Master Account");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 780);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // ── Main Panel with gradient ────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 25, 38),
                                                      0, getHeight(), new Color(35, 35, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        // ── Title Section ───────────────────────────────────
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(45, 0, 15, 0));

        JLabel iconLabel = new JLabel("\uD83D\uDEE1\uFE0F", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Create Master Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Set up your vault credentials", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitleLabel);

        // ── Card Panel ──────────────────────────────────────
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(70, 70, 90, 100));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));
        cardPanel.setPreferredSize(new Dimension(420, 440));
        cardPanel.setMaximumSize(new Dimension(420, 440));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // ── Username Field ──────────────────────────────────
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        cardPanel.add(createLabel("Choose a Username"), gbc);

        JTextField usernameField = createStyledTextField("Enter a username");
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        cardPanel.add(usernameField, gbc);

        // ── Password Field ──────────────────────────────────
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 5, 0);
        cardPanel.add(createLabel("Master Password"), gbc);

        JPasswordField passwordField = createStyledPasswordField();
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        cardPanel.add(passwordField, gbc);

        // ── Strength Bar ────────────────────────────────────
        JPanel strengthPanel = new JPanel(new BorderLayout(8, 0));
        strengthPanel.setOpaque(false);

        JProgressBar strengthBar = new JProgressBar(0, 4);
        strengthBar.setPreferredSize(new Dimension(200, 6));
        strengthBar.setBorderPainted(false);
        strengthBar.setStringPainted(false);
        strengthBar.setBackground(new Color(60, 60, 80));
        strengthBar.setForeground(ERROR_RED);

        JLabel strengthLabel = new JLabel("Very Weak");
        strengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        strengthLabel.setForeground(TEXT_MUTED);

        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        strengthPanel.add(strengthLabel, BorderLayout.EAST);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 16, 0);
        cardPanel.add(strengthPanel, gbc);

        // ── Confirm Password ────────────────────────────────
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 5, 0);
        cardPanel.add(createLabel("Confirm Password"), gbc);

        JPasswordField confirmField = createStyledPasswordField();
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 8, 0);
        cardPanel.add(confirmField, gbc);

        // ── Show Password ───────────────────────────────────
        JCheckBox showPass = new JCheckBox("Show passwords");
        showPass.setOpaque(false);
        showPass.setForeground(TEXT_SECONDARY);
        showPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPass.setFocusPainted(false);
        showPass.addActionListener(e -> {
            char echo = showPass.isSelected() ? (char) 0 : '\u2022';
            passwordField.setEchoChar(echo);
            confirmField.setEchoChar(echo);
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 6, 0);
        cardPanel.add(showPass, gbc);

        // ── Error Label ─────────────────────────────────────
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_RED);
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 10, 0);
        cardPanel.add(errorLabel, gbc);

        // ── Register Button ─────────────────────────────────
        JButton registerBtn = createStyledButton("Create Account", ACCENT, ACCENT_HOVER);
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        cardPanel.add(registerBtn, gbc);

        // ── Live Strength Meter ─────────────────────────────
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String pw = new String(passwordField.getPassword());
                int strength = PasswordGenerator.getStrength(pw);
                strengthBar.setValue(strength);
                strengthLabel.setText(PasswordGenerator.getStrengthLabel(strength));
                int[] rgb = PasswordGenerator.getStrengthColor(strength);
                Color c = new Color(rgb[0], rgb[1], rgb[2]);
                strengthBar.setForeground(c);
                strengthLabel.setForeground(c);
            }
            public void insertUpdate(DocumentEvent e)  { update(); }
            public void removeUpdate(DocumentEvent e)   { update(); }
            public void changedUpdate(DocumentEvent e)   { update(); }
        });

        // ── Register Logic ──────────────────────────────────
        ActionListener registerAction = e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm  = new String(confirmField.getPassword());

            // Validation
            if (username.isEmpty() || username.equals("Enter a username")) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Please choose a username.");
                usernameField.requestFocus();
                return;
            }
            if (username.length() < 3) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Username must be at least 3 characters.");
                usernameField.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Please enter a password.");
                passwordField.requestFocus();
                return;
            }
            if (password.length() < 6) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Password must be at least 6 characters.");
                passwordField.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Passwords do not match.");
                confirmField.setText("");
                confirmField.requestFocus();
                return;
            }

            // Hash + salt + save
            String hash = CryptoUtils.hashPassword(password);
            byte[] salt = CryptoUtils.generateSalt();
            String saltHex = CryptoUtils.bytesToHex(salt);

            boolean success = DatabaseManager.registerMasterUser(username, hash, saltHex);

            if (success) {
                errorLabel.setForeground(SUCCESS_GREEN);
                errorLabel.setText("\u2713  Account created! Redirecting to login...");
                registerBtn.setEnabled(false);

                Timer timer = new Timer(1000, ev -> {
                    frame.dispose();
                    new LoginPage();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                errorLabel.setForeground(ERROR_RED);
                errorLabel.setText("\u2717  Registration failed. Please try again.");
            }
        };

        registerBtn.addActionListener(registerAction);
        confirmField.addActionListener(registerAction);

        // ── Center Wrapper ──────────────────────────────────
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(cardPanel);

        // ── Footer ──────────────────────────────────────────
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        JLabel footerLabel = new JLabel("ChabiVault v1.0  \u2022  Your passwords, your rules");
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

    // ── Helper: Label ───────────────────────────────────────
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(200, 200, 220));
        return label;
    }

    // ── Helper: Styled Text Field ───────────────────────────
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

    // ── Helper: Styled Password Field ───────────────────────
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

    // ── Helper: Styled Button ───────────────────────────────
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
}
