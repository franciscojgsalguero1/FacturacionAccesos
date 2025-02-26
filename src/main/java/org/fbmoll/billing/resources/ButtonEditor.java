package org.fbmoll.billing.resources;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ButtonEditor<T> extends AbstractCellEditor implements TableCellEditor {
    private final JButton button;
    private final List<T> items;
    private T item;

    public ButtonEditor(ActionListener listener, List<T> items, String actionCommand) {
        this.items = items;

        this.button = new JButton();
        this.button.setOpaque(true);

        this.button.addActionListener(e -> {
            if (item != null) {
                stopCellEditing();
                listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, actionCommand));
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof JButton btn) {
            button.setText(btn.getText());
        }
        item = items.get(table.convertRowIndexToModel(row));
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return super.stopCellEditing();
    }
}