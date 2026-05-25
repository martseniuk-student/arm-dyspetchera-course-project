package model;

import java.time.LocalDateTime;

public class Payment {
    private int id;                     // Унікальний ідентифікатор оплати
    private double amount;              // Сума оплати
    private LocalDateTime paymentTime;  // Час оплати

    private Order order;                // Посилання на об'єкт замовлення

    // Конструктор з параметрами
    public Payment(int id, double amount, LocalDateTime paymentTime, Order order) {
        this.id = id;
        this.amount = amount;
        this.paymentTime = paymentTime;
        this.order = order;
    }

    public Payment() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public Order getOrder() { return order; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public void setOrder(Order order) { this.order = order; }
}
