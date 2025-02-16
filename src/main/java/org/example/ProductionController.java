package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Indicates that this class is a REST controller, to allows rest control
@RestController
// Maps HTTP requests to the chosen base URL.
@RequestMapping("/api")
public class ProductionController {
    // Declares a private variable for the Production object
    private final Production production;


    // Constructor for the controller class
    public ProductionController() throws Exception {
        // Initializing the Production object
        this.production = new Production();
    }


    // HTTP method to handle POST requests for setting parameters in startProduction
    // POST request in Postman at http://localhost:8080/api/setParameters
    @PostMapping("/setParameters")
    public ResponseEntity<String> startProduction(@RequestParam int batchId,
                                                  @RequestParam int productType,
                                                  @RequestParam int quantity,
                                                  @RequestParam int speed) {
        try {
            // Calls the startProduction method
            production.startProduction(batchId, productType, quantity, speed);
            // Returns a success response when production starts
            return ResponseEntity.ok("Production started successfully");
        } catch (Exception e) {
            // Returns error response if issues/errors occur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting production: " + e.getMessage());
        }
    }

    // Method for handleing POST request to send a command
    // POST request in Postman at http://localhost:8080/api/sendCommand
    @PostMapping("/sendCommand")
    public ResponseEntity<String> sendCommand(@RequestParam int command) {
        try {
            production.sendCommand(command);
            return ResponseEntity.ok("Command sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending command: " + e.getMessage());
        }
    }
}
