package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.dataClasses.Client;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class CreateClient extends JFrame {
    public void createNewClient(JPanel panel) {
        this.setTitle("Create Client");
        this.setSize(700, 500);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(200, 30));
        JTextField addressField = new JTextField(20);
        addressField.setPreferredSize(new Dimension(200, 30));
        JTextField postcodeField = new JTextField(20);
        postcodeField.setPreferredSize(new Dimension(200, 30));
        JTextField townField = new JTextField(20);
        townField.setPreferredSize(new Dimension(200, 30));
        JTextField provinceField = new JTextField(20);
        provinceField.setPreferredSize(new Dimension(200, 30));
        JTextField countryField = new JTextField(20);
        countryField.setPreferredSize(new Dimension(200, 30));
        JTextField cifField = new JTextField(20);
        cifField.setPreferredSize(new Dimension(200, 30));
        JTextField phoneField = new JTextField(20);
        phoneField.setPreferredSize(new Dimension(200, 30));
        JTextField emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(200, 30));
        JTextField ibanField = new JTextField(20);
        ibanField.setPreferredSize(new Dimension(200, 30));
        JTextField riskField = new JTextField(20);
        riskField.setPreferredSize(new Dimension(200, 30));
        JTextField discountField = new JTextField(20);
        discountField.setPreferredSize(new Dimension(200, 30));
        JTextField descriptionField = new JTextField(40);
        descriptionField.setPreferredSize(new Dimension(400, 30));

        formPanel.add(new JLabel("Client Name:"), setGBC(0, 0, 1));
        formPanel.add(nameField, setGBC(1, 0, 1));
        formPanel.add(new JLabel("Address:"), setGBC(2, 0, 1));
        formPanel.add(addressField, setGBC(3, 0, 1));
        formPanel.add(new JLabel("Postcode:"), setGBC(0, 1, 1));
        formPanel.add(postcodeField, setGBC(1, 1, 1));
        formPanel.add(new JLabel("Town:"), setGBC(2, 1, 1));
        formPanel.add(townField, setGBC(3, 1, 1));
        formPanel.add(new JLabel("Province:"), setGBC(0, 2, 1));
        formPanel.add(provinceField, setGBC(1, 2, 1));
        formPanel.add(new JLabel("Country:"), setGBC(2, 2, 1));
        formPanel.add(countryField, setGBC(3, 2, 1));
        formPanel.add(new JLabel("CIF:"), setGBC(0, 3, 1));
        formPanel.add(cifField, setGBC(1, 3, 1));
        formPanel.add(new JLabel("Phone:"), setGBC(2, 3, 1));
        formPanel.add(phoneField, setGBC(3, 3, 1));
        formPanel.add(new JLabel("Email:"), setGBC(0, 4, 1));
        formPanel.add(emailField, setGBC(1, 4, 1));
        formPanel.add(new JLabel("IBAN:"), setGBC(2, 4, 1));
        formPanel.add(ibanField, setGBC(3, 4, 1));
        formPanel.add(new JLabel("Risk:"), setGBC(0, 5, 1));
        formPanel.add(riskField, setGBC(1, 5, 1));
        formPanel.add(new JLabel("Discount:"), setGBC(2, 5, 1));
        formPanel.add(discountField, setGBC(3, 5, 1));
        formPanel.add(new JLabel("Description:"), setGBC(0, 6, 1));
        formPanel.add(descriptionField, setGBC(1, 6, 3));

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener((ActionEvent e) -> {
            try {
                double risk = Double.parseDouble(riskField.getText());
                double discount = Double.parseDouble(discountField.getText());

                if (risk < 0 || risk > 999.99) {
                    JOptionPane.showMessageDialog(this, "Riesgo tiene que ser entre 0 y " +
                                    "999.99", "Riesgo inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (discount < 0 || discount > 99.99) {
                    JOptionPane.showMessageDialog(this, "Descuento tiene que ser entre 0 y " +
                            "99.99", "Descuento inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Client client = new Client(0, nameField.getText(), addressField.getText(),
                        Integer.parseInt(postcodeField.getText()), townField.getText(), provinceField.getText(),
                        countryField.getText(), cifField.getText(), phoneField.getText(), emailField.getText(),
                        ibanField.getText(), risk, discount, descriptionField.getText()
                );

                String query = "INSERT INTO clientes (nombreCliente, direccionCliente, cpCliente, poblacionCliente, " +
                        "provinciaCliente, paisCliente, cifCliente, telCliente, emailCliente, ibanCliente, " +
                        "riesgoCliente, descuentoCliente, observacionesCliente) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = Utils.getConnection();
                    PreparedStatement ps = conn.prepareStatement(query)) {

                    ps.setString(1, client.getName());
                    ps.setString(2, client.getAddress());
                    ps.setInt(3, client.getPostCode());
                    ps.setString(4, client.getTown());
                    ps.setString(5, client.getProvince());
                    ps.setString(6, client.getCountry());
                    ps.setString(7, client.getCif());
                    ps.setString(8, client.getNumber());
                    ps.setString(9, client.getEmail());
                    ps.setString(10, client.getIban());
                    ps.setDouble(11, client.getRisk());
                    ps.setDouble(12, client.getDiscount());
                    ps.setString(13, client.getDescription());

                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Client Created Successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        ViewClients.visualizeClients(panel);
                    }
                }
            } catch (NumberFormatException | SQLException | IllegalAccessException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numerical values" +
                        "for risk and discount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(submitButton, setGBC(1, 7, 4));

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