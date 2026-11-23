package edu.oel.library;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Dependency-free regression suite for CRUD, validation, search, statuses, and persistence.
 *
 * <p>Each test has its own temporary database directory. Exit status is non-zero on failure.
 * Runs without JUnit or internet access.</p>
 *
 * @author OEL Student Team
 */
public final class LibraryServiceTest {
    private static int passed;
    private static int failed;

    private LibraryServiceTest() { }

    /**
     * Runs all cases and prints expected and actual outcomes.
     *
     * @param args optional arguments, not used
     * @throws Exception if the test harness cannot initialize
     */
    public static void main(String[] args) throws Exception {
        check("TC01", "Empty library", "size=0", () -> {
            withService(s -> { assertEquals(0, s.size()); return null; });
            return "size=0";
        });
        check("TC02", "Valid book added", "ID=BK-101; status=Available", () ->
                withService(s -> {
                    Book b = s.addBook("BK-101", "Clean Code", "Robert Martin");
                    assertEquals("BK-101", b.getId());
                    assertEquals(BookStatus.AVAILABLE, b.getStatus());
                    return "ID=BK-101; status=Available";
                }));
        check("TC03", "Trim input", "title=Clean Code", () ->
                withService(s -> {
                    Book b = s.addBook(" BK-101 ", "  Clean Code  ", "  Martin ");
                    assertEquals("Clean Code", b.getTitle());
                    return "title=Clean Code";
                }));
        check("TC04", "Duplicate ID", "rejected duplicate", () ->
                withService(s -> {
                    s.addBook("BK-101", "Clean Code", "Martin");
                    expectError(() -> s.addBook("BK-101", "Another", "Writer"), "already exists");
                    return "rejected duplicate";
                }));
        check("TC05", "Duplicate ID case insensitive", "rejected bk-101", () ->
                withService(s -> {
                    s.addBook("BK-101", "Clean Code", "Martin");
                    expectError(() -> s.addBook("bk-101", "Another", "Writer"), "already exists");
                    return "rejected bk-101";
                }));
        check("TC06", "Empty title", "rejected empty title", () ->
                withService(s -> {
                    expectError(() -> s.addBook("BK-101", "", "Martin"), "title cannot be empty");
                    return "rejected empty title";
                }));
        check("TC07", "Whitespace-only title", "rejected whitespace title", () ->
                withService(s -> {
                    expectError(() -> s.addBook("BK-101", "  ", "Martin"), "title cannot be empty");
                    return "rejected whitespace title";
                }));
        check("TC08", "Empty author", "rejected empty author", () ->
                withService(s -> {
                    expectError(() -> s.addBook("BK-101", "Clean Code", ""), "author name cannot be empty");
                    return "rejected empty author";
                }));
        check("TC09", "Missing ID", "rejected invalid ID", () ->
                withService(s -> {
                    expectError(() -> s.addBook("", "Clean Code", "Martin"), "Book ID must");
                    return "rejected invalid ID";
                }));
        check("TC10", "Invalid ID characters", "rejected bad ID", () ->
                withService(s -> {
                    expectError(() -> s.addBook("ID 22", "Clean Code", "Martin"), "Book ID must");
                    return "rejected bad ID";
                }));
        check("TC11", "View all books", "2 books listed", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(2, s.getAllBooks().size());
                    return "2 books listed";
                }));
        check("TC12", "Search by author", "1 author match", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(1, s.search("bloch").size());
                    return "1 author match";
                }));
        check("TC13", "Search by status", "2 available matches", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(2, s.search("AVAIL").size());
                    return "2 available matches";
                }));
        check("TC14", "Search no match", "0 results", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(0, s.search("unknown title").size());
                    return "0 results";
                }));
        check("TC15", "Update existing details", "title=Refactoring", () ->
                withService(s -> {
                    seed(s);
                    Book b = s.updateBook("BK-101", "BK-101", "Refactoring", "Martin Fowler", BookStatus.AVAILABLE);
                    assertEquals("Refactoring", b.getTitle());
                    assertEquals("Martin Fowler", s.findById("BK-101").getAuthor());
                    return "title=Refactoring";
                }));
        check("TC16", "Change book ID", "old=null; new=BK-105", () ->
                withService(s -> {
                    seed(s);
                    s.updateBook("BK-101", "BK-105", "Clean Code", "Martin", BookStatus.AVAILABLE);
                    assertEquals(null, s.findById("BK-101"));
                    assertEquals("BK-105", s.findById("BK-105").getId());
                    return "old=null; new=BK-105";
                }));
        check("TC17", "Update missing book", "rejected unknown ID", () ->
                withService(s -> {
                    expectError(() -> s.updateBook("UNKNOWN", "UNKNOWN", "Title", "Author", BookStatus.AVAILABLE),
                            "not found");
                    return "rejected unknown ID";
                }));
        check("TC18", "Update to duplicate ID", "rejected ID collision", () ->
                withService(s -> {
                    seed(s);
                    expectError(() -> s.updateBook("BK-101", "BK-102", "Title", "Author", BookStatus.AVAILABLE),
                            "already exists");
                    return "rejected ID collision";
                }));
        check("TC19", "Set Issued", "status=Issued", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(BookStatus.ISSUED, s.setStatus("BK-101", BookStatus.ISSUED).getStatus());
                    return "status=Issued";
                }));
        check("TC20", "Set Reserved", "status=Reserved", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(BookStatus.RESERVED, s.setStatus("BK-102", BookStatus.RESERVED).getStatus());
                    return "status=Reserved";
                }));
        check("TC21", "Return to Available", "status=Available", () ->
                withService(s -> {
                    seed(s);
                    s.setStatus("BK-101", BookStatus.ISSUED);
                    assertEquals(BookStatus.AVAILABLE, s.setStatus("BK-101", BookStatus.AVAILABLE).getStatus());
                    return "status=Available";
                }));
        check("TC22", "Null status", "rejected null status", () ->
                withService(s -> {
                    seed(s);
                    expectError(() -> s.setStatus("BK-101", null), "valid status");
                    return "rejected null status";
                }));
        check("TC23", "Delete existing book", "remaining=1", () ->
                withService(s -> {
                    seed(s);
                    s.deleteBook("BK-101");
                    assertEquals(1, s.size());
                    return "remaining=1";
                }));
        check("TC24", "Delete missing book", "rejected unknown ID", () ->
                withService(s -> {
                    expectError(() -> s.deleteBook("MISSING"), "not found");
                    return "rejected unknown ID";
                }));
        check("TC25", "Persistence after restart", "reloaded=2", () ->
                withService(s -> {
                    seed(s);
                    LibraryService restored = new LibraryService(new LibraryRepository(
                            Path.of(System.getProperty("oel.current.test.path"))));
                    assertEquals(2, restored.size());
                    return "reloaded=2";
                }));
        check("TC26", "Status saved after restart", "reloaded=Reserved", () ->
                withService(s -> {
                    seed(s);
                    s.setStatus("BK-102", BookStatus.RESERVED);
                    LibraryService restored = new LibraryService(new LibraryRepository(
                            Path.of(System.getProperty("oel.current.test.path"))));
                    assertEquals(BookStatus.RESERVED, restored.findById("BK-102").getStatus());
                    return "reloaded=Reserved";
                }));
        check("TC27", "Blank search", "all 2 results", () ->
                withService(s -> {
                    seed(s);
                    assertEquals(2, s.search("   ").size());
                    return "all 2 results";
                }));
        check("TC28", "Title with newline", "rejected control character", () ->
                withService(s -> {
                    expectError(() -> s.addBook("BK-101", "Book\nTitle", "Writer"), "line breaks");
                    return "rejected control character";
                }));
        check("TC29", "Invalid add has no side effects", "size remains 2", () ->
                withService(s -> {
                    seed(s);
                    expectError(() -> s.addBook("BK-101", "Another", "Writer"), "already exists");
                    assertEquals(2, s.size());
                    return "size remains 2";
                }));
        check("TC30", "Counters by status", "1 issued; 1 reserved", () ->
                withService(s -> {
                    seed(s);
                    s.setStatus("BK-101", BookStatus.ISSUED);
                    s.setStatus("BK-102", BookStatus.RESERVED);
                    assertEquals(1L, s.countByStatus(BookStatus.ISSUED));
                    assertEquals(1L, s.countByStatus(BookStatus.RESERVED));
                    return "1 issued; 1 reserved";
                }));
        System.out.printf("%nSUMMARY: %d PASS, %d FAIL, %d TOTAL%n", passed, failed, passed + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static <T> T withService(ServiceAction<T> action) throws Exception {
        Path folder = Files.createTempDirectory("oel-case-");
        Path db = folder.resolve("library.properties");
        System.setProperty("oel.current.test.path", db.toString());
        try {
            return action.execute(new LibraryService(new LibraryRepository(db)));
        } finally {
            try (var stream = Files.walk(folder)) {
                for (Path file : stream.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(file);
                }
            }
            System.clearProperty("oel.current.test.path");
        }
    }

    private static void seed(LibraryService service) {
        service.addBook("BK-101", "Clean Code", "Robert Martin");
        service.addBook("BK-102", "Effective Java", "Joshua Bloch");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    private static void expectError(Runnable operation, String phrase) {
        try {
            operation.run();
            throw new AssertionError("Expected validation error containing: " + phrase);
        } catch (LibraryException exception) {
            if (!exception.getMessage().toLowerCase().contains(phrase.toLowerCase())) {
                throw new AssertionError("Wrong error: " + exception.getMessage());
            }
        }
    }

    private static void check(String id, String name, String expected, CheckedAction action) {
        try {
            String actual = action.execute();
            assertEquals(expected, actual);
            System.out.printf("%s | %-30s | Expected: %-25s | Actual: %-25s | PASS%n",
                    id, name, expected, actual);
            passed++;
        } catch (Exception | AssertionError ex) {
            System.out.printf("%s | %-30s | Expected: %-25s | Actual: %-25s | FAIL (%s)%n",
                    id, name, expected, "failure", ex.getMessage());
            failed++;
        }
    }

    @FunctionalInterface
    private interface CheckedAction { String execute() throws Exception; }

    @FunctionalInterface
    private interface ServiceAction<T> { T execute(LibraryService service) throws Exception; }
}
