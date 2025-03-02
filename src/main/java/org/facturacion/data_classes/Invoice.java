package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateInvoiceForm;
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

        addActionListener(this.view, listener, Constants.INVOICE_VIEW);
        addActionListener(this.delete, listener, Constants.INVOICE_DELETE);
    }

    private void addActionListener(Button button, ActionListener listener, String command) {
        button.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command)));
    }

    public static void showInvoiceTable(JPanel panel, ActionListener listener) {
        List<Invoice> invoices = getInvoices(panel, listener);
        JPanel topPanel = createTopPanel(panel, listener);
        JTable table = setupInvoiceTable(invoices, listener);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        setupSearchFilter(topPanel, sorter);

        SwingUtilities.invokeLater(() -> updatePanel(panel, topPanel, tablePane));
    }

    private static void setupSearchFilter(JPanel topPanel, TableRowSorter<DefaultTableModel> sorter) {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar factura...");
        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{
                "ID", "Número", "Fecha", "ID Cliente", "ID Trabajador",
                "Base Imponible", "IVA", "Total", Constants.PAID, "Forma de Pago", "Fecha de Pago"
        });

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
        JButton createButton = new JButton("Crear Factura");

        createButton.addActionListener(e -> new CreateInvoiceForm(panel, listener));

        topPanel.add(createButton);
        return topPanel;
    }

    public static List<Invoice> getInvoices(JPanel panel, ActionListener listener) {
        List<Invoice> invoices = new ArrayList<>();
        String query = """
                SELECT f.idFacturaCliente, f.fechaFacturaCliente, 
                       c.nombreCliente, w.name AS trabajadorNombre, fp.tipoFormaPago, 
                       f.numeroFacturaCliente, f.baseImponibleFacturaCliente, 
                       f.ivaFacturaCliente, f.totalFacturaCliente, f.cobradaFactura, 
                       f.corrected, f.fechaCobroFactura 
                FROM facturasclientes f 
                JOIN clientes c ON f.idClienteFactura = c.idCliente 
                JOIN workers w ON f.idTrabajadorFactura = w.id 
                JOIN formapago fp ON f.formaCobroFactura = fp.idFormaPago
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(new Invoice(
                        panel, listener,
                        rs.getInt("idFacturaCliente"),
                        rs.getDate("fechaFacturaCliente"),
                        rs.getString("nombreCliente"),
                        rs.getString("trabajadorNombre"),
                        new InvoicePaymentDTO(
                                rs.getInt("numeroFacturaCliente"),
                                rs.getDouble("baseImponibleFacturaCliente"),
                                rs.getDouble("ivaFacturaCliente"),
                                rs.getDouble("totalFacturaCliente"),
                                rs.getBoolean("cobradaFactura"),
                                rs.getBoolean("corrected"),
                                rs.getString("tipoFormaPago"),
                                rs.getDate("fechaCobroFactura")
                        )
                ));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener facturas: {}", e.getMessage(), e);
        }
        return invoices;
    }

    private static JTable setupInvoiceTable(List<Invoice> invoices, ActionListener listener) {
        String[] columnNames = {
                "ID", "Número", "Fecha", "ID Cliente", "ID Trabajador",
                "Base Imponible", "IVA", "Total", Constants.PAID, "Rectificada", "Forma de Pago", "Fecha de Pago",
                "Ver", "Eliminar"
        };

        Object[][] data = invoices.stream().map(inv -> new Object[]{
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
                (inv.getInvoicePaymentDTO().getPaymentDate() != null) ? inv.getInvoicePaymentDTO().getPaymentDate() : "No registrada",
                new JButton("Ver"),
                new JButton(Constants.BUTTON_DELETE)
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 11; }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn("Ver").setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn("Ver").setCellEditor(new ButtonEditor<>(listener, invoices, Constants.INVOICE_VIEW));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, invoices, Constants.INVOICE_DELETE));

        return table;
    }

    /**
     * Elimina una factura de la base de datos después de confirmar la acción con el usuario.
     *
     * @param mainPanel Panel principal donde se mostrará la tabla de facturas actualizada.
     * @param id        ID de la factura que se va a eliminar.
     * @param view      Vista principal para manejar eventos y actualizar la interfaz.
     */
    public void deleteInvoice(JPanel mainPanel, int id, View view) {
        // Confirmar con el usuario antes de eliminar la factura
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar la factura con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        // Si el usuario selecciona "No", se cancela la operación
        if (confirm != JOptionPane.YES_OPTION) return;

        // Consulta SQL para eliminar la factura por su ID
        String query = "DELETE FROM facturasclientes WHERE idFacturaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Asignar el ID de la factura en la consulta preparada
            ps.setInt(1, id);

            // Ejecutar la consulta y obtener el número de filas afectadas
            int rowsAffected = ps.executeUpdate();

            // Mostrar un mensaje según el resultado de la eliminación
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Factura eliminada con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró una factura con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar factura: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de facturas en la interfaz gráfica
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            Invoice.showInvoiceTable(mainPanel, view);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}