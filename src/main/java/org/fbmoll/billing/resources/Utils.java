package org.fbmoll.billing.resources;

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

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Utils {
    static final Logger logger = LoggerFactory.getLogger(Utils.class);
    static String url = "";
    static String user = "";
    static String password = "";

    static {
        try (FileInputStream fis = new FileInputStream("dbconfig.properties")) {
            Properties properties = new Properties();
            properties.load(fis);

            url = properties.getProperty("db.url");
            user = properties.getProperty("db.user");
            password = properties.getProperty("db.password");
        } catch (IOException e) {
            logger.error("Error loading database configuration", e);
        }
    }

    // Private constructor to prevent instantiation
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            logger.error("Error loading MySQ+L Driver: {}", e.getMessage(), e);
        } catch (SQLException e) {
            logger.error("Error establishing database connection: {}", e.getMessage(), e);
        }
        return connection;
    }

    public static JScrollPane resizeTableColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader header = table.getTableHeader();
        TableColumnModel columnModel = table.getColumnModel();

        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = columnModel.getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, tableColumn.getHeaderValue(),
                    false, false, -1, column);
            preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 15);

            for (int row = 0; row < table.getRowCount(); row++) {
                Component cellComp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                preferredWidth = Math.max(preferredWidth, cellComp.getPreferredSize().width + 10);

                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }

        if (header != null) {
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);
        }

        return new JScrollPane(table);
    }
}