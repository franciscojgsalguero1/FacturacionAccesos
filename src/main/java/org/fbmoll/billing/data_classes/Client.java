package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateClientForm;
import org.fbmoll.billing.dto.AddressDTO;
import org.fbmoll.billing.dto.ClientDTO;
import org.fbmoll.billing.resources.Button;
import org.fbmoll.billing.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Client {
    final JPanel panel;
    final int id;
    final AddressDTO address;
    final ClientDTO personData;
    final double risk;
    final double discount;
    final Button edit;
    final Button delete;

    static final Logger logger = LoggerFactory.getLogger(Client.class);

    public Client(JPanel panel, ActionListener listener, int id, AddressDTO address,
                  ClientDTO personData, double risk, double discount) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.personData = personData;
        this.risk = risk;
        this.discount = discount;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

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
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Cliente");

        String[] filterOptions = {
                "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País", "CIF", "Teléfono", "Email"
        };

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar cliente...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateClientForm(panel));
        JTable table = setupClientTable(clients, listener);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = searchField.getText().trim();
                int columnIndex = filterDropdown.getSelectedIndex() + 1;

                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
            }
        });

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
                "descuentoCliente FROM clientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(new Client(
                        panel, listener,
                        rs.getInt("idCliente"),
                        new AddressDTO(rs.getString("direccionCliente"), rs.getInt("cpCliente"),
                                rs.getString("poblacionCliente"),
                                rs.getString("provinciaCliente"), rs.getString("paisCliente")
                        ),
                        new ClientDTO(rs.getString("nombreCliente"), rs.getString("cifCliente"),
                                rs.getString("telCliente"), rs.getString("emailCliente"),
                                rs.getString("ibanCliente")
                        ),
                        rs.getDouble("riesgoCliente"),
                        rs.getDouble("descuentoCliente")
                ));
            }
        } catch (SQLException e) {
            logger.info(String.format("Error al obtener clientes: %s", e.getMessage()));
        }
        return clients;
    }

    private static JTable setupClientTable(List<Client> clients, ActionListener listener) {
        String[] columnNames = {
                "ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País", "CIF", "Teléfono",
                "Email", "IBAN", "Riesgo", "Descuento",
                Constants.BUTTON_EDIT, Constants.BUTTON_DELETE
        };

        Object[][] data = new Object[clients.size()][columnNames.length];
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            data[i] = new Object[]{
                    c.id, c.getPersonData().getName(), c.getAddress().getStreet(), c.getAddress().getPostCode(),
                    c.getAddress().getTown(), c.getAddress().getProvince(), c.getAddress().getCountry(),
                    c.getPersonData().getCif(), c.getPersonData().getNumber(), c.getPersonData().getEmail(),
                    c.getPersonData().getIban(), c.risk, c.discount, editButton, deleteButton
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 13 || column == 14;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener,
                clients, Constants.CLIENT_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener,
                clients, Constants.CLIENT_DELETE));

        return table;
    }

    public void modifyClientAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Cliente", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField(this.getPersonData().getName());
        JTextField addressField = new JTextField(this.getAddress().getStreet());
        JTextField postCodeField = new JTextField(String.valueOf(this.getAddress().getPostCode()));
        JTextField townField = new JTextField(this.getAddress().getTown());
        JTextField provinceField = new JTextField(this.getAddress().getProvince());
        JTextField countryField = new JTextField(this.getAddress().getCountry());
        JTextField cifField = new JTextField(this.getPersonData().getCif());
        JTextField phoneField = new JTextField(this.getPersonData().getNumber());
        JTextField emailField = new JTextField(this.getPersonData().getEmail());
        JTextField ibanField = new JTextField(this.getPersonData().getIban());
        JTextField riskField = new JTextField(String.valueOf(this.getRisk()));
        JTextField discountField = new JTextField(String.valueOf(this.getDiscount()));

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

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Client updatedClient = new Client(
                        this.panel, listener, this.getId(),
                        new AddressDTO(addressField.getText(), Integer.parseInt(postCodeField.getText()),
                                townField.getText(), provinceField.getText(), countryField.getText()
                        ),
                        new ClientDTO(nameField.getText(), cifField.getText(), phoneField.getText(),
                                emailField.getText(), ibanField.getText()
                        ),
                        Double.parseDouble(riskField.getText()),
                        Double.parseDouble(discountField.getText())
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

    public void modifyClient(JPanel panel, Client newClient, int id) {
        String query = "UPDATE clientes SET nombreCliente = ?, direccionCliente = ?, cpCliente = ?, " +
                "poblacionCliente = ?, provinciaCliente = ?, paisCliente = ?, cifCliente = ?, telCliente = ?, " +
                "emailCliente = ?, ibanCliente = ?, riesgoCliente = ?, descuentoCliente = ? WHERE idCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newClient.getPersonData().getName());
            ps.setString(2, newClient.getAddress().getStreet());
            ps.setInt(3, newClient.getAddress().getPostCode());
            ps.setString(4, newClient.getAddress().getTown());
            ps.setString(5, newClient.getAddress().getProvince());
            ps.setString(6, newClient.getAddress().getCountry());
            ps.setString(7, newClient.getPersonData().getCif());
            ps.setString(8, newClient.getPersonData().getNumber());
            ps.setString(9, newClient.getPersonData().getEmail());
            ps.setString(10, newClient.getPersonData().getIban());
            ps.setDouble(11, newClient.getRisk());
            ps.setDouble(12, newClient.getDiscount());
            ps.setInt(13, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Cliente actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar el cliente. Verifica el ID.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar cliente: " + e.getMessage(),
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

    public void deleteClient(JPanel panel, int id, ActionListener listener) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "¿Estás seguro de que deseas eliminar al cliente con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM clientes WHERE idCliente = ?")) {
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
            Client.showClientTable(panel, listener);
            panel.revalidate();
            panel.repaint();
        });
    }
}