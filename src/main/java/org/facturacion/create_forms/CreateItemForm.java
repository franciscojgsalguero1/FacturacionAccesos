package org.facturacion.create_forms;

import org.facturacion.data_classes.Item;
import org.facturacion.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Formulario para la creación de un nuevo artículo.
 */
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

    /**
     * Constructor del formulario de creación de artículos.
     */
    public CreateItemForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        loadFamilies();
        loadProviders();
        familyDropdown = new JComboBox<>(familyMap.keySet().toArray(new String[0]));
        providerDropdown = new JComboBox<>(providerMap.keySet().toArray(new String[1]));
        initializeForm();
    }

    /**
     * Configura la interfaz del formulario.
     */
    private void initializeForm() {
        setTitle("Crear Artículo");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel buttonPanel = buildButtonPanel();
        JPanel formPanel = buildFormPanel();

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     * Carga las familias de artículos desde la base de datos.
     */
    private void loadFamilies() {
        String query = "SELECT idFamiliaArticulos, denominacionFamilias FROM familiaarticulos ORDER BY denominacionFamilias";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                familyMap.put(rs.getString("denominacionFamilias"), rs.getInt("idFamiliaArticulos"));
            }
        } catch (SQLException e) {
            showError("Error al cargar familias: ", e);
        }
    }

    /**
     * Carga los proveedores desde la base de datos.
     */
    private void loadProviders() {
        String query = "SELECT idProveedor, nombreProveedor FROM proveedores ORDER BY nombreProveedor";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                providerMap.put(rs.getString("nombreProveedor"), rs.getInt("idProveedor"));
            }
        } catch (SQLException e) {
            showError("Error al cargar proveedores: ", e);
        }
    }

    /**
     * Construye el panel del formulario con los campos de entrada.
     */
    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        addRow(formPanel, gbc, 0, new String[]{"Código:", "Código de Barras:", "Descripción:"},
                new JComponent[]{codeField, barCodeField, descriptionField});
        addRow(formPanel, gbc, 1, new String[]{"Familia:", "Costo:", "Margen:"},
                new JComponent[]{familyDropdown, costField, marginField});
        addRow(formPanel, gbc, 2, new String[]{"Precio:", "Proveedor:", "Stock:"},
                new JComponent[]{priceField, providerDropdown, stockField});
        return formPanel;
    }

    /**
     * Construye el panel de botones.
     */
    private JPanel buildButtonPanel() {
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveItem());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Agrega una fila de etiquetas y campos al formulario.
     */
    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String[] labels, JComponent[] fields) {
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = i * 2;
            gbc.gridy = row;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = i * 2 + 1;
            panel.add(fields[i], gbc);
        }
    }

    /**
     * Guarda un nuevo artículo en la base de datos.
     */
    private void saveItem() {
        if (!validateFields()) return;

        String query = "INSERT INTO articulos (codigoArticulo, codigoBarrasArticulo, descripcionArticulo, " +
                "familiaArticulo, costeArticulo, margenComercialArticulo, pvpArticulo, proveedorArticulo, stockArticulo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            int familyId = familyMap.get((String) familyDropdown.getSelectedItem());
            int providerId = providerMap.get((String) providerDropdown.getSelectedItem());

            ps.setString(1, codeField.getText().trim());
            ps.setString(2, barCodeField.getText().trim());
            ps.setString(3, descriptionField.getText().trim());
            ps.setInt(4, familyId);
            ps.setDouble(5, Double.parseDouble(costField.getText().trim()));
            ps.setDouble(6, Double.parseDouble(marginField.getText().trim()));
            ps.setDouble(7, Double.parseDouble(priceField.getText().trim()));
            ps.setInt(8, providerId);
            ps.setInt(9, Integer.parseInt(stockField.getText().trim()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo creado con éxito.");
            dispose();
            Item.showItemTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            showError("Error al crear artículo: ", e);
        }
    }

    /**
     * Valida que los campos requeridos no estén vacíos.
     */
    private boolean validateFields() {
        if (codeField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty() ||
                costField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty() ||
                stockField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Muestra un mensaje de error en caso de fallo.
     */
    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}