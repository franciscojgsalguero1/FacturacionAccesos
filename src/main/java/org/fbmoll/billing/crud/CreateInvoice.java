package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Getter
public class CreateInvoice extends JFrame {
    public void createInvoice(JPanel panel) {
        this.setTitle("Create Invoice");
        this.setSize(800, 500);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField numberField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField clientIdField = new JTextField(20);
        JTextField taxableAmountField = new JTextField(20);
        JTextField vatAmountField = new JTextField(20);
        JTextField totalAmountField = new JTextField(20);
        JTextField hashField = new JTextField(20);
        JTextField qrCodeField = new JTextField(20);
        JCheckBox paidField = new JCheckBox();
        JTextField paymentMethodField = new JTextField(20);
        JTextField paymentDateField = new JTextField(20);
        JTextField notesField = new JTextField(40);

        formPanel.add(new JLabel("Número de Factura:"), setGBC(0, 0, 1));
        formPanel.add(numberField, setGBC(1, 0, 1));
        formPanel.add(new JLabel("Fecha de Factura:"), setGBC(2, 0, 1));
        formPanel.add(dateField, setGBC(3, 0, 1));
        formPanel.add(new JLabel("ID Cliente:"), setGBC(0, 1, 1));
        formPanel.add(clientIdField, setGBC(1, 1, 1));
        formPanel.add(new JLabel("Base Imponible:"), setGBC(2, 1, 1));
        formPanel.add(taxableAmountField, setGBC(3, 1, 1));
        formPanel.add(new JLabel("Cantidad IVA:"), setGBC(0, 2, 1));
        formPanel.add(vatAmountField, setGBC(1, 2, 1));
        formPanel.add(new JLabel("Total:"), setGBC(2, 2, 1));
        formPanel.add(totalAmountField, setGBC(3, 2, 1));
        formPanel.add(new JLabel("Hash de Factura:"), setGBC(0, 3, 1));
        formPanel.add(hashField, setGBC(1, 3, 1));
        formPanel.add(new JLabel("Código QR:"), setGBC(2, 3, 1));
        formPanel.add(qrCodeField, setGBC(3, 3, 1));
        formPanel.add(new JLabel("¿Cobrada?:"), setGBC(0, 4, 1));
        formPanel.add(paidField, setGBC(1, 4, 1));
        formPanel.add(new JLabel("Método de Pago:"), setGBC(2, 4, 1));
        formPanel.add(paymentMethodField, setGBC(3, 4, 1));
        formPanel.add(new JLabel("Fecha de Pago:"), setGBC(0, 5, 1));
        formPanel.add(paymentDateField, setGBC(1, 5, 1));
        formPanel.add(new JLabel("Observaciones:"), setGBC(0, 6, 1));
        formPanel.add(notesField, setGBC(1, 6, 3));

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener((ActionEvent e) -> {
            try {
                int number = Integer.parseInt(numberField.getText());
                int clientId = Integer.parseInt(clientIdField.getText());
                double taxableAmount = Double.parseDouble(taxableAmountField.getText());
                double vatAmount = Double.parseDouble(vatAmountField.getText());
                double totalAmount = Double.parseDouble(totalAmountField.getText());
                int paymentMethod = Integer.parseInt(paymentMethodField.getText());
                java.sql.Date date = java.sql.Date.valueOf(dateField.getText());
                java.sql.Date paymentDate = java.sql.Date.valueOf(paymentDateField.getText());

                String query = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, " +
                        "idClienteFactura, baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, " +
                        "hashFacturaCliente, qrFacturaCliente, cobradaFactura, formaCobroFactura, fechaCobroFactura," +
                        " observacionesFacturaClientes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = Utils.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, number);
                    ps.setDate(2, date);
                    ps.setInt(3, clientId);
                    ps.setDouble(4, taxableAmount);
                    ps.setDouble(5, vatAmount);
                    ps.setDouble(6, totalAmount);
                    ps.setString(7, hashField.getText());
                    ps.setString(8, qrCodeField.getText());
                    ps.setBoolean(9, paidField.isSelected());
                    ps.setInt(10, paymentMethod);
                    ps.setDate(11, paymentDate);
                    ps.setString(12, notesField.getText());

                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Invoice Created Successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        ViewInvoices.showInvoiceTable(panel);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(submitButton, setGBC(1, 7, 2));

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