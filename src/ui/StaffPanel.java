
package ui;

import dao.StaffDAO;
import model.Staff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class StaffPanel extends JPanel {

    private final StaffDAO staffDAO = new StaffDAO();

    private final JTextField nameField = new JTextField(18);
    private final JComboBox<String> roleBox = new JComboBox<>(
            new String[]{"Manager", "Receptionist", "Housekeeping",
                    "Chef", "Waiter", "Security", "Maintenance", "Other"});

    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField salaryField = new JTextField(18);
    private final JTextField joiningDateField =
            new JTextField(LocalDate.now().toString(), 18);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Role", "Phone", "Email",
                    "Salary", "Joining Date", "Active"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable staffTable = new JTable(tableModel);

    public StaffPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Staff Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(createForm(), BorderLayout.NORTH);

        staffTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        staffTable.setRowHeight(24);

        JScrollPane tableScroll = new JScrollPane(staffTable);
        content.add(tableScroll, BorderLayout.CENTER);

        JPanel buttons = createButtons();
        content.add(buttons, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        staffTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        loadStaff();
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Staff Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(form, gbc, 0, 0, "Full Name:", nameField);
        addField(form, gbc, 0, 1, "Role:", roleBox);
        addField(form, gbc, 0, 2, "Phone:", phoneField);

        addField(form, gbc, 2, 0, "Email:", emailField);
        addField(form, gbc, 2, 1, "Salary:", salaryField);
        addField(form, gbc, 2, 2, "Joining Date:", joiningDateField);

        JLabel hint = new JLabel("Date format: YYYY-MM-DD");
        hint.setForeground(Color.GRAY);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        form.add(hint, gbc);

        return form;
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          int x, int y, String label, Component field) {
        gbc.gridwidth = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deactivateButton = new JButton("Deactivate");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear");

        addButton.addActionListener(e -> addStaff());
        updateButton.addActionListener(e -> updateStaff());
        deactivateButton.addActionListener(e -> deactivateStaff());
        refreshButton.addActionListener(e -> loadStaff());
        clearButton.addActionListener(e -> clearForm());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deactivateButton);
        panel.add(refreshButton);
        panel.add(clearButton);

        return panel;
    }

    private Staff readForm() {
        String name = nameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String salaryText = salaryField.getText().trim();
        String dateText = joiningDateField.getText().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Full name must be 100 characters or fewer.");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Please select a role.");
        }

        if (phone.length() > 20) {
            throw new IllegalArgumentException(
                    "Phone must be 20 characters or fewer.");
        }

        if (!phone.isEmpty() && !phone.matches("[+0-9() -]{7,20}")) {
            throw new IllegalArgumentException(
                    "Enter a valid phone number.");
        }

        if (email.length() > 100) {
            throw new IllegalArgumentException(
                    "Email must be 100 characters or fewer.");
        }

        if (!email.isEmpty()
                && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address.");
        }

        BigDecimal salary;

        try {
            salary = new BigDecimal(salaryText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a valid salary, such as 25000.00.");
        }

        if (salary.signum() < 0) {
            throw new IllegalArgumentException(
                    "Salary cannot be negative.");
        }

        if (salary.scale() > 2 || salary.precision() - salary.scale() > 8) {
            throw new IllegalArgumentException(
                    "Salary supports up to 8 integer digits and 2 decimals.");
        }

        LocalDate joiningDate;

        try {
            joiningDate = LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Enter a valid date in YYYY-MM-DD format.");
        }

        return new Staff(name, role, phone, email,
                salary, joiningDate, true);
    }

    private void addStaff() {
        try {
            Staff staff = readForm();

            if (staffDAO.addStaff(staff)) {
                showMessage("Staff member added successfully.");
                clearForm();
                loadStaff();
            } else {
                showMessage("No staff record was added.");
            }
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateStaff() {
        int selectedRow = staffTable.getSelectedRow();

        if (selectedRow == -1) {
            showMessage("Please select a staff member to update.");
            return;
        }

        try {
            int staffId = (int) tableModel.getValueAt(selectedRow, 0);
            Staff formStaff = readForm();

            Staff updatedStaff = new Staff(
                    staffId,
                    formStaff.getFullName(),
                    formStaff.getRole(),
                    formStaff.getPhone(),
                    formStaff.getEmail(),
                    formStaff.getSalary(),
                    formStaff.getJoiningDate(),
                    formStaff.isActive()
            );

            if (staffDAO.updateStaff(updatedStaff)) {
                showMessage("Staff member updated successfully.");
                clearForm();
                loadStaff();
            } else {
                showMessage("No staff record was updated.");
            }
        } catch (IllegalArgumentException ex) {
            showMessage(ex.getMessage());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deactivateStaff() {
        int selectedRow = staffTable.getSelectedRow();

        if (selectedRow == -1) {
            showMessage("Please select a staff member to deactivate.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deactivate this staff member?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int staffId = (int) tableModel.getValueAt(selectedRow, 0);

            if (staffDAO.deactivateStaff(staffId)) {
                showMessage("Staff member deactivated.");
                clearForm();
                loadStaff();
            } else {
                showMessage("No staff record was deactivated.");
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void loadStaff() {
        try {
            tableModel.setRowCount(0);

            List<Staff> staffList = staffDAO.getAllStaff();

            for (Staff staff : staffList) {
                tableModel.addRow(new Object[]{
                        staff.getStaffId(),
                        staff.getFullName(),
                        staff.getRole(),
                        staff.getPhone(),
                        staff.getEmail(),
                        staff.getSalary(),
                        staff.getJoiningDate(),
                        staff.isActive()
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = staffTable.getSelectedRow();

        if (row == -1) {
            return;
        }

        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        roleBox.setSelectedItem(tableModel.getValueAt(row, 2));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        emailField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        salaryField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        joiningDateField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
    }

    private void clearForm() {
        staffTable.clearSelection();
        nameField.setText("");
        roleBox.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        salaryField.setText("");
        joiningDateField.setText(LocalDate.now().toString());
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                "Database operation failed. Check the VS Code terminal for details.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
