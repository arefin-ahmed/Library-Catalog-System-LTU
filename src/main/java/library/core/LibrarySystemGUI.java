package library.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
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
import library.models.FacultyUser;
import library.models.ExternalVisitor;
import library.models.UG_Student;
import library.models.User;
import library.util.textfile;

//    Swing UI for the Library System. window-based application 

public class LibrarySystemGUI extends JFrame {
    private static final String LOGO_RESOURCE = "/assets/LTU2.png";
    private static final String ITEM_TYPE_BOOK = "Book";
    private static final String ITEM_TYPE_EBOOK = "E-Book";
    private static final String ITEM_TYPE_EJOURNAL = "E-Journal";
    private static final String ITEM_TYPE_DATABASE = "Database";

    private static final int UG_STUDENT_MAX_BOOK_BORROWS = 3;
    // private static final int UG_STUDENT_LOAN_DAYS = 10;

    private static final int FACULTY_MAX_BOOK_BORROWS = 10;
    // private static final int FACULTY_LOAN_DAYS = 30;

    private static final String[] GENRE_OPTIONS = {
            "Art, Culture and History",
            "Business",
            "Chemistry and Physics",
            "Communication",
            "Computer science",
            "Economics",
            "Education",
            "Engineering",
            "Environment Management",
            "Health and Population",
            "Language and Literature",
            "Mathematics and Statistics",
            "Political Science and Public Administration",
            "Science - General",
            "Social Science",
            "General Collection"
    };

    private static class UsersFileData {
        String[] headers;
        List<String[]> rows;

        UsersFileData(String[] headers, List<String[]> rows) {
            this.headers = headers;
            this.rows = rows;
        }
    }

    private final AbstractLibraryCatalog catalog;
    private User currentUser;
    private JLabel sessionLabel;

    private JButton addButton;
    private JButton addUserButton;
    private JButton viewUsersButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton searchButton;
    private JButton showAllButton;
    private JButton availableButton;
    private JButton borrowButton;
    private JButton returnButton;
    private JButton historyButton;
    private JButton topBorrowedButton;
    private JButton logoutButton;

    private DefaultTableModel tableModel;
    private JTable table;

    public LibrarySystemGUI() {
        this.catalog = new LibraryCatalogImpl(new FileCatalogPersistence("txt files/catalog.txt"));

        setTitle("Luleå University Library");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
        showLoginDialog();
        refreshBookViewForCurrentRole();
    }

    private void buildUI() {
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        sessionLabel = new JLabel("Not logged in", SwingConstants.RIGHT);
        sessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel headerPanel = createHeaderPanel(sessionLabel);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        tableModel = new DefaultTableModel(
                new Object[] { "ISBN", "Title", "Author", "Genre", "Item Type", "Available Copies", "Borrow Count" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1100, 280));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        addButton = new JButton("Add Book");
        addUserButton = new JButton("Add User");
        viewUsersButton = new JButton("View Users");
        updateButton = new JButton("Update Book");
        deleteButton = new JButton("Delete Book");
        searchButton = new JButton("Search");
        showAllButton = new JButton("Show All");
        availableButton = new JButton("Available Books");
        borrowButton = new JButton("Borrow/Access");
        returnButton = new JButton("Return Book");
        historyButton = new JButton("Borrow History");
        topBorrowedButton = new JButton("Top Borrowed");
        logoutButton = new JButton("Logout");

        addButton.addActionListener(e -> addBookFromInput());
        addUserButton.addActionListener(e -> addUserFromInput());
        viewUsersButton.addActionListener(e -> showAllUsers());
        updateButton.addActionListener(e -> updateBookFromInput());
        deleteButton.addActionListener(e -> deleteBookByIsbn());
        searchButton.addActionListener(e -> searchBooks());
        showAllButton.addActionListener(e -> showAllBooks());
        availableButton.addActionListener(e -> showAvailableBooks());
        borrowButton.addActionListener(e -> borrowBookForCurrentUser());
        returnButton.addActionListener(e -> returnBookForCurrentUser());
        historyButton.addActionListener(e -> showBorrowHistory());
        topBorrowedButton.addActionListener(e -> showTopBorrowedBooks());
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(addButton);
        buttonPanel.add(addUserButton);
        buttonPanel.add(viewUsersButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(showAllButton);
        buttonPanel.add(availableButton);
        buttonPanel.add(borrowButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(topBorrowedButton);
        buttonPanel.add(logoutButton);

        applyRolePermissions();

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);
    }

    private void addBookFromInput() {
        if (currentUser == null || !currentUser.canAddBook()) {
            JOptionPane.showMessageDialog(this, "Only Admin can add books.");
            return;
        }

        JTextField isbnInput = new JTextField();
        JTextField titleInput = new JTextField();
        JTextField authorInput = new JTextField();
        JComboBox<String> genreInput = new JComboBox<>(GENRE_OPTIONS);
        JComboBox<String> itemTypeInput = new JComboBox<>(
                new String[] { ITEM_TYPE_BOOK, ITEM_TYPE_EBOOK, ITEM_TYPE_EJOURNAL, ITEM_TYPE_DATABASE });
        JComboBox<String> courseTypeInput = new JComboBox<>(new String[] { "Course", "Non-Course" });
        JTextField publisherInput = new JTextField();
        JTextField totalCopiesInput = new JTextField();

        JPanel addPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        addPanel.add(new JLabel("ISBN:"));
        addPanel.add(isbnInput);
        addPanel.add(new JLabel("Title:"));
        addPanel.add(titleInput);
        addPanel.add(new JLabel("Author:"));
        addPanel.add(authorInput);
        addPanel.add(new JLabel("Genre:"));
        addPanel.add(genreInput);
        addPanel.add(new JLabel("Item Type:"));
        addPanel.add(itemTypeInput);
        addPanel.add(new JLabel("Course Type:"));
        addPanel.add(courseTypeInput);
        addPanel.add(new JLabel("Publisher:"));
        addPanel.add(publisherInput);
        addPanel.add(new JLabel("Total Copies:"));
        addPanel.add(totalCopiesInput);

        int option = JOptionPane.showConfirmDialog(
                this,
                addPanel,
                "Add New Book",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String isbn = isbnInput.getText().trim();
        String title = titleInput.getText().trim();
        String author = authorInput.getText().trim();
        String genre = String.valueOf(genreInput.getSelectedItem()).trim();
        if ("Keep current".equals(genre)) {
            genre = "";
        }
        String itemType = normalizeItemType(String.valueOf(itemTypeInput.getSelectedItem()));
        String publisher = publisherInput.getText().trim();
        String totalCopiesText = totalCopiesInput.getText().trim();
        String courseType = String.valueOf(courseTypeInput.getSelectedItem()).trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty() || publisher.isEmpty()
                || totalCopiesText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill ISBN, Title, Author, Genre, Item Type, Publisher, and Total Copies.");
            return;
        }

        int totalCopies;
        try {
            totalCopies = Integer.parseInt(totalCopiesText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total Copies must be a valid number.");
            return;
        }

        if (totalCopies <= 0) {
            JOptionPane.showMessageDialog(this, "Total Copies must be greater than 0.");
            return;
        }

        Book book = new Book(isbn, title, author, genre, publisher, itemType, totalCopies, totalCopies, 0, "");
        book.setCourseType(courseType);
        catalog.addBook(book);
        persistChanges();
        showAllBooks();

        JOptionPane.showMessageDialog(this, "Book added successfully.");
    }

    private void addUserFromInput() {
        if (currentUser == null || !currentUser.canAddBook()) {
            JOptionPane.showMessageDialog(this, "Only Admin can add users.");
            return;
        }

        JTextField userIdInput = new JTextField();
        JTextField nameInput = new JTextField();
        JTextField usernameInput = new JTextField();
        JTextField departmentInput = new JTextField();
        JComboBox<String> typeInput = new JComboBox<>(new String[] { "UG_Student", "External Visitor", "Faculty" });
        JTextField contactInput = new JTextField();

        JPanel addUserPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        addUserPanel.add(new JLabel("User ID:"));
        addUserPanel.add(userIdInput);
        addUserPanel.add(new JLabel("Name:"));
        addUserPanel.add(nameInput);
        addUserPanel.add(new JLabel("Username:"));
        addUserPanel.add(usernameInput);
        addUserPanel.add(new JLabel("Department:"));
        addUserPanel.add(departmentInput);
        addUserPanel.add(new JLabel("UG Student/External Visitor/Faculty:"));
        addUserPanel.add(typeInput);
        addUserPanel.add(new JLabel("Contact No:"));
        addUserPanel.add(contactInput);

        int option = JOptionPane.showConfirmDialog(
                this,
                addUserPanel,
                "Add User",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String userId = userIdInput.getText().trim();
        String name = nameInput.getText().trim();
        String username = usernameInput.getText().trim();
        String department = departmentInput.getText().trim();
        String type = String.valueOf(typeInput.getSelectedItem());
        String contactNo = contactInput.getText().trim();
        String password = username + "123";

        if (userId.isEmpty() || name.isEmpty() || username.isEmpty() || department.isEmpty() || type.isEmpty()
                || contactNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all user fields.");
            return;
        }

        if (isUsernameAlreadyExists(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.");
            return;
        }

        boolean saved = saveUserRecord(userId, name, username, department, type, contactNo, password);
        if (saved) {
            JOptionPane.showMessageDialog(this, "User added successfully. Auto password: " + password);
        } else {
            JOptionPane.showMessageDialog(this, "Could not save user record.");
        }
    }

    private boolean isUsernameAlreadyExists(String username) {
        if (username == null) {
            return false;
        }

        UsersFileData usersFileData = loadUsersFileData();
        if (usersFileData == null) {
            return false;
        }

        int usernameIndex = indexOfIgnoreCase(usersFileData.headers, "username");
        if (usernameIndex < 0) {
            return false;
        }

        for (String[] parts : usersFileData.rows) {
            if (usernameIndex < parts.length && username.equalsIgnoreCase(parts[usernameIndex].trim())) {
                return true;
            }
        }

        return false;
    }

    private boolean saveUserRecord(String userId, String name, String username, String department, String type,
            String contactNo, String password) {
        File file = new File("txt files/users.txt");
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.write("userId,name,username,department,type,contactNo,password");
                writer.newLine();
            }

            writer.write(String.join(",",
                    textfile.escape(userId),
                    textfile.escape(name),
                    textfile.escape(username),
                    textfile.escape(department),
                    textfile.escape(type),
                    textfile.escape(contactNo),
                    textfile.escape(password)));
            writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void showAllUsers() {
        if (currentUser == null || !currentUser.canAddBook()) {
            JOptionPane.showMessageDialog(this, "Only Admin can view users.");
            return;
        }

        UsersFileData usersFileData = loadUsersFileData();
        if (usersFileData == null || usersFileData.headers.length == 0) {
            JOptionPane.showMessageDialog(this, "No users found.");
            return;
        }

        DefaultTableModel userModel = new DefaultTableModel(usersFileData.headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String[] parts : usersFileData.rows) {
            Object[] row = new Object[usersFileData.headers.length];
            for (int i = 0; i < usersFileData.headers.length; i++) {
                row[i] = i < parts.length ? parts[i] : "";
            }
            userModel.addRow(row);
        }

        JTable userTable = new JTable(userModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userTable.setRowHeight(20);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane pane = new JScrollPane(userTable);
        pane.setPreferredSize(new Dimension(860, 260));
        JOptionPane.showMessageDialog(this, pane, "All Users", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateBookFromInput() {
        if (currentUser == null || !currentUser.canUpdateBookInfo()) {
            JOptionPane.showMessageDialog(this, "Only Admin can update book information.");
            return;
        }

        JTextField isbnInput = new JTextField();
        JTextField titleInput = new JTextField();
        JTextField authorInput = new JTextField();
        JComboBox<String> genreInput = new JComboBox<>(buildGenreUpdateOptions());

        JPanel updatePanel = new JPanel(new GridLayout(4, 2, 8, 8));
        updatePanel.add(new JLabel("ISBN (required):"));
        updatePanel.add(isbnInput);
        updatePanel.add(new JLabel("Title (optional):"));
        updatePanel.add(titleInput);
        updatePanel.add(new JLabel("Author (optional):"));
        updatePanel.add(authorInput);
        updatePanel.add(new JLabel("Genre (optional):"));
        updatePanel.add(genreInput);

        int option = JOptionPane.showConfirmDialog(
                this,
                updatePanel,
                "Update Book Information",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String isbn = isbnInput.getText().trim();
        String title = titleInput.getText().trim();
        String author = authorInput.getText().trim();
        String genre = String.valueOf(genreInput.getSelectedItem()).trim();

        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ISBN to update a book.");
            return;
        }

        boolean success = catalog.updateBookInfo(isbn, title, author, genre);
        if (success) {
            persistChanges();
        }
        showAllBooks();
        JOptionPane.showMessageDialog(this,
                success ? "Book information updated." : "Update failed. Book not found.");
    }

    private void deleteBookByIsbn() {
        if (currentUser == null || !currentUser.canDeleteBook()) {
            JOptionPane.showMessageDialog(this, "Only Admin can delete books.");
            return;
        }

        String isbn = JOptionPane.showInputDialog(this, "Enter ISBN to delete:");
        if (isbn == null) {
            return;
        }
        isbn = isbn.trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ISBN to delete a book.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete book with ISBN " + isbn + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = catalog.deleteBook(isbn);
        if (success) {
            persistChanges();
        }
        showAllBooks();
        JOptionPane.showMessageDialog(this, success ? "Book deleted." : "Delete failed. Book not found.");
    }

    private void searchBooks() {
        String[] searchTypes = { "ISBN", "Title", "Author", "Genre" };
        String selectedType = (String) JOptionPane.showInputDialog(
                this,
                "Search by:",
                "Search Books",
                JOptionPane.PLAIN_MESSAGE,
                null,
                searchTypes,
                searchTypes[0]);

        if (selectedType == null) {
            return;
        }

        String query = JOptionPane.showInputDialog(this, "Enter " + selectedType + ":");
        if (query == null) {
            return;
        }

        query = query.trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search text cannot be empty.");
            return;
        }

        List<Book> results = new ArrayList<>();

        if ("ISBN".equals(selectedType)) {
            Book found = catalog.getBookByIsbn(query);
            if (found != null) {
                results.add(found);
            }
        } else if ("Title".equals(selectedType)) {
            results = catalog.searchByTitle(query);
        } else if ("Author".equals(selectedType)) {
            results = catalog.searchByAuthor(query);
        } else if ("Genre".equals(selectedType)) {
            results = catalog.searchByGenre(query);
        } else {
            JOptionPane.showMessageDialog(this, "Enter ISBN, Title, Author, or Genre to search.");
            return;
        }

        populateTable(results);
    }

    private void showAllBooks() {
        List<Book> books = new ArrayList<>();
        for (List<Book> bucket : catalog.getAllBooks().values()) {
            books.addAll(bucket);
        }
        populateTable(books);
    }

    private void refreshBookViewForCurrentRole() {
        showAllBooks();
    }

    private void showAvailableBooks() {
        populateTable(catalog.getAvailableBooks());
    }

    private void borrowBookForCurrentUser() {
        if (currentUser == null || !currentUser.canBorrowBook()) {
            JOptionPane.showMessageDialog(this, "Only UG Student, External Visitor, or Faculty can borrow items.");
            return;
        }

        String isbn = getIsbnFromSelectionOrInput("borrow");
        if (isbn == null) {
            return;
        }

        Book book = catalog.getBookByIsbn(isbn);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Borrow failed. Book not found.");
            return;
        }

        String itemType = normalizeItemType(book.getItemType());
        if (isAccessOnlyType(itemType)) {
            boolean accessed = catalog.borrowBook(isbn, currentUser.getUsername(), currentUser.getRole());
            if (accessed) {
                persistChanges();
                showAvailableBooks();
                JOptionPane.showMessageDialog(this, itemType + " access recorded.");
            } else {
                JOptionPane.showMessageDialog(this, itemType + " access failed.");
            }
            return;
        }
        if (isExternalVisitorRole(currentUser.getRole()) && ITEM_TYPE_BOOK.equalsIgnoreCase(itemType)) {
            JOptionPane.showMessageDialog(this,
                    "External Visitors can only access E-Book, E-Journal, and Database items.");
            return;
        }
        int activeBorrows = catalog.getActiveBorrowCountForUserByType(currentUser.getUsername(), itemType);
        int limit = getBorrowLimitForRoleAndType(currentUser.getRole(), itemType);
        if (limit > 0 && activeBorrows >= limit) {
            JOptionPane.showMessageDialog(this,
                    "Borrow limit reached: " + roleLabel(currentUser.getRole()) + " can keep at most " + limit
                            + " active " + itemType + " items.");
            return;
        }

        boolean success = catalog.borrowBook(isbn, currentUser.getUsername(), currentUser.getRole());
        if (success) {
            persistChanges();
            showAvailableBooks();
            int loanDays = calculateLoanDays(currentUser.getRole(), itemType, book.getGenre());
            if (loanDays > 0) {
                String dueDate = LocalDate.now().plusDays(loanDays).toString();
                JOptionPane.showMessageDialog(this,
                        itemType + " borrowed successfully. Due date: " + dueDate + " (" + loanDays + " days).");
            } else {
                JOptionPane.showMessageDialog(this, itemType + " borrowed successfully.");
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Borrow failed. Book not found, no available copies, or borrow limit reached.");
        }
    }

    private void returnBookForCurrentUser() {
        if (currentUser == null || !currentUser.canBorrowBook()) {
            JOptionPane.showMessageDialog(this, "Only UG Student, External Visitor, or Faculty can return items.");
            return;
        }

        String isbn = getIsbnFromSelectionOrInput("return");
        if (isbn == null) {
            return;
        }

        Book book = catalog.getBookByIsbn(isbn);
        if (book != null && isAccessOnlyType(book.getItemType())) {
            JOptionPane.showMessageDialog(this,
                    "Return is not required for " + normalizeItemType(book.getItemType()) + ".");
            return;
        }

        boolean success = catalog.returnBook(isbn, currentUser.getUsername(), currentUser.getRole());
        if (success) {
            persistChanges();
            showAvailableBooks();
            JOptionPane.showMessageDialog(this, "Book returned successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Return failed. Book not found or all copies are already returned.");
        }
    }

    private String getIsbnFromSelectionOrInput(String actionName) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            Object isbnValue = table.getValueAt(selectedRow, 0);
            if (isbnValue != null) {
                String isbnFromTable = isbnValue.toString().trim();
                if (!isbnFromTable.isEmpty()) {
                    return isbnFromTable;
                }
            }
        }

        String isbn = JOptionPane.showInputDialog(this, "Enter ISBN to " + actionName + ":");
        if (isbn == null) {
            return null;
        }

        isbn = isbn.trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ISBN cannot be empty.");
            return null;
        }

        return isbn;
    }

    private void persistChanges() {
        catalog.saveCatalog();
        catalog.saveBorrowHistory();
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book book : books) {
            tableModel.addRow(new Object[] {
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    normalizeItemType(book.getItemType()),
                    book.getAvailableCopies(),
                    book.getBorrowCount()
            });
        }
    }

    private void showBorrowHistory() {
        List<BorrowRecord> history = getHistoryForCurrentUser();
        Map<String, String> userTypes = loadUserTypesFromFile();

        DefaultTableModel historyModel = new DefaultTableModel(
                new Object[] { "Action", "ISBN", "Title", "User", "Type", "Item Type", "Issue Date", "Due Date" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (BorrowRecord record : history) {
            historyModel.addRow(new Object[] {
                    record.getAction(),
                    record.getIsbn(),
                    record.getBookTitle(),
                    record.getBorrowerName(),
                    resolveUserTypeForHistory(record.getBorrowerName(), record.getUserRole(), userTypes),
                    normalizeItemType(record.getItemType()),
                    record.getIssueDate(),
                    record.getDueDate()

            });
        }

        JTable historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setRowHeight(20);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane pane = new JScrollPane(historyTable);
        pane.setPreferredSize(new Dimension(760, 260));
        String dialogTitle = currentUser != null && currentUser.canViewAllBorrowHistory()
                ? "Borrow History (All Users)"
                : "My Borrow History";
        JOptionPane.showMessageDialog(this, pane, dialogTitle, JOptionPane.INFORMATION_MESSAGE);
    }

    private String normalizeItemType(String itemType) {
        // Only explicit special types are handled; default is Book
        if (ITEM_TYPE_EBOOK.equalsIgnoreCase(String.valueOf(itemType).trim())) {
            return ITEM_TYPE_EBOOK;
        }
        if (ITEM_TYPE_EJOURNAL.equalsIgnoreCase(String.valueOf(itemType).trim())) {
            return ITEM_TYPE_EJOURNAL;
        }
        if (ITEM_TYPE_DATABASE.equalsIgnoreCase(String.valueOf(itemType).trim())) {
            return ITEM_TYPE_DATABASE;
        }
        return ITEM_TYPE_BOOK;
    }

    private boolean isAccessOnlyType(String itemType) {
        if (itemType == null) {
            return false;
        }
        String normalized = normalizeItemType(itemType);
        return ITEM_TYPE_EJOURNAL.equalsIgnoreCase(normalized) || ITEM_TYPE_DATABASE.equalsIgnoreCase(normalized);
    }

    private int getBorrowLimitForRoleAndType(String role, String itemType) {
        if (isAccessOnlyType(itemType)) {
            return -1;
        }
        if ("UG_Student".equalsIgnoreCase(role)) {
            return UG_STUDENT_MAX_BOOK_BORROWS;
        }
        if ("Faculty".equalsIgnoreCase(role)) {
            return FACULTY_MAX_BOOK_BORROWS;
        }
        if (isExternalVisitorRole(role)) {
            return -1;
        }
        return -1;
    }

    private int calculateLoanDays(String role, String itemType, String genre) {
        if (itemType == null) {
            return 0;
        }
        String normalized = normalizeItemType(itemType);
        if (isAccessOnlyType(normalized)) {
            return 0;
        }
        if (isNonCourseGenre(genre)) {
            return 28;
        }
        if (ITEM_TYPE_BOOK.equalsIgnoreCase(normalized) || ITEM_TYPE_EBOOK.equalsIgnoreCase(normalized)) {
            return 14;
        }
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

    private String roleLabel(String role) {
        if ("UG_Student".equalsIgnoreCase(role)) {
            return "Undergraduate Students";
        }
        if (isExternalVisitorRole(role)) {
            return "External Visitors";
        }
        if ("Faculty".equalsIgnoreCase(role)) {
            return "Faculty Members";
        }
        return "Users";
    }

    private boolean isExternalVisitorRole(String role) {
        if (role == null) {
            return false;
        }
        return "External Visitor".equalsIgnoreCase(role.trim());
    }

    private void showTopBorrowedBooks() {
        List<Book> topBooks = catalog.getTopBorrowedBooks(3);
        DefaultTableModel topModel = new DefaultTableModel(
                new Object[] { "ISBN", "Title", "Author", "Borrow Count" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Book book : topBooks) {
            topModel.addRow(new Object[] {
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getBorrowCount()
            });
        }

        JTable topTable = new JTable(topModel);
        topTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topTable.setRowHeight(20);
        topTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane pane = new JScrollPane(topTable);
        pane.setPreferredSize(new Dimension(600, 200));
        JOptionPane.showMessageDialog(this, pane, "Top Borrowed Books", JOptionPane.INFORMATION_MESSAGE);
    }

    private Map<String, String> loadUserTypesFromFile() {
        Map<String, String> userTypes = new HashMap<>();

        UsersFileData usersFileData = loadUsersFileData();
        if (usersFileData == null) {
            return userTypes;
        }

        int usernameIndex = indexOfIgnoreCase(usersFileData.headers, "username");
        int typeIndex = indexOfIgnoreCase(usersFileData.headers, "type");
        if (usernameIndex < 0 || typeIndex < 0) {
            return userTypes;
        }

        for (String[] parts : usersFileData.rows) {
            if (usernameIndex >= parts.length || typeIndex >= parts.length) {
                continue;
            }

            String username = parts[usernameIndex].trim();
            String type = parts[typeIndex].trim();
            if (!username.isEmpty() && !type.isEmpty()) {
                userTypes.put(username, type);
            }
        }

        return userTypes;
    }

    private String resolveUserTypeForHistory(String username, String fallbackType, Map<String, String> userTypes) {
        if (username == null) {
            return fallbackType;
        }

        String type = userTypes.get(username);
        if (type != null && !type.trim().isEmpty()) {
            return type;
        }

        if ("admin".equalsIgnoreCase(username)) {
            return "Admin";
        }

        return fallbackType;
    }

    private void showLoginDialog() {
        currentUser = null;

        JPanel loginPanel = new JPanel(new BorderLayout(0, 12));
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerPanel = createHeaderPanel(null);
        loginPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 6, 0);

        JLabel loginTitle = new JLabel("Log in to your account");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginTitle.setForeground(new Color(90, 90, 90));
        formPanel.add(loginTitle, gbc);

        gbc.gridy++;
        JLabel helperText = new JLabel(
                "<html>Use your username and password to log in.</html>");
        helperText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helperText.setForeground(new Color(100, 100, 100));
        formPanel.add(helperText, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 6, 12);
        JLabel usernameLabel = new JLabel("username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(12, 0, 6, 0);
        JTextField usernameField = new JTextField(24);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 6, 12);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(8, 0, 6, 0);
        JPasswordField passwordField = new JPasswordField(24);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.insets = new Insets(6, 0, 0, 0);
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(Color.WHITE);
        char defaultEchoChar = passwordField.getEchoChar();
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEchoChar);
        });
        formPanel.add(showPassword, gbc);

        loginPanel.add(formPanel, BorderLayout.CENTER);

        while (currentUser == null) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    loginPanel,
                    "Log in to your account",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (option != JOptionPane.OK_OPTION) {
                dispose();
                return;
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            currentUser = authenticate(username, password);
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid login.\nAdmin: admin / admin123\nOther users: username/password from txt files/users.txt");
            }
        }

        sessionLabel.setText("Logged in as: " + currentUser.getRole() + " (" + currentUser.getUsername() + ")");
        applyRolePermissions();
    }

    private JPanel createHeaderPanel(JLabel rightLabel) {
        Color headerBlue = new Color(23, 70, 120);
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setBackground(headerBlue);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(headerBlue);
        logoPanel.setPreferredSize(new Dimension(64, 64));
        JLabel logoLabel = createLogoLabel();
        logoPanel.add(logoLabel, BorderLayout.CENTER);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(headerBlue);
        JLabel orgTitle = new JLabel("Luleå University Library", SwingConstants.LEFT);
        orgTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        orgTitle.setForeground(Color.WHITE);
        titlePanel.add(orgTitle, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(12, 0));
        leftPanel.setBackground(headerBlue);
        leftPanel.add(logoPanel, BorderLayout.WEST);
        leftPanel.add(titlePanel, BorderLayout.CENTER);

        headerPanel.add(leftPanel, BorderLayout.WEST);

        return headerPanel;
    }

    private JLabel createLogoLabel() {
        URL logoUrl = getClass().getResource(LOGO_RESOURCE);
        if (logoUrl != null) {
            ImageIcon icon = new ImageIcon(logoUrl);
            Image scaled = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
        }

        JLabel fallback = new JLabel("IUB", SwingConstants.CENTER);
        fallback.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fallback.setForeground(Color.WHITE);
        fallback.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return fallback;
    }

    private List<BorrowRecord> getHistoryForCurrentUser() {
        List<BorrowRecord> allHistory = catalog.getBorrowHistory();

        if (currentUser == null) {
            return new ArrayList<>();
        }

        if (currentUser.canViewAllBorrowHistory()) {
            return allHistory;
        }

        List<BorrowRecord> myHistory = new ArrayList<>();
        for (BorrowRecord record : allHistory) {
            if (currentUser.getUsername().equals(record.getBorrowerName())) {
                myHistory.add(record);
            }
        }
        return myHistory;
    }

    private void applyRolePermissions() {
        boolean loggedIn = currentUser != null;
        boolean isAdmin = loggedIn && currentUser.canAddBook();
        boolean isBorrower = loggedIn && currentUser.canBorrowBook();

        addButton.setVisible(isAdmin);
        addUserButton.setVisible(isAdmin);
        viewUsersButton.setVisible(isAdmin);
        updateButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);
        showAllButton.setVisible(loggedIn);

        availableButton.setVisible(isBorrower);
        borrowButton.setVisible(isBorrower);
        returnButton.setVisible(isBorrower);

        searchButton.setVisible(loggedIn);
        historyButton.setVisible(loggedIn);
        topBorrowedButton.setVisible(loggedIn);
        logoutButton.setVisible(loggedIn);

        addButton.setEnabled(isAdmin);
        addUserButton.setEnabled(isAdmin);
        viewUsersButton.setEnabled(isAdmin);
        updateButton.setEnabled(loggedIn && currentUser.canUpdateBookInfo());
        deleteButton.setEnabled(loggedIn && currentUser.canDeleteBook());
        searchButton.setEnabled(loggedIn);
        showAllButton.setEnabled(loggedIn);
        availableButton.setEnabled(loggedIn);
        borrowButton.setEnabled(isBorrower);
        returnButton.setEnabled(isBorrower);
        historyButton.setEnabled(loggedIn);
        topBorrowedButton.setEnabled(loggedIn);
        logoutButton.setEnabled(loggedIn);

        revalidate();
        repaint();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to logout now?",
                "Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        currentUser = null;
        sessionLabel.setText("Not logged in");
        applyRolePermissions();

        showLoginDialog();
        refreshBookViewForCurrentRole();
    }

    private User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String normalizedUsername = username.trim();

        User admin = new AdminUser("admin", "admin123");
        if (admin.getUsername().equalsIgnoreCase(normalizedUsername) && admin.checkPassword(password)) {
            return admin;
        }

        User fileUser = authenticateFromUsersFile(normalizedUsername, password);
        if (fileUser != null) {
            return fileUser;
        }

        return null;
    }

    private User authenticateFromUsersFile(String username, String password) {
        UsersFileData usersFileData = loadUsersFileData();
        if (usersFileData == null) {
            return null;
        }

        int usernameIndex = indexOfIgnoreCase(usersFileData.headers, "username");
        int passwordIndex = indexOfIgnoreCase(usersFileData.headers, "password");
        int typeIndex = indexOfIgnoreCase(usersFileData.headers, "type");

        if (usernameIndex < 0 || passwordIndex < 0) {
            return null;
        }

        for (String[] parts : usersFileData.rows) {
            if (parts.length <= Math.max(usernameIndex, passwordIndex)) {
                continue;
            }

            String savedUsername = parts[usernameIndex].trim();
            String savedPassword = parts[passwordIndex].trim();
            if (!savedUsername.equals(username) || !savedPassword.equals(password)) {
                continue;
            }

            String userType = typeIndex >= 0 && typeIndex < parts.length ? parts[typeIndex].trim() : "UG_Student";
            if ("Admin".equalsIgnoreCase(userType)) {
                return new AdminUser(savedUsername, savedPassword);
            }
            if ("Faculty".equalsIgnoreCase(userType)) {
                return new FacultyUser(savedUsername, savedPassword);
            }
            if ("UG_Student".equalsIgnoreCase(userType)) {
                return new UG_Student(savedUsername, savedPassword);
            }
            if ("External Visitor".equalsIgnoreCase(userType)) {
                return new ExternalVisitor(savedUsername, savedPassword);
            }
            return null;
        }

        return null;
    }

    private UsersFileData loadUsersFileData() {
        File file = new File("txt files/users.txt");
        if (!file.exists() || file.length() == 0) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                return null;
            }

            String[] headers = parseCsvLine(headerLine);
            List<String[]> rows = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                rows.add(parseCsvLine(line));
            }

            return new UsersFileData(headers, rows);
        } catch (IOException e) {
            return null;
        }
    }

    private int indexOfIgnoreCase(String[] values, String target) {
        if (values == null || target == null) {
            return -1;
        }

        for (int i = 0; i < values.length; i++) {
            if (target.equalsIgnoreCase(values[i].trim())) {
                return i;
            }
        }
        return -1;
    }

    private String[] parseCsvLine(String line) {
        return textfile.parseCsvLine(line);
    }

    private String[] buildGenreUpdateOptions() {
        String[] options = new String[GENRE_OPTIONS.length + 1];
        options[0] = "Keep current";
        System.arraycopy(GENRE_OPTIONS, 0, options, 1, GENRE_OPTIONS.length);
        return options;
    }
}
