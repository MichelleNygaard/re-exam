package org.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import static spark.Spark.post;


import java.util.List;

public class Connect {
    private OpcUaClient client;
    // Singleton

    public Connect(String endpointUrl) throws Exception {
        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(endpoint["opc.tcp://192.168.0.122:4840"]);

        OpcUaClient client = OpcUaClient.create(cfg.build());
        client.connect().get();
    }
}


public static void main(String[] args) {
    try {
        String clientUrl = "http://localhost:8080";
        BeerConnect service = new BeerConnect(clientUrl);
        // REST-API endpoints
        post("/start", (req, res) -> service.sendCommand(2));
        post("/stop", (req, res) -> service.sendCommand(3));
        post("/abort", (req, res) -> service.sendCommand(4));
        post("/clear", (req, res) -> service.sendCommand(5));
        post("/reset", (req, res) -> service.sendCommand(1));

        System.out.println("Server is running on port 8080...");
    } catch (Exception e) {
        e.printStackTrace();
    }
}