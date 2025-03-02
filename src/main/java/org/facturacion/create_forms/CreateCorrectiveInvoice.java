package org.facturacion.create_forms;

import org.facturacion.resources.Constants;
import org.facturacion.resources.Utils;

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

/**
 * Clase para crear una Factura Rectificativa.
 * Muestra una lista de facturas no rectificadas y permite seleccionar una para crear su rectificativa.
 */
public class CreateCorrectiveInvoice extends JDialog {
    private final JTable invoiceTable;
    private final List<Integer> invoiceIds = new ArrayList<>();
    private final ActionListener refreshListener;

    /**
     * Constructor que inicializa la interfaz gráfica.
     * @param parentPanel Panel principal donde se mostrará el formulario.
     * @param refreshListener Listener para actualizar la tabla después de la acción.
     */
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

        add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Configura la tabla de facturas.
     */
    private void setupInvoiceTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Número", "Cliente", "Trabajador", "Base Imponible", "IVA", "Total", "Pagada", Constants.ACTION}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        invoiceTable.setModel(model);
        invoiceTable.getColumn(Constants.ACTION).setCellRenderer(new ButtonRenderer());
        invoiceTable.getColumn(Constants.ACTION).setCellEditor(new ButtonEditor(invoiceIds));
    }

    /**
     * Carga las facturas no rectificadas desde la base de datos.
     */
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
                model.addRow(new Object[]{
                        rs.getInt("numeroFacturaCliente"),
                        rs.getInt("idClienteFactura"),
                        rs.getInt("idTrabajadorFactura"),
                        rs.getDouble("baseImponibleFacturaCliente"),
                        rs.getDouble("ivaFacturaCliente"),
                        rs.getDouble("totalFacturaCliente"),
                        rs.getBoolean("cobradaFactura") ? "Sí" : "No",
                        Constants.RECTIFY
                });
            }
        } catch (SQLException e) {
            showError("Error al cargar facturas: " + e.getMessage());
        }
    }

    /**
     * Muestra un mensaje de error en un cuadro de diálogo.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Renderizador de botón para la tabla.
     */
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

    /**
     * Editor de celda para los botones en la tabla.
     */
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

        /**
         * Crea la factura rectificativa a partir de una factura existente.
         */
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
                showError("Error al crear la factura rectificativa: " + ex.getMessage());
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