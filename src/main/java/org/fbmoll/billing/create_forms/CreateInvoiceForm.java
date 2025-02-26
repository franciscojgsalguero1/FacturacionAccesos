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
    // Replace clientIdField with clientCombo
    private final JComboBox<ComboItem> clientCombo = new JComboBox<>();
    // Replace workerIdField with workerCombo
    private final JComboBox<ComboItem> workerCombo = new JComboBox<>();
    private final JTextField baseAmountField = new JTextField(20); // Non-editable
    // Replace vatAmountField with ivaCombo
    private final JComboBox<ComboItem> ivaCombo = new JComboBox<>();
    private final JTextField totalAmountField = new JTextField(20); // Non-editable
    private final JCheckBox isPaidCheckBox = new JCheckBox("Pagada");
    // Replace paymentMethodField with paymentMethodCombo
    private final JComboBox<ComboItem> paymentMethodCombo = new JComboBox<>();
    private final JTextField paymentDateField = new JTextField(20);
    private final JTable itemsTable;
    private final DefaultTableModel tableModel;

    // Helper inner class to hold combo items
    private static class ComboItem {
        private final int id;
        private final String display;
        private final double numeric; // used for IVA if needed

        // Constructor for items that only need an integer id and display string
        public ComboItem(int id, String display) {
            this.id = id;
            this.display = display;
            this.numeric = 0;
        }

        // Constructor for IVA items where the numeric value is important
        public ComboItem(double numeric, String display) {
            this.id = 0;
            this.numeric = numeric;
            this.display = display;
        }

        public int getId() {
            return id;
        }

        public double getNumeric() {
            return numeric;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    public CreateInvoiceForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Factura");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Load combo boxes
        loadClients();
        loadWorkers();
        loadIvaTypes();
        loadPaymentMethods();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.add(new JLabel("Número Factura:"));
        formPanel.add(numberField);
        formPanel.add(new JLabel("Fecha (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Cliente:"));
        formPanel.add(clientCombo);
        formPanel.add(new JLabel("Trabajador:"));
        formPanel.add(workerCombo);
        formPanel.add(new JLabel("Base Imponible:"));
        baseAmountField.setEditable(false); // Make non-editable
        formPanel.add(baseAmountField);
        formPanel.add(new JLabel("IVA:"));
        formPanel.add(ivaCombo);
        formPanel.add(new JLabel("Total:"));
        totalAmountField.setEditable(false); // Make non-editable
        formPanel.add(totalAmountField);
        formPanel.add(new JLabel("Pagada:"));
        formPanel.add(isPaidCheckBox);
        formPanel.add(new JLabel("Forma de Pago:"));
        formPanel.add(paymentMethodCombo);
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

    private void loadClients() {
        String query = "SELECT idCliente, nombreCliente FROM clientes ORDER BY nombreCliente";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientCombo.addItem(new ComboItem(rs.getInt("idCliente"), rs.getString("nombreCliente")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWorkers() {
        String query = "SELECT id, name FROM workers ORDER BY name";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                workerCombo.addItem(new ComboItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar trabajadores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadIvaTypes() {
        // Assumes table tiposiva has fields: iva (numeric) and observacionesTipoIva (string)
        String query = "SELECT iva, observacionesTipoIva FROM tiposiva ORDER BY iva";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double iva = rs.getDouble("iva");
                String obs = rs.getString("observacionesTipoIva");
                String display = iva + "% - " + obs;
                ivaCombo.addItem(new ComboItem(iva, display));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de IVA: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentMethods() {
        // Assumes table formapago has fields: idFormapago and tipoFormaPago
        String query = "SELECT idFormapago, tipoFormaPago FROM formapago ORDER BY tipoFormaPago";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                paymentMethodCombo.addItem(new ComboItem(rs.getInt("idFormapago"), rs.getString("tipoFormaPago")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar formas de pago: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        // Get IVA from the selected item in ivaCombo
        double iva = 0;
        ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
        if (ivaItem != null) {
            iva = ivaItem.getNumeric();
        }

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

    private void saveInvoice() {
        try (Connection conn = Utils.getConnection()) {
            String insertInvoice = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, idClienteFactura, " +
                    "idTrabajadorFactura, baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, " +
                    "cobradaFactura, formaCobroFactura, fechaCobroFactura) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertInvoice, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, Integer.parseInt(numberField.getText()));
                ps.setDate(2, java.sql.Date.valueOf(dateField.getText()));
                // Get client ID from selected item
                ComboItem clientItem = (ComboItem) clientCombo.getSelectedItem();
                ps.setInt(3, clientItem != null ? clientItem.getId() : 0);
                // Get worker ID from selected item
                ComboItem workerItem = (ComboItem) workerCombo.getSelectedItem();
                ps.setInt(4, workerItem != null ? workerItem.getId() : 0);
                ps.setDouble(5, Double.parseDouble(baseAmountField.getText()));
                // Get IVA from selected item
                ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
                ps.setDouble(6, ivaItem != null ? ivaItem.getNumeric() : 0);
                ps.setDouble(7, Double.parseDouble(totalAmountField.getText()));
                ps.setBoolean(8, isPaidCheckBox.isSelected());
                // Get payment method from selected item
                ComboItem pmItem = (ComboItem) paymentMethodCombo.getSelectedItem();
                ps.setInt(9, pmItem != null ? pmItem.getId() : 0);
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
                    int cantidad = Integer.parseInt(tableModel.getValueAt(i, 5).toString().trim());
                    if (cantidad > 0) {
                        int idArticulo = Integer.parseInt(tableModel.getValueAt(i, 1).toString().trim());
                        double precio = Double.parseDouble(tableModel.getValueAt(i, 3).toString().trim());
                        ps.setInt(2, idArticulo);
                        ps.setInt(3, cantidad);
                        ps.setDouble(4, precio);
                        // Use IVA from the invoice header for each line
                        ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
                        ps.setDouble(5, ivaItem != null ? ivaItem.getNumeric() : 0);
                        ps.addBatch();
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
}