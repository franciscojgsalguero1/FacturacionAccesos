package org.fbmoll.billing.content;

import org.fbmoll.billing.resources.Constants;
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

public class ViewCorrectiveInvoice extends JDialog {
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

    public ViewCorrectiveInvoice(JPanel parentPanel, int invoiceId) {
        setTitle("Factura Rectificativa");
        setSize(950, 750);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(mainPanel);

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("FACTURA RECTIFICATIVA", SwingConstants.CENTER);
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
        label.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(2, 4, 2, 4)));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font(Constants.ARIAL, Font.PLAIN, 13));
        label.setPreferredSize(new Dimension(140, 30));
        panel.add(lbl);
        panel.add(label);
        return panel;
    }

    private void loadInvoice(int invoiceId) {
        try (Connection conn = Utils.getConnection();
             PreparedStatement invoicePs = conn.prepareStatement(
                 "SELECT numeroRectificativaCliente, idClienteRectificativaCliente, " +
                         "idTrabajadorRectificativaCliente, fechaRectificativaCliente, " +
                         "baseImponibleRectificativaCliente, ivaRectificativaCliente, " +
                         "totalRectificativaCliente FROM rectificativasclientes WHERE idRectificativaCliente = ?")) {
            invoicePs.setInt(1, invoiceId);
            try (ResultSet invoiceRs = invoicePs.executeQuery()) {
                if (invoiceRs.next()) {
                    int invoiceNumber = invoiceRs.getInt("numeroRectificativaCliente");
                    int clientId = invoiceRs.getInt("idClienteRectificativaCliente");
                    int workerId = invoiceRs.getInt("idTrabajadorRectificativaCliente");

                    dateLabel.setText(invoiceRs.getString("fechaRectificativaCliente"));
                    invoiceNumberLabel.setText(String.valueOf(invoiceNumber));
                    baseAmountLabel.setText(String.format(Constants.TWO_DEC,
                            invoiceRs.getDouble("baseImponibleRectificativaCliente")));
                    vatAmountLabel.setText(String.format(Constants.TWO_DEC,
                            invoiceRs.getDouble("ivaRectificativaCliente")));
                    totalAmountLabel.setText(String.format(Constants.TWO_DEC,
                            invoiceRs.getDouble("totalRectificativaCliente")));

                    clientNameLabel.setText(fetchSingleValue(conn, "SELECT nombreCliente " +
                            "FROM clientes WHERE idCliente = ?", clientId));
                    workerNameLabel.setText(fetchSingleValue(conn, "SELECT name " +
                            "FROM workers WHERE id = ?", workerId));

                    try (PreparedStatement paymentPs = conn.prepareStatement(
                            "SELECT fechaCobroFactura, formaCobroFactura " +
                                    "FROM facturasclientes WHERE idFacturaCliente = ?")) {
                        paymentPs.setInt(1, invoiceNumber);
                        try (ResultSet paymentRs = paymentPs.executeQuery()) {
                            if (paymentRs.next()) {
                                paymentDateLabel.setText(paymentRs.getString("fechaCobroFactura"));
                                paymentMethodLabel.setText(paymentRs.getString("formaCobroFactura"));
                            }
                        }
                    }

                    loadInvoiceLines(conn, invoiceNumber);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la factura rectificativa: "
                    + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String fetchSingleValue(Connection conn, String query, int param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "No encontrado";
            }
        }
    }

    private void loadInvoiceLines(Connection conn, int invoiceNumber) throws SQLException {
        try (PreparedStatement linesPs = conn.prepareStatement(
                "SELECT idArticulo, cantidad, pvpArticulo, iva FROM lineasfacturasclientes " +
                        "WHERE numeroFacturaCliente = ?")) {
            linesPs.setInt(1, invoiceNumber);
            try (ResultSet linesRs = linesPs.executeQuery()) {
                tableModel.setRowCount(0);
                while (linesRs.next()) {
                    String articleCode = fetchSingleValue(conn, "SELECT codigoArticulo FROM articulos" +
                            " WHERE idArticulo = ?", linesRs.getInt("idArticulo"));
                    String articleName = fetchSingleValue(conn, "SELECT descripcionArticulo FROM articulos" +
                            " WHERE idArticulo = ?", linesRs.getInt("idArticulo"));

                    double price = linesRs.getDouble("pvpArticulo");
                    double vat = linesRs.getDouble("iva");
                    int quantity = linesRs.getInt("cantidad");
                    double subtotal = price * quantity;
                    double total = subtotal * (1 + vat / 100);

                    tableModel.addRow(new Object[]{articleCode, articleName, String.format(Constants.TWO_DEC, price),
                            vat + " %", quantity, String.format(Constants.TWO_DEC, subtotal),
                            String.format(Constants.TWO_DEC, total)});
                }
            }
        }
    }
}
