package org.facturacion.create_forms;

import org.facturacion.data_classes.ItemFamily;
import org.facturacion.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase para la creación de una nueva familia de artículos.
 * Esta clase proporciona un formulario donde el usuario puede ingresar
 * un código y una descripción para una nueva familia de artículos en la base de datos.
 */
public class CreateItemFamilyForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField codeField = new JTextField(6);
    private final JTextField descriptionField = new JTextField(20);

    /**
     * Constructor que inicializa el formulario para crear una nueva familia de artículos.
     *
     * @param parentPanel Panel donde se actualizará la tabla de familias de artículos.
     */
    public CreateItemFamilyForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        initializeForm();
    }

    /**
     * Inicializa y configura el formulario.
     */
    private void initializeForm() {
        setTitle("Crear Familia de Artículos");
        setSize(400, 200);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = buildFormPanel();
        JPanel buttonPanel = buildButtonPanel();

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
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

        addLabelAndField(formPanel, gbc, "Código:", codeField, 0);
        addLabelAndField(formPanel, gbc, "Descripción:", descriptionField, 1);

        return formPanel;
    }

    /**
     * Construye el panel de botones con las acciones de Guardar y Cancelar.
     */
    private JPanel buildButtonPanel() {
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveItemFamily());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Agrega una etiqueta y un campo de texto al formulario.
     */
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, JTextField field1, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.5;
        panel.add(new JLabel(label1), gbc);

        gbc.gridx = 1;
        panel.add(field1, gbc);
    }

    /**
     * Guarda la familia de artículos en la base de datos.
     */
    private void saveItemFamily() {
        if (!validateFields()) return;

        String query = "INSERT INTO familiaarticulos (codigoFamiliaArticulos, denominacionFamilias) VALUES (?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, codeField.getText().trim());
            ps.setString(2, descriptionField.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Familia de artículos creada con éxito.");
            dispose();
            ItemFamily.showItemFamilyTable(parentPanel, e -> {});
        } catch (SQLException e) {
            showError("Error al crear familia de artículos: ", e);
        }
    }

    /**
     * Valida que los campos no estén vacíos antes de guardar.
     */
    private boolean validateFields() {
        boolean vacio = true;
        if (codeField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            vacio = false;
        }
        return vacio;
    }

    /**
     * Muestra un mensaje de error en caso de fallo en la inserción.
     */
    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}