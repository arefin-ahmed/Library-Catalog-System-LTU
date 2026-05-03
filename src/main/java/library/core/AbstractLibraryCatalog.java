package library.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import library.models.Book;
import library.models.BorrowRecord;
import library.persistence.CatalogPersistence;

public abstract class AbstractLibraryCatalog { // Index Maps
    protected Map<String, List<Book>> catalog; // search book by ISBN, title, author, genre
    protected Map<String, List<Book>> titleIndex;
    protected Map<String, List<Book>> authorIndex;
    protected Map<String, List<Book>> genreIndex;
    protected CatalogPersistence persistence;

    public AbstractLibraryCatalog(CatalogPersistence persistence) {
        this.persistence = persistence; // Assign persistence in storage system
        this.catalog = new HashMap<>();
        this.titleIndex = new HashMap<>(); // Create empty HashMaps and restore previous data
        this.authorIndex = new HashMap<>();
        this.genreIndex = new HashMap<>();
        loadCatalog();
    }

    public void addBook(Book book) {
        if (book == null) {
            return;
        }

        String isbn = normalizedValueOrNull(book.getIsbn());
        if (isbn == null) {
            return;
        }

        List<Book> bucket = catalog.get(isbn);
        if (bucket == null) {
            bucket = new ArrayList<>();
            catalog.put(isbn, bucket);
        }

        if (bucket.isEmpty()) {
            bucket.add(book);
            indexBook(book);
            return;
        }

        Book existing = bucket.get(0);
        String oldTitle = existing.getTitle();
        String oldAuthor = existing.getAuthor();
        String oldGenre = existing.getGenre();
        existing.setTotalCopies(existing.getTotalCopies() + book.getTotalCopies());
        existing.setAvailableCopies(existing.getAvailableCopies() + book.getAvailableCopies());

        String newTitle = normalizedValueOrNull(book.getTitle());
        if (newTitle != null) {
            existing.setTitle(newTitle);
        }
        String newAuthor = normalizedValueOrNull(book.getAuthor());
        if (newAuthor != null) {
            existing.setAuthor(newAuthor);
        }
        String newGenre = normalizedValueOrNull(book.getGenre());
        if (newGenre != null) {
            existing.setGenre(newGenre);
        }

        reindexBook(existing, oldTitle, oldAuthor, oldGenre);
    }

    public Book getBookByIsbn(String isbn) {
        List<Book> bucket = catalog.get(isbn);
        if (bucket == null || bucket.isEmpty()) {
            return null;
        }
        return bucket.get(0);
    }

    public Map<String, List<Book>> getAllBooks() {
        return new HashMap<>(catalog);
    }

    public void saveCatalog() {
        try {
            persistence.saveCatalog(catalog);
        } catch (Exception e) {
            System.out.println("Error saving catalog: " + e.getMessage());
        }
    }

    public void loadCatalog() {
        try {
            Map<String, List<Book>> loaded = persistence.loadCatalog();
            if (loaded != null) {
                catalog.clear();
                catalog.putAll(loaded);
                rebuildIndexes();
            }
        } catch (Exception e) {
            System.out.println("Error loading catalog: " + e.getMessage());
        }
    }

    protected void rebuildIndexes() {
        titleIndex.clear();
        authorIndex.clear();
        genreIndex.clear();
        for (List<Book> bucket : catalog.values()) {
            for (Book book : bucket) {
                indexBook(book);
            }
        }
    }

    protected void reindexBook(Book book, String oldTitle, String oldAuthor, String oldGenre) {
        if (book == null) {
            return;
        }
        removeFromIndex(titleIndex, oldTitle, book);
        removeFromIndex(authorIndex, oldAuthor, book);
        removeFromIndex(genreIndex, oldGenre, book);
        indexBook(book);
    }

    protected void indexBook(Book book) {
        if (book == null) {
            return;
        }
        addToIndex(titleIndex, book.getTitle(), book);
        addToIndex(authorIndex, book.getAuthor(), book);
        addToIndex(genreIndex, book.getGenre(), book);
    }

    protected void unindexBook(Book book) {
        if (book == null) {
            return;
        }
        removeFromIndex(titleIndex, book.getTitle(), book);
        removeFromIndex(authorIndex, book.getAuthor(), book);
        removeFromIndex(genreIndex, book.getGenre(), book);
    }

    protected String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    protected String normalizedValueOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    protected List<Book> searchIndex(Map<String, List<Book>> index, String query) {
        String key = normalizeKey(query);
        if (key.isEmpty()) {
            return new ArrayList<>();
        }
        List<Book> exact = index.get(key);
        if (exact != null) {
            return new ArrayList<>(exact);
        }

        Set<Book> results = new LinkedHashSet<>();
        for (Map.Entry<String, List<Book>> entry : index.entrySet()) {
            if (entry.getKey().contains(key)) {
                results.addAll(entry.getValue());
            }
        }
        return new ArrayList<>(results);
    }

    private void addToIndex(Map<String, List<Book>> index, String key, Book book) {
        for (String normalized : buildIndexKeys(key)) {
            List<Book> books = index.get(normalized);
            if (books == null) {
                books = new ArrayList<>();
                index.put(normalized, books);
            }
            if (!books.contains(book)) {
                books.add(book);
            }
        }
    }

    private void removeFromIndex(Map<String, List<Book>> index, String key, Book book) {
        for (String normalized : buildIndexKeys(key)) {
            List<Book> books = index.get(normalized);
            if (books == null) {
                continue;
            }
            books.remove(book);
            if (books.isEmpty()) {
                index.remove(normalized);
            }
        }
    }

    private List<String> buildIndexKeys(String value) {
        Set<String> keys = new LinkedHashSet<>();
        String normalized = normalizeKey(value);
        if (!normalized.isEmpty()) {
            keys.add(normalized);
            String[] tokens = normalized.split("[^a-z0-9]+");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    keys.add(token);
                }
            }
        }
        return new ArrayList<>(keys);
    }

    public abstract java.util.List<Book> searchByTitle(String title);

    public abstract java.util.List<Book> searchByAuthor(String author);

    public abstract java.util.List<Book> searchByGenre(String genre);

    public abstract boolean borrowBook(String isbn);

    public abstract boolean returnBook(String isbn);

    public abstract boolean updateBookInfo(String isbn, String title, String author, String genre);

    public abstract boolean deleteBook(String isbn);

    public abstract List<Book> getAvailableBooks();

    public abstract boolean borrowBook(String isbn, String borrowerName, String userRole);

    public abstract boolean returnBook(String isbn, String borrowerName, String userRole);

    public abstract List<BorrowRecord> getBorrowHistory();

    public abstract int getActiveBorrowCountForUser(String borrowerName);

    public abstract int getActiveBorrowCountForUserByType(String borrowerName, String itemType);

    public abstract List<Book> getTopBorrowedBooks(int limit);

    public abstract void saveBorrowHistory();

    public abstract void loadBorrowHistory();
}