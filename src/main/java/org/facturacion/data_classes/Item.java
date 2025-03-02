package org.facturacion.data_classes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.facturacion.content.View;
import org.facturacion.create_forms.CreateItemForm;
import org.facturacion.dto.ItemDTO;
import org.facturacion.resources.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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
public class Item {
    static final Logger logger = LoggerFactory.getLogger(Item.class);

    final JPanel panel;
    final int id;
    final ItemDTO itemData;
    final String description;
    final String familyId;
    final Button edit;
    final Button delete;

    public Item(JPanel panel, ActionListener listener, int id, ItemDTO itemData, String description, String family) {
        this.panel = panel;
        this.id = id;
        this.itemData = itemData;
        this.description = description;
        this.familyId = family;

        this.edit = new Button(Constants.BUTTON_EDIT);
        this.delete = new Button(Constants.BUTTON_DELETE);

        addActionListener(this.edit, listener, Constants.ARTICLE_EDIT);
        addActionListener(this.delete, listener, Constants.ARTICLE_DELETE);
    }

    private void addActionListener(Button button, ActionListener listener, String command) {
        button.addActionListener(e -> listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command)));
    }

    public static void showItemTable(JPanel panel, ActionListener listener) {
        List<Item> items = getAllItems(panel, listener);
        JPanel topPanel = createTopPanel(panel, listener);
        JTable table = setupItemTable(items, listener, panel);
        JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        setupSearchFilter(topPanel, sorter);

        SwingUtilities.invokeLater(() -> updatePanel(panel, topPanel, tablePane));
    }

    private static void setupSearchFilter(JPanel topPanel, TableRowSorter<DefaultTableModel> sorter) {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Buscar artículo...");
        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"Código", "Código de Barras", "Descripción", "Familia", "Costo", "Margen", "Precio", "Proveedor", "Stock"});

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

    private static void updatePanel(JPanel panel, JPanel topPanel, JScrollPane tablePane) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tablePane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private static JPanel createTopPanel(JPanel panel, ActionListener listener) {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Crear Artículo");

        createButton.addActionListener(e -> new CreateItemForm(panel));

        topPanel.add(createButton);
        return topPanel;
    }

    public static List<Item> getAllItems(JPanel panel, ActionListener listener) {
        List<Item> items = new ArrayList<>();
        String query = """
                SELECT a.idArticulo, a.codigoArticulo, a.codigoBarrasArticulo, a.descripcionArticulo, 
                       f.denominacionFamilias AS familiaNombre, a.costeArticulo, a.margenComercialArticulo, 
                       a.pvpArticulo, COALESCE(p.nombreProveedor, 'N/A') AS proveedorNombre, a.stockArticulo 
                FROM articulos a 
                LEFT JOIN familiaarticulos f ON a.familiaArticulo = f.idFamiliaArticulos 
                LEFT JOIN proveedores p ON a.proveedorArticulo = p.idProveedor
                """;

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                items.add(new Item(
                        panel, listener,
                        rs.getInt("idArticulo"),
                        new ItemDTO(
                                rs.getString("codigoArticulo"),
                                rs.getString("codigoBarrasArticulo"),
                                rs.getDouble("costeArticulo"),
                                rs.getDouble("margenComercialArticulo"),
                                rs.getDouble("pvpArticulo"),
                                rs.getString("proveedorNombre"),
                                rs.getInt("stockArticulo")
                        ),
                        rs.getString("descripcionArticulo"),
                        rs.getString("familiaNombre")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener artículos: {}", e.getMessage(), e);
        }
        return items;
    }

    private static JTable setupItemTable(List<Item> items, ActionListener listener, JPanel panel) {
        String[] columnNames = {"ID", "Código", "Código de Barras", "Descripción", "Familia", "Costo", "Margen", "Precio", "Proveedor", "Stock", "Editar", "Eliminar"};

        Object[][] data = items.stream().map(item -> new Object[]{
                item.id, item.getItemData().getCode(), item.getItemData().getBarCode(), item.description,
                item.familyId, item.getItemData().getCost(), item.getItemData().getMargin(),
                item.getItemData().getPrice(), item.getItemData().getSupplier(),
                item.getItemData().getStock(),
                new JButton(Constants.BUTTON_EDIT),
                new JButton(Constants.BUTTON_DELETE)
        }).toArray(Object[][]::new);

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return column >= 10; }
        };

        JTable table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);

        table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Eliminar").setCellRenderer(new ButtonRenderer());

        table.getColumn("Editar").setCellEditor(new ButtonEditor<>(listener, items, Constants.ARTICLE_EDIT));
        table.getColumn("Eliminar").setCellEditor(new ButtonEditor<>(listener, items, Constants.ARTICLE_DELETE));

        return table;
    }

    /**
     * Método para modificar un artículo existente.
     * Abre un cuadro de diálogo con un formulario donde el usuario puede cambiar los detalles del artículo.
     *
     * @param mainPanel Panel principal donde se mostrarán los cambios.
     * @param view Vista principal de la aplicación.
     */
    public void modifyItemAction(JPanel mainPanel, View view) {
        // Crear un cuadro de diálogo modal para editar el artículo
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel),
                "Modificar Artículo", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);

        // Panel del formulario con un diseño de cuadrícula para organizar los campos
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Campos de entrada para editar la información del artículo
        JTextField codeField = new JTextField(this.getItemData().getCode());
        JTextField barCodeField = new JTextField(this.getItemData().getBarCode());
        JTextField descriptionField = new JTextField(this.getDescription());
        JTextField costField = new JTextField(String.valueOf(this.getItemData().getCost()));
        JTextField marginField = new JTextField(String.valueOf(this.getItemData().getMargin()));
        JTextField priceField = new JTextField(String.valueOf(this.getItemData().getPrice()));
        JTextField stockField = new JTextField(String.valueOf(this.getItemData().getStock()));

        // Agregar los campos al panel del formulario con etiquetas descriptivas
        formPanel.add(new JLabel("Código:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Código de Barras:"));
        formPanel.add(barCodeField);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Costo:"));
        formPanel.add(costField);
        formPanel.add(new JLabel("Margen:"));
        formPanel.add(marginField);
        formPanel.add(new JLabel("Precio:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(stockField);

        // Panel para los botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        // Acción del botón de guardar
        saveButton.addActionListener(e -> {
            try {
                // Crear un objeto actualizado con los nuevos valores del formulario
                Item updatedItem = new Item(
                        this.panel, view, this.getId(),
                        new ItemDTO(
                                codeField.getText(), barCodeField.getText(), Double.parseDouble(costField.getText()),
                                Double.parseDouble(marginField.getText()), Double.parseDouble(priceField.getText()),
                                this.getItemData().getSupplier(), Integer.parseInt(stockField.getText())
                        ),
                        descriptionField.getText(),
                        this.getFamilyId()
                );

                // Llamar al metodo para modificar el artículo en la base de datos
                this.modifyItem(mainPanel, updatedItem, this.getId());
                JOptionPane.showMessageDialog(dialog, "Artículo actualizado con éxito.");
                dialog.dispose(); // Cerrar el cuadro de diálogo

                // Refrescar la tabla de artículos después de la modificación
                SwingUtilities.invokeLater(() -> {
                    mainPanel.removeAll();
                    Item.showItemTable(mainPanel, view);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            } catch (Exception ex) {
                // Manejar errores en la actualización y mostrar un mensaje al usuario
                JOptionPane.showMessageDialog(dialog, "Error al actualizar artículo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Acción del botón de cancelar
        cancelButton.addActionListener(e -> dialog.dispose());

        // Agregar los botones al panel de botones
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Agregar el formulario y el panel de botones al cuadro de diálogo
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Hacer visible el cuadro de diálogo
        dialog.setVisible(true);
    }

    /**
     * Metodo para actualizar un artículo en la base de datos.
     * Recibe un objeto `Item` con los nuevos valores y actualiza la información en la base de datos.
     *
     * @param mainPanel   Panel principal donde se mostrarán los cambios.
     * @param updatedItem Objeto `Item` con los datos actualizados.
     * @param id          Identificador del artículo a modificar.
     */
    private void modifyItem(JPanel mainPanel, Item updatedItem, int id) {
        // Consulta SQL para actualizar los datos del artículo basado en su ID
        String query = "UPDATE articulos SET codigoArticulo = ?, codigoBarrasArticulo = ?, " +
                "descripcionArticulo = ?, costeArticulo = ?, margenComercialArticulo = ?, " +
                "pvpArticulo = ?, stockArticulo = ? WHERE idArticulo = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Asignar los valores del objeto `updatedItem` a la consulta SQL
            ps.setString(1, updatedItem.getItemData().getCode());
            ps.setString(2, updatedItem.getItemData().getBarCode());
            ps.setString(3, updatedItem.getDescription());
            ps.setDouble(4, updatedItem.getItemData().getCost());
            ps.setDouble(5, updatedItem.getItemData().getMargin());
            ps.setDouble(6, updatedItem.getItemData().getPrice());
            ps.setInt(7, updatedItem.getItemData().getStock());
            ps.setInt(8, id); // ID del artículo a actualizar

            // Ejecutar la actualización y obtener el número de filas afectadas
            int rowsAffected = ps.executeUpdate();

            // Verificar si la actualización fue exitosa
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Artículo actualizado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se pudo actualizar el artículo. Verifica el ID.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            // Manejo de errores en la actualización y mostrar mensaje al usuario
            JOptionPane.showMessageDialog(mainPanel, "Error al modificar artículo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de artículos después de la modificación
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            Item.showItemTable(mainPanel, e -> {}); // Recargar la tabla
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }

    /**
     * Metodo para eliminar un artículo de la base de datos.
     * Solicita confirmación antes de proceder con la eliminación.
     *
     * @param mainPanel Panel principal donde se mostrarán los cambios.
     * @param id Identificador del artículo a eliminar.
     * @param view Vista principal de la aplicación.
     */
    public void deleteItem(JPanel mainPanel, int id, View view) {
        // Mostrar cuadro de diálogo para confirmar la eliminación
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
                "¿Estás seguro de que deseas eliminar el artículo con ID " + id + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        // Si el usuario selecciona "No", salir del metodo sin hacer nada
        if (confirm != JOptionPane.YES_OPTION) return;

        // Consulta SQL para eliminar el artículo por su ID
        String query = "DELETE FROM articulos WHERE idArticulo = ?";

        try (Connection conn = Utils.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id); // Asignar el ID del artículo al parámetro de la consulta
            int rowsAffected = ps.executeUpdate(); // Ejecutar la consulta

            // Verificar si se eliminó correctamente el artículo
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(mainPanel, "Artículo eliminado con éxito.");
            } else {
                JOptionPane.showMessageDialog(mainPanel, "No se encontró un artículo con el ID proporcionado.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            // Manejo de errores en la eliminación y mensaje de error
            JOptionPane.showMessageDialog(mainPanel, "Error al eliminar artículo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Refrescar la tabla de artículos después de la eliminación
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            Item.showItemTable(mainPanel, view);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
    }
}