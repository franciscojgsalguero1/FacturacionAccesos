package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.create_forms.CreateWorkerForm;
import org.fbmoll.billing.dto.AddressDTO;
import org.fbmoll.billing.dto.WorkerDTO;
import org.fbmoll.billing.resources.*;
import org.fbmoll.billing.resources.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Worker {
    final JPanel panel;
    final int id;
    final AddressDTO address;
    final WorkerDTO workerDTO;
    final double salary;
    final double commissionPercentage;
    final Button edit;
    final Button delete;

    static final Logger logger = LoggerFactory.getLogger(Worker.class);

    public Worker(JPanel panel, ActionListener listener, int id, AddressDTO address, WorkerDTO workerDTO,
                  double salary, double commissionPercentage) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.workerDTO = workerDTO;
        this.salary = salary;
        this.commissionPercentage = commissionPercentage;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.WORKER_EDIT);
            listener.actionPerformed(event);
        });

        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.WORKER_DELETE);
            listener.actionPerformed(event);
        });
    }

    public void modifyWorker(JPanel panel, Worker updatedWorker, int id) {
        String query = "UPDATE workers SET name = ?, address = ?, postCode = ?, town = ?, province = ?, country = ?, " +
                "dni = ?, phone = ?, email = ?, position = ?, salary = ?, commissionPercentage = ? WHERE id = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, updatedWorker.getWorkerDTO().getName());
            ps.setString(2, updatedWorker.getAddress().getStreet());
            ps.setInt(3, updatedWorker.getAddress().getPostCode());
            ps.setString(4, updatedWorker.getAddress().getTown());
            ps.setString(5, updatedWorker.getAddress().getProvince());
            ps.setString(6, updatedWorker.getAddress().getCountry());
            ps.setString(7, updatedWorker.getWorkerDTO().getCif());
            ps.setString(8, updatedWorker.getWorkerDTO().getNumber());
            ps.setString(9, updatedWorker.getWorkerDTO().getEmail());
            ps.setString(10, updatedWorker.getWorkerDTO().getPosition());
            ps.setDouble(11, updatedWorker.getSalary());
            ps.setDouble(12, updatedWorker.getCommissionPercentage());
            ps.setInt(13, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Trabajador actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se pudo actualizar el trabajador.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al modificar trabajador: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> showWorkerTable(panel, e -> {}));
    }

    public void deleteWorker(JPanel panel, int id) {
        int confirm = JOptionPane.showConfirmDialog(panel,
                "¿Estás seguro de que deseas eliminar al trabajador con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM workers WHERE id = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(panel, "Trabajador eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(panel, "No se encontró un trabajador con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al eliminar trabajador: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            Worker.showWorkerTable(panel, e -> {});
            panel.revalidate();
            panel.repaint();
        });
    }

    public void modifyWorkerAction(JPanel panel, ActionListener listener) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(panel),
                "Modificar Trabajador", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(panel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2));

        JTextField nameField = new JTextField(this.getWorkerDTO().getName());
        JTextField addressField = new JTextField(this.getAddress().getStreet());
        JTextField postCodeField = new JTextField(String.valueOf(this.getAddress().getPostCode()));
        JTextField townField = new JTextField(this.getAddress().getTown());
        JTextField provinceField = new JTextField(this.getAddress().getProvince());
        JTextField countryField = new JTextField(this.getAddress().getCountry());
        JTextField cifField = new JTextField(this.getWorkerDTO().getCif());
        JTextField phoneField = new JTextField(this.getWorkerDTO().getNumber());
        JTextField emailField = new JTextField(this.getWorkerDTO().getEmail());
        JTextField positionField = new JTextField(this.getWorkerDTO().getPosition());
        JTextField salaryField = new JTextField(String.valueOf(this.getSalary()));
        JTextField commissionField = new JTextField(String.valueOf(this.getCommissionPercentage()));

        formPanel.add(new JLabel("Nombre:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Dirección:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Código Postal:"));
        formPanel.add(postCodeField);
        formPanel.add(new JLabel("Ciudad:"));
        formPanel.add(townField);
        formPanel.add(new JLabel("Provincia:"));
        formPanel.add(provinceField);
        formPanel.add(new JLabel("País:"));
        formPanel.add(countryField);
        formPanel.add(new JLabel("DNI:"));
        formPanel.add(cifField);
        formPanel.add(new JLabel("Teléfono:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Puesto:"));
        formPanel.add(positionField);
        formPanel.add(new JLabel("Salario:"));
        formPanel.add(salaryField);
        formPanel.add(new JLabel("Comisión %:"));
        formPanel.add(commissionField);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                Worker updatedWorker = new Worker(
                        this.panel, listener, this.getId(),
                        new AddressDTO(
                                addressField.getText(), Integer.parseInt(postCodeField.getText()), townField.getText(),
                                provinceField.getText(), countryField.getText()
                        ),
                        new WorkerDTO(
                                nameField.getText(), cifField.getText(), phoneField.getText(), emailField.getText(),
                                positionField.getText()
                        ),
                        Double.parseDouble(salaryField.getText()),
                        Double.parseDouble(commissionField.getText())
                );

                this.modifyWorker(panel, updatedWorker, this.getId());
                JOptionPane.showMessageDialog(dialog, "Trabajador actualizado con éxito.");
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar trabajador: " + ex.getMessage(),
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public static void showWorkerTable(JPanel panel, ActionListener listener) {
        List<Worker> workers = Worker.getAllWorkers(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Trabajador");

        String[] filterOptions = {
                "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "DNI", "Teléfono", "Email", "Puesto"
        };

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar trabajador...");

        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateWorkerForm(panel));
        JTable table = setupWorkerTable(workers, listener, panel);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }

            private void applyFilter() {
                String text = searchField.getText().trim();
                int columnIndex = filterDropdown.getSelectedIndex() + 1;
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
            }
        });

        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    private static JTable setupWorkerTable(List<Worker> workers, ActionListener listener, JPanel panel) {
        String[] columnNames = {
                "ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "DNI", "Teléfono", "Email", "Puesto", "Salario", "Comisión %",
                Constants.BUTTON_EDIT, Constants.BUTTON_DELETE
        };

        Object[][] data = new Object[workers.size()][columnNames.length];
        for (int i = 0; i < workers.size(); i++) {
            Worker w = workers.get(i);
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            data[i] = new Object[]{
                    w.id, w.getWorkerDTO().getName(), w.getAddress().getStreet(), w.getAddress().getPostCode(),
                    w.getAddress().getTown(), w.getAddress().getProvince(), w.getAddress().getCountry(),
                    w.getWorkerDTO().getCif(), w.getWorkerDTO().getNumber(), w.getWorkerDTO().getEmail(),
                    w.getWorkerDTO().getPosition(), w.salary, w.commissionPercentage, editButton, deleteButton
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 13 || column == 14;
            }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, workers, panel, Constants.WORKER_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(new JCheckBox(), listener, workers, panel, Constants.WORKER_DELETE));

        return table;
    }


    public static List<Worker> getAllWorkers(JPanel panel, ActionListener listener) {
        List<Worker> workers = new ArrayList<>();
        String query = "SELECT id, name, address, postCode, town, province, country, dni, phone, email, position, " +
                "salary, commissionPercentage FROM workers";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                workers.add(new Worker(
                        panel, listener, rs.getInt("id"),
                        new AddressDTO(
                                rs.getString("address"), rs.getInt("postCode"),
                                rs.getString("town"), rs.getString("province"),
                                rs.getString("country")
                        ),
                        new WorkerDTO(
                                rs.getString("name"), rs.getString("dni"),
                                rs.getString("phone"), rs.getString("email"),
                                rs.getString("position")
                        ),
                        rs.getDouble("salary"),
                        rs.getDouble("commissionPercentage")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al obtener trabajadores: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
        return workers;
    }
}