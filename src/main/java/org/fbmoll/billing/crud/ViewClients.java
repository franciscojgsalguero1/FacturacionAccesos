    package org.fbmoll.billing.crud;

    import lombok.Getter;
    import org.fbmoll.billing.classes.Client;
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
    public class ViewClients {
        public static void visualizeClients(JPanel panel) {
            ArrayList<Client> clients = queryGetClients();

            String[] columnNames = {
                    "ID", "Nombre", "Dirección", "Código Postal", "Población", "Provincia", "País",
                    "CIF", "Teléfono", "Email", "IBAN", "Riesgo", "Descuento", "Observaciones"
            };

            Object[][] data = new Object[clients.size()][columnNames.length];
            for (int i = 0; i < clients.size(); i++) {
                Client client = clients.get(i);
                data[i] = new Object[]{
                        client.getId(),
                        client.getName(),
                        client.getAddress(),
                        client.getPostCode(),
                        client.getTown(),
                        client.getProvince(),
                        client.getCountry(),
                        client.getCif(),
                        client.getNumber(),
                        client.getEmail(),
                        client.getIban(),
                        client.getRisk(),
                        client.getDiscount(),
                        client.getDescription()
                };
            }

            DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
            JTable table = new JTable(tableModel);
            JScrollPane tablePane = new JScrollPane(Utils.resizeTableColumns(table));
            JPanel filterPanel = createFilterPanel(panel, columnNames, tableModel, table);

            // Event Dispatcher Thread
            SwingUtilities.invokeLater(() -> {
                panel.removeAll();
                panel.setLayout(new BorderLayout());
                panel.add(filterPanel, BorderLayout.NORTH);
                panel.add(tablePane, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            });
        }

        private static ArrayList<Client> queryGetClients() {
            ArrayList<Client> clients = new ArrayList<>();
            String query = "SELECT * FROM clientes";

            try (Connection conn = Utils.getConnection();
                 PreparedStatement statement = conn.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int clientId = resultSet.getInt("idCliente");
                    String clientName = resultSet.getString("nombreCliente");
                    String clientAddress = resultSet.getString("direccionCliente");
                    int clientPostCode = resultSet.getInt("cpCliente");
                    String clientTown = resultSet.getString("poblacionCliente");
                    String clientProvince = resultSet.getString("provinciaCliente");
                    String clientCountry = resultSet.getString("paisCliente");
                    String clientCif = resultSet.getString("cifCliente");
                    String clientNumber = resultSet.getString("telCliente");
                    String clientEmail = resultSet.getString("emailCliente");
                    String clientIban = resultSet.getString("ibanCliente");
                    double clientRisk = resultSet.getDouble("riesgoCliente");
                    double clientDiscount = resultSet.getDouble("descuentoCliente");
                    String clientObservation = resultSet.getString("observacionesCliente");

                    clients.add(new Client(clientId, clientName, clientAddress, clientPostCode, clientTown,
                            clientProvince, clientCountry, clientCif, clientNumber, clientEmail, clientIban,
                            clientRisk, clientDiscount, clientObservation));
                }
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
            }

            return clients;
        }

        private static JPanel createFilterPanel(JPanel panel, String[] columns, DefaultTableModel model, JTable table) {
            JTextField filterField = new JTextField(20);
            JComboBox<String> columnSelector = new JComboBox<>(getFilterableColumns(columns));
            JLabel filterLabel = new JLabel("Filter:");

            JButton createClientButton = new JButton("Crear Cliente");
            createClientButton.addActionListener(e -> {
                // Trigger the client creation process
                new CreateClient().createNewClient(panel);
            });

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
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

            return styleFilterPanel(createClientButton, filterLabel, filterField, columnSelector);
        }

        private static JPanel styleFilterPanel(JButton createClientButton, JLabel filterLabel,
                                               JTextField filterField, JComboBox<String> columnSelector) {
            JPanel filterPanel = new JPanel(new GridBagLayout());
            filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            filterPanel.add(createClientButton, gbc);

            gbc.gridx = 1;
            filterPanel.add(filterLabel, gbc);

            gbc.gridx = 2;
            filterPanel.add(filterField, gbc);

            gbc.gridx = 3;
            filterPanel.add(columnSelector, gbc);

            return filterPanel;
        }

        private static String[] getFilterableColumns(String[] columnNames) {
            String[] filterableColumns = new String[columnNames.length - 1];
            System.arraycopy(columnNames, 1, filterableColumns, 0, columnNames.length - 1);
            return filterableColumns;
        }
    }