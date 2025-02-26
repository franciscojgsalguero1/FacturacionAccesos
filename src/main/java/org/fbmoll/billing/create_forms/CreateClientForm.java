package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Client;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateClientForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(5);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    // Removed countryField; replaced with a JComboBox
    private final JComboBox<String> countryCombo = new JComboBox<>();
    private final JTextField cifField = new JTextField(9);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(20);
    private final JTextField ibanField = new JTextField(16);
    private final JTextField riskField = new JTextField(4);
    private final JTextField discountField = new JTextField(4);

    public CreateClientForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Cliente");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Load countries from the database into the combo box
        loadCountries();

        // Panel principal con GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addLabelAndField(formPanel, gbc, "Nombre:", nameField, "Dirección:", addressField, y++);
        addLabelAndField(formPanel, gbc, "Ciudad:", townField, "Provincia:", provinceField, y++);
        // Use the countryCombo instead of a JTextField for the country field
        addLabelAndField(formPanel, gbc, "País:", countryCombo, "Código Postal:", postCodeField, y++);
        addLabelAndField(formPanel, gbc, "CIF:", cifField, "Teléfono:", phoneField, y++);
        addLabelAndField(formPanel, gbc, "Email:", emailField, "IBAN:", ibanField, y++);
        addLabelAndField(formPanel, gbc, "Riesgo:", riskField, "Descuento:", discountField, y++);

        // Panel de botones
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveClient());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Overloaded helper method to add a label and a component (JTextField, JComboBox, etc.) to the panel.
     */
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, Component comp1, String label2, Component comp2, int row) {
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
     * Loads the list of countries from the "countries" table (field "name") into the countryCombo.
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
            JOptionPane.showMessageDialog(this, "Error al cargar países: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveClient() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO clientes (nombreCliente, direccionCliente, cpCliente, poblacionCliente, " +
                     "provinciaCliente, paisCliente, cifCliente, telCliente, emailCliente, ibanCliente, " +
                     "riesgoCliente, descuentoCliente, observacionesCliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setString(1, nameField.getText());
            ps.setString(2, addressField.getText());
            ps.setInt(3, Integer.parseInt(postCodeField.getText()));
            ps.setString(4, townField.getText());
            ps.setString(5, provinceField.getText());
            // Get the selected country from the combo box
            ps.setString(6, (String) countryCombo.getSelectedItem());
            ps.setString(7, cifField.getText());
            ps.setString(8, phoneField.getText());
            ps.setString(9, emailField.getText());
            ps.setString(10, ibanField.getText());
            ps.setDouble(11, Double.parseDouble(riskField.getText()));
            ps.setDouble(12, Double.parseDouble(discountField.getText()));
            // Assuming observacionesCliente is optional, we pass an empty string
            ps.setString(13, "");

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente creado con éxito.");
            dispose();
            Client.showClientTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear cliente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}