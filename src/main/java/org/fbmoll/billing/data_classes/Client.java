package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateClientForm;
import org.fbmoll.billing.resources.*;
import org.fbmoll.billing.resources.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@FieldDefaults(level = AccessLevel.PUBLIC)
public class Client {
    JPanel panel;

    int id;
    String name;
    String address;
    int postCode;
    String town;
    String province;
    String country;
    String cif;
    String number;
    String email;
    String iban;
    double risk;
    double discount;
    String description;
    Button edit;
    Button delete;

    static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client(JPanel panel, ActionListener listener, int id, String name, String address, int postCode,
                  String town, String province, String cif, String country, String number, String email,
                  String iban, double risk, double discount, String description) {
        this.panel = panel;
        this.id = id;
        this.name = name;
        this.address = address;
        this.postCode = postCode;
        this.town = town;
        this.country = country;
        this.province = province;
        this.cif = cif;
        this.number = number;
        this.email = email;
        this.iban = iban;
        this.risk = risk;
        this.discount = discount;
        this.description = description;

        this.edit = new Button(Constants.BUTTON_EDIT, "📝");
        this.delete = new Button(Constants.BUTTON_DELETE, "❌");

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.CLIENT_EDIT);
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.CLIENT_DELETE);
            listener.actionPerformed(event);
        });
    }

    public static void showClientTable(JPanel panel, ActionListener listener) {
        List<Client> clients = Client.getClients(panel, listener);
        if (clients.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay clientes disponibles.");
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton createButton = new JButton("Crear Cliente");
        String[] filterOptions = {
                Constants.LABEL_CLIENT_NAME,
                Constants.LABEL_CLIENT_ADDRESS,
                Constants.LABEL_CLIENT_POSTCODE,
                Constants.LABEL_CITY,
                Constants.LABEL_CLIENT_PROVINCE,
                Constants.LABEL_CLIENT_COUNTRY,
                Constants.LABEL_CLIENT_CIF,
                Constants.LABEL_CLIENT_PHONE,
                Constants.LABEL_CLIENT_EMAIL};
        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar cliente...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateClientForm(panel));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterClientTable(panel, listener, filterDropdown, searchField.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterClientTable(panel, listener, filterDropdown, searchField.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterClientTable(panel, listener, filterDropdown, searchField.getText());
            }
        });

        JTable table = setupClientTable(clients, listener, panel);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }


    public static List<Client> getClients(JPanel panel, ActionListener listener) {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT idCliente, nombreCliente, direccionCliente, cpCliente, poblacionCliente, " +
                "provinciaCliente, cifCliente, paisCliente, telCliente, emailCliente, ibanCliente, riesgoCliente, " +
                "descuentoCliente, observacionesCliente FROM clientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(new Client(
                        panel, listener,
                        rs.getInt("idCliente"),
                        rs.getString("nombreCliente"),
                        rs.getString("direccionCliente"),
                        rs.getInt("cpCliente"),
                        rs.getString("poblacionCliente"),
                        rs.getString("provinciaCliente"),
                        rs.getString("cifCliente"),
                        rs.getString("paisCliente"),
                        rs.getString("telCliente"),
                        rs.getString("emailCliente"),
                        rs.getString("ibanCliente"),
                        rs.getDouble("riesgoCliente"),
                        rs.getDouble("descuentoCliente"),
                        rs.getString("observacionesCliente")
                ));
            }
        } catch (SQLException e) {
            logger.info(String.format("Error al obtener clientes: %s", e.getMessage()));
        }
        return clients;
    }

    private static JTable setupClientTable(List<Client> clients, ActionListener listener, JPanel panel) {
        String[] columnNames = {"ID",
                Constants.LABEL_CLIENT_NAME,
                Constants.LABEL_CLIENT_ADDRESS,
                Constants.LABEL_CLIENT_POSTCODE,
                Constants.LABEL_CITY,
                Constants.LABEL_CLIENT_PROVINCE,
                Constants.LABEL_CLIENT_COUNTRY,
                Constants.LABEL_CLIENT_CIF,
                Constants.LABEL_CLIENT_PHONE,
                Constants.LABEL_CLIENT_EMAIL,
                Constants.LABEL_CLIENT_IBAN,
                Constants.LABEL_CLIENT_RISK,
                Constants.LABEL_CLIENT_DISCOUNT,
                Constants.LABEL_CLIENT_NOTES,
                Constants.BUTTON_EDIT,
                Constants.BUTTON_DELETE};

        Object[][] data = new Object[clients.size()][columnNames.length];
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            data[i] = new Object[]{c.id, c.name, c.address, c.postCode, c.town, c.province, c.country, c.cif,
                    c.number, c.email, c.iban, c.risk, c.discount, c.description, c.edit, c.delete};
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 14 || column == 15;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor(new JCheckBox(),
                listener, clients, panel, Constants.CLIENT_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor(new JCheckBox(),
                listener, clients, panel, Constants.CLIENT_DELETE));

        return table;
    }

    private static void filterClientTable(JPanel panel, ActionListener al, JComboBox<String> filter, String query) {
        String selectedFilter = (String) filter.getSelectedItem();
        List<Client> filteredClients = new ArrayList<>();
        List<Client> allClients = Client.getClients(panel, al);

        for (Client client : allClients) {
            String fieldValue = switch (Objects.requireNonNull(selectedFilter)) {
                case Constants.LABEL_CLIENT_NAME -> client.getName();
                case Constants.LABEL_CLIENT_ADDRESS -> client.getAddress();
                case Constants.LABEL_CLIENT_POSTCODE -> String.valueOf(client.getPostCode());
                case Constants.LABEL_CITY -> client.getTown();
                case Constants.LABEL_CLIENT_PROVINCE -> client.getProvince();
                case Constants.LABEL_CLIENT_COUNTRY -> client.getCountry();
                case Constants.LABEL_CLIENT_CIF -> client.getCif();
                case Constants.LABEL_CLIENT_PHONE -> client.getNumber();
                case Constants.LABEL_CLIENT_EMAIL -> client.getEmail();
                default -> "";
            };

            if (fieldValue.toLowerCase().contains(query.toLowerCase())) {
                filteredClients.add(client);
            }
        }

        showFilteredClients(panel, al, filteredClients);
    }

    private static void showFilteredClients(JPanel panel, ActionListener listener, List<Client> clients) {
        String[] columnNames = {"ID",
                Constants.LABEL_CLIENT_NAME,
                Constants.LABEL_CLIENT_ADDRESS,
                Constants.LABEL_CLIENT_POSTCODE,
                Constants.LABEL_CITY,
                Constants.LABEL_CLIENT_PROVINCE,
                Constants.LABEL_CLIENT_COUNTRY,
                Constants.LABEL_CLIENT_CIF,
                Constants.LABEL_CLIENT_PHONE,
                Constants.LABEL_CLIENT_EMAIL,
                Constants.LABEL_CLIENT_IBAN,
                Constants.LABEL_CLIENT_RISK,
                Constants.LABEL_CLIENT_DISCOUNT,
                Constants.LABEL_CLIENT_NOTES,
                Constants.BUTTON_EDIT,
                Constants.BUTTON_DELETE};

        Object[][] data = new Object[clients.size()][columnNames.length];
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            data[i] = new Object[]{c.id, c.name, c.address, c.postCode, c.town, c.province, c.country, c.cif,
                    c.number, c.email, c.iban, c.risk, c.discount, c.description, c.edit, c.delete};
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 14 || column == 15;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor(new JCheckBox(),
                listener, clients, panel, Constants.CLIENT_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor(new JCheckBox(),
                listener, clients, panel, Constants.CLIENT_DELETE));

        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyClient(JPanel panel, Client newClient, int id) {
        String query = "UPDATE clientes SET nombreCliente = ?, direccionCliente = ?, cpCliente = ?, " +
                "poblacionCliente = ?, provinciaCliente = ?, paisCliente = ?, cifCliente = ?, telCliente = ?, " +
                "emailCliente = ?, ibanCliente = ?, riesgoCliente = ?, descuentoCliente = ?, " +
                "observacionesCliente = ? WHERE idCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, newClient.getName());
            ps.setString(2, newClient.getAddress());
            ps.setInt(3, newClient.getPostCode());
            ps.setString(4, newClient.getTown());
            ps.setString(5, newClient.getProvince());
            ps.setString(6, newClient.getCountry());
            ps.setString(7, newClient.getCif());
            ps.setString(8, newClient.getNumber());
            ps.setString(9, newClient.getEmail());
            ps.setString(10, newClient.getIban());
            ps.setDouble(11, newClient.getRisk());
            ps.setDouble(12, newClient.getDiscount());
            ps.setString(13, newClient.getDescription());
            ps.setInt(14, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.info(String.format("Error al modificar clientes: %s", e.getMessage()));
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.revalidate();
            panel.repaint();
        });
    }

    public void deleteClient(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(
                panel,
                "¿Estás seguro de que deseas eliminar al cliente con ID " + id + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String query = "DELETE FROM clientes WHERE idCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Cliente eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se encontró un cliente con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar cliente: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            Client.showClientTable(panel, e -> {
            });
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyClientAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Cliente", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField(this.getName());
        JTextField addressField = new JTextField(this.getAddress());
        JTextField postCodeField = new JTextField(String.valueOf(this.getPostCode()));
        JTextField townField = new JTextField(this.getTown());
        JTextField provinceField = new JTextField(this.getProvince());
        JTextField countryField = new JTextField(this.getCountry());
        JTextField cifField = new JTextField(this.getCif());
        JTextField phoneField = new JTextField(this.getNumber());
        JTextField emailField = new JTextField(this.getEmail());
        JTextField ibanField = new JTextField(this.getIban());
        JTextField riskField = new JTextField(String.valueOf(this.getRisk()));
        JTextField discountField = new JTextField(String.valueOf(this.getDiscount()));
        JTextField descriptionField = new JTextField(this.getDescription());

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
        formPanel.add(new JLabel("CIF:"));
        formPanel.add(cifField);
        formPanel.add(new JLabel("Teléfono:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("IBAN:"));
        formPanel.add(ibanField);
        formPanel.add(new JLabel("Riesgo:"));
        formPanel.add(riskField);
        formPanel.add(new JLabel("Descuento:"));
        formPanel.add(discountField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Client updatedClient = new Client(
                        panel,
                        listener,
                        this.getId(),
                        nameField.getText(),
                        addressField.getText(),
                        Integer.parseInt(postCodeField.getText()),
                        townField.getText(),
                        provinceField.getText(),
                        cifField.getText(),
                        countryField.getText(),
                        phoneField.getText(),
                        emailField.getText(),
                        ibanField.getText(),
                        Double.parseDouble(riskField.getText()),
                        Double.parseDouble(discountField.getText()),
                        descriptionField.getText()
                );

                this.modifyClient(panel, updatedClient, this.getId());
                JOptionPane.showMessageDialog(dialog, "Cliente actualizado con éxito.");
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    panel.removeAll();
                    Client.showClientTable(panel, listener);
                    panel.revalidate();
                    panel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar cliente: " + ex.getMessage(),
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}