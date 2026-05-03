package library.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import library.models.Book;
import library.persistence.CatalogPersistence;
import library.util.textfile;


public class FileCatalogPersistence implements CatalogPersistence {
    private final String filePath;

    public FileCatalogPersistence() {
        this("txt files/catalog.txt");                 // If no path - use default file
    }

    public FileCatalogPersistence(String filePath) {             // change file location later
        this.filePath = filePath;
    }

    @Override
    public void saveCatalog(Map<String, List<Book>> catalog) throws Exception {      // WRITE THE FILE
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(                                    // write column names
                    "isbn,title,author,genre,publisher,itemType,totalCopies,availableCopies,borrowCount,lastIssueDate,courseType"); 
            writer.newLine();

            for (List<Book> bucket : catalog.values()) {
                for (Book book : bucket) {
                    writer.write(toCsvLine(book));    // use this to convert to CSV formet
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Could not save catalog: " + e.getMessage());
        }
    }

    @Override 
    public Map<String, List<Book>> loadCatalog() throws Exception {     // Returns full catalog
        Map<String, List<Book>> loaded = new HashMap<>();              // Create empty map
        File file = new File(filePath);

        if (!file.exists()) {
            return loaded;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();               // Skip header
            if (line == null) {
                return loaded;
            }

            while ((line = reader.readLine()) != null) {          // Read line-by-line
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseCsvLine(line);
                if (parts.length < 8) {
                    continue;
                }

                Book book;
                if (parts.length >= 11) {
                    book = new Book(                // Creating Book Object (Converts text → object)
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4],
                            parts[5],
                            parseIntSafe(parts[6]),        // Prevents crash if invalid
                            parseIntSafe(parts[7]),
                            parseIntSafe(parts[8]),
                            parts.length > 9 ? parts[9] : "");         //if issue date is missing → set to empty string
                    // set courseType if present
                    if (parts.length > 10) {
                        book.setCourseType(parts[10]);
                    }
                } 
                
                else {
                    book = new Book(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4],
                            "Book",
                            parseIntSafe(parts[5]),
                            parseIntSafe(parts[6]),
                            parseIntSafe(parts[7]),
                            parts.length > 8 ? parts[8] : "");
                    // older rows have no courseType → default empty
                }

                List<Book> bucket = loaded.get(book.getIsbn());        // Insert into HashMap
                if (bucket == null) {
                    bucket = new ArrayList<>();
                    loaded.put(book.getIsbn(), bucket);
                }
                bucket.add(book);
            }
        } catch (IOException e) {
            System.out.println("Could not load catalog: " + e.getMessage());
        }

        return loaded;
    }

    private String toCsvLine(Book book) {                // Converts object → (CSV line format) text file
        String itemType = book.getItemType() == null || book.getItemType().trim().isEmpty()
                ? "Book"
                : book.getItemType().trim();
        return String.join(",",     
                textfile.escape(book.getIsbn()),                //  escape helps to Prevents CSV breaking issue
                textfile.escape(book.getTitle()),
                textfile.escape(book.getAuthor()),
                textfile.escape(book.getGenre()),
                textfile.escape(book.getPublisher()),
                textfile.escape(itemType),
                String.valueOf(book.getTotalCopies()),
                String.valueOf(book.getAvailableCopies()),
                String.valueOf(book.getBorrowCount()),
            textfile.escape(book.getLastIssueDate()),
            textfile.escape(book.getCourseType() == null ? "" : book.getCourseType()));
    }

    private String[] parseCsvLine(String line) {
        return textfile.parseCsvLine(line);
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}