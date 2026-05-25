package model;

public class TransportType {
    private int id;             // Унікальний ідентифікатор типу транспорту
    private String name;        // Назва типу
    private int capacity;       // Пасажиромісткість

    // Конструктор з параметрами
    public TransportType(int id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public TransportType() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    // Перевизначення методу для повернення моделі з кількістю місць
    @Override
    public String toString() {
        return name + " (" + capacity + " місць)";
    }
}
