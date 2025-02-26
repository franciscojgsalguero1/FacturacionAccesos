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
    final String client;
    final String worker;
    final InvoicePaymentDTO invoicePaymentDTO;
    final Button view;
    final Button delete;

    public Invoice(JPanel panel, ActionListener listener, int id, Date date, String client, String worker,
                   InvoicePaymentDTO invoicePaymentDTO) {
        this.panel = panel;
        this.id = id;
        this.date = date;
        this.client = client;
        this.worker = worker;
        this.invoicePaymentDTO = invoicePaymentDTO;

        this.view = new Button("Ver");
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.view.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "VIEW_INVOICE");
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

        JButton createButton = new JButton("Crear Factura");
        createButton.addActionListener(e -> new CreateInvoiceForm(panel, listener));

        String[] filterOptions = {
                "ID", "Número", "Fecha", "ID Cliente", "ID Trabajador",
                "Base Imponible", "IVA", "Total", Constants.PAID, "Forma de Pago", "Fecha de Pago"
        };

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar factura...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        JTable table = setupInvoiceTable(invoices, listener);
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

    public static List<Invoice> getInvoices(JPanel panel, ActionListener listener) {
        List<Invoice> invoices = new ArrayList<>();
        // Added f.corrected to the select list (assume the column name is "corrected")
        String query = "SELECT f.idFacturaCliente, f.fechaFacturaCliente, " +
                "c.nombreCliente, w.name AS trabajadorNombre, fp.tipoFormaPago, " +
                "f.numeroFacturaCliente, f.baseImponibleFacturaCliente, " +
                "f.ivaFacturaCliente, f.totalFacturaCliente, f.cobradaFactura, " +
                "f.corrected, f.fechaCobroFactura " +
                "FROM facturasclientes f " +
                "JOIN clientes c ON f.idClienteFactura = c.idCliente " +
                "JOIN workers w ON f.idTrabajadorFactura = w.id " +
                "JOIN formapago fp ON f.formaCobroFactura = fp.idFormaPago";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                boolean corrected = false;
                Object correctedObj = rs.getObject("corrected");
                if (correctedObj != null) {
                    corrected = rs.getBoolean("corrected");
                }

                invoices.add(new Invoice(
                        panel, listener, rs.getInt("idFacturaCliente"),
                        rs.getDate("fechaFacturaCliente"),
                        rs.getString("nombreCliente"),
                        rs.getString("trabajadorNombre"),
                        new InvoicePaymentDTO(
                                rs.getInt("numeroFacturaCliente"),
                                rs.getDouble("baseImponibleFacturaCliente"),
                                rs.getDouble("ivaFacturaCliente"),
                                rs.getDouble("totalFacturaCliente"),
                                rs.getBoolean("cobradaFactura"),
                                corrected,
                                rs.getString("tipoFormaPago"),
                                rs.getDate("fechaCobroFactura")
                        )
                ));
            }
        } catch (SQLException e) {
            logger.error(String.format("Error al obtener facturas: %s", e.getMessage()));
        }
        return invoices;
    }

    private static JTable setupInvoiceTable(List<Invoice> invoices, ActionListener listener) {
        String[] columnNames = {
                "ID", "Número", "Fecha", "ID Cliente", "ID Trabajador",
                "Base Imponible", "IVA", "Total", Constants.PAID, "Rectificada", "Forma de Pago", "Fecha de Pago",
                "Ver", "Eliminar"
        };

        Object[][] data = new Object[invoices.size()][columnNames.length];

        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            JButton viewButton = new JButton("Ver");
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            data[i] = new Object[]{
                    inv.getId(),
                    inv.getInvoicePaymentDTO().getNumber(),
                    inv.getDate(),
                    inv.getClient(),
                    inv.getWorker(),
                    inv.getInvoicePaymentDTO().getTaxableAmount(),
                    inv.getInvoicePaymentDTO().getVatAmount(),
                    inv.getInvoicePaymentDTO().getTotalAmount(),
                    inv.getInvoicePaymentDTO().isPaid() ? "Sí" : "No",
                    inv.getInvoicePaymentDTO().isCorrected() ? "Sí" : "No",
                    inv.getInvoicePaymentDTO().getPaymentMethod(),
                    (inv.getInvoicePaymentDTO().getPaymentDate() != null) ?
                            inv.getInvoicePaymentDTO().getPaymentDate() : "No registrada",
                    viewButton, deleteButton
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

        table.getColumn("Ver").setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn("Ver").setCellEditor(new ButtonEditor<>( listener, invoices, Constants.INVOICE_VIEW));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, invoices,
                Constants.INVOICE_DELETE));

        return table;
    }

    public void deleteInvoice(JPanel panel, int id, ActionListener listener) {
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
            Client.showClientTable(panel, listener);
            panel.revalidate();
            panel.repaint();
        });
    }
}