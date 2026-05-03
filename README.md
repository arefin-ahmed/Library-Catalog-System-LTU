# Smart Library Catalog System

A desktop Library Management System built with Java (Swing) using object-oriented design principles. The application supports role-based access, catalog management, borrowing and returning workflows, and file-based persistence for books, users, and borrow history.

## Project Highlights

- OOP-focused architecture with abstraction, inheritance, encapsulation, and polymorphism.
- Role-based operations for Admin, Student, and Faculty users.
- Book catalog management: add, update, delete, search, and availability listing.
- Borrow and return workflow with history tracking.
- Borrow limits and loan durations by user role.
- Persistent storage in text files (CSV format).

## Tech Stack

- Language: Java
- UI: Java Swing
- Build Tool: Maven
- Testing: JUnit 4
- Persistence: Local file storage (CSV-like text files)

## Current Rules

- UG_Student
  - Max active borrows: 3
  - Loan duration: 10 days
  - G_Student
  - Max active borrows: 5
  - Loan duration: 15 days
- Faculty
  - Max active borrows: 7
  - Loan duration: 20 days
- Admin
  - Can manage books and view full borrow history
  - Cannot borrow books

## Core Features

### 1. Authentication and User Roles

- Login-based access control.
- Permission checks are implemented through role methods in user model classes.

### 2. Catalog Management

- Add new books with metadata (ISBN, title, author, genre, publisher, copies).
- Update existing book information.
- Delete books by ISBN.
- List all books and available books.

### 3. Search

- Search books by title.
- Search books by author.
- Search books by genre.

### 4. Circulation

- Borrow books and e-books if copies are available and role rules allow it.
- Access-only resources (E-Journal, Database) are always available and do not require return.
- Return applies only to borrowable items.
- Automatic issue date and due date handling.

### 5. Borrow History

- Records BORROW, RETURN, and ACCESS events.
- Tracks user, role, issue date, due date, and action.
- Supports loading and saving history across sessions.

## OOP Design Overview

- Abstract layer:
  - `AbstractLibraryCatalog` defines catalog behaviors and contract methods.
- Concrete implementation:
  - `LibraryCatalogImpl` provides search, borrow/return logic, and history handling.
- Role hierarchy:
  - `User` (abstract) -> `AdminUser`, `StudentUser`, `FacultyUser`.
- Encapsulated domain models:
  - `Book`, `BorrowRecord`.
- Persistence abstraction:
  - `CatalogPersistence` interface with `FileCatalogPersistenceCSV` implementation.

## Project Structure

```text
src/main/java/library/
  core/
    AbstractLibraryCatalog.java
    LibrarySystemGUI.java
  impl/
    LibraryCatalogImpl.java
    FileCatalogPersistenceCSV.java
    BorrowHistoryPersistenceTXT.java
  models/
    User.java
    AdminUser.java
    UG_Student.java
    G_Student.java
    FacultyUser.java
    Book.java
    BorrowRecord.java
  persistence/
    CatalogPersistence.java
  util/
    textfiles.java
  Main.java

src/test/java/library/models/
  AppTest.java
```

## Data Files

- `catalog.txt`: stores book catalog data
- `users.txt`: stores user account data
- `borrow_history.txt`: stores borrow/return history

## Build and Run

### Prerequisites

- JDK compatible with the Maven compiler config in `pom.xml` (currently source/target = 25)
- Maven 3.8+

### Build

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Run the Application

Option 1 (recommended):

- Run `library.core.Main` from your IDE.

Option 2 (command line after compile):

```bash
java -cp target/classes library.core.Main
```

## Notes

- File persistence is human-readable and easy to inspect manually.
- CSV parsing is handled with a dedicated utility to support quoted values.
- The system is designed for educational OOP lab use and can be extended with additional modules.

## Suggested Future Improvements

- Password hashing for stronger security.
- Fine-grained validations for user and book inputs.
- More unit and integration test coverage.
- Export reports (CSV/PDF) for admin analytics.
- Database-backed persistence (optional advanced upgrade).

---

Developed as an OOP open-ended lab project for a library management use case.
