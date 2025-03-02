package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.create_forms.CreateCorrectiveInvoice;
import org.facturacion.dto.InvoicePaymentDTO;
import org.facturacion.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.Button;
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
    final String client;
    final String worker;
    final InvoicePaymentDTO invoicePaymentDTO;
    final Button view;
    final Button delete;

    public CorrectiveInvoice(JPanel panel, ActionListener listener, int id, Date date,
                             String client, String worker, InvoicePaymentDTO invoicePaymentDTO) {
        this.panel = panel;
        this.id = id;
        this.date = date;
        this.client = client;
        this.worker = worker;
        this.invoicePaymentDTO = invoicePaymentDTO;

        this.view = new Button("Ver");
        this.delete = new Button(Constants.BUTTON_DELETE);

        addActionListener(this.view, listener, Constants.INVOICE_VIEW);
        addActionListener(this.delete, listener, Constants.INVOICE_DELETE);
    }

    private void addActionListener(Button button, ActionListener listener, String command) {
        button.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command)));
    }

    public static void showCorrectiveInvoiceTable(JPanel panel, ActionListener listener) {
        List<CorrectiveInvoice> invoices = getCorrectiveInvoices(panel, listener);
        JPanel topPanel = createTopPanel(panel, listener);
        JTable table = setupCorrectiveInvoiceTable(invoices, listener);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        setupSearchFilter(topPanel, sorter);

        SwingUtilities.invokeLater(() -> updatePanel(panel, topPanel, tablePane));
    }

    private static void setupSearchFilter(JPanel topPanel, TableRowSorter<DefaultTableModel> sorter) {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar rectificativa...");
        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"ID", "Factura", "Fecha", "Cliente", "Trabajador", "Base Imponible", "IVA", "Total"});

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void applyFilter() {
                String text = searchField.getText().trim();
                int columnIndex = filterDropdown.getSelectedIndex();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
            }

            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);
    }

    private static void updatePanel(JPanel panel, JPanel topPanel, JScrollPane tablePane) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tablePane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private static JPanel createTopPanel(JPanel panel, ActionListener listener) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Rectificativa");

        createButton.addActionListener(e -> new CreateCorrectiveInvoice(panel, evt -> {
            if ("REFRESH".equals(evt.getActionCommand())) {
                SwingUtilities.invokeLater(() -> showCorrectiveInvoiceTable(panel, listener));
            }
        }));

        topPanel.add(createButton);
        return topPanel;
    }

    public static List<CorrectiveInvoice> getCorrectiveInvoices(JPanel panel, ActionListener listener) {
        List<CorrectiveInvoice> invoices = new ArrayList<>();
        String query = """
                SELECT fc.idFacturaCliente, fc.numeroFacturaCliente, fc.fechaFacturaCliente, 
                c.nombreCliente, w.name, fc.baseImponibleFacturaCliente, 
                fc.ivaFacturaCliente, fc.totalFacturaCliente 
                FROM facturasclientes fc 
                JOIN clientes c ON fc.idClienteFactura = c.idCliente 
                JOIN workers w ON fc.idTrabajadorFactura = w.id 
                WHERE fc.corrected = 1
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(new CorrectiveInvoice(
                        panel,
                        listener,
                        rs.getInt("idFacturaCliente"),
                        rs.getDate("fechaFacturaCliente"),
                        rs.getString("nombreCliente"),
                        rs.getString("name"),
                        new InvoicePaymentDTO(
                                rs.getInt("numeroFacturaCliente"),
                                rs.getDouble("baseImponibleFacturaCliente"),
                                rs.getDouble("ivaFacturaCliente"),
                                rs.getDouble("totalFacturaCliente"),
                                true, false, "", null
                        )
                ));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener facturas rectificadas: {}", e.getMessage(), e);
        }
        return invoices;
    }

    private static JTable setupCorrectiveInvoiceTable(List<CorrectiveInvoice> invoices, ActionListener listener) {
        String[] columnNames = {"ID", "Factura", "Fecha", "Cliente", "Trabajador", "Base Imponible", "IVA",
                "Total", "Ver", "Eliminar"};
        Object[][] data = invoices.stream().map(invoice -> new Object[]{
                invoice.getId(),
                invoice.getInvoicePaymentDTO().getNumber(),
                invoice.getDate(),
                invoice.getClient(),
                invoice.getWorker(),
                invoice.getInvoicePaymentDTO().getTaxableAmount(),
                invoice.getInvoicePaymentDTO().getVatAmount(),
                invoice.getInvoicePaymentDTO().getTotalAmount(),
                new JButton("Ver"),
                new JButton(Constants.BUTTON_DELETE)
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 7; }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn("Ver").setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn("Ver").setCellEditor(new ButtonEditor<>(listener, invoices, Constants.INVOICE_VIEW));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, invoices, Constants.INVOICE_DELETE));

        return table;
    }

    public void deleteCorrectiveInvoice(JPanel panel, int id, ActionListener listener) {
        int confirm = JOptionPane.showConfirmDialog(panel, "¿Estás seguro de eliminar la rectificativa?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM rectificativasclientes WHERE idRectificativaCliente = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar rectificativa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showCorrectiveInvoiceTable(panel, listener));
    }
}
