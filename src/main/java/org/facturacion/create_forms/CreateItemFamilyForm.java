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
    // Panel principal donde se actualizará la tabla después de crear una nueva familia de artículos.
    private final JPanel parentPanel;

    // Campos de entrada para el código y la descripción de la familia de artículos.
    private final JTextField codeField = new JTextField(6);
    private final JTextField descriptionField = new JTextField(20);

    /**
     * Constructor que inicializa el formulario para crear una nueva familia de artículos.
     *
     * @param parentPanel Panel donde se actualizará la tabla de familias de artículos.
     */
    public CreateItemFamilyForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Familia de Artículos");
        setSize(400, 200);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Panel principal con disposición de cuadrícula para organizar los campos
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Se añaden los campos al formulario
        addLabelAndField(formPanel, gbc, "Código:", codeField, 0);
        addLabelAndField(formPanel, gbc, "Descripción:", descriptionField, 1);

        // Creación de botones de acción
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveItemFamily());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Agregar los componentes al formulario principal
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Hacer visible el formulario
        setVisible(true);
    }

    /**
     * Agrega una etiqueta y un campo de texto al formulario.
     *
     * @param panel   Panel donde se agregará el componente.
     * @param gbc     Configuración de diseño para la disposición del GridBagLayout.
     * @param label1  Texto de la etiqueta.
     * @param field1  Campo de entrada asociado a la etiqueta.
     * @param row     Fila donde se colocará el componente.
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
     * Si la inserción es exitosa, se actualiza la tabla de familias de artículos en la interfaz de usuario.
     */
    private void saveItemFamily() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO familiaarticulos (codigoFamiliaArticulos," +
                     " denominacionFamilias) VALUES (?, ?)")) {

            // Se establecen los valores en la consulta SQL
            ps.setString(1, codeField.getText());
            ps.setString(2, descriptionField.getText());

            // Ejecutar la inserción en la base de datos
            ps.executeUpdate();

            // Mostrar mensaje de éxito y cerrar el formulario
            JOptionPane.showMessageDialog(this, "Familia de artículos creada con éxito.");
            dispose();

            // Actualizar la tabla en la interfaz de usuario
            ItemFamily.showItemFamilyTable(parentPanel, e -> {});

        } catch (SQLException | NumberFormatException e) {
            // Mostrar mensaje de error en caso de fallo en la inserción
            JOptionPane.showMessageDialog(this,
                    "Error al crear: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}