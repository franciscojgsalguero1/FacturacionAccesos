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
import java.sql.*;

/**
 * Clase que muestra los detalles de una factura en un JDialog.
 * Permite visualizar información de la factura y generar un PDF de la misma.
 */
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
    private final int invoiceId;

    /**
     * Constructor de la ventana de factura.
     *
     * @param parentPanel Panel padre de la vista.
     * @param listener    Listener para manejar eventos.
     * @param invoiceId   id de la factura a visualizar.
     */
    public ViewInvoice(JPanel parentPanel, ActionListener listener, int invoiceId) {
        this.invoiceId = invoiceId;
        setTitle("Factura");
        setSize(950, 750);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel mainPanel = createMainPanel();
        add(mainPanel);

        loadInvoiceData(invoiceId);
        setVisible(true);
    }

    /**
     * Crea y configura el panel principal de la vista.
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Sección superior (encabezado)
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel con los datos generales de la factura
        mainPanel.add(createDetailsPanel(), BorderLayout.CENTER);

        // Tabla de detalles de los artículos
        mainPanel.add(createTablePanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Crea el panel superior con el título y el botón de PDF.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        JButton pdfButton = new JButton("Generar PDF");
        pdfButton.setPreferredSize(new Dimension(pdfButton.getPreferredSize().width, 30));
        pdfButton.addActionListener(e -> generatePDF());

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

        return headerPanel;
    }

    /**
     * Crea el panel con los datos generales de la factura.
     */
    private JPanel createDetailsPanel() {
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
        return detailsPanel;
    }

    /**
     * Crea una fila de campo con una etiqueta y un valor.
     *
     * @param labelText Texto de la etiqueta.
     * @param valueLabel Componente JLabel donde se mostrará el valor.
     * @return JPanel con la fila de campo.
     */
    private JPanel createFieldRow(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));

        // Crear la etiqueta de la propiedad
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Constants.ARIAL, Font.BOLD, 13));
        label.setPreferredSize(new Dimension(110, 30)); // Ajusta el ancho fijo de la etiqueta

        // Configurar el JLabel donde se mostrará el valor
        valueLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),  // Borde gris
                new EmptyBorder(2, 4, 2, 4)           // Espaciado interno
        ));
        valueLabel.setOpaque(true);
        valueLabel.setBackground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font(Constants.ARIAL, Font.PLAIN, 13));
        valueLabel.setPreferredSize(new Dimension(140, 30)); // Tamaño del valor

        // Agregar los elementos al panel
        panel.add(label);
        panel.add(valueLabel);

        return panel;
    }

    /**
     * Crea el panel con la tabla de detalles de los artículos.
     */
    private JPanel createTablePanel() {
        DefaultTableModel tableModel = new DefaultTableModel(
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

        return tablePanel;
    }

    /**
     * Carga los datos de la factura desde la base de datos.
     */
    private void loadInvoiceData(int invoiceId) {
        try (Connection conn = Utils.getConnection();
             PreparedStatement invoicePs = conn.prepareStatement(
                     "SELECT f.fechaFacturaCliente, f.numeroFacturaCliente, f.baseImponibleFacturaCliente, " +
                             "f.ivaFacturaCliente, f.totalFacturaCliente, f.fechaCobroFactura, fp.tipoFormaPago, " +
                             "c.nombreCliente, w.name " +
                             "FROM facturasclientes f " +
                             "JOIN formapago fp ON f.formaCobroFactura = fp.idFormapago " +
                             "JOIN clientes c ON f.idClienteFactura = c.idCliente " +
                             "JOIN workers w ON f.idTrabajadorFactura = w.id " +
                             "WHERE f.idFacturaCliente = ?")) {

            invoicePs.setInt(1, invoiceId);
            ResultSet rs = invoicePs.executeQuery();

            if (rs.next()) {
                dateLabel.setText(rs.getString("fechaFacturaCliente"));
                invoiceNumberLabel.setText(String.valueOf(rs.getInt("numeroFacturaCliente")));
                baseAmountLabel.setText(String.format(Constants.TWO_DEC, rs.getDouble("baseImponibleFacturaCliente")));
                vatAmountLabel.setText(String.format(Constants.TWO_DEC, rs.getDouble("ivaFacturaCliente")));
                totalAmountLabel.setText(String.format(Constants.TWO_DEC, rs.getDouble("totalFacturaCliente")));
                paymentDateLabel.setText(rs.getString("fechaCobroFactura"));
                paymentMethodLabel.setText(rs.getString("tipoFormaPago"));
                clientNameLabel.setText(rs.getString("nombreCliente"));
                workerNameLabel.setText(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Genera un PDF de la factura.
     */
    private void generatePDF() {
        InvoicePDFGenerator.generateInvoicePDF(null, null, invoiceId, "factura.pdf");
        JOptionPane.showMessageDialog(this, "PDF generado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel createStyledLabel() {
        JLabel label = new JLabel();
        label.setFont(new Font(Constants.ARIAL, Font.PLAIN, 13));
        return label;
    }
}