package org.fbmoll.billing.content;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.fbmoll.billing.data_classes.Invoice;
import org.fbmoll.billing.dto.InvoicePaymentDTO;
import org.fbmoll.billing.resources.Constants;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InvoicePDFGenerator {
    // Private constructor to prevent instantiation
    private InvoicePDFGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void generateInvoicePDF(JPanel panel, ActionListener listener, int invoiceId, String filePath) {
        Invoice invoice = getInvoiceFromDatabase(panel, listener, invoiceId);
        if (invoice == null) {
            JOptionPane.showMessageDialog(null, "No se encontró la factura con ID: " + invoiceId,
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 810);
            contentStream.showText("Factura #" + invoice.getInvoicePaymentDTO().getNumber());
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 790);
            contentStream.showText("Empresa: FB MOLL");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 770);
            contentStream.showText("CIF: 43321423X");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("Fecha: " + invoice.getDate());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText("Cliente: " + invoice.getClient());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 690);
            contentStream.showText("CIF Cliente: " + getCifClient(invoice.getClient()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 670);
            contentStream.showText("Trabajador: " + invoice.getWorker());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 650);
            contentStream.showText("CIF Trabajador: " + getCifWorker(invoice.getWorker()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 610);
            contentStream.showText("Forma de Pago: " + invoice.getInvoicePaymentDTO().getPaymentMethod());
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 590);
            contentStream.showText("Base Imponible: " + invoice.getInvoicePaymentDTO().getTaxableAmount() + " €");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 570);
            contentStream.showText("IVA: " + invoice.getInvoicePaymentDTO().getVatAmount() + "%");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 550);
            contentStream.showText("Cantidad IVA: " + (invoice.getInvoicePaymentDTO().getTotalAmount() -
                    invoice.getInvoicePaymentDTO().getTaxableAmount()) + " €");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, 530);
            contentStream.showText("Total: " + invoice.getInvoicePaymentDTO().getTotalAmount() + " €");
            contentStream.endText();

            contentStream.close();
            document.save(new File(filePath));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String getCifClient(String client) {
        String cif = "";
        String query = "SELECT cifCliente FROM clientes WHERE nombreCliente = ?";
        try (Connection conn = Utils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, client);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cif = rs.getString("cifCliente");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al recuperar el CIF del cliente: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
        return cif;
    }

    private static String getCifWorker(String client) {
        String cif = "";
        String query = "SELECT dni FROM workers WHERE name = ?";
        try (Connection conn = Utils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, client);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cif = rs.getString("dni");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al recuperar el CIF del trabajador: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
        return cif;
    }

    private static Invoice getInvoiceFromDatabase(JPanel panel, ActionListener listener, int invoiceId) {
        Invoice invoice = null;
        String query = "SELECT f.idFacturaCliente, f.fechaFacturaCliente, " +
                "c.nombreCliente, w.name AS trabajadorNombre, fp.tipoFormaPago, " +
                "f.numeroFacturaCliente, f.baseImponibleFacturaCliente, " +
                "f.ivaFacturaCliente, f.totalFacturaCliente, f.cobradaFactura, " +
                "f.corrected, f.fechaCobroFactura " +
                "FROM facturasclientes f " +
                "JOIN clientes c ON f.idClienteFactura = c.idCliente " +
                "JOIN workers w ON f.idTrabajadorFactura = w.id " +
                "JOIN formapago fp ON f.formaCobroFactura = fp.idFormaPago " +
                "WHERE f.idFacturaCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, invoiceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                invoice = new Invoice(
                        panel,
                        listener,
                        rs.getInt("idFacturaCliente"),
                        rs.getDate("fechaFacturaCliente"),
                        rs.getString("nombreCliente"),
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al recuperar la factura: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return invoice;
    }
}