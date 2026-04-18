package Project;

public class Account {
    private String username = ""; 
    private String password = "";
    private String role;  

    public Account() {

    }

    public Account(String username, String password, String role) {
        this.username = username; 
        this.password = password; 
        this.role = role; 
    }

    public String getUsername() {
        return username; 
    }

    public String getPassword() {
        return password; 
    }

    public String getRole() {
        return role; 
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.role);
    }

    
}