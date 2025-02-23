package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.ItemFamily;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateItemFamilyForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField codeField = new JTextField(20);
    private final JTextField descriptionField = new JTextField(20);

    public CreateItemFamilyForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Familia de Artículos");
        setSize(400, 200);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        formPanel.add(new JLabel("Código:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveItemFamily());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void saveItemFamily() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO familiaarticulos (codigoFamiliaArticulos, denominacionFamilias) VALUES (?, ?)")) {

            ps.setString(1, codeField.getText());
            ps.setString(2, descriptionField.getText());

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Familia de artículos creada con éxito.");
            dispose();
            ItemFamily.showItemFamilyTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear familia de artículos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}