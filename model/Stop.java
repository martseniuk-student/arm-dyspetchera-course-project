package model;

public class Stop {
    private int id;         // Унікальний ідентифікатор зупинки
    private String name;    // Назва зупинки

    // Конструктор з параметрами
    public Stop(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Stop() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public String getName() { return name; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    // Перевизначення методу для повернення ім'я
    @Override
    public String toString() {
        return name;
    }
}
