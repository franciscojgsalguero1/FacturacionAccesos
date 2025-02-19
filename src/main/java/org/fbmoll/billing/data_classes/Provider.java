package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
@Getter
public class Provider {
    int id;
    String name;
    String address;
    int postCode;
    String town;
    String province;
    String country;
    String cif;
    String phone;
    String email;
    String website;
    String notes;

    public static void showProviderTable(JPanel panel) {
        List<Provider> providers = Provider.getAllProviders();
        if (providers.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay proveedores disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web", "Notas", "Editar", "Eliminar"};

        Object[][] data = new Object[providers.size()][columnNames.length];
        for (int i = 0; i < providers.size(); i++) {
            Provider p = providers.get(i);
            data[i] = new Object[]{p.id, p.name, p.address, p.postCode, p.town, p.province, p.country,
                    p.cif, p.phone, p.email, p.website, p.notes, "📝", "❌"};
        }
    }


    // **CREATE PROVIDER**
    public void createProvider(Provider provider) {
        String query = "INSERT INTO proveedores (nombreProveedor, direccionProveedor, cpProveedor, poblacionProveedor, " +
                "provinciaProveedor, paisProveedor, cifProveedor, telProveedor, emailProveedor, webProveedor, observacionesProveedor) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, provider.name);
            ps.setString(2, provider.address);
            ps.setInt(3, provider.postCode);
            ps.setString(4, provider.town);
            ps.setString(5, provider.province);
            ps.setString(6, provider.country);
            ps.setString(7, provider.cif);
            ps.setString(8, provider.phone);
            ps.setString(9, provider.email);
            ps.setString(10, provider.website);
            ps.setString(11, provider.notes);

            ps.executeUpdate();
            System.out.println("Proveedor creado con éxito.");

        } catch (SQLException e) {
            System.err.println("Error al crear proveedor: " + e.getMessage());
        }
    }

    // **GET ALL PROVIDERS**
    public static List<Provider> getAllProviders() {
        List<Provider> providers = new ArrayList<>();
        String query = "SELECT * FROM proveedores";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                providers.add(new Provider(
                        rs.getInt("idProveedor"),
                        rs.getString("nombreProveedor"),
                        rs.getString("direccionProveedor"),
                        rs.getInt("cpProveedor"),
                        rs.getString("poblacionProveedor"),
                        rs.getString("provinciaProveedor"),
                        rs.getString("paisProveedor"),
                        rs.getString("cifProveedor"),
                        rs.getString("telProveedor"),
                        rs.getString("emailProveedor"),
                        rs.getString("webProveedor"),
                        rs.getString("observacionesProveedor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores: " + e.getMessage());
        }
        return providers;
    }

    // **GET PROVIDER BY ID**
    public static Provider getProvider(int id) {
        String query = "SELECT * FROM proveedores WHERE idProveedor = ?";
        Provider provider = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    provider = new Provider(
                            rs.getInt("idProveedor"),
                            rs.getString("nombreProveedor"),
                            rs.getString("direccionProveedor"),
                            rs.getInt("cpProveedor"),
                            rs.getString("poblacionProveedor"),
                            rs.getString("provinciaProveedor"),
                            rs.getString("paisProveedor"),
                            rs.getString("cifProveedor"),
                            rs.getString("telProveedor"),
                            rs.getString("emailProveedor"),
                            rs.getString("webProveedor"),
                            rs.getString("observacionesProveedor")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedor: " + e.getMessage());
        }
        return provider;
    }

    // **MODIFY PROVIDER**
    public void modifyProvider(Provider provider, int id) {
        String query = "UPDATE proveedores SET nombreProveedor = ?, direccionProveedor = ?, cpProveedor = ?, " +
                "poblacionProveedor = ?, provinciaProveedor = ?, paisProveedor = ?, cifProveedor = ?, " +
                "telProveedor = ?, emailProveedor = ?, webProveedor = ?, observacionesProveedor = ? WHERE idProveedor = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, provider.name);
            ps.setString(2, provider.address);
            ps.setInt(3, provider.postCode);
            ps.setString(4, provider.town);
            ps.setString(5, provider.province);
            ps.setString(6, provider.country);
            ps.setString(7, provider.cif);
            ps.setString(8, provider.phone);
            ps.setString(9, provider.email);
            ps.setString(10, provider.website);
            ps.setString(11, provider.notes);
            ps.setInt(12, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Proveedor actualizado con éxito.");
            } else {
                System.out.println("No se encontró un proveedor con el ID proporcionado.");
            }

        } catch (SQLException e) {
            System.err.println("Error al modificar proveedor: " + e.getMessage());
        }
    }

    // **DELETE PROVIDER**
    public void deleteProvider(int id) {
        String query = "DELETE FROM proveedores WHERE idProveedor = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Proveedor eliminado con éxito.");
            } else {
                System.out.println("No se encontró un proveedor con el ID proporcionado.");
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar proveedor: " + e.getMessage());
        }
    }
}