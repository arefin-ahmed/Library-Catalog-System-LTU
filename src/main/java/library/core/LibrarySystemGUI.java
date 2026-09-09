package library.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import javax.swing.BorderFactory;
// import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import library.impl.FileCatalogPersistence;
import library.impl.LibraryCatalogImpl;
import library.models.AdminUser;
import library.models.Book;
import library.models.BorrowRecord;
import library.models.ExternalVisitor;
import library.models.FacultyUser;
import library.models.UG_Student;
import library.models.User;
import library.util.textfile;

public class LibrarySystemGUI extends JFrame {
    // private static final String LOGO = "/assets/LTU2.png";
    private static final String[] TYPES = { "Book", "E-Book", "E-Journal", "Database" };
    private static final String[] GENRES = { "Art, Culture and History", "Business", "Chemistry and Physics",
            "Communication", "Computer science", "Economics", "Education", "Engineering", "Environment Management",
            "Health and Population", "Language and Literature", "Mathematics and Statistics",
            "Political Science and Public Administration", "Science - General", "Social Science",
            "General Collection" };
    private static final int UG_LIMIT = 3;
    private static final int FACULTY_LIMIT = 10;

    private final AbstractLibraryCatalog catalog = new LibraryCatalogImpl(
            new FileCatalogPersistence("txt files/catalog.txt"));
    private User currentUser;
    private JLabel sessionLabel;
    private DefaultTableModel model;
    private JTable table;
    private JButton addButton, updateButton, deleteButton, searchButton, showAllButton, availableButton, borrowButton,
            returnButton, logoutButton;

    public LibrarySystemGUI() {
        setTitle("Lulea University Library");
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
        login();
        refreshBooks();
    }

    public static void main(String[] args) {
        new LibrarySystemGUI().setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(23, 70, 120));
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));

        JLabel title = new JLabel("Lulea University Library", SwingConstants.LEFT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title, BorderLayout.CENTER);

        sessionLabel = new JLabel("Not logged in", SwingConstants.RIGHT);
        sessionLabel.setForeground(Color.WHITE);
        sessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        header.add(sessionLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[] { "ISBN", "Title", "Author", "Genre", "Type", "Available", "Borrowed" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(1000, 360));
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 4, 8, 8));
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        buttons.setBackground(Color.WHITE);

        addButton = button("Add Book", e -> addBook());
        updateButton = button("Update Book", e -> updateBook());
        deleteButton = button("Delete Book", e -> deleteBook());
        searchButton = button("Search", e -> searchBooks());
        showAllButton = button("Show All", e -> refreshBooks());
        availableButton = button("Available", e -> showAvailableBooks());
        borrowButton = button("Borrow/Access", e -> borrowBook());
        returnButton = button("Return", e -> returnBook());
        logoutButton = button("Logout", e -> logout());

        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(searchButton);
        buttons.add(showAllButton);
        buttons.add(availableButton);
        buttons.add(borrowButton);
        buttons.add(returnButton);
        add(buttons, BorderLayout.SOUTH);
        add(logoutButton, BorderLayout.EAST);
        applyRolePermissions();
    }

    private JButton button(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void login() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JCheckBox show = new JCheckBox("Show password");
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : '\u2022'));
        panel.add(new JLabel("Username:"));
        panel.add(username);
        panel.add(new JLabel("Password:"));
        panel.add(password);
        panel.add(show);

        while (currentUser == null) {
            int choice = JOptionPane.showConfirmDialog(this, panel, "Log in", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                dispose();
                return;
            }
            currentUser = authenticate(username.getText().trim(), new String(password.getPassword()));
            if (currentUser == null)
                JOptionPane.showMessageDialog(this, "Invalid login. Admin uses admin/admin123.");
        }
        sessionLabel.setText("Logged in as: " + currentUser.getRole() + " (" + currentUser.getUsername() + ")");
        applyRolePermissions();
    }

    private User authenticate(String username, String password) {
        if (username.isEmpty())
            return null;
        User admin = new AdminUser("admin", "admin123");
        if (admin.getUsername().equalsIgnoreCase(username) && admin.checkPassword(password))
            return admin;
        return authenticateFromUsersFile(username, password);
    }

    private User authenticateFromUsersFile(String username, String password) {
        File file = new File("txt files/users.txt");
        if (!file.exists())
            return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            if (header == null)
                return null;
            String[] headers = textfile.parseCsvLine(header);
            int userIndex = indexOf(headers, "username"), passIndex = indexOf(headers, "password"),
                    typeIndex = indexOf(headers, "type");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] row = textfile.parseCsvLine(line);
                if (userIndex < 0 || passIndex < 0 || row.length <= Math.max(userIndex, passIndex))
                    continue;
                if (!username.equals(row[userIndex].trim()) || !password.equals(row[passIndex].trim()))
                    continue;
                String type = typeIndex >= 0 && typeIndex < row.length ? row[typeIndex].trim() : "UG_Student";
                if ("Admin".equalsIgnoreCase(type))
                    return new AdminUser(username, password);
                if ("Faculty".equalsIgnoreCase(type))
                    return new FacultyUser(username, password);
                if ("External Visitor".equalsIgnoreCase(type))
                    return new ExternalVisitor(username, password);
                return new UG_Student(username, password);
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private int indexOf(String[] values, String target) {
        if (values == null || target == null)
            return -1;
        for (int i = 0; i < values.length; i++)
            if (target.equalsIgnoreCase(values[i].trim()))
                return i;
        return -1;
    }

    private void addBook() {
        if (!isAdmin()) {
            info("Only admin can add books.");
            return;
        }
        JTextField isbn = new JTextField(), title = new JTextField(), author = new JTextField(),
                publisher = new JTextField(), copies = new JTextField();
        JComboBox<String> genre = new JComboBox<>(GENRES), type = new JComboBox<>(TYPES),
                course = new JComboBox<>(new String[] { "Course", "Non-Course" });
        JPanel panel = form(
                new String[] { "ISBN", "Title", "Author", "Genre", "Type", "Course Type", "Publisher", "Total Copies" },
                isbn, title, author, genre, type, course, publisher, copies);
        if (!ok(panel, "Add Book"))
            return;
        int total;
        try {
            total = Integer.parseInt(copies.getText().trim());
        } catch (NumberFormatException ex) {
            info("Total copies must be a number.");
            return;
        }
        if (total <= 0 || isbn.getText().trim().isEmpty() || title.getText().trim().isEmpty()
                || author.getText().trim().isEmpty() || publisher.getText().trim().isEmpty()) {
            info("Fill all fields and use a positive copy count.");
            return;
        }
        Book book = new Book(isbn.getText().trim(), title.getText().trim(), author.getText().trim(),
                genre.getSelectedItem().toString(), publisher.getText().trim(), type.getSelectedItem().toString(),
                total, total, 0, "");
        book.setCourseType(course.getSelectedItem().toString());
        catalog.addBook(book);
        persist();
        refreshBooks();
        info("Book added.");
    }

    private void updateBook() {
        if (!isAdmin()) {
            info("Only admin can update books.");
            return;
        }
        JTextField isbn = new JTextField(), title = new JTextField(), author = new JTextField();
        JComboBox<String> genre = new JComboBox<>(new String[] { "Keep current", "Art, Culture and History", "Business",
                "Chemistry and Physics", "Communication", "Computer science", "Economics", "Education", "Engineering",
                "Environment Management", "Health and Population", "Language and Literature",
                "Mathematics and Statistics", "Political Science and Public Administration", "Science - General",
                "Social Science", "General Collection" });
        JPanel panel = form(new String[] { "ISBN (required)", "Title", "Author", "Genre" }, isbn, title, author, genre);
        if (!ok(panel, "Update Book"))
            return;
        if (isbn.getText().trim().isEmpty()) {
            info("ISBN is required.");
            return;
        }
        String selectedGenre = genre.getSelectedItem().toString();
        boolean success = catalog.updateBookInfo(isbn.getText().trim(), title.getText().trim(), author.getText().trim(),
                "Keep current".equals(selectedGenre) ? "" : selectedGenre);
        if (success)
            persist();
        refreshBooks();
        info(success ? "Book updated." : "Book not found.");
    }

    private void deleteBook() {
        if (!isAdmin()) {
            info("Only admin can delete books.");
            return;
        }
        String isbn = prompt("ISBN to delete");
        if (isbn == null || isbn.trim().isEmpty())
            return;
        if (JOptionPane.showConfirmDialog(this, "Delete book " + isbn + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        boolean success = catalog.deleteBook(isbn.trim());
        if (success)
            persist();
        refreshBooks();
        info(success ? "Book deleted." : "Book not found.");
    }

    private void searchBooks() {
        String[] modes = { "ISBN", "Title", "Author", "Genre" };
        String mode = (String) JOptionPane.showInputDialog(this, "Search by:", "Search Books",
                JOptionPane.PLAIN_MESSAGE, null, modes, modes[0]);
        if (mode == null)
            return;
        String query = prompt("Enter " + mode);
        if (query == null || query.trim().isEmpty())
            return;
        List<Book> results = new ArrayList<>();
        if ("ISBN".equals(mode)) {
            Book found = catalog.getBookByIsbn(query.trim());
            if (found != null)
                results.add(found);
        } else if ("Title".equals(mode))
            results = catalog.searchByTitle(query.trim());
        else if ("Author".equals(mode))
            results = catalog.searchByAuthor(query.trim());
        else
            results = catalog.searchByGenre(query.trim());
        showBooks(results);
    }

    private void refreshBooks() {
        List<Book> books = new ArrayList<>();
        for (List<Book> bucket : catalog.getAllBooks().values())
            books.addAll(bucket);
        showBooks(books);
    }

    private void showAvailableBooks() {
        showBooks(catalog.getAvailableBooks());
    }

    private void borrowBook() {
        if (!canBorrow()) {
            info("This role cannot borrow items.");
            return;
        }
        String isbn = table.getSelectedRow() >= 0 ? String.valueOf(model.getValueAt(table.getSelectedRow(), 0)).trim()
                : prompt("Enter ISBN to borrow");
        if (isbn == null)
            return;
        isbn = isbn.trim();
        Book book = catalog.getBookByIsbn(isbn);
        if (book == null) {
            info("Book not found.");
            return;
        }
        String type = normalizeType(book.getItemType());
        if (isExternalVisitor() && "Book".equalsIgnoreCase(type)) {
            info("External visitors can only access E-Book, E-Journal, and Database items.");
            return;
        }
        if (isAccessOnly(type)) {
            if (catalog.borrowBook(isbn, currentUser.getUsername(), currentUser.getRole())) {
                persist();
                showAvailableBooks();
                info(type + " access recorded.");
            } else
                info(type + " access failed.");
            return;
        }
        int limit = borrowLimit();
        int active = catalog.getActiveBorrowCountForUserByType(currentUser.getUsername(), type);
        if (limit > 0 && active >= limit) {
            info("Borrow limit reached for " + type + ".");
            return;
        }
        if (catalog.borrowBook(isbn, currentUser.getUsername(), currentUser.getRole())) {
            persist();
            showAvailableBooks();
            int days = loanDays(type, book.getGenre());
            info(type + " borrowed successfully" + (days > 0 ? ". Due in " + days + " days." : "."));
        } else
            info("Borrow failed.");
    }

    private void returnBook() {
        if (!canBorrow()) {
            info("This role cannot return items.");
            return;
        }
        String isbn = chooseBorrowedBookForReturn();
        if (isbn == null)
            return;
        Book book = catalog.getBookByIsbn(isbn);
        if (book != null && isAccessOnly(book.getItemType())) {
            info("No return needed for " + normalizeType(book.getItemType()) + ".");
            return;
        }
        if (catalog.returnBook(isbn, currentUser.getUsername(), currentUser.getRole())) {
            persist();
            showAvailableBooks();
            info("Book returned.");
        } else
            info("Return failed.");
    }

    private void showBooks(List<Book> books) {
        model.setRowCount(0);
        for (Book book : books)
            model.addRow(new Object[] { book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                    normalizeType(book.getItemType()), book.getAvailableCopies(), book.getBorrowCount() });
    }

    private JPanel form(String[] labels, Object... fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i] + ":"));
            panel.add((java.awt.Component) fields[i]);
        }
        return panel;
    }

    private boolean ok(JPanel panel, String title) {
        return JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private String prompt(String message) {
        return JOptionPane.showInputDialog(this, message + ":");
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void persist() {
        catalog.saveCatalog();
        catalog.saveBorrowHistory();
    }

    private String chooseBorrowedBookForReturn() {
        List<Book> borrowedBooks = getBorrowedBooksForCurrentUser();
        if (borrowedBooks.isEmpty()) {
            info("You have no borrowed books to return.");
            return null;
        }

        String[] options = new String[borrowedBooks.size()];
        for (int i = 0; i < borrowedBooks.size(); i++) {
            Book book = borrowedBooks.get(i);
            options[i] = book.getIsbn() + " | " + book.getTitle() + " | " + normalizeType(book.getItemType());
        }

        String selection = (String) JOptionPane.showInputDialog(
                this,
                "Select the book you want to return:",
                "Return Book",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
        if (selection == null) {
            return null;
        }

        int separator = selection.indexOf(" |");
        return separator > 0 ? selection.substring(0, separator).trim() : selection.trim();
    }

    private List<Book> getBorrowedBooksForCurrentUser() {
        List<Book> borrowed = new ArrayList<>();
        Map<String, BorrowRecord> active = new LinkedHashMap<>();

        for (BorrowRecord record : catalog.getBorrowHistory()) {
            if (record == null || currentUser == null || !currentUser.getUsername().equals(record.getBorrowerName())) {
                continue;
            }

            String isbn = record.getIsbn();
            if (isbn == null || isbn.trim().isEmpty()) {
                continue;
            }

            if ("BORROW".equalsIgnoreCase(record.getAction())) {
                active.put(isbn.trim() + "#" + active.size(), record);
                continue;
            }

            if ("RETURN".equalsIgnoreCase(record.getAction())) {
                String targetKey = null;
                for (Map.Entry<String, BorrowRecord> entry : active.entrySet()) {
                    if (isbn.trim().equalsIgnoreCase(entry.getValue().getIsbn())) {
                        targetKey = entry.getKey();
                        break;
                    }
                }
                if (targetKey != null) {
                    active.remove(targetKey);
                }
            }
        }

        for (BorrowRecord record : active.values()) {
            Book book = catalog.getBookByIsbn(record.getIsbn());
            if (book != null) {
                borrowed.add(book);
            } else {
                borrowed.add(new Book(record.getIsbn(), record.getBookTitle(), "", "", "", record.getItemType(), 1, 1,
                        0, ""));
            }
        }
        return borrowed;
    }

    private void applyRolePermissions() {
        boolean loggedIn = currentUser != null, admin = loggedIn && currentUser.canAddBook(),
                borrower = loggedIn && currentUser.canBorrowBook();
        addButton.setVisible(admin);
        updateButton.setVisible(admin);
        deleteButton.setVisible(admin);
        searchButton.setVisible(loggedIn);
        showAllButton.setVisible(loggedIn);
        availableButton.setVisible(borrower);
        borrowButton.setVisible(borrower);
        returnButton.setVisible(borrower);
        logoutButton.setVisible(loggedIn);
        addButton.setEnabled(admin);
        updateButton.setEnabled(admin);
        deleteButton.setEnabled(admin);
        searchButton.setEnabled(loggedIn);
        showAllButton.setEnabled(loggedIn);
        availableButton.setEnabled(borrower);
        borrowButton.setEnabled(borrower);
        returnButton.setEnabled(borrower);
        logoutButton.setEnabled(loggedIn);
        revalidate();
        repaint();
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Logout now?", "Logout",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        currentUser = null;
        sessionLabel.setText("Not logged in");
        applyRolePermissions();
        login();
        refreshBooks();
    }

    private boolean isAdmin() {
        return currentUser != null && currentUser.canAddBook();
    }

    private boolean canBorrow() {
        return currentUser != null && currentUser.canBorrowBook();
    }

    private boolean isExternalVisitor() {
        return currentUser != null && "External Visitor".equalsIgnoreCase(currentUser.getRole());
    }

    private String normalizeType(String itemType) {
        if (itemType == null)
            return "Book";
        String value = itemType.trim();
        if ("E-Book".equalsIgnoreCase(value))
            return "E-Book";
        if ("E-Journal".equalsIgnoreCase(value))
            return "E-Journal";
        if ("Database".equalsIgnoreCase(value))
            return "Database";
        return "Book";
    }

    private boolean isAccessOnly(String itemType) {
        String type = normalizeType(itemType);
        return "E-Journal".equalsIgnoreCase(type) || "Database".equalsIgnoreCase(type);
    }

    private int borrowLimit() {
        if (currentUser == null || isExternalVisitor())
            return -1;
        if ("UG_Student".equalsIgnoreCase(currentUser.getRole()))
            return UG_LIMIT;
        if ("Faculty".equalsIgnoreCase(currentUser.getRole()))
            return FACULTY_LIMIT;
        return -1;
    }

    private int loanDays(String type, String genre) {
        if (isAccessOnly(type))
            return 0;
        return isNonCourseGenre(genre) ? 28 : 14;
    }

    private boolean isNonCourseGenre(String genre) {
        if (genre == null)
            return false;
        String g = genre.trim().toLowerCase();
        String[] nonCourse = { "contemporary fiction", "science fiction", "self-help", "psychological thriller",
                "history", "fantasy", "memoir" };
        for (String value : nonCourse)
            if (value.equalsIgnoreCase(g))
                return true;
        return false;
    }
}