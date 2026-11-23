package edu.oel.library;

import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Desktop application entry point; starts Swing on its Event Dispatch Thread.
 *
 * @author OEL Student Team
 */
public final class Main {
    /** Utility class: never instantiate. */
    private Main() { }

    /**
     * Starts the GUI, loading persisted data from the project data directory.
     *
     * @param args optional command-line arguments (currently unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                String location = System.getProperty("library.data.path", "data/library.properties");
                LibraryService service = new LibraryService(new LibraryRepository(Path.of(location)));
                LibraryFrame window = new LibraryFrame(service);
                window.setVisible(true);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Could not start ShelfWise: " + exception.getMessage(),
                        "Startup error", JOptionPane.ERROR_MESSAGE);
                exception.printStackTrace();
            }
        });
    }
}
