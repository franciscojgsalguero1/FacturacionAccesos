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
public class Item {
    int id;
    String code;
    String barCode;
    String description;
    int familyId;
    double cost;
    double margin;
    double price;
    int supplier;
    int stock;
    String notes;

    public static void showItemTable(JPanel panel) {
        List<Item> items = Item.getAllItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay artículos disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Código", "Código de Barras", "Descripción", "ID Familia", "Costo",
                "Margen", "Precio", "Proveedor", "Stock", "Notas", "Editar", "Eliminar"};

        Object[][] data = new Object[items.size()][columnNames.length];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            data[i] = new Object[]{item.id, item.code, item.barCode, item.description, item.familyId,
                    item.cost, item.margin, item.price, item.supplier, item.stock, item.notes, "📝", "❌"};
        }
    }


    public void createItem(Item item) {
        String query = "INSERT INTO articulos (codigoArticulo, codigoBarrasArticulo, descripcionArticulo, " +
                "familiaArticulo, costeArticulo, margenComercialArticulo, pvpArticulo, proveedorArticulo, stockArticulo, observacionesArticulo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, item.code);
            ps.setString(2, item.barCode);
            ps.setString(3, item.description);
            ps.setInt(4, item.familyId);
            ps.setDouble(5, item.cost);
            ps.setDouble(6, item.margin);
            ps.setDouble(7, item.price);
            ps.setInt(8, item.supplier);
            ps.setInt(9, item.stock);
            ps.setString(10, item.notes);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String query = "SELECT * FROM articulos";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(new Item(
                        rs.getInt("idArticulo"),
                        rs.getString("codigoArticulo"),
                        rs.getString("codigoBarrasArticulo"),
                        rs.getString("descripcionArticulo"),
                        rs.getInt("familiaArticulo"),
                        rs.getDouble("costeArticulo"),
                        rs.getDouble("margenComercialArticulo"),
                        rs.getDouble("pvpArticulo"),
                        rs.getInt("proveedorArticulo"),
                        rs.getInt("stockArticulo"),
                        rs.getString("observacionesArticulo")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static Item getItem(int id) {
        String query = "SELECT * FROM articulos WHERE idArticulo = ?";
        Item item = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item = new Item(
                            rs.getInt("idArticulo"),
                            rs.getString("codigoArticulo"),
                            rs.getString("codigoBarrasArticulo"),
                            rs.getString("descripcionArticulo"),
                            rs.getInt("familiaArticulo"),
                            rs.getDouble("costeArticulo"),
                            rs.getDouble("margenComercialArticulo"),
                            rs.getDouble("pvpArticulo"),
                            rs.getInt("proveedorArticulo"),
                            rs.getInt("stockArticulo"),
                            rs.getString("observacionesArticulo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }

    public void modifyItem(Item item, int id) {
        String query = "UPDATE articulos SET codigoArticulo = ?, codigoBarrasArticulo = ?, descripcionArticulo = ?, " +
                "familiaArticulo = ?, costeArticulo = ?, margenComercialArticulo = ?, pvpArticulo = ?, " +
                "proveedorArticulo = ?, stockArticulo = ?, observacionesArticulo = ? WHERE idArticulo = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, item.code);
            ps.setString(2, item.barCode);
            ps.setString(3, item.description);
            ps.setInt(4, item.familyId);
            ps.setDouble(5, item.cost);
            ps.setDouble(6, item.margin);
            ps.setDouble(7, item.price);
            ps.setInt(8, item.supplier);
            ps.setInt(9, item.stock);
            ps.setString(10, item.notes);
            ps.setInt(11, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(int id) {
        String query = "DELETE FROM articulos WHERE idArticulo = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}