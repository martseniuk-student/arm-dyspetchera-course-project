package ui;

import database.ReportDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportPanel extends JPanel {

    private final ReportDAO reportDAO;

    // Шрифти
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font MAIN_FONT  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT  = new Font("Segoe UI", Font.BOLD, 16);

    public ReportPanel() {
        reportDAO = new ReportDAO();
        setLayout(new BorderLayout());

        // Головний контейнер для всіх звітів
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1-й Звіт: аналіз виручки та пасажиропотоку
        DefaultTableModel revModel = new DefaultTableModel(new String[]{"Маршрут", "Кількість проданих квитків", "Загальна виручка (грн)"}, 0);
        JTable revTable = createReportTable(revModel);
        addReportSection(contentPanel, "Фінансовий аналіз виручки та пасажиропотоку за маршрутами", revTable, 120);

        // 2-й Звіт: популярність зупинок
        DefaultTableModel stopModel = new DefaultTableModel(new String[]{"Назва зупинки", "Кількість пасажирів"}, 0);
        JTable stopTable = createReportTable(stopModel);
        addReportSection(contentPanel, "Статистика пасажиропотоку за зупинками міста", stopTable, 140);

        // 3-й Звіт: транспорт у ремонті
        DefaultTableModel maintModel = new DefaultTableModel(new String[]{"Бортовий номер", "Модель транспорту", "Поточний технічний стан"}, 0);
        JTable maintTable = createReportTable(maintModel);
        addReportSection(contentPanel, "Моніторинг транспорту на технічному обслуговуванні", maintTable, 100);

        // 4-й Звіт: статистика типів оплати
        DefaultTableModel payModel = new DefaultTableModel(new String[]{"Тип оплати", "Кількість замовлень", "Загальна сума (грн)"}, 0);
        JTable payTable = createReportTable(payModel);
        addReportSection(contentPanel, "Розподіл фінансових надходжень за типами оплати", payTable, 100);

        // 5-й Звіт: маршрути та кількість зупинок
        DefaultTableModel routeStopsModel = new DefaultTableModel(new String[]{"Номер маршруту", "Загальна кількість зупинок"}, 0);
        JTable routeStopsTable = createReportTable(routeStopsModel);
        addReportSection(contentPanel, "Аналіз щільності маршрутної мережі (кількість зупинок)", routeStopsTable, 100);

        // 6-й Звіт: загальна пасажиромісткість
        JPanel capacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        capacityPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Сумарна потужність депо", 0, 0, TITLE_FONT));

        JLabel capacityLabel = new JLabel("Загальна пасажиромісткість активного транспорту: " + reportDAO.getTotalActiveCapacity() + " місць.");
        capacityLabel.setFont(MAIN_FONT);
        capacityPanel.add(capacityLabel);

        contentPanel.add(capacityPanel);

        // Додавання скролу
        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setBorder(null);
        add(mainScrollPane, BorderLayout.CENTER);

        // Панель з кнопкою оновлення звітів
        JButton refreshBtn = new JButton("Оновити всі звіти");
        refreshBtn.setFont(MAIN_FONT);
        refreshBtn.setFocusable(false);

        refreshBtn.addActionListener(e -> {
            loadData(revModel, reportDAO.getRouteRevenueReport());
            loadData(stopModel, reportDAO.getStopPopularityReport());
            loadData(maintModel, reportDAO.getTransportInMaintenanceReport());
            loadData(payModel, reportDAO.getPaymentTypeReport());
            loadData(routeStopsModel, reportDAO.getRouteStopCountReport());
            capacityLabel.setText("Загальна пасажиромісткість активного транспорту: " + reportDAO.getTotalActiveCapacity() + " місць.");
        });

        JPanel bottomBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBtnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bottomBtnPanel.add(refreshBtn);

        add(bottomBtnPanel, BorderLayout.SOUTH);

        refreshBtn.getActionListeners()[0].actionPerformed(null);
    }

    // Допоміжні методи
    // Створення таблиці звіту
    private JTable createReportTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setRowHeight(25);
        table.setFont(MAIN_FONT);
        table.setFocusable(false);
        table.setGridColor(Color.BLACK);
        table.setShowGrid(true);

        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBackground(Color.WHITE);
                setFont(BOLD_FONT);
                setHorizontalAlignment(SwingConstants.LEFT);

                boolean isLast = (column == t.getColumnCount() - 1);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, isLast ? 0 : 1, Color.BLACK),
                        BorderFactory.createEmptyBorder(5, 5, 5, 0)));
                return this;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return this;
            }
        });

        return table;
    }

    // Додавання секції звіту
    private void addReportSection(JPanel container, String title, JTable table, int preferHeight) {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 5));

        JLabel label = new JLabel(title);
        label.setFont(TITLE_FONT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width, preferHeight));

        sectionPanel.add(label, BorderLayout.NORTH);
        sectionPanel.add(scroll, BorderLayout.CENTER);

        container.add(sectionPanel);
        container.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    // Завантаження даних
    private void loadData(DefaultTableModel model, List<Object[]> data) {
        model.setRowCount(0);
        for (Object[] row : data) {
            model.addRow(row);
        }
    }
}