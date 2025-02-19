package org.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static spark.Spark.post;

import java.util.ArrayList;
import java.util.List;


public final class ConnectionClass {

    private static ConnectionClass instance;

    OpcUaClient client;

    private ConnectionClass() {
        create();
    }

    public void create() {
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
            client = OpcUaClient.create(cfg.build());
            client.connect().get();


            System.out.println("Connected successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConnectionClass getInstance() {
        if (instance == null) {
            instance = new ConnectionClass();
        }
        return instance;
    }

    public void subscribe(NodeId nodeId) throws Exception {

        // create a subscription @ 1000ms
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();
        UInteger clientHandle = subscription.getSubscriptionId();
        ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);

        // subscribe to the Value attribute of the server's CurrentTime node
        MonitoringParameters parameters = new MonitoringParameters(
                clientHandle,
                1000.0, // sampling interval
                null, //this means that we use default filters
                Unsigned.uint(10), //queue size
                true // discard oldest
        );

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                readValueId, //identifies the node to monitor
                MonitoringMode.Reporting, //recieve updates when value changes
                parameters //parameters for the item
        );

        //lambda expression for handling the creation of the item
        UaSubscription.ItemCreationCallback onItemCreated = (item, id) -> item.setValueConsumer(this::onSubscriptionValue);

        List<UaMonitoredItem> items = subscription.createMonitoredItems(
                TimestampsToReturn.Both, //return both server and source timestamps
                newArrayList(request), //list of items to create
                onItemCreated //callback for handling the creation of the item
        ).get();


        System.out.println("We have subscribed to the node: " + nodeId);

    }

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        System.out.println("A new value has been updated for node: " + item.getReadValueId().toString() + " The value is: " + value.getValue().getValue());
    }
}

