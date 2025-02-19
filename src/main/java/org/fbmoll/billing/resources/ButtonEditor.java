package org.fbmoll.billing.resources;

import org.fbmoll.billing.data_classes.Client;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
    private final JButton button;
    private final ActionListener listener;
    private Client client;
    private final List<Client> clients;
    private final JPanel panel;
    private final String actionCommand;

    public ButtonEditor(JCheckBox checkBox, ActionListener listener, List<Client> clients, JPanel panel, String actionCommand) {
        this.listener = listener;
        this.clients = clients;
        this.panel = panel;
        this.actionCommand = actionCommand;

        this.button = new JButton();
        this.button.setOpaque(true);

        this.button.addActionListener(e -> {
            if (client != null) {
                stopCellEditing(); // Ensures JTable registers the button click
                System.out.println("Button clicked: " + actionCommand + " for client: " + client.getName());
                listener.actionPerformed(new ActionEvent(client, ActionEvent.ACTION_PERFORMED, actionCommand));
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof JButton) {
            button.setText(((JButton) value).getText());
        }
        client = clients.get(row); // Ensures correct client is selected
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