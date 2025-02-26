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
        JMenu correctiveInvoices = new JMenu(Constants.FIELD_CORRECTIVE);
        JMenu configurationMenu = new JMenu(Constants.FIELD_CONFIGURATION);
        JMenu helpMenu = new JMenu(Constants.FIELD_HELP);

        createRecordsMenu(recordsMenu);
        createInvoicesMenu(invoicesMenu);
        createCorrectiveInvoicesMenu(correctiveInvoices);
        createConfigurationMenu(configurationMenu);
        createHelpMenu(helpMenu);

        menuBar.add(recordsMenu);
        menuBar.add(invoicesMenu);
        menuBar.add(correctiveInvoices);
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
        addMenuItems(menu, Constants.FIELD_SEE_INVOICE);
    }

    private void createCorrectiveInvoicesMenu(JMenu menu) {
        addMenuItems(menu, Constants.FIELD_SEE_CORRECTIVE);
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
                case Constants.FIELD_WORKERS -> Worker.showWorkerTable(mainPanel, this);
                case Constants.FIELD_FAMILIES -> ItemFamily.showItemFamilyTable(mainPanel, this);
                case Constants.FIELD_IVA_TYPES -> IVATypes.showIVATypesTable(mainPanel, this);
                case "Ver Facturas" -> Invoice.showInvoiceTable(mainPanel, this);
                default -> logger.info("Not found.");
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
        } else if (Constants.WORKER_EDIT.equals(e.getActionCommand()) && source instanceof Worker worker) {
            worker.modifyWorkerAction(mainPanel, this);
        } else if (Constants.WORKER_DELETE.equals(e.getActionCommand()) && source instanceof Worker worker) {
            worker.deleteWorker(mainPanel, worker.getId());
        } else if (Constants.FAMILY_EDIT.equals(e.getActionCommand()) && source instanceof ItemFamily itemFamily) {
            itemFamily.modifyItemFamilyAction(mainPanel, this);
        } else if (Constants.FAMILY_DELETE.equals(e.getActionCommand()) && source instanceof ItemFamily itemFamily) {
            itemFamily.deleteItemFamily(mainPanel, itemFamily.getId());
        } else if (Constants.IVA_EDIT.equals(e.getActionCommand()) && source instanceof IVATypes ivaTypes) {
            ivaTypes.modifyIVATypesAction(mainPanel, this);
        } else if (Constants.IVA_DELETE.equals(e.getActionCommand()) && source instanceof IVATypes ivaTypes) {
            ivaTypes.deleteIVATypes(mainPanel, ivaTypes.getId());
        } else if (Constants.INVOICE_VIEW.equals(e.getActionCommand()) && source instanceof Invoice invoice) {
            new ViewInvoice(mainPanel, invoice.getId());
        } else if (Constants.INVOICE_EDIT.equals(e.getActionCommand()) && source instanceof Invoice invoice) {
            invoice.modifyInvoiceAction(mainPanel, this);
        } else if (Constants.INVOICE_DELETE.equals(e.getActionCommand()) && source instanceof Invoice invoice) {
            invoice.deleteInvoice(mainPanel, invoice.getId());
        }
    }
}