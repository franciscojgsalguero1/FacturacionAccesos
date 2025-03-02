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
    private final JPanel parentPanel; // Panel donde se actualizará la tabla de tipos de IVA.

    // Campos de entrada de datos
    private final JTextField amountField = new JTextField(6); // Campo para el porcentaje de IVA.
    private final JTextField descriptionField = new JTextField(20); // Campo para la descripción.

    /**
     * Constructor de la clase. Inicializa el formulario.
     *
     * @param parentPanel Panel donde se actualizará la tabla de tipos de IVA después de la creación.
     */
    public CreateIVATypesForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Tipo de IVA");
        setSize(500, 200);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Crear el panel del formulario con disposición en cuadrícula
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Agregar los campos al formulario
        addLabelAndField(formPanel, gbc, "Porcentaje de IVA:", amountField, 0);
        addLabelAndField(formPanel, gbc, "Descripción:", descriptionField, 1);

        // Crear botones de acción
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveIVATypes());

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
     * Método para agregar una etiqueta y un campo de entrada al formulario.
     *
     * @param panel   Panel donde se agregarán los componentes.
     * @param gbc     Configuración de diseño de GridBagLayout.
     * @param label   Texto de la etiqueta.
     * @param field   Campo de entrada correspondiente.
     * @param row     Número de fila donde se colocarán los elementos.
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
     * Método para guardar un nuevo tipo de IVA en la base de datos.
     */
    private void saveIVATypes() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO tiposiva (iva, observacionesTipoIva) VALUES (?, ?)")) {

            // Asignar valores a la consulta SQL
            ps.setDouble(1, Double.parseDouble(amountField.getText())); // Porcentaje de IVA.
            ps.setString(2, descriptionField.getText()); // Descripción.

            // Ejecutar la inserción en la base de datos
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tipo de IVA creado con éxito.");
            dispose();

            // Actualizar la tabla de tipos de IVA en la interfaz de usuario
            IVATypes.showIVATypesTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear tipo de IVA: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}