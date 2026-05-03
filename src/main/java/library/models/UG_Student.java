package library.models;

public class UG_Student extends User {
    public UG_Student(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "UG_Student";
    }

    @Override
    public boolean canAddBook() {
        return false;
    }

    @Override
    public boolean canUpdateBookInfo() {
        return false;
    }

    @Override
    public boolean canDeleteBook() {
        return false;
    }

    @Override
    public boolean canBorrowBook() {
        return true;
    }

    @Override
    public boolean canViewAllBorrowHistory() {
        return false;
    }
}
