package ui;

import dao.ServiceDAO;
import model.Service;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/** Hotel service catalogue with searchable records and premium CRUD form. */
public class ServicePanel extends JPanel {
    private final ServiceDAO dao = new ServiceDAO();
    private final JTextField nameField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(3, 20);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{
            "Food", "Laundry", "Wellness", "Transport", "Housekeeping", "Room Service", "Dining", "Other"});
    private final JTextField priceField = new JTextField(20);
    private final JCheckBox availableBox = new JCheckBox("Available for guest requests", true);
    private final JTextField searchField = new JTextField(22);
    private final JLabel totalValue = new JLabel("—");
    private final JLabel availableValue = new JLabel("—");
    private final JLabel categoriesValue = new JLabel("—");
    private final JLabel statusLine = new JLabel("Ready");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID", "Service name", "Description", "Category", "Price (₹)", "Availability"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) { return column == 0 ? Integer.class : String.class; }
    };
    private final JTable table = new JTable(model);

    public ServicePanel() {
        setLayout(new BorderLayout(0, 18)); setBackground(PremiumUI.CANVAS);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildScreen();
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) populateForm(); });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        loadServices();
    }

    private void buildScreen() {
        JPanel north = new JPanel(); north.setOpaque(false); north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JPanel titleLine = new JPanel(new BorderLayout()); titleLine.setOpaque(false);
        JPanel titleCopy = new JPanel(); titleCopy.setOpaque(false); titleCopy.setLayout(new BoxLayout(titleCopy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Guest experiences"); title.setFont(PremiumUI.DISPLAY); title.setForeground(PremiumUI.INK);
        JLabel subtitle = PremiumUI.muted("Curate the little details that make a stay feel exceptional.");
        subtitle.setBorder(new EmptyBorder(5, 1, 0, 0)); titleCopy.add(title); titleCopy.add(subtitle);
        JButton refresh = PremiumUI.secondary("↻  Refresh catalogue"); refresh.addActionListener(e -> loadServices());
        titleLine.add(titleCopy, BorderLayout.WEST); titleLine.add(refresh, BorderLayout.EAST);
        north.add(titleLine); north.add(Box.createVerticalStrut(20)); north.add(summaryStrip());
        add(north, BorderLayout.NORTH);

        JPanel form = formCard();
        JPanel catalogue = recordsCard();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, form, catalogue);
        split.setBorder(null);
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(0.38);
        split.setDividerSize(9);
        split.setDividerLocation(0.38);
        form.setMinimumSize(new Dimension(520, 205));
        catalogue.setMinimumSize(new Dimension(520, 230));
        add(split, BorderLayout.CENTER);
    }

    private JPanel summaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 13, 0)); strip.setOpaque(false);
        strip.add(summaryCard("SERVICE CATALOGUE", totalValue, "All configured offerings", PremiumUI.FOREST, "01"));
        strip.add(summaryCard("CURRENTLY AVAILABLE", availableValue, "Ready for guests to request", new Color(72, 132, 91), "02"));
        strip.add(summaryCard("SERVICE CATEGORIES", categoriesValue, "Distinct experience types", PremiumUI.GOLD, "03"));
        return strip;
    }

    private JPanel summaryCard(String label, JLabel value, String detail, Color accent, String number) {
        JPanel card = PremiumUI.card(new BorderLayout(0, 8));
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new PremiumUI.SoftLineBorder(PremiumUI.LINE, 1, 15), new EmptyBorder(15, 18, 15, 18)));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel name = new JLabel(label); name.setFont(new Font("Segoe UI", Font.BOLD, 10)); name.setForeground(PremiumUI.MUTED);
        JLabel marker = new JLabel(number); marker.setFont(new Font("Georgia", Font.PLAIN, 16)); marker.setForeground(accent);
        top.add(name, BorderLayout.WEST); top.add(marker, BorderLayout.EAST);
        value.setFont(new Font("Georgia", Font.PLAIN, 29)); value.setForeground(PremiumUI.INK);
        JLabel caption = PremiumUI.muted(detail); caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        card.add(top, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); card.add(caption, BorderLayout.SOUTH);
        return card;
    }

    private JPanel formCard() {
        JPanel card = PremiumUI.card(new BorderLayout(0, 14));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(PremiumUI.title("Service editor"));
        JLabel note = PremiumUI.muted("Add a new offering or select an existing service to make changes.");
        note.setBorder(new EmptyBorder(4, 0, 0, 0)); copy.add(note);
        header.add(copy, BorderLayout.WEST);
        JLabel hint = new JLabel("PRICING  ·  INR ₹"); hint.setFont(new Font("Segoe UI", Font.BOLD, 10)); hint.setForeground(PremiumUI.GOLD);
        header.add(hint, BorderLayout.EAST); card.add(header, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        addField(fields, g, 0, 0, "SERVICE NAME", nameField);
        addField(fields, g, 1, 0, "CATEGORY", categoryBox);
        addField(fields, g, 2, 0, "PRICE (₹)", priceField);
        JPanel desc = new JPanel(new BorderLayout(0, 7)); desc.setOpaque(false);
        desc.add(PremiumUI.fieldLabel("DESCRIPTION"), BorderLayout.NORTH);
        descriptionArea.setFont(PremiumUI.BODY); descriptionArea.setForeground(PremiumUI.INK);
        descriptionArea.setBackground(PremiumUI.SURFACE); descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane descScroll = new JScrollPane(descriptionArea); descScroll.setPreferredSize(new Dimension(300, 62));
        descScroll.setBorder(new PremiumUI.SoftLineBorder(PremiumUI.LINE, 1, 10)); descScroll.getViewport().setBackground(PremiumUI.SURFACE);
        desc.add(descScroll, BorderLayout.CENTER);
        g.gridx = 0; g.gridy = 1; g.gridwidth = 2; g.weightx = 2; g.insets = new Insets(8, 0, 5, 8); fields.add(desc, g);
        JPanel availability = new JPanel(new BorderLayout()); availability.setOpaque(false);
        availableBox.setFont(PremiumUI.BODY); availableBox.setForeground(PremiumUI.FOREST_DARK);
        availableBox.setBackground(PremiumUI.SURFACE); availableBox.setFocusPainted(false);
        availability.add(availableBox, BorderLayout.CENTER);
        g.gridx = 2; g.gridy = 1; g.gridwidth = 1; g.weightx = 1; g.insets = new Insets(8, 8, 5, 0); fields.add(availability, g);
        card.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)); actions.setOpaque(false);
        JButton add = PremiumUI.primary("＋  Add service"); JButton update = PremiumUI.gold("↻  Save changes");
        JButton deactivate = PremiumUI.danger("Deactivate"); JButton clear = PremiumUI.secondary("Clear form");
        add.addActionListener(e -> addService()); update.addActionListener(e -> updateService());
        deactivate.addActionListener(e -> deactivateService()); clear.addActionListener(e -> clearForm());
        actions.add(add); actions.add(update); actions.add(deactivate); actions.add(clear);
        card.add(actions, BorderLayout.SOUTH); return card;
    }

    private void addField(JPanel panel, GridBagConstraints g, int col, int row, String label, JComponent input) {
        JPanel wrap = new JPanel(new BorderLayout(0, 7)); wrap.setOpaque(false);
        wrap.add(PremiumUI.fieldLabel(label), BorderLayout.NORTH); PremiumUI.styleInput(input);
        g.gridx = col; g.gridy = row; g.gridwidth = 1; g.weightx = 1; g.insets = new Insets(5, col == 0 ? 0 : 8, 5, 8);
        wrap.add(input, BorderLayout.CENTER); panel.add(wrap, g);
    }

    private JPanel recordsCard() {
        JPanel card = PremiumUI.card(new BorderLayout(0, 13));
        JPanel header = new JPanel(new BorderLayout(12, 0)); header.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(PremiumUI.title("Service catalogue"));
        JLabel hint = PremiumUI.muted("Select any service to inspect or edit its details."); hint.setBorder(new EmptyBorder(4, 0, 0, 0)); titles.add(hint);
        JPanel searchWrap = new JPanel(new BorderLayout(8, 0)); searchWrap.setOpaque(false);
        JLabel searchIcon = new JLabel("⌕"); searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22)); searchIcon.setForeground(PremiumUI.FOREST);
        searchWrap.add(searchIcon, BorderLayout.WEST); PremiumUI.styleInput(searchField);
        searchField.setToolTipText("Search by service name, description or category"); searchField.setPreferredSize(new Dimension(250, 38));
        searchWrap.add(searchField, BorderLayout.CENTER); header.add(titles, BorderLayout.WEST); header.add(searchWrap, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        PremiumUI.styleTable(table);
        // Keep columns readable; the catalogue scrolls horizontally when needed.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setPreferredScrollableViewportSize(new Dimension(900, 230));
        table.getColumnModel().getColumn(0).setPreferredWidth(48); table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(300); table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(105); table.getColumnModel().getColumn(5).setPreferredWidth(115);
        table.getColumnModel().getColumn(5).setCellRenderer(new AvailabilityRenderer());
        JScrollPane tableScroll = PremiumUI.scroll(table);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.getHorizontalScrollBar().setUnitIncrement(18);
        card.add(tableScroll, BorderLayout.CENTER);
        JPanel foot = new JPanel(new BorderLayout()); foot.setOpaque(false);
        statusLine.setFont(new Font("Segoe UI", Font.PLAIN, 11)); statusLine.setForeground(PremiumUI.MUTED); foot.add(statusLine, BorderLayout.WEST);
        JLabel tip = new JLabel("TIP  ·  Deactivation retains the service record"); tip.setFont(new Font("Segoe UI", Font.BOLD, 10)); tip.setForeground(PremiumUI.GOLD);
        foot.add(tip, BorderLayout.EAST); card.add(foot, BorderLayout.SOUTH); return card;
    }

    private Service readForm() {
        String name = nameField.getText().trim(), description = descriptionArea.getText().trim();
        String category = (String) categoryBox.getSelectedItem(), priceText = priceField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Enter a service name.");
        if (name.length() > 120) throw new IllegalArgumentException("Service name must be 120 characters or fewer.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Choose a service category.");
        if (priceText.isEmpty()) throw new IllegalArgumentException("Enter a price in rupees.");
        BigDecimal price;
        try { price = new BigDecimal(priceText); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Enter a valid price, such as 150 or 150.50."); }
        if (price.signum() < 0) throw new IllegalArgumentException("Price cannot be negative.");
        if (price.scale() > 2) throw new IllegalArgumentException("Price can have at most two decimal places.");
        if (price.precision() - price.scale() > 8) throw new IllegalArgumentException("Price is too large.");
        return new Service(name, description, category, price, availableBox.isSelected());
    }

    private void addService() {
        try {
            if (dao.addService(readForm())) { toast("Service added to the catalogue.", false); clearForm(); loadServices(); }
            else toast("Service could not be added.", true);
        } catch (IllegalArgumentException ex) { toast(ex.getMessage(), true); }
        catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void updateService() {
        int view = table.getSelectedRow(); if (view < 0) { toast("Select a service from the catalogue first.", true); return; }
        int row = table.convertRowIndexToModel(view); int id = (Integer) model.getValueAt(row, 0);
        try {
            Service v = readForm(); Service updated = new Service(id, v.getServiceName(), v.getDescription(), v.getCategory(), v.getPrice(), v.isAvailable());
            if (dao.updateService(updated)) { toast("Service details updated.", false); clearForm(); loadServices(); }
            else toast("Service not found. Refresh the catalogue.", true);
        } catch (IllegalArgumentException ex) { toast(ex.getMessage(), true); }
        catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void deactivateService() {
        int view = table.getSelectedRow(); if (view < 0) { toast("Select a service to deactivate.", true); return; }
        int row = table.convertRowIndexToModel(view); int id = (Integer) model.getValueAt(row, 0);
        String name = String.valueOf(model.getValueAt(row, 1));
        int choice = JOptionPane.showConfirmDialog(this, "Make ‘" + name + "’ unavailable?\nThe record will remain in the catalogue.",
                "Confirm service deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            if (dao.deactivateService(id)) { toast("Service marked unavailable.", false); clearForm(); loadServices(); }
            else toast("Service not found.", true);
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void loadServices() {
        try {
            List<Service> services = dao.getAllServices(); model.setRowCount(0);
            int available = 0; java.util.Set<String> categories = new java.util.HashSet<>();
            for (Service s : services) {
                if (s.isAvailable()) available++;
                if (s.getCategory() != null) categories.add(s.getCategory());
                model.addRow(new Object[]{s.getServiceId(), s.getServiceName(), s.getDescription(), s.getCategory(),
                        s.getPrice() == null ? "—" : s.getPrice().toPlainString(), s.isAvailable() ? "Available" : "Unavailable"});
            }
            totalValue.setText(String.valueOf(services.size())); availableValue.setText(String.valueOf(available));
            categoriesValue.setText(String.valueOf(categories.size()));
            statusLine.setText(services.size() + " service(s) · " + available + " available");
        } catch (SQLException ex) { showDatabaseError(ex); statusLine.setText("Could not load services."); }
    }

    private void filter() {
        String q = searchField.getText().trim();
        if (!(table.getRowSorter() instanceof javax.swing.table.TableRowSorter<?>)) return;
        @SuppressWarnings("unchecked") javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getRowSorter();
        sorter.setRowFilter(q.isEmpty() ? null : javax.swing.RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        statusLine.setText(table.getRowCount() + " matching service(s)");
    }

    private void populateForm() {
        int view = table.getSelectedRow(); if (view < 0) return; int r = table.convertRowIndexToModel(view);
        nameField.setText(str(r, 1)); descriptionArea.setText(str(r, 2)); categoryBox.setSelectedItem(str(r, 3));
        priceField.setText(str(r, 4)); availableBox.setSelected("Available".equals(str(r, 5)));
    }
    private String str(int row, int col) { Object v = model.getValueAt(row, col); return v == null ? "" : v.toString(); }
    private void clearForm() {
        table.clearSelection(); nameField.setText(""); descriptionArea.setText(""); categoryBox.setSelectedIndex(0);
        priceField.setText(""); availableBox.setSelected(true); nameField.requestFocusInWindow();
    }
    private void toast(String message, boolean error) {
        statusLine.setForeground(error ? PremiumUI.RED : PremiumUI.FOREST);
        statusLine.setText((error ? "!  " : "✓  ") + message);
        if (error) JOptionPane.showMessageDialog(this, message, "Hotel services", JOptionPane.WARNING_MESSAGE);
    }
    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace(); JOptionPane.showMessageDialog(this,
                "Database operation failed. Check the MySQL connection and schema.\n\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private static class AvailabilityRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            l.setHorizontalAlignment(SwingConstants.CENTER); l.setBorder(new EmptyBorder(0, 4, 0, 4));
            if (!selected) {
                boolean available = "Available".equals(value);
                l.setForeground(available ? PremiumUI.FOREST : PremiumUI.RED);
                l.setBackground(available ? new Color(240, 248, 239) : PremiumUI.RED_PALE); l.setOpaque(true);
            }
            return l;
        }
    }
}
