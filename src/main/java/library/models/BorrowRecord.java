package library.models;

public class BorrowRecord {
    private String isbn;
    private String bookTitle;
    private String borrowerName;
    private String userRole;
    private String itemType;
    private String issueDate;
    private String dueDate;
    private String action;

    // public BorrowRecord(String isbn, String bookTitle, String borrowerName, String userRole, String issueDate,
    //         String action) {
    //     this(isbn, bookTitle, borrowerName, userRole, "Book", issueDate, "", action);
    // }

    // public BorrowRecord(String isbn, String bookTitle, String borrowerName, String userRole, String issueDate,
    //         String dueDate, String action) {
    //     this(isbn, bookTitle, borrowerName, userRole, "Book", issueDate, dueDate, action);
    // }

    public BorrowRecord(String isbn, String bookTitle, String borrowerName, String userRole, String itemType,
            String issueDate, String dueDate, String action) {
        this.isbn = isbn;
        this.bookTitle = bookTitle;
        this.borrowerName = borrowerName;
        this.userRole = userRole;
        this.itemType = itemType;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.action = action;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getItemType() {
        return itemType;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getAction() {
        return action;
    }
}