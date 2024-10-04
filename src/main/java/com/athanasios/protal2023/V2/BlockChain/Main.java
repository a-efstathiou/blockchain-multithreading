package com.athanasios.protal2023.V2.BlockChain;

import com.athanasios.protal2023.V2.db.Database;

import java.util.Scanner;

public class Main {

    static Database db = Database.getInstance();
    static BlockChain bc = BlockChain.getInstance();
    static Console co = Console.getInstance();
    static Scanner scanner = co.getScanner();
    public static void main(String[] args) {

        viewOptions();
        
    }

    private static void viewOptions() {
        int choice;
        do {
            co.displayMenu();
            choice = co.getIntInput("Enter a number from the available choices: ",scanner);
            switch (choice) {
                case 1-> {
                    co.viewProducts();
                }
                case 2-> {
                    co.addProduct();
                }
                case 3-> co.addMultipleProducts();
                case 4-> co.searchProduct();
                case 5-> co.viewProductStats();
                case 6-> {
                    System.out.println("The program will close.");
                }
                case 7 -> {
                    db.checkIfBlockChainIsValid();
                    bc.printBlockChain();}
                default-> System.out.println("Invalid choice.");
            }
        } while (choice != 6);
    }




}
