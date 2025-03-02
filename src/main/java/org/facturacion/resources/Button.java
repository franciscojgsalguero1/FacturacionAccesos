package org.facturacion.resources;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

/**
 * Clase personalizada para un botón estilizado.
 */
@Getter
public class Button extends JButton {
    private final String name;

    /**
     * Constructor que inicializa el botón con un nombre y estilo predefinido.
     *
     * @param name Nombre del botón.
     */
    public Button(String name) {
        super(name); // Establece el texto del botón directamente en el constructor de JButton
        this.name = name;
        initializeButtonStyle(Color.CYAN);
    }

    /**
     * Metodo privado para configurar el estilo del botón.
     */
    private void initializeButtonStyle(Color color) {
        setFont(new Font("Arial", Font.BOLD, 30));
        setBackground(color);
        setPreferredSize(new Dimension(280, 80));
        setFocusPainted(false); // Elimina el borde de selección al hacer clic
    }
}