package edu.oel.library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Main Swing window that connects user interactions to the validated service layer.
 *
 * <p>The form supports add, update, delete, and independent status changes.
 * The table shows all books or a live-search subset. No persistence or business
 * validation is implemented in the GUI.</p>
 *
 * @author OEL Student Team
 */
public final class LibraryFrame extends JFrame {
    private static final Color NAVY = new Color(17, 34, 59);
    private static final Color NAVY_LIGHT = new Color(35, 56, 86);
    private static final Color TEAL = new Color(0, 152, 145);
    private static final Color INK = new Color(28, 47, 71);
    private static final Color MUTED = new Color(106, 122, 143);
    private static final Color SURFACE = new Color(246, 248, 252);
    private static final Color BORDER = new Color(222, 229, 236);
    private static final Font BODY = new Font("Dialog", Font.PLAIN, 14);
    private static final Font STRONG = new Font("Dialog", Font.BOLD, 14);

    private final LibraryService service;
    private final BookTableModel model = new BookTableModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField();
    private final JTextField idField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JComboBox<BookStatus> statusBox = new JComboBox<>(BookStatus.values());
    private final JLabel totalCount = makeStatValue();
    private final JLabel availableCount = makeStatValue();
    private final JLabel issuedCount = makeStatValue();
    private final JLabel reservedCount = makeStatValue();
    private final JLabel resultsLabel = new JLabel();
    private final JLabel infoLabel = new JLabel("Ready. Select a row to edit or add a new book.");
    private String selectedOriginalId;

    /**
     * Constructs and displays a fully configured library management interface.
     *
     * @param service library data and business operations
     */
    public LibraryFrame(LibraryService service) {
        super("ShelfWise | Library Book Management System");
        if (service == null) {
            throw new IllegalArgumentException("Library service is required.");
        }
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1390, 820);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
        setContentPane(buildLayout());
        attachListeners();
        refresh("Ready. Add your first book or select a record to edit.");
    }

    /**
     * Refreshes all visible counts and books from the service.
     *
     * @param notification message to show in the bottom status bar
     */
    public void refresh(String notification) {
        List<Book> matches = service.search(searchField.getText());
        model.setBooks(matches);
        totalCount.setText(String.valueOf(service.size()));
        availableCount.setText(String.valueOf(service.countByStatus(BookStatus.AVAILABLE)));
        issuedCount.setText(String.valueOf(service.countByStatus(BookStatus.ISSUED)));
        reservedCount.setText(String.valueOf(service.countByStatus(BookStatus.RESERVED)));
        resultsLabel.setText("Showing " + matches.size() + " of " + service.size() + " books"
                + (matches.isEmpty() ? "  |  No books found" : ""));
        infoLabel.setText(notification);
    }

    private JPanel buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);
        root.add(buildSidebar(), BorderLayout.WEST);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(SURFACE);
        body.setBorder(new EmptyBorder(32, 32, 22, 32));
        body.add(buildTitleAndStats(), BorderLayout.NORTH);
        body.add(buildWorkArea(), BorderLayout.CENTER);
        infoLabel.setForeground(MUTED);
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        infoLabel.setBorder(new EmptyBorder(8, 3, 0, 0));
        body.add(infoLabel, BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(NAVY);
        side.setPreferredSize(new Dimension(210, 700));
        side.setBorder(new EmptyBorder(36, 22, 22, 22));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        JLabel mark = new JLabel("S  /  W");
        mark.setFont(new Font("Dialog", Font.BOLD, 28));
        mark.setForeground(new Color(68, 218, 197));
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(mark);
        side.add(Box.createVerticalStrut(10));
        JLabel app = new JLabel("SHELFWISE");
        app.setFont(new Font("Dialog", Font.BOLD, 18));
        app.setForeground(Color.WHITE);
        app.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(app);
        JLabel sub = new JLabel("LIBRARY MANAGEMENT");
        sub.setFont(new Font("Dialog", Font.PLAIN, 9));
        sub.setForeground(new Color(154, 171, 196));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(Box.createVerticalStrut(6));
        side.add(sub);
        side.add(Box.createVerticalStrut(55));
        JPanel nav = new JPanel(new BorderLayout());
        nav.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        nav.setBackground(NAVY_LIGHT);
        nav.setBorder(new EmptyBorder(12, 14, 12, 8));
        JLabel navLabel = new JLabel("  BOOK CATALOGUE");
        navLabel.setForeground(Color.WHITE);
        navLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        nav.add(navLabel);
        nav.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(nav);
        side.add(Box.createVerticalStrut(20));
        JLabel hint = new JLabel("OEL  |  SOFTWARE CONSTRUCTION");
        hint.setForeground(new Color(143, 163, 188));
        hint.setFont(new Font("Dialog", Font.PLAIN, 9));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(hint);
        side.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("JAVA SWING  -  JDK 17+");
        footer.setForeground(new Color(155, 173, 198));
        footer.setFont(new Font("Dialog", Font.PLAIN, 10));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(footer);
        return side;
    }

    private JPanel buildTitleAndStats() {
        JPanel top = new JPanel(new BorderLayout(0, 22));
        top.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        JLabel heading = new JLabel("Book Catalogue");
        heading.setForeground(INK);
        heading.setFont(new Font("Dialog", Font.BOLD, 30));
        JLabel subtitle = new JLabel("Manage books, availability and records from one place.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(BODY);
        titles.add(heading, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);
        top.add(titles, BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(statCard("TOTAL BOOKS", totalCount, TEAL));
        cards.add(statCard("AVAILABLE", availableCount, new Color(31, 160, 117)));
        cards.add(statCard("ISSUED", issuedCount, new Color(232, 149, 42)));
        cards.add(statCard("RESERVED", reservedCount, new Color(96, 103, 200)));
        top.add(cards, BorderLayout.CENTER);
        return top;
    }

    private JPanel statCard(String name, JLabel number, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(16, 18, 15, 18)));
        JLabel title = new JLabel(name);
        title.setFont(new Font("Dialog", Font.BOLD, 11));
        title.setForeground(MUTED);
        number.setForeground(accent);
        card.add(title, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildWorkArea() {
        JPanel work = new JPanel(new BorderLayout(16, 0));
        work.setOpaque(false);
        work.add(buildCataloguePanel(), BorderLayout.CENTER);
        JPanel editor = buildEditorPanel();
        editor.setPreferredSize(new Dimension(320, 400));
        work.add(editor, BorderLayout.EAST);
        return work;
    }

    private JPanel buildCataloguePanel() {
        JPanel catalog = new JPanel(new BorderLayout(0, 18));
        catalog.setBackground(Color.WHITE);
        catalog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(19, 18, 18, 18)));
        JPanel searchPane = new JPanel(new BorderLayout(0, 10));
        searchPane.setOpaque(false);
        JPanel titlePane = new JPanel(new BorderLayout());
        titlePane.setOpaque(false);
        JLabel listTitle = new JLabel("All books");
        listTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        listTitle.setForeground(INK);
        resultsLabel.setForeground(MUTED);
        resultsLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        titlePane.add(listTitle, BorderLayout.WEST);
        titlePane.add(resultsLabel, BorderLayout.EAST);
        searchPane.add(titlePane, BorderLayout.NORTH);
        setupTextField(searchField, "searchField");
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, title, author, or status...");
        searchField.setToolTipText("Type any part of a book ID, title, author or status");
        searchPane.add(searchField, BorderLayout.CENTER);
        catalog.add(searchPane, BorderLayout.NORTH);
        table.setName("booksTable");
        table.setRowHeight(47);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Dialog", Font.PLAIN, 13));
        table.setForeground(INK);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(225, 244, 241));
        table.setSelectionForeground(INK);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(237, 240, 244));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 43));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(241, 245, 248));
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(165);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        catalog.add(scroll, BorderLayout.CENTER);
        JLabel helper = new JLabel("TIP: Click a book row to load its details into the editor.");
        helper.setFont(new Font("Dialog", Font.PLAIN, 11));
        helper.setForeground(MUTED);
        catalog.add(helper, BorderLayout.SOUTH);
        return catalog;
    }

    private JPanel buildEditorPanel() {
        JPanel editor = new JPanel();
        editor.setLayout(new BoxLayout(editor, BoxLayout.Y_AXIS));
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(20, 18, 20, 18)));
        JLabel heading = new JLabel("Book details");
        heading.setFont(new Font("Dialog", Font.BOLD, 19));
        heading.setForeground(INK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.add(heading);
        editor.add(Box.createVerticalStrut(5));
        JLabel note = new JLabel("Create or edit a catalogue record");
        note.setForeground(MUTED);
        note.setFont(new Font("Dialog", Font.PLAIN, 11));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.add(note);
        editor.add(Box.createVerticalStrut(26));
        setupTextField(idField, "idField");
        setupTextField(titleField, "titleField");
        setupTextField(authorField, "authorField");
        editor.add(fieldGroup("BOOK ID *", idField));
        editor.add(Box.createVerticalStrut(16));
        editor.add(fieldGroup("TITLE *", titleField));
        editor.add(Box.createVerticalStrut(16));
        editor.add(fieldGroup("AUTHOR *", authorField));
        editor.add(Box.createVerticalStrut(16));
        statusBox.setName("statusBox");
        statusBox.setFont(BODY);
        statusBox.setBackground(Color.WHITE);
        statusBox.setFocusable(false);
        statusBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        editor.add(fieldGroup("CIRCULATION STATUS", statusBox));
        editor.add(Box.createVerticalStrut(24));
        JButton add = button("+  Add Book", TEAL, Color.WHITE);
        add.setName("addButton");
        add.addActionListener(e -> addBook());
        editor.add(add);
        editor.add(Box.createVerticalStrut(9));
        JButton update = button("Update Selected", NAVY, Color.WHITE);
        update.setName("updateButton");
        update.addActionListener(e -> updateBook());
        editor.add(update);
        editor.add(Box.createVerticalStrut(9));
        JButton status = button("Set Status", new Color(225, 240, 239), INK);
        status.setName("statusButton");
        status.addActionListener(e -> setStatus());
        editor.add(status);
        editor.add(Box.createVerticalStrut(9));
        JButton delete = button("Delete Selected", new Color(255, 236, 236), new Color(160, 47, 55));
        delete.setName("deleteButton");
        delete.addActionListener(e -> deleteBook());
        editor.add(delete);
        editor.add(Box.createVerticalStrut(9));
        JButton clear = button("Clear Form", new Color(238, 242, 247), INK);
        clear.setName("clearButton");
        clear.addActionListener(e -> clearForm());
        editor.add(clear);
        editor.add(Box.createVerticalGlue());
        JLabel note2 = new JLabel("* Required. ID must be unique.");
        note2.setFont(new Font("Dialog", Font.PLAIN, 11));
        note2.setForeground(MUTED);
        note2.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.add(note2);
        return editor;
    }

    private JPanel fieldGroup(String text, Component input) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Dialog", Font.BOLD, 11));
        group.add(label, BorderLayout.NORTH);
        group.add(input, BorderLayout.CENTER);
        return group;
    }

    private void setupTextField(JTextField field, String name) {
        field.setName(name);
        field.setFont(BODY);
        field.setForeground(INK);
        field.setBackground(Color.WHITE);
        field.setCaretColor(TEAL);
        field.setPreferredSize(new Dimension(150, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(0, 12, 0, 12)));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TEAL, 2), new EmptyBorder(0, 11, 0, 11)));
            }
            @Override
            public void focusLost(FocusEvent event) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1), new EmptyBorder(0, 12, 0, 12)));
            }
        });
    }

    private JButton button(String label, Color background, Color foreground) {
        JButton button = new JButton(label);
        button.setFont(STRONG);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(265, 42));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private void attachListeners() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { refresh("Search updated."); }
            @Override public void removeUpdate(DocumentEvent event) { refresh("Search updated."); }
            @Override public void changedUpdate(DocumentEvent event) { refresh("Search updated."); }
        });
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            Book book = model.getBookAt(row);
            selectedOriginalId = book.getId();
            idField.setText(book.getId());
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            statusBox.setSelectedItem(book.getStatus());
            infoLabel.setText("Selected " + book.getId() + ". You can now update, delete or change its status.");
        });
    }

    private void addBook() {
        try {
            Book added = service.addBook(idField.getText(), titleField.getText(), authorField.getText());
            clearForm();
            refresh("Book " + added.getId() + " added successfully.");
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void updateBook() {
        if (selectedOriginalId == null) {
            showError(new LibraryException("Select an existing book row before updating."));
            return;
        }
        try {
            Book updated = service.updateBook(selectedOriginalId, idField.getText(), titleField.getText(),
                    authorField.getText(), (BookStatus) statusBox.getSelectedItem());
            clearForm();
            refresh("Book " + updated.getId() + " updated successfully.");
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void setStatus() {
        if (selectedOriginalId == null) {
            showError(new LibraryException("Select an existing book row before changing status."));
            return;
        }
        try {
            Book changed = service.setStatus(selectedOriginalId, (BookStatus) statusBox.getSelectedItem());
            clearForm();
            refresh("Book " + changed.getId() + " status changed to " + changed.getStatus() + ".");
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void deleteBook() {
        if (selectedOriginalId == null) {
            showError(new LibraryException("Select an existing book row before deleting."));
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete book " + selectedOriginalId + "? This action cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Book deleted = service.deleteBook(selectedOriginalId);
            clearForm();
            refresh("Book " + deleted.getId() + " deleted successfully.");
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void clearForm() {
        selectedOriginalId = null;
        table.clearSelection();
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        statusBox.setSelectedItem(BookStatus.AVAILABLE);
        infoLabel.setText("Form cleared. Enter details to add a book.");
    }

    private void showError(RuntimeException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Validation / operation error",
                JOptionPane.ERROR_MESSAGE);
        infoLabel.setText("Action rejected: " + exception.getMessage());
    }

    private static JLabel makeStatValue() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Dialog", Font.BOLD, 30));
        return label;
    }

    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean isSelected, boolean hasFocus,
                                                        int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Dialog", Font.BOLD, 12));
            if (!isSelected) {
                label.setBackground(Color.WHITE);
            }
            if ("Available".equals(value)) {
                label.setForeground(new Color(26, 143, 102));
            } else if ("Issued".equals(value)) {
                label.setForeground(new Color(185, 114, 23));
            } else {
                label.setForeground(new Color(94, 90, 185));
            }
            label.setBorder(new EmptyBorder(0, 5, 0, 5));
            return label;
        }
    }
}
