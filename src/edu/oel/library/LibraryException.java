package edu.oel.library;

/**
 * Represents a user-correctable validation or record-not-found error.
 *
 * @author OEL Student Team
 */
public class LibraryException extends RuntimeException {
    /**
     * Creates an exception with a displayable validation message.
     *
     * @param message human-readable explanation of the problem
     */
    public LibraryException(String message) {
        super(message);
    }
}
