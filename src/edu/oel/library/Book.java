package edu.oel.library;

import java.util.Objects;

/**
 * Immutable model containing the identity, metadata, and circulation status of a book.
 *
 * @author OEL Student Team
 */
public final class Book {
    private final String id;
    private final String title;
    private final String author;
    private final BookStatus status;

    /**
     * Creates an immutable book record.
     *
     * @param id unique book identifier
     * @param title non-empty book title
     * @param author non-empty author name
     * @param status current circulation status
     */
    public Book(String id, String title, String author, BookStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.author = Objects.requireNonNull(author, "author");
        this.status = Objects.requireNonNull(status, "status");
    }

    /** @return the book identifier */
    public String getId() { return id; }

    /** @return the title */
    public String getTitle() { return title; }

    /** @return the author's name */
    public String getAuthor() { return author; }

    /** @return the current circulation status */
    public BookStatus getStatus() { return status; }

    /**
     * Creates a modified copy; the original book is unchanged.
     *
     * @param newId updated identifier
     * @param newTitle updated title
     * @param newAuthor updated author
     * @param newStatus updated circulation status
     * @return an immutable copy with replacement values
     */
    public Book withDetails(String newId, String newTitle, String newAuthor, BookStatus newStatus) {
        return new Book(newId, newTitle, newAuthor, newStatus);
    }

    /**
     * Creates a copy with a changed circulation state.
     *
     * @param newStatus new circulation state
     * @return an immutable copy with the new state
     */
    public Book withStatus(BookStatus newStatus) {
        return new Book(id, title, author, newStatus);
    }
}
