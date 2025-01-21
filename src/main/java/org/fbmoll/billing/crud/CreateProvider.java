package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Getter
public class CreateProvider extends JFrame {
    public void createProvider(JPanel panel) {
        this.setTitle("Create Provider");
        this.setSize(800, 500);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField postCodeField = new JTextField(20);
        JTextField townField = new JTextField(20);
        JTextField provinceField = new JTextField(20);
        JTextField countryField = new JTextField(20);
        JTextField cifField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField websiteField = new JTextField(20);
        JTextField notesField = new JTextField(40);

        formPanel.add(new JLabel("Nombre:"), setGBC(0, 0, 1));
        formPanel.add(nameField, setGBC(1, 0, 1));
        formPanel.add(new JLabel("Dirección:"), setGBC(2, 0, 1));
        formPanel.add(addressField, setGBC(3, 0, 1));
        formPanel.add(new JLabel("Código Postal:"), setGBC(0, 1, 1));
        formPanel.add(postCodeField, setGBC(1, 1, 1));
        formPanel.add(new JLabel("Población:"), setGBC(2, 1, 1));
        formPanel.add(townField, setGBC(3, 1, 1));
        formPanel.add(new JLabel("Provincia:"), setGBC(0, 2, 1));
        formPanel.add(provinceField, setGBC(1, 2, 1));
        formPanel.add(new JLabel("País:"), setGBC(2, 2, 1));
        formPanel.add(countryField, setGBC(3, 2, 1));
        formPanel.add(new JLabel("CIF:"), setGBC(0, 3, 1));
        formPanel.add(cifField, setGBC(1, 3, 1));
        formPanel.add(new JLabel("Teléfono:"), setGBC(2, 3, 1));
        formPanel.add(phoneField, setGBC(3, 3, 1));
        formPanel.add(new JLabel("Email:"), setGBC(0, 4, 1));
        formPanel.add(emailField, setGBC(1, 4, 1));
        formPanel.add(new JLabel("Web:"), setGBC(2, 4, 1));
        formPanel.add(websiteField, setGBC(3, 4, 1));
        formPanel.add(new JLabel("Observaciones:"), setGBC(0, 5, 1));
        formPanel.add(notesField, setGBC(1, 5, 3));

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener((ActionEvent e) -> {
            try {
                String name = nameField.getText();
                String address = addressField.getText();
                int postCode = Integer.parseInt(postCodeField.getText());
                String town = townField.getText();
                String province = provinceField.getText();
                String country = countryField.getText();
                String cif = cifField.getText();
                String phone = phoneField.getText();
                String email = emailField.getText();
                String website = websiteField.getText();
                String notes = notesField.getText();

                String query = "INSERT INTO proveedores (nombreProveedor, direccionProveedor, cpProveedor, " +
                        "poblacionProveedor, provinciaProveedor, paisProveedor, cifProveedor, telProveedor, " +
                        "emailProveedor, webProveedor, observacionesProveedor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = Utils.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, name);
                    ps.setString(2, address);
                    ps.setInt(3, postCode);
                    ps.setString(4, town);
                    ps.setString(5, province);
                    ps.setString(6, country);
                    ps.setString(7, cif);
                    ps.setString(8, phone);
                    ps.setString(9, email);
                    ps.setString(10, website);
                    ps.setString(11, notes);

                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Provider Created Successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        ViewProviders.showProviderTable(panel); // Call your method to update the provider view
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(submitButton, setGBC(1, 6, 2));

        this.add(formPanel);
        this.setVisible(true);
    }

    private GridBagConstraints setGBC(int x, int y, int gw) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gw;
        return gbc;
    }
}