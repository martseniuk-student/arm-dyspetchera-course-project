package test;

import controller.ScheduleController;
import model.Transport;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScheduleMethodTestDriver {
    public static void main(String[] args) {
        System.out.println("Запуск тест-драйвера");

        // Створюємо контролер і перевизначаємо метод доступу до транспорту
        ScheduleController controller = new ScheduleController() {
            @Override
            public List<Transport> getFunctionalTransports() {
                return new ArrayList<>(); // Повертаємо 0 одиниць транспорту
            }
        };

        LocalDate testDate = LocalDate.of(2026, 6, 1);

        // Виклик методу, внутрішню логіку якого перевіряємо
        int result = controller.autoGenerateSchedule(testDate);

        // Верифікація результату
        if (result == -1) {
            System.out.println("Схвалено: метод виявив дефіцит техніки та заблокував генерацію.");
        } else {
            System.err.println("Провалено: метод пропустив невалідний стан системи.");
        }
    }
}
