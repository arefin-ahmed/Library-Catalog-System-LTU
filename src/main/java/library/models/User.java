package library.models;

/**
 * Base user abstraction for login roles.
 */
public abstract class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public abstract String getRole();

    public abstract boolean canAddBook();

    public abstract boolean canUpdateBookInfo();

    public abstract boolean canDeleteBook();

    public abstract boolean canBorrowBook();

    public abstract boolean canViewAllBorrowHistory();
}