package model;

public class Role {
    private int id;         // Унікальний ідентифікатор посади
    private String name;    // Назва посади

    // Конструктор з параметрами
    public Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Role() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public String getName() { return name; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    // Перевизначення методу toString(), повертає назву посади для інтерфейсу
    @Override
    public String toString() {
        return name;
    }
}
