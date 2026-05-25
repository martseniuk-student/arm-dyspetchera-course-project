package model;

public class Ticket {
    private int id;                         // Унікальний ідентифікатор квитка
    private double price;                   // Вартість квитка

    private Order order;                    // Посилання на об'єкт замовлення
    private ScheduleEntry scheduleEntry;    // Посилання на об'єкт рейсу
    private Stop stop;                      // Посилання на об'єкт зупинки

    // Конструктор з параметрами
    public Ticket(int id, double price, Order order, ScheduleEntry scheduleEntry, Stop stop) {
        this.id = id;
        this.price = price;
        this.order = order;
        this.scheduleEntry = scheduleEntry;
        this.stop = stop;
    }

    public Ticket() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public double getPrice() { return price; }
    public Order getOrder() { return order; }
    public ScheduleEntry getScheduleEntry() { return scheduleEntry; }
    public Stop getStop() { return stop; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setPrice(double price) { this.price = price; }
    public void setOrder(Order order) { this.order = order; }
    public void setScheduleEntry(ScheduleEntry scheduleEntry) { this.scheduleEntry = scheduleEntry; }
    public void setStop(Stop stop) { this.stop = stop; }
}
