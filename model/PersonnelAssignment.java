package model;

import java.util.Date;

public class PersonnelAssignment {
    private int id;                         // Унікальний ідентифікатор призначення персоналу
    private Date assignmentTime;            // Дата і час призначення персоналу

    private Personnel personnel;            // Посилання на персонал
    private ScheduleEntry scheduleEntry;    // Посилання на рейс

    // Конструктор з параметрами
    public PersonnelAssignment(int id, Date assignmentTime, Personnel personnel, ScheduleEntry scheduleEntry) {
        this.id = id;
        this.assignmentTime = assignmentTime;
        this.personnel = personnel;
        this.scheduleEntry = scheduleEntry;
    }

    public PersonnelAssignment() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public Date getAssignmentTime() { return assignmentTime; }
    public Personnel getPersonnel() { return personnel ; }
    public ScheduleEntry getScheduleEntry() { return scheduleEntry; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setAssignmentTime(Date assignmentTime) { this.assignmentTime = assignmentTime; }
    public void setPersonnel(Personnel personnel) { this.personnel = personnel; }
    public void setScheduleEntry(ScheduleEntry scheduleEntry) { this.scheduleEntry = scheduleEntry; }
}
