package ui;

import java.awt.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ModulePanel extends JPanel {

    private final String[] fields;
    private final JTextField[] inputs;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField = new JTextField(18);

    public ModulePanel(
            String title,
            String description,
            String[] columns,
            String[] fields) {

        if (columns.length != fields.length) {
            throw new IllegalArgumentException(
                "Columns and fields must have the same length.");
        }

        this.fields = fields.clone();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 251));
        setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitle = new JLabel(description);
        subtitle.setForeground(Color.GRAY);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 7));
        top.setOpaque(false);
        top.add(heading);
        top.add(subtitle);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(29);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(
            new Font("SansSerif", Font.BOLD, 12));

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel tablePanel = new JPanel(new BorderLayout(8, 8));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel =
            new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);

        JButton searchButton = makeButton("Search",
            new Color(48, 104, 190));
        JButton refreshButton = makeButton("Refresh",
            new Color(95, 105, 120));

        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        inputs = new JTextField[fields.length];

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(225, 229, 236)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int rows = Math.min(fields.length, 4);

        for (int i = 0; i < fields.length; i++) {
            int gridRow = i % rows;
            int gridColumn = (i / rows) * 2;

            gbc.gridx = gridColumn;
            gbc.gridy = gridRow;
            gbc.weightx = 0;

            form.add(new JLabel(fields[i] + ":"), gbc);

            inputs[i] = new JTextField(12);
            gbc.gridx = gridColumn + 1;
            gbc.weightx = 1;
            form.add(inputs[i], gbc);
        }

        JPanel actions =
            new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(Color.WHITE);

        JButton addButton = makeButton("Add",
            new Color(36, 142, 94));
        JButton updateButton = makeButton("Update",
            new Color(48, 104, 190));
        JButton deleteButton = makeButton("Delete",
            new Color(190, 65, 65));
        JButton clearButton = makeButton("Clear",
            new Color(95, 105, 120));

        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(clearButton);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setOpaque(false);
        bottom.add(form, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(tablePanel, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        addButton.addActionListener(e -> addRecord());
        updateButton.addActionListener(e -> updateRecord());
        deleteButton.addActionListener(e -> deleteRecord());
        clearButton.addActionListener(e -> clearFields());

        searchButton.addActionListener(e -> filterRows());
        searchField.addActionListener(e -> filterRows());

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            sorter.setRowFilter(null);
            clearFields();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRow();
            }
        });
    }

    private JButton makeButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        return button;
    }

    private String[] readInputs() {
        String[] values = new String[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            values[i] = inputs[i].getText().trim();

            if (values[i].isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    fields[i] + " is required.",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE
                );
                inputs[i].requestFocusInWindow();
                return null;
            }
        }

        return values;
    }

    private void addRecord() {
        String[] values = readInputs();
        if (values == null) {
            return;
        }

        model.addRow(values);
        clearFields();
    }

    private void updateRecord() {
        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                this, "Select a record to update.");
            return;
        }

        String[] values = readInputs();
        if (values == null) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        for (int i = 0; i < values.length; i++) {
            model.setValueAt(values[i], modelRow, i);
        }

        clearFields();
    }

    private void deleteRecord() {
        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                this, "Select a record to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Delete the selected record?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            model.removeRow(modelRow);
            clearFields();
        }
    }

    private void loadSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        for (int i = 0; i < inputs.length; i++) {
            Object value = model.getValueAt(modelRow, i);
            inputs[i].setText(value == null ? "" : value.toString());
        }
    }

    private void filterRows() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(query)));
        }

        clearFields();
    }

    private void clearFields() {
        for (JTextField input : inputs) {
            input.setText("");
        }

        table.clearSelection();
    }
} 