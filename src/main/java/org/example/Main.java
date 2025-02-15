package org.example;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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


        //start produktionen
        Production production = new Production();
        // Can be used to set parameters without PostMan
        production.machineReady();
        production.startProduction(296, 1, 25, 250);


    }
}
