package org.fbmoll.billing.resources;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Utils {
    static final String URL = "jdbc:mysql://localhost:3307/gestion";
    static final String USER = "root";
    static final String PASSWORD = "test123";

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading MySQL Driver: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error establishing database connection: " + e.getMessage());
        }
        return connection;
    }

    public static JScrollPane resizeTableColumns(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < table.getRowCount(); row++) {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                preferredWidth = Math.max(comp.getPreferredSize().width + 10, preferredWidth);

                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth(preferredWidth);
        }

        return new JScrollPane(table);
    }
}