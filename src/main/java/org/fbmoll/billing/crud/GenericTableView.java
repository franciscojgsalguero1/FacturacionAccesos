package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.resources.Constants;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.lang.reflect.Field;

@Getter
public class GenericTableView {
    private GenericTableView() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> void showTable(JPanel panel, List<T> dataList, String title) {
        if (dataList.isEmpty()) {
            JOptionPane.showMessageDialog(panel, Constants.ERROR_LACK_DATA + title);
            return;
        }

        Field[] fields = dataList.get(0).getClass().getDeclaredFields();
        String[] columnNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            columnNames[i] = fields[i].getName();
        }

        Object[][] data = new Object[dataList.size()][fields.length];
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = 0; j < fields.length; j++) {
                try {
                    data[i][j] = fields[j].get(dataList.get(i));
                } catch (IllegalAccessException e) {
                    data[i][j] = null;
                }
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(tableModel);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        JPanel filterPanel = createFilterPanel(panel, title, tableModel, table, columnNames);

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(filterPanel, BorderLayout.NORTH);
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    private static JPanel createFilterPanel(JPanel panel, String title, DefaultTableModel model, JTable table, String[] columns) {
        JTextField filterField = new JTextField(20);
        JComboBox<String> columnSelector = new JComboBox<>(columns);
        JLabel filterLabel = new JLabel(Constants.LABEL_FILTER);

        JButton createButton = new JButton(Constants.BUTTON_CREATE + title);
        createButton.addActionListener(e -> new GenericCreateForm(title, panel));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortable(0, false);
        table.setRowSorter(sorter);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }

            private void applyFilter() {
                String text = filterField.getText();
                int columnIndex = columnSelector.getSelectedIndex();

                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
                }

                table.repaint();
                table.revalidate();
            }
        });

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(createButton);
        filterPanel.add(filterLabel);
        filterPanel.add(filterField);
        filterPanel.add(columnSelector);
        return filterPanel;
    }
}