import dao.ServiceDAO;
import dao.StaffDAO;
import model.Service;
import model.Staff;
import ui.ModulePanel;
import ui.PremiumUI;
import ui.ServicePanel;
import ui.StaffPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** InnAura desktop shell: database-backed Staff/Services plus first-module prototype screens. */
public class Main {
    private static final Color SIDEBAR = new Color(28, 18, 13);
    private static final Color SIDEBAR_MUTED = new Color(196, 174, 148);
    private static final Color LINE = PremiumUI.LINE;
    private static final Font UI = PremiumUI.BODY;
    private static final Font UI_MEDIUM = PremiumUI.LABEL;

    private final String[] pageNames = {
            "Overview", "Room Management", "Guest Management", "Reservations",
            "Billing & Payments", "Employee Management", "Housekeeping", "Hotel Services"
    };
    private final String[] pageKeys = {
            "dashboard", "rooms", "guests", "reservations",
            "billing", "staff", "housekeeping", "services"
    };
    private final Map<String, JButton> navigationButtons = new LinkedHashMap<>();
    private final Map<String, String> keyToTitle = new LinkedHashMap<>();

    private JFrame frame;
    private CardLayout pageLayout;
    private JPanel pages;
    private JLabel pageTitle;
    private JLabel pageSubtitle;
    private JLabel connectionStatus;
    private JLabel staffCount;
    private JLabel activeStaffCount;
    private JLabel serviceCount;
    private JLabel availableServiceCount;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new Main().createAndShow();
        });
    }

    private void createAndShow() {
        frame = new JFrame("InnAura | Hotel Operations");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setSize(1440, 900);
        frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PremiumUI.CANVAS);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createWorkspace(), BorderLayout.CENTER);
        frame.setContentPane(root);
        showPage("dashboard");
        refreshDashboard();
        frame.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(252, 0));

        JPanel brand = new JPanel(new BorderLayout(11, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 19, 23, 14));
        JPanel mark = new JPanel(new GridBagLayout());
        mark.setBackground(PremiumUI.GOLD);
        mark.setPreferredSize(new Dimension(42, 42));
        JLabel monogram = new JLabel("I");
        monogram.setFont(new Font("Georgia", Font.BOLD, 25));
        monogram.setForeground(SIDEBAR);
        mark.add(monogram);
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel name = new JLabel("InnAura");
        name.setFont(new Font("Georgia", Font.BOLD, 23));
        name.setForeground(Color.WHITE);
        JLabel tag = new JLabel("HOTEL OPERATIONS");
        tag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        tag.setForeground(SIDEBAR_MUTED);
        tag.setBorder(new EmptyBorder(4, 1, 0, 0));
        brandText.add(name); brandText.add(tag);
        brand.add(mark, BorderLayout.WEST); brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(SIDEBAR);
        menu.setBorder(new EmptyBorder(3, 11, 12, 11));
        JLabel section = new JLabel("  WORKSPACE");
        section.setFont(new Font("Segoe UI", Font.BOLD, 10));
        section.setForeground(new Color(132, 151, 139));
        section.setBorder(new EmptyBorder(0, 3, 11, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(section);
        String[] icons = {"◫", "▦", "♙", "◇", "₹", "♧", "✓", "✧"};
        for (int i = 0; i < pageNames.length; i++) {
            final String key = pageKeys[i];
            keyToTitle.put(key, pageNames[i]);
            JButton button = navButton(icons[i], pageNames[i]);
            button.addActionListener(e -> showPage(key));
            navigationButtons.put(key, button);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            menu.add(button);
            menu.add(Box.createVerticalStrut(5));
        }
        JScrollPane menuScroll = new JScrollPane(menu);
        menuScroll.setBorder(null);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getViewport().setBackground(SIDEBAR);
        menuScroll.getVerticalScrollBar().setUnitIncrement(14);
        menuScroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        sidebar.add(menuScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(SIDEBAR);
        footer.setBorder(new EmptyBorder(12, 18, 18, 18));
        JLabel thought = new JLabel("A considered stay.");
        thought.setFont(new Font("Georgia", Font.ITALIC, 15));
        thought.setForeground(new Color(244, 231, 205));
        JLabel line = new JLabel("Thoughtful service, beautifully managed.");
        line.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        line.setForeground(SIDEBAR_MUTED);
        line.setBorder(new EmptyBorder(5, 0, 13, 0));
        JLabel version = new JLabel("INNAURA  •  DESKTOP");
        version.setFont(new Font("Segoe UI", Font.BOLD, 9));
        version.setForeground(new Color(177, 145, 108));
        footer.add(thought); footer.add(line); footer.add(version);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton navButton(String icon, String label) {
        JButton button = new JButton(icon + "    " + label);
        button.setFont(UI_MEDIUM);
        button.setForeground(SIDEBAR_MUTED);
        button.setBackground(SIDEBAR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 12, 12, 8));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(220, 43));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!button.getBackground().equals(PremiumUI.FOREST)) button.setBackground(new Color(57, 39, 29));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!button.getBackground().equals(PremiumUI.FOREST)) button.setBackground(SIDEBAR);
            }
        });
        return button;
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(PremiumUI.CANVAS);
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(PremiumUI.SURFACE);
        topbar.setBorder(new EmptyBorder(19, 31, 19, 31));
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                new EmptyBorder(19, 31, 19, 31)));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        pageTitle = new JLabel("Overview");
        pageTitle.setFont(new Font("Georgia", Font.PLAIN, 24));
        pageTitle.setForeground(PremiumUI.INK);
        pageSubtitle = PremiumUI.muted("A clear view of your hotel's day-to-day operations.");
        pageSubtitle.setBorder(new EmptyBorder(4, 1, 0, 0));
        heading.add(pageTitle); heading.add(pageSubtitle);
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        date.setFont(UI_MEDIUM); date.setForeground(PremiumUI.INK); date.setAlignmentX(Component.RIGHT_ALIGNMENT);
        connectionStatus = new JLabel("●  Checking database");
        connectionStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        connectionStatus.setForeground(PremiumUI.MUTED);
        connectionStatus.setBorder(new EmptyBorder(5, 0, 0, 0));
        connectionStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(date); right.add(connectionStatus);
        topbar.add(heading, BorderLayout.WEST); topbar.add(right, BorderLayout.EAST);
        workspace.add(topbar, BorderLayout.NORTH);

        pageLayout = new CardLayout();
        pages = new JPanel(pageLayout);
        pages.setBackground(PremiumUI.CANVAS);
        pages.add(createDashboard(), "dashboard");
        pages.add(new ModulePanel("Room Management", "Manage room details, prices, capacity and status.",
                new String[]{"Room No.", "Type", "Floor", "Capacity", "Price", "Status"},
                new String[]{"Room No.", "Type", "Floor", "Capacity", "Price", "Status"}), "rooms");
        pages.add(new ModulePanel("Guest Management", "Register and manage hotel guest details.",
                new String[]{"Guest ID", "Full Name", "Phone", "Email", "Address"},
                new String[]{"Guest ID", "Full Name", "Phone", "Email", "Address"}), "guests");
        pages.add(new ModulePanel("Reservations", "Manage guest bookings and reservation dates.",
                new String[]{"Booking ID", "Guest", "Room", "Check-in", "Check-out", "Status"},
                new String[]{"Booking ID", "Guest", "Room", "Check-in", "Check-out", "Status"}), "reservations");
        pages.add(new ModulePanel("Billing & Payments", "Track invoices, payments and outstanding balances.",
                new String[]{"Invoice ID", "Guest", "Amount", "Paid", "Balance", "Status"},
                new String[]{"Invoice ID", "Guest", "Amount", "Paid", "Balance", "Status"}), "billing");
        pages.add(new StaffPanel(), "staff");
        pages.add(new ModulePanel("Housekeeping", "Assign and track room cleaning and maintenance tasks.",
                new String[]{"Task ID", "Room", "Assigned Staff", "Task", "Status"},
                new String[]{"Task ID", "Room", "Assigned Staff", "Task", "Status"}), "housekeeping");
        pages.add(new ServicePanel(), "services");
        workspace.add(pages, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createDashboard() {
        JPanel page = new JPanel(new BorderLayout(0, 22));
        page.setBackground(PremiumUI.CANVAS);
        page.setBorder(new EmptyBorder(26, 30, 28, 30));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel hero = new JPanel(new BorderLayout(18, 0));
        hero.setBackground(PremiumUI.FOREST_DARK);
        hero.setBorder(new EmptyBorder(24, 26, 24, 26));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        JPanel heroCopy = new JPanel(); heroCopy.setOpaque(false);
        heroCopy.setLayout(new BoxLayout(heroCopy, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("INNAURA  /  OPERATIONS");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(new Color(226, 184, 111));
        JLabel heroTitle = new JLabel("Hospitality, in harmony.");
        heroTitle.setFont(new Font("Georgia", Font.PLAIN, 27)); heroTitle.setForeground(Color.WHITE);
        heroTitle.setBorder(new EmptyBorder(9, 0, 6, 0));
        JLabel heroBody = new JLabel("One workspace for your people, rooms and guest experiences.");
        heroBody.setFont(UI); heroBody.setForeground(new Color(224, 205, 179));
        heroCopy.add(eyebrow); heroCopy.add(heroTitle); heroCopy.add(heroBody);
        JButton refresh = PremiumUI.gold("Refresh overview  ↻");
        refresh.addActionListener(e -> refreshDashboard());
        hero.add(heroCopy, BorderLayout.CENTER); hero.add(refresh, BorderLayout.EAST);
        content.add(hero); content.add(Box.createVerticalStrut(23));

        JLabel metricsTitle = PremiumUI.title("Your operations at a glance");
        metricsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(metricsTitle); content.add(Box.createVerticalStrut(13));
        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0)); metrics.setOpaque(false);
        staffCount = new JLabel("—"); activeStaffCount = new JLabel("—");
        serviceCount = new JLabel("—"); availableServiceCount = new JLabel("—");
        metrics.add(metricCard("TEAM MEMBERS", staffCount, "Saved staff profiles", "♙", PremiumUI.FOREST));
        metrics.add(metricCard("ACTIVE TEAM", activeStaffCount, "Marked active", "✓", new Color(164, 124, 71)));
        metrics.add(metricCard("HOTEL SERVICES", serviceCount, "Configured offerings", "✧", PremiumUI.GOLD));
        metrics.add(metricCard("AVAILABLE", availableServiceCount, "Ready for guests", "◉", PremiumUI.BLUE));
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 143));
        content.add(metrics); content.add(Box.createVerticalStrut(24));

        JLabel quickTitle = PremiumUI.title("Quick access"); quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(quickTitle); content.add(Box.createVerticalStrut(12));
        JPanel quick = new JPanel(new GridLayout(2, 2, 12, 12)); quick.setOpaque(false);
        quick.add(shortcutCard("01", "Room management", "Rooms, rates and occupancy records.", "Open rooms →", () -> showPage("rooms")));
        quick.add(shortcutCard("02", "Guest management", "Guest details and contact information.", "Open guests →", () -> showPage("guests")));
        quick.add(shortcutCard("03", "Staff management", "Employee details, roles and status.", "Open staff →", () -> showPage("staff")));
        quick.add(shortcutCard("04", "Hotel services", "Offerings, categories and pricing.", "Manage services →", () -> showPage("services")));
        quick.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        content.add(quick);
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JPanel metricCard(String label, JLabel value, String detail, String symbol, Color accent) {
        JPanel card = PremiumUI.card(new BorderLayout(0, 8));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel name = new JLabel(label); name.setFont(new Font("Segoe UI", Font.BOLD, 10)); name.setForeground(PremiumUI.MUTED);
        JLabel icon = new JLabel(symbol); icon.setFont(new Font("Segoe UI", Font.BOLD, 18)); icon.setForeground(accent);
        top.add(name, BorderLayout.WEST); top.add(icon, BorderLayout.EAST);
        value.setFont(new Font("Georgia", Font.PLAIN, 29)); value.setForeground(PremiumUI.INK);
        JLabel note = PremiumUI.muted(detail); note.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        card.add(top, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel shortcutCard(String number, String title, String description, String action, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(PremiumUI.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new PremiumUI.SoftLineBorder(PremiumUI.LINE, 1, 14), new EmptyBorder(15, 17, 15, 17)));
        JLabel num = new JLabel(number); num.setFont(new Font("Georgia", Font.PLAIN, 23)); num.setForeground(PremiumUI.GOLD);
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title); heading.setFont(new Font("Segoe UI", Font.BOLD, 14)); heading.setForeground(PremiumUI.INK);
        JLabel body = new JLabel("<html>" + description + "</html>"); body.setFont(UI); body.setForeground(PremiumUI.MUTED);
        body.setBorder(new EmptyBorder(5, 0, 7, 0));
        JButton link = new JButton(action); link.setFont(new Font("Segoe UI", Font.BOLD, 11)); link.setForeground(PremiumUI.GOLD);
        link.setHorizontalAlignment(SwingConstants.LEFT); link.setBorderPainted(false); link.setContentAreaFilled(false);
        link.setFocusPainted(false); link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setBorder(new EmptyBorder(0, 0, 0, 0)); link.addActionListener(e -> onClick.run());
        copy.add(heading); copy.add(body); copy.add(link);
        card.add(num, BorderLayout.WEST); card.add(copy, BorderLayout.CENTER);
        return card;
    }

    private void showPage(String key) {
        pageLayout.show(pages, key);
        for (Map.Entry<String, JButton> entry : navigationButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            entry.getValue().setBackground(active ? PremiumUI.FOREST : SIDEBAR);
            entry.getValue().setForeground(active ? Color.WHITE : SIDEBAR_MUTED);
        }
        pageTitle.setText(keyToTitle.getOrDefault(key, "Overview"));
        switch (key) {
            case "dashboard": pageSubtitle.setText("A clear view of your hotel's day-to-day operations."); break;
            case "staff": pageSubtitle.setText("Manage your team, roles and employee records."); break;
            case "services": pageSubtitle.setText("Curate hotel offerings, pricing and availability."); break;
            case "rooms": pageSubtitle.setText("Manage room details, prices, capacity and status. Prototype records are temporary."); break;
            case "guests": pageSubtitle.setText("Register and manage hotel guests. Prototype records are temporary."); break;
            case "reservations": pageSubtitle.setText("Manage bookings and reservation dates. Prototype records are temporary."); break;
            case "billing": pageSubtitle.setText("Track invoices and payments. Prototype records are temporary."); break;
            case "housekeeping": pageSubtitle.setText("Track cleaning tasks and assignments. Prototype records are temporary."); break;
            default: pageSubtitle.setText("Manage hotel operations in InnAura.");
        }
        if ("dashboard".equals(key)) refreshDashboard();
        pages.revalidate(); pages.repaint();
    }

    private void refreshDashboard() {
        try {
            List<Staff> staff = new StaffDAO().getAllStaff();
            List<Service> services = new ServiceDAO().getAllServices();
            long activeStaff = staff.stream().filter(Staff::isActive).count();
            long availableServices = services.stream().filter(Service::isAvailable).count();
            if (staffCount != null) staffCount.setText(String.valueOf(staff.size()));
            if (activeStaffCount != null) activeStaffCount.setText(String.valueOf(activeStaff));
            if (serviceCount != null) serviceCount.setText(String.valueOf(services.size()));
            if (availableServiceCount != null) availableServiceCount.setText(String.valueOf(availableServices));
            if (connectionStatus != null) {
                connectionStatus.setText("●  Database connected");
                connectionStatus.setForeground(PremiumUI.FOREST);
            }
        } catch (SQLException ex) {
            if (connectionStatus != null) {
                connectionStatus.setText("●  Database unavailable");
                connectionStatus.setForeground(PremiumUI.RED);
            }
            if (staffCount != null) staffCount.setText("—");
            if (activeStaffCount != null) activeStaffCount.setText("—");
            if (serviceCount != null) serviceCount.setText("—");
            if (availableServiceCount != null) availableServiceCount.setText("—");
            System.err.println("Could not refresh InnAura dashboard: " + ex.getMessage());
        }
    }
}
