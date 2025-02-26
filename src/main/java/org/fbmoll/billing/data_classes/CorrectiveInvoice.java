package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateCorrectiveInvoice;
import org.fbmoll.billing.create_forms.CreateInvoiceForm;
import org.fbmoll.billing.dto.InvoicePaymentDTO;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CorrectiveInvoice {
    static final Logger logger = LoggerFactory.getLogger(CorrectiveInvoice.class);
    final JPanel panel;
    final int id;
    final Date date;
    final int clientId;
    final Integer workerId;
    final InvoicePaymentDTO invoicePaymentDTO;
    final Button view;
    final Button edit;
    final Button delete;

    public CorrectiveInvoice(JPanel panel, ActionListener listener, int id, Date date, int clientId, Integer workerId,
                             InvoicePaymentDTO invoicePaymentDTO) {
        this.panel = panel;
        this.id = id;
        this.date = date;
        this.clientId = clientId;
        this.workerId = workerId;
        this.invoicePaymentDTO = invoicePaymentDTO;

        this.view = new Button("Ver");
        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.view.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "VIEW_INVOICE");
            listener.actionPerformed(event);
        });

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "EDIT_INVOICE");
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "DELETE_INVOICE");
            listener.actionPerformed(event);
        });
    }

    public static void showCorrectiveInvoiceTable(JPanel panel, ActionListener listener) {
        List<CorrectiveInvoice> invoices = getCorrectiveInvoices(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton createButton = new JButton("Crear Rectificativa");
        createButton.addActionListener(e -> new CreateCorrectiveInvoice(panel));

        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"ID", "Fecha", "ID Cliente", "ID Trabajador", "Base Imponible", "IVA", "Total"});
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar rectificativa...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        JTable table = setupCorrectiveInvoiceTable(invoices, listener, panel);
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
                int columnIndex = filterDropdown.getSelectedIndex();
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

    public static List<CorrectiveInvoice> getCorrectiveInvoices(JPanel panel, ActionListener listener) {
        List<CorrectiveInvoice> invoices = new ArrayList<>();
        String query = "SELECT * FROM rectificativasclientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(new CorrectiveInvoice(
                        panel, listener, rs.getInt("idRectificativaCliente"),
                        rs.getDate("fechaRectificativaCliente"),
                        rs.getInt("idClienteRectificativaCliente"),
                        rs.getObject("idTrabajadorRectificativaCliente", Integer.class),
                        new InvoicePaymentDTO(
                                rs.getInt("numeroRectificativaCliente"),
                                rs.getDouble("baseImponibleRectificativaCliente"),
                                rs.getDouble("ivaRectificativaCliente"),
                                rs.getDouble("totalRectificativaCliente"),
                                false,false, "", null
                        )
                ));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener rectificativas: " + e.getMessage());
        }
        return invoices;
    }

    private static JTable setupCorrectiveInvoiceTable(List<CorrectiveInvoice> invoices, ActionListener listener, JPanel panel) {
        String[] columnNames = {"ID", "Fecha", "ID Cliente", "ID Trabajador", "Base Imponible", "IVA", "Total", "Ver", "Editar", "Eliminar"};
        Object[][] data = new Object[invoices.size()][columnNames.length];

        for (int i = 0; i < invoices.size(); i++) {
            CorrectiveInvoice inv = invoices.get(i);
            JButton viewButton = new JButton("Ver");
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            data[i] = new Object[]{
                    inv.getId(),
                    inv.getDate(),
                    inv.getClientId(),
                    inv.getWorkerId(),
                    inv.getInvoicePaymentDTO().getTaxableAmount(),
                    inv.getInvoicePaymentDTO().getVatAmount(),
                    inv.getInvoicePaymentDTO().getTotalAmount(),
                    viewButton,
                    editButton,
                    deleteButton
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 7; // Only allow buttons to be interactive
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        // Apply button renderers and editors
        table.getColumn("Ver").setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn("Ver").setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, invoices, panel, Constants.INVOICE_VIEW));
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, invoices, panel, Constants.INVOICE_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, invoices, panel, Constants.INVOICE_DELETE));

        return table;
    }


    public void modifyCorrectiveInvoiceAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel), "Modificar Rectificativa", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        JTextField dateField = new JTextField(this.getDate().toString());
        JTextField clientIdField = new JTextField(String.valueOf(this.getClientId()));
        JTextField workerIdField = new JTextField(String.valueOf(this.getWorkerId()));
        JTextField taxableAmountField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getTaxableAmount()));
        JTextField vatAmountField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getVatAmount()));
        JTextField totalAmountField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getTotalAmount()));
        totalAmountField.setEditable(false);

        formPanel.add(new JLabel("Fecha (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("ID Cliente:"));
        formPanel.add(clientIdField);
        formPanel.add(new JLabel("ID Trabajador:"));
        formPanel.add(workerIdField);
        formPanel.add(new JLabel("Base Imponible:"));
        formPanel.add(taxableAmountField);
        formPanel.add(new JLabel("IVA:"));
        formPanel.add(vatAmountField);
        formPanel.add(new JLabel("Total:"));
        formPanel.add(totalAmountField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                this.modifyCorrectiveInvoice(panel, Date.valueOf(dateField.getText()), Integer.parseInt(clientIdField.getText()),
                        Integer.parseInt(workerIdField.getText()), Double.parseDouble(taxableAmountField.getText()),
                        Double.parseDouble(vatAmountField.getText()), Double.parseDouble(totalAmountField.getText()));
                JOptionPane.showMessageDialog(dialog, "Rectificativa actualizada con éxito.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar rectificativa: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void modifyCorrectiveInvoice(JPanel panel, Date date, int clientId, int workerId, double taxableAmount, double vatAmount, double totalAmount) {
        String query = "UPDATE rectificativasclientes SET fechaRectificativaCliente = ?, idClienteRectificativaCliente = ?, idTrabajadorRectificativaCliente = ?, baseImponibleRectificativaCliente = ?, ivaRectificativaCliente = ?, totalRectificativaCliente = ? WHERE idRectificativaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, date);
            ps.setInt(2, clientId);
            ps.setInt(3, workerId);
            ps.setDouble(4, taxableAmount);
            ps.setDouble(5, vatAmount);
            ps.setDouble(6, totalAmount);
            ps.setInt(7, this.id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Rectificativa actualizada con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar la rectificativa.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar rectificativa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteCorrectiveInvoice(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(panel, "¿Estás seguro de eliminar la rectificativa?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM rectificativasclientes WHERE idRectificativaCliente = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar rectificativa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            showCorrectiveInvoiceTable(panel, e -> {});
            panel.revalidate();
            panel.repaint();
        });
    }
}