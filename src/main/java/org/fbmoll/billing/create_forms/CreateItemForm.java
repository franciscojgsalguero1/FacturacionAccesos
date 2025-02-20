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
    private final JTextField codeField = new JTextField(20);
    private final JTextField barCodeField = new JTextField(20);
    private final JTextField descriptionField = new JTextField(20);
    private final JTextField familyIdField = new JTextField(10);
    private final JTextField costField = new JTextField(10);
    private final JTextField marginField = new JTextField(10);
    private final JTextField priceField = new JTextField(10);
    private final JTextField supplierField = new JTextField(10);
    private final JTextField stockField = new JTextField(10);

    public CreateItemForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Artículo");
        setSize(400, 500);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        formPanel.add(new JLabel("Código:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Código de Barras:"));
        formPanel.add(barCodeField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("ID Familia:"));
        formPanel.add(familyIdField);
        formPanel.add(new JLabel("Costo:"));
        formPanel.add(costField);
        formPanel.add(new JLabel("Margen:"));
        formPanel.add(marginField);
        formPanel.add(new JLabel("Precio:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Proveedor:"));
        formPanel.add(supplierField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);

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