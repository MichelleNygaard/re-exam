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



        //start produktionen
//        Production production = new Production();
//        production.startProduction(1001, 2, 200, 120);
//        production.cmdNode();
    }
}
