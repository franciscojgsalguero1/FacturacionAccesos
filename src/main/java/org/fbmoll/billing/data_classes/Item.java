package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateItemForm;
import org.fbmoll.billing.dto.ItemDTO;
import org.fbmoll.billing.resources.*;
import org.fbmoll.billing.resources.Button;
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
public class Item {
    static final Logger logger = LoggerFactory.getLogger(Item.class);
    final JPanel panel;
    final int id;
    final ItemDTO itemData;
    final String description;
    final String familyId;
    final Button edit;
    final Button delete;

    public Item(JPanel panel, ActionListener listener, int id, ItemDTO itemData, String description, String family) {
        this.panel = panel;
        this.id = id;
        this.itemData = itemData;
        this.description = description;
        this.familyId = family;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.ARTICLE_EDIT);
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.ARTICLE_DELETE);
            listener.actionPerformed(event);
        });
    }

    public static void showItemTable(JPanel panel, ActionListener listener) {
        List<Item> items = Item.getAllItems(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Artículo");

        String[] filterOptions = {"Código", "Código de Barras", "Descripción", "Familia", "Costo", "Margen",
                "Precio", "Proveedor", "Stock"};

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar artículo...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateItemForm(panel));
        JTable table = setupItemTable(items, listener, panel);
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

    public static List<Item> getAllItems(JPanel panel, ActionListener listener) {
        List<Item> items = new ArrayList<>();
        String query = "SELECT a.idArticulo, a.codigoArticulo, a.codigoBarrasArticulo, a.descripcionArticulo, " +
                "f.denominacionFamilias AS familiaNombre, a.costeArticulo, a.margenComercialArticulo, " +
                "a.pvpArticulo, COALESCE(p.nombreProveedor, 'N/A') AS proveedorNombre, a.stockArticulo " +
                "FROM articulos a " +
                "LEFT JOIN familiaarticulos f ON a.familiaArticulo = f.idFamiliaArticulos " +
                "LEFT JOIN proveedores p ON a.proveedorArticulo = p.idProveedor";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(new Item(
                        panel, listener,
                        rs.getInt("idArticulo"),
                        new ItemDTO(
                                rs.getString("codigoArticulo"),
                                rs.getString("codigoBarrasArticulo"),
                                rs.getDouble("costeArticulo"),
                                rs.getDouble("margenComercialArticulo"),
                                rs.getDouble("pvpArticulo"),
                                rs.getString("proveedorNombre"), // Now stores supplier name
                                rs.getInt("stockArticulo")
                        ),
                        rs.getString("descripcionArticulo"),
                        rs.getString("familiaNombre") // Now stores family name
                ));
            }
        } catch (SQLException e) {
            logger.info(String.format("Error al obtener artículos: %s", e.getMessage()));
        }
        return items;
    }

    private static JTable setupItemTable(List<Item> items, ActionListener listener, JPanel panel) {
        String[] columnNames = {"ID", "Código", "Código de Barras", "Descripción", "Familia", "Costo", "Margen",
                "Precio", "Proveedor", "Stock", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE};

        Object[][] data = new Object[items.size()][columnNames.length];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            editButton.addActionListener(e -> item.modifyItemAction(panel, listener));
            deleteButton.addActionListener(e -> item.deleteItem(panel, item.getId()));

            data[i] = new Object[]{
                    item.id, item.getItemData().getCode(), item.getItemData().getBarCode(), item.description,
                    item.familyId, // This now contains the name instead of the ID
                    item.getItemData().getCost(), item.getItemData().getMargin(),
                    item.getItemData().getPrice(), item.getItemData().getSupplier(), // This contains the supplier name
                    item.getItemData().getStock(),
                    editButton, deleteButton
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10 || column == 11;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(new JCheckBox(),
                listener, items, panel, Constants.ARTICLE_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(new JCheckBox(),
                listener, items, panel, Constants.ARTICLE_DELETE));

        return table;
    }

    public void deleteItem(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "¿Estás seguro de que deseas eliminar el artículo con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM articulos WHERE idArticulo = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Artículo eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se encontró un artículo con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar artículo: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            Item.showItemTable(panel, e -> {});
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyItem(JPanel panel, Item updatedItem, int id) {
        String query = "UPDATE articulos SET codigoArticulo = ?, codigoBarrasArticulo = ?, descripcionArticulo = ?, " +
                "familiaArticulo = ?, costeArticulo = ?, margenComercialArticulo = ?, pvpArticulo = ?, " +
                "proveedorArticulo = ?, stockArticulo = ? WHERE idArticulo = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, updatedItem.getItemData().getCode());
            ps.setString(2, updatedItem.getItemData().getBarCode());
            ps.setString(3, updatedItem.getDescription());
            ps.setString(4, updatedItem.getFamilyId());
            ps.setDouble(5, updatedItem.getItemData().getCost());
            ps.setDouble(6, updatedItem.getItemData().getMargin());
            ps.setDouble(7, updatedItem.getItemData().getPrice());
            ps.setString(8, updatedItem.getItemData().getSupplier());
            ps.setInt(9, updatedItem.getItemData().getStock());
            ps.setInt(10, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Artículo actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar el artículo. Verifica el ID.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar artículo: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            Item.showItemTable(panel, e -> {});
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyItemAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Artículo", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2));

        JTextField codeField = new JTextField(this.getItemData().getCode());
        JTextField barCodeField = new JTextField(this.getItemData().getBarCode());
        JTextField descriptionField = new JTextField(this.getDescription());
        JTextField familyIdField = new JTextField(String.valueOf(this.getFamilyId()));
        JTextField costField = new JTextField(String.valueOf(this.getItemData().getCost()));
        JTextField marginField = new JTextField(String.valueOf(this.getItemData().getMargin()));
        JTextField priceField = new JTextField(String.valueOf(this.getItemData().getPrice()));
        JTextField supplierField = new JTextField(String.valueOf(this.getItemData().getSupplier()));
        JTextField stockField = new JTextField(String.valueOf(this.getItemData().getStock()));

        formPanel.add(new JLabel("Código:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Código de Barras:"));
        formPanel.add(barCodeField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Familia:"));
        formPanel.add(familyIdField);
        formPanel.add(new JLabel("Costo:"));
        formPanel.add(costField);
        formPanel.add(new JLabel("Margen:"));
        formPanel.add(marginField);
        formPanel.add(new JLabel("Precio:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Proveedor:"));
        formPanel.add(supplierField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Item updatedItem = new Item(
                        this.panel, listener, this.getId(),
                        new ItemDTO(
                                codeField.getText(), barCodeField.getText(), Double.parseDouble(costField.getText()),
                                Double.parseDouble(marginField.getText()), Double.parseDouble(priceField.getText()),
                                supplierField.getText(), Integer.parseInt(stockField.getText())
                        ),
                        descriptionField.getText(),
                        familyIdField.getText()
                );

                this.modifyItem(panel, updatedItem, this.getId());
                JOptionPane.showMessageDialog(dialog, "Artículo actualizado con éxito.");
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    panel.removeAll();
                    Item.showItemTable(panel, listener);
                    panel.revalidate();
                    panel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar artículo: " + ex.getMessage(),
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