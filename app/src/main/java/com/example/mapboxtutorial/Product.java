package com.example.mapboxtutorial;

public class Product {
    private String Name;
    private String Type;
    private float Price;

    public Product(String name, String type, float price) {
        Name = name;
        Type = type;
        Price = price;
    }
    public String getName() {
        return Name;
    }
    public String getType() {
        return Type;
    }
    public float getPrice() {
        return Price;
    }

    @Override
    public String toString() {
        return Name + " - " + Price;
    }
}
