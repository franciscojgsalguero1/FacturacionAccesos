package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.dataClasses.Client;
import org.fbmoll.billing.dataClasses.Provider;
import org.fbmoll.billing.resources.Queries;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

@Getter
public class ViewProviders {
    private ViewProviders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void showProviderTable(JPanel panel) throws SQLException {
        List<Provider> providers = Queries.queryGetProviders();

        String[] columnNames = {
                "ID", "Nombre", "Dirección", "Código Postal", "Población", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web", "Observaciones"
        };

        Object[][] data = new Object[providers.size()][columnNames.length];
        for (int i = 0; i < providers.size(); i++) {
            Field[] declaredFields = Client.class.getDeclaredFields();

            for (int j = 0; j < declaredFields.length; j++) {
                try {
                    data[i][j] = declaredFields[j].get(providers.get(i));
                } catch (IllegalAccessException e) {
                    data[i][j] = null;
                }
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(tableModel);
        JScrollPane pane = new JScrollPane(Utils.resizeTableColumns(table));
        JPanel filterPanel = createFilterPanel(panel, columnNames, tableModel, table);

        // Event Dispatcher Thread
        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(filterPanel, BorderLayout.NORTH);
            panel.add(pane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    private static JPanel createFilterPanel(JPanel panel, String[] columnNames, DefaultTableModel tableModel, JTable table) {
        JTextField filterField = new JTextField(20);
        JComboBox<String> columnSelector = new JComboBox<>(getFilterableColumns(columnNames));
        JLabel filterLabel = new JLabel("Filter:");

        JButton createProviderButton = new JButton("Crear Proveedor");
        createProviderButton.addActionListener(e -> new CreateProvider().createProvider(panel));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortable(0, false);
        table.setRowSorter(sorter);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = filterField.getText();
                int columnIndex = columnSelector.getSelectedIndex() + 1;

                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
                }

                table.repaint();
                table.revalidate();
            }
        });

        return styleFilterPanel(filterLabel, filterField, columnSelector, createProviderButton);
    }

    private static JPanel styleFilterPanel(JLabel filterLabel, JTextField filterField,
                                           JComboBox<String> columnSelector, JButton button) {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(button, gbc);

        gbc.gridx = 2;
        filterPanel.add(filterLabel, gbc);

        gbc.gridx = 3;
        filterPanel.add(filterField, gbc);

        gbc.gridx = 4;
        filterPanel.add(columnSelector, gbc);

        return filterPanel;
    }

    private static String[] getFilterableColumns(String[] columnNames) {
        String[] filterableColumns = new String[columnNames.length - 1];
        System.arraycopy(columnNames, 1, filterableColumns, 0, columnNames.length - 1);
        return filterableColumns;
    }
}