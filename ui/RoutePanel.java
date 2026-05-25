package ui;

import database.RouteDAO;
import model.Route;
import model.Stop;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoutePanel extends JPanel {

    private final RouteDAO routeDAO;

    private final DefaultTableModel tableModel;
    private final DefaultTableModel stopsTableModel;

    private final JTable table;
    private final JTable stopsTable;

    // Шрифти
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public RoutePanel() {
        routeDAO = new RouteDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ліва таблиця з маршрутами
        tableModel = new DefaultTableModel(new String[]{"ID", "Номер маршруту"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(MAIN_FONT);
        table.setFocusable(false);
        table.setGridColor(Color.BLACK);

        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        // Дизайн заголовків
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);
                boolean isLast = (c == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 5));
        JLabel leftLabel = new JLabel("Усі маршрути");
        leftLabel.setFont(MAIN_FONT);
        leftPanel.add(leftLabel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Права таблиці з зупинками
        stopsTableModel = new DefaultTableModel(new String[]{"Порядок", "Назва зупинки"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        stopsTable = new JTable(stopsTableModel);
        stopsTable.setRowHeight(25);
        stopsTable.setFont(MAIN_FONT);
        stopsTable.setFocusable(false);
        stopsTable.setGridColor(Color.BLACK);
        stopsTable.getColumnModel().getColumn(0).setMinWidth(85);
        stopsTable.getColumnModel().getColumn(0).setMaxWidth(85);

        // Дизайн заголовків
        stopsTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);
                boolean isLast = (c == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        stopsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        JScrollPane stopsScrollPane = new JScrollPane(stopsTable);
        stopsScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        JLabel rightLabel = new JLabel("Зупинки вибраного маршруту");
        rightLabel.setFont(MAIN_FONT);
        rightPanel.add(rightLabel, BorderLayout.NORTH);
        rightPanel.add(stopsScrollPane, BorderLayout.CENTER);

        // Розміщення таблиць
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        tablesPanel.add(leftPanel);
        tablesPanel.add(rightPanel);
        add(tablesPanel, BorderLayout.CENTER);

        // Слухач вибору маршруту
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateStopsTable();
        });

        refreshTable();

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton stopsDictButton = new JButton("Довідник зупинок");
        stopsDictButton.setFont(MAIN_FONT);
        stopsDictButton.setFocusable(false);
        leftButtonPanel.add(stopsDictButton);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addButton = new JButton("Додати");
        addButton.setFont(MAIN_FONT);
        addButton.setFocusable(false);

        JButton editButton = new JButton("Редагувати");
        editButton.setFont(MAIN_FONT);
        editButton.setFocusable(false);

        JButton deleteButton = new JButton("Видалити");
        deleteButton.setFont(MAIN_FONT);
        deleteButton.setFocusable(false);

        rightButtonPanel.add(addButton);
        rightButtonPanel.add(editButton);
        rightButtonPanel.add(deleteButton);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        // Логіка кнопок
        stopsDictButton.addActionListener(e -> showStopsManagerDialog());

        addButton.addActionListener(e -> showRouteDialog(null));

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть маршрут для редагування.", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String number = (String) tableModel.getValueAt(selectedRow, 1);

            showRouteDialog(new Object[]{id, number}); // Передача через масив Objects :)
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть маршрут для видалення!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String number = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Ви дійсно бажаєте видалити маршрут №" + number + "?",
                    "Підтвердження", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                routeDAO.updateRouteStops(id, new ArrayList<>()); // Очищаємо зупинки перед видаленням
                if (routeDAO.deleteRoute(id)) {
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при видаленні!\nМожливо, маршрут використовується в розкладі.", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // Оновлення таблиці маршрутів
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Route r : routeDAO.getAllRoutes()) {
            tableModel.addRow(new Object[]{ r.getId(), r.getNumber() });
        }
        updateStopsTable();
    }

    // Оновлення таблиці зупинок
    private void updateStopsTable() {
        stopsTableModel.setRowCount(0);
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int routeId = (int) tableModel.getValueAt(selectedRow, 0);
            List<Stop> stops = routeDAO.getStopsByRouteId(routeId);
            int order = 1;
            for (Stop s : stops) {
                stopsTableModel.addRow(new Object[]{order++, s.getName()});
            }
        }
    }

    // Відкриття діалогу "Довідник зупинок"
    private void showStopsManagerDialog() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Довідник зупинок", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel stopTableModel = new DefaultTableModel(new String[]{"ID", "Назва зупинки"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable stopTable = new JTable(stopTableModel);
        stopTable.setRowHeight(25);
        stopTable.setFont(MAIN_FONT);
        stopTable.getColumnModel().getColumn(0).setMinWidth(45);
        stopTable.getColumnModel().getColumn(0).setMaxWidth(45);

        // Дизайн заголовків
        stopTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);
                boolean isLast = (c == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        stopTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasFoc, int r, int c) {
                super.getTableCellRendererComponent(t, v, isSel, hasFoc, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(stopTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        Runnable refreshStopTable = () -> {
            stopTableModel.setRowCount(0);
            for (Stop stop : routeDAO.getAllStops()) {
                stopTableModel.addRow(new Object[]{stop.getId(), stop.getName()});
            }
            updateStopsTable();
        };
        refreshStopTable.run();

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton addBtn = new JButton("Додати");
        JButton editBtn = new JButton("Редагувати");
        JButton deleteBtn = new JButton("Видалити");
        addBtn.setFont(MAIN_FONT);
        editBtn.setFont(MAIN_FONT);
        deleteBtn.setFont(MAIN_FONT);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);

        // Логіка кнопок довідника
        addBtn.addActionListener(e -> {
            JTextField nameField = new JTextField();
            if (JOptionPane.showConfirmDialog(dialog, new Object[]{"Назва зупинки:", nameField}, "Додати", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    if (routeDAO.addStop(newName)) refreshStopTable.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Заповніть назву!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editBtn.addActionListener(e -> {
            int row = stopTable.getSelectedRow();
            if (row == -1) return;

            int id = (int) stopTableModel.getValueAt(row, 0);
            JTextField nameField = new JTextField((String) stopTableModel.getValueAt(row, 1));

            if (JOptionPane.showConfirmDialog(dialog, new Object[]{"Назва зупинки:", nameField}, "Редагувати", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    if (routeDAO.updateStop(id, newName)) refreshStopTable.run();
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = stopTable.getSelectedRow();
            if (row == -1) return;

            int id = (int) stopTableModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(dialog, "Видалити зупинку?", "Підтвердження", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (routeDAO.deleteStop(id)) refreshStopTable.run();
                else JOptionPane.showMessageDialog(dialog, "Ця зупинка прив'язана до маршрутів.", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    // Відкриття діалогу "Редагування маршруту"
    private void showRouteDialog(Object[] data) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        boolean isEditMode = (data != null);
        String title = isEditMode ? "Редагувати маршрут" : "Додати маршрут";
        int routeId = isEditMode ? (int) data[0] : -1;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(750, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Верхній блок з назвою
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        String initialNumber = isEditMode ? (String) data[1] : "";

        JLabel numberLabel = new JLabel("Номер маршруту:");
        numberLabel.setFont(MAIN_FONT);
        JTextField numberField = new JTextField(initialNumber);
        numberField.setFont(MAIN_FONT);

        topPanel.add(numberLabel, BorderLayout.WEST);
        topPanel.add(numberField, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Блок зі списками зупинок
        JPanel listsContainer = new JPanel(new GridLayout(1, 2, 5, 0));

        DefaultListModel<Stop> availableModel = new DefaultListModel<>();
        DefaultListModel<Stop> currentRouteModel = new DefaultListModel<>();

        List<Stop> allStops = routeDAO.getAllStops();
        List<Stop> routeStops = isEditMode ? routeDAO.getStopsByRouteId(routeId) : new ArrayList<>();

        for (Stop stop : routeStops) {
            currentRouteModel.addElement(stop);
        }

        for (Stop stop : allStops) {
            boolean alreadyInRoute = false;
            for (Stop rs : routeStops) {
                if (rs.getId() == stop.getId()) {
                    alreadyInRoute = true;
                    break;
                }
            }
            if (!alreadyInRoute) availableModel.addElement(stop);
        }

        // Лівий список (Доступні)
        JList<Stop> availableList = new JList<>(availableModel);
        availableList.setFont(MAIN_FONT);
        availableList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                return label;
            }
        });
        JScrollPane availableScroll = new JScrollPane(availableList);
        availableScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Правий список (Зупинки маршруту)
        JList<Stop> routeList = new JList<>(currentRouteModel);
        routeList.setFont(MAIN_FONT);
        routeList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String numberedText = (index + 1) + ". " + value.toString();
                JLabel label = (JLabel) super.getListCellRendererComponent(list, numberedText, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                return label;
            }
        });
        JScrollPane routeScroll = new JScrollPane(routeList);
        routeScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Ліва сторона компоновки
        JPanel leftSide = new JPanel(new BorderLayout(0, 5));
        JLabel availLabel = new JLabel("Усі зупинки міста:");
        availLabel.setFont(MAIN_FONT);

        JPanel leftButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton btnAddStop = new JButton("Додати");
        btnAddStop.setFont(MAIN_FONT);
        leftButtonsPanel.add(btnAddStop);

        leftSide.add(availLabel, BorderLayout.NORTH);
        leftSide.add(availableScroll, BorderLayout.CENTER);
        leftSide.add(leftButtonsPanel, BorderLayout.SOUTH);

        // Права сторона компоновки
        JPanel rightSide = new JPanel(new BorderLayout(0, 5));
        JLabel routeLabel = new JLabel("Зупинки цього маршруту:");
        routeLabel.setFont(MAIN_FONT);

        JPanel rightButtonsPanel = new JPanel(new BorderLayout());
        JButton btnRemoveStop = new JButton("Видалити");
        btnRemoveStop.setFont(MAIN_FONT);

        JPanel orderButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton btnUp = new JButton("Вгору");
        JButton btnDown = new JButton("Вниз");
        btnUp.setFont(MAIN_FONT);
        btnDown.setFont(MAIN_FONT);
        orderButtonsPanel.add(btnUp);
        orderButtonsPanel.add(btnDown);

        rightButtonsPanel.add(btnRemoveStop, BorderLayout.WEST);
        rightButtonsPanel.add(orderButtonsPanel, BorderLayout.EAST);

        rightSide.add(routeLabel, BorderLayout.NORTH);
        rightSide.add(routeScroll, BorderLayout.CENTER);
        rightSide.add(rightButtonsPanel, BorderLayout.SOUTH);

        listsContainer.add(leftSide);
        listsContainer.add(rightSide);
        mainPanel.add(listsContainer, BorderLayout.CENTER);
        dialog.add(mainPanel, BorderLayout.CENTER);

        // Логіка кнопок списків
        btnAddStop.addActionListener(e -> {
            for (Stop s : availableList.getSelectedValuesList()) {
                availableModel.removeElement(s);
                currentRouteModel.addElement(s);
            }
        });

        btnRemoveStop.addActionListener(e -> {
            for (Stop s : routeList.getSelectedValuesList()) {
                currentRouteModel.removeElement(s);
                availableModel.addElement(s);
            }
        });

        btnUp.addActionListener(e -> {
            int index = routeList.getSelectedIndex();
            if (index > 0) {
                Stop stop = currentRouteModel.remove(index);
                currentRouteModel.insertElementAt(stop, index - 1);
                routeList.setSelectedIndex(index - 1);
            }
        });

        btnDown.addActionListener(e -> {
            int index = routeList.getSelectedIndex();
            if (index >= 0 && index < currentRouteModel.getSize() - 1) {
                Stop stop = currentRouteModel.remove(index);
                currentRouteModel.insertElementAt(stop, index + 1);
                routeList.setSelectedIndex(index + 1);
            }
        });

        // Нижня панель
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomActionPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JButton cancelButton = new JButton("Скасувати");
        JButton saveButton = new JButton("Зберегти");
        cancelButton.setFont(MAIN_FONT);
        saveButton.setFont(MAIN_FONT);
        bottomActionPanel.add(cancelButton);
        bottomActionPanel.add(saveButton);
        dialog.add(bottomActionPanel, BorderLayout.SOUTH);

        // Логіка збереження та виходу
        Runnable checkAndClose = () -> {
            String currentNumber = numberField.getText().trim();
            if (!currentNumber.equals(initialNumber)) {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Є незбережені зміни. Закрити вікно?", "Увага", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) dialog.dispose();
            } else {
                dialog.dispose();
            }
        };

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent windowEvent) { checkAndClose.run(); }
        });
        cancelButton.addActionListener(e -> checkAndClose.run());

        saveButton.addActionListener(e -> {
            String number = numberField.getText().trim();
            if (number.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введіть номер маршруту!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Stop> finalStops = new ArrayList<>();
            for (int i = 0; i < currentRouteModel.getSize(); i++) {
                finalStops.add(currentRouteModel.getElementAt(i));
            }

            if (finalStops.isEmpty()) {
                if (JOptionPane.showConfirmDialog(dialog, "Маршрут не містить жодної зупинки. Зберегти?", "Увага", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            if (isEditMode) {
                if (routeDAO.updateRoute(routeId, number)) {
                    routeDAO.updateRouteStops(routeId, finalStops);
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Помилка бази даних!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                int newId = routeDAO.addRouteAndGetId(number);
                if (newId != -1) {
                    routeDAO.updateRouteStops(newId, finalStops);
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Маршрут з таким номером вже існує.", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.setVisible(true);
    }
}