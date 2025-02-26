package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Provider;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateProviderForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(5);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    private final JComboBox<String> countryCombo = new JComboBox<>();
    private final JTextField cifField = new JTextField(9);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(20);
    private final JTextField websiteField = new JTextField(20);

    public CreateProviderForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Proveedor");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);
        loadCountries();
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
                {"CIF:", cifField, "Teléfono:", phoneField},
                {"Email:", emailField, "Web:", websiteField}
        };

        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveProvider());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

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

    private void saveProvider() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO proveedores (nombreProveedor," +
                     " direccionProveedor, cpProveedor, poblacionProveedor, provinciaProveedor, paisProveedor," +
                     " cifProveedor, telProveedor, emailProveedor, webProveedor) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, nameField.getText());
            ps.setString(2, addressField.getText());
            ps.setInt(3, Integer.parseInt(postCodeField.getText()));
            ps.setString(4, townField.getText());
            ps.setString(5, provinceField.getText());
            ps.setString(6, (String) countryCombo.getSelectedItem());
            ps.setString(7, cifField.getText());
            ps.setString(8, phoneField.getText());
            ps.setString(9, emailField.getText());
            ps.setString(10, websiteField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Proveedor creado con éxito.");
            dispose();
            Provider.showProviderTable(parentPanel, e -> {
            });
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear proveedor: " +
                    e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}