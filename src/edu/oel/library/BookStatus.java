package edu.oel.library;

/**
 * Enumerates the three allowed circulation states of a book.
 *
 * @author OEL Student Team
 */
public enum BookStatus {
    /** The book can be borrowed. */
    AVAILABLE("Available"),
    /** The book is currently borrowed. */
    ISSUED("Issued"),
    /** The book is held for a reader. */
    RESERVED("Reserved");

    private final String label;

    /**
     * Creates a status with a readable label.
     *
     * @param label label shown to the user
     */
    BookStatus(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable status label.
     *
     * @return the display label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Provides the user-facing representation of this status.
     *
     * @return the display label
     */
    @Override
    public String toString() {
        return label;
    }
}
