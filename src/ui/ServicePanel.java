
package ui;

import dao.ServiceDAO;
import model.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ServicePanel extends JPanel {

    private final ServiceDAO serviceDAO = new ServiceDAO();

    private final JTextField nameField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(3, 20);

    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{
                "Food", "Laundry", "Wellness",
                "Transport", "Housekeeping", "Other"
            });

    private final JTextField priceField = new JTextField(20);
    private final JCheckBox availableBox =
            new JCheckBox("Service is available", true);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                new String[]{
                    "ID", "Name", "Description",
                    "Category", "Price", "Available"
                }, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable serviceTable = new JTable(tableModel);

    public ServicePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createTitle(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);

        serviceTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        serviceTable.setRowHeight(25);
        serviceTable.getTableHeader().setReorderingAllowed(false);

        serviceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        loadServices();
    }

    private JPanel createTitle() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Hotel Services Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        panel.add(title, BorderLayout.WEST);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.add(createForm(), BorderLayout.NORTH);
        panel.add(new JScrollPane(serviceTable), BorderLayout.CENTER);
        panel.add(createButtons(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Service Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, "Service Name:", nameField);
        addField(form, gbc, 1, "Description:",
                new JScrollPane(descriptionArea));
        addField(form, gbc, 2, "Category:", categoryBox);
        addField(form, gbc, 3, "Price (₹):", priceField);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1;
        form.add(availableBox, gbc);

        return form;
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          int row, String label, Component input) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(input, gbc);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

        JButton addButton = new JButton("Add Service");
        JButton updateButton = new JButton("Update Service");
        JButton deactivateButton = new JButton("Deactivate");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addService());
        updateButton.addActionListener(e -> updateService());
        deactivateButton.addActionListener(e -> deactivateService());
        refreshButton.addActionListener(e -> loadServices());
        clearButton.addActionListener(e -> clearForm());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deactivateButton);
        panel.add(refreshButton);
        panel.add(clearButton);

        return panel;
    }

    private Service readForm() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String priceText = priceField.getText().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException(
                    "Service name cannot be empty."
            );
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Please select a category."
            );
        }

        if (priceText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter a price."
            );
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a valid price, such as 150 or 150.50."
            );
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative."
            );
        }

        if (price.scale() > 2) {
            throw new IllegalArgumentException(
                    "Price can have at most two decimal places."
            );
        }

        if (price.precision() - price.scale() > 8) {
            throw new IllegalArgumentException(
                    "Price is too large."
            );
        }

        return new Service(
                name,
                description,
                category,
                price,
                availableBox.isSelected()
        );
    }

    private void addService() {
        try {
            Service service = readForm();

            if (serviceDAO.addService(service)) {
                showMessage("Service added successfully.");
                clearForm();
                loadServices();
            }

        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateService() {
        int row = serviceTable.getSelectedRow();

        if (row == -1) {
            showMessage("Select a service from the table first.");
            return;
        }

        try {
            int serviceId = (int) tableModel.getValueAt(row, 0);

            Service formService = readForm();

            Service updatedService = new Service(
                    serviceId,
                    formService.getServiceName(),
                    formService.getDescription(),
                    formService.getCategory(),
                    formService.getPrice(),
                    formService.isAvailable()
            );

            if (serviceDAO.updateService(updatedService)) {
                showMessage("Service updated successfully.");
                clearForm();
                loadServices();
            } else {
                showMessage("Service not found. Refresh the table.");
            }

        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deactivateService() {
        int row = serviceTable.getSelectedRow();

        if (row == -1) {
            showMessage("Select a service to deactivate.");
            return;
        }

        int serviceId = (int) tableModel.getValueAt(row, 0);
        String serviceName = (String) tableModel.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Deactivate " + serviceName + "?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (serviceDAO.deactivateService(serviceId)) {
                showMessage("Service deactivated successfully.");
                clearForm();
                loadServices();
            } else {
                showMessage("Service not found. Refresh the table.");
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void loadServices() {
        try {
            tableModel.setRowCount(0);

            for (Service service : serviceDAO.getAllServices()) {
                tableModel.addRow(new Object[]{
                    service.getServiceId(),
                    service.getServiceName(),
                    service.getDescription(),
                    service.getCategory(),
                    service.getPrice(),
                    service.isAvailable() ? "Yes" : "No"
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = serviceTable.getSelectedRow();

        if (row == -1) {
            return;
        }

        nameField.setText(
                String.valueOf(tableModel.getValueAt(row, 1))
        );

        Object description = tableModel.getValueAt(row, 2);
        descriptionArea.setText(
                description == null ? "" : description.toString()
        );

        categoryBox.setSelectedItem(
                tableModel.getValueAt(row, 3)
        );

        priceField.setText(
                String.valueOf(tableModel.getValueAt(row, 4))
        );

        availableBox.setSelected(
                "Yes".equals(tableModel.getValueAt(row, 5))
        );
    }

    private void clearForm() {
        serviceTable.clearSelection();

        nameField.setText("");
        descriptionArea.setText("");
        categoryBox.setSelectedIndex(0);
        priceField.setText("");
        availableBox.setSelected(true);

        nameField.requestFocusInWindow();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Database operation failed:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
