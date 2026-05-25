package model;

import java.util.List;

public class Route {
    private int id;                 // Унікальний ідентифікатор маршруту
    private String number;          // Номер маршруту

    private List<Stop> stopList;    // Список зупинок

    // Конструктор з параметрами
    public Route(int id, String number, List<Stop> stopList) {
        this.id = id;
        this.number = number;
        this.stopList = stopList;
    }

    // Конструктор з головними параметрами
    public Route(int id, String number) {
        this.id = id;
        this.number = number;
    }

    public Route() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public String getNumber() { return number; }
    public List<Stop> getStopList() { return stopList; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setNumber(String number) { this.number = number; }
    public void setStopList(List<Stop> stopList) { this.stopList = stopList; }

    @Override
    public String toString() {
        return number;
    }
}
