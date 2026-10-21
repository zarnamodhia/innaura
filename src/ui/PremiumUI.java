package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/** Shared premium visual system for InnAura. No external dependencies. */
public final class PremiumUI {
    private PremiumUI() {}

    public static final Color INK = new Color(244, 231, 211);
    public static final Color FOREST = new Color(153, 105, 49);
    public static final Color FOREST_DARK = new Color(39, 24, 17);
    public static final Color MINT = new Color(91, 63, 43);
    public static final Color GOLD = new Color(210, 164, 88);
    public static final Color GOLD_PALE = new Color(210, 164, 88);
    public static final Color CANVAS = new Color(43, 29, 22);
    public static final Color SURFACE = new Color(57, 39, 29);
    public static final Color LINE = new Color(100, 73, 52);
    public static final Color MUTED = new Color(199, 177, 151);
    public static final Color RED = new Color(225, 122, 101);
    public static final Color RED_PALE = new Color(79, 43, 35);
    public static final Color BLUE = new Color(193, 151, 105);
    public static final Color BLUE_PALE = new Color(70, 52, 38);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font DISPLAY = new Font("Georgia", Font.PLAIN, 25);

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new SoftLineBorder(LINE, 1, 16),
                new EmptyBorder(18, 20, 18, 20)));
        return p;
    }

    public static JPanel card() { return card(new BorderLayout(0, 12)); }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", Font.PLAIN, 21));
        l.setForeground(INK);
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BODY);
        l.setForeground(MUTED);
        return l;
    }

    public static void styleInput(JComponent c) {
        c.setFont(BODY);
        c.setForeground(INK);
        c.setBackground(SURFACE);
        c.setOpaque(true);
        c.setPreferredSize(new Dimension(190, 38));
        c.setBorder(BorderFactory.createCompoundBorder(
                new SoftLineBorder(LINE, 1, 10), new EmptyBorder(7, 10, 7, 10)));
        if (c instanceof JTextField f) f.setCaretColor(GOLD);
        if (c instanceof JComboBox<?> box) box.setFocusable(true);
    }

    public static JButton button(String text, Color base, Color foreground) {
        AnimatedButton b = new AnimatedButton(text, base, foreground);
        b.setFont(LABEL);
        b.setForeground(foreground);
        b.setBackground(base);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(11, 16, 11, 16));
        return b;
    }

    public static JButton primary(String text) { return button(text, FOREST, Color.WHITE); }
    public static JButton secondary(String text) { return button(text, new Color(81, 58, 42), INK); }
    public static JButton danger(String text) { return button(text, RED_PALE, RED); }
    public static JButton gold(String text) { return button(text, GOLD_PALE, new Color(39, 24, 17)); }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setForeground(INK);
        table.setBackground(SURFACE);
        table.setRowHeight(39);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(75, 54, 40));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(MINT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setForeground(new Color(223, 198, 164));
        h.setBackground(new Color(68, 47, 35));
        h.setPreferredSize(new Dimension(0, 42));
        h.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                c.setBackground(selected ? MINT : (row % 2 == 0 ? SURFACE : new Color(48, 33, 25)));
                c.setForeground(selected ? Color.WHITE : INK);
                if (c instanceof JLabel l) {
                    l.setBorder(new EmptyBorder(0, 10, 0, 10));
                    l.setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
    }

    public static JScrollPane scroll(JTable table) {
        JScrollPane s = new JScrollPane(table);
        s.setBorder(new SoftLineBorder(LINE, 1, 12));
        s.getViewport().setBackground(SURFACE);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL);
        l.setForeground(new Color(79, 91, 81));
        return l;
    }

    public static class SoftLineBorder extends AbstractBorder {
        private final Color color; private final int thickness, arc;
        public SoftLineBorder(Color color, int thickness, int arc) {
            this.color = color; this.thickness = thickness; this.arc = arc;
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 1, thickness + 1, thickness + 1, thickness + 1);
        }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(thickness + 1, thickness + 1, thickness + 1, thickness + 1); return insets;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    w - thickness, h - thickness, arc, arc);
            g2.dispose();
        }
    }

    /** Small 15ms hover-color transition using only Swing's built-in Timer. */
    private static class AnimatedButton extends JButton {
        private Color base, hover, current;
        private Timer timer;
        private float progress = 0f;
        AnimatedButton(String text, Color base, Color fg) {
            super(text); this.base = base; this.hover = blend(base, Color.WHITE, 0.13f); this.current = base;
            setForeground(fg);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { animate(true); }
                @Override public void mouseExited(MouseEvent e) { animate(false); }
            });
        }
        private void animate(boolean in) {
            if (timer != null && timer.isRunning()) timer.stop();
            final Color start = current, end = in ? hover : base;
            final long started = System.nanoTime();
            timer = new Timer(15, e -> {
                float t = Math.min(1f, (System.nanoTime() - started) / 150_000_000f);
                current = blend(start, end, t); repaint();
                if (t >= 1f) ((Timer)e.getSource()).stop();
            });
            timer.start();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(current); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
            g2.dispose(); super.paintComponent(g);
        }
        private static Color blend(Color a, Color b, float t) {
            t = Math.max(0, Math.min(1, t));
            return new Color((int)(a.getRed() + (b.getRed()-a.getRed())*t),
                    (int)(a.getGreen() + (b.getGreen()-a.getGreen())*t),
                    (int)(a.getBlue() + (b.getBlue()-a.getBlue())*t));
        }
    }
}
