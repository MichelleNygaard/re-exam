package org.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import static spark.Spark.post;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClientConfigBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;


import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpcUaClientExample {
    public static void main(String[] args) {
        try {
            // Server URL
            String serverUrl = "opc.tcp://192.168.0.122:4840";

            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints("opc.tcp://192.168.0.122:4840").get();

            OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
            EndpointDescription endpoint = endpoints.get(0);

            // Build the OPC UA client
            OpcUaClient client = new OpcUaClient(cfg.build());

            // Connect to the server
            CompletableFuture<UaClient> future = client.connect();
            future.get(); // Wait for connection

            System.out.println("Connected to OPC UA server!");

            // Perform read/write operations here...

            // Disconnect when done
            client.disconnect().get();
            System.out.println("Client disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//public class Connect {
//    private OpcUaClient client;
//    // Singleton
//
//    public Connect(String endpointUrl) throws Exception {
//        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
//        cfg.setEndpoint(endpoint["opc.tcp://192.168.0.122:4840"]);
//
//        OpcUaClient client = OpcUaClient.create(cfg.build());
//        client.connect().get();
//    }
//}
//
//// Lav read- of write-PackTags (To be able to read the sensors and control the production)
//// Lav metode for at sende PackML commands (For the sendCommand in main method)
//
//
//// Evt. flyttes i separat klasse
//
//public static void main(String[] args) {
//    try {
//        String clientUrl = "http://localhost:8080";
//        Connect service = new Connect(clientUrl);
//
//        post("/start", (req, res) -> service.sendCommand(2));
//        post("/stop", (req, res) -> service.sendCommand(3));
//        post("/abort", (req, res) -> service.sendCommand(4));
//        post("/clear", (req, res) -> service.sendCommand(5));
//        post("/reset", (req, res) -> service.sendCommand(1));
//
//        System.out.println("Server is running on port 8080...");
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//}
//

