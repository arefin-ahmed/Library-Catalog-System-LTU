package library.models;

public class G_Student extends User {
    public G_Student(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "G_Student";
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
