package org.facturacion.create_forms;

import org.facturacion.data_classes.Item;
import org.facturacion.resources.Constants;
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
 * Clase que representa un formulario para la creación de un nuevo artículo.
 * Permite ingresar datos como código, código de barras, descripción, costo, margen, precio, stock,
 * familia y proveedor del artículo.
 */
public class CreateItemForm extends JDialog {
    private final JPanel parentPanel;

    // Campos de entrada de texto
    private final JTextField codeField = new JTextField(6);
    private final JTextField barCodeField = new JTextField(9);
    private final JTextField descriptionField = new JTextField(12);
    private final JTextField costField = new JTextField(5);
    private final JTextField marginField = new JTextField(5);
    private final JTextField priceField = new JTextField(6);
    private final JTextField stockField = new JTextField(5);

    // Desplegables para seleccionar familia y proveedor
    private final JComboBox<String> familyDropdown;
    private final JComboBox<String> providerDropdown;

    // Mapas para almacenar los IDs de familias y proveedores asociados a sus nombres
    private final Map<String, Integer> familyMap = new HashMap<>();
    private final Map<String, Integer> providerMap = new HashMap<>();

    /**
     * Constructor de la clase. Inicializa el formulario y carga datos de la base de datos.
     *
     * @param parentPanel Panel donde se actualizará la tabla de artículos después de la creación.
     */
    public CreateItemForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Artículo");
        setSize(800, 300);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Cargar datos de la base de datos para los desplegables
        loadFamilyCodes();
        loadProviderNames();

        // Inicializar los JComboBox con los valores cargados
        familyDropdown = new JComboBox<>(familyMap.keySet().toArray(new String[0]));
        providerDropdown = new JComboBox<>(providerMap.keySet().toArray(new String[0]));

        // Crear y configurar el formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Agregar los campos al formulario en filas
        addRow(formPanel, gbc, 0, new String[]{"Código:", "Código de Barras:", "Descripción:"},
                new JComponent[]{codeField, barCodeField, descriptionField});
        addRow(formPanel, gbc, 1, new String[]{"Familia:", "Costo:", "Margen:"},
                new JComponent[]{familyDropdown, costField, marginField});
        addRow(formPanel, gbc, 2, new String[]{"Precio:", "Proveedor:", "Stock:"},
                new JComponent[]{priceField, providerDropdown, stockField});

        // Crear botones de acción
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveItem());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Agregar los componentes al formulario principal
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Mostrar el formulario
        setVisible(true);
    }

    /**
     * Método para agregar una fila de etiquetas y campos al formulario.
     *
     * @param panel   Panel donde se agregarán los componentes.
     * @param gbc     Configuración de diseño de GridBagLayout.
     * @param row     Número de fila donde se colocarán los elementos.
     * @param labels  Array con los textos de las etiquetas.
     * @param fields  Array con los campos de entrada correspondientes.
     */
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

    /**
     * Método para guardar un nuevo artículo en la base de datos.
     */
    private void saveItem() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO articulos " +
                     "(codigoArticulo, codigoBarrasArticulo, descripcionArticulo, familiaArticulo, " +
                     "costeArticulo, margenComercialArticulo, pvpArticulo, proveedorArticulo, stockArticulo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            // Obtener valores seleccionados en los JComboBox
            String selectedFamily = (String) familyDropdown.getSelectedItem();
            String selectedProvider = (String) providerDropdown.getSelectedItem();
            int familyId = familyMap.get(selectedFamily);
            int providerId = providerMap.get(selectedProvider);

            // Asignar valores a la consulta SQL
            ps.setString(1, codeField.getText());
            ps.setString(2, barCodeField.getText());
            ps.setString(3, descriptionField.getText());
            ps.setInt(4, familyId);
            ps.setDouble(5, Double.parseDouble(costField.getText()));
            ps.setDouble(6, Double.parseDouble(marginField.getText()));
            ps.setDouble(7, Double.parseDouble(priceField.getText()));
            ps.setInt(8, providerId);
            ps.setInt(9, Integer.parseInt(stockField.getText()));

            // Ejecutar la inserción en la base de datos
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo creado con éxito.");
            dispose();

            // Actualizar la tabla de artículos en la interfaz de usuario
            Item.showItemTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear artículo: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método para cargar los códigos de familias de artículos desde la base de datos.
     */
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

    /**
     * Método para cargar los nombres de los proveedores desde la base de datos.
     */
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