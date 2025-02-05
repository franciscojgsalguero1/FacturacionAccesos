package org.fbmoll.billing.crud;

import org.fbmoll.billing.resources.Constants;
import org.fbmoll.billing.resources.Queries;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericCreateForm extends JFrame {
    private final JPanel formPanel = new JPanel(new GridBagLayout());
    private final Map<String, JTextField> inputFields = new LinkedHashMap<>();
    private final String entityType;
    private final JPanel parentPanel;
    private JComboBox<String> workerDropdown;  // Worker selection for invoices

    public GenericCreateForm(String entityType, JPanel parentPanel) {
        this.entityType = entityType;
        this.parentPanel = parentPanel;

        setTitle(Constants.BUTTON_CREATE + entityType);
        setSize(600, 800);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        generateFormFields(entityType);

        JButton submitButton = new JButton(Constants.BUTTON_SAVE);
        submitButton.addActionListener(this::handleSubmit);
        formPanel.add(submitButton, setGBC(1, inputFields.size(), 2));

        add(formPanel);
        setVisible(true);
    }

    private void generateFormFields(String entityType) {
        String[][] fieldData;

        switch (entityType) {
            case Constants.FIELD_CLIENTS -> fieldData = new String[][]{
                    {Constants.FIELD_CLIENT_NAME, Constants.LABEL_CLIENT_NAME},
                    {Constants.FIELD_CLIENT_ADDRESS, Constants.LABEL_CLIENT_ADDRESS},
                    {Constants.FIELD_CLIENT_POSTCODE, Constants.LABEL_CLIENT_POSTCODE},
                    {Constants.FIELD_CLIENT_CITY, Constants.LABEL_CLIENT_CITY},
                    {Constants.FIELD_CLIENT_PROVINCE, Constants.LABEL_CLIENT_PROVINCE},
                    {Constants.FIELD_CLIENT_COUNTRY, Constants.LABEL_CLIENT_COUNTRY},
                    {Constants.FIELD_CLIENT_CIF, Constants.LABEL_CLIENT_CIF},
                    {Constants.FIELD_CLIENT_PHONE, Constants.LABEL_CLIENT_PHONE},
                    {Constants.FIELD_CLIENT_EMAIL, Constants.LABEL_CLIENT_EMAIL},
                    {Constants.FIELD_CLIENT_IBAN, Constants.LABEL_CLIENT_IBAN},
                    {Constants.FIELD_CLIENT_RISK, Constants.LABEL_CLIENT_RISK},
                    {Constants.FIELD_CLIENT_DISCOUNT, Constants.LABEL_CLIENT_DISCOUNT},
                    {Constants.FIELD_CLIENT_NOTES, Constants.LABEL_CLIENT_NOTES}
            };

            case Constants.FIELD_INVOICES, Constants.FIELD_CORRECTIVE -> {
                fieldData = new String[][]{
                        {Constants.FIELD_INVOICE_NUMBER, Constants.LABEL_INVOICE_NUMBER},
                        {Constants.FIELD_INVOICE_DATE, Constants.LABEL_INVOICE_DATE},
                        {Constants.FIELD_INVOICE_CLIENT_ID, Constants.LABEL_INVOICE_CLIENT_ID},
                        {Constants.FIELD_INVOICE_TAXABLE_AMOUNT, Constants.LABEL_INVOICE_TAXABLE_AMOUNT},
                        {Constants.FIELD_INVOICE_VAT, Constants.LABEL_INVOICE_VAT},
                        {Constants.FIELD_INVOICE_TOTAL, Constants.LABEL_INVOICE_TOTAL},
                        {Constants.FIELD_INVOICE_HASH, Constants.LABEL_INVOICE_HASH},
                        {Constants.FIELD_INVOICE_QR, Constants.LABEL_INVOICE_QR},
                        {Constants.FIELD_INVOICE_PAID, Constants.LABEL_INVOICE_PAID},
                        {Constants.FIELD_INVOICE_PAYMENT_METHOD, Constants.LABEL_INVOICE_PAYMENT_METHOD},
                        {Constants.FIELD_INVOICE_PAYMENT_DATE, Constants.LABEL_INVOICE_PAYMENT_DATE},
                        {Constants.FIELD_INVOICE_NOTES, Constants.LABEL_INVOICE_NOTES}
                };

                // Dropdown for selecting worker
                JLabel workerLabel = new JLabel(Constants.FIELD_WORKERS);
                workerDropdown = new JComboBox<>(getWorkerNames());
                formPanel.add(workerLabel, setGBC(0, fieldData.length, 1));
                formPanel.add(workerDropdown, setGBC(1, fieldData.length, 3));
            }

            default -> {
                JOptionPane.showMessageDialog(this, Constants.ERROR_FORM_NOT_AVAILABLE + entityType,
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        }

        for (int i = 0; i < fieldData.length; i++) {
            String dbField = fieldData[i][0];
            String label = fieldData[i][1];

            JTextField textField = new JTextField(20);
            inputFields.put(dbField, textField);

            formPanel.add(new JLabel(label + ":"), setGBC(0, i, 1));
            formPanel.add(textField, setGBC(1, i, 3));
        }
    }

    private void handleSubmit(ActionEvent e) {
        String tableName = getTableName(entityType);
        if (tableName == null) {
            JOptionPane.showMessageDialog(this, Constants.ERROR_FORM_NOT_AVAILABLE + entityType,
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = Utils.getConnection()) {
            String sql = generateInsertQuery(tableName);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 1;
                for (JTextField field : inputFields.values()) {
                    ps.setString(index++, field.getText());
                }

                // Add workerId to invoices
                if (entityType.equals(Constants.FIELD_INVOICES) || entityType.equals(Constants.FIELD_CORRECTIVE)) {
                    ps.setInt(index, getSelectedWorkerId());
                }

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, entityType + Constants.SUCCESS_CREATED,
                            Constants.SUCCESS, JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    refreshView();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, Constants.ERROR_FORM_SAVING
                            + entityType + ": " + ex.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] getWorkerNames() {
        List<String> workers = Queries.queryGetWorkerNames();
        return workers.toArray(new String[0]);
    }

    private int getSelectedWorkerId() {
        return Queries.queryGetWorkerId(workerDropdown.getSelectedItem().toString());
    }

    private String generateInsertQuery(String tableName) {
        String columns = String.join(", ", inputFields.keySet()) + ", workerId";
        String values = String.join(", ", inputFields.keySet()).replaceAll("[^,]+", "?") + ", ?";
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
    }

    private String getTableName(String entityType) {
        return switch (entityType) {
            case Constants.FIELD_CLIENTS -> Constants.TABLE_CLIENTS;
            case Constants.FIELD_INVOICES -> Constants.TABLE_INVOICES;
            case Constants.FIELD_ARTICLES -> Constants.TABLE_ARTICLES;
            case Constants.FIELD_PROVIDERS -> Constants.TABLE_PROVIDERS;
            case Constants.FIELD_FAMILIES -> Constants.TABLE_FAMILIES;
            case Constants.FIELD_IVA_TYPES -> Constants.TABLE_IVA_TYPES;
            default -> null;
        };
    }

    private void refreshView() throws SQLException {
        GenericTableView.showTable(parentPanel, Queries.queryGetAllClients(), entityType);
    }

    private GridBagConstraints setGBC(int x, int y, int gw) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = gw;
        return gbc;
    }
}