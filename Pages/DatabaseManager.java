package Pages;

import java.sql.*;

/**
 * DatabaseManager — Manages the local SQLite database for ChabiVault.
 * 
 * Database file: chabivault.db (created in the working directory)
 * 
 * Tables:
 *  - master_user: stores the master username, BCrypt hash, and AES salt
 *  - passwords:   stores encrypted platform credentials
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chabivault.db";
    private static Connection connection;

    // ── Initialization ──────────────────────────────────────

    /**
     * Initialize the database connection and create tables if needed.
     */
    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("[DB] Database initialized: chabivault.db");
        } catch (Exception e) {
            System.err.println("[DB] Failed to initialize database:");
            e.printStackTrace();
        }
    }

    /**
     * Create the master_user and passwords tables if they don't exist.
     */
    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS master_user (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  username TEXT NOT NULL UNIQUE," +
            "  password_hash TEXT NOT NULL," +
            "  aes_salt TEXT NOT NULL," +
            "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS passwords (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  platform TEXT NOT NULL," +
            "  account_username TEXT NOT NULL," +
            "  encrypted_password TEXT NOT NULL," +
            "  website_url TEXT DEFAULT ''," +
            "  notes TEXT DEFAULT ''," +
            "  created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        stmt.close();
    }

    // ── Master User Operations ──────────────────────────────

    /**
     * Check if a master user has been registered.
     */
    public static boolean masterUserExists() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM master_user");
            rs.next();
            boolean exists = rs.getInt(1) > 0;
            rs.close();
            stmt.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Register the master user.
     * @param username      the chosen username
     * @param passwordHash  BCrypt hash of the master password
     * @param aesSaltHex    hex-encoded AES salt for key derivation
     */
    public static boolean registerMasterUser(String username, String passwordHash, String aesSaltHex) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO master_user (username, password_hash, aes_salt) VALUES (?, ?, ?)"
            );
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, aesSaltHex);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the master user's credentials.
     * @return [username, passwordHash, aesSaltHex] or null if not found
     */
    public static String[] getMasterUser() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT username, password_hash, aes_salt FROM master_user LIMIT 1"
            );
            if (rs.next()) {
                String[] result = {
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("aes_salt")
                };
                rs.close();
                stmt.close();
                return result;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Password CRUD Operations ────────────────────────────

    /**
     * Add a new password entry.
     */
    public static boolean addPassword(String platform, String username,
                                       String encryptedPassword, String url, String notes) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO passwords (platform, account_username, encrypted_password, website_url, notes) " +
                "VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, platform);
            ps.setString(2, username);
            ps.setString(3, encryptedPassword);
            ps.setString(4, url != null ? url : "");
            ps.setString(5, notes != null ? notes : "");
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all password entries ordered by platform name.
     */
    public static ResultSet getAllPasswords() {
        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery("SELECT * FROM passwords ORDER BY platform ASC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the total count of stored passwords.
     */
    public static int getPasswordCount() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM passwords");
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Update an existing password entry.
     */
    public static boolean updatePassword(int id, String platform, String username,
                                          String encryptedPassword, String url, String notes) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE passwords SET platform=?, account_username=?, encrypted_password=?, " +
                "website_url=?, notes=?, updated_at=CURRENT_TIMESTAMP WHERE id=?"
            );
            ps.setString(1, platform);
            ps.setString(2, username);
            ps.setString(3, encryptedPassword);
            ps.setString(4, url != null ? url : "");
            ps.setString(5, notes != null ? notes : "");
            ps.setInt(6, id);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a password entry by ID.
     */
    public static boolean deletePassword(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM passwords WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search passwords by platform name or username.
     */
    public static ResultSet searchPasswords(String query) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM passwords WHERE platform LIKE ? OR account_username LIKE ? " +
                "ORDER BY platform ASC"
            );
            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Close the database connection.
     */
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
