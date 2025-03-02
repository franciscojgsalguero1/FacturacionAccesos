package org.facturacion.create_forms;

import org.facturacion.data_classes.Client;
import org.facturacion.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Formulario para la creación de un nuevo cliente.
 * Extiende JDialog para proporcionar una ventana modal.
 */
public class CreateClientForm extends JDialog {
    private final JPanel parentPanel; // Panel principal donde se actualizarán los datos
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(5);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    private final JComboBox<String> countryCombo = new JComboBox<>();
    private final JTextField cifField = new JTextField(9);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(20);
    private final JTextField ibanField = new JTextField(16);
    private final JTextField riskField = new JTextField(4);
    private final JTextField discountField = new JTextField(4);

    /**
     * Constructor que inicializa el formulario.
     * @param parentPanel Panel principal al que se actualizarán los datos después de guardar.
     */
    public CreateClientForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Cliente");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true); // Bloquea interacción con la ventana principal hasta que se cierre
        setLocationRelativeTo(parentPanel); // Centra la ventana respecto al panel padre

        loadCountries(); // Carga la lista de países en el comboBox

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Añade margen
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Matriz con los campos del formulario
        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryCombo, "Código Postal:", postCodeField},
                {"CIF:", cifField, "Teléfono:", phoneField},
                {"Email:", emailField, "IBAN:", ibanField},
                {"Riesgo:", riskField, "Descuento:", discountField}
        };

        // Agregar filas al formulario
        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        // Botón para guardar el cliente
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveClient());

        // Botón para cancelar la operación y cerrar la ventana
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true); // Muestra la ventana
    }

    /**
     * Carga la lista de países desde la base de datos y los añade al comboBox.
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
            JOptionPane.showMessageDialog(this, "Error al cargar países: "
                    + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Añade un par de etiquetas y campos al formulario en una fila.
     * @param panel Panel al que se añadirán los elementos.
     * @param gbc Restricciones para GridBagLayout.
     * @param label1 Texto de la primera etiqueta.
     * @param comp1 Primer campo asociado.
     * @param label2 Texto de la segunda etiqueta.
     * @param comp2 Segundo campo asociado.
     * @param row Fila en la que se colocarán los elementos.
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
     * Guarda un nuevo cliente en la base de datos con los valores ingresados en el formulario.
     */
    private void saveClient() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO clientes (nombreCliente, " +
                     "direccionCliente, cpCliente, poblacionCliente, provinciaCliente, paisCliente, cifCliente," +
                     " telCliente, emailCliente, ibanCliente, riesgoCliente, descuentoCliente," +
                     " observacionesCliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setString(1, nameField.getText()); // Nombre del cliente
            ps.setString(2, addressField.getText()); // Dirección
            ps.setInt(3, Integer.parseInt(postCodeField.getText())); // Código postal
            ps.setString(4, townField.getText()); // Ciudad
            ps.setString(5, provinceField.getText()); // Provincia
            ps.setString(6, (String) countryCombo.getSelectedItem()); // País
            ps.setString(7, cifField.getText()); // CIF
            ps.setString(8, phoneField.getText()); // Teléfono
            ps.setString(9, emailField.getText()); // Email
            ps.setString(10, ibanField.getText()); // IBAN
            ps.setDouble(11, Double.parseDouble(riskField.getText())); // Riesgo financiero
            ps.setDouble(12, Double.parseDouble(discountField.getText())); // Descuento
            ps.setString(13, ""); // Observaciones (vacío por defecto)

            // Ejecutar la consulta de inserción
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente creado con éxito.");

            // Cerrar la ventana y actualizar la tabla de clientes en la UI
            dispose();
            Client.showClientTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear cliente: "
                    + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}