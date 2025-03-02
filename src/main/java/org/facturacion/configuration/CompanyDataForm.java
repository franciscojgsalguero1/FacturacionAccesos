package org.facturacion.configuration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CompanyDataForm extends JDialog {
    private JComboBox<String> paisComboBox = null;
    private final Map<String, JTextField> fieldMap = new HashMap<>(); // Mapa para almacenar los campos de entrada

    public CompanyDataForm(JPanel parentPanel) {
        setTitle("Datos de la Empresa");
        setSize(600, 500);
        setLayout(new BorderLayout());
        setModal(true);
        setLocationRelativeTo(parentPanel);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Definir los campos en orden
        String[] labels = {
                "Nombre", "Direccion", "Codigo Postal", "Ciudad", "Provincia", "Pais",
                "CIF", "Telefono", "Email", "IBAN", "Web"
        };

        // Crear los campos y añadirlos al formulario
        for (String label : labels) {
            JLabel lbl = new JLabel(label + ":");
            if (label.equals("Pais")) {
                // ComboBox para países
                paisComboBox = new JComboBox<>(new String[]{"España", "Francia", "Italia", "Alemania", "Portugal"});
                formPanel.add(lbl);
                formPanel.add(paisComboBox);
            } else {
                // Campos de texto para los demás datos
                JTextField field = new JTextField(20);
                fieldMap.put(label, field);
                formPanel.add(lbl);
                formPanel.add(field);
            }
        }

        // Cargar datos existentes en el formulario
        loadCompanyData();

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");

        // Acción de guardado
        saveButton.addActionListener(e -> {
            // Obtener valores desde los campos del formulario
            String nombre = fieldMap.get("Nombre").getText().trim();
            String direccion = fieldMap.get("Direccion").getText().trim();
            String codigoPostal = fieldMap.get("Codigo Postal").getText().trim();
            String ciudad = fieldMap.get("Ciudad").getText().trim();
            String provincia = fieldMap.get("Provincia").getText().trim();
            String pais = (String) paisComboBox.getSelectedItem();
            String cif = fieldMap.get("CIF").getText().trim();
            String telefono = fieldMap.get("Telefono").getText().trim();
            String email = fieldMap.get("Email").getText().trim();
            String iban = fieldMap.get("IBAN").getText().trim();
            String web = fieldMap.get("Web").getText().trim();

            // Llamar a la función pasando los valores obtenidos
            saveCompanyData(nombre, direccion, codigoPostal, ciudad, provincia, pais, cif, telefono, email, iban, web);
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Carga los datos de la empresa desde la base de datos y los muestra en los campos del formulario.
     */
    private void loadCompanyData() {
        // Aquí iría la consulta a la base de datos para cargar los datos de la empresa
        // Ejemplo: Simulación de carga de datos (estos datos vendrían de la BD)
        fieldMap.get("Nombre").setText("Ejemplo S.L.");
        fieldMap.get("Direccion").setText("Calle Falsa 123");
        fieldMap.get("Codigo Postal").setText("08001");
        fieldMap.get("Ciudad").setText("Barcelona");
        fieldMap.get("Provincia").setText("Barcelona");
        paisComboBox.setSelectedItem("España");
        fieldMap.get("CIF").setText("B12345678");
        fieldMap.get("Telefono").setText("+34 600 123 456");
        fieldMap.get("Email").setText("contacto@ejemplo.com");
        fieldMap.get("IBAN").setText("ES9121000418450200051332");
        fieldMap.get("Web").setText("https://www.ejemplo.com");
    }

    /**
     * Guarda o actualiza los datos de la empresa en la base de datos.
     */
    private void saveCompanyData(String nombre, String direccion, String codigoPostal, String ciudad,
                                 String provincia, String pais, String cif, String telefono,
                                 String email, String iban, String web) {
        // Aquí iría la lógica para guardar o actualizar en la base de datos
        JOptionPane.showMessageDialog(this, "Datos de la empresa guardados correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        dispose(); // Cierra el formulario tras guardar
    }
}