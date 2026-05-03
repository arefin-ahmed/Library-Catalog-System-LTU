package library.models;

public class AdminUser extends User {
    public AdminUser(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public boolean canAddBook() {
        return true;
    }

    @Override
    public boolean canUpdateBookInfo() {
        return true;
    }

    @Override
    public boolean canDeleteBook() {
        return true;
    }

    @Override
    public boolean canBorrowBook() {
        return false;
    }

    @Override
    public boolean canViewAllBorrowHistory() {
        return true;
    }
}