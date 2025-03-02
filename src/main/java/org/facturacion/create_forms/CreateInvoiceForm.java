package org.facturacion.create_forms;

import org.facturacion.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formulario para la creación de facturas.
 */
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

    /**
     * Constructor que inicializa el formulario de creación de facturas.
     */
    public CreateInvoiceForm(JPanel parentPanel, ActionListener listener) {
        this.parentPanel = parentPanel;
        initializeDialog();
        loadComboBoxes();
        setDefaultDate();
        setupPaymentDateValidation();
        addComponents(listener);
        setVisible(true);
    }

    /**
     * Agrega los componentes al formulario.
     */
    private void addComponents(ActionListener listener) {
        JPanel formPanel = buildFormPanel();
        JPanel buttonPanel = buildButtonPanel(listener);
        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Construye el panel de botones con las acciones de Guardar y Cancelar.
     */
    private JPanel buildButtonPanel(ActionListener listener) {
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveInvoice(listener));

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Construye el panel del formulario con los campos de entrada.
     */
    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        baseAmountField.setEditable(false);
        totalAmountField.setEditable(false);
        paymentDateField.setEditable(false);

        addLabelAndField(formPanel, gbc, "Número Factura:", numberField, "Fecha (YYYY-MM-DD):", dateField, 0);
        addLabelAndField(formPanel, gbc, "Cliente:", clientCombo, "Trabajador:", workerCombo, 1);
        addLabelAndField(formPanel, gbc, "Base Imponible:", baseAmountField, "IVA:", ivaCombo, 2);
        addLabelAndField(formPanel, gbc, "Total:", totalAmountField, "Pagada:", isPaidCheckBox, 3);
        addLabelAndField(formPanel, gbc, "Forma de Pago:", paymentMethodCombo, "Fecha de Pago:", paymentDateField, 4);
        return formPanel;
    }

    /**
     * Configura la validación del campo de fecha de pago según si la factura está marcada como pagada.
     */
    private void setupPaymentDateValidation() {
        isPaidCheckBox.addActionListener(e -> {
            boolean isPaid = isPaidCheckBox.isSelected();
            paymentDateField.setEnabled(isPaid);
            if (!isPaid) {
                paymentDateField.setText("");
            }
        });
    }

    /**
     * Establece la fecha actual como valor predeterminado en el campo de fecha.
     */
    private void setDefaultDate() {
        dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
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

        /**
         * Carga las formas de pago desde la base de datos y las añade al JComboBox.
         */
        private void loadPaymentMethods() {
            String query = "SELECT idFormapago, tipoFormaPago FROM formapago ORDER BY tipoFormaPago";
            try (Connection conn = Utils.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paymentMethodCombo.addItem(new ComboItem(rs.getInt("idFormapago"), rs.getString("tipoFormaPago")));
                }
            } catch (SQLException e) {
                showError("formas de pago", e);
            }
        }

        /**
         * Carga los tipos de IVA desde la base de datos y los añade al JComboBox.
         */
        private void loadIvaTypes() {
            String query = "SELECT iva, observacionesTipoIva FROM tiposiva ORDER BY iva";
            try (Connection conn = Utils.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double iva = rs.getDouble("iva");
                    String obs = rs.getString("observacionesTipoIva");
                    ivaCombo.addItem(new ComboItem(iva, iva + "% - " + obs));
                }
            } catch (SQLException e) {
                showError("tipos de IVA", e);
            }
        }

        /**
         * Carga los trabajadores desde la base de datos y los añade al JComboBox.
         */
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

        /**
         * Carga los clientes desde la base de datos y los añade al JComboBox.
         */
        private void loadClients() {
            String query = "SELECT idCliente, nombreCliente FROM clientes ORDER BY nombreCliente";
            try (Connection conn = Utils.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientCombo.addItem(new ComboItem(rs.getInt("idCliente"), rs.getString("nombreCliente")));
                }
            } catch (SQLException e) {
                showError("clientes", e);
            }
        }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc,
                                  String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(label1), gbc);
        gbc.gridx = 1;
        panel.add(comp1, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel(label2), gbc);
        gbc.gridx = 3;
        panel.add(comp2, gbc);
    }

    private void showError(String entity, Exception e) {
        JOptionPane.showMessageDialog(this, "Error al cargar " + entity + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void saveInvoice(ActionListener listener) {
        if (!validatePaymentDate()) return;
        String insertInvoice = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, " +
                "idClienteFactura, idTrabajadorFactura, baseImponibleFacturaCliente, ivaFacturaCliente, " +
                "totalFacturaCliente, cobradaFactura, formaCobroFactura, fechaCobroFactura) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertInvoice)) {
            ps.setInt(1, Integer.parseInt(numberField.getText()));
            ps.setDate(2, java.sql.Date.valueOf(dateField.getText()));
            ps.setInt(3, ((ComboItem) clientCombo.getSelectedItem()).getId());
            ps.setInt(4, ((ComboItem) workerCombo.getSelectedItem()).getId());
            ps.setDouble(5, Double.parseDouble(baseAmountField.getText().replace(",", ".")));
            ps.setDouble(6, ((ComboItem) ivaCombo.getSelectedItem()).getNumeric());
            ps.setDouble(7, Double.parseDouble(totalAmountField.getText().replace(",", ".")));
            ps.setBoolean(8, isPaidCheckBox.isSelected());
            ps.setInt(9, ((ComboItem) paymentMethodCombo.getSelectedItem()).getId());
            ps.setDate(10, paymentDateField.getText().isEmpty() ? null : java.sql.Date.valueOf(paymentDateField.getText()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Factura creada con éxito.");
            dispose();
        } catch (SQLException | NumberFormatException e) {
            showError("factura", e);
        }
    }

    /**
     * Valida la fecha de pago de la factura.
     *
     * @return true si la fecha de pago es válida, false en caso contrario.
     */
    private boolean validatePaymentDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date invoiceDate = sdf.parse(dateField.getText().trim());
            String paymentDateText = paymentDateField.getText().trim();

            if (isPaidCheckBox.isSelected()) {
                if (paymentDateText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe ingresar una fecha de pago si la factura está marcada como pagada.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                Date paymentDate = sdf.parse(paymentDateText);
                if (!paymentDate.after(invoiceDate)) {
                    JOptionPane.showMessageDialog(this, "La fecha de pago debe ser posterior a la fecha de la factura.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}