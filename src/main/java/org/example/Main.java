package org.example;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {

        ConnectionClass instance = ConnectionClass.getInstance();
        SpringApplication.run(Main.class, args);

        instance.subscribe(Production.CURRENT_STATE_NODE_ID);
        instance.subscribe(Production.SPEED_NODE_ID);
        instance.subscribe(Production.PRODUCED);
        instance.subscribe(Production.PROD_DEFECTIVE);
        instance.subscribe(Production.PROD_SUCCESS);

            Scanner scanner = new Scanner(System.in);
            Production production = new Production();

            try {
                // Get production parameters from the user
                System.out.println("Enter batchId:");
                int batchId = scanner.nextInt();
                System.out.println("Enter productType:");
                int productType = scanner.nextInt();
                System.out.println("Enter quantity:");
                int quantity = scanner.nextInt();
                System.out.println("Enter speed:");
                int speed = scanner.nextInt();

                // Start production with the entered parameters
                production.startProduction(batchId, productType, quantity, speed);

                // Command loop
                while (true) {
                    System.out.println("Enter command (or -1 to exit):");
                    int command = scanner.nextInt();

                    if (command == -1) {
                        break; // Exit the loop if -1 is entered
                    }

                    try {
                        production.sendCommand(command);
                        System.out.println("Command sent successfully.");
                    } catch (Exception e) {
                        System.out.println("Error sending command: " + e.getMessage());
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please provide integer values.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            } finally {
                scanner.close(); // Close the scanner to release resources
            }
        }
    }

        //start produktionen

        // Can be used to set parameters without PostMan
//        production.startProduction(0, 2, 200, 120);