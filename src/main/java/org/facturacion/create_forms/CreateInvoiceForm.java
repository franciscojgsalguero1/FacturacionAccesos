package org.facturacion.create_forms;

import org.facturacion.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
    // Panel donde se actualizará la tabla de facturas después de la creación.
    private final JPanel parentPanel;

    // Campos de entrada de datos para la factura.
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

    // Tabla para seleccionar los artículos de la factura.
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    /**
     * Constructor de la clase. Inicializa el formulario de creación de facturas.
     *
     * @param parentPanel Panel donde se mostrará la tabla de facturas después de la creación.
     * @param listener    Listener para manejar eventos después de guardar la factura.
     */
    public CreateInvoiceForm(JPanel parentPanel, ActionListener listener) {
        this.parentPanel = parentPanel;
        initializeDialog();
        loadComboBoxes();
        setDefaultDate();
        setupPaymentDateValidation();
        JPanel formPanel = buildFormPanel();
        JScrollPane tableScroll = new JScrollPane(itemsTable);
        JPanel buttonPanel = buildButtonPanel(listener);
        add(formPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     * Configura las propiedades del cuadro de diálogo.
     */
    private void initializeDialog() {
        setTitle("Crear Factura");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);
    }

    /**
     * Carga los datos de los clientes, trabajadores, IVA y formas de pago en los JComboBox.
     */
    private void loadComboBoxes() {
        loadClients();
        loadWorkers();
        loadIvaTypes();
        loadPaymentMethods();
    }

    /**
     * Establece la fecha actual como valor predeterminado en el campo de fecha.
     */
    private void setDefaultDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(sdf.format(new Date()));
    }

    /**
     * Configura la validación del campo de fecha de pago según si la factura está marcada como pagada.
     */
    private void setupPaymentDateValidation() {
        isPaidCheckBox.addActionListener(e -> {
            if (isPaidCheckBox.isSelected()) {
                paymentDateField.setEnabled(true);
            } else {
                paymentDateField.setEnabled(false);
                paymentDateField.setText("");
            }
        });
    }

    /**
     * Construye el panel del formulario con los campos de entrada.
     *
     * @return Panel con el formulario.
     */
    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        // Configurar tamaños de los campos
        numberField.setPreferredSize(new Dimension(150, 25));
        dateField.setPreferredSize(new Dimension(150, 25));
        baseAmountField.setPreferredSize(new Dimension(150, 25));
        totalAmountField.setPreferredSize(new Dimension(150, 25));
        paymentDateField.setPreferredSize(new Dimension(150, 25));

        // Deshabilitar campos calculados
        baseAmountField.setEditable(false);
        totalAmountField.setEditable(false);
        paymentDateField.setEditable(false);

        // Agregar los campos al formulario
        addLabelAndField(formPanel, gbc, "Número Factura:", numberField, "Fecha (YYYY-MM-DD):", dateField, 0);
        addLabelAndField(formPanel, gbc, "Cliente:", clientCombo, "Trabajador:", workerCombo, 1);
        addLabelAndField(formPanel, gbc, "Base Imponible:", baseAmountField, "IVA:", ivaCombo, 2);
        addLabelAndField(formPanel, gbc, "Total:", totalAmountField, "Pagada:", isPaidCheckBox, 3);
        addLabelAndField(formPanel, gbc, "Forma de Pago:", paymentMethodCombo, "Fecha de Pago:", paymentDateField, 4);

        return formPanel;
    }

    /**
     * Agrega un par de etiquetas y campos al formulario en una fila específica.
     */
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

    /**
     * Crea el panel de botones de acción (Guardar y Cancelar).
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
     * Valida la fecha de pago de la factura.
     *
     * @return true si la fecha de pago es válida, false en caso contrario.
     */
    private boolean validatePaymentDate() {
        try {
            // Formato de fecha esperado
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // Obtener la fecha de la factura
            Date invoiceDate = sdf.parse(dateField.getText());
            String paymentDateText = paymentDateField.getText().trim();

            // Si la factura no está marcada como pagada pero se ha ingresado una fecha de pago
            if (!isPaidCheckBox.isSelected() && !paymentDateText.isEmpty()) {
                Date paymentDate = sdf.parse(paymentDateText);

                // La fecha de pago debe ser posterior a la fecha de la factura
                if (!paymentDate.after(invoiceDate)) {
                    JOptionPane.showMessageDialog(this,
                            "La fecha de pago debe ser posterior a la fecha de la factura.",
                            "Error de Fecha", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } catch (ParseException e) {
            // Si la fecha no tiene el formato correcto, mostrar mensaje de error
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha incorrecto. Use YYYY-MM-DD.",
                    "Error de Fecha", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
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
                // Agregar cada cliente al JComboBox
                clientCombo.addItem(new ComboItem(rs.getInt("idCliente"), rs.getString("nombreCliente")));
            }
        } catch (SQLException e) {
            showError("clientes", e);
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
                // Agregar cada trabajador al JComboBox
                workerCombo.addItem(new ComboItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            showError("trabajadores", e);
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

                // Agregar cada tipo de IVA con su descripción al JComboBox
                ivaCombo.addItem(new ComboItem(iva, iva + "% - " + obs));
            }
        } catch (SQLException e) {
            showError("tipos de IVA", e);
        }
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
                // Agregar cada forma de pago al JComboBox
                paymentMethodCombo.addItem(new ComboItem(rs.getInt("idFormapago"), rs.getString("tipoFormaPago")));
            }
        } catch (SQLException e) {
            showError("formas de pago", e);
        }
    }

    /**
     * Muestra un mensaje de error en caso de fallo en la carga de datos.
     *
     * @param entity Nombre de la entidad (ej. clientes, trabajadores).
     * @param e      Excepción capturada.
     */
    private void showError(String entity, Exception e) {
        JOptionPane.showMessageDialog(this, "Error al cargar " + entity + ": " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Guarda la factura en la base de datos.
     */
    private void saveInvoice(ActionListener listener) {
        if (!validatePaymentDate()) {
            return;
        }

        String insertInvoice = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, " +
                "idClienteFactura, idTrabajadorFactura, baseImponibleFacturaCliente, ivaFacturaCliente, " +
                "totalFacturaCliente, cobradaFactura, formaCobroFactura, fechaCobroFactura)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertInvoice, Statement.RETURN_GENERATED_KEYS)) {

            // Configurar valores en la consulta SQL
            ps.setInt(1, Integer.parseInt(numberField.getText()));
            ps.setDate(2, java.sql.Date.valueOf(dateField.getText()));

            ComboItem clientItem = (ComboItem) clientCombo.getSelectedItem();
            ps.setInt(3, clientItem != null ? clientItem.getId() : 0);

            ComboItem workerItem = (ComboItem) workerCombo.getSelectedItem();
            ps.setInt(4, workerItem != null ? workerItem.getId() : 0);

            ps.setDouble(5, Double.parseDouble(baseAmountField.getText().replace(",", ".")));

            ComboItem ivaItem = (ComboItem) ivaCombo.getSelectedItem();
            ps.setDouble(6, ivaItem != null ? ivaItem.getNumeric() : 0);

            ps.setDouble(7, Double.parseDouble(totalAmountField.getText().replace(",", ".")));
            ps.setBoolean(8, isPaidCheckBox.isSelected());

            ComboItem pmItem = (ComboItem) paymentMethodCombo.getSelectedItem();
            ps.setInt(9, pmItem != null ? pmItem.getId() : 0);

            ps.setDate(10, paymentDateField.getText().isEmpty() ? null :
                    java.sql.Date.valueOf(paymentDateField.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Factura creada con éxito.");
            dispose();
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}