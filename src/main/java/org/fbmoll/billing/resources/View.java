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
        JMenu recordsMenu = new JMenu(Constants.FIELD_CLIENTS);
        JMenu invoicesMenu = new JMenu(Constants.FIELD_INVOICES);
        JMenu correctiveInvoicesMenu = new JMenu(Constants.FIELD_CORRECTIVE);
        JMenu configurationMenu = new JMenu(Constants.FIELD_CONFIGURATION);
        JMenu helpMenu = new JMenu(Constants.FIELD_HELP);

        createRecordsMenu(recordsMenu);
        createInvoicesMenu(invoicesMenu);
        createCorrectiveInvoicesMenu(correctiveInvoicesMenu);
        createConfigurationMenu(configurationMenu);
        createHelpMenu(helpMenu);

        menuBar.add(recordsMenu);
        menuBar.add(invoicesMenu);
        menuBar.add(correctiveInvoicesMenu);
        menuBar.add(configurationMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void createRecordsMenu(JMenu menu) {
        addMenuItems(menu, Constants.FIELD_CLIENTS, Constants.FIELD_ARTICLES, Constants.FIELD_PROVIDERS,
                Constants.FIELD_SELLERS, Constants.FIELD_WORKERS, Constants.FIELD_IVA_TYPES, Constants.FIELD_FAMILIES);
    }

    private void createInvoicesMenu(JMenu menu) {
        addMenuItems(menu, Constants.BUTTON_CREATE + Constants.FIELD_INVOICES, Constants.FIELD_SEE_INVOICE);
    }

    private void createCorrectiveInvoicesMenu(JMenu menu) {
        addMenuItems(menu, Constants.BUTTON_CREATE + Constants.FIELD_CORRECTIVE, Constants.FIELD_SEE_CORRECTIVE);
    }

    private void createConfigurationMenu(JMenu menu) {
        addMenuItems(menu, Constants.FIELD_EMPLOYER_DATA);
    }

    private void createHelpMenu(JMenu menu) {
        addMenuItems(menu, Constants.FIELD_USER_GUIDE, Constants.FIELD_ABOUT);
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
                case Constants.FIELD_CLIENTS -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetAllClients(), Constants.FIELD_CLIENTS);
                case Constants.FIELD_ARTICLES -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetArticles(), Constants.FIELD_ARTICLES);
                case Constants.FIELD_PROVIDERS -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetProviders(), Constants.FIELD_PROVIDERS);
                case Constants.FIELD_IVA_TYPES -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetIVATypes(), Constants.FIELD_IVA_TYPES);
                case Constants.FIELD_FAMILIES -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetItemFamilies(), Constants.FIELD_FAMILIES);

                case Constants.BUTTON_CREATE + Constants.FIELD_INVOICES ->
                        new GenericCreateForm(Constants.FIELD_INVOICES, mainPanel);
                case Constants.BUTTON_CREATE + Constants.FIELD_CORRECTIVE ->
                        new GenericCreateForm(Constants.FIELD_CORRECTIVE, mainPanel);

                case Constants.FIELD_SEE_INVOICE -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetInvoices(), Constants.FIELD_INVOICES);
                case Constants.FIELD_SEE_CORRECTIVE -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetCorrective(), Constants.FIELD_SEE_CORRECTIVE);

                case Constants.FIELD_SELLERS -> logger.info(Constants.FIELD_SELLERS);
                case Constants.FIELD_WORKERS -> GenericTableView.showTable(mainPanel,
                        Queries.queryGetWorkers(), Constants.FIELD_WORKERS);
                case Constants.FIELD_EMPLOYER_DATA -> logger.info(Constants.FIELD_EMPLOYER_DATA);
                case Constants.FIELD_USER_GUIDE -> logger.info(Constants.FIELD_USER_GUIDE);
                case Constants.FIELD_ABOUT -> logger.info(Constants.FIELD_ABOUT);

                default -> logger.info("Unknown menu item selected");
            }
        }
    }
}