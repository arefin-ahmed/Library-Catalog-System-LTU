package library.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import library.core.AbstractLibraryCatalog;
import library.models.Book;
import library.models.BorrowRecord;
import library.persistence.CatalogPersistence;

public class LibraryCatalogImpl extends AbstractLibraryCatalog {
    private static final String ITEM_TYPE_BOOK = "Book";
    private static final String ITEM_TYPE_EBOOK = "E-Book";
    private static final String ITEM_TYPE_EJOURNAL = "E-Journal";
    private static final String ITEM_TYPE_DATABASE = "Database";

    private static final String ACTION_BORROW = "BORROW";
    private static final String ACTION_RETURN = "RETURN";
    private static final String ACTION_ACCESS = "ACCESS";

    private static final int UG_STUDENT_MAX_BOOK_BORROWS = 3;
    // private static final int UG_STUDENT_LOAN_DAYS = 10;

    private static final int FACULTY_MAX_BOOK_BORROWS = 10;
    // private static final int FACULTY_LOAN_DAYS = 30;

    private List<BorrowRecord> borrowHistory;
    private BorrowHistoryPersistenceTXT historyPersistence;

    public LibraryCatalogImpl(CatalogPersistence persistence) {
        super(persistence);
        this.borrowHistory = new ArrayList<>();
        this.historyPersistence = new BorrowHistoryPersistenceTXT("txt files/borrow_history.txt");
        loadBorrowHistory();
    }

    @Override
    public List<Book> searchByTitle(String title) {
        return searchIndex(titleIndex, title);
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return searchIndex(authorIndex, author);
    }

    @Override
    public List<Book> searchByGenre(String genre) {
        return searchIndex(genreIndex, genre);
    }

    @Override
    public boolean borrowBook(String isbn) {
        return borrowBook(isbn, "Unknown", "Unknown");
    }

    @Override
    public boolean borrowBook(String isbn, String borrowerName, String userRole) {
        Book book = getBookByIsbn(isbn);
        if (book == null) {
            return false;
        }

        String itemType = normalizeItemType(book.getItemType());

        if (isExternalVisitorRole(userRole) && !isAccessOnlyType(itemType) && !isEBookType(itemType)) {
            return false;
        }

        if (isAccessOnlyType(itemType)) {
            String issueDate = LocalDate.now().toString();
            book.setLastIssueDate(issueDate);
            book.setBorrowCount(book.getBorrowCount() + 1);
            recordHistory(book, borrowerName, userRole, itemType, issueDate, "", ACTION_ACCESS);
            return true;
        }

        int roleBorrowLimit = getBorrowLimitForRoleAndType(userRole, itemType);
        if (roleBorrowLimit > 0 && getActiveBorrowCountForUserByType(borrowerName, itemType) >= roleBorrowLimit) {
            return false;
        }

        // Borrow logic
        boolean borrowed = book.borrowBook();
        if (borrowed) {
            LocalDate issue = LocalDate.now();
            String issueDate = issue.toString();
            int loanDays = calculateLoanDays(userRole, itemType, book.getGenre());
            String dueDate = loanDays > 0 ? issue.plusDays(loanDays).toString() : "";
            book.setLastIssueDate(issueDate);
            recordHistory(book, borrowerName, userRole, itemType, issueDate, dueDate, ACTION_BORROW);
        }
        return borrowed;
    }

    @Override
    public boolean returnBook(String isbn) {
        return returnBook(isbn, "Unknown", "Unknown");
    }

    @Override
    public boolean returnBook(String isbn, String borrowerName, String userRole) {
        Book book = getBookByIsbn(isbn);
        if (book == null) {
            return false;
        }

        String itemType = normalizeItemType(book.getItemType());

        if (isAccessOnlyType(itemType)) {
            return false;
        }

        boolean returned = book.returnBook();
        if (returned) {
            recordHistory(book, borrowerName, userRole, itemType, LocalDate.now().toString(), "", ACTION_RETURN);
        }
        return returned;
    }

    @Override
    public boolean updateBookInfo(String isbn, String title, String author, String genre) {
        Book book = getBookByIsbn(isbn);
        if (book == null) {
            return false;
        }

        String oldTitle = book.getTitle();
        String oldAuthor = book.getAuthor();
        String oldGenre = book.getGenre();

        if (title != null && !title.trim().isEmpty()) {
            book.setTitle(title.trim());
        }
        if (author != null && !author.trim().isEmpty()) {
            book.setAuthor(author.trim());
        }
        if (genre != null && !genre.trim().isEmpty()) {
            book.setGenre(genre.trim());
        }

        reindexBook(book, oldTitle, oldAuthor, oldGenre);

        return true;
    }

    @Override
    public boolean deleteBook(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        List<Book> removed = catalog.remove(isbn);
        if (removed == null || removed.isEmpty()) {
            return false;
        }
        for (Book book : removed) {
            unindexBook(book);
        }
        return true;
    }

    @Override
    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (List<Book> bucket : catalog.values()) {
            for (Book book : bucket) {
                if (isAccessOnlyType(book.getItemType()) || book.getAvailableCopies() > 0) {
                    available.add(book);
                    break;
                }
            }
        }
        return available;
    }

    @Override
    public List<BorrowRecord> getBorrowHistory() {
        return new ArrayList<>(borrowHistory);
    }

    @Override
    public List<Book> getTopBorrowedBooks(int limit) {
        List<Book> books = new ArrayList<>();
        for (List<Book> bucket : catalog.values()) {
            books.addAll(bucket);
        }
        books.sort((left, right) -> {
            int countCompare = Integer.compare(right.getBorrowCount(), left.getBorrowCount());
            if (countCompare != 0) {
                return countCompare;
            }
            String leftTitle = left.getTitle() == null ? "" : left.getTitle();
            String rightTitle = right.getTitle() == null ? "" : right.getTitle();
            return leftTitle.compareToIgnoreCase(rightTitle);
        });

        if (limit <= 0 || books.isEmpty()) {
            return new ArrayList<>();
        }

        int cappedLimit = Math.min(limit, books.size());
        return new ArrayList<>(books.subList(0, cappedLimit));
    }

    @Override
    public int getActiveBorrowCountForUser(String borrowerName) {
        return getActiveBorrowCount(borrowerName, null);
    }

    @Override
    public int getActiveBorrowCountForUserByType(String borrowerName, String itemType) {
        return getActiveBorrowCount(borrowerName, itemType);
    }

    @Override
    public void saveBorrowHistory() {
        historyPersistence.saveHistory(borrowHistory);
    }

    @Override
    public void loadBorrowHistory() {
        this.borrowHistory = historyPersistence.loadHistory();
    }

    private boolean isUGStudentRole(String userRole) {
        return isRole(userRole, "ug_student");
    }

    private boolean isExternalVisitorRole(String userRole) {
        return isRole(userRole, "external visitor");
    }

    private boolean isFacultyRole(String userRole) {
        return isRole(userRole, "faculty");
    }

    private boolean isEBookType(String itemType) {
        return itemType != null && ITEM_TYPE_EBOOK.equalsIgnoreCase(itemType.trim());
    }

    private boolean isEJournalType(String itemType) {
        return itemType != null && ITEM_TYPE_EJOURNAL.equalsIgnoreCase(itemType.trim());
    }

    private boolean isDatabaseType(String itemType) {
        return itemType != null && ITEM_TYPE_DATABASE.equalsIgnoreCase(itemType.trim());
    }

    private boolean isAccessOnlyType(String itemType) {
        return isEJournalType(itemType) || isDatabaseType(itemType);
    }

    private String normalizeItemType(String itemType) {
        // Default to Book unless other explicit type matches
        if (isEBookType(itemType)) {
            return ITEM_TYPE_EBOOK;
        }
        if (isEJournalType(itemType)) {
            return ITEM_TYPE_EJOURNAL;
        }
        if (isDatabaseType(itemType)) {
            return ITEM_TYPE_DATABASE;
        }
        return ITEM_TYPE_BOOK;
    }

    private int getBorrowLimitForRoleAndType(String userRole, String itemType) {
        if (isAccessOnlyType(itemType)) {
            return -1;
        }
        if (isUGStudentRole(userRole)) {
            return UG_STUDENT_MAX_BOOK_BORROWS;
        }
        if (isFacultyRole(userRole)) {
            return FACULTY_MAX_BOOK_BORROWS;
        }
        if (isExternalVisitorRole(userRole)) {
            return -1;
        }
        return -1;
    }

    // New loan period rules:
    // - Books and E-Books: 14 days by default
    // - Non-course books (specific genres): 28 days
    private int calculateLoanDays(String userRole, String itemType, String genre) {
        if (itemType == null) {
            return 0;
        }
        String normalizedType = normalizeItemType(itemType);
        // Access-only items aren't borrowed
        if (isAccessOnlyType(normalizedType)) {
            return 0;
        }

        // Non-course genres get extended loan period
        if (isNonCourseGenre(genre)) {
            return 28;
        }

        // Default for book-like items
        if (ITEM_TYPE_BOOK.equalsIgnoreCase(normalizedType) || ITEM_TYPE_EBOOK.equalsIgnoreCase(normalizedType)) {
            return 14;
        }

        // Fallback to 14 days
        return 14;
    }

    private boolean isNonCourseGenre(String genre) {
        if (genre == null) {
            return false;
        }
        String g = genre.trim().toLowerCase();
        String[] nonCourse = new String[] {
                "contemporary fiction",
                "science fiction",
                "self-help",
                "psychological thriller",
                "history",
                "fantasy",
                "memoir"
        };
        for (String n : nonCourse) {
            if (n.equalsIgnoreCase(g)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRole(String userRole, String expectedRole) {
        return userRole != null && expectedRole.equalsIgnoreCase(userRole.trim());
    }

    private int getActiveBorrowCount(String borrowerName, String itemType) {
        if (borrowerName == null || borrowerName.trim().isEmpty()) {
            return 0;
        }

        String normalizedType = itemType == null ? null : normalizeItemType(itemType);
        int activeCount = 0;
        for (BorrowRecord record : borrowHistory) {
            if (!borrowerName.equals(record.getBorrowerName())) {
                continue;
            }

            if (normalizedType != null) {
                String recordType = normalizeItemType(record.getItemType());
                if (!recordType.equalsIgnoreCase(normalizedType)) {
                    continue;
                }
            }

            if (ACTION_BORROW.equalsIgnoreCase(record.getAction())) {
                activeCount++;
            } else if (ACTION_RETURN.equalsIgnoreCase(record.getAction()) && activeCount > 0) {
                activeCount--;
            }
        }
        return activeCount;
    }

    private void recordHistory(
            Book book,
            String borrowerName,
            String userRole,
            String itemType,
            String issueDate,
            String dueDate,
            String action) {
        borrowHistory.add(new BorrowRecord(
                book.getIsbn(),
                book.getTitle(),
                borrowerName,
                userRole,
                itemType,
                issueDate,
                dueDate,
                action));
    }
}