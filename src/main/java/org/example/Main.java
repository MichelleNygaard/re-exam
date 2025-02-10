package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {
        ConnectionClass.getInstance();
        SpringApplication.run(Main.class, args);

        //start produktionen
<<<<<<< Updated upstream
//        Production production = new Production();
//        production.startProduction(1001, 2, 200, 120);
//        production.cmdNode();
=======
        Production production = new Production();
        production.startProduction(1001, 2, 200, 120);
        //production.cmdNode();
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
    }
}
