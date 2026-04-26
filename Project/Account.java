package Project;

/**
 * Represents a user account in the bus management system.
 *
 * Stores the account's credentials and role, and provides an
 * isAdmin() convenience method that the UI uses to decide which
 * menu items to display after login.
 *
 * Passwords are stored as SHA-256 hex strings — never plain text.
 * The hashing itself is performed in UserInterface.hashPassword().
 */
public class Account {

    /** The account's login username. */
    private String username = "";

    /**
     * SHA-256 hash of the account's password.
     * Stored rather than the plain-text password so credentials cannot be
     * read directly from memory or the CSV file.
     */
    private String password = "";

    /**
     * Permission level for this account.
     * Expected values: "USER" or "ADMIN" (case-insensitive).
     */
    private String role;

    // Constructors

    /**
     * No-argument constructor.
     * Leaves all fields at their default values; used as a generic placeholder.
     */
    public Account() {
    }

    /**
     * Full constructor used when a user successfully logs in.
     *
     * @param username The account's username.
     * @param password The SHA-256 hex hash of the account's password.
     * @param role     The permission level ("USER" or "ADMIN").
     */
    public Account(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    // Getters

    /** @return The account's username. */
    public String getUsername() { return username; }

    /** @return The SHA-256 hex hash of the account's password. */
    public String getPassword() { return password; }

    /** @return The account's role string ("USER" or "ADMIN"). */
    public String getRole() { return role; }

    // Role Check

    /**
     * Returns true if this account has administrator privileges.
     *
     * The comparison is case-insensitive so that "admin", "ADMIN",
     * and "Admin" are all accepted.
     *
     * true if the role is "admin" (case-insensitive).
     */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }
}
