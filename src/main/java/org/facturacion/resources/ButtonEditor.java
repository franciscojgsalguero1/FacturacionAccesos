package org.facturacion.resources;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Editor de celdas personalizado para botones dentro de una tabla.
 * Permite asociar una acción específica a cada botón en función de la fila seleccionada.
 *
 * @param <T> Tipo de objeto asociado a cada fila de la tabla.
 */
public class ButtonEditor<T> extends AbstractCellEditor implements TableCellEditor {
    private final JButton button;
    private final List<T> items;
    private T item;

    /**
     * Constructor para crear un editor de celda basado en botones.
     *
     * @param listener      Acción a ejecutar cuando se presiona el botón.
     * @param items         Lista de elementos asociados a cada fila.
     * @param actionCommand Comando de acción para identificar el evento.
     */
    public ButtonEditor(ActionListener listener, List<T> items, String actionCommand) {
        this.items = items;
        this.button = createButton(listener, actionCommand);
    }

    /**
     * Configura y crea el botón de la celda.
     */
    private JButton createButton(ActionListener listener, String actionCommand) {
        JButton btn = new JButton();
        btn.setOpaque(true);
        btn.addActionListener(e -> {
            if (item != null) {
                stopCellEditing();
                listener.actionPerformed(new ActionEvent(item, ActionEvent.ACTION_PERFORMED, actionCommand));
            }
        });
        return btn;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof JButton btn) {
            button.setText(btn.getText());
        }
        item = items.get(table.convertRowIndexToModel(row));
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return button;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return super.stopCellEditing();
    }
}