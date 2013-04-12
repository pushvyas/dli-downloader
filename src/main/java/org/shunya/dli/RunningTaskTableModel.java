package org.shunya.dli;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class RunningTaskTableModel extends AbstractTableModel implements DownloadObserver {
    public final int[] width = {100, 130, 32};
    private static final long serialVersionUID = 1L;
    private ArrayList<InteractiveTask> data = new ArrayList<InteractiveTask>();
    private String[] columnNames = new String[]
            {"<html><b>DLI Barcode", "<html><b>Progress", "<html><b>Status"};
    private Class[] columnClasses = new Class[]
            {String.class, Integer.class, RunState.class};

    public ArrayList<InteractiveTask> getModelData() {
        ArrayList<InteractiveTask> newdata = new ArrayList<InteractiveTask>(data);
        return newdata;
    }

    public ArrayList<InteractiveTask> getData() {
        return data;
    }

    public RunningTaskTableModel() {}

    public boolean addDownload(InteractiveTask task) {
        if (!data.contains(task)) {
            task.addObserver(this);
            insertRowAtBeginning(task);
            return true;
        }
        return false;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Overrides AbstractTableModel method.
     *
     * @param <b>rows </b> row number
     * @param <b>rows </b> column number
     * @return <b>Object</b> the value at the specified cell.
     */
    public Object getValueAt(int row, int col) {
        InteractiveTask task = data.get(row);
        switch (col) {
            case 0:
                return task.getName();
            case 1:
                return task.getProgress();
            case 2:
                return task.getState();
        }
        return "";
    }

    public Class<?> getColumnClass(int col) {
        return columnClasses[col];
    }


    public synchronized InteractiveTask insertRowAtBeginning(InteractiveTask newrow) {
        data.add(0, newrow);
        super.fireTableRowsInserted(0, 0);
        return data.get(0);
    }

    public synchronized InteractiveTask insertRow(InteractiveTask newrow) {
        data.add(newrow);
        super.fireTableRowsInserted(data.size() - 1, data.size() - 1);
        return data.get(data.size() - 1);
    }

    public synchronized void deleteRow(int row) {
        data.remove(row);
        super.fireTableDataChanged();
    }


    public synchronized void deleteRows(ArrayList<Object> rows) {
        data.removeAll(rows);
        super.fireTableDataChanged();
    }

    /**
     * Delete all the rows existing after the selected row from the JTable
     *
     * @param <b>row </b> row number
     */
    public void deleteAfterSelectedRow(int row) {
        // Get the initial size of the table before the deletion has started.
        int size = this.getRowCount();

        // The number of items to be deleted is got by subtracting the
        // selected row + 1 from size. This is done because as each row is deleted
        // the rows next to it are moved up by one space and the number of rows
        // in the JTable decreases. So the technique used here is always deleting
        // the row next to the selected row from the table n times so that all the
        // rows after the selected row are deleted.
        int n = size - (row + 1);
        for (int i = 1; i <= n; i++) {
            data.remove(row + 1);
        }
        super.fireTableDataChanged();
    }

    public InteractiveTask getRow(int row) {
        return data.get(row);
    }

    /**
     * Updates the specified row. It replaces the row ArrayList at the specified
     * row with the new ArrayList.
     *
     * @param <b>ArrayList </b> row data
     * @param <b>row       </b> row number
     */
    public void updateRow(InteractiveTask updatedRow, int row) {
        data.set(row, updatedRow);
        super.fireTableDataChanged();
    }

    public synchronized void refreshTable() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                super.fireTableCellUpdated(i, j);
            }
        }
    }

    /**
     * Clears the table data.
     */
    public void clearTable() {
        data = new ArrayList<InteractiveTask>();
        super.fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i = 0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++) {
                System.out.print("  " + data.get(i));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    @Override
    public void update(InteractiveTask task) {
        final int index = data.indexOf(task);
        //TODO might need to map to virtual index in case table sorting is enabled
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fireTableRowsUpdated(index, index);
            }
        });
    }
}
