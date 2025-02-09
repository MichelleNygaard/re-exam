package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductionController {
    private final Production production;

    public ProductionController() throws Exception {
        this.production = new Production();
    }

    @PostMapping("/startProduction")
    public ResponseEntity<String> startProduction(@RequestParam int batchId,
                                                  @RequestParam int productType,
                                                  @RequestParam int quantity,
                                                  @RequestParam int speed) {
        try {
            production.startProduction(batchId, productType, quantity, speed);
            return ResponseEntity.ok("Production started successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error starting production: " + e.getMessage());
        }
    }

    @PostMapping("/sendCommand")
    public ResponseEntity<String> sendCommand(@RequestParam int command) {
        try {
            production.cmdNode();
            return ResponseEntity.ok("Command sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending command: " + e.getMessage());
        }
    }
}
