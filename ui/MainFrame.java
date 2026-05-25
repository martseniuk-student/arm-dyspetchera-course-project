package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        // Імплементація дизайну операційної системи
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Налаштування головного вікна
        setTitle("АІС «Міськтранс» - АРМ диспетчера");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Створення панелі вкладок та їх оформлення
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setFocusable(false);

        // Підключення класів панелей
        tabbedPane.addTab("Розклади", new SchedulePanel());
        tabbedPane.addTab("Маршрути", new RoutePanel());
        tabbedPane.addTab("Транспорт", new TransportPanel());
        tabbedPane.addTab("Персонал", new PersonnelPanel());
        tabbedPane.addTab("Квитки", new TicketPanel());
        tabbedPane.addTab("Звітність", new ui.ReportPanel());

        // Додавання панелей до вікна й відображення
        add(tabbedPane);
        setVisible(true);
    }
}
