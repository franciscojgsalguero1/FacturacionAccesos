package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateProviderForm;
import org.fbmoll.billing.dto.Address;
import org.fbmoll.billing.dto.ProviderData;
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
public class Provider {
    static final Logger logger = LoggerFactory.getLogger(Provider.class);
    final JPanel panel;
    final int id;
    final Address address;
    final ProviderData providerData;
    final Button edit;
    final Button delete;

    public Provider(JPanel panel, ActionListener listener, int id, Address address, ProviderData providerData) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.providerData = providerData;

        this.edit = new Button(Constants.BUTTON_EDIT, "📝");
        this.delete = new Button(Constants.BUTTON_DELETE, "❌");

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
        JTable table = setupProviderTable(providers, listener, panel);
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
                        new Address(rs.getString("direccionProveedor"), rs.getInt("cpProveedor"),
                                rs.getString("poblacionProveedor"),
                                rs.getString("provinciaProveedor"),
                                rs.getString("paisProveedor")),
                        new ProviderData(rs.getString("nombreProveedor"),
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

    private static JTable setupProviderTable(List<Provider> providers, ActionListener listener, JPanel panel) {
        String[] columnNames = {"ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE};

        Object[][] data = new Object[providers.size()][columnNames.length];
        for (int i = 0; i < providers.size(); i++) {
            Provider p = providers.get(i);
            data[i] = new Object[]{
                    p.id, p.getProviderData().getName(), p.getAddress().getStreet(), p.getAddress().getPostCode(),
                    p.getAddress().getTown(), p.getAddress().getProvince(), p.getAddress().getCountry(),
                    p.getProviderData().getCif(), p.getProviderData().getNumber(), p.getProviderData().getEmail(),
                    p.getProviderData().getWebsite(), Constants.BUTTON_EDIT, Constants.BUTTON_DELETE
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
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(new JCheckBox(),
                listener, providers, panel, Constants.PROVIDER_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(new JCheckBox(),
                listener, providers, panel, Constants.PROVIDER_DELETE));

        return table;
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

    public void deleteProvider(JPanel panel, int id) {
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
            Provider.showProviderTable(panel, e -> {});
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyProviderAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Proveedor", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2));

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
        formPanel.add(new JLabel("Web:"));
        formPanel.add(websiteField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Provider updatedProvider = new Provider(
                        this.panel, listener, this.getId(),
                        new Address(addressField.getText(), Integer.parseInt(postCodeField.getText()),
                                townField.getText(), provinceField.getText(), countryField.getText()),
                        new ProviderData(nameField.getText(), cifField.getText(), phoneField.getText(),
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
}