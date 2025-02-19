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
public class CorrectiveInvoice {
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
    String notes;

    public static void showCorrectiveInvoiceTable(JPanel panel) {
        List<CorrectiveInvoice> invoices = CorrectiveInvoice.getCorrectiveInvoices();
        if (invoices.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay facturas rectificativas disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Número", "Fecha", "ID Cliente", "ID Trabajador", "Base Imponible",
                "IVA", "Total", "Hash", "QR", "Notas", "Editar", "Eliminar"};

        Object[][] data = new Object[invoices.size()][columnNames.length];
        for (int i = 0; i < invoices.size(); i++) {
            CorrectiveInvoice inv = invoices.get(i);
            data[i] = new Object[]{inv.id, inv.number, inv.date, inv.clientId, inv.workerId, inv.taxableAmount,
                    inv.vatAmount, inv.totalAmount, inv.hash, inv.qrCode, inv.notes, "📝", "❌"};
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


    // **CREATE CORRECTIVE INVOICE**
    public void createCorrectiveInvoice(CorrectiveInvoice invoice) {
        String query = "INSERT INTO rectificativasclientes (numeroRectificativaCliente, fechaRectificativaCliente, " +
                "idClienteRectificativaCliente, idTrabajadorRectificativaCliente, baseImponibleRectificativaCliente, " +
                "ivaRectificativaCliente, totalRectificativaCliente, hashRectificativaCliente, qrRectificativaCliente, " +
                "observacionesRectificativaCliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            ps.setString(10, invoice.notes);

            ps.executeUpdate();
            System.out.println("Factura rectificativa creada con éxito.");

        } catch (SQLException e) {
            System.err.println("Error al crear factura rectificativa: " + e.getMessage());
        }
    }

    // **GET ALL CORRECTIVE INVOICES**
    public static List<CorrectiveInvoice> getCorrectiveInvoices() {
        List<CorrectiveInvoice> invoices = new ArrayList<>();
        String query = "SELECT * FROM rectificativasclientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(new CorrectiveInvoice(
                        rs.getInt("idRectificativaCliente"),
                        rs.getInt("numeroRectificativaCliente"),
                        rs.getDate("fechaRectificativaCliente"),
                        rs.getInt("idClienteRectificativaCliente"),
                        rs.getInt("idTrabajadorRectificativaCliente"),
                        rs.getDouble("baseImponibleRectificativaCliente"),
                        rs.getDouble("ivaRectificativaCliente"),
                        rs.getDouble("totalRectificativaCliente"),
                        rs.getString("hashRectificativaCliente"),
                        rs.getString("qrRectificativaCliente"),
                        rs.getString("observacionesRectificativaCliente")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener facturas rectificativas: " + e.getMessage());
        }
        return invoices;
    }

    // **GET CORRECTIVE INVOICE BY ID**
    public static CorrectiveInvoice getCorrectiveInvoice(int id) {
        String query = "SELECT * FROM rectificativasclientes WHERE idRectificativaCliente = ?";
        CorrectiveInvoice invoice = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    invoice = new CorrectiveInvoice(
                            rs.getInt("idRectificativaCliente"),
                            rs.getInt("numeroRectificativaCliente"),
                            rs.getDate("fechaRectificativaCliente"),
                            rs.getInt("idClienteRectificativaCliente"),
                            rs.getInt("idTrabajadorRectificativaCliente"),
                            rs.getDouble("baseImponibleRectificativaCliente"),
                            rs.getDouble("ivaRectificativaCliente"),
                            rs.getDouble("totalRectificativaCliente"),
                            rs.getString("hashRectificativaCliente"),
                            rs.getString("qrRectificativaCliente"),
                            rs.getString("observacionesRectificativaCliente")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener factura rectificativa: " + e.getMessage());
        }
        return invoice;
    }

    // **MODIFY CORRECTIVE INVOICE**
    public void modifyCorrectiveInvoice(CorrectiveInvoice invoice, int id) {
        String query = "UPDATE rectificativasclientes SET numeroRectificativaCliente = ?, fechaRectificativaCliente = ?, " +
                "idClienteRectificativaCliente = ?, idTrabajadorRectificativaCliente = ?, baseImponibleRectificativaCliente = ?, " +
                "ivaRectificativaCliente = ?, totalRectificativaCliente = ?, hashRectificativaCliente = ?, " +
                "qrRectificativaCliente = ?, observacionesRectificativaCliente = ? WHERE idRectificativaCliente = ?";

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
            ps.setString(10, invoice.notes);
            ps.setInt(11, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Factura rectificativa actualizada con éxito.");
            } else {
                System.out.println("No se encontró una factura rectificativa con el ID proporcionado.");
            }

        } catch (SQLException e) {
            System.err.println("Error al modificar factura rectificativa: " + e.getMessage());
        }
    }

    // **DELETE CORRECTIVE INVOICE**
    public void deleteCorrectiveInvoice(int id) {
        String query = "DELETE FROM rectificativasclientes WHERE idRectificativaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Factura rectificativa eliminada con éxito.");
            } else {
                System.out.println("No se encontró una factura rectificativa con el ID proporcionado.");
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar factura rectificativa: " + e.getMessage());
        }
    }
}