package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateInvoiceForm;
import org.fbmoll.billing.dto.InvoicePaymentDTO;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {
    static final Logger logger = LoggerFactory.getLogger(Invoice.class);
    final JPanel panel;
    final int id;
    final Date date;
    final int clientId;
    final int workerId;
    final InvoicePaymentDTO invoicePaymentDTO;
    final Button view;
    final Button edit;
    final Button delete;

    public Invoice(JPanel panel, ActionListener listener, int id, Date date, int clientId, int workerId,
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

    public static void showInvoiceTable(JPanel panel, ActionListener listener) {
        List<Invoice> invoices = getInvoices(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // "Crear Factura" Button
        JButton createButton = new JButton("Crear Factura");
        createButton.addActionListener(e -> new CreateInvoiceForm(panel));

        // Filter Options
        String[] filterOptions = {
                "ID", "Número", "Fecha", "ID Cliente", "ID Trabajador",
                "Base Imponible", "IVA", "Total", "Pagada", "Forma de Pago", "Fecha de Pago"
        };

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar factura...");

        // Add components to topPanel
        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        JTable table = setupInvoiceTable(invoices, listener, panel);
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
                int columnIndex = filterDropdown.getSelectedIndex(); // Match dropdown index with table columns
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

    public static List<Invoice> getInvoices(JPanel panel, ActionListener listener) {
        List<Invoice> invoices = new ArrayList<>();
        String query = "SELECT * FROM facturasclientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(new Invoice(
                        panel, listener, rs.getInt("idFacturaCliente"),
                        rs.getDate("fechaFacturaCliente"),
                        rs.getInt("idClienteFactura"),
                        rs.getInt("idTrabajadorFactura"),
                        new InvoicePaymentDTO(
                                rs.getInt("numeroFacturaCliente"),
                                rs.getDouble("baseImponibleFacturaCliente"),
                                rs.getDouble("ivaFacturaCliente"),
                                rs.getDouble("totalFacturaCliente"),
                                rs.getBoolean("cobradaFactura"),
                                rs.getInt("formaCobroFactura"),
                                rs.getDate("fechaCobroFactura")
                        )
                ));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener facturas: " + e.getMessage());
        }
        return invoices;
    }

    private static JTable setupInvoiceTable(List<Invoice> invoices, ActionListener listener, JPanel panel) {
        String[] columnNames = {
                "ID", "Número", "Fecha", "ID Cliente", "ID Trabajador",
                "Base Imponible", "IVA", "Total", "Pagada", "Forma de Pago", "Fecha de Pago",
                "Ver", "Editar", "Eliminar"
        };

        Object[][] data = new Object[invoices.size()][columnNames.length];

        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            JButton viewButton = new JButton("Ver");
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            data[i] = new Object[]{
                    inv.getId(),
                    inv.getInvoicePaymentDTO().getNumber(),
                    inv.getDate(),
                    inv.getClientId(),
                    inv.getWorkerId(),
                    inv.getInvoicePaymentDTO().getTaxableAmount(),
                    inv.getInvoicePaymentDTO().getVatAmount(),
                    inv.getInvoicePaymentDTO().getTotalAmount(),
                    inv.getInvoicePaymentDTO().isPaid() ? "Sí" : "No",
                    inv.getInvoicePaymentDTO().getPaymentMethod(),
                    (inv.getInvoicePaymentDTO().getPaymentDate() != null) ? inv.getInvoicePaymentDTO().getPaymentDate() : "No registrada",
                    viewButton,
                    editButton,
                    deleteButton
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 11; // Only allow editing on buttons
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        // Set button renderers and editors for action buttons
        table.getColumn("Ver").setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn("Ver").setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, invoices, panel, Constants.INVOICE_VIEW));
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, invoices, panel, Constants.INVOICE_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, invoices, panel, Constants.INVOICE_DELETE));

        return table;
    }

    public void modifyInvoice(JPanel panel, Invoice updatedInvoice, int id) {
        String query = "UPDATE facturasclientes SET numeroFacturaCliente = ?, fechaFacturaCliente = ?, " +
                "idClienteFactura = ?, totalFacturaCliente = ? WHERE idFacturaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, updatedInvoice.getInvoicePaymentDTO().getNumber());
            ps.setDate(2, updatedInvoice.getDate());
            ps.setInt(3, updatedInvoice.getClientId());
            ps.setDouble(4, updatedInvoice.getInvoicePaymentDTO().getTotalAmount());
            ps.setInt(5, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Factura actualizada con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar la factura.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar factura: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            showInvoiceTable(panel, e -> {
            });
            panel.revalidate();
            panel.repaint();
        });
    }

    public void deleteInvoice(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(panel, "¿Estás seguro de eliminar la factura?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM facturasclientes " +
                     "WHERE idFacturaCliente = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            showInvoiceTable(panel, e -> {
            });
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyInvoiceAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Factura", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        JTextField numberField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getNumber()));
        JTextField dateField = new JTextField(this.getDate().toString());
        JTextField clientIdField = new JTextField(String.valueOf(this.getClientId()));
        JTextField workerIdField = new JTextField(String.valueOf(this.getWorkerId()));

        JTextField taxableAmountField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getTaxableAmount()));
        JTextField vatAmountField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getVatAmount()));
        JTextField totalAmountField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getTotalAmount()));
        totalAmountField.setEditable(false);

        JCheckBox isPaidCheckBox = new JCheckBox("Pagada", this.getInvoicePaymentDTO().isPaid());
        JTextField paymentMethodField = new JTextField(String.valueOf(this.getInvoicePaymentDTO().getPaymentMethod()));
        JTextField paymentDateField = new JTextField(
                this.getInvoicePaymentDTO().getPaymentDate() != null ? this.getInvoicePaymentDTO().getPaymentDate().toString() : ""
        );

        formPanel.add(new JLabel("Número de Factura:"));
        formPanel.add(numberField);
        formPanel.add(new JLabel("Fecha Factura (YYYY-MM-DD):"));
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
        formPanel.add(new JLabel("Pagada:"));
        formPanel.add(isPaidCheckBox);
        formPanel.add(new JLabel("Forma de Pago:"));
        formPanel.add(paymentMethodField);
        formPanel.add(new JLabel("Fecha de Pago (YYYY-MM-DD):"));
        formPanel.add(paymentDateField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Invoice updatedInvoice = new Invoice(
                        this.panel, listener, this.getId(),
                        Date.valueOf(dateField.getText()),
                        Integer.parseInt(clientIdField.getText()),
                        Integer.parseInt(workerIdField.getText()),
                        new InvoicePaymentDTO(
                                Integer.parseInt(numberField.getText()),
                                Double.parseDouble(taxableAmountField.getText()),
                                Double.parseDouble(vatAmountField.getText()),
                                Double.parseDouble(totalAmountField.getText()),
                                isPaidCheckBox.isSelected(),
                                Integer.parseInt(paymentMethodField.getText()),
                                paymentDateField.getText().isEmpty() ? null : Date.valueOf(paymentDateField.getText())
                        )
                );

                this.modifyInvoice(panel, updatedInvoice, this.getId());
                JOptionPane.showMessageDialog(dialog, "Factura actualizada con éxito.");
                dialog.dispose();

                SwingUtilities.invokeLater(() -> {
                    panel.removeAll();
                    Invoice.showInvoiceTable(panel, listener);
                    panel.revalidate();
                    panel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar factura: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
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