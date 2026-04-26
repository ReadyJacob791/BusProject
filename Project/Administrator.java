package Project;

/**
 * Represents an administrator account in the bus management system.
 *
 * Currently inherits all behaviour directly from {@link Account}.
 * This class exists as a named type to allow future administrator-specific
 * logic (e.g. audit logging, elevated permissions checks) to be added here
 * without modifying the base {@link Account} class.
 *
 * Administrator privileges are currently determined at runtime by the
 * {@code "ADMIN"} role string stored in the accounts CSV and checked via
 * {@link Account#isAdmin()}.
 */
public class Administrator extends Account {
    // No additional fields or methods at this time.
    // Extend this class to add admin-only behaviour in future iterations.
}
