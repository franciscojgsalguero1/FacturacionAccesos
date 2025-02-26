package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Item;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateItemForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField codeField = new JTextField(6);
    private final JTextField barCodeField = new JTextField(9);
    private final JTextField descriptionField = new JTextField(12);
    private final JTextField familyIdField = new JTextField(3);
    private final JTextField costField = new JTextField(5);
    private final JTextField marginField = new JTextField(5);
    private final JTextField priceField = new JTextField(6);
    private final JTextField supplierField = new JTextField(3);
    private final JTextField stockField = new JTextField(5);

    public CreateItemForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Artículo");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Panel principal con GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addLabelAndField(formPanel, gbc, "Código:", codeField, "Código de Barras:", barCodeField, "Descripción:", descriptionField, y++);
        addLabelAndField(formPanel, gbc, "ID Familia:", familyIdField, "Costo:", costField, "Margen:", marginField, y++);
        addLabelAndField(formPanel, gbc, "Precio:", priceField,"Proveedor:", supplierField, "Stock:", stockField, y++);

        // Panel de botones
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveItem());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, JTextField field1, String label2, JTextField field2, String label3, JTextField field3, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.5;
        panel.add(new JLabel(label1), gbc);

        gbc.gridx = 1;
        panel.add(field1, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel(label2), gbc);

        gbc.gridx = 3;
        panel.add(field2, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel(label3), gbc);

        gbc.gridx = 5;
        panel.add(field3, gbc);
    }

    private void saveItem() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO articulos (codigoArticulo, codigoBarrasArticulo, " +
                     "descripcionArticulo, familiaArticulo, costeArticulo, margenComercialArticulo, pvpArticulo, " +
                     "proveedorArticulo, stockArticulo, observacionesArticulo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setString(1, codeField.getText());
            ps.setString(2, barCodeField.getText());
            ps.setString(3, descriptionField.getText());
            ps.setInt(4, Integer.parseInt(familyIdField.getText()));
            ps.setDouble(5, Double.parseDouble(costField.getText()));
            ps.setDouble(6, Double.parseDouble(marginField.getText()));
            ps.setDouble(7, Double.parseDouble(priceField.getText()));
            ps.setInt(8, Integer.parseInt(supplierField.getText()));
            ps.setInt(9, Integer.parseInt(stockField.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo creado con éxito.");
            dispose();
            Item.showItemTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear artículo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}