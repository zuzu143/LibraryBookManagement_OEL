import edu.oel.library.BookStatus;
import edu.oel.library.LibraryFrame;
import edu.oel.library.LibraryRepository;
import edu.oel.library.LibraryService;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Captures genuine rendered Swing screens in a virtual or physical display. */
public final class ScreenshotCapture {
    private static LibraryFrame frame;
    private static LibraryService library;
    private static Robot robot;
    private static Path destination;

    private ScreenshotCapture() { }

    public static void main(String[] args) throws Exception {
        destination = Path.of("evidence/screenshots");
        Files.createDirectories(destination);
        Path temp = Files.createTempDirectory("oel-screenshot-");
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    library = new LibraryService(new LibraryRepository(temp.resolve("data.properties")));
                    frame = new LibraryFrame(library);
                    frame.setVisible(true);
                } catch (Exception exception) { throw new RuntimeException(exception); }
            });
            robot = new Robot();
            robot.setAutoDelay(30);
            Thread.sleep(600);
            capture("01-empty-library.png");
            SwingUtilities.invokeAndWait(() -> {
                library.addBook("BK-101", "Clean Code", "Robert C. Martin");
                library.addBook("BK-102", "Effective Java", "Joshua Bloch");
                library.addBook("BK-103", "Design Patterns", "Erich Gamma et al.");
                library.addBook("BK-104", "Head First Java", "Kathy Sierra");
                library.addBook("BK-105", "Introduction to Algorithms", "Thomas Cormen");
                library.setStatus("BK-103", BookStatus.RESERVED);
                library.setStatus("BK-105", BookStatus.ISSUED);
                frame.refresh("Five example books added successfully. Data saved to disk.");
            });
            capture("02-books-and-statistics.png");
            SwingUtilities.invokeAndWait(() -> {
                JTable table = find("booksTable", JTable.class);
                table.setRowSelectionInterval(1, 1);
                @SuppressWarnings("unchecked")
                JComboBox<BookStatus> statusBox = find("statusBox", JComboBox.class);
                statusBox.setSelectedItem(BookStatus.RESERVED);
            });
            capture("03-select-and-edit.png");
            SwingUtilities.invokeAndWait(() -> {
                library.setStatus("BK-102", BookStatus.RESERVED);
                frame.refresh("Book BK-102 status changed to Reserved.");
            });
            capture("04-changed-status.png");
            System.out.println("START searching");
            SwingUtilities.invokeAndWait(() -> {
                JButton clear = find("clearButton", JButton.class);
                clear.doClick();
                find("searchField", JTextField.class).setText("java");
            });
            System.out.println("END searching");
            capture("05-search-results.png");
            SwingUtilities.invokeAndWait(() -> {
                find("searchField", JTextField.class).setText("");
                find("idField", JTextField.class).setText("BK-101");
                find("titleField", JTextField.class).setText("Duplicate sample");
                find("authorField", JTextField.class).setText("Sample author");
            });
            SwingUtilities.invokeLater(() -> find("addButton", JButton.class).doClick());
            Thread.sleep(700);
            capture("06-duplicate-id-error.png");
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            Thread.sleep(350);
            SwingUtilities.invokeAndWait(() -> {
                find("clearButton", JButton.class).doClick();
                find("idField", JTextField.class).setText("BK-106");
                find("titleField", JTextField.class).setText(" ");
                find("authorField", JTextField.class).setText("Test author");
            });
            SwingUtilities.invokeLater(() -> find("addButton", JButton.class).doClick());
            Thread.sleep(700);
            capture("07-empty-title-error.png");
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            Thread.sleep(350);
            SwingUtilities.invokeAndWait(() -> {
                find("clearButton", JButton.class).doClick();
                library.deleteBook("BK-104");
                frame.refresh("Book BK-104 deleted successfully. Total books: 4.");
            });
            capture("08-delete-result.png");
            SwingUtilities.invokeAndWait(frame::dispose);
        } finally {
            try (var files = Files.walk(temp)) {
                for (Path file : files.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(file);
                }
            }
        }
        System.out.println("Captured 8 Swing screenshots from real live application windows.");
    }

    private static void capture(String filename) throws Exception {
        Thread.sleep(260);
        Rectangle area = new Rectangle(frame.getLocationOnScreen(), frame.getSize());
        ImageIO.write(robot.createScreenCapture(area), "png", destination.resolve(filename).toFile());
        System.out.println(filename);
    }

    private static <T extends JComponent> T find(String name, Class<T> type) {
        return search(frame.getContentPane(), name, type);
    }

    private static <T extends JComponent> T search(Container parent, String name, Class<T> type) {
        for (Component component : parent.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                T nested = search((Container) component, name, type);
                if (nested != null) return nested;
            }
        }
        return null;
    }
}
