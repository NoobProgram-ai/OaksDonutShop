package entity;

public class Order {
    private int ID;
    private double price;
    private String dateTime;
    private String itemName;

    public Order(int ID, double price, String dateTime, String itemName) {
        this.ID = ID;
        this.price = price;
        this.dateTime = dateTime;
        this.itemName = itemName;
    }

    // Getters
    public int getID() { return ID; }
    public double getPrice() { return price; }
    public String getDateTime() { return dateTime; }
    public String getItemName() { return itemName; }

    // Setters (optional if you want to update fields)
    public void setPrice(double price) { this.price = price; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    @Override
    public String toString() {
        return "Order{" +
                "ID=" + ID +
                ", price=" + price +
                ", dateTime='" + dateTime + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}