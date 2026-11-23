# 2-minute demonstration (perform live)

1. Launch Main and show the empty library. Explain that a LinkedHashMap maintains unique IDs.
2. Add `BK-101`, `Clean Code`, `Robert C. Martin`. Confirm Available status.
3. Add `BK-102`, `Effective Java`, `Joshua Bloch`. Search for `java`.
4. Select BK-101, change title to `Clean Code (Updated)`, click Update.
5. Select BK-102, select Reserved, click Set Status. Then choose Issued.
6. Try adding BK-101 again: duplicate-ID validation dialog appears.
7. Try an empty title: required-field validation dialog appears.
8. Select BK-101, Delete, confirm. Exit and reopen to demonstrate persistence.
9. Run the test script, inspect Javadoc HTML, and show Git log and diff.

**Viva explanation:** `Book` is immutable; `LibraryService` validates and handles business operations; `LibraryRepository` safely writes to properties files; `LibraryFrame` only handles GUI events; `BookTableModel` supplies the JTable; `Main` starts Swing on the Event Dispatch Thread.
