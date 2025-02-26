package org.fbmoll.billing.content;

import org.fbmoll.billing.resources.Utils;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewInvoice extends JDialog {
    private final JLabel dateLabel = createStyledLabel();
    private final JLabel invoiceNumberLabel = createStyledLabel();
    private final JLabel clientNameLabel = createStyledLabel();
    private final JLabel workerNameLabel = createStyledLabel();
    private final JLabel baseAmountLabel = createStyledLabel();
    private final JLabel vatAmountLabel = createStyledLabel();
    private final JLabel totalAmountLabel = createStyledLabel();
    private final JLabel paymentMethodLabel = createStyledLabel();
    private final JLabel paymentDateLabel = createStyledLabel();
    private final JTable itemsTable;
    private final DefaultTableModel tableModel;

    public ViewInvoice(JPanel parentPanel, int invoiceId) {
        setTitle("Factura");
        setSize(950, 750);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        // Padding around entire invoice
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(mainPanel);

        // HEADER - Title and Invoice Info
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("FACTURA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel topRightPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topRightPanel.add(createFieldRow("N° Factura:", invoiceNumberLabel));
        topRightPanel.add(createFieldRow("Fecha:", dateLabel));
        topRightPanel.add(createFieldRow("Fecha de Pago:", paymentDateLabel));
        headerPanel.add(topRightPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // CLIENT & WORKER DETAILS (reordered and compacted)
        JPanel detailsPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        // Create a compound border for inner padding (10px) inside the titled border
        detailsPanel.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Datos de la Factura"),
                new EmptyBorder(10, 10, 0, 10)
        ));
        // Set a preferred size to make the panel smaller overall
        detailsPanel.setPreferredSize(new Dimension(400, 100));
        // Row 1
        detailsPanel.add(createFieldRow("Cliente:", clientNameLabel));
        detailsPanel.add(createFieldRow("Trabajador:", workerNameLabel));
        detailsPanel.add(createFieldRow("Forma de Pago:", paymentMethodLabel));
        // Row 2
        detailsPanel.add(createFieldRow("Base Imponible:", baseAmountLabel));
        detailsPanel.add(createFieldRow("IVA:", vatAmountLabel));
        detailsPanel.add(createFieldRow("Total:", totalAmountLabel));

        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        // TABLE - Items List
        tableModel = new DefaultTableModel(
                new String[]{"Código", "Descripción", "Precio", "IVA", "Cantidad", "Subtotal", "Total"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(30);

        // Center the text in all table cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < itemsTable.getColumnCount(); i++) {
            itemsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScroll = new JScrollPane(itemsTable);
        // Add inner padding (10px) to the titled border of the table scroll pane
        tableScroll.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Detalles de los Artículos"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Adjust table panel: add a moderate top margin and fix its preferred height
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        tablePanel.add(tableScroll);
        tablePanel.setPreferredSize(new Dimension(0, 350)); // Adjust height as needed

        mainPanel.add(tablePanel, BorderLayout.SOUTH);

        // Load Invoice Data
        loadInvoice(invoiceId);
        setVisible(true);
    }

    /**
     * Creates a row with a label and a value field for styling.
     */
    private JPanel createFieldRow(String labelText, JLabel label) {
        // Use a FlowLayout with a small vertical gap to keep things compact
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setPreferredSize(new Dimension(110, 30)); // Label size

        // Create a compound border with the line border and an inner empty border for padding
        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        label.setPreferredSize(new Dimension(140, 30)); // Value box size

        panel.add(lbl);
        panel.add(label);
        return panel;
    }

    /**
     * Creates a styled label for display purposes.
     */
    private JLabel createStyledLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }

    private void loadInvoice(int invoiceId) {
        String invoiceQuery = "SELECT * FROM facturasclientes WHERE idFacturaCliente = ?";
        String clientQuery = "SELECT nombreCliente FROM clientes WHERE idCliente = ?";
        String workerQuery = "SELECT name FROM workers WHERE id = ?";
        // New prepared statement for payment method lookup from formapago table
        String paymentQuery = "SELECT tipoformapago FROM formapago WHERE idFormapago = ?";
        String linesQuery = "SELECT idArticulo, cantidad, pvpArticulo, iva FROM lineasfacturasclientes WHERE numeroFacturaCliente = ?";
        String articleQuery = "SELECT codigoArticulo, descripcionArticulo, pvpArticulo FROM articulos WHERE idArticulo = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement invoicePs = conn.prepareStatement(invoiceQuery);
             PreparedStatement clientPs = conn.prepareStatement(clientQuery);
             PreparedStatement workerPs = conn.prepareStatement(workerQuery);
             PreparedStatement paymentPs = conn.prepareStatement(paymentQuery);
             PreparedStatement linesPs = conn.prepareStatement(linesQuery);
             PreparedStatement articlePs = conn.prepareStatement(articleQuery)) {

            // Load Invoice Data
            invoicePs.setInt(1, invoiceId);
            try (ResultSet invoiceRs = invoicePs.executeQuery()) {
                if (invoiceRs.next()) {
                    dateLabel.setText(invoiceRs.getString("fechaFacturaCliente"));
                    invoiceNumberLabel.setText(String.valueOf(invoiceRs.getInt("numeroFacturaCliente")));

                    int clientId = invoiceRs.getInt("idClienteFactura");
                    int workerId = invoiceRs.getInt("idTrabajadorFactura");

                    baseAmountLabel.setText(String.format("%.2f €", invoiceRs.getDouble("baseImponibleFacturaCliente")));
                    vatAmountLabel.setText(String.format("%.2f €", invoiceRs.getDouble("ivaFacturaCliente")));
                    totalAmountLabel.setText(String.format("%.2f €", invoiceRs.getDouble("totalFacturaCliente")));
                    paymentDateLabel.setText(invoiceRs.getString("fechaCobroFactura"));

                    // Obtain the payment method from the formapago table
                    int formaPagoId = invoiceRs.getInt("formaCobroFactura");
                    paymentPs.setInt(1, formaPagoId);
                    try (ResultSet paymentRs = paymentPs.executeQuery()) {
                        if (paymentRs.next()) {
                            paymentMethodLabel.setText(paymentRs.getString("tipoformapago"));
                        }
                    }

                    // Load Client Name
                    clientPs.setInt(1, clientId);
                    try (ResultSet clientRs = clientPs.executeQuery()) {
                        if (clientRs.next()) {
                            clientNameLabel.setText(clientRs.getString("nombreCliente"));
                        }
                    }

                    // Load Worker Name
                    workerPs.setInt(1, workerId);
                    try (ResultSet workerRs = workerPs.executeQuery()) {
                        if (workerRs.next()) {
                            workerNameLabel.setText(workerRs.getString("name"));
                        }
                    }
                }
            }

            // Load Invoice Lines
            linesPs.setInt(1, invoiceId);
            try (ResultSet linesRs = linesPs.executeQuery()) {
                tableModel.setRowCount(0);
                while (linesRs.next()) {
                    int itemId = linesRs.getInt("idArticulo");
                    int quantity = linesRs.getInt("cantidad");
                    double lineVat = linesRs.getDouble("iva");
                    double price;
                    String code = "";
                    String name = "";

                    articlePs.setInt(1, itemId);
                    try (ResultSet articleRs = articlePs.executeQuery()) {
                        if (articleRs.next()) {
                            code = articleRs.getString("codigoArticulo");
                            name = articleRs.getString("descripcionArticulo");
                            price = articleRs.getDouble("pvpArticulo");
                        } else {
                            continue;
                        }
                    }

                    double subtotal = price * quantity;
                    double total = subtotal + subtotal * lineVat / 100;
                    tableModel.addRow(new Object[]{
                            code,
                            name,
                            String.format("%.2f €", price),
                            lineVat + " %",
                            quantity,
                            String.format("%.2f €", subtotal),
                            String.format("%.2f €", total)
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la factura: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}