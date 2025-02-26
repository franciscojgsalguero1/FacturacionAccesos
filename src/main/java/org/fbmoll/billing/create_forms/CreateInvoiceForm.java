package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CreateInvoiceForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField numberField = new JTextField(20);
    private final JTextField dateField = new JTextField(20);
    private final JComboBox<ComboItem> clientCombo = new JComboBox<>();
    private final JComboBox<ComboItem> workerCombo = new JComboBox<>();
    private final JTextField baseAmountField = new JTextField(20);
    private final JComboBox<ComboItem> ivaCombo = new JComboBox<>();
    private final JTextField totalAmountField = new JTextField(20);
    private final JCheckBox isPaidCheckBox = new JCheckBox("Pagada");
    private final JComboBox<ComboItem> paymentMethodCombo = new JComboBox<>();
    private final JTextField paymentDateField = new JTextField(20);
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    public CreateInvoiceForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        initializeDialog();
        loadComboBoxes();
        JPanel formPanel = buildFormPanel();
        JScrollPane tableScroll = new JScrollPane(itemsTable);
        JPanel buttonPanel = buildButtonPanel();
        add(formPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void initializeDialog() {
        setTitle("Crear Factura");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);
    }

    private void loadComboBoxes() {
        loadClients();
        loadWorkers();
        loadIvaTypes();
        loadPaymentMethods();
    }

    private JPanel buildFormPanel() {

        JPanel formPanel = new JPanel(new GridBagLayout());

        baseAmountField.setEditable(false);
        totalAmountField.setEditable(false);

        setTitle("Crear Factura");
        setSize(1000, 650);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        Object[][] rows = {
                {"Número Factura:", numberField, "Fecha (YYYY-MM-DD):", dateField},
                {"Cliente:", clientCombo, "Trabajador:", workerCombo},
                {"Base Imponible:", baseAmountField, "IVA:", ivaCombo},
                {"Total:", totalAmountField, "Pagada:", isPaidCheckBox},
                {"Forma de Pago:", paymentMethodCombo, "Fecha de Pago:", paymentDateField}
        };

        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Código", "Nombre", "Precio", "Stock", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        itemsTable = new JTable(tableModel);
        loadItems();
        tableModel.addTableModelListener(e -> updateTotals());
        ivaCombo.addActionListener(e -> updateTotals());

        return formPanel;
    }

    private JPanel buildButtonPanel() {
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveInvoice());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void loadClients() {
        String query = "SELECT idCliente, nombreCliente FROM clientes ORDER BY nombreCliente";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientCombo.addItem(new ComboItem(rs.getInt("idCliente"),
                        rs.getString("nombreCliente")));
            }
        } catch (SQLException e) {
            showError("clientes", e);
        }
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        panel.add(new JLabel(label1), gbc);

        gbc.gridx = 1;
        panel.add(comp1, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel(label2), gbc);

        gbc.gridx = 3;
        panel.add(comp2, gbc);
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
            showError("trabajadores", e);
        }
    }

    private void loadIvaTypes() {
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
            showError("tipos de IVA", e);
        }
    }

    private void loadPaymentMethods() {
        String query = "SELECT idFormapago, tipoFormaPago FROM formapago ORDER BY tipoFormaPago";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                paymentMethodCombo.addItem(new ComboItem(rs.getInt("idFormapago"),
                        rs.getString("tipoFormaPago")));
            }
        } catch (SQLException e) {
            showError("formas de pago", e);
        }
    }

    private void loadItems() {
        String query = "SELECT idArticulo, codigoArticulo, descripcionArticulo, " +
                "pvpArticulo, stockArticulo FROM articulos";
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
            showError("los artículos", e);
        }
    }

    private void updateTotals() {
        double baseImponible = 0;
        double ivaPercentage = 0;
        ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
        if (ivaItem != null) {
            ivaPercentage = ivaItem.getNumeric();
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double price = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
            int stock = Integer.parseInt(tableModel.getValueAt(i, 4).toString());
            int quantity = Integer.parseInt(tableModel.getValueAt(i, 5).toString());

            if (quantity > stock) {
                JOptionPane.showMessageDialog(this,
                        "La cantidad no puede ser superior al stock en la fila " + (i + 1),
                        "Error de Stock", JOptionPane.ERROR_MESSAGE);
                tableModel.setValueAt(stock, i, 5);
                return;
            }

            if (quantity > 0) {
                baseImponible += price * quantity;
            }
        }

        double total = baseImponible + (baseImponible * ivaPercentage / 100);
        baseAmountField.setText(String.format("%.2f", baseImponible));
        totalAmountField.setText(String.format("%.2f", total));
    }

    private void saveInvoice() {
        String insertInvoice = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, " +
                "idClienteFactura, idTrabajadorFactura, baseImponibleFacturaCliente, ivaFacturaCliente, " +
                "totalFacturaCliente, cobradaFactura, formaCobroFactura, fechaCobroFactura)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertInvoice, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, Integer.parseInt(numberField.getText()));
            ps.setDate(2, java.sql.Date.valueOf(dateField.getText()));

            ComboItem clientItem = (ComboItem) clientCombo.getSelectedItem();
            ps.setInt(3, clientItem != null ? clientItem.getId() : 0);

            ComboItem workerItem = (ComboItem) workerCombo.getSelectedItem();
            ps.setInt(4, workerItem != null ? workerItem.getId() : 0);

            ps.setDouble(5, Double.parseDouble(baseAmountField.getText()));

            ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
            ps.setDouble(6, ivaItem != null ? ivaItem.getNumeric() : 0);

            ps.setDouble(7, Double.parseDouble(totalAmountField.getText()));
            ps.setBoolean(8, isPaidCheckBox.isSelected());

            ComboItem pmItem = (ComboItem) paymentMethodCombo.getSelectedItem();
            ps.setInt(9, pmItem != null ? pmItem.getId() : 0);

            ps.setDate(10, paymentDateField.getText().isEmpty() ? null :
                    java.sql.Date.valueOf(paymentDateField.getText()));

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int invoiceId = generatedKeys.getInt(1);
                    saveInvoiceLines(invoiceId, conn);
                }
            }
            JOptionPane.showMessageDialog(this, "Factura creada con éxito.");
            dispose();
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveInvoiceLines(int invoiceId, Connection conn) {
        String insertLine = "INSERT INTO lineasfacturasclientes (numeroFacturaCliente, idArticulo," +
                " cantidad, pvpArticulo, iva) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertLine)) {
            ps.setInt(1, invoiceId);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int cantidad = Integer.parseInt(tableModel.getValueAt(i, 5).toString().trim());
                int stock = Integer.parseInt(tableModel.getValueAt(i, 4).toString().trim());
                if (cantidad > 0) {
                    if (cantidad > stock) {
                        JOptionPane.showMessageDialog(this,
                                "La cantidad del artículo con ID " + tableModel.getValueAt(i, 0) +
                                        " no puede ser superior al stock disponible.",
                                "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int idArticulo = Integer.parseInt(tableModel.getValueAt(i, 0).toString().trim());
                    double precio = Double.parseDouble(tableModel.getValueAt(i, 3).toString().trim());
                    ps.setInt(2, idArticulo);
                    ps.setInt(3, cantidad);
                    ps.setDouble(4, precio);
                    ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
                    ps.setDouble(5, ivaItem != null ? ivaItem.getNumeric() : 0);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showError(String entity, Exception e) {
        JOptionPane.showMessageDialog(this,
                "Error al cargar " + entity + ": " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class ComboItem {
        private final int id;
        private final String display;
        private final double numeric;

        public ComboItem(int id, String display) {
            this.id = id;
            this.display = display;
            this.numeric = 0;
        }

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
}