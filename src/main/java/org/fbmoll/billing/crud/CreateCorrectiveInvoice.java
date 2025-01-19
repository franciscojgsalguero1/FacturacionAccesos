package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.classes.CorrectiveInvoice;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

@Getter
public class CreateCorrectiveInvoice {
    public void createNewCorrectiveInvoice(JPanel panel) {
        JFrame frame = new JFrame("Create Corrective Invoice");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

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
        JTextField notesField = new JTextField(40);

        formPanel.add(new JLabel("Número de Rectificativa:"), setGBC(0, 0, 1));
        formPanel.add(numberField, setGBC(1, 0, 3));
        formPanel.add(new JLabel("Fecha de Rectificativa:"), setGBC(0, 1, 1));
        formPanel.add(dateField, setGBC(1, 1, 3));
        formPanel.add(new JLabel("ID Cliente:"), setGBC(0, 2, 1));
        formPanel.add(clientIdField, setGBC(1, 2, 3));
        formPanel.add(new JLabel("Base Imponible:"), setGBC(0, 3, 1));
        formPanel.add(taxableAmountField, setGBC(1, 3, 3));
        formPanel.add(new JLabel("Cantidad IVA:"), setGBC(0, 4, 1));
        formPanel.add(vatAmountField, setGBC(1, 4, 3));
        formPanel.add(new JLabel("Total:"), setGBC(0, 5, 1));
        formPanel.add(totalAmountField, setGBC(1, 5, 3));
        formPanel.add(new JLabel("Hash de Rectificativa:"), setGBC(0, 6, 1));
        formPanel.add(hashField, setGBC(1, 6, 3));
        formPanel.add(new JLabel("Código QR:"), setGBC(0, 7, 1));
        formPanel.add(qrCodeField, setGBC(1, 7, 3));
        formPanel.add(new JLabel("Observaciones de Rectificativa:"), setGBC(0, 8, 1));
        formPanel.add(notesField, setGBC(1, 8, 3));

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener((ActionEvent e) -> {
            try {
                double taxableAmount = Double.parseDouble(taxableAmountField.getText());
                double vatAmount = Double.parseDouble(vatAmountField.getText());
                double totalAmount = Double.parseDouble(totalAmountField.getText());

                if (taxableAmount < 0 || vatAmount < 0 || totalAmount < 0) {
                    JOptionPane.showMessageDialog(frame, "Amounts must be positive.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                CorrectiveInvoice correctiveInvoice = new CorrectiveInvoice(
                        0,
                        Integer.parseInt(numberField.getText()),
                        Date.valueOf(dateField.getText()),
                        Integer.parseInt(clientIdField.getText()),
                        taxableAmount,
                        vatAmount,
                        totalAmount,
                        hashField.getText(),
                        qrCodeField.getText(),
                        notesField.getText()
                );

                String query = "INSERT INTO rectificativasclientes (numeroRectificativaCliente, " +
                        "fechaRectificativaCliente, idClienteRectificativaCliente, baseImponibleRectificativaCliente, " +
                        "ivaRectificativaCliente, totalRectificativaCliente, hashRectificativaCliente, " +
                        "qrRectificativaCliente, observacionesRectificativaCliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = Utils.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {

                    ps.setInt(1, correctiveInvoice.getNumber());
                    ps.setDate(2, correctiveInvoice.getDate());
                    ps.setInt(3, correctiveInvoice.getClientId());
                    ps.setDouble(4, correctiveInvoice.getTaxableAmount());
                    ps.setDouble(5, correctiveInvoice.getVatAmount());
                    ps.setDouble(6, correctiveInvoice.getTotalAmount());
                    ps.setString(7, correctiveInvoice.getHash());
                    ps.setString(8, correctiveInvoice.getQrCode());
                    ps.setString(9, correctiveInvoice.getNotes());

                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(frame, "Corrective Invoice Created Successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        frame.dispose();
                        ViewCorrectiveInvoices.showCorrectiveInvoiceTable(panel);
                    }
                }
            } catch (NumberFormatException | SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid values.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(submitButton, setGBC(1, 9, 3));

        frame.add(formPanel);
        frame.setVisible(true);
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