package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateIVATypesForm;
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
public class IVATypes {
    // Logger para registrar eventos y errores
    static final Logger logger = LoggerFactory.getLogger(IVATypes.class);

    // Atributos de la clase
    final JPanel panel;          // Panel donde se muestra la tabla de tipos de IVA
    final int id;                // Identificador del tipo de IVA
    final double amount;         // Porcentaje del IVA
    final String description;    // Descripción del tipo de IVA
    final Button edit;           // Botón para editar el tipo de IVA
    final Button delete;         // Botón para eliminar el tipo de IVA

    /**
     * Constructor de la clase IVATypes.
     *
     * @param panel       Panel donde se mostrará la tabla.
     * @param listener    Listener para manejar eventos de botones.
     * @param id          ID único del tipo de IVA.
     * @param amount      Porcentaje de IVA.
     * @param description Descripción del tipo de IVA.
     */
    public IVATypes(JPanel panel, ActionListener listener, int id, double amount, String description) {
        this.panel = panel;
        this.id = id;
        this.amount = amount;
        this.description = description;

        // Creación de botones de edición y eliminación
        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        // Agregar eventos a los botones
        this.edit.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.IVA_EDIT)));
        this.delete.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Constants.IVA_DELETE)));
    }

    /**
     * Metodo estático para mostrar la tabla de tipos de IVA en la interfaz gráfica.
     *
     * @param panel    Panel donde se mostrará la tabla.
     * @param listener Listener para manejar eventos.
     */
    public static void showIVATypesTable(JPanel panel, ActionListener listener) {
        List<IVATypes> ivaTypes = getAllIVATypes(panel, listener); // Obtener tipos de IVA desde la base de datos
        JPanel topPanel = createTopPanel(panel, listener); // Crear panel superior con botón de creación
        JTable table = setupIVATypesTable(ivaTypes, listener); // Configurar la tabla con datos
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table)); // Contenedor con barra de desplazamiento

        // Configurar el modelo y la funcionalidad de ordenamiento de la tabla
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Configurar el filtro de búsqueda en la tabla
        setupSearchFilter(topPanel, sorter);

        // Actualizar la interfaz con la tabla generada
        SwingUtilities.invokeLater(() -> updatePanel(panel, topPanel, tablePane));
    }

    /**
     * Metodo para crear el panel superior con el botón de creación de tipos de IVA.
     */
    private static JPanel createTopPanel(JPanel panel, ActionListener listener) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Tipo de IVA");

        createButton.addActionListener(e -> new CreateIVATypesForm(panel)); // Acción al presionar el botón
        topPanel.add(createButton);

        return topPanel;
    }

    /**
     * Metodo para configurar el filtro de búsqueda en la tabla.
     */
    private static void setupSearchFilter(JPanel topPanel, TableRowSorter<DefaultTableModel> sorter) {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar tipo de IVA...");
        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"Porcentaje", "Descripción"});

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void applyFilter() {
                String text = searchField.getText().trim();
                int columnIndex = filterDropdown.getSelectedIndex() + 1;
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
            }

            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        topPanel.add(new JLabel("Filtrar por:"));
        topPanel.add(filterDropdown);
        topPanel.add(searchField);
    }

    /**
     * Metodo para actualizar el panel con la tabla de datos.
     */
    private static void updatePanel(JPanel panel, JPanel topPanel, JScrollPane tablePane) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tablePane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Metodo para obtener todos los tipos de IVA desde la base de datos.
     */
    public static List<IVATypes> getAllIVATypes(JPanel panel, ActionListener listener) {
        List<IVATypes> ivaTypes = new ArrayList<>();
        String query = "SELECT idTipoIva, iva, observacionesTipoIva FROM tiposiva";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ivaTypes.add(new IVATypes(
                        panel, listener,
                        rs.getInt("idTipoIva"),
                        rs.getDouble("iva"),
                        rs.getString("observacionesTipoIva")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Error al obtener tipos de IVA: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }
        return ivaTypes;
    }

    /**
     * Metodo para configurar la tabla con los datos de los tipos de IVA.
     */
    private static JTable setupIVATypesTable(List<IVATypes> ivaTypes, ActionListener listener) {
        String[] columnNames = {"ID", "Porcentaje", "Descripción", "Editar", "Eliminar"};

        Object[][] data = ivaTypes.stream().map(iva -> new Object[]{
                iva.id, iva.amount, iva.description,
                new JButton(Constants.BUTTON_EDIT),
                new JButton(Constants.BUTTON_DELETE)
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 3; }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Eliminar").setCellRenderer(new ButtonRenderer());

        table.getColumn("Editar").setCellEditor(new ButtonEditor<>(listener, ivaTypes, Constants.IVA_EDIT));
        table.getColumn("Eliminar").setCellEditor(new ButtonEditor<>(listener, ivaTypes, Constants.IVA_DELETE));

        return table;
    }

    /**
     * Abre un formulario para modificar un tipo de IVA existente.
     *
     * @param mainPanel Panel principal donde se actualizará la vista.
     * @param view      Vista principal de la aplicación.
     */
    public void modifyIVATypesAction(JPanel mainPanel, View view) {
        // Crear diálogo modal para la edición del tipo de IVA
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel),
                "Modificar Tipo de IVA", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setLayout(new BorderLayout());

        // Configuración del formulario con GridBagLayout para mejor organización
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Crear campos de entrada con los valores actuales del tipo de IVA
        JTextField amountField = new JTextField(String.valueOf(this.getAmount()));
        JTextField descriptionField = new JTextField(this.getDescription());

        // Añadir etiquetas y campos al formulario
        addLabelAndField(formPanel, gbc, "Porcentaje de IVA:", amountField, "Descripción:", descriptionField, 0);

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        // Acción del botón "Guardar"
        saveButton.addActionListener(e -> {
            try {
                // Crear un nuevo objeto IVATypes con los datos modificados
                IVATypes updatedIVA = new IVATypes(
                        this.panel, view, this.getId(),
                        Double.parseDouble(amountField.getText()), descriptionField.getText()
                );

                // Modificar el tipo de IVA en la base de datos
                this.modifyIVATypes(mainPanel, updatedIVA, this.getId());
                JOptionPane.showMessageDialog(dialog, "Tipo de IVA actualizado con éxito.");
                dialog.dispose();

                // Refrescar la tabla en la vista principal
                SwingUtilities.invokeLater(() -> {
                    mainPanel.removeAll();
                    IVATypes.showIVATypesTable(mainPanel, view);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar tipo de IVA: " + ex.getMessage(),
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción del botón "Cancelar"
        cancelButton.addActionListener(e -> dialog.dispose());

        // Agregar botones al panel
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Añadir los paneles al diálogo
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Mostrar el diálogo
        dialog.setVisible(true);
    }

    /**
     * Añade una fila con una etiqueta y un campo de texto al formulario usando GridBagLayout.
     *
     * @param formPanel     Panel del formulario donde se añadirán los componentes.
     * @param gbc           Restricciones de diseño para el GridBagLayout.
     * @param label1        Texto de la primera etiqueta.
     * @param textField1    Campo de texto correspondiente a la primera etiqueta.
     * @param label2        Texto de la segunda etiqueta.
     * @param textField2    Campo de texto correspondiente a la segunda etiqueta.
     * @param row           Número de fila donde se insertarán los componentes.
     */
    private void addLabelAndField(JPanel formPanel, GridBagConstraints gbc,
                                  String label1, JTextField textField1,
                                  String label2, JTextField textField2, int row) {
        gbc.gridx = 0; // Primera columna (posición X en la cuadrícula)
        gbc.gridy = row; // Fila actual (posición Y en la cuadrícula)
        gbc.weightx = 0.4; // Ancho proporcional de la celda
        formPanel.add(new JLabel(label1), gbc);

        gbc.gridx = 1; // Segunda columna
        gbc.weightx = 0.6;
        formPanel.add(textField1, gbc);

        gbc.gridx = 2; // Tercera columna
        gbc.weightx = 0.4;
        formPanel.add(new JLabel(label2), gbc);

        gbc.gridx = 3; // Cuarta columna
        gbc.weightx = 0.6;
        formPanel.add(textField2, gbc);
    }

    /**
     * Modifica un tipo de IVA en la base de datos.
     *
     * @param mainPanel  Panel principal donde se actualizará la vista.
     * @param updatedIVA Objeto IVATypes con los nuevos valores a actualizar.
     * @param id         ID del tipo de IVA que se modificará.
     */
    private void modifyIVATypes(JPanel mainPanel, IVATypes updatedIVA, int id) {
        // Consulta SQL para actualizar el tipo de IVA en la base de datos
        String query = "UPDATE tiposiva SET iva = ?, observacionesTipoIva = ? WHERE idTipoIva = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Asignar los valores nuevos en la consulta preparada
            ps.setDouble(1, updatedIVA.getAmount());
            ps.setString(2, updatedIVA.getDescription());
            ps.setInt(3, id);

            // Ejecutar la consulta y obtener el número de filas afectadas
            int rowsAffected = ps.executeUpdate();

            // Mostrar mensaje de éxito o error según el resultado
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Tipo de IVA actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se pudo actualizar el tipo de IVA.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al modificar tipo de IVA: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de tipos de IVA en la interfaz de usuario
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            IVATypes.showIVATypesTable(mainPanel, e -> {});
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }

    /**
     * Elimina un tipo de IVA de la base de datos después de una confirmación del usuario.
     *
     * @param mainPanel Panel donde se actualizará la vista tras la eliminación.
     * @param id        ID del tipo de IVA a eliminar.
     * @param view      Vista principal de la aplicación.
     */
    public void deleteIVATypes(JPanel mainPanel, int id, View view) {
        // Confirmar la eliminación
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar el tipo de IVA con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        // Si el usuario no confirma, salir del metodo
        if (confirm != JOptionPane.YES_OPTION) return;

        // Intentar eliminar el tipo de IVA de la base de datos
        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tiposiva WHERE idTipoIva = ?")) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            // Verificar si se eliminó correctamente
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Tipo de IVA eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró un tipo de IVA con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar tipo de IVA: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla en la vista principal
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            IVATypes.showIVATypesTable(mainPanel, view);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}