package library.models;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private String publisher;
    private String itemType;
    private int totalCopies;
    private int availableCopies;
    private int borrowCount;
    private String lastIssueDate;

    public Book() {
    }

    public Book(String isbn, String title, String author, String genre, String publisher, int totalCopies,
            int availableCopies, int borrowCount, String lastIssueDate) {
        this(isbn, title, author, genre, publisher, "Book", totalCopies, availableCopies, borrowCount, lastIssueDate);
    }

    public Book(String isbn, String title, String author, String genre, String publisher, String itemType,
            int totalCopies, int availableCopies, int borrowCount, String lastIssueDate) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publisher = publisher;
        this.itemType = itemType;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.borrowCount = borrowCount;
        this.lastIssueDate = lastIssueDate;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    public String getLastIssueDate() {
        return lastIssueDate;
    }

    public void setLastIssueDate(String lastIssueDate) {
        this.lastIssueDate = lastIssueDate;
    }

    /**
     * Borrow book if there is an available book.
     */
    public boolean borrowBook() {
        if (availableCopies > 0) {
            availableCopies--;
            borrowCount++;
            return true;
        }
        return false;
    }

    /**
     * Return book if it does not exceed total copies book.
     */
    public boolean returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
            if (borrowCount > 0) {
                borrowCount--;
            }
            return true;
        }
        return false;
    }
}