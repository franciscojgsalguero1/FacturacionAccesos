package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateIVATypesForm;
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
public class IVATypes {
    final JPanel panel;
    final int id;
    final double amount;
    final String description;
    final Button edit;
    final Button delete;

    static final Logger logger = LoggerFactory.getLogger(IVATypes.class);

    public IVATypes(JPanel panel, ActionListener listener, int id, double amount, String description) {
        this.panel = panel;
        this.id = id;
        this.amount = amount;
        this.description = description;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.IVA_EDIT);
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.IVA_DELETE);
            listener.actionPerformed(event);
        });
    }

    public static void showIVATypesTable(JPanel panel, ActionListener listener) {
        List<IVATypes> ivaTypes = getAllIVATypes(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Tipo de IVA");

        String[] filterOptions = {"Porcentaje", "Descripción"};
        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar tipo de IVA...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateIVATypesForm(panel));
        JTable table = setupIVATypesTable(ivaTypes, listener);
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

    private static JTable setupIVATypesTable(List<IVATypes> ivaTypes, ActionListener listener) {
        String[] columnNames = {"ID", "Porcentaje", "Descripción", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE};

        Object[][] data = new Object[ivaTypes.size()][columnNames.length];
        for (int i = 0; i < ivaTypes.size(); i++) {
            IVATypes iva = ivaTypes.get(i);
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);
            data[i] = new Object[]{iva.id, iva.amount, iva.description, editButton, deleteButton};
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
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener, ivaTypes,
                Constants.IVA_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, ivaTypes,
                Constants.IVA_DELETE));

        return table;
    }

    public static List<IVATypes> getAllIVATypes(JPanel panel, ActionListener listener) {
        List<IVATypes> ivaTypes = new ArrayList<>();
        String query = "SELECT idTipoIva, iva, observacionesTipoIva FROM tiposiva";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ivaTypes.add(new IVATypes(
                        panel, listener,
                        rs.getInt("idTipoIva"),
                        rs.getDouble("iva"),
                        rs.getString("observacionesTipoIva")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al obtener tipos de IVA: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
        return ivaTypes;
    }

    public void modifyIVATypes(JPanel panel, IVATypes updatedIVA, int id) {
        String query = "UPDATE tiposiva SET iva = ?, observacionesTipoIva = ? WHERE idTipoIva = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, updatedIVA.getAmount());
            ps.setString(2, updatedIVA.getDescription());
            ps.setInt(3, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Tipo de IVA actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar el tipo de IVA.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar tipo de IVA: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showIVATypesTable(panel, e -> {}));
    }

    public void deleteIVATypes(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "¿Estás seguro de que deseas eliminar el tipo de IVA con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tiposiva WHERE idTipoIva = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Tipo de IVA eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se encontró un tipo de IVA con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar tipo de IVA: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showIVATypesTable(panel, e -> {}));
    }

    public void modifyIVATypesAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Tipo de IVA", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        JTextField amountField = new JTextField(String.valueOf(this.getAmount()));
        JTextField descriptionField = new JTextField(this.getDescription());

        formPanel.add(new JLabel("Porcentaje de IVA:"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                IVATypes updatedIVA = new IVATypes(
                        this.panel, listener, this.getId(),
                        Double.parseDouble(amountField.getText()), descriptionField.getText()
                );

                this.modifyIVATypes(panel, updatedIVA, this.getId());
                JOptionPane.showMessageDialog(dialog, "Tipo de IVA actualizado con éxito.");
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    panel.removeAll();
                    IVATypes.showIVATypesTable(panel, listener);
                    panel.revalidate();
                    panel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar tipo de IVA: " + ex.getMessage(),
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