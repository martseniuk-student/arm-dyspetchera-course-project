package ui;

import database.ScheduleDAO;
import database.TicketDAO;
import model.ScheduleEntry;
import model.Stop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TicketPanel extends JPanel {

    private final TicketDAO ticketDAO;
    private final ScheduleDAO scheduleDAO;

    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);

    // Стандартна ціна квитка для генерації
    private static final double STANDARD_TICKET_PRICE = 15.00;

    public TicketPanel() {
        // Ініціалізація DAO
        ticketDAO = new TicketDAO();
        scheduleDAO = new ScheduleDAO();

        // Встановлення лейауту
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Таблиця проданих квитків
        String[] columns = {"ID", "Маршрут", "Відправлення", "Зупинка призначення", "Тип оплати", "Вартість (грн)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        applyTableStyle(table);
        table.getColumnModel().getColumn(0).setMinWidth(45);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        // Дизайн заголовків головної таблиці
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 16));
                setHorizontalAlignment(SwingConstants.LEFT);

                boolean isLast = (column == table.getColumnCount() - 1);
                javax.swing.border.Border matte = BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK);
                javax.swing.border.Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 0);

                setBorder(BorderFactory.createCompoundBorder(matte, padding));
                return this;
            }
        });

        // Дизайн звичайних комірок головної таблиці
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createCompoundBorder(getBorder(), BorderFactory.createEmptyBorder(0, 5, 0, 0)));
                return this;
            }
        });

        // Створення скролу для таблиці
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Обгортання таблиці в панель
        JPanel tableWrap = new JPanel(new BorderLayout(0, 5));
        JLabel titleLabel = new JLabel("Історія проданих квитків:");
        titleLabel.setFont(MAIN_FONT);
        tableWrap.add(titleLabel, BorderLayout.NORTH);
        tableWrap.add(scrollPane, BorderLayout.CENTER);

        add(tableWrap, BorderLayout.CENTER);

        // Оновлення даних
        refreshTable();

        // Панель кнопок внизу
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Кнопка генерації
        JButton generateButton = new JButton("Згенерувати квитки");
        generateButton.setFont(MAIN_FONT);
        generateButton.setFocusable(false);

        buttonPanel.add(generateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Логіка кнопки
        generateButton.addActionListener(e -> showGenerateDialog());
    }

    // Допоміжні методи
    // Метод для стилізації таблиць
    private void applyTableStyle(JTable targetTable) {
        targetTable.setRowHeight(25);
        targetTable.setFont(MAIN_FONT);
        targetTable.setShowGrid(true);
        targetTable.setGridColor(Color.BLACK);

        targetTable.getTableHeader().setFont(BOLD_FONT);
        targetTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBackground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.LEFT);
                boolean isLast = (column == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        targetTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });
    }

    // Метод для оновлення даних в таблиці
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Object[]> data = ticketDAO.getAllTicketsData();
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    // Діалогове вікно генерації квитків
    private void showGenerateDialog() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // Налаштування самого вікна
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Генерація квитків", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Отримання активних рейсів на сьогодні
        List<ScheduleEntry> todayEntries = scheduleDAO.getSchedulesByDate(LocalDate.now());
        if (todayEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "На сьогодні немає жодного рейсу!\nСпочатку згенеруйте розклад.", "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Вибір рейсу
        JLabel entryLabel = new JLabel("Оберіть рейс:");
        entryLabel.setFont(MAIN_FONT);
        entryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<ScheduleEntry> entryCombo = new JComboBox<>();
        entryCombo.setFont(MAIN_FONT);
        entryCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        entryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Кастомний рендер для комбобоксу
        entryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ScheduleEntry se) {
                    String time = se.getDepartureTime() != null ? se.getDepartureTime().toString().substring(0, 5) : "";
                    String route = se.getRoute() != null ? se.getRoute().getNumber() : "";
                    setText("Маршрут " + route + " (Відправлення: " + time + ")");
                }
                return this;
            }
        });
        for (ScheduleEntry se : todayEntries) entryCombo.addItem(se);

        // Кількість квитків
        JLabel countLabel = new JLabel("Кількість квитків:");
        countLabel.setFont(MAIN_FONT);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Значення за замовчуванням
        JTextField countField = new JTextField("10");
        countField.setFont(MAIN_FONT);
        countField.setAlignmentX(Component.LEFT_ALIGNMENT);
        countField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Кінцева зупинка
        JLabel stopLabel = new JLabel("Кінцева зупинка:");
        stopLabel.setFont(MAIN_FONT);
        stopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<Stop> stopCombo = new JComboBox<>();
        stopCombo.setFont(MAIN_FONT);
        stopCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        stopCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Логіка автоматичного завантаження зупинок
        entryCombo.addActionListener(e -> {
            stopCombo.removeAllItems();
            stopCombo.addItem(new Stop(-1, "--- Випадкові зупинки ---"));

            ScheduleEntry selected = (ScheduleEntry) entryCombo.getSelectedItem();
            if (selected != null && selected.getRoute() != null) {
                List<Stop> routeStops = ticketDAO.getStopsByRoute(selected.getRoute().getId());
                for (Stop s : routeStops) stopCombo.addItem(s);
            }
        });
        entryCombo.getActionListeners()[0].actionPerformed(null);

        // Тип оплати
        JLabel paymentLabel = new JLabel("Тип оплати:");
        paymentLabel.setFont(MAIN_FONT);
        paymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<String> paymentCombo = new JComboBox<>(new String[]{"Випадковий", "Безготівкова", "Готівка"});
        paymentCombo.setFont(MAIN_FONT);
        paymentCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Додавання всіх елементів на панель форми
        formPanel.add(entryLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(entryCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(countLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(countField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(stopLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(stopCombo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(paymentLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(paymentCombo);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Панель для кнопок скасування і генерації
        JPanel dialogBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dialogBtnPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        JButton btnCancel = new JButton("Скасувати");
        btnCancel.setFont(MAIN_FONT);
        btnCancel.setFocusable(false);

        JButton btnGenerate = new JButton("Згенерувати");
        btnGenerate.setFont(MAIN_FONT);
        btnGenerate.setFocusable(false);

        dialogBtnPanel.add(btnCancel);
        dialogBtnPanel.add(btnGenerate);
        dialog.add(dialogBtnPanel, BorderLayout.SOUTH);

        // Логіка закриття вікна
        btnCancel.addActionListener(e -> dialog.dispose());

        // Логіка генерації
        btnGenerate.addActionListener(e -> {
            ScheduleEntry selectedEntry = (ScheduleEntry) entryCombo.getSelectedItem();
            Stop selectedStop = (Stop) stopCombo.getSelectedItem();
            String paymentType = (String) paymentCombo.getSelectedItem();

            // Логіка випадкової оплати
            if ("Випадковий".equals(paymentType)) {
                paymentType = new java.util.Random().nextBoolean() ? "Безготівкова" : "Готівка";
            }

            int count;

            // Парсинг кількості квитків
            try {
                count = Integer.parseInt(countField.getText().trim());
                if (count <= 0 || count > 200) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введіть коректну кількість квитків (від 1 до 200)!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Перевірка чи вибрані рейс і зупинка
            if (selectedEntry == null || selectedStop == null) {
                JOptionPane.showMessageDialog(dialog, "Оберіть рейс та зупинку!", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int routeId = selectedEntry.getRoute().getId();

            // Генерація через DAO
            boolean success = ticketDAO.generateTickets(selectedEntry.getId(), routeId, count, selectedStop.getId(), paymentType, STANDARD_TICKET_PRICE);

            if (success) {
                // Успішна генерація
                dialog.dispose();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Успішно згенеровано " + count + " квитків!\nТип оплати: " + paymentType, "Успіх", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Помилка! Можливо, для цього маршруту ще не додано зупинок.", "Помилка БД", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}