package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Getter
public class CreateItem extends JFrame {
    public void createItem(JPanel panel) {
        this.setTitle("Create Item");
        this.setSize(800, 500);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField codeField = new JTextField(20);
        JTextField barCodeField = new JTextField(20);
        JTextField descriptionField = new JTextField(20);
        JTextField familyIdField = new JTextField(20);
        JTextField costField = new JTextField(20);
        JTextField marginField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField supplierField = new JTextField(20);
        JTextField stockField = new JTextField(20);
        JTextField notesField = new JTextField(40);

        formPanel.add(new JLabel("Código Artículo:"), setGBC(0, 0, 1));
        formPanel.add(codeField, setGBC(1, 0, 1));
        formPanel.add(new JLabel("Código de Barras:"), setGBC(2, 0, 1));
        formPanel.add(barCodeField, setGBC(3, 0, 1));
        formPanel.add(new JLabel("Descripción:"), setGBC(0, 1, 1));
        formPanel.add(descriptionField, setGBC(1, 1, 1));
        formPanel.add(new JLabel("Familia:"), setGBC(2, 1, 1));
        formPanel.add(familyIdField, setGBC(3, 1, 1));
        formPanel.add(new JLabel("Coste:"), setGBC(0, 2, 1));
        formPanel.add(costField, setGBC(1, 2, 1));
        formPanel.add(new JLabel("Margen Comercial:"), setGBC(2, 2, 1));
        formPanel.add(marginField, setGBC(3, 2, 1));
        formPanel.add(new JLabel("PVP:"), setGBC(0, 3, 1));
        formPanel.add(priceField, setGBC(1, 3, 1));
        formPanel.add(new JLabel("Proveedor:"), setGBC(2, 3, 1));
        formPanel.add(supplierField, setGBC(3, 3, 1));
        formPanel.add(new JLabel("Stock:"), setGBC(0, 4, 1));
        formPanel.add(stockField, setGBC(1, 4, 1));
        formPanel.add(new JLabel("Observaciones:"), setGBC(0, 5, 1));
        formPanel.add(notesField, setGBC(1, 5, 3));

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener((ActionEvent e) -> {
            try {
                String code = codeField.getText();
                String barCode = barCodeField.getText();
                String description = descriptionField.getText();
                int familyId = Integer.parseInt(familyIdField.getText());
                double cost = Double.parseDouble(costField.getText());
                double margin = Double.parseDouble(marginField.getText());
                double price = Double.parseDouble(priceField.getText());
                int supplier = Integer.parseInt(supplierField.getText());
                int stock = Integer.parseInt(stockField.getText());
                String notes = notesField.getText();

                String query = "INSERT INTO articulos (codigoArticulo, codigoBarrasArticulo, descripcionArticulo, " +
                        "familiaArticulo, costeArticulo, margenComercialArticulo, pvpArticulo, proveedorArticulo, " +
                        "stockArticulo, observacionesArticulo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = Utils.getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, code);
                    ps.setString(2, barCode);
                    ps.setString(3, description);
                    ps.setInt(4, familyId);
                    ps.setDouble(5, cost);
                    ps.setDouble(6, margin);
                    ps.setDouble(7, price);
                    ps.setInt(8, supplier);
                    ps.setInt(9, stock);
                    ps.setString(10, notes);

                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Item Created Successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                        ViewItems.showItemsTable(panel); // Call your method to update the item view
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