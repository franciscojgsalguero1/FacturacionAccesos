package org.fbmoll.billing.crud;

import lombok.Getter;
import org.fbmoll.billing.classes.Item;
import org.fbmoll.billing.resources.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Getter
public class ViewItems {
    public static void showArticleTable(JPanel panel) throws SQLException {
        ArrayList<Item> articles = queryGetArticles();

        String[] columnNames = {
                "ID", "Código", "Código de Barras", "Descripción", "Familia", "Coste", "Margen Comercial", "PVP",
                "Proveedor", "Stock", "Observaciones"
        };

        Object[][] data = new Object[articles.size()][columnNames.length];
        for (int i = 0; i < articles.size(); i++) {
            Item article = articles.get(i);
            data[i] = new Object[]{
                    article.getId(),
                    article.getCode(),
                    article.getBarCode(),
                    article.getDescription(),
                    article.getFamilyId(),
                    article.getCost(),
                    article.getMargin(),
                    article.getPrice(),
                    article.getSupplier(),
                    article.getStock(),
                    article.getNotes()
            };
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        JTable table = new JTable(tableModel);
        JScrollPane pane = new JScrollPane(Utils.resizeTableColumns(table));
        JPanel filterPanel = createFilterPanel(columnNames, tableModel, table);

        // Event Dispatcher Thread
        SwingUtilities.invokeLater(() -> {
            panel.removeAll();
            panel.setLayout(new BorderLayout());
            panel.add(filterPanel, BorderLayout.NORTH);
            panel.add(pane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    private static ArrayList<Item> queryGetArticles() throws SQLException {
        ArrayList<Item> articles = new ArrayList<>();
        String query = "SELECT * FROM articulos";

        try (Connection conn = Utils.getConnection()) {
            try (PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int articleId = resultSet.getInt("idArticulo");
                    String articleCode = resultSet.getString("codigoArticulo");
                    String articleBarcode = resultSet.getString("codigoBarrasArticulo");
                    String articleDescription = resultSet.getString("descripcionArticulo");
                    int articleFamily = resultSet.getInt("familiaArticulo");
                    double articleCost = resultSet.getDouble("costeArticulo");
                    double articleMargin = resultSet.getDouble("margenComercialArticulo");
                    double articlePrice = resultSet.getDouble("pvpArticulo");
                    int articleSupplier = resultSet.getInt("proveedorArticulo");
                    int articleStock = resultSet.getInt("stockArticulo");
                    String articleNotes = resultSet.getString("observacionesArticulo");

                    articles.add(new Item(articleId, articleCode, articleBarcode, articleDescription,
                            articleFamily, articleCost, articleMargin, articlePrice, articleSupplier, articleStock,
                            articleNotes));
                }
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
            }
        }

        return articles;
    }

    private static JPanel createFilterPanel(String[] columnNames, DefaultTableModel tableModel, JTable table) {
        JTextField filterField = new JTextField(20);
        JComboBox<String> columnSelector = new JComboBox<>(getFilterableColumns(columnNames));
        JLabel filterLabel = new JLabel("Filter:");

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortable(0, false);
        table.setRowSorter(sorter);

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = filterField.getText();
                int columnIndex = columnSelector.getSelectedIndex() + 1;

                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, columnIndex));
                }

                table.repaint();
                table.revalidate();
            }
        });

        return styleFilterPanel(filterLabel, filterField, columnSelector);
    }

    private static JPanel styleFilterPanel(JLabel filterLabel, JTextField filterField, JComboBox<String> columnSelector) {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(filterLabel, gbc);

        gbc.gridx = 1;
        filterPanel.add(filterField, gbc);

        gbc.gridx = 2;
        filterPanel.add(columnSelector, gbc);

        return filterPanel;
    }

    private static String[] getFilterableColumns(String[] columnNames) {
        String[] filterableColumns = new String[columnNames.length - 1];
        System.arraycopy(columnNames, 1, filterableColumns, 0, columnNames.length - 1);
        return filterableColumns;
    }
}