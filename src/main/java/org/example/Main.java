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
                // Input integer værdier af variable
                int batchId = -1; // Initialize with an invalid value
                while (!production.isValidBatchId(batchId)) {
                    System.out.println("Enter batchId (must be positive):");
                    batchId = scanner.nextInt();
                    if (!production.isValidBatchId(batchId)) {
                        System.out.println("Invalid batchId. Please enter a value between 0 and 65535.");
                    }
                }

                int productType = -1; // Initialize with an invalid value
                while (!production.isValidProductType(productType)) {
                    System.out.println("Enter productType (0-5):");
                    productType = scanner.nextInt();
                    if (!production.isValidProductType(productType)) {
                        System.out.println("Invalid productType. Please enter a value between 0 and 5.");
                    }
                }

                int quantity = -1; // Initialize with an invalid value
                while (!production.isValidQuantity(quantity)) {
                    System.out.println("Enter quantity (must be positive):");
                    quantity = scanner.nextInt();
                    if (!production.isValidQuantity(quantity)) {
                        System.out.println("Invalid quantity. Please enter a value between 0 and 65535.");
                    }
                }

                int speed = -1; // Initialize with an invalid value
                while (!production.isValidSpeed(productType, speed)) {  // Use productType for validation
                    System.out.println("Enter speed (valid range depends on productType):");
                    speed = scanner.nextInt();
                    if (!production.isValidSpeed(productType, speed)) {
                        System.out.println("Invalid speed for the selected product type. Please check the valid range.");
                    }
                }


                // Start produktionen med de inputtede værdier
                production.machineReady();
                production.startProduction(batchId, productType, quantity, speed);

                // Command loop
                while (true) {
                    System.out.println("Enter command (or -1 to exit):");
                    int command = scanner.nextInt();

                    if (command == -1) {
                        break; // Afbryd forbindelsen
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