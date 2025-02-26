package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateInvoiceForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField numberField = new JTextField(20);
    private final JTextField dateField = new JTextField(20);
    private final JTextField clientIdField = new JTextField(20);
    private final JTextField workerIdField = new JTextField(20);
    private final JTextField baseAmountField = new JTextField(20); // Non-editable
    private final JTextField vatAmountField = new JTextField(20);
    private final JTextField totalAmountField = new JTextField(20); // Non-editable
    private final JCheckBox isPaidCheckBox = new JCheckBox("Pagada");
    private final JTextField paymentMethodField = new JTextField(20);
    private final JTextField paymentDateField = new JTextField(20);
    private final JTable itemsTable;
    private final DefaultTableModel tableModel;

    public CreateInvoiceForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Factura");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.add(new JLabel("Número Factura:"));
        formPanel.add(numberField);
        formPanel.add(new JLabel("Fecha (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("ID Cliente:"));
        formPanel.add(clientIdField);
        formPanel.add(new JLabel("ID Trabajador:"));
        formPanel.add(workerIdField);
        formPanel.add(new JLabel("Base Imponible:"));
        baseAmountField.setEditable(false); // Make non-editable
        formPanel.add(baseAmountField);
        formPanel.add(new JLabel("IVA (%):"));
        formPanel.add(vatAmountField);
        formPanel.add(new JLabel("Total:"));
        totalAmountField.setEditable(false); // Make non-editable
        formPanel.add(totalAmountField);
        formPanel.add(new JLabel("Pagada:"));
        formPanel.add(isPaidCheckBox);
        formPanel.add(new JLabel("Forma de Pago:"));
        formPanel.add(paymentMethodField);
        formPanel.add(new JLabel("Fecha de Pago:"));
        formPanel.add(paymentDateField);

        tableModel = new DefaultTableModel(new String[]{"ID", "Código", "Nombre", "Precio", "Stock", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        itemsTable = new JTable(tableModel);
        loadItems();
        JScrollPane tableScroll = new JScrollPane(itemsTable);

        // Add table listener to update totals
        tableModel.addTableModelListener(e -> updateTotals());

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveInvoice());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void saveInvoice() {
        try (Connection conn = Utils.getConnection()) {
            String insertInvoice = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, idClienteFactura, " +
                    "idTrabajadorFactura, baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, " +
                    "cobradaFactura, formaCobroFactura, fechaCobroFactura) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertInvoice, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, Integer.parseInt(numberField.getText()));
                ps.setDate(2, java.sql.Date.valueOf(dateField.getText()));
                ps.setInt(3, Integer.parseInt(clientIdField.getText()));
                ps.setInt(4, Integer.parseInt(workerIdField.getText()));
                ps.setDouble(5, Double.parseDouble(baseAmountField.getText()));
                ps.setDouble(6, Double.parseDouble(vatAmountField.getText()));
                ps.setDouble(7, Double.parseDouble(totalAmountField.getText()));
                ps.setBoolean(8, isPaidCheckBox.isSelected());
                ps.setString(9, paymentMethodField.getText());
                ps.setDate(10, paymentDateField.getText().isEmpty() ? null : java.sql.Date.valueOf(paymentDateField.getText()));

                ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int invoiceId = generatedKeys.getInt(1);
                    saveInvoiceLines(invoiceId, conn);
                }
                JOptionPane.showMessageDialog(this, "Factura creada con éxito.");
                dispose();
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveInvoiceLines(int invoiceId, Connection conn) {
        String insertLine = "INSERT INTO lineasfacturasclientes (numeroFacturaCliente, idArticulo, cantidad," +
                " pvpArticulo, iva) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertLine)) {
            ps.setInt(1, invoiceId);

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                try {
                    // Ensure all values are valid before inserting
                    int cantidad = Integer.parseInt(tableModel.getValueAt(i, 5).toString().trim());
                    if (cantidad > 0) {
                        int idArticulo = Integer.parseInt(tableModel.getValueAt(i, 1).toString().trim());
                        double precio = Double.parseDouble(tableModel.getValueAt(i, 4).toString().trim());

                        ps.setInt(2, idArticulo);
                        ps.setInt(3, cantidad);
                        ps.setDouble(4, precio);
                        ps.setInt(5, Integer.parseInt(vatAmountField.getText()));
                        ps.addBatch();

                        System.out.println("Added to batch: " + idArticulo + ", " + cantidad + ", " + precio);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing row " + i + ": " + e.getMessage());
                }
            }
            int[] affectedRows = ps.executeBatch();
            System.out.println("Inserted rows: " + affectedRows.length);
        } catch (SQLException e) {
            System.err.println("Error saving invoice lines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadItems() {
        String query = "SELECT idArticulo, codigoArticulo, descripcionArticulo, pvpArticulo, stockArticulo FROM articulos";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("idArticulo"),
                        rs.getString("codigoArticulo"),
                        rs.getString("descripcionArticulo"),
                        rs.getDouble("pvpArticulo"),
                        rs.getInt("stockArticulo"),
                        0
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los artículos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotals() {
        double baseImponible = 0;
        double iva = Double.parseDouble(vatAmountField.getText());

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double price = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
            double stock = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
            double quantity = Double.parseDouble(tableModel.getValueAt(i, 5).toString());

            if (quantity > 0 && stock >= quantity) {
                baseImponible += price * quantity;
            }
        }

        double total = baseImponible + (baseImponible * iva / 100);
        baseAmountField.setText(String.format("%.2f", baseImponible));
        totalAmountField.setText(String.format("%.2f", total));
    }
}