package ui;

import dao.BillingDAO;
import dao.ReservationDAO;
import model.Billing;
import model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * InnAura - Billing & Payments
 *
 * Features:
 * - Guest checkout
 * - GST/tax calculation
 * - Service charges
 * - Discounts
 * - Payment mode/status
 * - Billing history
 * - Receipt generation
 *
 * Layout:
 * The entire page is vertically scrollable.
 * This prevents the checkout section or billing history
 * from disappearing on smaller screens.
 */
public class BillingPanel extends JPanel {

    private final BillingDAO billingDAO = new BillingDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    // =========================================================
    // FORM CONTROLS
    // =========================================================

    private final JComboBox<ResComboItem> resComboBox =
            new JComboBox<>();

    private final JTextField roomChargesField =
            new JTextField(18);

    private final JTextField serviceChargesField =
            new JTextField("0.00", 18);

    private final JTextField discountField =
            new JTextField("0.00", 18);

    private final JComboBox<String> taxBox =
            new JComboBox<>(new String[]{
                    "12% GST",
                    "18% GST",
                    "5% GST",
                    "0% None"
            });

    private final JTextField totalField =
            new JTextField("0.00", 18);

    private final JComboBox<String> paymentModeBox =
            new JComboBox<>(new String[]{
                    "CASH",
                    "CREDIT_CARD",
                    "DEBIT_CARD",
                    "UPI",
                    "NET_BANKING"
            });

    private final JComboBox<String> paymentStatusBox =
            new JComboBox<>(new String[]{
                    "PAID",
                    "PENDING"
            });

    // =========================================================
    // SUMMARY VALUES
    // =========================================================

    private final JLabel totalValue =
            new JLabel("—");

    private final JLabel paidValue =
            new JLabel("—");

    private final JLabel pendingValue =
            new JLabel("—");

    private final JLabel statusLine =
            new JLabel("Ready");

    // =========================================================
    // BILLING TABLE
    // =========================================================

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Bill #",
                            "Res #",
                            "Guest name",
                            "Room",
                            "Room (₹)",
                            "Services (₹)",
                            "Discount",
                            "Tax (₹)",
                            "Total (₹)",
                            "Mode",
                            "Status",
                            "Date"
                    },
                    0
            ) {

                @Override
                public boolean isCellEditable(
                        int row,
                        int column
                ) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(
                        int column
                ) {
                    if (column == 0 || column == 1) {
                        return Integer.class;
                    }

                    if (column >= 4 && column <= 8) {
                        return BigDecimal.class;
                    }

                    return String.class;
                }
            };

    private final JTable billTable =
            new JTable(tableModel);

    // =========================================================
    // RESERVATION COMBO ITEM
    // =========================================================

    private static class ResComboItem {

        final int id;
        final String label;
        final BigDecimal calculatedRoomCharges;
        final String guestName;
        final String roomNumber;
        final long nights;

        ResComboItem(Reservation r) {

            id = r.getReservationId();

            nights = r.getNumberOfNights();

            calculatedRoomCharges =
                    r.calculateRoomTotal();

            guestName =
                    r.getGuestName();

            roomNumber =
                    r.getRoomNumber();

            label =
                    "Booking #" +
                    id +
                    " - " +
                    guestName +
                    " (Room " +
                    roomNumber +
                    " | " +
                    nights +
                    " nights)";
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BillingPanel() {

        setLayout(new BorderLayout());

        setBackground(
                PremiumUI.CANVAS
        );

        setBorder(
                new EmptyBorder(
                        24,
                        28,
                        24,
                        28
                )
        );

        buildScreen();

        // Reservation selection
        resComboBox.addActionListener(
                e -> onReservationSelected()
        );

        // Recalculate when values change
        serviceChargesField.addActionListener(
                e -> calculateTotal()
        );

        discountField.addActionListener(
                e -> calculateTotal()
        );

        taxBox.addActionListener(
                e -> calculateTotal()
        );

        // Load data
        reloadActiveReservations();
        loadBills();
    }

    // =========================================================
    // MAIN SCREEN
    // =========================================================

    private void buildScreen() {

        /*
         * IMPORTANT:
         *
         * There is intentionally NO JSplitPane here.
         *
         * The old JSplitPane was squeezing the checkout
         * and history sections depending on the window size.
         *
         * The whole page is now inside one vertical scrollbar.
         */

        JPanel page = new JPanel();

        page.setOpaque(false);

        page.setLayout(
                new BoxLayout(
                        page,
                        BoxLayout.Y_AXIS
                )
        );

        // =====================================================
        // PAGE HEADER
        // =====================================================

        JPanel titleLine =
                new JPanel(
                        new BorderLayout()
                );

        titleLine.setOpaque(false);

        titleLine.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        titleLine.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        72
                )
        );

        JPanel titleCopy =
                new JPanel();

        titleCopy.setOpaque(false);

        titleCopy.setLayout(
                new BoxLayout(
                        titleCopy,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title =
                new JLabel(
                        "Billing & payments"
                );

        title.setFont(
                PremiumUI.DISPLAY
        );

        title.setForeground(
                PremiumUI.INK
        );

        JLabel subtitle =
                PremiumUI.muted(
                        "Prepare guest invoices, apply charges and complete checkout."
                );

        subtitle.setBorder(
                new EmptyBorder(
                        5,
                        1,
                        0,
                        0
                )
        );

        titleCopy.add(title);
        titleCopy.add(subtitle);

        JButton refresh =
                PremiumUI.secondary(
                        "↻  Refresh records"
                );

        refresh.setPreferredSize(
                new Dimension(
                        160,
                        44
                )
        );

        refresh.addActionListener(
                e -> {

                    reloadActiveReservations();

                    loadBills();
                }
        );

        titleLine.add(
                titleCopy,
                BorderLayout.WEST
        );

        titleLine.add(
                refresh,
                BorderLayout.EAST
        );

        page.add(titleLine);

        page.add(
                Box.createVerticalStrut(18)
        );

        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        JPanel summary =
                summaryStrip();

        summary.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        summary.setPreferredSize(
                new Dimension(
                        1000,
                        140
                )
        );

        summary.setMinimumSize(
                new Dimension(
                        700,
                        140
                )
        );

        summary.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        140
                )
        );

        page.add(summary);

        page.add(
                Box.createVerticalStrut(18)
        );

        // =====================================================
        // CHECKOUT SECTION
        // =====================================================

        JPanel form =
                formCard();

        form.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        form.setPreferredSize(
                new Dimension(
                        1000,
                        430
                )
        );

        form.setMinimumSize(
                new Dimension(
                        700,
                        430
                )
        );

        form.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        430
                )
        );

        page.add(form);

        page.add(
                Box.createVerticalStrut(18)
        );

        // =====================================================
        // BILLING HISTORY
        // =====================================================

        JPanel history =
                recordsCard();

        history.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        history.setPreferredSize(
                new Dimension(
                        1000,
                        430
                )
        );

        history.setMinimumSize(
                new Dimension(
                        700,
                        430
                )
        );

        history.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        430
                )
        );

        page.add(history);

        page.add(
                Box.createVerticalStrut(24)
        );

        // =====================================================
        // PAGE SCROLL
        // =====================================================

        JScrollPane pageScroll =
                new JScrollPane(page);

        pageScroll.setBorder(null);

        pageScroll.setOpaque(false);

        pageScroll
                .getViewport()
                .setOpaque(false);

        pageScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants
                        .HORIZONTAL_SCROLLBAR_NEVER
        );

        pageScroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants
                        .VERTICAL_SCROLLBAR_AS_NEEDED
        );

        pageScroll
                .getVerticalScrollBar()
                .setUnitIncrement(18);

        add(
                pageScroll,
                BorderLayout.CENTER
        );
    }

    // =========================================================
    // SUMMARY STRIP
    // =========================================================

    private JPanel summaryStrip() {

        JPanel strip =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                13,
                                0
                        )
                );

        strip.setOpaque(false);

        strip.add(
                summaryCard(
                        "TOTAL BILLS",
                        totalValue,
                        "Invoices recorded",
                        PremiumUI.FOREST,
                        "01"
                )
        );

        strip.add(
                summaryCard(
                        "PAID",
                        paidValue,
                        "Completed payments",
                        new Color(
                                72,
                                132,
                                91
                        ),
                        "02"
                )
        );

        strip.add(
                summaryCard(
                        "PENDING",
                        pendingValue,
                        "Payments awaiting settlement",
                        PremiumUI.GOLD,
                        "03"
                )
        );

        return strip;
    }

    // =========================================================
    // SUMMARY CARD
    // =========================================================

    private JPanel summaryCard(
            String label,
            JLabel value,
            String detail,
            Color accent,
            String number
    ) {

        JPanel card =
                PremiumUI.card(
                        new BorderLayout(
                                0,
                                8
                        )
                );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new PremiumUI.SoftLineBorder(
                                PremiumUI.LINE,
                                1,
                                15
                        ),
                        new EmptyBorder(
                                15,
                                18,
                                15,
                                18
                        )
                )
        );

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        top.setOpaque(false);

        JLabel name =
                new JLabel(label);

        name.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        name.setForeground(
                PremiumUI.MUTED
        );

        JLabel marker =
                new JLabel(number);

        marker.setFont(
                new Font(
                        "Georgia",
                        Font.PLAIN,
                        16
                )
        );

        marker.setForeground(
                accent
        );

        top.add(
                name,
                BorderLayout.WEST
        );

        top.add(
                marker,
                BorderLayout.EAST
        );

        value.setFont(
                new Font(
                        "Georgia",
                        Font.PLAIN,
                        29
                )
        );

        value.setForeground(
                PremiumUI.INK
        );

        JLabel caption =
                PremiumUI.muted(detail);

        caption.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        card.add(
                top,
                BorderLayout.NORTH
        );

        card.add(
                value,
                BorderLayout.CENTER
        );

        card.add(
                caption,
                BorderLayout.SOUTH
        );

        return card;
    }

    // =========================================================
    // CHECKOUT CARD
    // =========================================================

    private JPanel formCard() {

        JPanel card =
                PremiumUI.card(
                        new BorderLayout(
                                0,
                                16
                        )
                );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new PremiumUI.SoftLineBorder(
                                PremiumUI.LINE,
                                1,
                                15
                        ),
                        new EmptyBorder(
                                20,
                                24,
                                20,
                                24
                        )
                )
        );

        // =====================================================
        // CHECKOUT HEADER
        // =====================================================

        JPanel header =
                new JPanel(
                        new BorderLayout()
                );

        header.setOpaque(false);

        JPanel copy =
                new JPanel();

        copy.setOpaque(false);

        copy.setLayout(
                new BoxLayout(
                        copy,
                        BoxLayout.Y_AXIS
                )
        );

        copy.add(
                PremiumUI.title(
                        "Checkout & invoice"
                )
        );

        JLabel note =
                PremiumUI.muted(
                        "Select a reservation, confirm charges and generate the guest bill."
                );

        note.setBorder(
                new EmptyBorder(
                        4,
                        0,
                        0,
                        0
                )
        );

        copy.add(note);

        JLabel hint =
                new JLabel(
                        "AMOUNTS  ·  INR ₹"
                );

        hint.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        hint.setForeground(
                PremiumUI.GOLD
        );

        header.add(
                copy,
                BorderLayout.WEST
        );

        header.add(
                hint,
                BorderLayout.EAST
        );

        card.add(
                header,
                BorderLayout.NORTH
        );

        // =====================================================
        // FORM FIELDS
        // =====================================================

        JPanel fields =
                new JPanel(
                        new GridBagLayout()
                );

        fields.setOpaque(false);

        GridBagConstraints g =
                new GridBagConstraints();

        g.fill =
                GridBagConstraints.HORIZONTAL;

        g.weightx = 1.0;

        g.weighty = 0.0;

        // Row 1
        addField(
                fields,
                g,
                0,
                0,
                "SELECT BOOKING",
                resComboBox
        );

        addField(
                fields,
                g,
                1,
                0,
                "TAX / GST RATE",
                taxBox
        );

        // Row 2
        addField(
                fields,
                g,
                0,
                1,
                "ROOM CHARGES (₹)",
                roomChargesField
        );

        addField(
                fields,
                g,
                1,
                1,
                "TOTAL AMOUNT (₹)",
                totalField
        );

        // Row 3
        addField(
                fields,
                g,
                0,
                2,
                "SERVICE CHARGES (₹)",
                serviceChargesField
        );

        addField(
                fields,
                g,
                1,
                2,
                "PAYMENT MODE",
                paymentModeBox
        );

        // Row 4
        addField(
                fields,
                g,
                0,
                3,
                "DISCOUNT (₹)",
                discountField
        );

        addField(
                fields,
                g,
                1,
                3,
                "PAYMENT STATUS",
                paymentStatusBox
        );

        card.add(
                fields,
                BorderLayout.CENTER
        );

        // =====================================================
        // ACTION BUTTONS
        // =====================================================

        JPanel actions =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                0
                        )
                );

        actions.setOpaque(false);

        JButton calculate =
                PremiumUI.secondary(
                        "Calculate total"
                );

        JButton checkout =
                PremiumUI.primary(
                        "Generate bill & checkout"
                );

        JButton receipt =
                PremiumUI.gold(
                        "View receipt"
                );

        JButton clear =
                PremiumUI.secondary(
                        "Clear form"
                );

        // Explicit button sizes prevent text clipping.
        calculate.setPreferredSize(
                new Dimension(
                        145,
                        44
                )
        );

        checkout.setPreferredSize(
                new Dimension(
                        205,
                        44
                )
        );

        receipt.setPreferredSize(
                new Dimension(
                        140,
                        44
                )
        );

        clear.setPreferredSize(
                new Dimension(
                        125,
                        44
                )
        );

        calculate.addActionListener(
                e -> calculateTotal()
        );

        checkout.addActionListener(
                e -> generateBill()
        );

        receipt.addActionListener(
                e -> showSelectedReceipt()
        );

        clear.addActionListener(
                e -> clearForm()
        );

        actions.add(calculate);
        actions.add(checkout);
        actions.add(receipt);
        actions.add(clear);

        card.add(
                actions,
                BorderLayout.SOUTH
        );

        return card;
    }

    // =========================================================
    // FIELD
    // =========================================================

    private void addField(
            JPanel panel,
            GridBagConstraints g,
            int col,
            int row,
            String label,
            JComponent input
    ) {

        JPanel wrap =
                new JPanel(
                        new BorderLayout(
                                0,
                                7
                        )
                );

        wrap.setOpaque(false);

        JLabel fieldName =
                PremiumUI.fieldLabel(label);

        wrap.add(
                fieldName,
                BorderLayout.NORTH
        );

        PremiumUI.styleInput(input);

        input.setPreferredSize(
                new Dimension(
                        300,
                        38
                )
        );

        input.setMinimumSize(
                new Dimension(
                        120,
                        38
                )
        );

        wrap.add(
                input,
                BorderLayout.CENTER
        );

        g.gridx = col;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 1.0;

        g.insets =
                new Insets(
                        6,
                        col == 0 ? 0 : 10,
                        8,
                        col == 0 ? 10 : 0
                );

        panel.add(
                wrap,
                g
        );
    }

    // =========================================================
    // BILLING HISTORY
    // =========================================================

    private JPanel recordsCard() {

        JPanel card =
                PremiumUI.card(
                        new BorderLayout(
                                0,
                                13
                        )
                );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new PremiumUI.SoftLineBorder(
                                PremiumUI.LINE,
                                1,
                                15
                        ),
                        new EmptyBorder(
                                20,
                                24,
                                20,
                                24
                        )
                )
        );

        // =====================================================
        // HISTORY HEADER
        // =====================================================

        JPanel header =
                new JPanel(
                        new BorderLayout(
                                12,
                                0
                        )
                );

        header.setOpaque(false);

        JPanel titles =
                new JPanel();

        titles.setOpaque(false);

        titles.setLayout(
                new BoxLayout(
                        titles,
                        BoxLayout.Y_AXIS
                )
        );

        titles.add(
                PremiumUI.title(
                        "Billing history"
                )
        );

        JLabel hint =
                PremiumUI.muted(
                        "Select an invoice below to view its receipt."
                );

        hint.setBorder(
                new EmptyBorder(
                        4,
                        0,
                        0,
                        0
                )
        );

        titles.add(hint);

        header.add(
                titles,
                BorderLayout.WEST
        );

        card.add(
                header,
                BorderLayout.NORTH
        );

        // =====================================================
        // TABLE
        // =====================================================

        PremiumUI.styleTable(
                billTable
        );

        billTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        billTable.setPreferredScrollableViewportSize(
                new Dimension(
                        1000,
                        260
                )
        );

        int[] widths = {
                55,
                55,
                170,
                70,
                105,
                115,
                95,
                95,
                105,
                110,
                95,
                115
        };

        for (
                int i = 0;
                i < widths.length;
                i++
        ) {

            billTable
                    .getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(
                            widths[i]
                    );
        }

        JScrollPane tableScroll =
                PremiumUI.scroll(
                        billTable
                );

        tableScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants
                        .HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        tableScroll.setVerticalScrollBarPolicy(
                ScrollPaneConstants
                        .VERTICAL_SCROLLBAR_AS_NEEDED
        );

        card.add(
                tableScroll,
                BorderLayout.CENTER
        );

        // =====================================================
        // FOOTER
        // =====================================================

        JPanel foot =
                new JPanel(
                        new BorderLayout()
                );

        foot.setOpaque(false);

        statusLine.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        statusLine.setForeground(
                PremiumUI.MUTED
        );

        foot.add(
                statusLine,
                BorderLayout.WEST
        );

        JLabel tip =
                new JLabel(
                        "TIP  ·  Sort columns by clicking a header"
                );

        tip.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        10
                )
        );

        tip.setForeground(
                PremiumUI.GOLD
        );

        foot.add(
                tip,
                BorderLayout.EAST
        );

        card.add(
                foot,
                BorderLayout.SOUTH
        );

        return card;
    }

    // =========================================================
    // LOAD ACTIVE RESERVATIONS
    // =========================================================

    public void reloadActiveReservations() {

        try {

            resComboBox.removeAllItems();

            List<Reservation> activeList =
                    reservationDAO.getActiveReservations();

            for (Reservation r : activeList) {

                resComboBox.addItem(
                        new ResComboItem(r)
                );
            }

            if (
                    resComboBox.getItemCount() > 0
            ) {

                resComboBox.setSelectedIndex(0);

                onReservationSelected();

            } else {

                roomChargesField.setText(
                        "0.00"
                );

                totalField.setText(
                        "0.00"
                );
            }

        } catch (SQLException e) {

            statusLine.setText(
                    "Could not load active reservations."
            );
        }
    }

    // =========================================================
    // RESERVATION SELECTED
    // =========================================================

    private void onReservationSelected() {

        ResComboItem item =
                (ResComboItem)
                        resComboBox
                                .getSelectedItem();

        if (item != null) {

            roomChargesField.setText(
                    item.calculatedRoomCharges
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            .toString()
            );

            calculateTotal();
        }
    }

    // =========================================================
    // CALCULATE TOTAL
    // =========================================================

    private BigDecimal calculateTotal() {

        try {

            BigDecimal room =
                    money(
                            roomChargesField
                                    .getText()
                    );

            BigDecimal service =
                    money(
                            serviceChargesField
                                    .getText()
                    );

            BigDecimal discount =
                    money(
                            discountField
                                    .getText()
                    );

            BigDecimal subtotal =
                    room
                            .add(service)
                            .subtract(discount);

            // Never allow negative subtotal.
            if (
                    subtotal.signum() < 0
            ) {

                subtotal =
                        BigDecimal.ZERO;
            }

            BigDecimal taxRate =
                    selectedTaxRate();

            BigDecimal tax =
                    subtotal
                            .multiply(taxRate)
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal total =
                    subtotal
                            .add(tax)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalField.setText(
                    total.toPlainString()
            );

            return total;

        } catch (Exception ex) {

            totalField.setText(
                    "0.00"
            );

            return BigDecimal.ZERO;
        }
    }

    // =========================================================
    // TAX RATE
    // =========================================================

    private BigDecimal selectedTaxRate() {

        String taxStr =
                (String)
                        taxBox.getSelectedItem();

        if (
                taxStr != null &&
                taxStr.contains("%")
        ) {

            return new BigDecimal(
                    taxStr
                            .substring(
                                    0,
                                    taxStr.indexOf('%')
                            )
                            .trim()
            );
        }

        return BigDecimal.ZERO;
    }

    // =========================================================
    // MONEY VALIDATION
    // =========================================================

    private BigDecimal money(
            String value
    ) {

        BigDecimal amount =
                new BigDecimal(
                        value.trim()
                );

        if (
                amount.signum() < 0
        ) {

            throw new IllegalArgumentException(
                    "Amounts cannot be negative."
            );
        }

        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    // =========================================================
    // GENERATE BILL
    // =========================================================

    private void generateBill() {

        ResComboItem selectedRes =
                (ResComboItem)
                        resComboBox
                                .getSelectedItem();

        if (selectedRes == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No active reservation selected for checkout.",
                    "Notice",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            BigDecimal room =
                    money(
                            roomChargesField
                                    .getText()
                    );

            BigDecimal service =
                    money(
                            serviceChargesField
                                    .getText()
                    );

            BigDecimal discount =
                    money(
                            discountField
                                    .getText()
                    );

            BigDecimal subtotal =
                    room
                            .add(service)
                            .subtract(discount);

            if (
                    subtotal.signum() < 0
            ) {

                subtotal =
                        BigDecimal.ZERO;
            }

            BigDecimal tax =
                    subtotal
                            .multiply(
                                    selectedTaxRate()
                            )
                            .divide(
                                    BigDecimal.valueOf(100),
                                    2,
                                    RoundingMode.HALF_UP
                            );

            BigDecimal total =
                    subtotal
                            .add(tax)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalField.setText(
                    total.toPlainString()
            );

            String mode =
                    String.valueOf(
                            paymentModeBox
                                    .getSelectedItem()
                    );

            String status =
                    String.valueOf(
                            paymentStatusBox
                                    .getSelectedItem()
                    );

            Billing bill =
                    new Billing(
                            selectedRes.id,
                            room,
                            service,
                            discount,
                            tax,
                            total,
                            mode,
                            status
                    );

            if (
                    billingDAO.createBill(bill)
            ) {

                showReceiptDialog(
                        selectedRes.guestName,
                        selectedRes.roomNumber,
                        selectedRes.nights,
                        room,
                        service,
                        discount,
                        tax,
                        total,
                        mode,
                        bill.getBillId()
                );

                reloadActiveReservations();

                loadBills();
            }

        } catch (
                IllegalArgumentException ex
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Input validation",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (
                SQLException ex
        ) {

            showDatabaseError(ex);
        }
    }

    // =========================================================
    // RECEIPT
    // =========================================================

    private void showReceiptDialog(
            String guest,
            String room,
            long nights,
            BigDecimal roomCost,
            BigDecimal serviceCost,
            BigDecimal discount,
            BigDecimal tax,
            BigDecimal total,
            String mode,
            int billId
    ) {

        String receipt =
                String.format(
                        """
                        ====================================================
                                    INNAURA HOTEL OPERATIONS
                                       FINAL CHECKOUT RECEIPT
                        ====================================================
                        Invoice Number : #BILL-%05d
                        Guest Name     : %s
                        Room Number    : Room %s
                        Duration       : %d Night(s)
                        Payment Mode   : %s
                        ----------------------------------------------------
                        Room Charges                     : ₹ %.2f
                        Room Service / Amenities         : ₹ %.2f
                        Special Discount                 : - ₹ %.2f
                        ----------------------------------------------------
                        GST / Tax Amount                 : + ₹ %.2f
                        ====================================================
                        TOTAL AMOUNT PAID                : ₹ %.2f
                        ====================================================
                                Thank you for staying with us!
                        ====================================================
                        """,
                        billId,
                        guest,
                        room,
                        nights,
                        mode,
                        roomCost,
                        serviceCost,
                        discount,
                        tax,
                        total
                );

        JTextArea area =
                new JTextArea(
                        receipt
                );

        area.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        area.setEditable(false);

        area.setBackground(
                PremiumUI.SURFACE
        );

        area.setForeground(
                PremiumUI.INK
        );

        area.setBorder(
                new EmptyBorder(
                        10,
                        15,
                        10,
                        15
                )
        );

        JScrollPane receiptScroll =
                new JScrollPane(area);

        receiptScroll.setPreferredSize(
                new Dimension(
                        600,
                        450
                )
        );

        JOptionPane.showMessageDialog(
                this,
                receiptScroll,
                "Checkout completed - Receipt #BILL-" + billId,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // SELECTED RECEIPT
    // =========================================================

    private void showSelectedReceipt() {

        int view =
                billTable.getSelectedRow();

        if (view < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a bill from the history table first.",
                    "Select a bill",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // IMPORTANT:
        // JTable may be sorted, therefore convert the
        // visible row to the actual model row.

        int row =
                billTable.convertRowIndexToModel(
                        view
                );

        int billId =
                (Integer)
                        tableModel.getValueAt(
                                row,
                                0
                        );

        String guest =
                String.valueOf(
                        tableModel.getValueAt(
                                row,
                                2
                        )
                );

        String room =
                String.valueOf(
                        tableModel.getValueAt(
                                row,
                                3
                        )
                );

        BigDecimal roomCost =
                (BigDecimal)
                        tableModel.getValueAt(
                                row,
                                4
                        );

        BigDecimal serviceCost =
                (BigDecimal)
                        tableModel.getValueAt(
                                row,
                                5
                        );

        BigDecimal discount =
                (BigDecimal)
                        tableModel.getValueAt(
                                row,
                                6
                        );

        BigDecimal tax =
                (BigDecimal)
                        tableModel.getValueAt(
                                row,
                                7
                        );

        BigDecimal total =
                (BigDecimal)
                        tableModel.getValueAt(
                                row,
                                8
                        );

        String mode =
                String.valueOf(
                        tableModel.getValueAt(
                                row,
                                9
                        )
                );

        showReceiptDialog(
                guest,
                room,
                1,
                roomCost,
                serviceCost,
                discount,
                tax,
                total,
                mode,
                billId
        );
    }

    // =========================================================
    // LOAD BILLING HISTORY
    // =========================================================

    private void loadBills() {

        try {

            tableModel.setRowCount(0);

            List<Billing> bills =
                    billingDAO.getAllBills();

            int paid = 0;

            int pending = 0;

            for (
                    Billing b : bills
            ) {

                if (
                        "PAID".equalsIgnoreCase(
                                b.getPaymentStatus()
                        )
                ) {

                    paid++;
                }

                if (
                        "PENDING".equalsIgnoreCase(
                                b.getPaymentStatus()
                        )
                ) {

                    pending++;
                }

                tableModel.addRow(
                        new Object[]{
                                b.getBillId(),
                                b.getReservationId(),
                                b.getGuestName(),
                                b.getRoomNumber(),
                                b.getRoomCharges(),
                                b.getServiceCharges(),
                                b.getDiscount(),
                                b.getTaxAmount(),
                                b.getTotalAmount(),
                                b.getPaymentMode(),
                                b.getPaymentStatus(),
                                b.getBillingDate()
                        }
                );
            }

            totalValue.setText(
                    String.valueOf(
                            bills.size()
                    )
            );

            paidValue.setText(
                    String.valueOf(
                            paid
                    )
            );

            pendingValue.setText(
                    String.valueOf(
                            pending
                    )
            );

            statusLine.setText(
                    bills.size() +
                    " invoice(s) · " +
                    paid +
                    " paid · " +
                    pending +
                    " pending"
            );

        } catch (
                SQLException ex
        ) {

            showDatabaseError(ex);
        }
    }

    // =========================================================
    // CLEAR FORM
    // =========================================================

    private void clearForm() {

        if (
                resComboBox.getItemCount() > 0
        ) {

            resComboBox.setSelectedIndex(0);
        }

        serviceChargesField.setText(
                "0.00"
        );

        discountField.setText(
                "0.00"
        );

        taxBox.setSelectedIndex(0);

        paymentModeBox.setSelectedIndex(0);

        paymentStatusBox.setSelectedIndex(0);

        onReservationSelected();
    }

    // =========================================================
    // DATABASE ERROR
    // =========================================================

    private void showDatabaseError(
            SQLException ex
    ) {

        ex.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                "Database operation failed. Check the MySQL connection and schema.\n\n"
                        + ex.getMessage(),
                "Database error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}