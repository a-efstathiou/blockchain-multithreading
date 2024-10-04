package com.athanasios.protal2023.V3.BlockChain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Product {
    private final int ID;
    private final long code;
    private final String title;
    private final String timestamp;
    private final double price;
    private final String description;
    private final String category;
    private final int previousID;

    public Product(int ID, long code, String title, String timestamp, double price, String description, String category, int previousID) {
        this.ID = ID;
        this.code = code;
        this.title = title;
        this.timestamp = timestamp;
        this.price = price;
        this.description = description;
        this.category = category;
        this.previousID = previousID; //default value 0 = no previous registry
    }



    @Override
    public String toString() {
        return "Product{" +
                " code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    public String toJSON(){
        //Transform into Json
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static Product fromJSON(String JsonString){
        //Transform Json String back to Product Object
        return new Gson().fromJson(JsonString, Product.class);
    }



}
