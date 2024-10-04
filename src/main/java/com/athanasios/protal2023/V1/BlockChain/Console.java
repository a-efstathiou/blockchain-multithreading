package com.athanasios.protal2023.V1.BlockChain;

import com.athanasios.protal2023.V1.db.Database;

import java.time.Instant;
import java.util.*;


public class Console {
    private final Database db = Database.getInstance();
    private final BlockChain bc = BlockChain.getInstance();
    private final Scanner scanner;

    //We use a design pattern to create a unique instance of console using
    //Singleton Lazy Initialization with on-demand holder
    private Console(){
        this.scanner = new Scanner(System.in).useLocale(Locale.US);
    }
    private static class ConsoleHolder {
        static Console console = new Console();
    }
    public static Console getInstance() {
        return ConsoleHolder.console;
    }
    public Scanner getScanner(){return scanner;}

    public void displayMenu(){
        System.out.println("\nAvailable operations: ");
        System.out.println("1. View all products");
        System.out.println("2. Add a product");
        System.out.println("3. Add multiple products");
        System.out.println("4. Search for a product");
        System.out.println("5. View product statistics");
        System.out.println("6. Exit");
    }

    public void viewProducts(){
        System.out.println("The product list: ");
        db.selectAll();
    }

    private String getStringInput(String prompt,Scanner scanner){
        while(true){
            try {
                System.out.println(prompt);
                return scanner.nextLine().trim();
            }
            catch(InputMismatchException e){
                System.out.println("Invalid input. Please enter a word or text");
            }
        }
    }

    private double getDoubleInput(String prompt, Scanner scanner){
        while(true){
            try {
                System.out.println(prompt);
                String input = scanner.nextLine().trim();
                return Double.parseDouble(input);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid input. Please enter a valid number or decimal value");
            }
        }
    }

    public int getIntInput(String prompt, Scanner scanner) {
        while(true){
            try {
                System.out.println(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid input. Please enter a valid number");
            }
        }
    }



    private long getLongInput(String prompt, Scanner scanner) {
        while(true){
            try {
                System.out.println(prompt);
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid input. Please enter a valid number");
            }
        }
    }

    public void addProduct(){
        System.out.println("\nPlease enter the product details: ");
        String title = getStringInput("Product Title: ",scanner);
        long code = getLongInput("Product Code: ",scanner);
        String description = getStringInput("Product Description: ",scanner);
        String category = getStringInput("Product Category: ",scanner);
        double price = getDoubleInput("Product Price: ",scanner);



        //Calculate id, previous_id and create the data JsonString
        int id = db.getNextID();
        int previous_id = db.getLastID(code);
        String ProductTimestamp = Instant.now().toString();
        String data = (new Product(id, code, title, ProductTimestamp, price, description, category, previous_id)).toJSON();
        Block block = bc.createBlock(data);
        System.out.println("Created block hash: "+block.getHash());
        System.out.println("Generated block hash: "+block.calculateBlockHash());

        //Check if the blockchain is valid and if it is not the product does not get added to the db.
        if(db.checkIfBlockChainIsValid() && bc.willBlockChainBeValid(block)){
            db.insertNewProduct(block);
        }
        else{
            System.out.println("Error: The BlockChain is not valid. The product won't be added.");
        }

    }

    public void addMultipleProducts(){
        while(true){
            int numberOfProducts = getIntInput("Enter how many products you would like to add: ",scanner);
            if(numberOfProducts <= 0 ){
                System.out.println("Invalid choice. The number must be > 0");
            }
            else{
                for(int i=0; i<numberOfProducts;i++){
                    System.out.println("Enter the product #"+(i+1)+"\n");
                    addProduct();
                }
                break;
            }
        }
    }

    public void searchProduct(){
        long code = getLongInput("\nEnter the code of the product you want to search:",scanner);
        System.out.println("Choose if you want the first or the last appearance of the product: ");
        System.out.println("1. First appearance");
        System.out.println("2. Last appearance");
        while(true){
            int appearance = getIntInput("Enter your choice: ",scanner);
            if(appearance != 1 && appearance != 2) {
                System.out.println("Invalid choice. Choose between 1 or 2.");
            }
            else{
                String data = db.selectProduct(code,appearance);
                System.out.println(Objects.requireNonNullElse(data, "The product was not found"));
                break;
            }

        }
    }

    public void viewProductStats(){

        //Search for the product with that code in the blockchain and then print all the prices in accordance to time.
        long code = getLongInput("\nEnter the code of the product: ",scanner);
        int status = db.getProductStatistics(code);
        if(status == 0){
            System.out.println("The product was not found");
        }
    }


}
