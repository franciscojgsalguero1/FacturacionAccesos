package org.facturacion.create_forms;

import org.facturacion.data_classes.Worker;
import org.facturacion.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase para la creación de un nuevo trabajador.
 */
public class CreateWorkerForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(10);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    private final JComboBox<String> countryCombo = new JComboBox<>();
    private final JTextField dniField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(25);
    private final JTextField positionField = new JTextField(20);
    private final JTextField salaryField = new JTextField(10);
    private final JTextField commissionField = new JTextField(10);

    /**
     * Constructor que inicializa el formulario.
     */
    public CreateWorkerForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        initializeForm();
    }

    /**
     * Configura la interfaz del formulario.
     */
    private void initializeForm() {
        setTitle("Crear Trabajador");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        loadCountries();
        JPanel formPanel = buildFormPanel();
        JPanel buttonPanel = buildButtonPanel();

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     * Construye el panel del formulario con los campos de entrada.
     */
    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryCombo, "Código Postal:", postCodeField},
                {"DNI:", dniField, "Teléfono:", phoneField},
                {"Email:", emailField, "Puesto:", positionField},
                {"Salario:", salaryField, "Comisión %:", commissionField}
        };

        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc, (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }
        return formPanel;
    }

    /**
     * Agrega una fila de etiquetas y campos al formulario.
     *
     * @param panel   Panel donde se agregarán los componentes.
     * @param gbc     Configuración de diseño de GridBagLayout.
     * @param label1  Texto de la primera etiqueta.
     * @param comp1   Primer campo de entrada.
     * @param label2  Texto de la segunda etiqueta.
     * @param comp2   Segundo campo de entrada.
     * @param row     Fila donde se colocarán los elementos.
     */
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridy = row;

        // Primera etiqueta
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(label1), gbc);

        // Primer campo de entrada
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        panel.add(comp1, gbc);

        // Segunda etiqueta
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        panel.add(new JLabel(label2), gbc);

        // Segundo campo de entrada
        gbc.gridx = 3;
        gbc.weightx = 0.8;
        panel.add(comp2, gbc);
    }

    /**
     * Construye el panel de botones.
     */
    private JPanel buildButtonPanel() {
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveWorker());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Carga la lista de países desde la base de datos.
     */
    private void loadCountries() {
        String query = "SELECT name FROM countries ORDER BY name";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countryCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            showError("Error al cargar países: ", e);
        }
    }

    /**
     * Guarda el trabajador en la base de datos.
     */
    private void saveWorker() {
        if (!validateFields()) return;

        String query = "INSERT INTO workers (name, address, postCode, town, province, country, dni, phone, " +
                "email, position, salary, commissionPercentage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, nameField.getText().trim());
            ps.setString(2, addressField.getText().trim());
            ps.setInt(3, Integer.parseInt(postCodeField.getText().trim()));
            ps.setString(4, townField.getText().trim());
            ps.setString(5, provinceField.getText().trim());
            ps.setString(6, (String) countryCombo.getSelectedItem());
            ps.setString(7, dniField.getText().trim());
            ps.setString(8, phoneField.getText().trim());
            ps.setString(9, emailField.getText().trim());
            ps.setString(10, positionField.getText().trim());
            ps.setDouble(11, Double.parseDouble(salaryField.getText().trim()));
            ps.setDouble(12, Double.parseDouble(commissionField.getText().trim()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Trabajador creado con éxito.");
            dispose();
            Worker.showWorkerTable(parentPanel, e -> {});
        } catch (SQLException e) {
            showError("Error al crear trabajador: ", e);
        }
    }

    /**
     * Valida que los campos requeridos no estén vacíos.
     */
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty() || addressField.getText().trim().isEmpty() ||
                postCodeField.getText().trim().isEmpty() || dniField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos obligatorios deben estar completos.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Muestra un mensaje de error en caso de fallo.
     */
    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}