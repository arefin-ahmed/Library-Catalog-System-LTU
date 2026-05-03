package library.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import library.models.BorrowRecord;
import library.util.textfile;

/**
 * Saves and loads borrow history in borrow_history.txt.
 */
public class BorrowHistoryPersistenceTXT {
    private final String filePath;

    public BorrowHistoryPersistenceTXT() {
        this("txt files/borrow_history.txt");
    }

    public BorrowHistoryPersistenceTXT(String filePath) {
        this.filePath = filePath;
    }

    public void saveHistory(List<BorrowRecord> history) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath)))) {
            writer.write("isbn,bookTitle,borrowerName,userRole,itemType,issueDate,dueDate,action");
            writer.newLine();
            for (BorrowRecord record : history) {
                String itemType = record.getItemType() == null || record.getItemType().trim().isEmpty()
                        ? "Book"
                        : record.getItemType().trim();
                writer.write(String.join(",",
                        textfile.escape(record.getIsbn()),
                        textfile.escape(record.getBookTitle()),
                        textfile.escape(record.getBorrowerName()),
                        textfile.escape(record.getUserRole()),
                        textfile.escape(itemType),
                        textfile.escape(record.getIssueDate()),
                        textfile.escape(record.getDueDate()),
                        textfile.escape(record.getAction())));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save borrow history: " + e.getMessage());
        }
    }

    public List<BorrowRecord> loadHistory() {
        List<BorrowRecord> history = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null) {
                return history;
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = parseCsvLine(line);
                if (parts.length < 6) {
                    continue;
                }

                String itemType = "Book";
                String issueDate;
                String dueDate;
                String action;

                if (parts.length >= 8) {
                    itemType = parts[4];
                    issueDate = parts[5];
                    dueDate = parts[6];
                    action = parts[7];
                } else {
                    issueDate = parts[4];
                    dueDate = parts.length >= 7 ? parts[5] : "";
                    action = parts.length >= 7 ? parts[6] : parts[5];
                }

                history.add(new BorrowRecord(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        itemType,
                        issueDate,
                        dueDate,
                        action));
            }
        } catch (IOException e) {
            System.out.println("Could not load borrow history: " + e.getMessage());
        }

        return history;
    }

    private String[] parseCsvLine(String line) {
        return textfile.parseCsvLine(line);
    }
}