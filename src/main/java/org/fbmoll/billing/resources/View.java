package org.fbmoll.billing.resources;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.crud.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class View extends JFrame implements ActionListener {
    final JPanel mainPanel;

    public View() {
        setTitle("Sistema de Gestión");
        setSize(1400, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu recordsMenu = new JMenu("Fichas");
        JMenu invoicesMenu = new JMenu("Facturas");
        JMenu correctiveInvoicesMenu = new JMenu("Rectificativas");
        JMenu listingsMenu = new JMenu("Listados");
        JMenu configurationMenu = new JMenu("Configuración");
        JMenu helpMenu = new JMenu("Ayuda");

        createRecordsMenu(recordsMenu);
        createInvoicesMenu(invoicesMenu);
        createCorrectiveInvoicesMenu(correctiveInvoicesMenu);
        createListingsMenu(listingsMenu);
        createConfigurationMenu(configurationMenu);
        createHelpMenu(helpMenu);

        menuBar.add(recordsMenu);
        menuBar.add(invoicesMenu);
        menuBar.add(correctiveInvoicesMenu);
        menuBar.add(listingsMenu);
        menuBar.add(configurationMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private void createRecordsMenu(JMenu menu) {
        addMenuItems(menu, "Clientes", "Artículos", "Proveedores", "Comerciales", "Distribuidores",
                "Trabajadores", "Tipos IVA", "Familias");
    }

    private void createInvoicesMenu(JMenu menu) {
        addMenuItems(menu, "Crear Factura", "Ver Facturas");
    }

    private void createCorrectiveInvoicesMenu(JMenu menu) {
        addMenuItems(menu, "Crear Rectificativa", "Ver Rectificativas");
    }

    private void createListingsMenu(JMenu menu) {
        addMenuItems(menu, "Listar Clientes", "Listar Artículos", "Listar Proveedores", "Listar Comerciales",
                "Listar Distribuidores", "Listar Trabajadores", "Listar Tipos IVA", "Listar Familias");
    }

    private void createConfigurationMenu(JMenu menu) {
        addMenuItems(menu, "Datos de la Empresa");
    }

    private void createHelpMenu(JMenu menu) {
        addMenuItems(menu, "Manual de Usuario", "Acerca de");
    }

    private void addMenuItems(JMenu menu, String... items) {
        for (String item : items) {
            JMenuItem menuItem = new JMenuItem(item);
            menu.add(menuItem);
            menuItem.addActionListener(this);
        }
    }

    @Override
    @SneakyThrows
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source instanceof JMenuItem item) {
            String itemText = item.getText();

            switch (itemText) {
                case "Clientes" -> ViewClients.visualizeClients(mainPanel);
                case "Artículos" -> ViewItems.showArticleTable(mainPanel);
                case "Proveedores" -> ViewProviders.showProviderTable(mainPanel);
                case "Comerciales" -> System.out.println("Comerciales menu item selected");
                case "Distribuidores" -> System.out.println("Distribuidores menu item selected");
                case "Trabajadores" -> System.out.println("Trabajadores menu item selected");
                case "Tipos IVA" -> ViewIVATypes.showIVATypesTable(mainPanel);
                case "Familias" -> ViewItemFamilies.showArticleFamilyTable(mainPanel);
                case "Crear Factura" -> System.out.println("Crear Factura menu item selected");
                case "Ver Facturas" -> ViewInvoices.showInvoiceTable(mainPanel);
                case "Crear Rectificativa" -> System.out.println("Crear Rectificativa menu item selected");
                case "Ver Rectificativas" -> ViewCorrectiveInvoices.showCorrectiveInvoiceTable(mainPanel);
                case "Listar Clientes" -> System.out.println("Listar Clientes menu item selected");
                case "Listar Artículos" -> System.out.println("Listar Artículos menu item selected");
                case "Listar Proveedores" -> System.out.println("Listar Proveedores menu item selected");
                case "Listar Comerciales" -> System.out.println("Listar Comerciales menu item selected");
                case "Listar Distribuidores" -> System.out.println("Listar Distribuidores menu item selected");
                case "Listar Trabajadores" -> System.out.println("Listar Trabajadores menu item selected");
                case "Listar Tipos IVA" -> System.out.println("Listar Tipos IVA menu item selected");
                case "Listar Familias" -> System.out.println("Listar Familias menu item selected");
                case "Datos de la Empresa" -> System.out.println("Datos de la Empresa menu item selected");
                case "Manual de Usuario" -> System.out.println("Manual de Usuario menu item selected");
                case "Acerca de" -> System.out.println("Acerca de menu item selected");
                default -> System.out.println("Unknown menu item selected");
            }
        }
    }
}