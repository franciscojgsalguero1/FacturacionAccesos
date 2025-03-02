package org.facturacion.resources;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;

@Getter
public class Button extends JButton {
    String name;

    public Button(String name) {
        this.name = name;
        this.setFont(new Font("Arial", Font.BOLD, 30));
        this.setBackground(Color.CYAN);
        this.setPreferredSize(new Dimension(280, 80));
    }
}