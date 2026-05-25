package model;

public class Personnel {
    private int id;             // Унікальний ідентифікатор співробітника
    private String fullName;    // Прізвище, Ім'я, По батькові
    private String status;      // Статус співробітника (наприклад, вільний, на лікарняному)

    private Role role;          // Посилання на об'єкт посади

    // Конструктор з параметрами
    public Personnel(int id, String fullName, String status,  Role role) {
        this.id = id;
        this.fullName = fullName;
        this.status = status;
        this.role = role;
    }

    public Personnel() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public Role getRole() { return role; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setStatus(String status) { this.status = status; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return fullName;
    }
}
