package org.facturacion.content;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.facturacion.configuration.CompanyDataForm;
import org.facturacion.data_classes.*;
import org.facturacion.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Clase principal de la vista de la aplicación que gestiona la interfaz gráfica y las acciones del usuario.
 * Extiende JFrame y maneja eventos de botones y menús.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class View extends JFrame implements ActionListener {
    static final Logger logger = LoggerFactory.getLogger(View.class);
    final JPanel mainPanel; // Panel principal donde se mostrarán los datos.

    /**
     * Constructor de la vista principal.
     */
    public View() {
        this.setTitle("Sistema de Gestión");
        this.setSize(1400, 750);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Creación de la barra de menú en el orden deseado
        JMenuBar menuBar = new JMenuBar();

        JMenu recordsMenu = new JMenu("Registros");
        JMenu invoicesMenu = new JMenu("Facturas");
        JMenu correctiveInvoicesMenu = new JMenu("Rectificativas");
        JMenu configurationMenu = new JMenu("Configuración");
        JMenu helpMenu = new JMenu("Ayuda");

        // Añadir opciones a cada menú
        createRecordsMenu(recordsMenu);
        createInvoicesMenu(invoicesMenu);
        createCorrectiveInvoicesMenu(correctiveInvoicesMenu);
        createConfigurationMenu(configurationMenu);
        createHelpMenu(helpMenu);

        // Añadir menús a la barra en el orden correcto
        menuBar.add(recordsMenu);
        menuBar.add(invoicesMenu);
        menuBar.add(correctiveInvoicesMenu);
        menuBar.add(configurationMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        // Configuración del panel principal
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Crea el menú de registros con opciones en orden fijo.
     */
    private void createRecordsMenu(JMenu menu) {
        addMenuItems(menu, "Clientes", "Artículos", "Proveedores", "Trabajadores", "Tipos IVA", "Familias");
    }

    /**
     * Crea el menú de facturas con opciones en orden fijo.
     */
    private void createInvoicesMenu(JMenu menu) {
        addMenuItems(menu, "Ver Facturas");
    }

    /**
     * Crea el menú de facturas rectificativas con opciones en orden fijo.
     */
    private void createCorrectiveInvoicesMenu(JMenu menu) {
        addMenuItems(menu, "Ver Rectificativas");
    }

    /**
     * Crea el menú de configuración con opciones en orden fijo.
     */
    private void createConfigurationMenu(JMenu menu) {
        JMenuItem companyDataMenuItem = new JMenuItem("Datos de la Empresa");
        menu.add(companyDataMenuItem);
        companyDataMenuItem.addActionListener(e -> new CompanyDataForm(mainPanel));
    }

    /**
     * Crea el menú de ayuda con opciones en orden fijo.
     */
    private void createHelpMenu(JMenu menu) {
        addMenuItems(menu, "Manual de Usuario", "Acerca de");
    }

    /**
     * Agrega elementos de menú con sus respectivos ActionListener.
     *
     * @param menu  JMenu al que se agregarán los elementos.
     * @param items Nombres de los elementos a agregar.
     */
    private void addMenuItems(JMenu menu, String... items) {
        for (String item : items) {
            JMenuItem menuItem = new JMenuItem(item);
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }
    }

    /**
     * Maneja los eventos de los botones y menús.
     */
    @Override
    @SneakyThrows
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String actionCommand = e.getActionCommand();

        if (source instanceof JMenuItem) {
            handleMenuItemAction(actionCommand);
        } else {
            handleEntityActions(source, actionCommand);
        }
    }

    /**
     * Maneja las acciones de los elementos del menú.
     */
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

        menuActions.getOrDefault(itemText, () -> logger.warn("Opción no encontrada: " + itemText)).run();
    }

    /**
     * Maneja las acciones de las entidades de la aplicación (clientes, artículos, proveedores, etc.).
     */
    private void handleEntityActions(Object source, String actionCommand) {
        Map<Class<?>, Consumer<Object>> entityHandlers = Map.of(
                Client.class, obj -> handleClientActions((Client) obj, actionCommand),
                Item.class, obj -> handleItemActions((Item) obj, actionCommand),
                Provider.class, obj -> handleProviderActions((Provider) obj, actionCommand),
                Worker.class, obj -> handleWorkerActions((Worker) obj, actionCommand),
                ItemFamily.class, obj -> handleItemFamilyActions((ItemFamily) obj, actionCommand),
                IVATypes.class, obj -> handleIVAActions((IVATypes) obj, actionCommand),
                Invoice.class, obj -> handleInvoiceActions((Invoice) obj, actionCommand),
                CorrectiveInvoice.class, obj -> handleCorrectiveInvoiceActions((CorrectiveInvoice) obj, actionCommand)
        );

        entityHandlers.getOrDefault(source.getClass(), obj -> logger.warn("Acción no encontrada para: " + source))
                .accept(source);
    }

    // Métodos específicos para manejar acciones de cada entidad.

    private void handleClientActions(Client client, String actionCommand) {
        switch (actionCommand) {
            case Constants.CLIENT_EDIT -> client.modifyClientAction(mainPanel, this);
            case Constants.CLIENT_DELETE -> client.deleteClient(mainPanel, client.getId(), this);
            default -> logger.warn("Acción desconocida para cliente: " + actionCommand);
        }
    }

    private void handleItemActions(Item item, String actionCommand) {
        switch (actionCommand) {
            case Constants.ARTICLE_EDIT -> item.modifyItemAction(mainPanel, this);
            case Constants.ARTICLE_DELETE -> item.deleteItem(mainPanel, item.getId(), this);
            default -> logger.warn("Acción desconocida para artículo: " + actionCommand);
        }
    }

    private void handleProviderActions(Provider provider, String actionCommand) {
        switch (actionCommand) {
            case Constants.PROVIDER_EDIT -> provider.modifyProviderAction(mainPanel, this);
            case Constants.PROVIDER_DELETE -> provider.deleteProvider(mainPanel, provider.getId(), this);
            default -> logger.warn("Acción desconocida para proveedor: " + actionCommand);
        }
    }

    private void handleWorkerActions(Worker worker, String actionCommand) {
        switch (actionCommand) {
            case Constants.WORKER_EDIT -> worker.modifyWorkerAction(mainPanel, this);
            case Constants.WORKER_DELETE -> worker.deleteWorker(mainPanel, worker.getId(), this);
            default -> logger.warn("Acción desconocida para trabajador: " + actionCommand);
        }
    }

    private void handleItemFamilyActions(ItemFamily itemFamily, String actionCommand) {
        switch (actionCommand) {
            case Constants.FAMILY_EDIT -> itemFamily.modifyItemFamilyAction(mainPanel, this);
            case Constants.FAMILY_DELETE -> itemFamily.deleteItemFamily(mainPanel, itemFamily.getId(), this);
            default -> logger.warn("Acción desconocida para familia de artículos: " + actionCommand);
        }
    }

    private void handleIVAActions(IVATypes ivaTypes, String actionCommand) {
        switch (actionCommand) {
            case Constants.IVA_EDIT -> ivaTypes.modifyIVATypesAction(mainPanel, this);
            case Constants.IVA_DELETE -> ivaTypes.deleteIVATypes(mainPanel, ivaTypes.getId(), this);
            default -> logger.warn("Acción desconocida para tipo de IVA: " + actionCommand);
        }
    }

    private void handleInvoiceActions(Invoice invoice, String actionCommand) {
        switch (actionCommand) {
            case Constants.INVOICE_VIEW -> new ViewInvoice(mainPanel, this, invoice.getId());
            case Constants.INVOICE_DELETE -> invoice.deleteInvoice(mainPanel, invoice.getId(), this);
            default -> logger.warn("Acción desconocida para factura: " + actionCommand);
        }
    }

    private void handleCorrectiveInvoiceActions(CorrectiveInvoice invoice, String actionCommand) {
        switch (actionCommand) {
            case Constants.INVOICE_VIEW -> new ViewCorrectiveInvoice(mainPanel, invoice.getId());
            case Constants.INVOICE_DELETE -> invoice.deleteCorrectiveInvoice(mainPanel, invoice.getId(), this);
            default -> logger.warn("Acción desconocida para factura rectificativa: " + actionCommand);
        }
    }
}