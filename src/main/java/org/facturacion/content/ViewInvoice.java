package org.facturacion.content;

import org.facturacion.resources.Constants;
import org.facturacion.resources.Utils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
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
    private final DefaultTableModel tableModel;
    private final int invoiceId;

    public ViewInvoice(JPanel parentPanel, ActionListener listener, int invoiceId) {
        this.invoiceId = invoiceId;
        setTitle("Factura");
        setSize(950, 750);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());

        JButton pdfButton = new JButton("Generar PDF");
        Dimension originalSize = pdfButton.getPreferredSize();
        pdfButton.setPreferredSize(new Dimension(originalSize.width, 30));
        pdfButton.addActionListener(e -> generatePDF(parentPanel, listener));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBorder(new EmptyBorder(20, 20, 20, 0));
        buttonPanel.add(pdfButton);
        headerPanel.add(buttonPanel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("FACTURA", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Constants.ARIAL, Font.BOLD, 22));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel topRightPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topRightPanel.add(createFieldRow("N° Factura:", invoiceNumberLabel));
        topRightPanel.add(createFieldRow("Fecha:", dateLabel));
        topRightPanel.add(createFieldRow("Fecha de Pago:", paymentDateLabel));
        headerPanel.add(topRightPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        detailsPanel.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Datos de la Factura"),
                new EmptyBorder(10, 10, 0, 10)
        ));
        detailsPanel.setPreferredSize(new Dimension(400, 100));
        detailsPanel.add(createFieldRow("Cliente:", clientNameLabel));
        detailsPanel.add(createFieldRow("Trabajador:", workerNameLabel));
        detailsPanel.add(createFieldRow("Forma de Pago:", paymentMethodLabel));
        detailsPanel.add(createFieldRow("Base Imponible:", baseAmountLabel));
        detailsPanel.add(createFieldRow("IVA:", vatAmountLabel));
        detailsPanel.add(createFieldRow("Total:", totalAmountLabel));
        mainPanel.add(detailsPanel, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(
                new String[]{"Código", "Descripción", "Precio", "IVA", "Cantidad", "Subtotal", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(30);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < itemsTable.getColumnCount(); i++) {
            itemsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScroll = new JScrollPane(itemsTable);
        tableScroll.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Detalles de los Artículos"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        tablePanel.add(tableScroll);
        tablePanel.setPreferredSize(new Dimension(0, 350));
        mainPanel.add(tablePanel, BorderLayout.SOUTH);

        loadInvoice(invoiceId);
        setVisible(true);
    }


    private void loadInvoice(int invoiceId) {
        try (Connection conn = Utils.getConnection()) {
            loadInvoiceDetails(conn, invoiceId);
            loadInvoiceLines(conn, invoiceId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadInvoiceDetails(Connection conn, int invoiceId) throws SQLException {
        String invoiceQuery = "SELECT fechaFacturaCliente, numeroFacturaCliente, baseImponibleFacturaCliente, " +
                "ivaFacturaCliente, totalFacturaCliente, fechaCobroFactura, formaCobroFactura, " +
                "idClienteFactura, idTrabajadorFactura " +
                "FROM facturasclientes WHERE idFacturaCliente = ?";
        try (PreparedStatement invoicePs = conn.prepareStatement(invoiceQuery)) {
            invoicePs.setInt(1, invoiceId);
            try (ResultSet invoiceRs = invoicePs.executeQuery()) {
                if (invoiceRs.next()) {
                    dateLabel.setText(invoiceRs.getString("fechaFacturaCliente"));
                    invoiceNumberLabel.setText(String.valueOf(invoiceRs.getInt("numeroFacturaCliente")));
                    baseAmountLabel.setText(String.format(Constants.TWO_DEC,
                            invoiceRs.getDouble("baseImponibleFacturaCliente")));
                    vatAmountLabel.setText(String.format(Constants.TWO_DEC,
                            invoiceRs.getDouble("ivaFacturaCliente")));
                    totalAmountLabel.setText(String.format(Constants.TWO_DEC,
                            invoiceRs.getDouble("totalFacturaCliente")));
                    paymentDateLabel.setText(invoiceRs.getString("fechaCobroFactura"));

                    int clientId = invoiceRs.getInt("idClienteFactura");
                    int workerId = invoiceRs.getInt("idTrabajadorFactura");
                    int formaPagoId = invoiceRs.getInt("formaCobroFactura");

                    loadPaymentMethod(conn, formaPagoId);
                    loadClientName(conn, clientId);
                    loadWorkerName(conn, workerId);
                }
            }
        }
    }

    private void loadPaymentMethod(Connection conn, int formaPagoId) throws SQLException {
        String paymentQuery = "SELECT tipoformapago FROM formapago WHERE idFormapago = ?";
        try (PreparedStatement paymentPs = conn.prepareStatement(paymentQuery)) {
            paymentPs.setInt(1, formaPagoId);
            try (ResultSet paymentRs = paymentPs.executeQuery()) {
                if (paymentRs.next()) {
                    paymentMethodLabel.setText(paymentRs.getString("tipoformapago"));
                }
            }
        }
    }

    private void loadClientName(Connection conn, int clientId) throws SQLException {
        String clientQuery = "SELECT nombreCliente FROM clientes WHERE idCliente = ?";
        try (PreparedStatement clientPs = conn.prepareStatement(clientQuery)) {
            clientPs.setInt(1, clientId);
            try (ResultSet clientRs = clientPs.executeQuery()) {
                if (clientRs.next()) {
                    clientNameLabel.setText(clientRs.getString("nombreCliente"));
                }
            }
        }
    }

    private void loadWorkerName(Connection conn, int workerId) throws SQLException {
        String workerQuery = "SELECT name FROM workers WHERE id = ?";
        try (PreparedStatement workerPs = conn.prepareStatement(workerQuery)) {
            workerPs.setInt(1, workerId);
            try (ResultSet workerRs = workerPs.executeQuery()) {
                if (workerRs.next()) {
                    workerNameLabel.setText(workerRs.getString("name"));
                }
            }
        }
    }

    private void loadInvoiceLines(Connection conn, int invoiceId) throws SQLException {
        String linesQuery = "SELECT idArticulo, cantidad, iva FROM lineasfacturasclientes " +
                "WHERE numeroFacturaCliente = ?";
        try (PreparedStatement linesPs = conn.prepareStatement(linesQuery)) {
            linesPs.setInt(1, invoiceId);
            try (ResultSet linesRs = linesPs.executeQuery()) {
                tableModel.setRowCount(0);
                while (linesRs.next()) {
                    int itemId = linesRs.getInt("idArticulo");
                    int quantity = linesRs.getInt("cantidad");
                    double lineVat = linesRs.getDouble("iva");

                    Article article = getArticleDetails(conn, itemId);
                    if (article == null) {
                        continue;
                    }

                    double subtotal = article.price * quantity;
                    double total = subtotal + (subtotal * lineVat / 100);
                    tableModel.addRow(new Object[]{
                            article.code,
                            article.description,
                            String.format(Constants.TWO_DEC, article.price),
                            lineVat + " %",
                            quantity,
                            String.format(Constants.TWO_DEC, subtotal),
                            String.format(Constants.TWO_DEC, total)
                    });
                }
            }
        }
    }

    private void generatePDF(JPanel panel, ActionListener listener) {
        InvoicePDFGenerator.generateInvoicePDF(panel, listener, invoiceId, "factura.pdf");
        JOptionPane.showMessageDialog(this, "PDF generado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel createStyledLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font(Constants.ARIAL, Font.PLAIN, 13));
        return label;
    }

    private JPanel createFieldRow(String labelText, JLabel label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font(Constants.ARIAL, Font.BOLD, 13));
        lbl.setPreferredSize(new Dimension(110, 30));

        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(2, 4, 2, 4)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font(Constants.ARIAL, Font.PLAIN, 13));
        label.setPreferredSize(new Dimension(140, 30));

        panel.add(lbl);
        panel.add(label);
        return panel;
    }

    private Article getArticleDetails(Connection conn, int itemId) throws SQLException {
        String articleQuery = "SELECT codigoArticulo, descripcionArticulo, pvpArticulo FROM articulos" +
                " WHERE idArticulo = ?";
        try (PreparedStatement articlePs = conn.prepareStatement(articleQuery)) {
            articlePs.setInt(1, itemId);
            try (ResultSet articleRs = articlePs.executeQuery()) {
                if (articleRs.next()) {
                    String code = articleRs.getString("codigoArticulo");
                    String description = articleRs.getString("descripcionArticulo");
                    double price = articleRs.getDouble("pvpArticulo");
                    return new Article(code, description, price);
                }
            }
        }
        return null;
    }

    private record Article(String code, String description, double price) {}
}