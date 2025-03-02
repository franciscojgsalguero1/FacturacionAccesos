package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateItemFamilyForm;
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
import java.util.List;
import java.util.ArrayList;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemFamily {
    // Logger para registrar eventos y errores
    static final Logger logger = LoggerFactory.getLogger(ItemFamily.class);

    // Atributos de la clase
    final JPanel panel;         // Panel donde se muestra la tabla de familias de artículos
    final int id;               // Identificador de la familia de artículos
    final String code;          // Código de la familia
    final String description;   // Descripción de la familia
    final Button edit;          // Botón para editar la familia
    final Button delete;        // Botón para eliminar la familia

    /**
     * Constructor de la clase ItemFamily.
     *
     * @param panel     Panel de la interfaz donde se mostrará la tabla.
     * @param listener  Listener para manejar eventos de botones.
     * @param id        ID único de la familia de artículos.
     * @param code      Código identificador de la familia.
     * @param description Descripción de la familia.
     */
    public ItemFamily(JPanel panel, ActionListener listener, int id, String code, String description) {
        this.panel = panel;
        this.id = id;
        this.code = code;
        this.description = description;

        // Creación de botones de edición y eliminación
        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        // Agregar eventos a los botones
        addActionListener(this.edit, listener, Constants.FAMILY_EDIT);
        addActionListener(this.delete, listener, Constants.FAMILY_DELETE);
    }

    /**
     * Metodo para agregar ActionListener a los botones.
     *
     * @param button   Botón al que se le asignará el evento.
     * @param listener Listener que gestionará el evento.
     * @param command  Comando que se ejecutará cuando se presione el botón.
     */
    private void addActionListener(Button button, ActionListener listener, String command) {
        button.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command)));
    }

    /**
     * Metodo estático para mostrar la tabla de familias de artículos en la interfaz gráfica.
     *
     * @param panel     Panel donde se mostrará la tabla.
     * @param listener  Listener para manejar los eventos de botones.
     */
    public static void showItemFamilyTable(JPanel panel, ActionListener listener) {
        List<ItemFamily> families = getAllItemFamilies(panel, listener); // Obtener familias desde la base de datos
        JPanel topPanel = createTopPanel(panel, listener); // Crear panel superior con botón de creación
        JTable table = setupItemFamilyTable(families, listener); // Configurar la tabla con datos
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
     * Metodo para crear el panel superior con el botón de creación de familias.
     *
     * @param panel     Panel donde se agregará el botón.
     * @param listener  Listener para manejar eventos.
     * @return          Panel con los controles superiores.
     */
    private static JPanel createTopPanel(JPanel panel, ActionListener listener) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Familia");

        createButton.addActionListener(e -> new CreateItemFamilyForm(panel)); // Acción al presionar el botón
        topPanel.add(createButton);

        return topPanel;
    }

    /**
     * Metodo para configurar el filtro de búsqueda en la tabla.
     *
     * @param topPanel  Panel donde se agregarán los filtros.
     * @param sorter    Objeto para ordenar y filtrar la tabla.
     */
    private static void setupSearchFilter(JPanel topPanel, TableRowSorter<DefaultTableModel> sorter) {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar familia...");
        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"Código", "Descripción"});

        // Configurar el evento de filtrado en el campo de búsqueda
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
     *
     * @param panel      Panel donde se actualizará la tabla.
     * @param topPanel   Panel superior con controles.
     * @param tablePane  Panel con la tabla de datos.
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
     * Metodo para obtener todas las familias de artículos desde la base de datos.
     *
     * @param panel     Panel donde se mostrará la tabla.
     * @param listener  Listener para manejar eventos.
     * @return          Lista de objetos ItemFamily con los datos obtenidos.
     */
    public static List<ItemFamily> getAllItemFamilies(JPanel panel, ActionListener listener) {
        List<ItemFamily> families = new ArrayList<>();
        String query = "SELECT idFamiliaArticulos, codigoFamiliaArticulos, denominacionFamilias FROM familiaarticulos";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                families.add(new ItemFamily(
                        panel, listener,
                        rs.getInt("idFamiliaArticulos"),
                        rs.getString("codigoFamiliaArticulos"),
                        rs.getString("denominacionFamilias")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener familias de artículos: {}", e.getMessage(), e);
        }
        return families;
    }

    /**
     * Metodo para configurar la tabla con los datos de las familias de artículos.
     *
     * @param families  Lista de familias de artículos.
     * @param listener  Listener para manejar eventos en la tabla.
     * @return          Tabla con los datos cargados.
     */
    private static JTable setupItemFamilyTable(List<ItemFamily> families, ActionListener listener) {
        String[] columnNames = {"ID", "Código", "Descripción", "Editar", "Eliminar"};

        Object[][] data = families.stream().map(f -> new Object[]{
                f.id, f.code, f.description,
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

        table.getColumn("Editar").setCellEditor(new ButtonEditor<>(listener, families, Constants.FAMILY_EDIT));
        table.getColumn("Eliminar").setCellEditor(new ButtonEditor<>(listener, families, Constants.FAMILY_DELETE));

        return table;
    }

    /**
     * Muestra un formulario para modificar una familia de artículos.
     *
     * @param mainPanel Panel principal donde se mostrará el formulario.
     * @param view      Vista principal para manejar eventos.
     */
    public void modifyItemFamilyAction(JPanel mainPanel, View view) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel),
                "Modificar Familia de Artículos", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos de entrada para la modificación
        JTextField codeField = new JTextField(this.getCode());
        JTextField descriptionField = new JTextField(this.getDescription());

        // Añadir etiquetas y campos al formulario
        addLabelAndField(formPanel, gbc, "Código:", codeField, "Descripción:", descriptionField, 0);

        // Botones para guardar o cancelar
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        saveButton.addActionListener(e -> {
            try {
                // Crear un nuevo objeto con los datos modificados
                ItemFamily updatedFamily = new ItemFamily(mainPanel, view, this.getId(),
                        codeField.getText(), descriptionField.getText());

                // Modificar la familia de artículos en la base de datos
                this.modifyItemFamily(mainPanel, updatedFamily, this.getId());
                JOptionPane.showMessageDialog(dialog, "Familia de artículos actualizada con éxito.");
                dialog.dispose();

                // Refrescar la tabla de familias en la vista principal
                SwingUtilities.invokeLater(() -> {
                    mainPanel.removeAll();
                    ItemFamily.showItemFamilyTable(mainPanel, view);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar: " + ex.getMessage(),
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
     * Modifica los datos de una familia de artículos en la base de datos.
     *
     * @param mainPanel      Panel donde se mostrará el resultado.
     * @param updatedFamily  Objeto ItemFamily con los nuevos datos.
     * @param id             ID de la familia a modificar.
     */
    private void modifyItemFamily(JPanel mainPanel, ItemFamily updatedFamily, int id) {
        String query = "UPDATE familiaarticulos SET codigoFamiliaArticulos = ?, " +
                "denominacionFamilias = ? WHERE idFamiliaArticulos = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, updatedFamily.getCode());         // Código de la familia
            ps.setString(2, updatedFamily.getDescription());  // Descripción de la familia
            ps.setInt(3, id);                                 // ID de la familia a actualizar

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Familia de artículos actualizada con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se pudo actualizar la familia de artículos.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al modificar familia de artículos: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de familias de artículos en la vista principal
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            ItemFamily.showItemFamilyTable(mainPanel, e -> {});
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }

    /**
     * Añade una etiqueta y un campo de entrada al formulario en la fila especificada.
     *
     * @param formPanel  Panel donde se agregará la etiqueta y el campo.
     * @param gbc        Restricciones de diseño para GridBagLayout.
     * @param label1     Texto de la primera etiqueta.
     * @param comp1      Componente de entrada correspondiente a la primera etiqueta.
     * @param label2     Texto de la segunda etiqueta.
     * @param comp2      Componente de entrada correspondiente a la segunda etiqueta.
     * @param row        Fila donde se insertarán los componentes en el formulario.
     */
    private void addLabelAndField(JPanel formPanel, GridBagConstraints gbc,
                                  String label1, Component comp1,
                                  String label2, Component comp2, int row) {
        gbc.gridx = 0;  // Primera columna
        gbc.gridy = row; // Fila especificada
        gbc.weightx = 0.3; // Peso para distribuir espacio
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel(label1), gbc);

        gbc.gridx = 1; // Segunda columna
        gbc.weightx = 0.7;
        formPanel.add(comp1, gbc);

        gbc.gridx = 2; // Tercera columna
        gbc.weightx = 0.3;
        formPanel.add(new JLabel(label2), gbc);

        gbc.gridx = 3; // Cuarta columna
        gbc.weightx = 0.7;
        formPanel.add(comp2, gbc);
    }

    /**
     * Elimina una familia de artículos de la base de datos.
     *
     * @param mainPanel Panel principal donde se muestra la lista de familias.
     * @param id        ID de la familia a eliminar.
     * @param view      Vista principal para manejar eventos.
     */
    public void deleteItemFamily(JPanel mainPanel, int id, View view) {
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar la familia con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM familiaarticulos WHERE idFamiliaArticulos = ?")) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Familia de artículos eliminada con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró una familia con el ID proporcionado.",
                        Constants.ERROR, JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar familia de artículos: " + e.getMessage(),
                    Constants.ERROR, JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de familias en la vista principal
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            ItemFamily.showItemFamilyTable(mainPanel, view);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}