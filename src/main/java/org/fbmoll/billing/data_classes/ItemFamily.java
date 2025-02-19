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
public class ItemFamily {
    int id;
    String code;
    String description;

    public static void showItemFamilyTable(JPanel panel) {
        List<ItemFamily> families = ItemFamily.getAllItemFamilies();
        if (families.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay familias de artículos disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Código", "Descripción", "Editar", "Eliminar"};

        Object[][] data = new Object[families.size()][columnNames.length];
        for (int i = 0; i < families.size(); i++) {
            ItemFamily family = families.get(i);
            data[i] = new Object[]{family.id, family.code, family.description, "📝", "❌"};
        }
    }


    public void createItemFamily(ItemFamily family) {
        String query = "INSERT INTO familiaarticulos (codigoFamiliaArticulos, denominacionFamilias) VALUES (?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, family.code);
            ps.setString(2, family.description);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ItemFamily> getAllItemFamilies() {
        List<ItemFamily> families = new ArrayList<>();
        String query = "SELECT * FROM familiaarticulos";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                families.add(new ItemFamily(
                        rs.getInt("idFamiliaArticulos"),
                        rs.getString("codigoFamiliaArticulos"),
                        rs.getString("denominacionFamilias")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return families;
    }

    public static ItemFamily getItemFamily(int id) {
        String query = "SELECT * FROM familiaarticulos WHERE idFamiliaArticulos = ?";
        ItemFamily family = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    family = new ItemFamily(
                            rs.getInt("idFamiliaArticulos"),
                            rs.getString("codigoFamiliaArticulos"),
                            rs.getString("denominacionFamilias")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return family;
    }

    public void modifyItemFamily(ItemFamily family, int id) {
        String query = "UPDATE familiaarticulos SET codigoFamiliaArticulos = ?, denominacionFamilias = ? WHERE idFamiliaArticulos = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, family.code);
            ps.setString(2, family.description);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteItemFamily(int id) {
        String query = "DELETE FROM familiaarticulos WHERE idFamiliaArticulos = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}