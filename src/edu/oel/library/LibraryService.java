package edu.oel.library;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Application business layer for validated CRUD, status changes, search and persistence.
 *
 * <p>A LinkedHashMap provides unique ID lookup with expected O(1) access.
 * Updates are first written to a copy and are committed in memory only after
 * the corresponding save to disk succeeds.</p>
 *
 * @author OEL Student Team
 */
public final class LibraryService {
    private final LibraryRepository repository;
    private LinkedHashMap<String, Book> books = new LinkedHashMap<>();

    /**
     * Loads all records from a repository into the service.
     *
     * @param repository persistent storage implementation
     * @throws IOException if the database cannot be opened or contains invalid records
     */
    public LibraryService(LibraryRepository repository) throws IOException {
        if (repository == null) {
            throw new IllegalArgumentException("Repository is required.");
        }
        this.repository = repository;
        for (Book book : repository.load()) {
            String key = key(book.getId());
            if (books.containsKey(key)) {
                throw new IOException("Duplicate stored book ID: " + book.getId());
            }
            books.put(key, book);
        }
    }

    /**
     * Validates all required book inputs; usable by repository and service.
     *
     * @param id book ID
     * @param title book title
     * @param author author name
     * @throws LibraryException if a required value is empty or invalid
     */
    public static void validateFields(String id, String title, String author) {
        if (id == null || !id.trim().matches("[A-Za-z0-9_-]{1,20}")) {
            throw new LibraryException("Book ID must be 1-20 letters, digits, '-' or '_'.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new LibraryException("Book title cannot be empty.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new LibraryException("Author name cannot be empty.");
        }
        if (containsControl(title) || containsControl(author)) {
            throw new LibraryException("Title and author cannot contain tabs or line breaks.");
        }
    }

    /**
     * Inserts a new book with default Available status.
     *
     * @param id unique ID
     * @param title book title
     * @param author author name
     * @return newly inserted book
     * @throws LibraryException if input is invalid or ID already exists
     */
    public Book addBook(String id, String title, String author) {
        validateFields(id, title, author);
        String bookId = id.trim();
        if (books.containsKey(key(bookId))) {
            throw new LibraryException("Book ID already exists: " + bookId);
        }
        Book book = new Book(bookId, title.trim(), author.trim(), BookStatus.AVAILABLE);
        LinkedHashMap<String, Book> changed = new LinkedHashMap<>(books);
        changed.put(key(bookId), book);
        persist(changed);
        return book;
    }

    /**
     * Updates an existing book, optionally changing its identifier.
     *
     * @param originalId ID identifying the record to update
     * @param newId replacement ID
     * @param title replacement title
     * @param author replacement author name
     * @param status replacement circulation status
     * @return the updated book
     * @throws LibraryException if the original is missing or new values conflict
     */
    public Book updateBook(String originalId, String newId, String title, String author,
                           BookStatus status) {
        validateFields(newId, title, author);
        if (status == null) {
            throw new LibraryException("Please choose a valid status.");
        }
        Book existing = books.get(key(originalId));
        if (existing == null) {
            throw new LibraryException("Cannot update: book ID not found: " + originalId);
        }
        String newKey = key(newId.trim());
        if (!key(existing.getId()).equals(newKey) && books.containsKey(newKey)) {
            throw new LibraryException("Book ID already exists: " + newId.trim());
        }
        Book updated = existing.withDetails(newId.trim(), title.trim(), author.trim(), status);
        LinkedHashMap<String, Book> changed = new LinkedHashMap<>();
        for (Map.Entry<String, Book> entry : books.entrySet()) {
            if (entry.getKey().equals(key(originalId))) {
                changed.put(newKey, updated);
            } else {
                changed.put(entry.getKey(), entry.getValue());
            }
        }
        persist(changed);
        return updated;
    }

    /**
     * Removes a book by its ID.
     *
     * @param id book ID to remove
     * @return the removed book
     * @throws LibraryException if the ID does not exist
     */
    public Book deleteBook(String id) {
        Book existing = getRequired(id);
        LinkedHashMap<String, Book> changed = new LinkedHashMap<>(books);
        changed.remove(key(id));
        persist(changed);
        return existing;
    }

    /**
     * Changes a book to Available, Issued, or Reserved.
     *
     * @param id book ID to change
     * @param status required new state
     * @return the updated book
     * @throws LibraryException if the ID does not exist or status is null
     */
    public Book setStatus(String id, BookStatus status) {
        if (status == null) {
            throw new LibraryException("Please choose a valid status.");
        }
        Book existing = getRequired(id);
        Book updated = existing.withStatus(status);
        LinkedHashMap<String, Book> changed = new LinkedHashMap<>(books);
        changed.put(key(id), updated);
        persist(changed);
        return updated;
    }

    /**
     * Finds an existing book, returning null if absent.
     *
     * @param id the ID to search
     * @return the matching book, or null when absent
     */
    public Book findById(String id) {
        return books.get(key(id));
    }

    /**
     * Returns a safe snapshot of all known books in insertion order.
     *
     * @return immutable list of books
     */
    public List<Book> getAllBooks() {
        return List.copyOf(books.values());
    }

    /**
     * Finds books matching an ID, title, author, or status, ignoring case.
     *
     * @param query the search phrase; null or blank returns all books
     * @return the filtered snapshot
     */
    public List<Book> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        String term = query.trim().toLowerCase(Locale.ROOT);
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getId().toLowerCase(Locale.ROOT).contains(term)
                    || book.getTitle().toLowerCase(Locale.ROOT).contains(term)
                    || book.getAuthor().toLowerCase(Locale.ROOT).contains(term)
                    || book.getStatus().getLabel().toLowerCase(Locale.ROOT).contains(term)) {
                result.add(book);
            }
        }
        return result;
    }

    /**
     * Counts records for a particular status.
     *
     * @param status circulation status
     * @return the number of books currently in that state
     */
    public long countByStatus(BookStatus status) {
        return books.values().stream().filter(book -> book.getStatus() == status).count();
    }

    /**
     * Returns the number of books stored.
     *
     * @return library size
     */
    public int size() {
        return books.size();
    }

    private static String key(String id) {
        return id == null ? "" : id.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean containsControl(String value) {
        return value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\t') >= 0;
    }

    private Book getRequired(String id) {
        Book book = findById(id);
        if (book == null) {
            throw new LibraryException("Book ID not found: " + id);
        }
        return book;
    }

    private void persist(LinkedHashMap<String, Book> changed) {
        try {
            repository.save(changed.values());
            books = changed;
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to save library data: " + ex.getMessage(), ex);
        }
    }
}
