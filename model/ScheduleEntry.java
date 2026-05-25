package model;

import java.time.LocalTime;
import java.util.List;

public class ScheduleEntry {
    private int id;                                         // Унікальний ідентифікатор рейсу
    private LocalTime departureTime;                        // Час відправлення

    private Route route;                                    // Посилання на об'єкт маршруту
    private Transport transport;                            // Посилання на об'єкт транспорту
    private Schedule schedule;                              // Посилання на об'єкт розкладу

    private List<PersonnelAssignment> personalAssignments;  // Перелік призначеного персоналу

    // Конструктор з параметрами
    public ScheduleEntry(int id, LocalTime departureTime, Route route, Transport transport,
                         Schedule schedule, List<PersonnelAssignment> personalAssignments) {
        this.id = id;
        this.departureTime = departureTime;
        this.route = route;
        this.transport = transport;
        this.schedule = schedule;
        this.personalAssignments = personalAssignments;
    }

    public ScheduleEntry() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public LocalTime getDepartureTime() { return departureTime; }
    public Route getRoute() { return route; }
    public Transport getTransport() { return transport; }
    public Schedule getSchedule() { return schedule; }
    public List<PersonnelAssignment> getPersonalAssignments() { return personalAssignments; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    public void setRoute(Route route) { this.route = route; }
    public void setTransport(Transport transport) { this.transport = transport; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    public void setPersonalAssignments(List<PersonnelAssignment> personalAssignments) { this.personalAssignments = personalAssignments; }
}
