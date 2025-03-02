package org.facturacion.content;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.facturacion.data_classes.Invoice;
import org.facturacion.dto.InvoicePaymentDTO;
import org.facturacion.resources.Constants;
import org.facturacion.resources.Utils;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * Clase utilitaria para la generación de facturas en formato PDF.
 */
public class InvoicePDFGenerator {
    private static final float MARGIN_LEFT = 50; // Margen izquierdo del texto en el PDF
    private static final float START_HEIGHT = 810; // Posición inicial en el eje Y para el contenido
    private static final float LINE_SPACING = 20; // Espaciado entre líneas de texto en el PDF

    /**
     * Constructor privado para evitar la instanciación de la clase.
     */
    private InvoicePDFGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Genera un archivo PDF con los datos de una factura.
     *
     * @param panel Panel donde se mostrará la información.
     * @param listener Listener para gestionar acciones.
     * @param invoiceId ID de la factura a generar.
     * @param filePath Ruta donde se guardará el archivo PDF.
     */
    public static void generateInvoicePDF(JPanel panel, ActionListener listener, int invoiceId, String filePath) {
        Invoice invoice = getInvoiceFromDatabase(panel, listener, invoiceId);
        if (invoice == null) {
            showError("No se encontró la factura con ID: " + invoiceId);
            return;
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                float yPosition = START_HEIGHT;

                // Agregar información al PDF
                yPosition = addTextToPDF(contentStream, "Factura #" + invoice.getInvoicePaymentDTO().getNumber(), PDType1Font.HELVETICA_BOLD, 14, yPosition);
                yPosition = addTextToPDF(contentStream, "Empresa: FB MOLL", yPosition);
                yPosition = addTextToPDF(contentStream, "CIF: 43321423X", yPosition);
                yPosition -= LINE_SPACING;

                yPosition = addTextToPDF(contentStream, "Fecha: " + invoice.getDate(), yPosition);
                yPosition = addTextToPDF(contentStream, "Cliente: " + invoice.getClient(), yPosition);
                yPosition = addTextToPDF(contentStream, "CIF Cliente: " + getCifClient(invoice.getClient()), yPosition);
                yPosition = addTextToPDF(contentStream, "Trabajador: " + invoice.getWorker(), yPosition);
                yPosition = addTextToPDF(contentStream, "CIF Trabajador: " + getCifWorker(invoice.getWorker()), yPosition);
                yPosition -= LINE_SPACING;

                yPosition = addTextToPDF(contentStream, "Forma de Pago: " + invoice.getInvoicePaymentDTO().getPaymentMethod(), yPosition);
                yPosition = addTextToPDF(contentStream, "Base Imponible: " + invoice.getInvoicePaymentDTO().getTaxableAmount() + " €", yPosition);
                yPosition = addTextToPDF(contentStream, "IVA: " + invoice.getInvoicePaymentDTO().getVatAmount() + "%", yPosition);
                yPosition = addTextToPDF(contentStream, "Cantidad IVA: " + (invoice.getInvoicePaymentDTO().getTotalAmount() - invoice.getInvoicePaymentDTO().getTaxableAmount()) + " €", yPosition);
                yPosition = addTextToPDF(contentStream, "Total: " + invoice.getInvoicePaymentDTO().getTotalAmount() + " €", yPosition);
            }

            document.save(new File(filePath));
        } catch (IOException e) {
            showError("Error al generar el PDF: " + e.getMessage());
        }
    }

    /**
     * Obtiene los datos de una factura desde la base de datos.
     */
    private static Invoice getInvoiceFromDatabase(JPanel panel, ActionListener listener, int invoiceId) {
        String query = "SELECT f.idFacturaCliente, f.fechaFacturaCliente, c.nombreCliente, w.name AS trabajadorNombre, fp.tipoFormaPago, " +
                "f.numeroFacturaCliente, f.baseImponibleFacturaCliente, f.ivaFacturaCliente, f.totalFacturaCliente, f.cobradaFactura, " +
                "f.corrected, f.fechaCobroFactura " +
                "FROM facturasclientes f " +
                "JOIN clientes c ON f.idClienteFactura = c.idCliente " +
                "JOIN workers w ON f.idTrabajadorFactura = w.id " +
                "JOIN formapago fp ON f.formaCobroFactura = fp.idFormaPago " +
                "WHERE f.idFacturaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Invoice(
                            panel, listener, rs.getInt("idFacturaCliente"),
                            rs.getDate("fechaFacturaCliente"), rs.getString("nombreCliente"),
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
                    );
                }
            }
        } catch (SQLException e) {
            showError("Error al recuperar la factura: " + e.getMessage());
        }
        return null;
    }

    /**
     * Agrega texto al PDF con una fuente y tamaño específico.
     */
    private static float addTextToPDF(PDPageContentStream contentStream, String text, float yPosition) throws IOException {
        return addTextToPDF(contentStream, text, PDType1Font.HELVETICA, 12, yPosition);
    }

    private static float addTextToPDF(PDPageContentStream contentStream, String text, PDType1Font font, int fontSize, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(MARGIN_LEFT, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - LINE_SPACING;
    }

    /**
     * Obtiene el CIF del cliente desde la base de datos.
     */
    private static String getCifClient(String client) {
        return getCifFromDatabase("SELECT cifCliente FROM clientes WHERE nombreCliente = ?", client);
    }

    /**
     * Obtiene el CIF del trabajador desde la base de datos.
     */
    private static String getCifWorker(String worker) {
        return getCifFromDatabase("SELECT dni FROM workers WHERE name = ?", worker);
    }

    /**
     * Metodo genérico para obtener el CIF de la base de datos.
     */
    private static String getCifFromDatabase(String query, String parameter) {
        try (Connection conn = Utils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, parameter);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            showError("Error al recuperar el CIF: " + e.getMessage());
        }
        return "";
    }

    /**
     * Muestra un mensaje de error utilizando JOptionPane.
     */
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, Constants.ERROR, JOptionPane.ERROR_MESSAGE);
    }
}