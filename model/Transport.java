package model;

public class Transport {
    private int id;             // Унікальний ідентифікатор транспорту
    private String boardNumber; // Бортовий номер
    private String status;      // Статус транспорту (наприклад, функціональний, в ремонті тощо)

    private TransportType type; // Посилання на об'єкт типу транспорту

    // Конструктор з параметрами
    public Transport(int id, String boardNumber, String status, TransportType type) {
        this.id = id;
        this.boardNumber = boardNumber;
        this.status = status;
        this.type = type;
    }

    // Конструктор з двома параметрами
    public Transport(int transportId, String boardNumber) {
        this.id = transportId;
        this.boardNumber = boardNumber;
    }

    public Transport() {} // Пустий конструктор


    // Геттери
    public int getId() { return id; }
    public String getBoardNumber() { return boardNumber; }
    public String getStatus() { return status; }
    public TransportType getType() { return type; }

    // Сеттери
    public void setId(int id) { this.id = id; }
    public void setBoardNumber(String boardNumber) { this.boardNumber = boardNumber; }
    public void setStatus(String status) { this.status = status; }
    public void setType(TransportType type) { this.type = type; }

    // Перевизначення методу для повернення бортового номера
    @Override
    public String toString() {
        return boardNumber;
    }
}
