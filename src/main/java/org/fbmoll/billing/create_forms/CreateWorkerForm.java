package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.data_classes.Worker;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateWorkerForm extends JDialog {
    private final JPanel parentPanel;
    private final JTextField nameField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);
    private final JTextField postCodeField = new JTextField(10);
    private final JTextField townField = new JTextField(20);
    private final JTextField provinceField = new JTextField(20);
    private final JComboBox<String> countryCombo = new JComboBox<>();
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
        loadCountries();
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addRow(formPanel, gbc, 0, "Nombre:", nameField);
        addRow(formPanel, gbc, 1, "Dirección:", addressField);
        addRow(formPanel, gbc, 2, "Código Postal:", postCodeField);
        addRow(formPanel, gbc, 3, "Ciudad:", townField);
        addRow(formPanel, gbc, 4, "Provincia:", provinceField);
        addRow(formPanel, gbc, 5, "País:", countryCombo);
        addRow(formPanel, gbc, 6, "DNI:", dniField);
        addRow(formPanel, gbc, 7, "Teléfono:", phoneField);
        addRow(formPanel, gbc, 8, "Email:", emailField);
        addRow(formPanel, gbc, 9, "Puesto:", positionField);
        addRow(formPanel, gbc, 10, "Salario:", salaryField);
        addRow(formPanel, gbc, 11, "Comisión %:", commissionField);

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

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void loadCountries() {
        String query = "SELECT name FROM countries ORDER BY name";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countryCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar países: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveWorker() {
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO workers (name, address, postCode, town, province, country, dni, phone, email," +
                             " position, salary, commissionPercentage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, nameField.getText());
            ps.setString(2, addressField.getText());
            ps.setInt(3, Integer.parseInt(postCodeField.getText()));
            ps.setString(4, townField.getText());
            ps.setString(5, provinceField.getText());
            ps.setString(6, (String) countryCombo.getSelectedItem());
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
            JOptionPane.showMessageDialog(this, "Error al crear trabajador: "
                    + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}