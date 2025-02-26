package org.fbmoll.billing.resources;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== Database Table Names ==========
    public static final String TABLE_CLIENTS = "clientes";
    public static final String TABLE_INVOICES = "facturasclientes";
    public static final String TABLE_ARTICLES = "articulos";
    public static final String TABLE_PROVIDERS = "proveedores";
    public static final String TABLE_FAMILIES = "familias";
    public static final String TABLE_IVA_TYPES = "tiposiva";

    // ========== Entity Types (For Form Mapping) ==========
    public static final String FIELD_CLIENTS = "Clientes";
    public static final String FIELD_INVOICES = "Facturas";
    public static final String FIELD_INVOICE = "Factura";
    public static final String FIELD_ARTICLES = "Artículos";
    public static final String FIELD_PROVIDERS = "Proveedores";
    public static final String FIELD_SELLERS = "Comerciales";
    public static final String FIELD_WORKERS = "Trabajadores";
    public static final String FIELD_FAMILIES = "Familias";
    public static final String FIELD_IVA_TYPES = "Tipos IVA";
    public static final String FIELD_CORRECTIVE = "Rectificativas";
    public static final String FIELD_SEE_INVOICE = "Ver Facturas";
    public static final String FIELD_SEE_CORRECTIVE = "Ver Rectificativas";
    public static final String FIELD_EMPLOYER_DATA = "Datos de la Empresa";
    public static final String FIELD_USER_GUIDE = "Manual de Usuario";
    public static final String FIELD_ABOUT = "Acerca de";
    public static final String FIELD_HELP = "Ayuda";
    public static final String FIELD_CONFIGURATION = "Configuración";

    // ========== Client Fields ==========
    public static final String FIELD_CLIENT_NAME = "nombreCliente";
    public static final String LABEL_CLIENT_NAME = "Nombre";

    public static final String FIELD_CLIENT_ADDRESS = "direccionCliente";
    public static final String LABEL_CLIENT_ADDRESS = "Dirección";

    public static final String FIELD_CLIENT_POSTCODE = "cpCliente";
    public static final String LABEL_CLIENT_POSTCODE = "Código Postal";

    public static final String FIELD_CLIENT_CITY = "poblacionCliente";
    public static final String LABEL_CLIENT_CITY = "Población";

    public static final String FIELD_CLIENT_PROVINCE = "provinciaCliente";
    public static final String LABEL_CLIENT_PROVINCE = "Provincia";

    public static final String FIELD_CLIENT_COUNTRY = "paisCliente";
    public static final String LABEL_CLIENT_COUNTRY = "País";

    public static final String FIELD_CLIENT_CIF = "cifCliente";
    public static final String LABEL_CLIENT_CIF = "CIF";

    public static final String FIELD_CLIENT_PHONE = "telCliente";
    public static final String LABEL_CLIENT_PHONE = "Teléfono";

    public static final String FIELD_CLIENT_EMAIL = "emailCliente";
    public static final String LABEL_CLIENT_EMAIL = "Email";

    public static final String FIELD_CLIENT_IBAN = "ibanCliente";
    public static final String LABEL_CLIENT_IBAN = "IBAN";

    public static final String FIELD_CLIENT_RISK = "riesgoCliente";
    public static final String LABEL_CLIENT_RISK = "Riesgo";

    public static final String FIELD_CLIENT_DISCOUNT = "descuentoCliente";
    public static final String LABEL_CLIENT_DISCOUNT = "Descuento";

    public static final String FIELD_CLIENT_NOTES = "observacionesCliente";
    public static final String LABEL_CLIENT_NOTES = "Observaciones";
    public static final String LABEL_CITY = "Ciudad";
    public static final String CLIENT_EDIT = "Client Edit";
    public static final String CLIENT_DELETE = "Client Delete";


    // ========== Invoice Fields ==========
    public static final String FIELD_INVOICE_NUMBER = "numeroFacturaCliente";
    public static final String LABEL_INVOICE_NUMBER = "Número de Factura";

    public static final String FIELD_INVOICE_DATE = "fechaFacturaCliente";
    public static final String LABEL_INVOICE_DATE = "Fecha";

    public static final String FIELD_INVOICE_CLIENT_ID = "idClienteFactura";
    public static final String LABEL_INVOICE_CLIENT_ID = "ID Cliente";

    public static final String FIELD_INVOICE_WORKER_ID = "idTrabajadorFactura";
    public static final String LABEL_INVOICE_WORKER_ID = "ID Trabajador";

    public static final String FIELD_INVOICE_TAXABLE_AMOUNT = "baseImponibleFacturaCliente";
    public static final String LABEL_INVOICE_TAXABLE_AMOUNT = "Base Imponible";

    public static final String FIELD_INVOICE_VAT = "ivaFacturaCliente";
    public static final String LABEL_INVOICE_VAT = "IVA";

    public static final String FIELD_INVOICE_TOTAL = "totalFacturaCliente";
    public static final String LABEL_INVOICE_TOTAL = "Total";

    public static final String FIELD_INVOICE_HASH = "hashFacturaCliente";
    public static final String LABEL_INVOICE_HASH = "Hash";

    public static final String FIELD_INVOICE_QR = "qrFacturaCliente";
    public static final String LABEL_INVOICE_QR = "Código QR";

    public static final String FIELD_INVOICE_PAID = "cobradaFactura";
    public static final String LABEL_INVOICE_PAID = "Pagada";

    public static final String FIELD_INVOICE_PAYMENT_METHOD = "formaCobroFactura";
    public static final String LABEL_INVOICE_PAYMENT_METHOD = "Método de Pago";

    public static final String FIELD_INVOICE_PAYMENT_DATE = "fechaCobroFactura";
    public static final String LABEL_INVOICE_PAYMENT_DATE = "Fecha de Pago";

    public static final String FIELD_INVOICE_NOTES = "observacionesFacturaClientes";
    public static final String LABEL_INVOICE_NOTES = "Observaciones";
    public static final String INVOICE_VIEW = "Invoice View";
    public static final String INVOICE_EDIT = "Invoice Edit";
    public static final String INVOICE_DELETE = "Invoice Delete";

    // ========== Article Fields ==========
    public static final String FIELD_ARTICLE_CODE = "codigoArticulo";
    public static final String LABEL_ARTICLE_CODE = "Código";

    public static final String FIELD_ARTICLE_BARCODE = "codigoBarrasArticulo";
    public static final String LABEL_ARTICLE_BARCODE = "Código de Barras";


    public static final String FIELD_ARTICLE_DESCRIPTION = "descripcionArticulo";
    public static final String LABEL_ARTICLE_DESCRIPTION = "Descripción";

    public static final String FIELD_ARTICLE_PRICE = "pvpArticulo";
    public static final String LABEL_ARTICLE_PRICE = "Precio";

    public static final String FIELD_ARTICLE_FAMILY = "familiaArticulo";
    public static final String LABEL_ARTICLE_FAMILY = "Familia";

    public static final String FIELD_ARTICLE_COST = "costeArticulo";
    public static final String LABEL_ARTICLE_COST = "Coste";

    public static final String FIELD_ARTICLE_MARGIN = "margenComercialArticulo";
    public static final String LABEL_ARTICLE_MARGIN = "Margen Comercial";

    public static final String FIELD_ARTICLE_SUPPLIER = "proveedorArticulo";
    public static final String LABEL_ARTICLE_SUPPLIER = "Proveedor";

    public static final String FIELD_ARTICLE_STOCK = "stockArticulo";
    public static final String LABEL_ARTICLE_STOCK = "Stock";

    public static final String FIELD_ARTICLE_NOTES = "observacionesArticulo";
    public static final String LABEL_ARTICLE_NOTES = "Observaciones";
    public static final String ARTICLE_EDIT = "Article Edit";
    public static final String ARTICLE_DELETE = "Article Delete";

    // ========== Provider Fields ==========
    public static final String FIELD_PROVIDER_NAME = "nombreProveedor";
    public static final String LABEL_PROVIDER_NAME = "Nombre";

    public static final String FIELD_PROVIDER_ADDRESS = "direccionProveedor";
    public static final String LABEL_PROVIDER_ADDRESS = "Dirección";

    public static final String FIELD_PROVIDER_POSTCODE = "cpProveedor";
    public static final String LABEL_PROVIDER_POSTCODE = "Código Postal";

    public static final String FIELD_PROVIDER_CITY = "poblacionProveedor";
    public static final String LABEL_PROVIDER_CITY = "Población";

    public static final String FIELD_PROVIDER_PROVINCE = "provinciaProveedor";
    public static final String LABEL_PROVIDER_PROVINCE = "Provincia";

    public static final String FIELD_PROVIDER_COUNTRY = "paisProveedor";
    public static final String LABEL_PROVIDER_COUNTRY = "País";

    public static final String FIELD_PROVIDER_CIF = "cifProveedor";
    public static final String LABEL_PROVIDER_CIF = "CIF";

    public static final String FIELD_PROVIDER_PHONE = "telProveedor";
    public static final String LABEL_PROVIDER_PHONE = "Teléfono";

    public static final String FIELD_PROVIDER_EMAIL = "emailProveedor";
    public static final String LABEL_PROVIDER_EMAIL = "Email";

    public static final String FIELD_PROVIDER_WEB = "webProveedor";
    public static final String LABEL_PROVIDER_WEB = "Web";

    public static final String FIELD_PROVIDER_NOTES = "observacionesProveedor";
    public static final String LABEL_PROVIDER_NOTES = "Observaciones";
    public static final String PROVIDER_EDIT = "Provider Edit";
    public static final String PROVIDER_DELETE = "Provider Delete";

    // ========== Family Fields ==========
    public static final String FIELD_FAMILY_CODE = "codigoFamiliaArticulos";
    public static final String LABEL_FAMILY_CODE = "Código";
    public static final String FIELD_FAMILY_DESCRIPTION = "denominacionFamilias";
    public static final String LABEL_FAMILY_DESCRIPTION = "Descripción";
    public static final String FAMILY_EDIT = "Family Edit";
    public static final String FAMILY_DELETE = "Family Delete";

    // ========== IVA Type Fields ==========
    public static final String FIELD_IVA_AMOUNT = "iva";
    public static final String LABEL_IVA_AMOUNT = "Porcentaje";
    public static final String FIELD_IVA_DESCRIPTION = "observacionesTipoIva";
    public static final String LABEL_IVA_DESCRIPTION = "Descripción";

    public static final String IVA_EDIT = "IVA Edit";
    public static final String IVA_DELETE = "IVA Delete";

    // ========== Worker Fields ===========
    public static final String WORKER_EDIT = "Worker Edit";
    public static final String WORKER_DELETE = "Worker Delete";

    // ========== Error Messages ==========
    public static final String ERROR = "Error";
    public static final String ERROR_FORM_NOT_AVAILABLE = "Formulario no disponible para ";
    public static final String ERROR_LACK_DATA = "No hay datos disponibles para ";
    public static final String ERROR_FORM_SAVING = "Error al guardar ";

    // ========== Success Messages ==========
    public static final String SUCCESS = "Éxito";
    public static final String SUCCESS_CREATED = " creado exitosamente!";

    // ========== Buttons ==========
    public static final String BUTTON_CREATE = "Crear ";
    public static final String BUTTON_EDIT = "Editar";
    public static final String BUTTON_DELETE = "Eliminar";
    public static final String BUTTON_SAVE = "Guardar";
    public static final String LABEL_FILTER = "Filtrar:";
}