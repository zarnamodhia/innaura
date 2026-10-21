package ui;

import dao.StaffDAO;
import model.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/** Staff CRUD screen with search, live summary cards and polished Swing styling. */
public class StaffPanel extends JPanel {
    private final StaffDAO dao = new StaffDAO();
    private final JTextField nameField = new JTextField(18);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{
            "Manager", "Receptionist", "Housekeeping", "Chef", "Waiter", "Security", "Maintenance", "Other"});
    private final JTextField phoneField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField salaryField = new JTextField(18);
    private final JTextField joiningDateField = new JTextField(LocalDate.now().toString(), 18);
    private final JTextField searchField = new JTextField(22);
    private final JLabel totalValue = new JLabel("—");
    private final JLabel activeValue = new JLabel("—");
    private final JLabel rolesValue = new JLabel("—");
    private final JLabel statusLine = new JLabel("Ready");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "ID", "Full name", "Role", "Phone", "Email", "Salary (₹)", "Joining date", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 0 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(model);

    public StaffPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(PremiumUI.CANVAS);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildScreen();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        loadStaff();
    }

    private void buildScreen() {
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JPanel titleLine = new JPanel(new BorderLayout());
        titleLine.setOpaque(false);
        JPanel titleCopy = new JPanel();
        titleCopy.setOpaque(false);
        titleCopy.setLayout(new BoxLayout(titleCopy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("People & team");
        title.setFont(PremiumUI.DISPLAY); title.setForeground(PremiumUI.INK);
        JLabel subtitle = PremiumUI.muted("A thoughtful view of the people behind every guest experience.");
        subtitle.setBorder(new EmptyBorder(5, 1, 0, 0));
        titleCopy.add(title); titleCopy.add(subtitle);
        JButton refresh = PremiumUI.secondary("↻  Refresh data");
        refresh.addActionListener(e -> loadStaff());
        titleLine.add(titleCopy, BorderLayout.WEST); titleLine.add(refresh, BorderLayout.EAST);
        north.add(titleLine); north.add(Box.createVerticalStrut(20));
        north.add(summaryStrip());
        add(north, BorderLayout.NORTH);

        JPanel form = formCard();
        JPanel catalogue = recordsCard();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, form, catalogue);
        split.setBorder(null);
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(0.50);
        split.setDividerSize(9);
        split.setDividerLocation(340);
        form.setMinimumSize(new Dimension(520, 300));
        form.setPreferredSize(new Dimension(900, 340));
        catalogue.setMinimumSize(new Dimension(520, 230));
        add(split, BorderLayout.CENTER);
    }

    private JPanel summaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 13, 0)); strip.setOpaque(false);
        strip.add(summaryCard("TEAM DIRECTORY", totalValue, "All saved staff profiles", PremiumUI.FOREST, "01"));
        strip.add(summaryCard("ACTIVE COLLEAGUES", activeValue, "Currently active employees", new Color(72, 132, 91), "02"));
        strip.add(summaryCard("ROLES REPRESENTED", rolesValue, "Distinct roles in your team", PremiumUI.GOLD, "03"));
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
        JPanel card = PremiumUI.card(new BorderLayout(0, 15));
        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(PremiumUI.title("Staff profile"));
        JLabel note = PremiumUI.muted("Create a profile or select a row below to edit it.");
        note.setBorder(new EmptyBorder(4, 0, 0, 0)); copy.add(note);
        header.add(copy, BorderLayout.WEST);
        JLabel dateHint = new JLabel("DATE FORMAT  YYYY-MM-DD");
        dateHint.setFont(new Font("Segoe UI", Font.BOLD, 9)); dateHint.setForeground(PremiumUI.GOLD);
        header.add(dateHint, BorderLayout.EAST); card.add(header, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 14); g.weightx = 1;
        addField(fields, g, 0, 0, "FULL NAME", nameField);
        addField(fields, g, 1, 0, "ROLE / DEPARTMENT", roleBox);
        addField(fields, g, 2, 0, "PHONE NUMBER", phoneField);
        addField(fields, g, 0, 1, "EMAIL ADDRESS", emailField);
        addField(fields, g, 1, 1, "MONTHLY SALARY (₹)", salaryField);
        addField(fields, g, 2, 1, "JOINING DATE", joiningDateField);
        card.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)); actions.setOpaque(false);
        JButton add = PremiumUI.primary("＋  Add staff");
        JButton update = PremiumUI.gold("↻  Save changes");
        JButton deactivate = PremiumUI.danger("Deactivate");
        JButton clear = PremiumUI.secondary("Clear form");
        add.addActionListener(e -> addStaff()); update.addActionListener(e -> updateStaff());
        deactivate.addActionListener(e -> deactivateStaff()); clear.addActionListener(e -> clearForm());
        actions.add(add); actions.add(update); actions.add(deactivate); actions.add(clear);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel panel, GridBagConstraints g, int col, int row, String label, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 7)); wrapper.setOpaque(false);
        wrapper.add(PremiumUI.fieldLabel(label), BorderLayout.NORTH);
        PremiumUI.styleInput(field);
        if (field instanceof JComboBox<?>) field.setPreferredSize(new Dimension(190, 38));
        wrapper.add(field, BorderLayout.CENTER);
        g.gridx = col; g.gridy = row; g.weightx = 1; g.insets = new Insets(5, col == 0 ? 0 : 8, 5, 8);
        panel.add(wrapper, g);
    }

    private JPanel recordsCard() {
        JPanel card = PremiumUI.card(new BorderLayout(0, 13));
        JPanel header = new JPanel(new BorderLayout(12, 0)); header.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(PremiumUI.title("Team directory"));
        JLabel hint = PremiumUI.muted("Select a record to load it into the profile form.");
        hint.setBorder(new EmptyBorder(4, 0, 0, 0)); titles.add(hint);
        JPanel searchWrap = new JPanel(new BorderLayout(8, 0)); searchWrap.setOpaque(false);
        JLabel searchIcon = new JLabel("⌕"); searchIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22)); searchIcon.setForeground(PremiumUI.FOREST);
        searchWrap.add(searchIcon, BorderLayout.WEST); PremiumUI.styleInput(searchField);
        searchField.setToolTipText("Search by name, role, email or phone");
        searchField.setPreferredSize(new Dimension(250, 38)); searchWrap.add(searchField, BorderLayout.CENTER);
        header.add(titles, BorderLayout.WEST); header.add(searchWrap, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);
        PremiumUI.styleTable(table);
        // Keep columns readable; the catalogue scrolls horizontally when needed.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setPreferredScrollableViewportSize(new Dimension(900, 230));
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(155);
        table.getColumnModel().getColumn(2).setPreferredWidth(115);
        table.getColumnModel().getColumn(3).setPreferredWidth(115);
        table.getColumnModel().getColumn(4).setPreferredWidth(190);
        table.getColumnModel().getColumn(5).setPreferredWidth(105);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        JScrollPane tableScroll = PremiumUI.scroll(table);
        tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScroll.getHorizontalScrollBar().setUnitIncrement(18);
        card.add(tableScroll, BorderLayout.CENTER);
        JPanel foot = new JPanel(new BorderLayout()); foot.setOpaque(false);
        statusLine.setFont(new Font("Segoe UI", Font.PLAIN, 11)); statusLine.setForeground(PremiumUI.MUTED);
        foot.add(statusLine, BorderLayout.WEST);
        JLabel shortcut = new JLabel("TIP  ·  Click a row to edit"); shortcut.setFont(new Font("Segoe UI", Font.BOLD, 10)); shortcut.setForeground(PremiumUI.GOLD);
        foot.add(shortcut, BorderLayout.EAST); card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private Staff readForm() {
        String name = nameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String salaryText = salaryField.getText().trim();
        String dateText = joiningDateField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Enter the staff member's full name.");
        if (name.length() > 100) throw new IllegalArgumentException("Name must be 100 characters or fewer.");
        if (phone.isEmpty()) throw new IllegalArgumentException("Enter a phone number.");
        if (!Pattern.matches("[+0-9() .-]{7,20}", phone)) throw new IllegalArgumentException("Enter a valid phone number.");
        if (!email.isEmpty() && !Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", email))
            throw new IllegalArgumentException("Enter a valid email address.");
        if (salaryText.isEmpty()) throw new IllegalArgumentException("Enter a monthly salary.");
        BigDecimal salary;
        try { salary = new BigDecimal(salaryText); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Salary must be a valid number."); }
        if (salary.signum() < 0) throw new IllegalArgumentException("Salary cannot be negative.");
        if (salary.scale() > 2) throw new IllegalArgumentException("Salary can have at most two decimal places.");
        LocalDate date;
        try { date = LocalDate.parse(dateText); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Use the date format YYYY-MM-DD."); }
        if (date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Joining date cannot be in the future.");
        return new Staff(name, role, phone, email, salary, date, true);
    }

    private void addStaff() {
        try {
            Staff staff = readForm();
            if (dao.addStaff(staff)) { toast("Staff profile added successfully.", false); clearForm(); loadStaff(); }
            else toast("No staff record was added.", true);
        } catch (IllegalArgumentException ex) { toast(ex.getMessage(), true); }
        catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void updateStaff() {
        int view = table.getSelectedRow();
        if (view < 0) { toast("Select a staff member from the directory first.", true); return; }
        int row = table.convertRowIndexToModel(view);
        int id = (Integer) model.getValueAt(row, 0);
        try {
            Staff values = readForm();
            Staff updated = new Staff(id, values.getFullName(), values.getRole(), values.getPhone(),
                    values.getEmail(), values.getSalary(), values.getJoiningDate(), true);
            if (dao.updateStaff(updated)) { toast("Staff profile updated.", false); clearForm(); loadStaff(); }
            else toast("Staff record not found. Refresh the directory.", true);
        } catch (IllegalArgumentException ex) { toast(ex.getMessage(), true); }
        catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void deactivateStaff() {
        int view = table.getSelectedRow();
        if (view < 0) { toast("Select a staff member to deactivate.", true); return; }
        int row = table.convertRowIndexToModel(view);
        int id = (Integer) model.getValueAt(row, 0);
        String name = String.valueOf(model.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate " + name + "?\nTheir record will be retained in the database.",
                "Confirm staff deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (dao.deactivateStaff(id)) { toast("Staff member deactivated.", false); clearForm(); loadStaff(); }
            else toast("Staff record not found.", true);
        } catch (SQLException ex) { showDatabaseError(ex); }
    }

    private void loadStaff() {
        try {
            List<Staff> staff = dao.getAllStaff();
            model.setRowCount(0);
            int active = 0; java.util.Set<String> roles = new java.util.HashSet<>();
            for (Staff s : staff) {
                if (s.isActive()) active++;
                if (s.getRole() != null) roles.add(s.getRole());
                model.addRow(new Object[]{s.getStaffId(), s.getFullName(), s.getRole(), s.getPhone(),
                        s.getEmail(), s.getSalary() == null ? "—" : s.getSalary().toPlainString(),
                        s.getJoiningDate(), s.isActive() ? "Active" : "Inactive"});
            }
            totalValue.setText(String.valueOf(staff.size())); activeValue.setText(String.valueOf(active));
            rolesValue.setText(String.valueOf(roles.size()));
            statusLine.setText(staff.size() + " staff profile(s) · " + active + " active");
        } catch (SQLException ex) { showDatabaseError(ex); statusLine.setText("Could not load staff records."); }
    }

    private void filter() {
        String q = searchField.getText().trim().toLowerCase();
        if (!(table.getRowSorter() instanceof javax.swing.table.TableRowSorter<?>)) return;
        @SuppressWarnings("unchecked") javax.swing.table.TableRowSorter<DefaultTableModel> sorter =
                (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getRowSorter();
        sorter.setRowFilter(q.isEmpty() ? null : javax.swing.RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        statusLine.setText(table.getRowCount() + " matching profile(s)");
    }

    private void populateForm() {
        int view = table.getSelectedRow(); if (view < 0) return;
        int r = table.convertRowIndexToModel(view);
        nameField.setText(str(r, 1)); roleBox.setSelectedItem(str(r, 2)); phoneField.setText(str(r, 3));
        emailField.setText(str(r, 4)); salaryField.setText(str(r, 5)); joiningDateField.setText(str(r, 6));
    }
    private String str(int row, int col) { Object v = model.getValueAt(row, col); return v == null ? "" : v.toString(); }

    private void clearForm() {
        table.clearSelection(); nameField.setText(""); roleBox.setSelectedIndex(0); phoneField.setText("");
        emailField.setText(""); salaryField.setText(""); joiningDateField.setText(LocalDate.now().toString());
        nameField.requestFocusInWindow();
    }

    private void toast(String message, boolean error) {
        statusLine.setForeground(error ? PremiumUI.RED : PremiumUI.FOREST);
        statusLine.setText((error ? "!  " : "✓  ") + message);
        if (error) JOptionPane.showMessageDialog(this, message, "Staff management", JOptionPane.WARNING_MESSAGE);
    }
    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database operation failed. Check the MySQL connection and schema.\n\n" + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(new EmptyBorder(0, 4, 0, 4));
            if (!selected) {
                boolean active = "Active".equals(value);
                l.setForeground(active ? PremiumUI.FOREST : PremiumUI.RED);
                l.setBackground(active ? new Color(77, 67, 43) : PremiumUI.RED_PALE);
                l.setOpaque(true);
            }
            return l;
        }
    }
}
