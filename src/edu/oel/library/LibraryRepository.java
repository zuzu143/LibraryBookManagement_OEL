package edu.oel.library;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Stores books in a human-readable Java properties file using a temporary-file swap.
 *
 * <p>No external database or libraries are required. On an existing file, malformed
 * records produce an I/O error instead of being silently discarded.</p>
 *
 * @author OEL Student Team
 */
public final class LibraryRepository {
    private final Path dataPath;

    /**
     * Creates the persistence component for the supplied path.
     *
     * @param dataPath local path of the book database file
     */
    public LibraryRepository(Path dataPath) {
        if (dataPath == null) {
            throw new IllegalArgumentException("Data path cannot be null.");
        }
        this.dataPath = dataPath.toAbsolutePath().normalize();
    }

    /**
     * Reads existing books; a missing file represents a new empty library.
     *
     * @return books sorted by case-insensitive identifier
     * @throws IOException if stored records are invalid or cannot be read
     */
    public List<Book> load() throws IOException {
        if (!Files.exists(dataPath)) {
            return new ArrayList<>();
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(dataPath, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        List<Book> books = new ArrayList<>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith("book.") || !key.endsWith(".title")) {
                continue;
            }
            String id = key.substring(5, key.length() - 6);
            String prefix = "book." + id + ".";
            String title = properties.getProperty(prefix + "title");
            String author = properties.getProperty(prefix + "author");
            String rawStatus = properties.getProperty(prefix + "status");
            if (title == null || author == null || rawStatus == null) {
                throw new IOException("Incomplete stored record for book ID: " + id);
            }
            try {
                BookStatus status = BookStatus.valueOf(rawStatus);
                LibraryService.validateFields(id, title, author);
                books.add(new Book(id, title, author, status));
            } catch (IllegalArgumentException | LibraryException ex) {
                throw new IOException("Invalid stored record for book ID: " + id, ex);
            }
        }
        books.sort(Comparator.comparing(Book::getId, String.CASE_INSENSITIVE_ORDER));
        return books;
    }

    /**
     * Writes all books to disk, replacing the existing file only when a full write succeeds.
     *
     * @param books complete collection to store
     * @throws IOException if any directory or file operation fails
     */
    public void save(Collection<Book> books) throws IOException {
        Path directory = dataPath.getParent();
        if (directory != null) {
            Files.createDirectories(directory);
        }
        Properties properties = new Properties();
        for (Book book : books) {
            String prefix = "book." + book.getId() + ".";
            properties.setProperty(prefix + "title", book.getTitle());
            properties.setProperty(prefix + "author", book.getAuthor());
            properties.setProperty(prefix + "status", book.getStatus().name());
        }
        Path temporary = Files.createTempFile(directory, "library-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                properties.store(writer, "Library Book Management System - generated data");
            }
            try {
                Files.move(temporary, dataPath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, dataPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * Returns the absolute storage location for display or diagnostics.
     *
     * @return absolute database file path
     */
    public Path getDataPath() {
        return dataPath;
    }
}
