package org.fbmoll.billing.resources;

import org.fbmoll.billing.data_classes.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Queries {
    private Queries() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static List<Client> queryGetAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = """
                    SELECT idCliente, nombreCliente, direccionCliente, cpCliente, poblacionCliente,
                           provinciaCliente, paisCliente, cifCliente, telCliente, emailCliente,
                           ibanCliente, riesgoCliente, descuentoCliente, observacionesCliente
                    FROM clientes
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                clients.add(new Client(
                        resultSet.getInt("idCliente"),
                        resultSet.getString("nombreCliente"),
                        resultSet.getString("direccionCliente"),
                        resultSet.getInt("cpCliente"),
                        resultSet.getString("poblacionCliente"),
                        resultSet.getString("provinciaCliente"),
                        resultSet.getString("paisCliente"),
                        resultSet.getString("cifCliente"),
                        resultSet.getString("telCliente"),
                        resultSet.getString("emailCliente"),
                        resultSet.getString("ibanCliente"),
                        resultSet.getDouble("riesgoCliente"),
                        resultSet.getDouble("descuentoCliente"),
                        resultSet.getString("observacionesCliente")
                ));
            }
        }
        return clients;
    }

    public static List<CorrectiveInvoice> queryGetCorrective() throws SQLException {
        List<CorrectiveInvoice> correctiveInvoices = new ArrayList<>();
        String query = """
                    SELECT idRectificativaCliente, numeroRectificativaCliente, fechaRectificativaCliente,
                           idClienteRectificativaCliente, baseImponibleRectificativaCliente, ivaRectificativaCliente,
                           totalRectificativaCliente, hashRectificativaCliente, qrRectificativaCliente, observacionesRectificativaCliente
                    FROM rectificativasclientes
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                correctiveInvoices.add(new CorrectiveInvoice(
                        resultSet.getInt("idRectificativaCliente"),
                        resultSet.getInt("numeroRectificativaCliente"),
                        resultSet.getDate("fechaRectificativaCliente"),
                        resultSet.getInt("idClienteRectificativaCliente"),
                        resultSet.getInt("idTrabajadorRectificativaCliente"), // TODO ADD TO BD
                        resultSet.getDouble("baseImponibleRectificativaCliente"),
                        resultSet.getDouble("ivaRectificativaCliente"),
                        resultSet.getDouble("totalRectificativaCliente"),
                        resultSet.getString("hashRectificativaCliente"),
                        resultSet.getString("qrRectificativaCliente"),
                        resultSet.getString("observacionesRectificativaCliente")
                ));
            }
        }
        return correctiveInvoices;
    }

    public static List<Invoice> queryGetInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String query = """
                    SELECT idFacturaCliente, numeroFacturaCliente, fechaFacturaCliente, idClienteFactura,
                           baseImponibleFacturaCliente, ivaFacturaCliente, totalFacturaCliente, hashFacturaCliente,
                           qrFacturaCliente, cobradaFactura, formaCobroFactura, fechaCobroFactura, observacionesFacturaClientes
                    FROM facturasclientes
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                invoices.add(new Invoice(
                        resultSet.getInt("idFacturaCliente"),
                        resultSet.getInt("numeroFacturaCliente"),
                        resultSet.getDate("fechaFacturaCliente"),
                        resultSet.getInt("idClienteFactura"),
                        resultSet.getInt("idTrabajadorFactura"), // TODO ADD TO DB
                        resultSet.getInt("baseImponibleFacturaCliente"),
                        resultSet.getInt("ivaFacturaCliente"),
                        resultSet.getInt("totalFacturaCliente"),
                        resultSet.getString("hashFacturaCliente"),
                        resultSet.getString("qrFacturaCliente"),
                        resultSet.getBoolean("cobradaFactura"),
                        resultSet.getInt("formaCobroFactura"),
                        resultSet.getDate("fechaCobroFactura"),
                        resultSet.getString("observacionesFacturaClientes")
                ));
            }
        }
        return invoices;
    }

    public static List<ItemFamily> queryGetItemFamilies() throws SQLException {
        List<ItemFamily> articleFamilies = new ArrayList<>();
        String query = """
                    SELECT idFamiliaArticulos, codigoFamiliaArticulos, denominacionFamilias
                    FROM familiaarticulos
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                articleFamilies.add(new ItemFamily(
                        resultSet.getInt("idFamiliaArticulos"),
                        resultSet.getString("codigoFamiliaArticulos"),
                        resultSet.getString("denominacionFamilias")
                ));
            }
        }
        return articleFamilies;
    }

    public static List<Item> queryGetArticles() throws SQLException {
        List<Item> articles = new ArrayList<>();
        String query = """
                    SELECT idArticulo, codigoArticulo, codigoBarrasArticulo, descripcionArticulo,
                           familiaArticulo, costeArticulo, margenComercialArticulo, pvpArticulo,
                           proveedorArticulo, stockArticulo, observacionesArticulo
                    FROM articulos
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                articles.add(new Item(
                        resultSet.getInt("idArticulo"),
                        resultSet.getString("codigoArticulo"),
                        resultSet.getString("codigoBarrasArticulo"),
                        resultSet.getString("descripcionArticulo"),
                        resultSet.getInt("familiaArticulo"),
                        resultSet.getDouble("costeArticulo"),
                        resultSet.getDouble("margenComercialArticulo"),
                        resultSet.getDouble("pvpArticulo"),
                        resultSet.getInt("proveedorArticulo"),
                        resultSet.getInt("stockArticulo"),
                        resultSet.getString("observacionesArticulo")
                ));
            }
        }
        return articles;
    }

    public static List<IVATypes> queryGetIVATypes() throws SQLException {
        List<IVATypes> ivaTypes = new ArrayList<>();
        String query = """
                    SELECT idTipoIva, iva, observacionesTipoIva
                    FROM tiposiva
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                ivaTypes.add(new IVATypes(
                        resultSet.getInt("idTipoIva"),
                        resultSet.getDouble("iva"),
                        resultSet.getString("observacionesTipoIva")
                ));
            }
        }
        return ivaTypes;
    }

    public static List<Provider> queryGetProviders() throws SQLException {
        List<Provider> providers = new ArrayList<>();
        String query = """
                    SELECT idProveedor, nombreProveedor, direccionProveedor, cpProveedor,
                           poblacionProveedor, provinciaProveedor, paisProveedor, cifProveedor,
                           telProveedor, emailProveedor, webProveedor, observacionesProveedor
                    FROM proveedores
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement statement = conn.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                providers.add(new Provider(
                        resultSet.getInt("idProveedor"),
                        resultSet.getString("nombreProveedor"),
                        resultSet.getString("direccionProveedor"),
                        resultSet.getInt("cpProveedor"),
                        resultSet.getString("poblacionProveedor"),
                        resultSet.getString("provinciaProveedor"),
                        resultSet.getString("paisProveedor"),
                        resultSet.getString("cifProveedor"),
                        resultSet.getString("telProveedor"),
                        resultSet.getString("emailProveedor"),
                        resultSet.getString("webProveedor"),
                        resultSet.getString("observacionesProveedor")
                ));
            }
        }
        return providers;
    }

    public static List<String> queryGetWorkerNames() {
        List<String> workers = new ArrayList<>(); // TODO IMPLEMENT
        return workers;
    }

    public static List<Worker> queryGetWorkers() {
        List<Worker> workers = new ArrayList<>(); // TODO IMPLEMENT
        return workers;
    }

    public static int queryGetWorkerId(String name) {
        return 0; // TODO IMPLEMENT
    }
}