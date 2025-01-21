package org.fbmoll.billing.resources;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.crud.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class View extends JFrame implements ActionListener {
    final JPanel mainPanel;
    static final Logger logger = LoggerFactory.getLogger(View.class);

    public View() {
        this.setTitle("Sistema de Gestión");
        this.setSize(1400, 750);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

        this.setLocationRelativeTo(null);
        this.setVisible(true);
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
                case "Artículos" -> ViewItems.showItemsTable(mainPanel);
                case "Proveedores" -> ViewProviders.showProviderTable(mainPanel);
                case "Comerciales" -> logger.info("Comerciales menu item selected");
                case "Distribuidores" -> logger.info("Distribuidores menu item selected");
                case "Trabajadores" -> logger.info("Trabajadores menu item selected");
                case "Tipos IVA" -> ViewIVATypes.showIVATypesTable(mainPanel);
                case "Familias" -> ViewItemFamilies.showArticleFamilyTable(mainPanel);
                case "Crear Factura" -> {
                    ViewInvoices.showInvoiceTable(mainPanel);
                    new CreateInvoice().createInvoice(mainPanel);
                }
                case "Ver Facturas" -> ViewInvoices.showInvoiceTable(mainPanel);
                case "Crear Rectificativa" -> {
                    ViewCorrectiveInvoices.showCorrectiveInvoiceTable(mainPanel);
                    new CreateCorrectiveInvoice().createInvoice(mainPanel);
                }
                case "Ver Rectificativas" -> ViewCorrectiveInvoices.showCorrectiveInvoiceTable(mainPanel);
                case "Listar Clientes" -> logger.info("Listar Clientes menu item selected");
                case "Listar Artículos" -> logger.info("Listar Artículos menu item selected");
                case "Listar Proveedores" -> logger.info("Listar Proveedores menu item selected");
                case "Listar Comerciales" -> logger.info("Listar Comerciales menu item selected");
                case "Listar Distribuidores" -> logger.info("Listar Distribuidores menu item selected");
                case "Listar Trabajadores" -> logger.info("Listar Trabajadores menu item selected");
                case "Listar Tipos IVA" -> logger.info("Listar Tipos IVA menu item selected");
                case "Listar Familias" -> logger.info("Listar Familias menu item selected");
                case "Datos de la Empresa" -> logger.info("Datos de la Empresa menu item selected");
                case "Manual de Usuario" -> logger.info("Manual de Usuario menu item selected");
                case "Acerca de" -> logger.info("Acerca de menu item selected");
                default -> logger.info("Unknown menu item selected");
            }
        }
    }
}