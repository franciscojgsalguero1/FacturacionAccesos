package org.facturacion.content;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.facturacion.data_classes.*;
import org.facturacion.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class View extends JFrame implements ActionListener {
    static final Logger logger = LoggerFactory.getLogger(View.class);
    final JPanel mainPanel;

    public View() {
        this.setTitle("Sistema de Gestión");
        this.setSize(1400, 750);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        JMenu recordsMenu = new JMenu(Constants.FIELD_CLIENTS);
        JMenu invoicesMenu = new JMenu("Facturas");
        JMenu correctiveInvoices = new JMenu("Rectificativas");
        JMenu configurationMenu = new JMenu("Configuración");
        JMenu helpMenu = new JMenu("Ayuda");

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
        addMenuItems(menu, Constants.FIELD_CLIENTS, "Artículos", "Proveedores",
                "Trabajadores", "Tipos IVA", "Familias");
    }

    private void createInvoicesMenu(JMenu menu) {
        addMenuItems(menu, "Ver Facturas");
    }

    private void createCorrectiveInvoicesMenu(JMenu menu) {
        addMenuItems(menu, "Ver Rectificativas");
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
        String actionCommand = e.getActionCommand();

        if (source instanceof JMenuItem item) {
            handleMenuItemAction(item.getText());
        } else {
            handleEntityActions(source, actionCommand);
        }
    }

    private void handleMenuItemAction(String itemText) {
        Map<String, Runnable> menuActions = Map.of(
                Constants.FIELD_CLIENTS, () -> Client.showClientTable(mainPanel, this),
                "Artículos", () -> Item.showItemTable(mainPanel, this),
                "Proveedores", () -> Provider.showProviderTable(mainPanel, this),
                "Trabajadores", () -> Worker.showWorkerTable(mainPanel, this),
                "Familias", () -> ItemFamily.showItemFamilyTable(mainPanel, this),
                "Tipos IVA", () -> IVATypes.showIVATypesTable(mainPanel, this),
                "Ver Facturas", () -> Invoice.showInvoiceTable(mainPanel, this),
                "Ver Rectificativas", () -> CorrectiveInvoice.showCorrectiveInvoiceTable(mainPanel, this)
        );

        menuActions.getOrDefault(itemText, () -> logger.info("Not found.")).run();
    }

    private void handleEntityActions(Object source, String actionCommand) {
        if (source instanceof Client client) {
            handleClientActions(client, actionCommand);
        } else if (source instanceof Item item) {
            handleItemActions(item, actionCommand);
        } else if (source instanceof Provider provider) {
            handleProviderActions(provider, actionCommand);
        } else if (source instanceof Worker worker) {
            handleWorkerActions(worker, actionCommand);
        } else if (source instanceof ItemFamily itemFamily) {
            handleItemFamilyActions(itemFamily, actionCommand);
        } else if (source instanceof IVATypes ivaTypes) {
            handleIVAActions(ivaTypes, actionCommand);
        } else if (source instanceof Invoice invoice) {
            handleInvoiceActions(invoice, actionCommand);
        } else if (source instanceof CorrectiveInvoice invoice) {
            handleCorrectiveInvoiceActions(invoice, actionCommand);
        }
    }

    private void handleClientActions(Client client, String actionCommand) {
        if (actionCommand.equals(Constants.CLIENT_EDIT)) {
            client.modifyClientAction(mainPanel, this);
        } else if (actionCommand.equals(Constants.CLIENT_DELETE)) {
            client.deleteClient(mainPanel, client.getId(), this);
        }
    }

    private void handleItemActions(Item item, String actionCommand) {
        if (actionCommand.equals(Constants.ARTICLE_EDIT)) {
            item.modifyItemAction(mainPanel, this);
        } else if (actionCommand.equals(Constants.ARTICLE_DELETE)) {
            item.deleteItem(mainPanel, item.getId(), this);
        }
    }

    private void handleProviderActions(Provider provider, String actionCommand) {
        if (actionCommand.equals(Constants.PROVIDER_EDIT)) {
            provider.modifyProviderAction(mainPanel, this);
        } else if (actionCommand.equals(Constants.PROVIDER_DELETE)) {
            provider.deleteProvider(mainPanel, provider.getId(), this);
        }
    }

    private void handleWorkerActions(Worker worker, String actionCommand) {
        if (actionCommand.equals(Constants.WORKER_EDIT)) {
            worker.modifyWorkerAction(mainPanel, this);
        } else if (actionCommand.equals(Constants.WORKER_DELETE)) {
            worker.deleteWorker(mainPanel, worker.getId(), this);
        }
    }

    private void handleItemFamilyActions(ItemFamily itemFamily, String actionCommand) {
        if (actionCommand.equals(Constants.FAMILY_EDIT)) {
            itemFamily.modifyItemFamilyAction(mainPanel, this);
        } else if (actionCommand.equals(Constants.FAMILY_DELETE)) {
            itemFamily.deleteItemFamily(mainPanel, itemFamily.getId(), this);
        }
    }

    private void handleIVAActions(IVATypes ivaTypes, String actionCommand) {
        if (actionCommand.equals(Constants.IVA_EDIT)) {
            ivaTypes.modifyIVATypesAction(mainPanel, this);
        } else if (actionCommand.equals(Constants.IVA_DELETE)) {
            ivaTypes.deleteIVATypes(mainPanel, ivaTypes.getId(), this);
        }
    }

    private void handleInvoiceActions(Invoice invoice, String actionCommand) {
        switch (actionCommand) {
            case Constants.INVOICE_VIEW -> new ViewInvoice(mainPanel, this, invoice.getId());
            case Constants.INVOICE_DELETE -> invoice.deleteInvoice(mainPanel, invoice.getId(), this);
            default -> throw new IllegalStateException("Unexpected value: " + actionCommand);
        }
    }

    private void handleCorrectiveInvoiceActions(CorrectiveInvoice invoice, String actionCommand) {
        switch (actionCommand) {
            case Constants.INVOICE_VIEW -> new ViewCorrectiveInvoice(mainPanel, invoice.getId());
            case Constants.INVOICE_DELETE -> invoice.deleteCorrectiveInvoice(mainPanel, invoice.getId(), this);
            default -> throw new IllegalStateException("Unexpected value: " + actionCommand);
        }
    }
}