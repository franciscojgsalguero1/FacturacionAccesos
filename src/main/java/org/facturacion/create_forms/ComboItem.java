package org.facturacion.create_forms;

import lombok.Getter;

/**
 * Clase que representa un elemento de un JComboBox con un identificador numérico o un valor decimal.
 * Se usa para almacenar tanto identificadores de base de datos como valores numéricos de IVA.
 */
public class ComboItem {

    @Getter
    private final int id;  // Identificador del elemento (por ejemplo, ID de cliente, trabajador, método de pago)

    private final String display; // Texto que se mostrará en el JComboBox

    @Getter
    private final double numeric; // Valor numérico (por ejemplo, porcentaje de IVA)

    /**
     * Constructor para elementos que tienen un ID (por ejemplo, clientes, trabajadores, métodos de pago).
     *
     * @param id      Identificador del elemento.
     * @param display Texto que se mostrará en el JComboBox.
     */
    public ComboItem(int id, String display) {
        this.id = id;
        this.display = display;
        this.numeric = 0; // Se establece en 0 porque este constructor es para elementos con ID.
    }

    /**
     * Constructor para elementos que representan valores numéricos (por ejemplo, IVA).
     *
     * @param numeric Valor numérico del elemento.
     * @param display Texto que se mostrará en el JComboBox.
     */
    public ComboItem(double numeric, String display) {
        this.id = 0; // Se establece en 0 porque este constructor es para valores numéricos.
        this.numeric = numeric;
        this.display = display;
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

