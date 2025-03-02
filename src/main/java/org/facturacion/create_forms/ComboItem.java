package org.facturacion.create_forms;

import lombok.Getter;

/**
 * Clase que representa un elemento de un JComboBox con un identificador numérico o un valor decimal.
 * Se usa para almacenar tanto identificadores de base de datos como valores numéricos de IVA.
 */
@Getter
public class ComboItem {
    private final int id;  // Identificador del elemento (por ejemplo, ID de cliente, trabajador, metodo de pago)
    private final String display; // Texto que se mostrará en el JComboBox
    private final double numeric; // Valor numérico (por ejemplo, porcentaje de IVA)

    /**
     * Constructor para elementos que tienen un ID (por ejemplo, clientes, trabajadores, métodos de pago).
     *
     * @param id      Identificador del elemento.
     * @param display Texto que se mostrará en el JComboBox.
     */
    public ComboItem(int id, String display) {
        this(id, display, 0); // Delegación al constructor principal
    }

    /**
     * Constructor para elementos que representan valores numéricos (por ejemplo, IVA).
     *
     * @param numeric Valor numérico del elemento.
     * @param display Texto que se mostrará en el JComboBox.
     */
    public ComboItem(double numeric, String display) {
        this(0, display, numeric); // Delegación al constructor principal
    }

    /**
     * Constructor principal privado para evitar duplicación de código.
     *
     * @param id      Identificador del elemento.
     * @param display Texto que se mostrará en el JComboBox.
     * @param numeric Valor numérico asociado al elemento.
     */
    private ComboItem(int id, String display, double numeric) {
        this.id = id;
        this.display = display;
        this.numeric = numeric;
    }

    /**
     * Devuelve el texto que se mostrará en el JComboBox.
     *
     * @return El texto del elemento.
     */
    @Override
    public String toString() {
        return display;
    }
}