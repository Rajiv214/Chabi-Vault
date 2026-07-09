package Pages;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * VaultPage — Main password management dashboard for ChabiVault.
 * 
 * Features:
 *  - View all saved passwords in a styled table
 *  - Add / Edit / Delete password entries
 *  - Copy passwords to clipboard
 *  - Show / Hide passwords
 *  - Real-time search filtering
 *  - Integrated password generator dialog
 *  - AES-256 encryption for all stored passwords
 */
public class VaultPage {

    // ── Color Palette ───────────────────────────────────────
    private static final Color BG_DARK         = new Color(22, 22, 34);
    private static final Color BG_HEADER       = new Color(30, 30, 44);
    private static final Color CARD_BG         = new Color(38, 38, 54);
    private static final Color TABLE_BG        = new Color(32, 32, 48);
    private static final Color TABLE_ALT       = new Color(38, 38, 56);
    private static final Color TABLE_HEADER_BG = new Color(45, 45, 65);
    private static final Color TABLE_GRID      = new Color(55, 55, 75);
    private static final Color INPUT_BG        = new Color(50, 50, 70);
    private static final Color INPUT_BORDER    = new Color(70, 70, 95);
    private static final Color ACCENT          = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER    = new Color(79, 82, 221);
    private static final Color GREEN           = new Color(34, 197, 94);
    private static final Color GREEN_HOVER     = new Color(22, 163, 74);
    private static final Color AMBER           = new Color(245, 158, 11);
    private static final Color AMBER_HOVER     = new Color(217, 119, 6);
    private static final Color RED             = new Color(239, 68, 68);
    private static final Color RED_HOVER       = new Color(220, 38, 38);
    private static final Color TEAL            = new Color(20, 184, 166);
    private static final Color TEAL_HOVER      = new Color(13, 148, 136);
    private static final Color TEXT_PRIMARY    = Color.WHITE;
    private static final Color TEXT_SECONDARY  = new Color(160, 160, 185);
    private static final Color TEXT_MUTED      = new Color(120, 120, 145);

    private JFrame frame;
    private SecretKey aesKey;
    private String masterUsername;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statsLabel;
    private ArrayList<int[]> passwordIds = new ArrayList<>(); // row → db id
    private ArrayList<String> decryptedPasswords = new ArrayList<>();

    public VaultPage(SecretKey aesKey, String masterUsername) {
        this.aesKey = aesKey;
        this.masterUsername = masterUsername;
        createAndShowGUI();
    }

    private void createAndShowGUI() {

        // ── Frame ───────────────────────────────────────────
        frame = new JFrame("ChabiVault — Password Vault");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 700);
        frame.setMinimumSize(new Dimension(850, 550));
        frame.setLocationRelativeTo(null);

        // ── Main Panel ──────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);

        // ── Header ──────────────────────────────────────────
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);

        // ── Table Panel ─────────────────────────────────────
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────
        JPanel footer = createFooter();
        mainPanel.add(footer, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.setVisible(true);

        // Load passwords
        refreshTable(null);
    }

    // ═══════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(35, 35, 55),
                                                      getWidth(), 0, new Color(50, 40, 70));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom border line
                g2.setColor(new Color(99, 102, 241, 60));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Left side: Logo + title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel logo = new JLabel("\uD83D\uDD10");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel title = new JLabel("ChabiVault");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel welcome = new JLabel("  \u2022  Welcome, " + masterUsername);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcome.setForeground(TEXT_SECONDARY);

        leftPanel.add(logo);
        leftPanel.add(title);
        leftPanel.add(welcome);

        // Right side: Search + buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        // Search field
        JTextField searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBackground(INPUT_BG);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        searchField.setToolTipText("Search by platform or username");

        // Placeholder
        searchField.setText("\uD83D\uDD0D Search passwords...");
        searchField.setForeground(TEXT_MUTED);
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().contains("Search passwords")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("\uD83D\uDD0D Search passwords...");
                    searchField.setForeground(TEXT_MUTED);
                }
            }
        });

        // Real-time search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void doSearch() {
                String q = searchField.getText().trim();
                if (q.contains("Search passwords")) q = "";
                refreshTable(q.isEmpty() ? null : q);
            }
            public void insertUpdate(DocumentEvent e)  { doSearch(); }
            public void removeUpdate(DocumentEvent e)   { doSearch(); }
            public void changedUpdate(DocumentEvent e)   { doSearch(); }
        });

        // Buttons
        JButton addBtn = createHeaderButton("\u2795 Add Password", GREEN, GREEN_HOVER);
        addBtn.addActionListener(e -> showAddEditDialog(null));

        JButton genBtn = createHeaderButton("\u26A1 Generator", TEAL, TEAL_HOVER);
        genBtn.addActionListener(e -> showGeneratorDialog());

        JButton logoutBtn = createHeaderButton("Logout", RED, RED_HOVER);
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to log out?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                frame.dispose();
                new LoginPage();
            }
        });

        rightPanel.add(searchField);
        rightPanel.add(addBtn);
        rightPanel.add(genBtn);
        rightPanel.add(logoutBtn);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ═══════════════════════════════════════════════════════
    //  TABLE PANEL
    // ═══════════════════════════════════════════════════════

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        // Table Model (non-editable)
        String[] columns = {"Platform", "Username", "Password", "URL", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // Only actions column is "editable" (for buttons)
            }
        };

        table = new JTable(tableModel);
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(48);
        table.setGridColor(TABLE_GRID);
        table.setSelectionBackground(new Color(99, 102, 241, 40));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SECONDARY);
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setPreferredSize(new Dimension(0, 40));

        // Custom header renderer
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setForeground(TEXT_SECONDARY);
                label.setBackground(TABLE_HEADER_BG);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return label;
            }
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);

        // Custom cell renderer for alternating rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

                if (isSelected) {
                    setBackground(new Color(99, 102, 241, 40));
                } else {
                    setBackground(row % 2 == 0 ? TABLE_BG : TABLE_ALT);
                }
                return this;
            }
        });

        // Actions column renderer & editor
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(TABLE_GRID, 1));
        scrollPane.getViewport().setBackground(TABLE_BG);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Stats bar above table
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 0));

        statsLabel = new JLabel("0 passwords stored");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(TEXT_SECONDARY);
        statsPanel.add(statsLabel);

        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════════════
    //  FOOTER
    // ═══════════════════════════════════════════════════════

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_DARK);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TABLE_GRID),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));

        JLabel left = new JLabel("ChabiVault v1.0  \u2022  AES-256 Encrypted  \u2022  BCrypt Hashed");
        left.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        left.setForeground(TEXT_MUTED);

        JLabel right = new JLabel("Secure & Private \uD83D\uDD12");
        right.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        right.setForeground(TEXT_MUTED);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    // ═══════════════════════════════════════════════════════
    //  TABLE DATA
    // ═══════════════════════════════════════════════════════

    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0);
        passwordIds.clear();
        decryptedPasswords.clear();

        try {
            ResultSet rs;
            if (searchQuery != null && !searchQuery.isEmpty()) {
                rs = DatabaseManager.searchPasswords(searchQuery);
            } else {
                rs = DatabaseManager.getAllPasswords();
            }

            if (rs != null) {
                int count = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String platform = rs.getString("platform");
                    String username = rs.getString("account_username");
                    String encPassword = rs.getString("encrypted_password");
                    String url = rs.getString("website_url");

                    // Decrypt password
                    String decrypted = "";
                    try {
                        decrypted = CryptoUtils.decrypt(encPassword, aesKey);
                    } catch (Exception e) {
                        decrypted = "[decryption error]";
                    }

                    passwordIds.add(new int[]{id});
                    decryptedPasswords.add(decrypted);

                    // Show masked password in table
                    String masked = "\u2022".repeat(Math.min(decrypted.length(), 12));

                    tableModel.addRow(new Object[]{platform, username, masked, url, "actions"});
                    count++;
                }
                rs.close();
                statsLabel.setText(count + " password" + (count != 1 ? "s" : "") + " stored");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  ADD / EDIT DIALOG
    // ═══════════════════════════════════════════════════════

    private void showAddEditDialog(int[] editData) {
        // editData: null = add, or {rowIndex} = edit
        boolean isEdit = (editData != null);
        String dialogTitle = isEdit ? "Edit Password" : "Add New Password";

        JDialog dialog = new JDialog(frame, dialogTitle, true);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        // Title
        JLabel dlgTitle = new JLabel(isEdit ? "\u270F\uFE0F  Edit Password" : "\u2795  Add New Password");
        dlgTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dlgTitle.setForeground(TEXT_PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(dlgTitle, gbc);

        // Platform
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createDialogLabel("Platform / Service"), gbc);

        JTextField platformField = createDialogField("e.g. Google, GitHub, Netflix");
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(platformField, gbc);

        // Username
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createDialogLabel("Username / Email"), gbc);

        JTextField usernameField = createDialogField("e.g. user@example.com");
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(usernameField, gbc);

        // Password row (field + generate button)
        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createDialogLabel("Password"), gbc);

        JPanel passRow = new JPanel(new BorderLayout(8, 0));
        passRow.setOpaque(false);

        JTextField passwordField = createDialogField("Enter or generate password");
        passRow.add(passwordField, BorderLayout.CENTER);

        JButton genPassBtn = new JButton("\u26A1");
        genPassBtn.setToolTipText("Generate Password");
        genPassBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        genPassBtn.setBackground(TEAL);
        genPassBtn.setForeground(Color.WHITE);
        genPassBtn.setFocusPainted(false);
        genPassBtn.setBorderPainted(false);
        genPassBtn.setOpaque(true);
        genPassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        genPassBtn.setPreferredSize(new Dimension(44, 44));
        genPassBtn.addActionListener(e -> {
            String generated = PasswordGenerator.generate(16, true, true, true, true);
            passwordField.setText(generated);
        });
        genPassBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { genPassBtn.setBackground(TEAL_HOVER); }
            public void mouseExited(MouseEvent e)  { genPassBtn.setBackground(TEAL); }
        });
        passRow.add(genPassBtn, BorderLayout.EAST);

        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(passRow, gbc);

        // URL
        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createDialogLabel("Website URL (optional)"), gbc);

        JTextField urlField = createDialogField("e.g. https://google.com");
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 14, 0);
        panel.add(urlField, gbc);

        // Notes
        gbc.gridy = 9; gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(createDialogLabel("Notes (optional)"), gbc);

        JTextField notesField = createDialogField("Additional notes...");
        gbc.gridy = 10; gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(notesField, gbc);

        // Pre-fill if editing
        if (isEdit) {
            int row = editData[0];
            platformField.setText((String) tableModel.getValueAt(row, 0));
            platformField.setForeground(TEXT_PRIMARY);
            usernameField.setText((String) tableModel.getValueAt(row, 1));
            usernameField.setForeground(TEXT_PRIMARY);
            passwordField.setText(decryptedPasswords.get(row));
            passwordField.setForeground(TEXT_PRIMARY);
            urlField.setText((String) tableModel.getValueAt(row, 3));
            urlField.setForeground(TEXT_PRIMARY);
            // Notes not shown in table, could query DB — skip for simplicity
        }

        // Error label
        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(RED);
        gbc.gridy = 11; gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(errLabel, gbc);

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton cancelBtn = createDialogButton("Cancel", new Color(80, 80, 100), new Color(100, 100, 120));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = createDialogButton(isEdit ? "Update" : "Save", ACCENT, ACCENT_HOVER);
        saveBtn.addActionListener(e -> {
            String platform = getFieldText(platformField, "e.g. Google, GitHub, Netflix");
            String username = getFieldText(usernameField, "e.g. user@example.com");
            String password = getFieldText(passwordField, "Enter or generate password");
            String url = getFieldText(urlField, "e.g. https://google.com");
            String notes = getFieldText(notesField, "Additional notes...");

            if (platform.isEmpty()) {
                errLabel.setText("\u2717  Platform name is required.");
                return;
            }
            if (username.isEmpty()) {
                errLabel.setText("\u2717  Username is required.");
                return;
            }
            if (password.isEmpty()) {
                errLabel.setText("\u2717  Password is required.");
                return;
            }

            try {
                String encrypted = CryptoUtils.encrypt(password, aesKey);

                boolean success;
                if (isEdit) {
                    int dbId = passwordIds.get(editData[0])[0];
                    success = DatabaseManager.updatePassword(dbId, platform, username, encrypted, url, notes);
                } else {
                    success = DatabaseManager.addPassword(platform, username, encrypted, url, notes);
                }

                if (success) {
                    dialog.dispose();
                    refreshTable(null);
                } else {
                    errLabel.setText("\u2717  Failed to save. Try again.");
                }
            } catch (Exception ex) {
                errLabel.setText("\u2717  Encryption error: " + ex.getMessage());
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 12; gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(btnRow, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════
    //  PASSWORD GENERATOR DIALOG
    // ═══════════════════════════════════════════════════════

    private void showGeneratorDialog() {
        JDialog dialog = new JDialog(frame, "Password Generator", true);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;

        // Title
        JLabel titleLbl = new JLabel("\u26A1  Password Generator");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(TEXT_PRIMARY);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(titleLbl, gbc);

        // Generated password display
        JTextField generatedField = new JTextField();
        generatedField.setFont(new Font("Consolas", Font.BOLD, 16));
        generatedField.setBackground(INPUT_BG);
        generatedField.setForeground(TEAL);
        generatedField.setCaretColor(TEXT_PRIMARY);
        generatedField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        generatedField.setEditable(false);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(generatedField, gbc);

        // Strength indicator
        JPanel strengthPanel = new JPanel(new BorderLayout(8, 0));
        strengthPanel.setOpaque(false);

        JProgressBar strengthBar = new JProgressBar(0, 4);
        strengthBar.setPreferredSize(new Dimension(200, 6));
        strengthBar.setBorderPainted(false);
        strengthBar.setStringPainted(false);
        strengthBar.setBackground(new Color(60, 60, 80));

        JLabel strengthLbl = new JLabel(" ");
        strengthLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        strengthLbl.setForeground(TEXT_MUTED);

        strengthPanel.add(strengthBar, BorderLayout.CENTER);
        strengthPanel.add(strengthLbl, BorderLayout.EAST);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(strengthPanel, gbc);

        // Length slider
        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 5, 0);
        JLabel lenLabel = new JLabel("Length: 16");
        lenLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lenLabel.setForeground(new Color(200, 200, 220));
        panel.add(lenLabel, gbc);

        JSlider lengthSlider = new JSlider(4, 64, 16);
        lengthSlider.setBackground(CARD_BG);
        lengthSlider.setForeground(ACCENT);
        lengthSlider.setMajorTickSpacing(20);
        lengthSlider.setMinorTickSpacing(4);
        lengthSlider.setPaintTicks(true);
        lengthSlider.addChangeListener(e -> lenLabel.setText("Length: " + lengthSlider.getValue()));
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(lengthSlider, gbc);

        // Character options
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;

        JCheckBox upperCb = createStyledCheckbox("Uppercase (A-Z)", true);
        gbc.gridy = 5; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 8, 10);
        panel.add(upperCb, gbc);

        JCheckBox lowerCb = createStyledCheckbox("Lowercase (a-z)", true);
        gbc.gridx = 1; gbc.insets = new Insets(0, 10, 8, 0);
        panel.add(lowerCb, gbc);

        JCheckBox digitsCb = createStyledCheckbox("Digits (0-9)", true);
        gbc.gridy = 6; gbc.gridx = 0; gbc.insets = new Insets(0, 0, 8, 10);
        panel.add(digitsCb, gbc);

        JCheckBox specialCb = createStyledCheckbox("Special (!@#$)", true);
        gbc.gridx = 1; gbc.insets = new Insets(0, 10, 8, 0);
        panel.add(specialCb, gbc);

        // Reset gridwidth
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        JButton generateBtn = createDialogButton("\u26A1 Generate", ACCENT, ACCENT_HOVER);
        JButton copyBtn = createDialogButton("\uD83D\uDCCB Copy", GREEN, GREEN_HOVER);

        // Generate action
        Runnable doGenerate = () -> {
            String pw = PasswordGenerator.generate(
                lengthSlider.getValue(),
                upperCb.isSelected(), lowerCb.isSelected(),
                digitsCb.isSelected(), specialCb.isSelected()
            );
            generatedField.setText(pw);
            int str = PasswordGenerator.getStrength(pw);
            strengthBar.setValue(str);
            strengthLbl.setText(PasswordGenerator.getStrengthLabel(str));
            int[] rgb = PasswordGenerator.getStrengthColor(str);
            Color c = new Color(rgb[0], rgb[1], rgb[2]);
            strengthBar.setForeground(c);
            strengthLbl.setForeground(c);
        };

        generateBtn.addActionListener(e -> doGenerate.run());
        copyBtn.addActionListener(e -> {
            String pw = generatedField.getText();
            if (!pw.isEmpty()) {
                copyToClipboard(pw);
                copyBtn.setText("\u2713 Copied!");
                Timer t = new Timer(1500, ev -> copyBtn.setText("\uD83D\uDCCB Copy"));
                t.setRepeats(false);
                t.start();
            }
        });

        btnRow.add(generateBtn);
        btnRow.add(copyBtn);

        gbc.gridy = 7; gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(btnRow, gbc);

        // Generate one initially
        dialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) { doGenerate.run(); }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════
    //  ACTION BUTTONS (Table Cell Renderer/Editor)
    // ═══════════════════════════════════════════════════════

    /**
     * Renders the action buttons column in the table.
     */
    private class ActionRenderer extends JPanel implements TableCellRenderer {
        private final JButton showBtn, copyBtn, editBtn, deleteBtn;

        ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 4, 6));
            setOpaque(true);

            showBtn   = createSmallButton("\uD83D\uDC41", "Show/Hide");
            copyBtn   = createSmallButton("\uD83D\uDCCB", "Copy");
            editBtn   = createSmallButton("\u270F\uFE0F", "Edit");
            deleteBtn = createSmallButton("\uD83D\uDDD1", "Delete");

            add(showBtn);
            add(copyBtn);
            add(editBtn);
            add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(row % 2 == 0 ? TABLE_BG : TABLE_ALT);
            if (isSelected) setBackground(new Color(99, 102, 241, 40));
            return this;
        }
    }

    /**
     * Handles button clicks in the actions column.
     */
    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;
        private final JButton showBtn, copyBtn, editBtn, deleteBtn;
        private int currentRow;

        ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            panel.setOpaque(true);

            showBtn   = createSmallButton("\uD83D\uDC41", "Show/Hide");
            copyBtn   = createSmallButton("\uD83D\uDCCB", "Copy");
            editBtn   = createSmallButton("\u270F\uFE0F", "Edit");
            deleteBtn = createSmallButton("\uD83D\uDDD1", "Delete");

            // Show/Hide password
            showBtn.addActionListener(e -> {
                String current = (String) tableModel.getValueAt(currentRow, 2);
                if (current.contains("\u2022")) {
                    // Show decrypted
                    tableModel.setValueAt(decryptedPasswords.get(currentRow), currentRow, 2);
                } else {
                    // Mask again
                    String masked = "\u2022".repeat(Math.min(decryptedPasswords.get(currentRow).length(), 12));
                    tableModel.setValueAt(masked, currentRow, 2);
                }
                fireEditingStopped();
            });

            // Copy to clipboard
            copyBtn.addActionListener(e -> {
                copyToClipboard(decryptedPasswords.get(currentRow));
                JOptionPane.showMessageDialog(frame,
                    "Password copied to clipboard!",
                    "Copied", JOptionPane.INFORMATION_MESSAGE);
                fireEditingStopped();
            });

            // Edit
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> showAddEditDialog(new int[]{currentRow}));
            });

            // Delete
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                String platform = (String) tableModel.getValueAt(currentRow, 0);
                int choice = JOptionPane.showConfirmDialog(frame,
                    "Delete password for \"" + platform + "\"?\nThis cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    int dbId = passwordIds.get(currentRow)[0];
                    if (DatabaseManager.deletePassword(dbId)) {
                        refreshTable(null);
                    }
                }
            });

            panel.add(showBtn);
            panel.add(copyBtn);
            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            panel.setBackground(row % 2 == 0 ? TABLE_BG : TABLE_ALT);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "actions";
        }
    }

    // ═══════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════

    private JButton createHeaderButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });

        return btn;
    }

    private JButton createSmallButton(String emoji, String tooltip) {
        JButton btn = new JButton(emoji);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setToolTipText(tooltip);
        btn.setBackground(new Color(60, 60, 85));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(36, 32));
        btn.setMargin(new Insets(2, 4, 2, 4));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(80, 80, 110)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(60, 60, 85)); }
        });

        return btn;
    }

    private JLabel createDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(200, 200, 220));
        return label;
    }

    private JTextField createDialogField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_MUTED);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(300, 44));
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

    private String getFieldText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    private JButton createDialogButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });

        return btn;
    }

    private JCheckBox createStyledCheckbox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setForeground(TEXT_SECONDARY);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cb.setFocusPainted(false);
        return cb;
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(text), null);
    }
}
