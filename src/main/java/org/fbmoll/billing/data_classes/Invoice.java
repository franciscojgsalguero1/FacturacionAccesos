package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter
public class Invoice {
    int id;
    int number;
    Date date;
    int clientId;
    int workerId;
    double taxableAmount;
    double vatAmount;
    double totalAmount;
    String hash;
    String qrCode;
    boolean isPaid;
    int paymentMethod;
    Date paymentDate;
    String notes;

    public static void showInvoiceTable(JPanel panel) {
        List<Invoice> invoices = Invoice.getAllInvoices();
        if (invoices.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay facturas disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Número", "Fecha", "ID Cliente", "ID Trabajador", "Base Imponible", "IVA",
                "Total", "Hash", "QR", "Pagada", "Método Pago", "Fecha Pago", "Notas", "Editar", "Eliminar"};

        Object[][] data = new Object[invoices.size()][columnNames.length];
        for (int i = 0; i < invoices.size(); i++) {
            Invoice inv = invoices.get(i);
            data[i] = new Object[]{inv.id, inv.number, inv.date, inv.clientId, inv.workerId, inv.taxableAmount,
                    inv.vatAmount, inv.totalAmount, inv.hash, inv.qrCode, inv.isPaid, inv.paymentMethod, inv.paymentDate,
                    inv.notes, "📝", "❌"};
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));
        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }


    public void createInvoice(Invoice invoice) {
        String query = "INSERT INTO facturasclientes (numeroFacturaCliente, fechaFacturaCliente, idClienteFactura, " +
                "idTrabajadorFactura, baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, " +
                "hashFacturaCliente, qrFacturaCliente, cobradaFactura, formaCobroFactura, fechaCobroFactura, " +
                "observacionesFacturaClientes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, invoice.number);
            ps.setDate(2, invoice.date);
            ps.setInt(3, invoice.clientId);
            ps.setInt(4, invoice.workerId);
            ps.setDouble(5, invoice.taxableAmount);
            ps.setDouble(6, invoice.vatAmount);
            ps.setDouble(7, invoice.totalAmount);
            ps.setString(8, invoice.hash);
            ps.setString(9, invoice.qrCode);
            ps.setBoolean(10, invoice.isPaid);
            ps.setInt(11, invoice.paymentMethod);
            ps.setDate(12, invoice.paymentDate);
            ps.setString(13, invoice.notes);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String query = "SELECT * FROM facturasclientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(new Invoice(
                        rs.getInt("idFacturaCliente"),
                        rs.getInt("numeroFacturaCliente"),
                        rs.getDate("fechaFacturaCliente"),
                        rs.getInt("idClienteFactura"),
                        rs.getInt("idTrabajadorFactura"),
                        rs.getDouble("baseImponibleFacturaCliente"),
                        rs.getDouble("ivaFacturaCliente"),
                        rs.getDouble("totalFacturaCliente"),
                        rs.getString("hashFacturaCliente"),
                        rs.getString("qrFacturaCliente"),
                        rs.getBoolean("cobradaFactura"),
                        rs.getInt("formaCobroFactura"),
                        rs.getDate("fechaCobroFactura"),
                        rs.getString("observacionesFacturaClientes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public static Invoice getInvoice(int id) {
        String query = "SELECT * FROM facturasclientes WHERE idFacturaCliente = ?";
        Invoice invoice = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    invoice = new Invoice(
                            rs.getInt("idFacturaCliente"),
                            rs.getInt("numeroFacturaCliente"),
                            rs.getDate("fechaFacturaCliente"),
                            rs.getInt("idClienteFactura"),
                            rs.getInt("idTrabajadorFactura"),
                            rs.getDouble("baseImponibleFacturaCliente"),
                            rs.getDouble("ivaFacturaCliente"),
                            rs.getDouble("totalFacturaCliente"),
                            rs.getString("hashFacturaCliente"),
                            rs.getString("qrFacturaCliente"),
                            rs.getBoolean("cobradaFactura"),
                            rs.getInt("formaCobroFactura"),
                            rs.getDate("fechaCobroFactura"),
                            rs.getString("observacionesFacturaClientes")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoice;
    }

    public void modifyInvoice(Invoice invoice, int id) {
        String query = "UPDATE facturasclientes SET numeroFacturaCliente = ?, fechaFacturaCliente = ?, " +
                "idClienteFactura = ?, idTrabajadorFactura = ?, baseImponibleFacturaCliente = ?, " +
                "ivaFacturaCliente = ?, totalFacturaCliente = ?, hashFacturaCliente = ?, " +
                "qrFacturaCliente = ?, cobradaFactura = ?, formaCobroFactura = ?, " +
                "fechaCobroFactura = ?, observacionesFacturaClientes = ? WHERE idFacturaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, invoice.number);
            ps.setDate(2, invoice.date);
            ps.setInt(3, invoice.clientId);
            ps.setInt(4, invoice.workerId);
            ps.setDouble(5, invoice.taxableAmount);
            ps.setDouble(6, invoice.vatAmount);
            ps.setDouble(7, invoice.totalAmount);
            ps.setString(8, invoice.hash);
            ps.setString(9, invoice.qrCode);
            ps.setBoolean(10, invoice.isPaid);
            ps.setInt(11, invoice.paymentMethod);
            ps.setDate(12, invoice.paymentDate);
            ps.setString(13, invoice.notes);
            ps.setInt(14, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteInvoice(int id) {
        String query = "DELETE FROM facturasclientes WHERE idFacturaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}