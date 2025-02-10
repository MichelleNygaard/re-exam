package org.example;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
                readValueId,
                MonitoringMode.Reporting,
                parameters
        );



        // when creating items in MonitoringMode.Reporting this callback is where each item needs to have its
        // value/event consumer hooked up. The alternative is to create the item in sampling mode, hook up the
        // consumer after the creation call completes, and then change the mode for all items to reporting.
        UaSubscription.ItemCreationCallback onItemCreated = (item, id) ->
            item.setValueConsumer(this::onSubscriptionValue);

        List<MonitoredItemCreateRequest> items = List.of(request);

        subscription.createMonitoredItems(TimestampsToReturn.Both, items, onItemCreated).get();

        System.out.println("We have subscribed to the node: " + nodeId);

    }

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        System.out.println("A new value has been updated for node: " + item.getReadValueId().toString() + " The value is: " + value.getValue().getValue().toString());
    }
}

