package org.facturacion.create_forms;

import org.facturacion.data_classes.IVATypes;
import org.facturacion.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase que representa un formulario para la creación de un nuevo tipo de IVA.
 * Permite ingresar el porcentaje de IVA y una descripción.
 */
public class CreateIVATypesForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField amountField = new JTextField(6);
    private final JTextField descriptionField = new JTextField(20);

    /**
     * Constructor que inicializa el formulario.
     */
    public CreateIVATypesForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        initializeForm();
    }

    /**
     * Configura la interfaz del formulario.
     */
    private void initializeForm() {
        setTitle("Crear Tipo de IVA");
        setSize(500, 200);
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

        addLabelAndField(formPanel, gbc, "Porcentaje de IVA:", amountField, 0);
        addLabelAndField(formPanel, gbc, "Descripción:", descriptionField, 1);
        return formPanel;
    }

    /**
     * Construye el panel de botones.
     */
    private JPanel buildButtonPanel() {
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveIVATypes());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Agrega una etiqueta y un campo de entrada al formulario.
     */
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label, JTextField field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.5;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    /**
     * Guarda un nuevo tipo de IVA en la base de datos.
     */
    private void saveIVATypes() {
        if (!validateFields()) return;

        String query = "INSERT INTO tiposiva (iva, observacionesTipoIva) VALUES (?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, Double.parseDouble(amountField.getText().trim()));
            ps.setString(2, descriptionField.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tipo de IVA creado con éxito.");
            dispose();
            IVATypes.showIVATypesTable(parentPanel, e -> {});
        } catch (SQLException e) {
            showError("Error al crear tipo de IVA: ", e);
        }
    }

    /**
     * Valida que los campos requeridos no estén vacíos y que el porcentaje de IVA sea válido.
     */
    private boolean validateFields() {
        if (amountField.getText().trim().isEmpty() || descriptionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            double iva = Double.parseDouble(amountField.getText().trim());
            if (iva < 0 || iva > 100) {
                JOptionPane.showMessageDialog(this, "El porcentaje de IVA debe estar entre 0 y 100.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un valor numérico válido para el porcentaje de IVA.", "Error", JOptionPane.ERROR_MESSAGE);
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