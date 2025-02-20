package org.fbmoll.billing.resources;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ButtonEditor<T> extends AbstractCellEditor implements TableCellEditor {
    private final JButton button;
    private final ActionListener listener;
    private final List<T> items;
    private final JPanel panel;
    private final String actionCommand;
    private T item; // Generic type for any object

    public ButtonEditor(JCheckBox checkBox, ActionListener listener, List<T> items, JPanel panel, String actionCommand) {
        this.listener = listener;
        this.items = items;
        this.panel = panel;
        this.actionCommand = actionCommand;

        this.button = new JButton();
        this.button.setOpaque(true);

        this.button.addActionListener(e -> {
            if (item != null) {
                stopCellEditing(); // Stops editing so action can be handled properly
                listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, actionCommand));
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof JButton) {
            button.setText(((JButton) value).getText());
        }
        item = items.get(table.convertRowIndexToModel(row)); // Ensures correct row selection
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped(); // Notify JTable that editing has stopped
        return super.stopCellEditing();
    }
}