package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Worker;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateWorkerForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(10);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    private final JTextField countryField = new JTextField(20);
    private final JTextField dniField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextField emailField = new JTextField(25);
    private final JTextField positionField = new JTextField(20);
    private final JTextField salaryField = new JTextField(10);
    private final JTextField commissionField = new JTextField(10);

    public CreateWorkerForm(JPanel parentPanel) {
        this.parentPanel = parentPanel;
        setTitle("Crear Trabajador");
        setSize(400, 500);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Dirección:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Código Postal:"));
        formPanel.add(postCodeField);
        formPanel.add(new JLabel("Ciudad:"));
        formPanel.add(townField);
        formPanel.add(new JLabel("Provincia:"));
        formPanel.add(provinceField);
        formPanel.add(new JLabel("País:"));
        formPanel.add(countryField);
        formPanel.add(new JLabel("DNI:"));
        formPanel.add(dniField);
        formPanel.add(new JLabel("Teléfono:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Puesto:"));
        formPanel.add(positionField);
        formPanel.add(new JLabel("Salario:"));
        formPanel.add(salaryField);
        formPanel.add(new JLabel("Comisión %:"));
        formPanel.add(commissionField);

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> saveWorker());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void saveWorker() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO workers (name, address, postCode, town, province, country, dni, phone, " +
                             "email, position, salary, commissionPercentage) VALUES " +
                             "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setString(1, nameField.getText());
            ps.setString(2, addressField.getText());
            ps.setInt(3, Integer.parseInt(postCodeField.getText()));
            ps.setString(4, townField.getText());
            ps.setString(5, provinceField.getText());
            ps.setString(6, countryField.getText());
            ps.setString(7, dniField.getText());
            ps.setString(8, phoneField.getText());
            ps.setString(9, emailField.getText());
            ps.setString(10, positionField.getText());
            ps.setDouble(11, Double.parseDouble(salaryField.getText()));
            ps.setDouble(12, Double.parseDouble(commissionField.getText()));

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Trabajador creado con éxito.");
            dispose();
            Worker.showWorkerTable(parentPanel, e -> {});
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al crear trabajador: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}