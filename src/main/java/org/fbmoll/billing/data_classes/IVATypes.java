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
public class IVATypes {
    int id;
    double amount;
    String description;

    public static void showIVATypesTable(JPanel panel) {
        List<IVATypes> ivaTypes = IVATypes.getAllIVATypes();
        if (ivaTypes.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay tipos de IVA disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Porcentaje", "Descripción", "Editar", "Eliminar"};

        Object[][] data = new Object[ivaTypes.size()][columnNames.length];
        for (int i = 0; i < ivaTypes.size(); i++) {
            IVATypes iva = ivaTypes.get(i);
            data[i] = new Object[]{iva.id, iva.amount, iva.description, "📝", "❌"};
        }
    }


    public void createIVATypes(IVATypes iva) {
        String query = "INSERT INTO tiposiva (iva, observacionesTipoIva) VALUES (?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, iva.amount);
            ps.setString(2, iva.description);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<IVATypes> getAllIVATypes() {
        List<IVATypes> ivaList = new ArrayList<>();
        String query = "SELECT * FROM tiposiva";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ivaList.add(new IVATypes(
                        rs.getInt("idTipoIva"),
                        rs.getDouble("iva"),
                        rs.getString("observacionesTipoIva")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ivaList;
    }

    public static IVATypes getIVATypes(int id) {
        String query = "SELECT * FROM tiposiva WHERE idTipoIva = ?";
        IVATypes iva = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    iva = new IVATypes(
                            rs.getInt("idTipoIva"),
                            rs.getDouble("iva"),
                            rs.getString("observacionesTipoIva")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return iva;
    }

    public void modifyIVATypes(IVATypes iva, int id) {
        String query = "UPDATE tiposiva SET iva = ?, observacionesTipoIva = ? WHERE idTipoIva = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, iva.amount);
            ps.setString(2, iva.description);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteIVATypes(int id) {
        String query = "DELETE FROM tiposiva WHERE idTipoIva = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}