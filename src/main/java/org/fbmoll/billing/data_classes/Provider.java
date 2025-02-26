package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateProviderForm;
import org.fbmoll.billing.dto.AddressDTO;
import org.fbmoll.billing.dto.ProviderDTO;
import org.fbmoll.billing.resources.Button;
import org.fbmoll.billing.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
public class Provider {
    static final Logger logger = LoggerFactory.getLogger(Provider.class);
    final JPanel panel;
    final int id;
    final AddressDTO address;
    final ProviderDTO providerData;
    final Button edit;
    final Button delete;
    final JComboBox<String> countryCombo = new JComboBox<>();

    public Provider(JPanel panel, ActionListener listener, int id, AddressDTO address, ProviderDTO providerData) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.providerData = providerData;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.PROVIDER_EDIT);
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.PROVIDER_DELETE);
            listener.actionPerformed(event);
        });
    }

    public static void showProviderTable(JPanel panel, ActionListener listener) {
        List<Provider> providers = Provider.getAllProviders(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Proveedor");

        String[] filterOptions = {
                "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web"
        };

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar proveedor...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateProviderForm(panel));
        JTable table = setupProviderTable(providers, listener);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }

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

    public static List<Provider> getAllProviders(JPanel panel, ActionListener listener) {
        List<Provider> providers = new ArrayList<>();
        String query = "SELECT idProveedor, nombreProveedor, direccionProveedor, cpProveedor, poblacionProveedor, " +
                "provinciaProveedor, paisProveedor, cifProveedor, telProveedor, emailProveedor, webProveedor " +
                "FROM proveedores";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                providers.add(new Provider(
                        panel, listener,
                        rs.getInt("idProveedor"),
                        new AddressDTO(rs.getString("direccionProveedor"), rs.getInt("cpProveedor"),
                                rs.getString("poblacionProveedor"),
                                rs.getString("provinciaProveedor"),
                                rs.getString("paisProveedor")),
                        new ProviderDTO(rs.getString("nombreProveedor"),
                                rs.getString("cifProveedor"),
                                rs.getString("telProveedor"), rs.getString("emailProveedor"),
                                rs.getString("webProveedor"))
                ));
            }
        } catch (SQLException e) {
            logger.info(String.format("Error al obtener proveedores: %s", e.getMessage()));
        }
        return providers;
    }

    private static JTable setupProviderTable(List<Provider> providers, ActionListener listener) {
        String[] columnNames = {"ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE};

        Object[][] data = new Object[providers.size()][columnNames.length];
        for (int i = 0; i < providers.size(); i++) {
            Provider p = providers.get(i);
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            data[i] = new Object[]{
                    p.id, p.getProviderData().getName(), p.getAddress().getStreet(), p.getAddress().getPostCode(),
                    p.getAddress().getTown(), p.getAddress().getProvince(), p.getAddress().getCountry(),
                    p.getProviderData().getCif(), p.getProviderData().getNumber(), p.getProviderData().getEmail(),
                    p.getProviderData().getWebsite(), editButton, deleteButton
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 11 || column == 12;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener, providers,
                Constants.PROVIDER_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, providers,
                Constants.PROVIDER_DELETE));

        return table;
    }

    public void modifyProviderAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Proveedor", true);
        dialog.setSize(800, 300);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(this.getProviderData().getName());
        JTextField addressField = new JTextField(this.getAddress().getStreet());
        JTextField postCodeField = new JTextField(String.valueOf(this.getAddress().getPostCode()));
        JTextField townField = new JTextField(this.getAddress().getTown());
        JTextField provinceField = new JTextField(this.getAddress().getProvince());
        JTextField countryField = new JTextField(this.getAddress().getCountry());
        JTextField cifField = new JTextField(this.getProviderData().getCif());
        JTextField phoneField = new JTextField(this.getProviderData().getNumber());
        JTextField emailField = new JTextField(this.getProviderData().getEmail());
        JTextField websiteField = new JTextField(this.getProviderData().getWebsite());

        loadCountries();

        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryCombo, "Código Postal:", postCodeField},
                {"CIF:", cifField, "Teléfono:", phoneField},
                {"Email:", emailField, "Web:", websiteField}
        };

        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Provider updatedProvider = new Provider(
                        this.panel, listener, this.getId(),
                        new AddressDTO(addressField.getText(), Integer.parseInt(postCodeField.getText()),
                                townField.getText(), provinceField.getText(), countryField.getText()),
                        new ProviderDTO(nameField.getText(), cifField.getText(), phoneField.getText(),
                                emailField.getText(), websiteField.getText())
                );

                this.modifyProvider(panel, updatedProvider, this.getId());
                JOptionPane.showMessageDialog(dialog, "Proveedor actualizado con éxito.");
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    panel.removeAll();
                    Provider.showProviderTable(panel, listener);
                    panel.revalidate();
                    panel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar proveedor: " + ex.getMessage(),
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

    private void loadCountries() {
        String query = "SELECT name FROM countries ORDER BY name";
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                countryCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.info("Error al cargar países.");
        }
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.5;
        panel.add(new JLabel(label1), gbc);
        gbc.gridx = 1;
        panel.add(comp1, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel(label2), gbc);
        gbc.gridx = 3;
        panel.add(comp2, gbc);
    }

    public void modifyProvider(JPanel panel, Provider updatedProvider, int id) {
        String query = "UPDATE proveedores SET nombreProveedor = ?, direccionProveedor = ?, cpProveedor = ?, " +
                "poblacionProveedor = ?, provinciaProveedor = ?, paisProveedor = ?, cifProveedor = ?, " +
                "telProveedor = ?, emailProveedor = ?, webProveedor = ? WHERE idProveedor = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, updatedProvider.getProviderData().getName());
            ps.setString(2, updatedProvider.getAddress().getStreet());
            ps.setInt(3, updatedProvider.getAddress().getPostCode());
            ps.setString(4, updatedProvider.getAddress().getTown());
            ps.setString(5, updatedProvider.getAddress().getProvince());
            ps.setString(6, updatedProvider.getAddress().getCountry());
            ps.setString(7, updatedProvider.getProviderData().getCif());
            ps.setString(8, updatedProvider.getProviderData().getNumber());
            ps.setString(9, updatedProvider.getProviderData().getEmail());
            ps.setString(10, updatedProvider.getProviderData().getWebsite());
            ps.setInt(11, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Proveedor actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar el proveedor.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar proveedor: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showProviderTable(panel, e -> {}));
    }

    public void deleteProvider(JPanel panel, int id, ActionListener listener) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "¿Estás seguro de que deseas eliminar al proveedor con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM proveedores WHERE idProveedor = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Proveedor eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se encontró un proveedor con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar proveedor: " + e.getMessage(),
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