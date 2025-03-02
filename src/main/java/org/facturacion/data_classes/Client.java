package org.facturacion.data_classes;

// Importaciones de bibliotecas y clases necesarias
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateClientForm;
import org.facturacion.dto.AddressDTO;
import org.facturacion.dto.ClientDTO;
import org.facturacion.resources.*;
import org.facturacion.resources.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

// Clase Client que representa a un cliente con sus datos y acciones disponibles
@Getter  // Genera automáticamente los Metodos getter para los atributos de la clase
@FieldDefaults(level = AccessLevel.PRIVATE)  // Define el nivel de acceso de los campos como privado por defecto
public class Client {
    // Atributos de la clase Client
    final JPanel panel;  // Panel donde se muestra la tabla de clientes
    final int id;  // ID único del cliente en la base de datos
    final AddressDTO address;  // Objeto que contiene la dirección del cliente
    final ClientDTO personData;  // Objeto que contiene los datos personales del cliente
    final double risk;  // Riesgo financiero asociado al cliente
    final double discount;  // Descuento aplicado al cliente
    final Button edit;  // Botón para editar al cliente
    final Button delete;  // Botón para eliminar al cliente
    final JComboBox<String> countryCombo = new JComboBox<>();  // ComboBox para seleccionar el país del cliente

    // Logger para registrar información y errores
    static final Logger logger = LoggerFactory.getLogger(Client.class);

    // Constructor de la clase Client
    public Client(JPanel panel, ActionListener listener, int id, AddressDTO address,
                  ClientDTO personData, double risk, double discount) {
        this.panel = panel;
        this.id = id;
        this.address = address;
        this.personData = personData;
        this.risk = risk;
        this.discount = discount;

        // Inicializa los botones de editar y eliminar
        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        // Agrega un ActionListener al botón de editar para ejecutar la acción correspondiente
        this.edit.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.CLIENT_EDIT);
            listener.actionPerformed(event);
        });

        // Agrega un ActionListener al botón de eliminar para ejecutar la acción correspondiente
        this.delete.addActionListener(e -> {
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.CLIENT_DELETE);
            listener.actionPerformed(event);
        });
    }

    // Metodo estático para mostrar la tabla de clientes en un panel dado
    public static void showClientTable(JPanel panel, ActionListener listener) {
        // Obtiene la lista de clientes desde la base de datos
        List<Client> clients = Client.getClients(panel, listener);

        // Panel superior con botón para crear clientes y filtros de búsqueda
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Cliente");

        // Opciones de filtro para la búsqueda de clientes
        String[] filterOptions = {
                "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País", "CIF", "Teléfono", "Email"
        };
        JComboBox<String> filterDropdown = new JComboBox<>(filterOptions);
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar cliente...");

        // Agrega los componentes al panel superior
        topPanel.add(createButton);
        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);

        // Evento para abrir el formulario de creación de clientes al presionar el botón
        createButton.addActionListener(e -> new CreateClientForm(panel));

        // Configuración de la tabla de clientes
        JTable table = setupClientTable(clients, listener);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        // Habilita el filtrado de la tabla según la búsqueda
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Agrega eventos para actualizar el filtro cuando el usuario escribe en el campo de búsqueda
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = searchField.getText().trim();
                int columnIndex = filterDropdown.getSelectedIndex() + 1;
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
            }
        });

        // Actualiza el panel con la tabla de clientes
        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(tablePane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    // Metodo que obtiene la lista de clientes desde la base de datos
    public static List<Client> getClients(JPanel panel, ActionListener listener) {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT idCliente, nombreCliente, direccionCliente, cpCliente, poblacionCliente, " +
                "provinciaCliente, cifCliente, paisCliente, telCliente, emailCliente, ibanCliente, riesgoCliente, " +
                "descuentoCliente FROM clientes";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(new Client(
                        panel, listener,
                        rs.getInt("idCliente"),
                        new AddressDTO(rs.getString("direccionCliente"), rs.getInt("cpCliente"),
                                rs.getString("poblacionCliente"),
                                rs.getString("provinciaCliente"), rs.getString("paisCliente")
                        ),
                        new ClientDTO(rs.getString("nombreCliente"), rs.getString("cifCliente"),
                                rs.getString("telCliente"), rs.getString("emailCliente"),
                                rs.getString("ibanCliente")
                        ),
                        rs.getDouble("riesgoCliente"),
                        rs.getDouble("descuentoCliente")
                ));
            }
        } catch (SQLException e) {
            logger.info(String.format("Error al obtener clientes: %s", e.getMessage()));
        }
        return clients;
    }

    private static JTable setupClientTable(List<Client> clients, ActionListener listener) {
        // Definimos los nombres de las columnas de la tabla
        String[] columnNames = {
                "ID", "Nombre", "Dirección", "Código Postal", "Ciudad", "Provincia", "País", "CIF", "Teléfono",
                "Email", "IBAN", "Riesgo", "Descuento", Constants.BUTTON_EDIT, Constants.BUTTON_DELETE
        };

        // Creamos una matriz para almacenar los datos de los clientes
        Object[][] data = new Object[clients.size()][columnNames.length];

        // Iteramos sobre la lista de clientes y llenamos la matriz de datos
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);

            // Creamos botones de edición y eliminación para cada fila
            JButton editButton = new JButton(Constants.BUTTON_EDIT);
            JButton deleteButton = new JButton(Constants.BUTTON_DELETE);

            // Llenamos la fila con los datos del cliente
            data[i] = new Object[]{
                    c.getId(), c.getPersonData().getName(), c.getAddress().getStreet(), c.getAddress().getPostCode(),
                    c.getAddress().getTown(), c.getAddress().getProvince(), c.getAddress().getCountry(),
                    c.getPersonData().getCif(), c.getPersonData().getNumber(), c.getPersonData().getEmail(),
                    c.getPersonData().getIban(), c.getRisk(), c.getDiscount(), editButton, deleteButton
            };
        }

        // Creamos un modelo de tabla con los datos y los nombres de las columnas
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo las columnas de los botones de editar y eliminar son editables
                return column == 13 || column == 14;
            }
        };

        // Creamos la tabla con el modelo definido
        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        // Configuramos los botones en la tabla
        table.getColumn(Constants.BUTTON_EDIT).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_DELETE).setCellRenderer(new ButtonRenderer());
        table.getColumn(Constants.BUTTON_EDIT).setCellEditor(new ButtonEditor<>(listener, clients, Constants.CLIENT_EDIT));
        table.getColumn(Constants.BUTTON_DELETE).setCellEditor(new ButtonEditor<>(listener, clients, Constants.CLIENT_DELETE));

        return table; // Devolvemos la tabla configurada
    }

    public void modifyClientAction(JPanel mainPanel, View view) {
        // Crear un cuadro de diálogo modal para modificar el cliente
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel),
                "Modificar Cliente", true);
        dialog.setSize(800, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(mainPanel);

        // Panel para contener los campos del formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Campos de entrada con los datos actuales del cliente
        JTextField nameField = new JTextField(this.getPersonData().getName(), 20);
        JTextField addressField = new JTextField(this.getAddress().getStreet(), 20);
        JTextField postCodeField = new JTextField(String.valueOf(this.getAddress().getPostCode()), 6);
        JTextField townField = new JTextField(this.getAddress().getTown(), 20);
        JTextField provinceField = new JTextField(this.getAddress().getProvince(), 20);
        JTextField countryField = new JTextField(this.getAddress().getCountry(), 20);
        JTextField cifField = new JTextField(this.getPersonData().getCif(), 10);
        JTextField phoneField = new JTextField(this.getPersonData().getNumber(), 15);
        JTextField emailField = new JTextField(this.getPersonData().getEmail(), 20);
        JTextField ibanField = new JTextField(this.getPersonData().getIban(), 24);
        JTextField riskField = new JTextField(String.valueOf(this.getRisk()), 5);
        JTextField discountField = new JTextField(String.valueOf(this.getDiscount()), 5);

        // Matriz para generar filas de etiquetas y campos de entrada
        Object[][] rows = {
                {"Nombre:", nameField, "Dirección:", addressField},
                {"Ciudad:", townField, "Provincia:", provinceField},
                {"País:", countryField, "Código Postal:", postCodeField},
                {"CIF:", cifField, "Teléfono:", phoneField},
                {"Email:", emailField, "IBAN:", ibanField},
                {"Riesgo:", riskField, "Descuento:", discountField}
        };

        // Añadir etiquetas y campos al panel de formulario
        for (int row = 0; row < rows.length; row++) {
            addLabelAndField(formPanel, gbc,
                    (String) rows[row][0], (Component) rows[row][1],
                    (String) rows[row][2], (Component) rows[row][3], row);
        }

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        // Acción del botón "Guardar"
        saveButton.addActionListener(e -> {
            try {
                // Crear un nuevo objeto Cliente con los valores actualizados
                Client updatedClient = new Client(
                        this.panel, view, this.getId(),
                        new AddressDTO(addressField.getText(), Integer.parseInt(postCodeField.getText()),
                                townField.getText(), provinceField.getText(), countryField.getText()),
                        new ClientDTO(nameField.getText(), cifField.getText(), phoneField.getText(),
                                emailField.getText(), ibanField.getText()),
                        Double.parseDouble(riskField.getText()),
                        Double.parseDouble(discountField.getText())
                );

                // Llamar a la función que modifica el cliente en la BD
                this.modifyClient(mainPanel, updatedClient, this.getId());

                // Mostrar mensaje de éxito y cerrar el diálogo
                JOptionPane.showMessageDialog(dialog, "Cliente actualizado con éxito.");
                dialog.dispose();

                // Actualizar la tabla de clientes
                SwingUtilities.invokeLater(() -> {
                    mainPanel.removeAll();
                    Client.showClientTable(mainPanel, view);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error al actualizar cliente: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción del botón "Cancelar"
        cancelButton.addActionListener(e -> dialog.dispose());

        // Agregar botones al panel
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Agregar paneles al diálogo
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Mostrar el cuadro de diálogo
        dialog.setVisible(true);
    }

    private void modifyClient(JPanel mainPanel, Client updatedClient, int id) {
        // Consulta SQL para actualizar un cliente existente
        String query = "UPDATE clientes SET nombreCliente = ?, direccionCliente = ?, cpCliente = ?, " +
                "poblacionCliente = ?, provinciaCliente = ?, paisCliente = ?, cifCliente = ?, telCliente = ?, " +
                "emailCliente = ?, ibanCliente = ?, riesgoCliente = ?, descuentoCliente = ? WHERE idCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Configurar los valores de la consulta con los datos actualizados
            ps.setString(1, updatedClient.getPersonData().getName());
            ps.setString(2, updatedClient.getAddress().getStreet());
            ps.setInt(3, updatedClient.getAddress().getPostCode());
            ps.setString(4, updatedClient.getAddress().getTown());
            ps.setString(5, updatedClient.getAddress().getProvince());
            ps.setString(6, updatedClient.getAddress().getCountry());
            ps.setString(7, updatedClient.getPersonData().getCif());
            ps.setString(8, updatedClient.getPersonData().getNumber());
            ps.setString(9, updatedClient.getPersonData().getEmail());
            ps.setString(10, updatedClient.getPersonData().getIban());
            ps.setDouble(11, updatedClient.getRisk());
            ps.setDouble(12, updatedClient.getDiscount());
            ps.setInt(13, id);

            // Ejecutar la consulta de actualización
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Cliente actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se pudo actualizar el cliente. Verifica el ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al modificar cliente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de clientes en la interfaz
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            Client.showClientTable(mainPanel, e -> {});
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }

    private void addLabelAndField(JPanel formPanel, GridBagConstraints gbc,
                                  String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridy = row;  // Fila donde se añadirá la nueva línea

        // Primera etiqueta y campo
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel(label1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(comp1, gbc);

        // Segunda etiqueta y campo
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel(label2), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.7;
        formPanel.add(comp2, gbc);
    }

    public void deleteClient(JPanel mainPanel, int id, View view) {
        // Confirmación antes de eliminar el cliente
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar al cliente con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        // Si el usuario no confirma, se cancela la operación
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Consulta SQL para eliminar un cliente por su ID
        String query = "DELETE FROM clientes WHERE idCliente = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id); // Asigna el ID del cliente a eliminar en la consulta SQL

            int rowsAffected = ps.executeUpdate(); // Ejecuta la eliminación

            // Verificar si se eliminó correctamente
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Cliente eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró un cliente con el ID proporcionado.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar cliente: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Actualizar la tabla de clientes en la interfaz gráfica
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll(); // Limpia el panel
            Client.showClientTable(mainPanel, e -> {}); // Recarga la tabla de clientes
            mainPanel.revalidate(); // Revalida el panel para actualizarlo
            mainPanel.repaint(); // Redibuja el contenido
        });
    }
}