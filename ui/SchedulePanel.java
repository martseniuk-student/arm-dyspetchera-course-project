package ui;

import controller.ScheduleController;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SchedulePanel extends JPanel {

    private final ScheduleController controller;

    private final DefaultTableModel scheduleTableModel;
    private final DefaultTableModel crewTableModel;
    private final JTable scheduleTable;
    private final JTable crewTable;

    private LocalDate currentDate;
    private final JLabel dateLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);

    public SchedulePanel() {
        controller = new ScheduleController();
        currentDate = LocalDate.now();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Верхня панель з датою
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        JButton prevDayButton = new JButton("< Попередній день");
        prevDayButton.setFont(MAIN_FONT);
        prevDayButton.setFocusable(false);

        JButton nextDayButton = new JButton("Наступний день >");
        nextDayButton.setFont(MAIN_FONT);
        nextDayButton.setFocusable(false);

        dateLabel = new JLabel("Розклад на: " + currentDate.format(DATE_FORMATTER), SwingConstants.CENTER);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        topPanel.add(prevDayButton, BorderLayout.WEST);
        topPanel.add(dateLabel, BorderLayout.CENTER);
        topPanel.add(nextDayButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        prevDayButton.addActionListener(e -> { currentDate = currentDate.minusDays(1); updateDateView(); });
        nextDayButton.addActionListener(e -> { currentDate = currentDate.plusDays(1); updateDateView(); });

        // Таблиця рейсів
        scheduleTableModel = new DefaultTableModel(new String[]{"ID", "Відправлення", "Маршрут", "Транспорт", "Місткість", "Статус"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        scheduleTable = new JTable(scheduleTableModel);
        applyTableStyle(scheduleTable);
        scheduleTable.getColumnModel().getColumn(0).setMaxWidth(50);
        scheduleTable.getColumnModel().getColumn(1).setMinWidth(120);
        scheduleTable.getColumnModel().getColumn(1).setMaxWidth(120);

        JScrollPane scheduleScroll = new JScrollPane(scheduleTable);
        scheduleScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel scheduleWrap = new JPanel(new BorderLayout(0, 5));
        JLabel scheduleLabel = new JLabel("Розклад рейсів:");
        scheduleLabel.setFont(MAIN_FONT);
        scheduleWrap.add(scheduleLabel, BorderLayout.NORTH);
        scheduleWrap.add(scheduleScroll, BorderLayout.CENTER);

        // Таблиця екіпажу
        crewTableModel = new DefaultTableModel(new String[]{"ID", "ПІБ", "Посада"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        crewTable = new JTable(crewTableModel);
        applyTableStyle(crewTable);
        crewTable.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane crewScroll = new JScrollPane(crewTable);
        crewScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel crewWrap = new JPanel(new BorderLayout(0, 5));
        JLabel crewLabel = new JLabel("Екіпаж обраного рейсу:");
        crewLabel.setFont(MAIN_FONT);
        crewWrap.add(crewLabel, BorderLayout.NORTH);
        crewWrap.add(crewScroll, BorderLayout.CENTER);

        // Спліт між таблицями
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scheduleWrap, crewWrap);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(0.6);
        add(splitPane, BorderLayout.CENTER);

        scheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) refreshCrewTable();
        });

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel leftBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton autoBtn = new JButton("Автопланування");
        JButton approveBtn = new JButton("Затвердити розклад");
        autoBtn.setFont(MAIN_FONT); autoBtn.setFocusable(false);
        approveBtn.setFont(MAIN_FONT); approveBtn.setFocusable(false);
        leftBtn.add(autoBtn);
        leftBtn.add(approveBtn);

        JPanel rightBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addBtn = new JButton("Додати");
        JButton editBtn = new JButton("Редагувати");
        JButton delBtn = new JButton("Видалити");

        for (JButton b : new JButton[]{addBtn, editBtn, delBtn}) {
            b.setFont(MAIN_FONT);
            b.setFocusable(false);
            rightBtn.add(b);
        }

        buttonPanel.add(leftBtn, BorderLayout.WEST);
        buttonPanel.add(rightBtn, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        // Логіка кнопок
        addBtn.addActionListener(e -> showScheduleDialog(null));

        editBtn.addActionListener(e -> {
            int row = scheduleTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Оберіть рейс!", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showScheduleDialog((int) scheduleTableModel.getValueAt(row, 0));
        });

        delBtn.addActionListener(e -> {
            int row = scheduleTable.getSelectedRow();
            if (row != -1) {
                int id = (int) scheduleTableModel.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Видалити рейс?", "Підтвердження", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    controller.deleteSchedule(id);
                    refreshScheduleTable();
                }
            }
        });

        autoBtn.addActionListener(e -> {
            if (scheduleTableModel.getRowCount() > 0) {
                if (JOptionPane.showConfirmDialog(this, "Видалити існуючий розклад на цю дату?", "Увага", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                for (ScheduleEntry en : controller.getSchedulesByDate(currentDate)) {
                    controller.deleteSchedule(en.getId());
                }
            }
            int count = controller.autoGenerateSchedule(currentDate);
            if (count > 0) {
                refreshScheduleTable();
                JOptionPane.showMessageDialog(this, "Створено " + count + " рейсів!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Недостатньо ресурсів (водіїв, транспорту або маршрутів).", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        approveBtn.addActionListener(e -> {
            if (scheduleTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "На " + currentDate.format(DATE_FORMATTER) + " немає жодного рейсу для затвердження.", "Увага", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Ви дійсно бажаєте затвердити розклад на " + currentDate.format(DATE_FORMATTER) + "?\nУсі рейси отримають статус «Активний».",
                    "Підтвердження", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (controller.approveSchedule(currentDate)) {
                    refreshScheduleTable();
                    JOptionPane.showMessageDialog(this, "Розклад успішно затверджено!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Помилка при затвердженні розкладу!", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshScheduleTable();
    }

    // Допоміжні методи
    // Оновлення дати у заголовку
    private void updateDateView() {
        dateLabel.setText("Розклад на: " + currentDate.format(DATE_FORMATTER));
        refreshScheduleTable();
    }

    // Використання стилю до таблиць
    private void applyTableStyle(JTable table) {
        table.setRowHeight(25);
        table.setFont(MAIN_FONT);
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);

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
    }

    // Оновлення таблиці розкладу
    private void refreshScheduleTable() {
        scheduleTableModel.setRowCount(0);
        for (ScheduleEntry s : controller.getSchedulesByDate(currentDate)) {

            String statusDb = s.getSchedule() != null ? s.getSchedule().getStatus() : "";
            String statusUkr = statusDb;

            // Перекладаємо статус
            if ("Active".equals(statusDb)) {
                statusUkr = "Активний";
            } else if ("Draft".equals(statusDb)) {
                statusUkr = "Чернетка";
            }

            int capacity = (s.getTransport() != null && s.getTransport().getType() != null)
                    ? s.getTransport().getType().getCapacity() : 0;

            String typeStr = (s.getTransport() != null && s.getTransport().getType() != null)
                    ? s.getTransport().getType().getName() + " [" + s.getTransport().getBoardNumber() + "]" : "—";

            scheduleTableModel.addRow(new Object[]{
                    s.getId(),
                    s.getDepartureTime() != null ? s.getDepartureTime().toString().substring(0, 5) : "—",
                    s.getRoute() != null ? s.getRoute().getNumber() : "—",
                    typeStr,
                    capacity,
                    statusUkr
            });
        }
        refreshCrewTable();
    }

    // Оновлення таблиці екіпажу
    private void refreshCrewTable() {
        crewTableModel.setRowCount(0);
        int row = scheduleTable.getSelectedRow();
        if (row != -1) {
            int entryId = (int) scheduleTableModel.getValueAt(row, 0);

            // Типовий O(N^2) студентський підхід: замість того, щоб взяти об'єкт з моделі, знову перебираємо весь розклад :)
            for (ScheduleEntry s : controller.getSchedulesByDate(currentDate)) {
                if (s.getId() == entryId && s.getPersonalAssignments() != null) {
                    for (PersonnelAssignment pa : s.getPersonalAssignments()) {
                        Personnel p = pa.getPersonnel();
                        if (p != null) {
                            String role = p.getRole() != null ? p.getRole().getName() : "—";
                            crewTableModel.addRow(new Object[]{p.getId(), p.getFullName(), role});
                        }
                    }
                    break;
                }
            }
        }
    }

    // Відкриття діалогу "Додавання/Редагування рейсу"
    private void showScheduleDialog(Integer entryId) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        boolean isEditMode = (entryId != null);
        String title = isEditMode ? "Редагувати рейс" : "Додати рейс у розклад";

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(750, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<Route> routes = controller.getAllRoutes();
        List<Transport> transports = controller.getFunctionalTransports();
        List<Personnel> allActiveStaff = controller.getActivePersonnelByRoles(List.of("Водій", "Кондуктор"));

        ScheduleEntry currentEntry = null;
        if (isEditMode) {
            for (ScheduleEntry e : controller.getSchedulesByDate(currentDate)) {
                if (e.getId() == entryId) {
                    currentEntry = e;
                    break;
                }
            }
        }

        // Верхня панель
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel timeLabel = new JLabel("Час відправлення (ГГ:ХХ):");
        timeLabel.setFont(MAIN_FONT);
        topPanel.add(timeLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        String initTime = (isEditMode && currentEntry != null && currentEntry.getDepartureTime() != null)
                ? currentEntry.getDepartureTime().toString().substring(0, 5) : "";
        JTextField timeField = new JTextField(initTime);
        timeField.setFont(MAIN_FONT);
        topPanel.add(timeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel routeLabel = new JLabel("Маршрут:");
        routeLabel.setFont(MAIN_FONT);
        topPanel.add(routeLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<Route> routeCombo = new JComboBox<>();
        routeCombo.setFont(MAIN_FONT);
        for (Route r : routes) {
            routeCombo.addItem(r);
            if (isEditMode && currentEntry != null && currentEntry.getRoute() != null && currentEntry.getRoute().getId() == r.getId()) {
                routeCombo.setSelectedItem(r);
            }
        }
        topPanel.add(routeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel transportLabel = new JLabel("Транспорт:");
        transportLabel.setFont(MAIN_FONT);
        topPanel.add(transportLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<Transport> transportCombo = new JComboBox<>();
        transportCombo.setFont(MAIN_FONT);
        transportCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Transport t) {
                    String type = t.getType() != null ? t.getType().getName() : "—";
                    setText(type + " [" + t.getBoardNumber() + "]");
                }
                return this;
            }
        });

        for (Transport t : transports) {
            transportCombo.addItem(t);
            if (isEditMode && currentEntry != null && currentEntry.getTransport() != null && currentEntry.getTransport().getId() == t.getId()) {
                transportCombo.setSelectedItem(t);
            }
        }
        topPanel.add(transportCombo, gbc);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Нижня панель з персоналом
        JPanel listsContainer = new JPanel(new GridLayout(1, 2, 10, 0));

        DefaultListModel<Personnel> availableModel = new DefaultListModel<>();
        DefaultListModel<Personnel> assignedModel = new DefaultListModel<>();

        if (isEditMode && currentEntry != null && currentEntry.getPersonalAssignments() != null) {
            for (PersonnelAssignment pa : currentEntry.getPersonalAssignments()) {
                if (pa.getPersonnel() != null) assignedModel.addElement(pa.getPersonnel());
            }
        }

        for (Personnel p : allActiveStaff) {
            boolean alreadyAssigned = false;
            for (int i = 0; i < assignedModel.getSize(); i++) {
                if (assignedModel.getElementAt(i).getId() == p.getId()) {
                    alreadyAssigned = true;
                    break;
                }
            }
            if (!alreadyAssigned) availableModel.addElement(p);
        }

        DefaultListCellRenderer staffRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Personnel p) {
                    String role = (p.getRole() != null) ? " (" + p.getRole().getName() + ")" : "";
                    label.setText(p.getFullName() + role);
                }
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                return label;
            }
        };

        // Лівий список (Доступні)
        JList<Personnel> availableList = new JList<>(availableModel);
        availableList.setFont(MAIN_FONT);
        availableList.setCellRenderer(staffRenderer);
        JScrollPane availableScroll = new JScrollPane(availableList);
        availableScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel leftSide = new JPanel(new BorderLayout(0, 5));
        JLabel availLabel = new JLabel("Доступний персонал:");
        availLabel.setFont(MAIN_FONT);

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        leftBtns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        JButton btnAdd = new JButton("Додати >");
        btnAdd.setFont(MAIN_FONT);
        btnAdd.setFocusable(false);
        leftBtns.add(btnAdd);

        leftSide.add(availLabel, BorderLayout.NORTH);
        leftSide.add(availableScroll, BorderLayout.CENTER);
        leftSide.add(leftBtns, BorderLayout.SOUTH);

        // Правий список (Призначені)
        JList<Personnel> assignedList = new JList<>(assignedModel);
        assignedList.setFont(MAIN_FONT);
        assignedList.setCellRenderer(staffRenderer);
        JScrollPane assignedScroll = new JScrollPane(assignedList);
        assignedScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JPanel rightSide = new JPanel(new BorderLayout(0, 5));
        JLabel assignLabel = new JLabel("Призначений екіпаж:");
        assignLabel.setFont(MAIN_FONT);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rightBtns.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        JButton btnRemove = new JButton("< Видалити");
        btnRemove.setFont(MAIN_FONT);
        btnRemove.setFocusable(false);
        rightBtns.add(btnRemove);

        rightSide.add(assignLabel, BorderLayout.NORTH);
        rightSide.add(assignedScroll, BorderLayout.CENTER);
        rightSide.add(rightBtns, BorderLayout.SOUTH);

        listsContainer.add(leftSide);
        listsContainer.add(rightSide);
        mainPanel.add(listsContainer, BorderLayout.CENTER);

        // Логіка кнопок списків
        btnAdd.addActionListener(e -> {
            for (Personnel p : availableList.getSelectedValuesList()) {
                availableModel.removeElement(p);
                assignedModel.addElement(p);
            }
        });

        btnRemove.addActionListener(e -> {
            for (Personnel p : assignedList.getSelectedValuesList()) {
                assignedModel.removeElement(p);
                availableModel.addElement(p);
            }
        });

        dialog.add(mainPanel, BorderLayout.CENTER);

        // Нижня панель: кнопки "Скасувати", "Зберегти"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        JButton btnCancel = new JButton("Скасувати");
        btnCancel.setFont(MAIN_FONT);
        btnCancel.setFocusable(false);

        JButton btnSave = new JButton("Зберегти");
        btnSave.setFont(MAIN_FONT);
        btnSave.setFocusable(false);

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSave);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String timeText = timeField.getText().trim();

            if (!timeText.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                JOptionPane.showMessageDialog(dialog, "Введіть час у форматі ГГ:ХХ (наприклад, 14:30)!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Route selectedRoute = (Route) routeCombo.getSelectedItem();
            Transport selectedTransport = (Transport) transportCombo.getSelectedItem();

            if (selectedRoute == null || selectedTransport == null) {
                JOptionPane.showMessageDialog(dialog, "Оберіть маршрут та транспорт!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Integer> personnelIds = new ArrayList<>();
            for (int i = 0; i < assignedModel.getSize(); i++) {
                personnelIds.add(assignedModel.getElementAt(i).getId());
            }

            if (personnelIds.isEmpty()) {
                if (JOptionPane.showConfirmDialog(dialog, "Зберегти рейс без екіпажу?", "Увага", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            boolean success = isEditMode
                    ? controller.updateSchedule(entryId, selectedRoute.getId(), selectedTransport.getId(), personnelIds, timeText)
                    : controller.addSchedule(currentDate, selectedRoute.getId(), selectedTransport.getId(), personnelIds, timeText);

            if (success) {
                dialog.dispose();
                refreshScheduleTable();
            } else {
                JOptionPane.showMessageDialog(dialog, "Помилка збереження в базі даних!", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}