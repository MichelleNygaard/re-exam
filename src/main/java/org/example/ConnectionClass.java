package org.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import static spark.Spark.post;

import java.util.List;

//        OpcUaClient client = create();

//        while (true) {
//            Thread.sleep(10);
//        }

public final class ConnectionClass {


    /**
     * this creates and connects a client for the OPC UA endpoint.
//     * @return OPCuaClient client
     */

    private static ConnectionClass instance;

    OpcUaClient client;


    private ConnectionClass() {
        create();
    }

    public OpcUaClient create() {
        try {
            //get all endpoints from server
            List<EndpointDescription> endpoints = DiscoveryClient
                    .getEndpoints("opc.tcp://127.0.0.1:4840") //192.168.0.122
                    .get();
            OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
            for (int i = 0; i < endpoints.size(); i++) {
                if(endpoints.get(i).getSecurityMode().name().equals("None")){
                    EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(i), "127.0.0.1", 4840);
                    cfg.setEndpoint(configPoint);
                    break;
                }
            }

            //setting up client with config
//            OpcUaClient client = OpcUaClient.create(cfg.build());
            client = OpcUaClient.create(cfg.build());
            client.connect().get();

            System.out.println("Connected successfully");
            return client;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ConnectionClass getInstance() {
        if (instance == null) {
            instance = new ConnectionClass();
        }
        return instance;
    }
}

