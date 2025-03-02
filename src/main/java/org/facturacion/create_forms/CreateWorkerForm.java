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
 * Permite registrar información como nombre, dirección, contacto, salario y comisión.
 */
public class CreateWorkerForm extends JDialog {
    private final JPanel parentPanel; // Panel donde se actualizará la tabla de trabajadores.

    // Campos de entrada de datos para el trabajador
    private final JTextField nameField = new JTextField(20); // Nombre del trabajador.
    private final JTextField addressField = new JTextField(20); // Dirección.
    private final JTextField postCodeField = new JTextField(10); // Código postal.
    private final JTextField townField = new JTextField(20); // Ciudad.
    private final JTextField provinceField = new JTextField(20); // Provincia.
    private final JComboBox<String> countryCombo = new JComboBox<>(); // Lista desplegable de países.
    private final JTextField dniField = new JTextField(15); // DNI del trabajador.
    private final JTextField phoneField = new JTextField(15); // Teléfono de contacto.
    private final JTextField emailField = new JTextField(25); // Correo electrónico.
    private final JTextField positionField = new JTextField(20); // Puesto del trabajador.
    private final JTextField salaryField = new JTextField(10); // Salario.
    private final JTextField commissionField = new JTextField(10); // Comisión en porcentaje.

    /**
     * Constructor de la clase. Inicializa la ventana del formulario.
     *
     * @param parentPanel Panel donde se actualizará la tabla de trabajadores.
     */
    public CreateWorkerForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Trabajador");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        loadCountries(); // Carga la lista de países en el combo box.

        // Configuración del panel del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Estructura del formulario con etiquetas y campos
        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryCombo, "Código Postal:", postCodeField},
                {"DNI:", dniField, "Teléfono:", phoneField},
                {"Email:", emailField, "Puesto:", positionField},
                {"Salario:", salaryField, "Comisión %:", commissionField}
        };

        // Agregar cada fila al formulario
        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        // Botón para guardar el trabajador
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveWorker());

        // Botón para cancelar y cerrar el formulario
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Agregar los componentes al formulario principal
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Mostrar el formulario
        setVisible(true);
    }

    /**
     * Carga la lista de países desde la base de datos y los agrega al JComboBox countryCombo.
     */
    private void loadCountries() {
        String query = "SELECT name FROM countries ORDER BY name";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countryCombo.addItem(rs.getString("name")); // Agrega cada país al JComboBox.
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar países: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método para agregar una fila de etiquetas y campos de entrada al formulario.
     *
     * @param panel   Panel donde se agregarán los elementos.
     * @param gbc     Configuración de GridBagLayout.
     * @param label1  Etiqueta del primer campo.
     * @param comp1   Primer campo de entrada.
     * @param label2  Etiqueta del segundo campo.
     * @param comp2   Segundo campo de entrada.
     * @param row     Número de fila donde se colocarán los elementos.
     */
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

    /**
     * Método para guardar el trabajador en la base de datos.
     */
    private void saveWorker() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO workers (name, address, postCode, town, province, country, dni, phone, " +
                             "email, position, salary, commissionPercentage) VALUES " +
                             "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            // Asignar valores a la consulta SQL
            ps.setString(1, nameField.getText()); // Nombre del trabajador.
            ps.setString(2, addressField.getText()); // Dirección.
            ps.setInt(3, Integer.parseInt(postCodeField.getText())); // Código postal.
            ps.setString(4, townField.getText()); // Ciudad.
            ps.setString(5, provinceField.getText()); // Provincia.
            ps.setString(6, (String) countryCombo.getSelectedItem()); // País.
            ps.setString(7, dniField.getText()); // DNI.
            ps.setString(8, phoneField.getText()); // Teléfono.
            ps.setString(9, emailField.getText()); // Email.
            ps.setString(10, positionField.getText()); // Puesto de trabajo.
            ps.setDouble(11, Double.parseDouble(salaryField.getText())); // Salario.
            ps.setDouble(12, Double.parseDouble(commissionField.getText())); // Comisión.

            // Ejecutar la inserción en la base de datos
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Trabajador creado con éxito.");
            dispose();

            // Actualizar la tabla de trabajadores en la interfaz de usuario
            Worker.showWorkerTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear trabajador: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}