# GUI README

This document explains how the Swing-based GUI works in the library system, what each screen does, and which actions are available by role.

## Overview

The GUI is implemented in `LibrarySystemGUI`. It builds a single main window (JFrame) with:

- A header area (logo + title)
- A toolbar row of action buttons
- A table that lists books
- Dialogs for login, add/update/search, and history views

## How the GUI Starts

1. The constructor initializes the catalog using text-file persistence.
2. The UI is built (table + buttons + header).
3. The login dialog appears.
4. After login, role permissions are applied and the book list is shown.

## Login Flow

- Login uses a modal dialog with username and password fields.
- Admin credentials are fixed: `admin / admin123`.
- All other users are read from `txt files/users.txt`.
- If login fails, the dialog stays open until valid credentials are entered or the dialog is canceled.

## Main Table (Center Area)

The main table always shows books with these columns:

- ISBN
- Title
- Author
- Genre
- Item Type (Book or Book-CD)
- Available Copies
- Borrow Count

The table is read-only. Selecting a row makes borrow/return actions use that ISBN by default.

## Role-Based Buttons

Buttons are shown or hidden based on the logged-in role.

### Admin (can manage catalog and users)

Visible buttons:

- Add Book
- Add User
- View Users
- Update Book
- Delete Book
- Search
- Show All
- Borrow History
- Top Borrowed
- Logout

Admin cannot borrow or return books.

### UG/G Student and Faculty (borrowers)

Visible buttons:

- Search
- Show All
- Available Books
- Borrow Book
- Return Book
- Borrow History (only their own)
- Top Borrowed
- Logout

## Button Details

### Add Book (Admin)

- Opens a form dialog to enter ISBN, Title, Author, Genre, Item Type, Publisher, Total Copies.
- Validates required fields and numeric copies.
- Adds the book to the catalog and saves to `catalog.txt`.

### Add User (Admin)

- Opens a form dialog to enter User ID, Name, Username, Department, Type, Contact No.
- Auto-generates the password as `username + "123"`.
- Saves the user to `users.txt` and shows the generated password.

### View Users (Admin)

- Reads `users.txt` and shows all users in a table dialog.

### Update Book (Admin)

- Opens a form dialog: ISBN (required), Title/Author/Genre (optional).
- Only non-empty fields are applied.
- Saves back to the catalog file.

### Delete Book (Admin)

- Prompts for ISBN, then asks for confirmation.
- Removes the book and saves the catalog.

### Search (All Roles)

- Prompts for search type: ISBN, Title, Author, Genre.
- Filters the table with matching results.

### Show All (All Roles)

- Reloads and displays all books.

### Available Books (Borrowers)

- Filters the table to only items with available copies and access-only resources (E-Journal, Database).

### Borrow Book (Borrowers)

- Uses selected table row ISBN, or asks for ISBN.
- Checks role-based limits and item type.
- Borrowable items (Book, Book-CD, E-Book) update availability and write history.
- Access-only items (E-Journal, Database) record an ACCESS entry without changing copies.
- Shows a due-date message for borrowable items.

### Return Book (Borrowers)

- Uses selected table row ISBN, or asks for ISBN.
- Updates availability and history if the return is valid.
- Access-only items do not require return.

### Borrow History (All Roles)

- Admin sees all records.
- Borrowers see only their own history.
- Displays action, ISBN, title, user, type, item type, issue date, due date.
- Includes ACCESS actions for E-Journal and Database.

### Top Borrowed (All Roles)

- Shows the top 3 most-borrowed books in a dialog.

### Logout (All Roles)

- Confirms logout, then reopens the login dialog.

## Borrow Limits and Loan Duration

The GUI enforces limits by user role and item type:

- UG_Student
  - Max Book borrows: 3
  - Max Book-CD borrows: 3
  - Loan duration: 10 days

- G_Student
  - Max Book borrows: 5
  - Max Book-CD borrows: 5
  - Loan duration: 15 days

- Faculty
  - Max Book borrows: 10
  - Max Book-CD borrows: 5
  - Loan duration: 30 days

## Files Used by the GUI

- `txt files/catalog.txt` for the book catalog
- `txt files/users.txt` for user records
- `txt files/borrow_history.txt` for borrow/return history

## Common Troubleshooting

- If login fails, confirm the user entry exists in `users.txt` and the password matches.
- If the table looks empty, use Show All or check that `catalog.txt` has data.
- If a user cannot borrow, verify role limits or available copies.

## Line-by-Line Explanation (LibrarySystemGUI.java)

Notes:

- This list skips blank lines and brace-only lines.
- Each item references the exact line in the source file.

### Lines 1-300

- Line 1: Declares the package `library.core`.
- Line 3: Imports `BorderLayout` for top/center layouts.
- Line 4: Imports `Color` for UI colors.
- Line 5: Imports `Dimension` for sizing components.
- Line 6: Imports `Font` for font styling.
- Line 7: Imports `GridBagConstraints` for flexible form layout.
- Line 8: Imports `GridBagLayout` manager.
- Line 9: Imports `GridLayout` manager.
- Line 10: Imports `Image` for logo scaling.
- Line 11: Imports `Insets` for padding in grid bag layout.
- Line 12: Imports `BufferedReader` for file reading.
- Line 13: Imports `BufferedWriter` for file writing.
- Line 14: Imports `File` for file paths.
- Line 15: Imports `FileReader` for reading files.
- Line 16: Imports `FileWriter` for writing files.
- Line 17: Imports `IOException` for I/O error handling.
- Line 18: Imports `URL` for resource loading.
- Line 19: Imports `LocalDate` for due dates.
- Line 20: Imports `ArrayList` for list storage.
- Line 21: Imports `HashMap` for maps.
- Line 22: Imports `List` interface.
- Line 23: Imports `Map` interface.
- Line 25: Imports `BorderFactory` for border helpers.
- Line 26: Imports `JButton`.
- Line 27: Imports `JCheckBox`.
- Line 28: Imports `JComboBox`.
- Line 29: Imports `JFrame`.
- Line 30: Imports `ImageIcon`.
- Line 31: Imports `JLabel`.
- Line 32: Imports `JOptionPane`.
- Line 33: Imports `JPasswordField`.
- Line 34: Imports `JPanel`.
- Line 35: Imports `JScrollPane`.
- Line 36: Imports `JTable`.
- Line 37: Imports `JTextField`.
- Line 38: Imports `SwingConstants` for alignment.
- Line 39: Imports `DefaultTableModel` for table data.
- Line 41: Imports file-based catalog persistence implementation.
- Line 42: Imports concrete catalog implementation.
- Line 43: Imports `AdminUser` model.
- Line 44: Imports `Book` model.
- Line 45: Imports `BorrowRecord` model.
- Line 46: Imports `FacultyUser` model.
- Line 47: Imports `G_Student` model.
- Line 48: Imports `UG_Student` model.
- Line 49: Imports base `User` model.
- Line 50: Imports CSV helper `textfile`.
- Line 52: Starts class JavaDoc.
- Line 53: Describes this class as a beginner-friendly Swing UI.
- Line 55: Declares `LibrarySystemGUI` extending `JFrame`.
- Line 56: Logo resource path.
- Line 57: Constant for `Book` item type.
- Line 58: Constant for `Book-CD` item type.
- Line 60: UG max book borrows.
- Line 61: UG max book-CD borrows.
- Line 62: UG loan days.
- Line 64: G max book borrows.
- Line 65: G max book-CD borrows.
- Line 66: G loan days.
- Line 68: Faculty max book borrows.
- Line 69: Faculty max book-CD borrows.
- Line 70: Faculty loan days.
- Line 72: Begins genre options array.
- Line 73: Genre entry.
- Line 74: Genre entry.
- Line 75: Genre entry.
- Line 76: Genre entry.
- Line 77: Genre entry.
- Line 78: Genre entry.
- Line 79: Genre entry.
- Line 80: Genre entry.
- Line 81: Genre entry.
- Line 82: Genre entry.
- Line 83: Genre entry.
- Line 84: Genre entry.
- Line 85: Genre entry.
- Line 86: Genre entry.
- Line 87: Genre entry.
- Line 88: Genre entry.
- Line 91: Starts helper class `UsersFileData`.
- Line 92: Header fields for users file.
- Line 93: Row fields for users file.
- Line 95: Constructor for `UsersFileData`.
- Line 96: Assigns headers.
- Line 97: Assigns rows.
- Line 101: Catalog reference (abstraction).
- Line 102: Current user session.
- Line 103: Label for login session text.
- Line 105: Add book button.
- Line 106: Add user button.
- Line 107: View users button.
- Line 108: Update book button.
- Line 109: Delete book button.
- Line 110: Search button.
- Line 111: Show all books button.
- Line 112: Available books button.
- Line 113: Borrow button.
- Line 114: Return button.
- Line 115: Borrow history button.
- Line 116: Top borrowed button.
- Line 117: Logout button.
- Line 119: Table model field.
- Line 120: Table field.
- Line 122: GUI constructor start.
- Line 123: Creates catalog with file persistence.
- Line 125: Sets window title.
- Line 126: Sets window size.
- Line 127: Centers window on screen.
- Line 128: Exits app on close.
- Line 130: Builds UI components.
- Line 131: Shows login dialog.
- Line 132: Loads initial book view.
- Line 135: Begins UI construction method.
- Line 136: Sets content pane background.
- Line 137: Uses border layout for frame.
- Line 139: Creates session label.
- Line 140: Sets session label font.
- Line 141: Builds header panel.
- Line 142: Adds padding around header.
- Line 144: Creates center panel.
- Line 145: Sets center background.
- Line 146: Adds padding around center.
- Line 148: Creates table model with columns.
- Line 149: Column names and empty initial data.
- Line 151: Overrides cell editability.
- Line 152: Always non-editable.
- Line 157: Creates table from model.
- Line 158: Sets table font.
- Line 159: Sets row height.
- Line 160: Sets header font.
- Line 162: Wraps table in scroll pane.
- Line 163: Sets scroll pane size.
- Line 164: Places table in center.
- Line 166: Adds center panel to frame.
- Line 168: Creates button panel.
- Line 169: Button panel background.
- Line 170: Button panel padding.
- Line 172: Instantiates Add Book button.
- Line 173: Instantiates Add User button.
- Line 174: Instantiates View Users button.
- Line 175: Instantiates Update Book button.
- Line 176: Instantiates Delete Book button.
- Line 177: Instantiates Search button.
- Line 178: Instantiates Show All button.
- Line 179: Instantiates Available Books button.
- Line 180: Instantiates Borrow Book button.
- Line 181: Instantiates Return Book button.
- Line 182: Instantiates Borrow History button.
- Line 183: Instantiates Top Borrowed button.
- Line 184: Instantiates Logout button.
- Line 186: Wires Add Book action.
- Line 187: Wires Add User action.
- Line 188: Wires View Users action.
- Line 189: Wires Update Book action.
- Line 190: Wires Delete Book action.
- Line 191: Wires Search action.
- Line 192: Wires Show All action.
- Line 193: Wires Available Books action.
- Line 194: Wires Borrow action.
- Line 195: Wires Return action.
- Line 196: Wires History action.
- Line 197: Wires Top Borrowed action.
- Line 198: Wires Logout action.
- Line 200: Adds Add Book button to panel.
- Line 201: Adds Add User button.
- Line 202: Adds View Users button.
- Line 203: Adds Update Book button.
- Line 204: Adds Delete Book button.
- Line 205: Adds Search button.
- Line 206: Adds Show All button.
- Line 207: Adds Available Books button.
- Line 208: Adds Borrow button.
- Line 209: Adds Return button.
- Line 210: Adds History button.
- Line 211: Adds Top Borrowed button.
- Line 212: Adds Logout button.
- Line 214: Applies role-based button visibility.
- Line 216: Creates container for header and buttons.
- Line 217: Sets container background.
- Line 218: Places header in top container.
- Line 219: Places buttons under header.
- Line 220: Adds top container to frame.
- Line 223: Starts add-book flow.
- Line 224: Guards add-book for admin only.
- Line 225: Shows unauthorized message.
- Line 229: Creates ISBN field.
- Line 230: Creates title field.
- Line 231: Creates author field.
- Line 232: Creates genre dropdown.
- Line 233: Creates item type dropdown.
- Line 234: Creates publisher field.
- Line 235: Creates total copies field.
- Line 237: Lays out add-book form.
- Line 238: Adds ISBN label.
- Line 239: Adds ISBN input.
- Line 240: Adds title label.
- Line 241: Adds title input.
- Line 242: Adds author label.
- Line 243: Adds author input.
- Line 244: Adds genre label.
- Line 245: Adds genre input.
- Line 246: Adds item type label.
- Line 247: Adds item type input.
- Line 248: Adds publisher label.
- Line 249: Adds publisher input.
- Line 250: Adds copies label.
- Line 251: Adds copies input.
- Line 253: Opens confirm dialog.
- Line 254: Dialog parent set to this frame.
- Line 255: Uses the add-book form panel.
- Line 256: Dialog title.
- Line 257: Uses OK/Cancel options.
- Line 258: Plain message style.
- Line 260: Exit if canceled.
- Line 264: Reads ISBN.
- Line 265: Reads title.
- Line 266: Reads author.
- Line 267: Reads genre.
- Line 268: Treats "Keep current" as empty.
- Line 271: Normalizes item type.
- Line 272: Reads publisher.
- Line 273: Reads total copies.
- Line 275: Validates required fields.
- Line 276: Required field check continuation.
- Line 277: Shows validation error.
- Line 282: Declares `totalCopies` integer.
- Line 283: Attempts parse of copies.
- Line 284: Parses integer.
- Line 285: Catches parse error.
- Line 286: Shows number error.
- Line 290: Validates positive copies.
- Line 291: Shows positive-number error.
- Line 295: Creates new `Book`.
- Line 296: Adds book to catalog.
- Line 297: Persists catalog/history.
- Line 298: Refreshes table.
- Line 300: Shows success message.

### Lines 301-600

- Line 303: Starts add-user flow.
- Line 304: Admin-only guard for adding users.
- Line 305: Shows unauthorized message.
- Line 309: Creates user ID field.
- Line 310: Creates name field.
- Line 311: Creates username field.
- Line 312: Creates department field.
- Line 313: Creates user-type dropdown.
- Line 314: Creates contact number field.
- Line 316: Lays out add-user form.
- Line 317: Adds user ID label.
- Line 318: Adds user ID input.
- Line 319: Adds name label.
- Line 320: Adds name input.
- Line 321: Adds username label.
- Line 322: Adds username input.
- Line 323: Adds department label.
- Line 324: Adds department input.
- Line 325: Adds type label.
- Line 326: Adds type dropdown.
- Line 327: Adds contact label.
- Line 328: Adds contact input.
- Line 330: Opens add-user dialog.
- Line 331: Dialog parent set to this frame.
- Line 332: Uses add-user panel.
- Line 333: Dialog title.
- Line 334: OK/Cancel options.
- Line 335: Plain dialog style.
- Line 337: Exit if canceled.
- Line 341: Reads user ID.
- Line 342: Reads name.
- Line 343: Reads username.
- Line 344: Reads department.
- Line 345: Reads user type.
- Line 346: Reads contact number.
- Line 347: Auto-generates password.
- Line 349: Validates all user fields.
- Line 350: Field validation continuation.
- Line 351: Shows validation error.
- Line 355: Checks for duplicate username.
- Line 356: Shows duplicate warning.
- Line 360: Saves user record to file.
- Line 361: Shows success with password.
- Line 363: Shows failure message.
- Line 368: Starts username existence check.
- Line 369: Null guard for username.
- Line 373: Loads users file data.
- Line 374: Returns false if no data.
- Line 378: Finds username column index.
- Line 379: If missing, return false.
- Line 383: Iterates user rows.
- Line 384: Compares username case-insensitively.
- Line 385: Returns true if found.
- Line 389: Returns false if not found.
- Line 392: Starts save-user method.
- Line 394: Opens users file.
- Line 395: Checks if header is needed.
- Line 397: Opens writer in append mode.
- Line 398: If needed, writes header.
- Line 399: Header column names.
- Line 400: Moves to next line.
- Line 403: Writes CSV row with escaped fields.
- Line 404: Escapes user ID.
- Line 405: Escapes name.
- Line 406: Escapes username.
- Line 407: Escapes department.
- Line 408: Escapes type.
- Line 409: Escapes contact number.
- Line 410: Escapes password.
- Line 411: New line after record.
- Line 412: Returns success.
- Line 413: Catches I/O error.
- Line 414: Returns failure.
- Line 418: Starts show-users dialog.
- Line 419: Admin-only guard.
- Line 420: Shows unauthorized message.
- Line 424: Loads users file data.
- Line 425: If empty, show "no users".
- Line 426: "No users found" dialog.
- Line 430: Builds read-only users table model.
- Line 431: Overrides editability.
- Line 432: Non-editable rows.
- Line 437: Iterates each user row.
- Line 438: Creates display row.
- Line 439: Copies columns safely.
- Line 440: Uses empty string if missing.
- Line 442: Adds row to model.
- Line 445: Creates users table.
- Line 446: Sets table font.
- Line 447: Sets row height.
- Line 448: Sets header font.
- Line 450: Wraps table in scroll pane.
- Line 451: Sets pane size.
- Line 452: Shows users dialog.
- Line 455: Starts update-book flow.
- Line 456: Admin-only guard.
- Line 457: Unauthorized message.
- Line 461: Creates ISBN input.
- Line 462: Creates title input.
- Line 463: Creates author input.
- Line 464: Creates genre dropdown with "Keep current".
- Line 466: Lays out update form.
- Line 467: Adds ISBN label.
- Line 468: Adds ISBN field.
- Line 469: Adds title label.
- Line 470: Adds title field.
- Line 471: Adds author label.
- Line 472: Adds author field.
- Line 473: Adds genre label.
- Line 474: Adds genre field.
- Line 476: Opens update dialog.
- Line 477: Dialog parent.
- Line 478: Uses update panel.
- Line 479: Dialog title.
- Line 480: OK/Cancel buttons.
- Line 481: Plain dialog style.
- Line 483: Exit if canceled.
- Line 487: Reads ISBN.
- Line 488: Reads title.
- Line 489: Reads author.
- Line 490: Reads genre.
- Line 492: Validates ISBN required.
- Line 493: Shows missing ISBN message.
- Line 497: Updates book info in catalog.
- Line 498: If success, persist changes.
- Line 501: Refreshes book list.
- Line 502: Shows success/failure message.
- Line 506: Starts delete-book flow.
- Line 507: Admin-only guard.
- Line 508: Unauthorized message.
- Line 512: Prompts for ISBN.
- Line 513: Exit if canceled.
- Line 516: Trims ISBN input.
- Line 517: Checks empty ISBN.
- Line 518: Shows missing ISBN message.
- Line 522: Confirms delete action.
- Line 523: Shows ISBN in prompt.
- Line 524: Dialog title.
- Line 525: Yes/No options.
- Line 527: Exit if not confirmed.
- Line 531: Attempts delete in catalog.
- Line 532: If success, persist changes.
- Line 535: Refreshes book list.
- Line 536: Shows result message.
- Line 539: Starts search flow.
- Line 540: Defines search types.
- Line 541: Opens search type chooser.
- Line 542: Dialog parent.
- Line 543: Prompt text.
- Line 544: Dialog title.
- Line 545: Plain dialog style.
- Line 546: No icon.
- Line 547: Options list.
- Line 548: Default option.
- Line 550: Exit if canceled.
- Line 554: Prompts for search query.
- Line 555: Exit if canceled.
- Line 559: Trims query.
- Line 560: Checks empty query.
- Line 561: Shows error for empty search.
- Line 565: Creates results list.
- Line 567: If searching by ISBN.
- Line 568: Gets book by ISBN.
- Line 569: Adds if found.
- Line 572: If searching by title.
- Line 573: Searches by title.
- Line 574: If searching by author.
- Line 575: Searches by author.
- Line 576: If searching by genre.
- Line 577: Searches by genre.
- Line 579: Invalid type fallback.
- Line 580: Shows error message.
- Line 583: Populates table with results.
- Line 586: Starts show-all method.
- Line 587: Creates list to hold all books.
- Line 588: Iterates map buckets from catalog.
- Line 589: Adds all books from each bucket.
- Line 591: Shows all books in table.
- Line 594: Starts refresh method.
- Line 595: Currently just shows all.
- Line 598: Starts available-books method.
- Line 599: Populates table with available books.

### Lines 601-1212

- Line 602: Starts borrow-book flow.
- Line 603: Borrower-only guard.
- Line 604: Unauthorized message.
- Line 608: Gets ISBN from selection or prompt.
- Line 609: Exit if no ISBN.
- Line 613: Loads book by ISBN.
- Line 614: If not found, show error.
- Line 615: Borrow failed message.
- Line 619: Normalizes item type.
- Line 620: Counts active borrows by user and item type.
- Line 621: Gets borrow limit for role/type.
- Line 622: If limit exceeded, show error.
- Line 623: Builds limit message.
- Line 624: Message continues with role label and item type.
- Line 625: Completes message string.
- Line 629: Attempts borrow in catalog.
- Line 630: If success, persist and refresh.
- Line 631: Persists catalog/history.
- Line 632: Shows available books after borrow.
- Line 633: Gets loan duration for role.
- Line 634: If duration > 0, compute due date.
- Line 635: Builds due date string.
- Line 636: Shows success with due date.
- Line 637: Message includes days.
- Line 639: Success message without due date.
- Line 642: Borrow failed message.
- Line 643: Explains possible failure reasons.
- Line 647: Starts return-book flow.
- Line 648: Borrower-only guard.
- Line 649: Unauthorized message.
- Line 653: Gets ISBN from selection or prompt.
- Line 654: Exit if no ISBN.
- Line 658: Attempts return in catalog.
- Line 659: If success, persist and refresh.
- Line 660: Persists catalog/history.
- Line 661: Shows available books after return.
- Line 662: Shows return success.
- Line 664: Return failed message.
- Line 668: Starts ISBN helper.
- Line 669: Reads selected row index.
- Line 670: If a row is selected, use it.
- Line 671: Reads ISBN cell.
- Line 672: If ISBN exists, use it.
- Line 673: Trims ISBN.
- Line 674: If non-empty, return it.
- Line 680: Prompts for ISBN if none selected.
- Line 681: Exit if canceled.
- Line 685: Trims ISBN.
- Line 686: Checks empty ISBN.
- Line 687: Shows empty ISBN error.
- Line 691: Returns ISBN.
- Line 694: Starts persist method.
- Line 695: Saves catalog to file.
- Line 696: Saves borrow history to file.
- Line 699: Starts table population.
- Line 700: Clears existing rows.
- Line 701: Iterates books list.
- Line 702: Adds row with book fields.
- Line 703: ISBN column.
- Line 704: Title column.
- Line 705: Author column.
- Line 706: Genre column.
- Line 707: Normalized item type column.
- Line 708: Available copies column.
- Line 709: Borrow count column.
- Line 714: Starts borrow-history dialog.
- Line 715: Gets history filtered by role.
- Line 716: Loads user types from file.
- Line 718: Creates history table model.
- Line 719: Defines history columns.
- Line 721: Overrides editability.
- Line 722: Non-editable rows.
- Line 727: Iterates history records.
- Line 728: Adds history row.
- Line 729: Action column.
- Line 730: ISBN column.
- Line 731: Title column.
- Line 732: Borrower name.
- Line 733: Resolves user type.
- Line 734: Item type column.
- Line 735: Issue date.
- Line 736: Due date.
- Line 741: Creates history table.
- Line 742: Sets table font.
- Line 743: Sets row height.
- Line 744: Sets header font.
- Line 746: Wraps history table in scroll pane.
- Line 747: Sets pane size.
- Line 748: Chooses dialog title by role.
- Line 749: Admin title.
- Line 750: User title.
- Line 751: Shows history dialog.
- Line 754: Starts item-type normalization.
- Line 755: If type matches Book-CD.
- Line 756: Returns Book-CD.
- Line 758: Otherwise returns Book.
- Line 761: Starts borrow-limit helper.
- Line 762: Detects Book-CD.
- Line 763: If UG, return limit by type.
- Line 764: UG limit value.
- Line 766: If G, return limit by type.
- Line 767: G limit value.
- Line 769: If Faculty, return limit by type.
- Line 770: Faculty limit value.
- Line 772: Default "no limit" sentinel.
- Line 775: Starts loan-days helper.
- Line 776: UG loan days.
- Line 779: G loan days.
- Line 782: Faculty loan days.
- Line 785: Default 0 days.
- Line 788: Starts role-label helper.
- Line 789: UG label.
- Line 792: G label.
- Line 795: Faculty label.
- Line 798: Default label.
- Line 801: Starts top-borrowed dialog.
- Line 802: Gets top 3 borrowed books.
- Line 803: Creates top-borrowed table model.
- Line 804: Defines top-borrowed columns.
- Line 806: Overrides editability.
- Line 807: Non-editable rows.
- Line 812: Iterates top books.
- Line 813: Adds row with book fields.
- Line 814: ISBN column.
- Line 815: Title column.
- Line 816: Author column.
- Line 817: Borrow count column.
- Line 821: Creates top-borrowed table.
- Line 822: Sets table font.
- Line 823: Sets row height.
- Line 824: Sets header font.
- Line 826: Wraps table in scroll pane.
- Line 827: Sets pane size.
- Line 828: Shows top-borrowed dialog.
- Line 831: Starts user-type loading helper.
- Line 832: Creates map for username to type.
- Line 834: Loads users file data.
- Line 835: Returns empty map if no data.
- Line 839: Finds username column index.
- Line 840: Finds type column index.
- Line 841: If missing, return empty map.
- Line 845: Iterates user rows.
- Line 846: Skips rows missing columns.
- Line 850: Reads username.
- Line 851: Reads type.
- Line 852: Stores mapping if both present.
- Line 857: Returns user-type map.
- Line 860: Starts type resolver for history.
- Line 861: If username missing, return fallback.
- Line 865: Looks up type in map.
- Line 866: If found and not empty, return it.
- Line 870: Special-case admin username.
- Line 871: Returns `Admin` label.
- Line 874: Returns fallback type.
- Line 877: Starts login dialog method.
- Line 878: Clears current user.
- Line 880: Creates login panel.
- Line 881: Sets login panel background.
- Line 882: Sets login panel padding.
- Line 884: Builds header panel for dialog.
- Line 885: Adds header to dialog.
- Line 887: Creates form panel with GridBagLayout.
- Line 888: Sets form panel background.
- Line 890: Creates layout constraints.
- Line 891: Sets grid x.
- Line 892: Sets grid y.
- Line 893: Sets grid width.
- Line 894: Aligns components left.
- Line 895: Adds padding.
- Line 897: Creates login title label.
- Line 898: Sets title font.
- Line 899: Sets title color.
- Line 900: Adds title to form.
- Line 902: Advances to next row.
- Line 903: Creates helper text.
- Line 904: HTML wrapper for wrapping text.
- Line 905: Sets helper font.
- Line 906: Sets helper color.
- Line 907: Adds helper text.
- Line 909: Sets grid width back to 1.
- Line 910: Moves to username row.
- Line 911: Sets label insets.
- Line 912: Creates username label.
- Line 913: Sets username label font.
- Line 914: Adds username label.
- Line 916: Moves to input column.
- Line 917: Sets input insets.
- Line 918: Creates username field.
- Line 919: Adds username field.
- Line 921: Moves to password label column.
- Line 922: Next row for password.
- Line 923: Sets label insets.
- Line 924: Creates password label.
- Line 925: Sets password label font.
- Line 926: Adds password label.
- Line 928: Moves to input column.
- Line 929: Sets input insets.
- Line 930: Creates password field.
- Line 931: Adds password field.
- Line 933: Moves to checkbox row.
- Line 934: Next row for checkbox.
- Line 935: Sets checkbox insets.
- Line 936: Creates show-password checkbox.
- Line 937: Matches checkbox background.
- Line 938: Stores default echo char.
- Line 939: Adds toggle behavior.
- Line 940: Toggles echo char for show/hide.
- Line 942: Adds checkbox to form.
- Line 944: Adds form to login panel.
- Line 946: Loops until valid login.
- Line 947: Shows login confirm dialog.
- Line 948: Dialog parent.
- Line 949: Uses login panel.
- Line 950: Dialog title.
- Line 951: OK/Cancel.
- Line 952: Plain dialog style.
- Line 954: If canceled, exit app.
- Line 955: Disposes frame.
- Line 959: Reads username input.
- Line 960: Reads password input.
- Line 962: Authenticates credentials.
- Line 963: If invalid, show message.
- Line 964: Message includes admin and file login hint.
- Line 969: Updates session label with role and username.
- Line 970: Applies role permissions after login.
- Line 973: Starts header panel builder.
- Line 974: Defines header color.
- Line 975: Creates header panel.
- Line 976: Sets header background.
- Line 977: Sets header padding.
- Line 979: Creates logo panel.
- Line 980: Sets logo panel background.
- Line 981: Sets logo panel size.
- Line 982: Builds logo label.
- Line 983: Adds logo label.
- Line 985: Creates title panel.
- Line 986: Sets title panel background.
- Line 987: Creates org title label.
- Line 988: Sets title font.
- Line 989: Sets title color.
- Line 990: Adds title label.
- Line 992: Creates left container.
- Line 993: Sets left container background.
- Line 994: Adds logo panel left.
- Line 995: Adds title panel center.
- Line 997: Adds left container to header.
- Line 999: Returns header panel.
- Line 1002: Starts logo creation.
- Line 1003: Loads logo resource.
- Line 1004: If found, use it.
- Line 1005: Creates ImageIcon.
- Line 1006: Scales logo.
- Line 1007: Returns logo label.
- Line 1010: Creates fallback label.
- Line 1011: Sets fallback font.
- Line 1012: Sets fallback color.
- Line 1013: Adds fallback border.
- Line 1014: Returns fallback label.
- Line 1017: Starts history filter helper.
- Line 1018: Loads all history.
- Line 1020: If no user, return empty list.
- Line 1021: Returns empty list instance.
- Line 1024: If admin, return all history.
- Line 1025: Returns full history.
- Line 1028: Creates list for user history.
- Line 1029: Iterates all history.
- Line 1030: Matches by username.
- Line 1031: Adds matching record.
- Line 1034: Returns filtered history.
- Line 1037: Starts role permission application.
- Line 1038: Checks if logged in.
- Line 1039: Detects admin.
- Line 1040: Detects borrower.
- Line 1042: Shows Add Book for admin.
- Line 1043: Shows Add User for admin.
- Line 1044: Shows View Users for admin.
- Line 1045: Shows Update for admin.
- Line 1046: Shows Delete for admin.
- Line 1047: Shows Show All for logged-in.
- Line 1049: Shows Available for borrowers.
- Line 1050: Shows Borrow for borrowers.
- Line 1051: Shows Return for borrowers.
- Line 1053: Shows Search for logged-in.
- Line 1054: Shows History for logged-in.
- Line 1055: Shows Top Borrowed for logged-in.
- Line 1056: Shows Logout for logged-in.
- Line 1058: Enables Add Book if admin.
- Line 1059: Enables Add User if admin.
- Line 1060: Enables View Users if admin.
- Line 1061: Enables Update if allowed.
- Line 1062: Enables Delete if allowed.
- Line 1063: Enables Search if logged in.
- Line 1064: Enables Show All if logged in.
- Line 1065: Enables Available if logged in.
- Line 1066: Enables Borrow for borrowers.
- Line 1067: Enables Return for borrowers.
- Line 1068: Enables History if logged in.
- Line 1069: Enables Top Borrowed if logged in.
- Line 1070: Enables Logout if logged in.
- Line 1072: Revalidates layout.
- Line 1073: Repaints UI.
- Line 1076: Starts logout flow.
- Line 1077: Confirms logout.
- Line 1078: Dialog parent.
- Line 1079: Message text.
- Line 1080: Dialog title.
- Line 1081: Yes/No option.
- Line 1083: Exit if not confirmed.
- Line 1087: Clears current user.
- Line 1088: Resets session label.
- Line 1089: Reapplies permissions.
- Line 1091: Shows login again.
- Line 1092: Refreshes book list.
- Line 1095: Starts authentication.
- Line 1096: Validates username is not empty.
- Line 1097: Returns null if invalid.
- Line 1100: Normalizes username.
- Line 1102: Creates admin user for fixed login.
- Line 1103: Checks admin credentials.
- Line 1104: Returns admin on match.
- Line 1107: Tries users file authentication.
- Line 1108: Returns file user if found.
- Line 1112: Returns null if no match.
- Line 1115: Starts users-file authentication.
- Line 1116: Loads users file.
- Line 1117: Returns null if missing.
- Line 1121: Finds username column index.
- Line 1122: Finds password column index.
- Line 1123: Finds type column index.
- Line 1125: If username/password missing, return null.
- Line 1129: Iterates each user row.
- Line 1130: Skips short rows.
- Line 1134: Reads saved username.
- Line 1135: Reads saved password.
- Line 1136: Compares credentials.
- Line 1137: Continues if mismatch.
- Line 1140: Reads user type or defaults.
- Line 1141: Admin type returns AdminUser.
- Line 1144: Faculty type returns FacultyUser.
- Line 1148: UG type returns UG_Student.
- Line 1151: G type returns G_Student.
- Line 1154: Unknown type returns null.
- Line 1157: Returns null if not found.
- Line 1160: Starts users-file loader.
- Line 1161: Opens users file path.
- Line 1162: Returns null if file missing/empty.
- Line 1166: Opens reader with try-with-resources.
- Line 1167: Reads header line.
- Line 1168: Returns null if header missing.
- Line 1172: Parses header CSV.
- Line 1173: Creates row list.
- Line 1175: Reads each line.
- Line 1176: Skips empty lines.
- Line 1180: Parses CSV row and adds it.
- Line 1183: Returns `UsersFileData`.
- Line 1184: Catches I/O errors.
- Line 1185: Returns null on error.
- Line 1189: Starts case-insensitive index helper.
- Line 1190: Null guard.
- Line 1194: Iterates values.
- Line 1195: Compares trimmed values case-insensitively.
- Line 1196: Returns index if matched.
- Line 1199: Returns -1 if not found.
- Line 1202: Starts CSV parse wrapper.
- Line 1203: Delegates to `textfile.parseCsvLine`.
- Line 1206: Starts genre update options builder.
- Line 1207: Creates array with extra slot.
- Line 1208: Adds "Keep current" at index 0.
- Line 1209: Copies genres into remaining slots.
- Line 1210: Returns options array.
