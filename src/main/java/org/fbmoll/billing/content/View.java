package org.fbmoll.billing.content;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.fbmoll.billing.data_classes.*;
import org.fbmoll.billing.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
                Constants.FIELD_WORKERS, Constants.FIELD_IVA_TYPES, Constants.FIELD_FAMILIES);
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
                case Constants.FIELD_CLIENTS -> Client.showClientTable(mainPanel, this);
                case Constants.FIELD_ARTICLES -> Item.showItemTable(mainPanel, this);
                case Constants.FIELD_PROVIDERS -> Provider.showProviderTable(mainPanel, this);
            }
        } else if (Constants.CLIENT_EDIT.equals(e.getActionCommand()) && source instanceof Client client) {
            client.modifyClientAction(mainPanel, this);
        } else if (Constants.CLIENT_DELETE.equals(e.getActionCommand()) && source instanceof Client client) {
            client.deleteClient(mainPanel, client.getId());
        } else if (Constants.ARTICLE_EDIT.equals(e.getActionCommand()) && source instanceof Item item) {
            item.modifyItemAction(mainPanel, this);
        } else if (Constants.ARTICLE_DELETE.equals(e.getActionCommand()) && source instanceof Item item) {
            item.deleteItem(mainPanel, item.getId());
        } else if (Constants.PROVIDER_EDIT.equals(e.getActionCommand()) && source instanceof Provider provider) {
            provider.modifyProviderAction(mainPanel, this);
        } else if (Constants.PROVIDER_DELETE.equals(e.getActionCommand()) && source instanceof Provider provider) {
            provider.deleteProvider(mainPanel, provider.getId());
        }
    }
}