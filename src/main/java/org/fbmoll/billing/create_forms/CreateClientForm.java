package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Client;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateClientForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(10);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    private final JTextField countryField = new JTextField(20);
    private final JTextField cifField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField ibanField = new JTextField(20);
    private final JTextField riskField = new JTextField(10);
    private final JTextField discountField = new JTextField(10);
    private final JTextField descriptionField = new JTextField(20);

    public CreateClientForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Cliente");
        setSize(400, 500);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Dirección:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Código Postal:"));
        formPanel.add(postCodeField);
        formPanel.add(new JLabel("Ciudad:"));
        formPanel.add(townField);
        formPanel.add(new JLabel("Provincia:"));
        formPanel.add(provinceField);
        formPanel.add(new JLabel("País:"));
        formPanel.add(countryField);
        formPanel.add(new JLabel("CIF:"));
        formPanel.add(cifField);
        formPanel.add(new JLabel("Teléfono:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("IBAN:"));
        formPanel.add(ibanField);
        formPanel.add(new JLabel("Riesgo:"));
        formPanel.add(riskField);
        formPanel.add(new JLabel("Descuento:"));
        formPanel.add(discountField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);

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
            ps.setString(6, countryField.getText());
            ps.setString(7, cifField.getText());
            ps.setString(8, phoneField.getText());
            ps.setString(9, emailField.getText());
            ps.setString(10, ibanField.getText());
            ps.setDouble(11, Double.parseDouble(riskField.getText()));
            ps.setDouble(12, Double.parseDouble(discountField.getText()));
            ps.setString(13, descriptionField.getText());

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente creado con éxito.");
            dispose();
            Client.showClientTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear cliente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
