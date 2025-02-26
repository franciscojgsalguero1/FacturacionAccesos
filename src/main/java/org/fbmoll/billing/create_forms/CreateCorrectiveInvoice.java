package org.fbmoll.billing.create_forms;

import org.fbmoll.billing.resources.Constants;
import org.fbmoll.billing.resources.Utils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateCorrectiveInvoice extends JDialog {
    private final JTable invoiceTable;
    private final List<Integer> invoiceIds = new ArrayList<>();
    private final ActionListener refreshListener;

    public CreateCorrectiveInvoice(JPanel parentPanel, ActionListener refreshListener) {
        setTitle("Crear Factura Rectificativa");
        setSize(800, 500);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);
        this.refreshListener = refreshListener;

        invoiceTable = new JTable();
        setupInvoiceTable();
        loadUncorrectedInvoices();

        JScrollPane tableScrollPane = new JScrollPane(invoiceTable);
        add(tableScrollPane, BorderLayout.CENTER);

        setVisible(true);
    }


    private void setupInvoiceTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Número", "Cliente", "Trabajador", "Base Imponible", "IVA", "Total", "Pagada",
                        Constants.ACTION}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        invoiceTable.setModel(model);
        invoiceTable.getColumn(Constants.ACTION).setCellRenderer(new ButtonRenderer());
        invoiceTable.getColumn(Constants.ACTION).setCellEditor(new ButtonEditor(invoiceIds));
    }

    private void loadUncorrectedInvoices() {
        String query = "SELECT idFacturaCliente, numeroFacturaCliente, idClienteFactura, idTrabajadorFactura, " +
                "baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, cobradaFactura " +
                "FROM facturasclientes WHERE corrected = 0";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) invoiceTable.getModel();

            while (rs.next()) {
                int id = rs.getInt("idFacturaCliente");
                invoiceIds.add(id);
                Object[] rowData = {
                        rs.getInt("numeroFacturaCliente"),
                        rs.getInt("idClienteFactura"),
                        rs.getInt("idTrabajadorFactura"),
                        rs.getDouble("baseImponibleFacturaCliente"),
                        rs.getDouble("ivaFacturaCliente"),
                        rs.getDouble("totalFacturaCliente"),
                        rs.getBoolean("cobradaFactura") ? "Sí" : "No",
                        Constants.RECTIFY
                };
                model.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar facturas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(Constants.RECTIFY);
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedInvoiceId;
        private final List<Integer> invoiceIds;

        public ButtonEditor(List<Integer> invoiceIds) {
            super(new JCheckBox());
            this.invoiceIds = invoiceIds;
            this.button = new JButton(Constants.RECTIFY);
            this.button.addActionListener(e -> rectifyInvoice(selectedInvoiceId));
        }

        private void rectifyInvoice(int invoiceId) {
            String insertQuery = "INSERT INTO rectificativasclientes " +
                    "(idClienteRectificativaCliente, idTrabajadorRectificativaCliente, fechaRectificativaCliente, " +
                    "numeroRectificativaCliente, baseImponibleRectificativaCliente, ivaRectificativaCliente," +
                    " totalRectificativaCliente) SELECT idClienteFactura, idTrabajadorFactura, NOW()," +
                    " idFacturaCliente, baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente " +
                    "FROM facturasclientes WHERE idFacturaCliente = ?";

            String updateQuery = "UPDATE facturasclientes SET corrected = 1 WHERE idFacturaCliente = ?";

            try (Connection conn = Utils.getConnection();
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

                insertStmt.setInt(1, invoiceId);
                insertStmt.executeUpdate();

                updateStmt.setInt(1, invoiceId);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(CreateCorrectiveInvoice.this,
                        "Factura rectificativa creada con éxito.");

                if (refreshListener != null) {
                    SwingUtilities.invokeLater(() -> refreshListener.actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "REFRESH")));
                }

                dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(CreateCorrectiveInvoice.this,
                        "Error al crear la factura rectificativa: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedInvoiceId = invoiceIds.get(row);
            return button;
        }
    }
}