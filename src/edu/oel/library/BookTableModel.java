package edu.oel.library;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Read-only adapter between LibraryService book records and the Swing JTable.
 *
 * @author OEL Student Team
 */
public final class BookTableModel extends AbstractTableModel {
    /**
     * Constructs an empty book table model.
     */
    public BookTableModel() { }

    private static final String[] COLUMNS = {"BOOK ID", "TITLE", "AUTHOR", "STATUS"};
    private List<Book> books = new ArrayList<>();

    /**
     * Replaces the displayed table content and notifies JTable of the change.
     *
     * @param records books to display
     */
    public void setBooks(List<Book> records) {
        books = new ArrayList<>(records);
        fireTableDataChanged();
    }

    /**
     * Returns the selected book using its underlying model index.
     *
     * @param row row number in the model
     * @return the matching book
     */
    public Book getBookAt(int row) {
        return books.get(row);
    }

    /**
     * Reports the number of books on screen.
     *
     * @return row count
     */
    @Override
    public int getRowCount() {
        return books.size();
    }

    /**
     * Reports the number of data columns.
     *
     * @return column count
     */
    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    /**
     * Reports the name for the specified column.
     *
     * @param column column index
     * @return column name
     */
    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    /**
     * Resolves book fields for JTable rendering.
     *
     * @param rowIndex model row index
     * @param columnIndex model column index
     * @return field value
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Book book = books.get(rowIndex);
        switch (columnIndex) {
            case 0: return book.getId();
            case 1: return book.getTitle();
            case 2: return book.getAuthor();
            case 3: return book.getStatus().getLabel();
            default: throw new IndexOutOfBoundsException("Unknown column " + columnIndex);
        }
    }

    /**
     * Provides the type used by table sorting and rendering.
     *
     * @param columnIndex column index
     * @return String.class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * Keeps the table read-only; edits occur in the dedicated form.
     *
     * @param rowIndex model row index
     * @param columnIndex model column index
     * @return false for all cells
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
