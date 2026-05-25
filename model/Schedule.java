package model;

import java.time.LocalDate;

public class Schedule {
    private int id;         // Унікальний ідентифікатор розкладу
    private LocalDate date; // Дата, на яку призначено розклад
    private String status;  // Статус розкладу (наприклад, чернетка, затверджено тощо)

    // Конструктор з параметрами
    public Schedule(int id, LocalDate date, String status) {
        this.id = id;
        this.date = date;
        this.status = status;
    }

    public Schedule() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStatus(String status) { this.status = status; }
}
