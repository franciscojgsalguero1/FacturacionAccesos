package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.classes.CorrectiveInvoice;
import org.fbmoll.billing.resources.Utils;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Getter
public class ViewCorrectiveInvoices {
    public static void showCorrectiveInvoiceTable(JPanel panel) throws SQLException {
        ArrayList<CorrectiveInvoice> correctiveInvoices = queryGetCorrectiveInvoices();

        String[] columnNames = {
                "ID", "Número de Rectificativa", "Fecha de Rectificativa", "ID Cliente", "Base Imponible", "Cantidad IVA", "Total",
                "Hash de Rectificativa", "Código QR", "Observaciones de Rectificativa"
        };

        Object[][] data = new Object[correctiveInvoices.size()][columnNames.length];
        for (int i = 0; i < correctiveInvoices.size(); i++) {
            CorrectiveInvoice correctiveInvoice = correctiveInvoices.get(i);
            data[i] = new Object[]{
                    correctiveInvoice.getId(),
                    correctiveInvoice.getNumber(),
                    correctiveInvoice.getDate(),
                    correctiveInvoice.getClientId(),
                    correctiveInvoice.getTaxableAmount(),
                    correctiveInvoice.getVatAmount(),
                    correctiveInvoice.getTotalAmount(),
                    correctiveInvoice.hashCode(),
                    correctiveInvoice.getQrCode(),
                    correctiveInvoice.getNotes()
            };
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

    private static ArrayList<CorrectiveInvoice> queryGetCorrectiveInvoices() throws SQLException {
        ArrayList<CorrectiveInvoice> correctiveInvoices = new ArrayList<>();
        String query = "SELECT * FROM rectificativasclientes";

        try (Connection conn = Utils.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int correctiveInvoiceId = resultSet.getInt("idRectificativaCliente");
                    int correctiveInvoiceNumber = resultSet.getInt("numeroRectificativaCliente");
                    Date correctiveInvoiceDate = resultSet.getDate("fechaRectificativaCliente");
                    int clientId = resultSet.getInt("idClienteRectificativaCliente");
                    double taxableAmount = resultSet.getDouble("baseImponibleRectificativaCliente");
                    double vatAmount = resultSet.getDouble("ivaRectificativaCliente");
                    double totalAmount = resultSet.getDouble("totalRectificativaCliente");
                    String correctiveInvoiceHash = resultSet.getString("hashRectificativaCliente");
                    String correctiveInvoiceQrCode = resultSet.getString("qrRectificativaCliente");
                    String correctiveInvoiceNotes = resultSet.getString("observacionesRectificativaCliente");

                    correctiveInvoices.add(new CorrectiveInvoice(correctiveInvoiceId, correctiveInvoiceNumber, correctiveInvoiceDate,
                            clientId, taxableAmount, vatAmount, totalAmount, correctiveInvoiceHash,
                            correctiveInvoiceQrCode, correctiveInvoiceNotes));
                }
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
            }
        }

        return correctiveInvoices;
    }

    private static JPanel createFilterPanel(JPanel panel, String[] columns, DefaultTableModel model, JTable table) {
        JTextField filterField = new JTextField(20);
        JComboBox<String> columnSelector = new JComboBox<>(getFilterableColumns(columns));
        JLabel filterLabel = new JLabel("Filter:");

        // Add the "Create Corrective Invoice" button
        JButton createCorrectiveInvoiceButton = new JButton("Create Corrective Invoice");
        createCorrectiveInvoiceButton.addActionListener(e -> {
            // Trigger the Corrective Invoice creation process
            new CreateCorrectiveInvoice().createNewCorrectiveInvoice(panel);
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
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

        return styleFilterPanel(filterLabel, filterField, columnSelector, createCorrectiveInvoiceButton);
    }

    private static JPanel styleFilterPanel(JLabel filterLabel, JTextField filterField,
                                           JComboBox<String> columnSelector, JButton createInvoiceButton) {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(createInvoiceButton, gbc);  // Create Invoice button added here

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