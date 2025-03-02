package org.facturacion.resources;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Renderizador de celdas para botones dentro de una tabla.
 * Permite mostrar botones en celdas de una JTable con estilos personalizados.
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {
    /**
     * Constructor que inicializa el botón con las propiedades necesarias.
     */
    public ButtonRenderer() {
        setOpaque(true);
    }

    /**
     * Configura el botón como renderizador de una celda de la tabla.
     *
     * @param table     La JTable donde se está renderizando el botón.
     * @param value     El valor de la celda (generalmente un botón).
     * @param isSelected Indica si la celda está seleccionada.
     * @param hasFocus  Indica si la celda tiene el foco.
     * @param row       Fila de la celda.
     * @param column    Columna de la celda.
     * @return          El botón configurado como renderizador de la celda.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof JButton btn) {
            setText(btn.getText());
        }

        setBackground(isSelected ? table.getSelectionBackground() : UIManager.getColor("Button.background"));
        return this;
    }
}