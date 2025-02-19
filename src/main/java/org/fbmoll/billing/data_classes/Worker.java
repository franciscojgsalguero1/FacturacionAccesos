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
public class Worker {
    int id;
    String name;
    String address;
    int postCode;
    String town;
    String province;
    String country;
    String dni;
    String phone;
    String email;
    String position;
    double salary;
    double commissionPercentage;
    String notes;

    public static void showWorkerTable(JPanel panel) {
        List<Worker> workers = Worker.getAllWorkers();
        if (workers.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "No hay trabajadores disponibles.");
            return;
        }

        String[] columnNames = {"ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "DNI", "Teléfono", "Email", "Puesto", "Salario", "Comisión %", "Notas", "Editar", "Eliminar"};

        Object[][] data = new Object[workers.size()][columnNames.length];
        for (int i = 0; i < workers.size(); i++) {
            Worker w = workers.get(i);
            data[i] = new Object[]{w.id, w.name, w.address, w.postCode, w.town, w.province, w.country, w.dni,
                    w.phone, w.email, w.position, w.salary, w.commissionPercentage, w.notes, "📝", "❌"};
        }
    }


    public void createWorker(Worker worker) {
        String query = "INSERT INTO workers (name, address, postCode, town, province, country, dni, phone, " +
                "email, position, salary, commissionPercentage, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, worker.name);
            ps.setString(2, worker.address);
            ps.setInt(3, worker.postCode);
            ps.setString(4, worker.town);
            ps.setString(5, worker.province);
            ps.setString(6, worker.country);
            ps.setString(7, worker.dni);
            ps.setString(8, worker.phone);
            ps.setString(9, worker.email);
            ps.setString(10, worker.position);
            ps.setDouble(11, worker.salary);
            ps.setDouble(12, worker.commissionPercentage);
            ps.setString(13, worker.notes);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Worker> getAllWorkers() {
        List<Worker> workers = new ArrayList<>();
        String query = "SELECT * FROM workers";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                workers.add(new Worker(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getInt("postCode"),
                        rs.getString("town"),
                        rs.getString("province"),
                        rs.getString("country"),
                        rs.getString("dni"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("position"),
                        rs.getDouble("salary"),
                        rs.getDouble("commissionPercentage"),
                        rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return workers;
    }

    public static Worker getWorker(int id) {
        String query = "SELECT * FROM workers WHERE id = ?";
        Worker worker = null;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    worker = new Worker(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getInt("postCode"),
                            rs.getString("town"),
                            rs.getString("province"),
                            rs.getString("country"),
                            rs.getString("dni"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("position"),
                            rs.getDouble("salary"),
                            rs.getDouble("commissionPercentage"),
                            rs.getString("notes")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return worker;
    }

    public void modifyWorker(Worker worker, int id) {
        String query = "UPDATE workers SET name = ?, address = ?, postCode = ?, town = ?, province = ?, country = ?, " +
                "dni = ?, phone = ?, email = ?, position = ?, salary = ?, commissionPercentage = ?, notes = ? WHERE id = ?";

        try (Connection conn = Utils.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, worker.name);
            ps.setString(2, worker.address);
            ps.setInt(3, worker.postCode);
            ps.setString(4, worker.town);
            ps.setString(5, worker.province);
            ps.setString(6, worker.country);
            ps.setString(7, worker.dni);
            ps.setString(8, worker.phone);
            ps.setString(9, worker.email);
            ps.setString(10, worker.position);
            ps.setDouble(11, worker.salary);
            ps.setDouble(12, worker.commissionPercentage);
            ps.setString(13, worker.notes);
            ps.setInt(14, id);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteWorker(int id) {
        String query = "DELETE FROM workers WHERE id = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}