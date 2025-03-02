package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateWorkerForm;
import org.facturacion.dto.AddressDTO;
import org.facturacion.dto.WorkerDTO;
import org.facturacion.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.Button;
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
    // Logger para registrar eventos y errores
    static final Logger logger = LoggerFactory.getLogger(Worker.class);

    // Atributos de la clase
    final JPanel panel;                  // Panel donde se mostrará la tabla de trabajadores
    final int id;                         // ID único del trabajador en la base de datos
    final AddressDTO address;             // Dirección del trabajador
    final WorkerDTO workerDTO;            // Datos personales del trabajador
    final double salary;                  // Salario del trabajador
    final double commissionPercentage;    // Porcentaje de comisión del trabajador
    final Button edit;                     // Botón para editar el trabajador
    final Button delete;                   // Botón para eliminar el trabajador
    final JComboBox<String> countryCombo = new JComboBox<>(); // ComboBox para seleccionar el país

    /**
     * Constructor de la clase Worker.
     *
     * @param panel               Panel donde se mostrará la tabla.
     * @param listener            Listener para manejar eventos de botones.
     * @param id                  ID único del trabajador.
     * @param address             Dirección del trabajador.
     * @param workerDTO           Datos personales del trabajador.
     * @param salary              Salario del trabajador.
     * @param commissionPercentage Porcentaje de comisión del trabajador.
     */
    public Worker(JPanel panel, ActionListener listener, int id, AddressDTO address, WorkerDTO workerDTO,
                  double salary, double commissionPercentage) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.workerDTO = workerDTO;
        this.salary = salary;
        this.commissionPercentage = commissionPercentage;

        // Creación de botones de edición y eliminación
        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        // Agregar eventos a los botones
        this.edit.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.WORKER_EDIT)));
        this.delete.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.WORKER_DELETE)));
    }

    /**
     * Metodo estático para mostrar la tabla de trabajadores en la interfaz gráfica.
     */
    public static void showWorkerTable(JPanel panel, ActionListener listener) {
        List<Worker> workers = Worker.getAllWorkers(panel, listener);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Trabajador");

        // Opciones de filtro para la búsqueda de trabajadores
        String[] filterOptions = {"Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "DNI", "Teléfono", "Email", "Puesto"};

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar trabajador...");

        // Agregar componentes al panel superior
        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateWorkerForm(panel)); // Evento para crear trabajador
        JTable table = setupWorkerTable(workers, listener); // Configurar la tabla con los datos
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table)); // Contenedor con barra de desplazamiento

        // Configuración de la funcionalidad de filtrado en la tabla
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

        // Actualizar la interfaz con la tabla generada
        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    /**
     * Metodo para obtener todos los trabajadores desde la base de datos.
     */
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
            logger.info("Error al obtener trabajadores: {}", e.getMessage());
        }
        return workers;
    }

    /**
     * Metodo para configurar la tabla con los datos de los trabajadores.
     */
    private static JTable setupWorkerTable(List<Worker> workers, ActionListener listener) {
        String[] columnNames = {
                "ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "DNI", "Teléfono", "Email", "Puesto", "Salario", "Comisión %",
                Constants.BUTTON_EDIT, Constants.BUTTON_DELETE
        };

        Object[][] data = workers.stream().map(w -> new Object[]{
                w.id, w.getWorkerDTO().getName(), w.getAddress().getStreet(), w.getAddress().getPostCode(),
                w.getAddress().getTown(), w.getAddress().getProvince(), w.getAddress().getCountry(),
                w.getWorkerDTO().getCif(), w.getWorkerDTO().getNumber(), w.getWorkerDTO().getEmail(),
                w.getWorkerDTO().getPosition(), w.salary, w.commissionPercentage,
                new JButton(Constants.BUTTON_EDIT), new JButton(Constants.BUTTON_DELETE)
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 13; }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener, workers, Constants.WORKER_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, workers, Constants.WORKER_DELETE));

        return table;
    }

    /**
     * Abre un formulario para modificar los datos de un trabajador.
     *
     * @param mainPanel Panel principal donde se mostrará el formulario.
     * @param view Referencia a la vista principal de la aplicación.
     */
    public void modifyWorkerAction(JPanel mainPanel, View view) {
        // Crear un cuadro de diálogo para modificar los datos del trabajador
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel),
                "Modificar Trabajador", true);
        dialog.setSize(800, 300);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);

        // Crear el formulario con un diseño en GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Campos de entrada para los datos del trabajador
        JTextField nameField = new JTextField(this.getWorkerDTO().getName());
        JTextField addressField = new JTextField(this.getAddress().getStreet());
        JTextField postCodeField = new JTextField(String.valueOf(this.getAddress().getPostCode()));
        JTextField townField = new JTextField(this.getAddress().getTown());
        JTextField provinceField = new JTextField(this.getAddress().getProvince());
        JTextField countryField = new JTextField(this.getAddress().getCountry());
        JTextField dniField = new JTextField(this.getWorkerDTO().getCif());
        JTextField phoneField = new JTextField(this.getWorkerDTO().getNumber());
        JTextField emailField = new JTextField(this.getWorkerDTO().getEmail());
        JTextField positionField = new JTextField(this.getWorkerDTO().getPosition());
        JTextField salaryField = new JTextField(String.valueOf(this.getSalary()));
        JTextField commissionField = new JTextField(String.valueOf(this.getCommissionPercentage()));

        // Cargar la lista de países
        loadCountries();

        // Organizar los campos en el formulario
        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryCombo, "Código Postal:", postCodeField},
                {"DNI:", dniField, "Teléfono:", phoneField},
                {"Email:", emailField, "Puesto:", positionField},
                {"Salario:", salaryField, "Comisión %:", commissionField}
        };

        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        // Crear botones de guardar y cancelar
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        // Acción del botón Guardar
        saveButton.addActionListener(e -> {
            try {
                // Crear objeto actualizado con los nuevos datos ingresados
                Worker updatedWorker = new Worker(
                        this.panel, view, this.getId(),
                        new AddressDTO(addressField.getText(), Integer.parseInt(postCodeField.getText()),
                                townField.getText(), provinceField.getText(), countryField.getText()),
                        new WorkerDTO(nameField.getText(), dniField.getText(), phoneField.getText(),
                                emailField.getText(), positionField.getText()),
                        Double.parseDouble(salaryField.getText()),
                        Double.parseDouble(commissionField.getText())
                );

                // Llamar al metodo para modificar los datos en la base de datos
                this.modifyWorker(mainPanel, updatedWorker, this.getId());
                JOptionPane.showMessageDialog(dialog, "Trabajador actualizado con éxito.");
                dialog.dispose();

                // Actualizar la tabla de trabajadores
                SwingUtilities.invokeLater(() -> {
                    mainPanel.removeAll();
                    Worker.showWorkerTable(mainPanel, view);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar trabajador: " + ex.getMessage(),
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción del botón Cancelar
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Carga la lista de países desde la base de datos y los añade al JComboBox `countryCombo`.
     * Si ocurre un error en la consulta, se muestra un mensaje en el log.
     */
    private void loadCountries() {
        String query = "SELECT name FROM countries ORDER BY name"; // Consulta SQL para obtener la lista de países

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            countryCombo.removeAllItems(); // Limpiar los elementos previos del JComboBox
            while (rs.next()) {
                countryCombo.addItem(rs.getString("name")); // Agregar cada país al JComboBox
            }
        } catch (SQLException e) {
            logger.error("Error al cargar la lista de países: {}", e.getMessage());
        }
    }

    /**
     * Modifica la información de un trabajador en la base de datos.
     *
     * @param mainPanel     Panel principal donde se mostrarán los cambios.
     * @param updatedWorker Objeto Worker con la información actualizada del trabajador.
     * @param id            Identificador único del trabajador a modificar.
     */
    private void modifyWorker(JPanel mainPanel, Worker updatedWorker, int id) {
        String query = "UPDATE workers SET name = ?, address = ?, postCode = ?, town = ?, province = ?, country = ?, " +
                "dni = ?, phone = ?, email = ?, position = ?, salary = ?, commissionPercentage = ? WHERE id = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Asignar valores a la consulta SQL
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

            // Verificar si la actualización fue exitosa
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Trabajador actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se pudo actualizar el trabajador.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al modificar trabajador: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de trabajadores en la interfaz
        SwingUtilities.invokeLater(() -> showWorkerTable(mainPanel, e -> {}));
    }

    /**
     * Agrega dos etiquetas y dos componentes en una fila dentro de un `JPanel` con `GridBagLayout`.
     * Se usa para organizar de manera estructurada los campos de entrada en formularios.
     *
     * @param panel     Panel donde se agregarán los elementos.
     * @param gbc       Configuración de `GridBagConstraints` para la distribución en el `GridBagLayout`.
     * @param label1    Texto de la primera etiqueta.
     * @param comp1     Primer componente (Ej: JTextField, JComboBox).
     * @param label2    Texto de la segunda etiqueta.
     * @param comp2     Segundo componente (Ej: JTextField, JComboBox).
     * @param row       Fila en la que se agregarán los elementos dentro del `GridBagLayout`.
     */
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        // Primera etiqueta (columna 0)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;  // Menos peso para las etiquetas
        panel.add(new JLabel(label1), gbc);

        // Primer campo de entrada (columna 1)
        gbc.gridx = 1;
        gbc.weightx = 0.7;  // Más peso para los campos de entrada
        panel.add(comp1, gbc);

        // Segunda etiqueta (columna 2)
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label2), gbc);

        // Segundo campo de entrada (columna 3)
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        panel.add(comp2, gbc);
    }

    /**
     * Elimina un trabajador de la base de datos después de la confirmación del usuario.
     *
     * @param mainPanel Panel principal donde se muestra la tabla de trabajadores.
     * @param id Identificador único del trabajador a eliminar.
     * @param view Referencia a la vista principal de la aplicación.
     */
    public void deleteWorker(JPanel mainPanel, int id, View view) {
        // Confirmación antes de eliminar el trabajador
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar al trabajador con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM workers WHERE id = ?")) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Trabajador eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró un trabajador con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar trabajador: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Recargar la tabla de trabajadores después de la eliminación
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            Worker.showWorkerTable(mainPanel, view);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}