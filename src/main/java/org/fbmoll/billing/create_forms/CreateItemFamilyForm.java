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
    private final JTextField codeField = new JTextField(6);
    private final JTextField descriptionField = new JTextField(20);

    public CreateItemFamilyForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Familia de Artículos");
        setSize(400, 200);
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
        addLabelAndField(formPanel, gbc, "Código:", codeField, y++);
        addLabelAndField(formPanel, gbc, "Descripción:", descriptionField, y);

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

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, JTextField field1, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.5;
        panel.add(new JLabel(label1), gbc);

        gbc.gridx = 1;
        panel.add(field1, gbc);
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