package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.IVATypes;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateIVATypesForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField amountField = new JTextField(6);
    private final JTextField descriptionField = new JTextField(20);

    public CreateIVATypesForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Tipo de IVA");
        setSize(400, 200);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        addLabelAndField(formPanel, gbc, "Porcentaje de IVA:", amountField, 0);
        addLabelAndField(formPanel, gbc, "Descripción:", descriptionField, 1);

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveIVATypes());

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

    private void saveIVATypes() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO tiposiva (iva, observacionesTipoIva) VALUES (?, ?)")) {

            ps.setDouble(1, Double.parseDouble(amountField.getText()));
            ps.setString(2, descriptionField.getText());

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Tipo de IVA creado con éxito.");
            dispose();
            IVATypes.showIVATypesTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear tipo de IVA: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}