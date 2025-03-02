package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateProviderForm;
import org.facturacion.dto.AddressDTO;
import org.facturacion.dto.ProviderDTO;
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
public class Provider {
    // Logger para registrar eventos y errores
    static final Logger logger = LoggerFactory.getLogger(Provider.class);

    // Atributos de la clase
    final JPanel panel;            // Panel donde se mostrará la tabla de proveedores
    final int id;                  // ID único del proveedor en la base de datos
    final AddressDTO address;       // Objeto que almacena la dirección del proveedor
    final ProviderDTO providerData; // Objeto que almacena los datos del proveedor
    final Button edit;              // Botón para editar el proveedor
    final Button delete;            // Botón para eliminar el proveedor
    final JComboBox<String> countryCombo = new JComboBox<>(); // ComboBox para seleccionar el país

    /**
     * Constructor de la clase Provider.
     *
     * @param panel        Panel donde se mostrará la tabla.
     * @param listener     Listener para manejar eventos de botones.
     * @param id           ID único del proveedor.
     * @param address      Dirección del proveedor.
     * @param providerData Datos del proveedor.
     */
    public Provider(JPanel panel, ActionListener listener, int id, AddressDTO address, ProviderDTO providerData) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.providerData = providerData;

        // Creación de botones de edición y eliminación
        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        // Agregar eventos a los botones
        this.edit.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.PROVIDER_EDIT)));
        this.delete.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.PROVIDER_DELETE)));
    }

    /**
     * Metodo estático para mostrar la tabla de proveedores en la interfaz gráfica.
     *
     * @param panel    Panel donde se mostrará la tabla.
     * @param listener Listener para manejar eventos.
     */
    public static void showProviderTable(JPanel panel, ActionListener listener) {
        List<Provider> providers = Provider.getAllProviders(panel, listener); // Obtener proveedores desde la BD
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Proveedor");

        // Opciones de filtro para la búsqueda de proveedores
        String[] filterOptions = {"Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web"};

        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar proveedor...");

        // Agregar componentes al panel superior
        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        createButton.addActionListener(e -> new CreateProviderForm(panel)); // Evento para crear proveedor
        JTable table = setupProviderTable(providers, listener); // Configurar la tabla con los datos
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
     * Metodo para obtener todos los proveedores desde la base de datos.
     */
    public static List<Provider> getAllProviders(JPanel panel, ActionListener listener) {
        List<Provider> providers = new ArrayList<>();
        String query = "SELECT idProveedor, nombreProveedor, direccionProveedor, cpProveedor, poblacionProveedor, " +
                "provinciaProveedor, paisProveedor, cifProveedor, telProveedor, emailProveedor, webProveedor " +
                "FROM proveedores";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                providers.add(new Provider(
                        panel, listener,
                        rs.getInt("idProveedor"),
                        new AddressDTO(rs.getString("direccionProveedor"), rs.getInt("cpProveedor"),
                                rs.getString("poblacionProveedor"),
                                rs.getString("provinciaProveedor"),
                                rs.getString("paisProveedor")),
                        new ProviderDTO(rs.getString("nombreProveedor"),
                                rs.getString("cifProveedor"),
                                rs.getString("telProveedor"), rs.getString("emailProveedor"),
                                rs.getString("webProveedor"))
                ));
            }
        } catch (SQLException e) {
            logger.info("Error al obtener proveedores: {}", e.getMessage());
        }
        return providers;
    }

    /**
     * Metodo para configurar la tabla con los datos de los proveedores.
     */
    private static JTable setupProviderTable(List<Provider> providers, ActionListener listener) {
        String[] columnNames = {"ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País",
                "CIF", "Teléfono", "Email", "Web", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE};

        Object[][] data = providers.stream().map(p -> new Object[]{
                p.id, p.getProviderData().getName(), p.getAddress().getStreet(), p.getAddress().getPostCode(),
                p.getAddress().getTown(), p.getAddress().getProvince(), p.getAddress().getCountry(),
                p.getProviderData().getCif(), p.getProviderData().getNumber(), p.getProviderData().getEmail(),
                p.getProviderData().getWebsite(), new JButton(Constants.BUTTON_EDIT), new JButton(Constants.BUTTON_DELETE)
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 11; }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());

        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener, providers, Constants.PROVIDER_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, providers, Constants.PROVIDER_DELETE));

        return table;
    }

    /**
     * Método para modificar un proveedor existente.
     * Abre un cuadro de diálogo con un formulario para actualizar la información del proveedor seleccionado.
     *
     * @param mainPanel Panel principal donde se mostrarán los cambios.
     * @param view      Instancia de la vista principal para la gestión de eventos.
     */
    public void modifyProviderAction(JPanel mainPanel, View view) {
        // Crear un cuadro de diálogo modal para la edición del proveedor
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel),
                "Modificar Proveedor", true);
        dialog.setSize(800, 300);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setLayout(new BorderLayout());

        // Panel de formulario con disposición en cuadrícula
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Campos de entrada con valores actuales del proveedor
        JTextField nameField = new JTextField(this.getProviderData().getName());
        JTextField addressField = new JTextField(this.getAddress().getStreet());
        JTextField postCodeField = new JTextField(String.valueOf(this.getAddress().getPostCode()));
        JTextField townField = new JTextField(this.getAddress().getTown());
        JTextField provinceField = new JTextField(this.getAddress().getProvince());
        JTextField countryField = new JTextField(this.getAddress().getCountry());
        JTextField cifField = new JTextField(this.getProviderData().getCif());
        JTextField phoneField = new JTextField(this.getProviderData().getNumber());
        JTextField emailField = new JTextField(this.getProviderData().getEmail());
        JTextField websiteField = new JTextField(this.getProviderData().getWebsite());

        // Disposición de los elementos en la cuadrícula
        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryField, "Código Postal:", postCodeField},
                {"CIF:", cifField, "Teléfono:", phoneField},
                {"Email:", emailField, "Web:", websiteField}
        };

        // Agregar etiquetas y campos al formulario
        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        // Botones de guardar y cancelar
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                // Crear un objeto con los valores actualizados
                Provider updatedProvider = new Provider(
                        this.panel, view, this.getId(),
                        new AddressDTO(addressField.getText(), Integer.parseInt(postCodeField.getText()),
                                townField.getText(), provinceField.getText(), countryField.getText()),
                        new ProviderDTO(nameField.getText(), cifField.getText(), phoneField.getText(),
                                emailField.getText(), websiteField.getText())
                );

                // Ejecutar la modificación en la base de datos
                this.modifyProvider(mainPanel, updatedProvider, this.getId());
                JOptionPane.showMessageDialog(dialog, "Proveedor actualizado con éxito.");
                dialog.dispose();

                // Refrescar la tabla de proveedores
                SwingUtilities.invokeLater(() -> {
                    mainPanel.removeAll();
                    Provider.showProviderTable(mainPanel, view);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar proveedor: " + ex.getMessage(),
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

    /**
     * Modifica los datos de un proveedor en la base de datos y actualiza la vista.
     *
     * @param mainPanel Panel principal donde se muestra la tabla de proveedores.
     * @param updatedProvider Objeto Provider con los datos actualizados.
     * @param id Identificador único del proveedor a modificar.
     */
    private void modifyProvider(JPanel mainPanel, Provider updatedProvider, int id) {
        // Consulta SQL para actualizar los datos del proveedor
        String query = "UPDATE proveedores SET nombreProveedor = ?, direccionProveedor = ?, cpProveedor = ?, " +
                "poblacionProveedor = ?, provinciaProveedor = ?, paisProveedor = ?, cifProveedor = ?, " +
                "telProveedor = ?, emailProveedor = ?, webProveedor = ? WHERE idProveedor = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Asignación de valores al PreparedStatement
            ps.setString(1, updatedProvider.getProviderData().getName());
            ps.setString(2, updatedProvider.getAddress().getStreet());
            ps.setInt(3, updatedProvider.getAddress().getPostCode());
            ps.setString(4, updatedProvider.getAddress().getTown());
            ps.setString(5, updatedProvider.getAddress().getProvince());
            ps.setString(6, updatedProvider.getAddress().getCountry());
            ps.setString(7, updatedProvider.getProviderData().getCif());
            ps.setString(8, updatedProvider.getProviderData().getNumber());
            ps.setString(9, updatedProvider.getProviderData().getEmail());
            ps.setString(10, updatedProvider.getProviderData().getWebsite());
            ps.setInt(11, id); // ID del proveedor a actualizar

            // Ejecutar la actualización
            int rowsAffected = ps.executeUpdate();

            // Verificar si la actualización fue exitosa
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Proveedor actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se pudo actualizar el proveedor.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al modificar proveedor: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Recargar la tabla de proveedores para reflejar los cambios
        SwingUtilities.invokeLater(() -> Provider.showProviderTable(mainPanel, e -> {}));
    }

    /**
     * Metodo auxiliar para agregar una etiqueta y un campo de entrada en un formulario con GridBagLayout.
     * Permite organizar los elementos en filas de manera uniforme.
     *
     * @param formPanel Panel donde se agregan los componentes.
     * @param gbc       Restricciones de disposición para GridBagLayout.
     * @param label1    Texto de la primera etiqueta.
     * @param comp1     Componente asociado a la primera etiqueta (campo de entrada).
     * @param label2    Texto de la segunda etiqueta.
     * @param comp2     Componente asociado a la segunda etiqueta (campo de entrada).
     * @param row       Número de fila en la que se colocarán los componentes.
     */
    private void addLabelAndField(JPanel formPanel, GridBagConstraints gbc,
                                  String label1, Component comp1, String label2, Component comp2, int row) {
        // Configurar la posición de la primera etiqueta
        gbc.gridx = 0;  // Primera columna
        gbc.gridy = row; // Fila especificada
        gbc.weightx = 0.3; // Peso menor para las etiquetas
        formPanel.add(new JLabel(label1), gbc);

        // Configurar la posición del primer campo de entrada
        gbc.gridx = 1;  // Segunda columna
        gbc.weightx = 0.7; // Peso mayor para los campos de entrada
        formPanel.add(comp1, gbc);

        // Configurar la posición de la segunda etiqueta
        gbc.gridx = 2;  // Tercera columna
        gbc.weightx = 0.3;
        formPanel.add(new JLabel(label2), gbc);

        // Configurar la posición del segundo campo de entrada
        gbc.gridx = 3;  // Cuarta columna
        gbc.weightx = 0.7;
        formPanel.add(comp2, gbc);
    }


    /**
     * Metodo para eliminar un proveedor de la base de datos.
     * Solicita confirmación antes de proceder con la eliminación.
     *
     * @param mainPanel Panel principal donde se mostrarán los cambios.
     * @param id        Identificador del proveedor a eliminar.
     * @param view      Instancia de la vista principal para la gestión de eventos.
     */
    public void deleteProvider(JPanel mainPanel, int id, View view) {
        // Confirmar con el usuario antes de eliminar el proveedor
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar al proveedor con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return; // Cancelar si el usuario selecciona "No"

        // Consulta SQL para eliminar el proveedor basado en su ID
        String query = "DELETE FROM proveedores WHERE idProveedor = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id); // Asignar el ID del proveedor a eliminar
            int rowsAffected = ps.executeUpdate(); // Ejecutar la eliminación

            // Verificar si la eliminación fue exitosa
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Proveedor eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró un proveedor con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar proveedor: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de proveedores después de la eliminación
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            Provider.showProviderTable(mainPanel, view);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}