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
    private final JTable invoiceTable; // Tabla para mostrar las facturas disponibles
    private final List<Integer> invoiceIds = new ArrayList<>(); // Lista de IDs de facturas no rectificadas
    private final ActionListener refreshListener; // Listener para actualizar la interfaz después de crear la rectificativa

    /**
     * Constructor que inicializa la interfaz gráfica.
     * @param parentPanel Panel principal donde se mostrará el formulario.
     * @param refreshListener Listener para actualizar la tabla después de la acción.
     */
    public CreateCorrectiveInvoice(JPanel parentPanel, ActionListener refreshListener) {
        setTitle("Crear Factura Rectificativa");
        setSize(800, 500);
        setLayout(new BorderLayout());
        setModal(true); // Hace que la ventana bloquee la interacción con la principal
        setLocationRelativeTo(parentPanel); // Centra la ventana respecto al panel padre
        this.refreshListener = refreshListener;

        invoiceTable = new JTable(); // Inicializa la tabla
        setupInvoiceTable(); // Configura la tabla
        loadUncorrectedInvoices(); // Carga las facturas no rectificadas

        JScrollPane tableScrollPane = new JScrollPane(invoiceTable);
        add(tableScrollPane, BorderLayout.CENTER);

        setVisible(true); // Muestra la ventana
    }

    /**
     * Configura el modelo y los componentes de la tabla de facturas.
     */
    private void setupInvoiceTable() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Número", "Cliente", "Trabajador", "Base Imponible", "IVA", "Total", "Pagada",
                        Constants.ACTION}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Solo la columna de acción (rectificar) es editable
            }
        };
        invoiceTable.setModel(model);

        // Configura la columna de botones para rectificar facturas
        invoiceTable.getColumn(Constants.ACTION).setCellRenderer(new ButtonRenderer());
        invoiceTable.getColumn(Constants.ACTION).setCellEditor(new ButtonEditor(invoiceIds));
    }

    /**
     * Carga las facturas no rectificadas desde la base de datos y las añade a la tabla.
     */
    private void loadUncorrectedInvoices() {
        String query = "SELECT idFacturaCliente, numeroFacturaCliente, idClienteFactura, idTrabajadorFactura, " +
                "baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, cobradaFactura " +
                "FROM facturasclientes WHERE corrected = 0"; // Solo selecciona facturas que aún no han sido rectificadas

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = (DefaultTableModel) invoiceTable.getModel();

            while (rs.next()) {
                int id = rs.getInt("idFacturaCliente");
                invoiceIds.add(id); // Almacena los IDs de las facturas disponibles

                Object[] rowData = {
                        rs.getInt("numeroFacturaCliente"),
                        rs.getInt("idClienteFactura"),
                        rs.getInt("idTrabajadorFactura"),
                        rs.getDouble("baseImponibleFacturaCliente"),
                        rs.getDouble("ivaFacturaCliente"),
                        rs.getDouble("totalFacturaCliente"),
                        rs.getBoolean("cobradaFactura") ? "Sí" : "No",
                        Constants.RECTIFY // Botón de acción
                };
                model.addRow(rowData);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar facturas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Clase interna para renderizar un botón en la tabla.
     */
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(Constants.RECTIFY); // Texto del botón
            return this;
        }
    }

    /**
     * Clase interna que maneja la edición del botón en la tabla.
     */
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button; // Botón que se mostrará en la celda
        private int selectedInvoiceId; // ID de la factura seleccionada
        private final List<Integer> invoiceIds; // Lista de IDs de facturas

        /**
         * Constructor del editor de botones.
         * @param invoiceIds Lista de IDs de facturas disponibles.
         */
        public ButtonEditor(List<Integer> invoiceIds) {
            super(new JCheckBox());
            this.invoiceIds = invoiceIds;
            this.button = new JButton(Constants.RECTIFY);
            this.button.addActionListener(e -> rectifyInvoice(selectedInvoiceId)); // Acción del botón
        }

        /**
         * Método para crear la factura rectificativa a partir de una factura existente.
         * @param invoiceId ID de la factura a rectificar.
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
                insertStmt.executeUpdate(); // Inserta la nueva factura rectificativa

                updateStmt.setInt(1, invoiceId);
                updateStmt.executeUpdate(); // Marca la factura original como rectificada

                JOptionPane.showMessageDialog(CreateCorrectiveInvoice.this,
                        "Factura rectificativa creada con éxito.");

                // Refrescar la tabla principal
                if (refreshListener != null) {
                    SwingUtilities.invokeLater(() -> refreshListener.actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "REFRESH")));
                }

                dispose(); // Cierra la ventana después de la creación

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(CreateCorrectiveInvoice.this,
                        "Error al crear la factura rectificativa: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedInvoiceId = invoiceIds.get(row); // Obtiene el ID de la factura en la fila seleccionada
            return button;
        }
    }
}