package model;

import java.time.LocalDateTime;

public class Order {
    private int id;                     // Унікальний ідентифікатор замовлення
    private int ticketCount;            // Кількість квитків
    private double totalPrice;          // Загальна вартість
    private String paymentType;         // Тип оплати
    private LocalDateTime orderTime;    // Час ініціації замовлення кондуктором

    // Конструктор з параметрами
    public Order(int id, int ticketCount, double totalPrice, String paymentType, LocalDateTime orderTime) {
        this.id = id;
        this.ticketCount = ticketCount;
        this.totalPrice = totalPrice;
        this.paymentType = paymentType;
        this.orderTime = orderTime;
    }

    public Order() {} // Пустий конструктор

    // Геттери
    public int getId() { return id; }
    public int getTicketCount() { return ticketCount; }
    public double getTotalPrice() { return totalPrice; }
    public String getPaymentType() { return paymentType; }
    public LocalDateTime getOrderTime() { return orderTime; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
}
