package org.facturacion.resources;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase de utilidad con métodos comunes para la aplicación.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Utils {
    static final Logger logger = LoggerFactory.getLogger(Utils.class);
    static String url;
    static String user;
    static String password;

    static {
        loadDatabaseConfig();
    }

    /**
     * Constructor privado para evitar la instanciación.
     */
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Carga la configuración de la base de datos desde un archivo de propiedades.
     */
    private static void loadDatabaseConfig() {
        try (FileInputStream fis = new FileInputStream("dbconfig.properties")) {
            Properties properties = new Properties();
            properties.load(fis);

            url = properties.getProperty("db.url", "");
            user = properties.getProperty("db.user", "");
            password = properties.getProperty("db.password", "");
        } catch (IOException e) {
            logger.error("Error cargando la configuración de la base de datos", e);
        }
    }

    /**
     * Obtiene una conexión a la base de datos.
     *
     * @return Conexión a la base de datos o null si no es posible establecerla.
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            logger.error("Error al cargar el driver de MySQL: {}", e.getMessage(), e);
        } catch (SQLException e) {
            logger.error("Error al establecer conexión con la base de datos: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Ajusta automáticamente el tamaño de las columnas de una tabla en función del contenido.
     *
     * @param table JTable a ajustar.
     * @return JScrollPane que contiene la tabla ajustada.
     */
    public static JScrollPane resizeTableColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = table.getTableHeader();
        TableColumnModel columnModel = table.getColumnModel();

        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = columnModel.getColumn(column);
            int preferredWidth = calculatePreferredWidth(table, tableColumn, column);
            tableColumn.setPreferredWidth(preferredWidth);
        }

        if (header != null) {
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);
        }

        return new JScrollPane(table);
    }

    /**
     * Calcula el ancho óptimo de una columna de tabla en función del contenido.
     *
     * @param table JTable de referencia.
     * @param tableColumn Columna a analizar.
     * @param column Índice de la columna.
     * @return Ancho óptimo calculado.
     */
    private static int calculatePreferredWidth(JTable table, TableColumn tableColumn, int column) {
        int preferredWidth = tableColumn.getMinWidth();
        int maxWidth = tableColumn.getMaxWidth();

        // Determinar el ancho basado en el encabezado
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        Component headerComp = headerRenderer.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, -1, column);
        preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 15);

        // Determinar el ancho basado en el contenido
        for (int row = 0; row < table.getRowCount(); row++) {
            Component cellComp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
            preferredWidth = Math.max(preferredWidth, cellComp.getPreferredSize().width + 10);
            if (preferredWidth >= maxWidth) {
                return maxWidth;
            }
        }
        return preferredWidth;
    }
}