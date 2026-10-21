package ui;

import dao.GuestDAO;
import dao.ReservationDAO;
import model.Guest;
import model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * ReservationPanel - UI for Booking and Managing Room Reservations.
 * Allows booking rooms, checking in guests, and status transitions.
 */
public class ReservationPanel extends JPanel {

    private final ReservationDAO reservationDAO;
    private final GuestDAO guestDAO;

    // Form inputs
    private final JComboBox<GuestComboItem> guestComboBox = new JComboBox<>();
    private final JComboBox<String> roomBox = new JComboBox<>(new String[]{
        "101 - Deluxe Single (₹1500)",
        "102 - Deluxe Double (₹2500)",
        "201 - Executive Suite (₹4500)",
        "202 - Presidential Suite (₹8000)",
        "301 - Standard Single (₹1200)"
    });
    private final JTextField checkInField = new JTextField(12);
    private final JTextField checkOutField = new JTextField(12);
    private final JTextField rateField = new JTextField(10);
    private final JComboBox<String> statusBox = new JComboBox<>(new String[]{
        "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"
    });

    // Table components
    private final DefaultTableModel tableModel;
    private final JTable resTable;

    // Helper wrapper for Guest dropdown
    private static class GuestComboItem {
        final int id;
        final String name;

        GuestComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }

    public ReservationPanel() {
        this.reservationDAO = new ReservationDAO();
        this.guestDAO = new GuestDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(PremiumUI.CANVAS);

        // 1. Header
        add(PremiumUI.createHeader("Room Reservations", "Book rooms, manage check-ins, and track reservation schedules."), BorderLayout.NORTH);

        // 2. Center Content: Form + Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        centerPanel.add(createFormPanel(), BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"Res #", "Guest Name", "Room", "Check-In", "Check-Out", "Rate (₹)", "Nights", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resTable = new JTable(tableModel);
        PremiumUI.styleTable(resTable);
        resTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(resTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PremiumUI.COLOR_BORDER),
                "Reservation Records", 0, 0, PremiumUI.FONT_SECTION, PremiumUI.MUTED
        ));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);


        // Default initial dates
        LocalDate today = LocalDate.now();
        checkInField.setText(today.toString());
        checkOutField.setText(today.plusDays(2).toString());
        rateField.setText("1500.00");

        // Auto update rate when room changes
        roomBox.addActionListener(e -> updateRateFromRoomBox());

        // Initial Data Load
        reloadGuestsList();
        loadReservations();
    }

    private void updateRateFromRoomBox() {
        String selected = (String) roomBox.getSelectedItem();
        if (selected != null && selected.contains("₹")) {
            String price = selected.substring(selected.indexOf('₹') + 1).replace(")", "").trim();
            rateField.setText(price + ".00");
        }
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PremiumUI.COLOR_CARD_BG);
        form.setBorder(PremiumUI.createCardBorder("Reservation Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Column 1 (Left): Guest, Check-in Date, Rate
        addFormField(form, gbc, 0, 0, "Select Guest:", guestComboBox);
        addFormField(form, gbc, 0, 1, "Check-In Date (YYYY-MM-DD):", checkInField);
        addFormField(form, gbc, 0, 2, "Room Rate (₹/Night):", rateField);

        // Column 2 (Right): Room, Check-out Date, Status
        addFormField(form, gbc, 2, 0, "Select Room:", roomBox);
        addFormField(form, gbc, 2, 1, "Check-Out Date (YYYY-MM-DD):", checkOutField);
        addFormField(form, gbc, 2, 2, "Booking Status:", statusBox);

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

        JButton bookBtn = PremiumUI.primary("＋  Book room");
        JButton checkInBtn = PremiumUI.gold("Check-in guest");
        JButton cancelBtn = PremiumUI.danger("Cancel booking");
        JButton refreshBtn = PremiumUI.secondary("↻  Refresh");
        JButton clearBtn = PremiumUI.secondary("Clear form");

        bookBtn.addActionListener(e -> bookRoom());
        checkInBtn.addActionListener(e -> markCheckIn());
        cancelBtn.addActionListener(e -> cancelBooking());
        refreshBtn.addActionListener(e -> {
            reloadGuestsList();
            loadReservations();
        });
        clearBtn.addActionListener(e -> clearForm());

        panel.add(bookBtn);
        panel.add(checkInBtn);
        panel.add(cancelBtn);
        panel.add(refreshBtn);
        panel.add(clearBtn);

        return panel;
    }

    public void reloadGuestsList() {
        try {
            guestComboBox.removeAllItems();
            List<Guest> guests = guestDAO.getAllGuests();
            for (Guest g : guests) {
                guestComboBox.addItem(new GuestComboItem(g.getGuestId(), g.getFullName()));
            }
        } catch (SQLException e) {
            // silent or handled on table load
        }
    }

    private Reservation readForm() {
        GuestComboItem selectedGuest = (GuestComboItem) guestComboBox.getSelectedItem();
        if (selectedGuest == null) {
            throw new IllegalArgumentException("No guest selected. Please register a guest first in the Guests tab!");
        }

        String rawRoom = (String) roomBox.getSelectedItem();
        String roomNumber = rawRoom != null ? rawRoom.split(" ")[0].trim() : "101";

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(checkInField.getText().trim());
            checkOut = LocalDate.parse(checkOutField.getText().trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD (e.g. 2026-10-25)");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-Out date must be after Check-In date.");
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateField.getText().trim());
            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Rate must be greater than zero.");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid room rate number (e.g. 1500.00)");
        }

        String status = (String) statusBox.getSelectedItem();
        return new Reservation(selectedGuest.id, roomNumber, checkIn, checkOut, rate, status != null ? status : "CONFIRMED");
    }

    private void bookRoom() {
        try {
            Reservation res = readForm();
            if (reservationDAO.addReservation(res)) {
                JOptionPane.showMessageDialog(this, "Room booked successfully!\nReservation ID: " + res.getReservationId(),
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadReservations();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void markCheckIn() {
        int row = resTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = resTable.convertRowIndexToModel(row);
        int resId = (int) tableModel.getValueAt(modelRow, 0);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 7);

        if ("CHECKED_IN".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Guest is already checked in!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            if (reservationDAO.updateStatus(resId, "CHECKED_IN")) {
                JOptionPane.showMessageDialog(this, "Guest checked in successfully!\nStatus updated to CHECKED_IN.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadReservations();
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void cancelBooking() {
        int row = resTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = resTable.convertRowIndexToModel(row);
        int resId = (int) tableModel.getValueAt(modelRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel Reservation #" + resId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (reservationDAO.updateStatus(resId, "CANCELLED")) {
                    JOptionPane.showMessageDialog(this, "Reservation #" + resId + " has been cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    loadReservations();
                }
            } catch (SQLException ex) {
                showDatabaseError(ex);
            }
        }
    }

    public void loadReservations() {
        try {
            tableModel.setRowCount(0);
            for (Reservation r : reservationDAO.getAllReservations()) {
                tableModel.addRow(new Object[]{
                    r.getReservationId(),
                    r.getGuestName(),
                    r.getRoomNumber(),
                    r.getCheckInDate(),
                    r.getCheckOutDate(),
                    r.getRoomRate(),
                    r.getNumberOfNights(),
                    r.getStatus()
                });
            }
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = resTable.getSelectedRow();
        if (row == -1) return;

        int modelRow = resTable.convertRowIndexToModel(row);
        String room = String.valueOf(tableModel.getValueAt(modelRow, 2));
        for (int i = 0; i < roomBox.getItemCount(); i++) {
            if (roomBox.getItemAt(i).startsWith(room)) {
                roomBox.setSelectedIndex(i);
                break;
            }
        }

        checkInField.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
        checkOutField.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
        rateField.setText(String.valueOf(tableModel.getValueAt(modelRow, 5)));
        statusBox.setSelectedItem(tableModel.getValueAt(modelRow, 7));
    }

    private void clearForm() {
        resTable.clearSelection();
        LocalDate today = LocalDate.now();
        checkInField.setText(today.toString());
        checkOutField.setText(today.plusDays(2).toString());
        updateRateFromRoomBox();
        statusBox.setSelectedIndex(0);
    }

    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
