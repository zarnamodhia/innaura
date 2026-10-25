package ui;

import dao.GuestDAO;
import model.Guest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/**
 * GuestPanel - UI for Managing Hotel Guests.
 * Matches the layout, color palette, and behavior of the Hotel Management System.
 */
public class GuestPanel extends JPanel {

    private final GuestDAO guestDAO;

    // Form input fields
    private final JTextField nameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField idProofField = new JTextField(20);
    private final JTextField addressField = new JTextField(20);

    // Table components
    private final DefaultTableModel tableModel;
    private final JTable guestTable;

    public GuestPanel() {
        this.guestDAO = new GuestDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(PremiumUI.CANVAS);

        // 1. Header (Title & Subtitle)
        add(PremiumUI.createHeader("Guest Management", "Register and manage guest profiles, contact records and ID proofs."), BorderLayout.NORTH);

        // 2. Center Content: Form + Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        centerPanel.add(createFormPanel(), BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"ID", "Full Name", "Phone", "Email", "ID Proof", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        guestTable = new JTable(tableModel);
        PremiumUI.styleTable(guestTable);
        guestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(guestTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PremiumUI.COLOR_BORDER),
                "Guest Records", 0, 0, PremiumUI.FONT_SECTION, PremiumUI.MUTED
        ));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);


        // Load existing guests from database on startup
        loadGuests();
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PremiumUI.COLOR_CARD_BG);
        form.setBorder(PremiumUI.createCardBorder("Guest Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Column 1 (Left): Full Name, Phone, Address
        addFormField(form, gbc, 0, 0, "Full Name:", nameField);
        addFormField(form, gbc, 0, 1, "Phone:", phoneField);
        addFormField(form, gbc, 0, 2, "Address:", addressField);

        // Column 2 (Right): Email, ID Proof
        addFormField(form, gbc, 2, 0, "Email:", emailField);
        addFormField(form, gbc, 2, 1, "ID Proof (Aadhaar/Passport):", idProofField);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 10, 4, 10);
        form.add(createButtonsPanel(), gbc);

        return form;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int col, int row, String label, Component input) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(PremiumUI.FONT_LABEL);
        lbl.setForeground(PremiumUI.INK);
        panel.add(lbl, gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 1.0;
        PremiumUI.styleInput((JComponent) input);
        panel.add(input, gbc);
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JButton addBtn = PremiumUI.primary("＋  Add guest");
        JButton updateBtn = PremiumUI.gold("↻  Save changes");
        JButton deleteBtn = PremiumUI.danger("Delete guest");
        JButton refreshBtn = PremiumUI.secondary("↻  Refresh");
        JButton clearBtn = PremiumUI.secondary("Clear form");

        addBtn.addActionListener(e -> addGuest());
        updateBtn.addActionListener(e -> updateGuest());
        deleteBtn.addActionListener(e -> deleteGuest());
        refreshBtn.addActionListener(e -> loadGuests());
        clearBtn.addActionListener(e -> clearForm());

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(refreshBtn);
        panel.add(clearBtn);

        return panel;
    }

    private Guest readForm() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String idProof = idProofField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            throw new IllegalArgumentException("Guest Full Name is required.");
        }
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        return new Guest(name, email, phone, idProof, address);
    } 
     if (!phone.matches("[+0-9() .\\-]{7,20}")) {
            throw new IllegalArgumentException("Enter a valid phone number (e.g. +91 9876543210).");
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address (e.g. guest@example.com).");
        }

    private void addGuest() {
        try {
            Guest guest = readForm();
            if (guestDAO.addGuest(guest)) {
                JOptionPane.showMessageDialog(this, "Guest added successfully! Generated ID: " + guest.getGuestId(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadGuests();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void updateGuest() {
        int row = guestTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a guest from the table to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int modelRow = guestTable.convertRowIndexToModel(row);
            int guestId = (int) tableModel.getValueAt(modelRow, 0);
            Guest formGuest = readForm();
            formGuest.setGuestId(guestId);

            if (guestDAO.updateGuest(formGuest)) {
                JOptionPane.showMessageDialog(this, "Guest details updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadGuests();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void deleteGuest() {
        int row = guestTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a guest from the table to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = guestTable.convertRowIndexToModel(row);
        int guestId = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete guest '" + name + "' (ID: " + guestId + ")?\nThis will also remove associated reservations.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (guestDAO.deleteGuest(guestId)) {
                JOptionPane.showMessageDialog(this, "Guest deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadGuests();
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    public void loadGuests() {
        try {
            tableModel.setRowCount(0);
            for (Guest g : guestDAO.getAllGuests()) {
                tableModel.addRow(new Object[]{
                    g.getGuestId(),
                    g.getFullName(),
                    g.getPhone(),
                    g.getEmail(),
                    g.getIdProof(),
                    g.getAddress()
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = guestTable.getSelectedRow();
        if (row == -1) return;

        int modelRow = guestTable.convertRowIndexToModel(row);
        nameField.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
        Object email = tableModel.getValueAt(modelRow, 3);
        emailField.setText(email == null ? "" : email.toString());
        Object idProof = tableModel.getValueAt(modelRow, 4);
        idProofField.setText(idProof == null ? "" : idProof.toString());
        Object address = tableModel.getValueAt(modelRow, 5);
        addressField.setText(address == null ? "" : address.toString());
    }

    private void clearForm() {
        guestTable.clearSelection();
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        idProofField.setText("");
        addressField.setText("");
        nameField.requestFocusInWindow();
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Database error:\n" + ex.getMessage() + "\n\nPlease ensure MySQL is running on port 3306.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
