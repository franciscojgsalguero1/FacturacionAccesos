package org.fbmoll.billing.resources;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof JButton btn) {
            this.setText(btn.getText());
        }

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(UIManager.getColor("Button.background"));
        }
        return this;
    }
}