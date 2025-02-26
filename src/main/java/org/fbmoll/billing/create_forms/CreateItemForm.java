package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Item;
import org.fbmoll.billing.resources.Constants;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CreateItemForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField codeField = new JTextField(6);
    private final JTextField barCodeField = new JTextField(9);
    private final JTextField descriptionField = new JTextField(12);
    private final JTextField costField = new JTextField(5);
    private final JTextField marginField = new JTextField(5);
    private final JTextField priceField = new JTextField(6);
    private final JTextField stockField = new JTextField(5);

    private final JComboBox<String> familyDropdown;
    private final JComboBox<String> providerDropdown;

    private final Map<String, Integer> familyMap = new HashMap<>();
    private final Map<String, Integer> providerMap = new HashMap<>();

    public CreateItemForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Artículo");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        loadFamilyCodes();
        loadProviderNames();

        familyDropdown = new JComboBox<>(familyMap.keySet().toArray(new String[0]));
        providerDropdown = new JComboBox<>(providerMap.keySet().toArray(new String[0]));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        addRow(formPanel, gbc, 0,
                new String[]{"Código:", "Código de Barras:", "Descripción:"},
                new JComponent[]{codeField, barCodeField, descriptionField});
        addRow(formPanel, gbc, 1,
                new String[]{"Familia:", "Costo:", "Margen:"},
                new JComponent[]{familyDropdown, costField, marginField});
        addRow(formPanel, gbc, 2,
                new String[]{"Precio:", "Proveedor:", "Stock:"},
                new JComponent[]{priceField, providerDropdown, stockField});

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
    
    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String[] labels, JComponent[] fields) {
        if (labels.length != fields.length) {
            throw new IllegalArgumentException("Labels and fields arrays must have the same length.");
        }
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = i * 2;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = i * 2 + 1;
            gbc.weightx = 0.5;
            panel.add(fields[i], gbc);
        }
    }

    private void saveItem() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO articulos (codigoArticulo, codigoBarrasArticulo, " +
                     "descripcionArticulo, familiaArticulo, costeArticulo, margenComercialArticulo, pvpArticulo, " +
                     "proveedorArticulo, stockArticulo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            String selectedFamily = (String) familyDropdown.getSelectedItem();
            String selectedProvider = (String) providerDropdown.getSelectedItem();
            int familyId = familyMap.get(selectedFamily);
            int providerId = providerMap.get(selectedProvider);

            ps.setString(1, codeField.getText());
            ps.setString(2, barCodeField.getText());
            ps.setString(3, descriptionField.getText());
            ps.setInt(4, familyId);
            ps.setDouble(5, Double.parseDouble(costField.getText()));
            ps.setDouble(6, Double.parseDouble(marginField.getText()));
            ps.setDouble(7, Double.parseDouble(priceField.getText()));
            ps.setInt(8, providerId);
            ps.setInt(9, Integer.parseInt(stockField.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo creado con éxito.");
            dispose();
            Item.showItemTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear artículo: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFamilyCodes() {
        String query = "SELECT idFamiliaArticulos, denominacionFamilias FROM familiaarticulos";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString("denominacionFamilias");
                int id = rs.getInt("idFamiliaArticulos");
                familyMap.put(code, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar familias: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProviderNames() {
        String query = "SELECT idProveedor, nombreProveedor FROM proveedores";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("nombreProveedor");
                int id = rs.getInt("idProveedor");
                providerMap.put(name, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar proveedores: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
}