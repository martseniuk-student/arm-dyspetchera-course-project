package controller;

import database.PersonnelDAO;
import database.RouteDAO;
import database.ScheduleDAO;
import database.TransportDAO;
import model.Personnel;
import model.Route;
import model.ScheduleEntry;
import model.Transport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ScheduleController {

    private final ScheduleDAO scheduleDAO;
    private final RouteDAO routeDAO;
    private final TransportDAO transportDAO;
    private final PersonnelDAO personnelDAO;

    public ScheduleController() {
        this.scheduleDAO = new ScheduleDAO();
        this.routeDAO = new RouteDAO();
        this.transportDAO = new TransportDAO();
        this.personnelDAO = new PersonnelDAO();
    }

    // Операції читання
    // Витягує розклад на конкретний день
    public List<ScheduleEntry> getSchedulesByDate(LocalDate date) {
        return scheduleDAO.getSchedulesByDate(date);
    }

    // Витягує всі маршрути
    public List<Route> getAllRoutes() {
        return routeDAO.getAllRoutes();
    }

    // Отримує весь транспорт і відфільтровує лише активний
    public List<Transport> getFunctionalTransports() {
        List<Transport> active = new ArrayList<>();
        for (Transport t : transportDAO.getAllTransport()) {
            if ("Functional".equalsIgnoreCase(t.getStatus())) {
                active.add(t);
            }
        }
        return active;
    }

    // Отримує весь персонал і відфільтровує активних за списком ролей
    public List<Personnel> getActivePersonnelByRoles(List<String> allowedRoles) {
        List<Personnel> active = new ArrayList<>();
        for (Personnel p : personnelDAO.getAllPersonnel()) {
            if ("Active".equalsIgnoreCase(p.getStatus()) && p.getRole() != null) {
                // Мікс класичного циклу та Stream API для солідності
                if (allowedRoles.stream().anyMatch(r -> r.equalsIgnoreCase(p.getRole().getName()))) {
                    active.add(p);
                }
            }
        }
        return active;
    }

    // Операції зміни розкладу
    // Додати розклад
    public boolean addSchedule(LocalDate date, int routeId, int transportId, List<Integer> personnelIds, String time) {
        return scheduleDAO.addSchedule(date, routeId, transportId, personnelIds, time);
    }

    // Оновити розклад
    public boolean updateSchedule(int entryId, int routeId, int transportId, List<Integer> personnelIds, String time) {
        return scheduleDAO.updateSchedule(entryId, routeId, transportId, personnelIds, time);
    }

    // Видалити розклад
    public boolean deleteSchedule(int entryId) {
        return scheduleDAO.deleteSchedule(entryId);
    }

    // Підтвердити розклад
    public boolean approveSchedule(LocalDate date) {
        return scheduleDAO.approveScheduleForDate(date);
    }

    // Автоматичне планування
    // Повертає кількість створених рейсів або -1 у випадку нестачі ресурсів
    public int autoGenerateSchedule(LocalDate date) {
        List<Route> routes = getAllRoutes();
        List<Transport> transports = getFunctionalTransports();

        Queue<Personnel> drivers = new LinkedList<>();
        Queue<Personnel> conductors = new LinkedList<>();

        for (Personnel p : getActivePersonnelByRoles(List.of("Водій", "Кондуктор"))) {
            if (p.getRole().getName().toLowerCase().contains("водій")) {
                drivers.add(p);
            } else {
                conductors.add(p);
            }
        }

        if (routes.isEmpty() || transports.isEmpty() || drivers.isEmpty()) {
            return -1;
        }

        int createdCount = 0;
        int transportIdx = 0;

        List<Personnel> dList = new ArrayList<>(drivers);
        List<Personnel> cList = new ArrayList<>(conductors);

        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);

            // Беремо транспорт по колу
            Transport t1 = transports.get(transportIdx % transports.size());
            transportIdx++;
            Transport t2 = transports.get(transportIdx % transports.size());
            transportIdx++;

            // Числа для генерації часу: 360 = 06:00, 840 = 14:00
            // Кожен наступний маршрут +30 хв
            int forwardMinutes = 360 + i * 30;
            int returnMinutes = 840 + i * 30;

            String fTime = String.format("%02d:%02d", forwardMinutes / 60, forwardMinutes % 60);
            String rTime = String.format("%02d:%02d", returnMinutes / 60, returnMinutes % 60);

            // Формуємо екіпаж для першого рейсу
            List<Integer> crew1 = new ArrayList<>();
            crew1.add(dList.get((i * 2) % dList.size()).getId());
            if (!cList.isEmpty()) {
                crew1.add(cList.get((i * 2) % cList.size()).getId());
            }

            // Формуємо екіпаж для другого рейсу
            List<Integer> crew2 = new ArrayList<>();
            crew2.add(dList.get((i * 2 + 1) % dList.size()).getId());
            if (!cList.isEmpty()) {
                crew2.add(cList.get((i * 2 + 1) % cList.size()).getId());
            }

            // Зберігаємо в базу
            if (scheduleDAO.addSchedule(date, route.getId(), t1.getId(), crew1, fTime)) {
                createdCount++;
            }
            if (scheduleDAO.addSchedule(date, route.getId(), t2.getId(), crew2, rTime)) {
                createdCount++;
            }
        }

        return createdCount;
    }
}
