package com.athanasios.protal2023.V3.db;

import com.athanasios.protal2023.V3.BlockChain.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    public static void main(String[] args) {
        deleteTable();
        createTable();
        //selectAll();

    }

    //We use a design pattern to create a unique instance of the BlockChain using
    //Singleton Lazy Initialization with on-demand holder
    private Database(){}
    private static class DatabaseHolder {
        static Database database = new Database();
    }
    public static Database getInstance() {
        return DatabaseHolder.database;
    }

    public synchronized void insertNewProduct(Block block){
        try {
            JsonObject jsonObject = JsonParser.parseString(block.getData()).getAsJsonObject();
            long codeValue = jsonObject.get("code").getAsLong();
            System.out.println("Code: "+codeValue);
            int previousID = getLastID(codeValue);

            Connection connection = connect();
            String insertSQL = "INSERT INTO PRODUCTS (DATA, HASH, TIMESTAMP,NONCE, PREVIOUS_HASH,PREVIOUS_ID) VALUES(?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, block.getData());
            preparedStatement.setString(2, block.getHash());
            preparedStatement.setLong(3, block.getTimeStamp());
            preparedStatement.setInt(4, block.getNonce());
            preparedStatement.setString(5, block.getPreviousHash());
            preparedStatement.setInt(6, previousID);
            int count = preparedStatement.executeUpdate();
            if(count>0){
                //System.out.println(count+" record updated");
            }
            preparedStatement.close();
            connection.close();
            System.out.println("The product was uploaded to the database successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void selectAll(){
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            String selectSQL = "select * from PRODUCTS";
            ResultSet resultSet = statement.executeQuery(selectSQL);
            while(resultSet.next()){
                System.out.println(resultSet.getString("DATA"));
            }
            statement.close();
            connection.close();
            //System.out.println("Done!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String selectProduct(long code, int position){
        // int position :    1 -> First appearance
        //                   2 -> Last  appearance
        String data = null; //default value if the product was not found in the database
        List<String> productData = new ArrayList<>();
        try {
            Connection connection = connect();
            String selectSQL = "SELECT DATA FROM PRODUCTS" +
                    " WHERE json_extract(data, '$.code') =?"+
                    " ORDER BY ID ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setLong(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                productData.add(resultSet.getString("DATA"));
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();

            if(productData.size()== 1){
                data = productData.get(0);
            }
            else if(productData.isEmpty()){
                return null;
            }
            else{
                if(position == 1){
                    data = productData.get(0);
                }
                else if(position == 2){
                    data = productData.get(productData.size()-1);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    private static void createTable(){
        try {
            Connection connection = connect();
            String createTableSQL = "CREATE TABLE PRODUCTS"
                    + "(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + "DATA TEXT,"
                    + "HASH TEXT,"
                    + "TIMESTAMP INTEGER,"
                    + "NONCE INTEGER,"
                    + "PREVIOUS_HASH TEXT,"
                    + "PREVIOUS_ID INTEGER NOT NULL DEFAULT 0)";
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableSQL);
            connection.close();
            System.out.println("The table was created successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static Connection connect(){
        String connectionString = "jdbc:sqlite:javaprotalproject2.db";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;
    }

    private static void deleteTable(){
        try {
            Connection connection = connect();
            String deleteTableSQL = "DROP TABLE PRODUCTS";
            Statement statement = connection.createStatement();
            statement.executeUpdate(deleteTableSQL);
            connection.close();
            System.out.println("The table was deleted successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Updates the blockChain that is stored locally with the data of the products in the database.
    public void updateBlockChain(){
        try {
            List<Block> blocks = new ArrayList<>();
            BlockChain bc = BlockChain.getInstance();

            Connection connection = connect();
            Statement statement = connection.createStatement();
            String getBlockChain = "SELECT * FROM PRODUCTS";
            ResultSet resultSet = statement.executeQuery(getBlockChain);
            while(resultSet.next()){
                String data = resultSet.getString("DATA");
                String hash = resultSet.getString("HASH");
                String previousHash = resultSet.getString("PREVIOUS_HASH");
                long timeStamp = resultSet.getLong("TIMESTAMP");
                int nonce = resultSet.getInt("NONCE");
                Block block = new Block(previousHash,data, hash,timeStamp,nonce);
                blocks.add(block);
            }
            bc.updateBlockChain(blocks);
            statement.close();
            connection.close();
            //System.out.println("BlockChain was retrieved successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean checkIfBlockChainIsValid(){
        BlockChain bc = BlockChain.getInstance();
        updateBlockChain();
        //System.out.println("Is the chain valid? : "+bc.isChainValid());
        return bc.isChainValid();
    }

    //Searches the blockChain in reverse to find the last product with the same code as the product we want
    public int getLastID(long code){
        int id = 0; //default value if it's the first time a product has been added
        try {
            Connection connection = connect();
            String selectIDSQL = "SELECT ID FROM PRODUCTS" +
                    " WHERE json_extract(data, '$.code') =?"+
                    " ORDER BY ID DESC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(selectIDSQL);
            preparedStatement.setLong(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                id = resultSet.getInt("ID");
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();

            //System.out.println("getLastID Done!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    public int getNextID(){
        int id = 1; //default value if it is the first row that has been inserted
        try {
            Connection connection = connect();
            Statement statement = connection.createStatement();
            String selectSQL = "select seq from sqlite_sequence where name='PRODUCTS'";
            ResultSet resultSet = statement.executeQuery(selectSQL);
            while(resultSet.next()){
                id = resultSet.getInt("seq")+1;
            }
            resultSet.close();
            statement.close();
            connection.close();
            //System.out.println("Done! ID= "+id);
            return id;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }

       return id;
    }

    public int getProductStatistics(long code){
        //Returns   0 -> Product not found
        //          1 -> Success
        int status = 0;
        try {
            Connection connection = connect();
            String selectSQL = "SELECT DATA FROM PRODUCTS" +
                    " WHERE json_extract(data, '$.code') =?"+
                    " ORDER BY ID ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setLong(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                String data = resultSet.getString("DATA");
                JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
                String priceValue = jsonObject.get("price").getAsString();
                String timestamp = jsonObject.get("timestamp").getAsString();
                System.out.println("Price: "+priceValue+" , Timestamp: "+timestamp);
                status = 1;
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }



}