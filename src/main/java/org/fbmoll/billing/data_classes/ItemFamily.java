package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateItemFamilyForm;
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
public class ItemFamily {
    final JPanel panel;
    final int id;
    final String code;
    final String description;
    final Button edit;
    final Button delete;

    static final Logger logger = LoggerFactory.getLogger(ItemFamily.class);

    public ItemFamily(JPanel panel, ActionListener listener, int id, String code, String description) {
        this.panel = panel;
        this.id = id;
        this.code = code;
        this.description = description;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.FAMILY_EDIT);
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.FAMILY_DELETE);
            listener.actionPerformed(event);
        });
    }

    public static void showItemFamilyTable(JPanel panel, ActionListener listener) {
        List<ItemFamily> families = getAllItemFamilies(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Familia");

        String[] filterOptions = {"Código", "Descripción"};
        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar familia...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateItemFamilyForm(panel));
        JTable table = setupItemFamilyTable(families, listener);
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

    private static JTable setupItemFamilyTable(List<ItemFamily> families, ActionListener listener) {
        String[] columnNames = {"ID", "Código", "Descripción", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE};

        Object[][] data = new Object[families.size()][columnNames.length];
        for (int i = 0; i < families.size(); i++) {
            ItemFamily f = families.get(i);
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);
            data[i] = new Object[]{f.id, f.code, f.description, editButton, deleteButton};
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener, families,
                Constants.FAMILY_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, families,
                Constants.FAMILY_DELETE));

        return table;
    }

    public static List<ItemFamily> getAllItemFamilies(JPanel panel, ActionListener listener) {
        List<ItemFamily> families = new ArrayList<>();
        String query = "SELECT idFamiliaArticulos, codigoFamiliaArticulos, denominacionFamilias FROM familiaarticulos";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                families.add(new ItemFamily(
                        panel, listener,
                        rs.getInt("idFamiliaArticulos"),
                        rs.getString("codigoFamiliaArticulos"),
                        rs.getString("denominacionFamilias")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al obtener familias de artículos: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
        return families;
    }

    public void modifyItemFamily(JPanel panel, ItemFamily updatedFamily, int id) {
        String query = "UPDATE familiaarticulos SET codigoFamiliaArticulos = ?, " +
                "denominacionFamilias = ? WHERE idFamiliaArticulos = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, updatedFamily.getCode());
            ps.setString(2, updatedFamily.getDescription());
            ps.setInt(3, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Familia de artículos actualizada con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar la familia de artículos.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar familia de artículos: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showItemFamilyTable(panel, e -> {}));
    }

    public void deleteItemFamily(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "¿Estás seguro de que deseas eliminar la familia con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM familiaarticulos" +
                     " WHERE idFamiliaArticulos = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Familia de artículos eliminada con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se encontró una familia con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar familia de artículos: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showItemFamilyTable(panel, e -> {}));
    }

    public void modifyItemFamilyAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Familia de Artículos", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField codeField = new JTextField(this.getCode());
        JTextField descriptionField = new JTextField(this.getDescription());

        formPanel.add(new JLabel("Código:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                ItemFamily updatedFamily = new ItemFamily(
                        this.panel, listener, this.getId(),
                        codeField.getText(), descriptionField.getText()
                );

                this.modifyItemFamily(panel, updatedFamily, this.getId());
                JOptionPane.showMessageDialog(dialog, "Familia de artículos actualizada con éxito.");
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    panel.removeAll();
                    ItemFamily.showItemFamilyTable(panel, listener);
                    panel.revalidate();
                    panel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar: " + ex.getMessage(),
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