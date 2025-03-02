package org.facturacion.create_forms;

import org.facturacion.data_classes.Provider;
import org.facturacion.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase que representa un formulario para la creación de un nuevo proveedor.
 * Permite ingresar datos como nombre, dirección, contacto y otros detalles relevantes.
 */
public class CreateProviderForm extends JDialog {
    private final JPanel parentPanel; // Panel donde se actualizará la tabla de proveedores.

    // Campos de entrada de datos
    private final JTextField nameField = new JTextField(20); // Nombre del proveedor.
    private final JTextField addressField = new JTextField(20); // Dirección del proveedor.
    private final JTextField postCodeField = new JTextField(5); // Código postal.
    private final JTextField townField = new JTextField(20); // Ciudad.
    private final JTextField provinceField = new JTextField(20); // Provincia.
    private final JComboBox<String> countryCombo = new JComboBox<>(); // País.
    private final JTextField cifField = new JTextField(9); // CIF del proveedor.
    private final JTextField phoneField = new JTextField(15); // Teléfono de contacto.
    private final JTextField emailField = new JTextField(20); // Email del proveedor.
    private final JTextField websiteField = new JTextField(20); // Sitio web del proveedor.

    /**
     * Constructor de la clase. Inicializa el formulario.
     *
     * @param parentPanel Panel donde se actualizará la tabla de proveedores.
     */
    public CreateProviderForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Proveedor");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        loadCountries(); // Carga los países en el combo box.

        // Configuración del panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Estructura de filas para organizar los campos en el formulario
        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryCombo, "Código Postal:", postCodeField},
                {"CIF:", cifField, "Teléfono:", phoneField},
                {"Email:", emailField, "Web:", websiteField}
        };

        // Agrega las filas al formulario
        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        // Botón para guardar los datos del proveedor
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveProvider());

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
     * Método para agregar una fila de etiquetas y campos al formulario.
     *
     * @param panel   Panel donde se agregarán los componentes.
     * @param gbc     Configuración de GridBagLayout.
     * @param label1  Etiqueta para el primer campo.
     * @param comp1   Primer campo de entrada.
     * @param label2  Etiqueta para el segundo campo.
     * @param comp2   Segundo campo de entrada.
     * @param row     Número de fila donde se colocarán los elementos.
     */
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
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
     * Método para cargar los países desde la base de datos y agregarlos al JComboBox.
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
            JOptionPane.showMessageDialog(this, "Error al cargar países: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método para guardar el proveedor en la base de datos.
     */
    private void saveProvider() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO proveedores (nombreProveedor," +
                     " direccionProveedor, cpProveedor, poblacionProveedor, provinciaProveedor, paisProveedor," +
                     " cifProveedor, telProveedor, emailProveedor, webProveedor) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            // Asignar valores a la consulta SQL
            ps.setString(1, nameField.getText()); // Nombre del proveedor.
            ps.setString(2, addressField.getText()); // Dirección.
            ps.setInt(3, Integer.parseInt(postCodeField.getText())); // Código postal.
            ps.setString(4, townField.getText()); // Ciudad.
            ps.setString(5, provinceField.getText()); // Provincia.
            ps.setString(6, (String) countryCombo.getSelectedItem()); // País.
            ps.setString(7, cifField.getText()); // CIF.
            ps.setString(8, phoneField.getText()); // Teléfono.
            ps.setString(9, emailField.getText()); // Email.
            ps.setString(10, websiteField.getText()); // Sitio web.

            // Ejecutar la inserción en la base de datos
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Proveedor creado con éxito.");
            dispose();

            // Actualizar la tabla de proveedores en la interfaz de usuario
            Provider.showProviderTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear proveedor: " +
                    e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}