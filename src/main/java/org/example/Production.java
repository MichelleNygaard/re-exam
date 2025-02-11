package org.example;
import com.google.common.collect.ImmutableList;

import org.eclipse.jetty.client.ProxyProtocolClientConnectionFactory;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Production {
    private final OpcUaClient client;

    ConnectionClass instance = ConnectionClass.getInstance();

    //NodeID fra UAExpert
    public static final NodeId CURRENT_STATE_NODE_ID = new NodeId(6, "::Program:Cube.Status.StateCurrent");
    public static final NodeId BATCH_VALUE_NODE_ID = new NodeId(6, "::Program:Cube.Command.Parameter[0].Value");
    public static final NodeId PRODUCT_TYPE_NODE_ID = new NodeId(6, "::Program:Cube.Command.Parameter[1].Value");
    public static final NodeId QUANTITY_VALUE_NODE_ID = new NodeId(6, "::Program:Cube.Command.Parameter[2].Value");
    public static final NodeId SPEED_NODE_ID = new NodeId(6, "::Program:Cube.Command.MachSpeed");
    public static final NodeId CNTRL_CMD_NODE_ID = new NodeId(6, "::Program:Cube.Command.CntrlCmd");
    public static final NodeId CMD_CHANGE_REQUEST_NODE_ID = new NodeId(6, "::Program:Cube.Command.CmdChangeRequest");
    public static final NodeId STOP_REASON_ID_NODE_ID = new NodeId(6, "::Program.Cube.Admin.StopReason");
    public static final NodeId PROD_DEFECTIVE = new NodeId(6, "::Program.Cube.Admin.ProdDefectiveCount");
    public static final NodeId PROD_SUCCESS = new NodeId(6, "::Program.Cube.Admin.ProdProcessedCount");
    public static final NodeId PRODUCED = new NodeId(6, "::Program:product.produced");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Production() throws Exception {
        this.client = ConnectionClass.getInstance().client;
        this.client.connect().get();
    }

    public void machineReady() throws Exception {
        int currentState = readMachineState();
        //int stopReason = readStopReason();
        if (currentState == 4) { // idle state
            sendCommand(2); // kør
            TimeUnit.MILLISECONDS.sleep(500);
        } else {
            logger.info("Machine is not in idle. Resetting...");
            sendCommand(1); // Reset

        }
        readMachineState();
    }

    public void startProduction(int batchId, int productType, int quantity, int speed) throws Exception {
        //Check machine state
        machineReady();
        System.out.println("Current Machine State: " + readMachineState());

        //batch id checker
        if (isValidBatchId(batchId)) {
            client.writeValues(
                    ImmutableList.of(BATCH_VALUE_NODE_ID),
                    ImmutableList.of(new DataValue(new Variant(batchId), null, null))
            ).get();
        } else {
            sendCommand(3);
            System.out.println("BatchId is not valid, production stopped");
        }
        //produkt type/id //Hastighed
        if (isValidSpeed(productType, speed)) {
            client.writeValues(
                    ImmutableList.of(PRODUCT_TYPE_NODE_ID),
                    ImmutableList.of(new DataValue(new Variant(productType), null, null))
            ).get();
            client.writeValues(
                    ImmutableList.of(SPEED_NODE_ID),
                    ImmutableList.of(new DataValue(new Variant(speed), null, null))
            ).get();
        } else {
            sendCommand(3);
            System.out.println("ProductType or speed is not valid, production stopped");
        }

        //Antal
        if (isValidQuantity(quantity)) {
        client.writeValues(
                ImmutableList.of(QUANTITY_VALUE_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(quantity), null, null))
        ).get();
        } else {
            sendCommand(3);
            System.out.println("Quantity is not valid, production stopped");
        }




        //Vent lidt
        TimeUnit.MILLISECONDS.sleep(500);
        try {
            //tjekker at hastighed er inden for den range det specifikke produkt tillader
            if (!isValidSpeed(productType, speed)) {
                System.out.println("Denne type tillader ikke denne hastighed");
                return;
            } if (!isValidBatchId(batchId)) {
                System.out.println("Dette BatchId er 0 eller over 65535");
                return;
            } if (!isValidQuantity(quantity)) {
                System.out.println("Dette Quantity er 0 eller over 65535");
                return;
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Fejler i startproduction");

        }
    }

    /*public void cmdNode() throws Exception{
        System.out.println("STATE INDEN START: " + readMachineState());
        client.writeValues(
                ImmutableList.of(CNTRL_CMD_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(1), null, null))
        ).get();
        client.writeValues(
                ImmutableList.of(CMD_CHANGE_REQUEST_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(true), null, null))
        ).get();

        TimeUnit.MILLISECONDS.sleep(10000);
        readMachineState();

        client.writeValues(
                ImmutableList.of(CNTRL_CMD_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(2), null, null))
        ).get();

        //vent
        TimeUnit.MILLISECONDS.sleep(200);

        client.writeValues(
                ImmutableList.of(CMD_CHANGE_REQUEST_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(true), null, null))
        ).get();

        TimeUnit.MILLISECONDS.sleep(500);

        readMachineState();


    }*/
    private void sendCommand(int command) throws Exception {
        client.writeValues(
                ImmutableList.of(CNTRL_CMD_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(command), null, null))
        ).get();

        client.writeValues(
                ImmutableList.of(CMD_CHANGE_REQUEST_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(true), null, null))
        ).get();
    }

    private int readMachineState() throws Exception {
        if (client == null) {
            System.out.println("OPC UA client is not connected or session is null.");
            return -1;
        }


        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, CURRENT_STATE_NODE_ID);
        DataValue dataValue = futureValue.get();
        Object value = dataValue.getValue().getValue();
        System.out.println("Value: " + value);
        return (Integer) value;
    }

    private int prodSuccess() throws Exception {
        if (client == null) {
            System.out.println("OPC UA client is not connected or session is null.");
            return -1;
        }

        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, PROD_SUCCESS);
        DataValue dataValue = futureValue.get();
        Object valueSuc = dataValue.getValue().getValue();
        System.out.println("Value: " + valueSuc);
        return (Integer) valueSuc;

    }

    private int prodFail() throws Exception {
        if (client == null) {
            System.out.println("OPC UA client is not connected or session is null.");
            return -1;
        }

        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, PROD_DEFECTIVE);
        DataValue dataValue = futureValue.get();
        Object valueFail = dataValue.getValue().getValue();
        System.out.println("Value: " + valueFail);
        return (Integer) valueFail;
    }


    /*private int readStopReason() throws Exception{
        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, STOP_REASON_ID_NODE_ID);
        DataValue dataValue = futureValue.get();
        return (Integer) dataValue.getValue().getValue();
    }*/


    /*private void NewWriteValue(NodeId nodeId, Object value) {
        try{
            CompletableFuture<Void> future = this.client.writeValue(nodeId, new DataValue(new Variant(value))).thenAccept(v -> System.out.println("Wrote " + value + " to " + nodeId));

            future.get();
        }catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
            System.out.println("fejler i NewWriteValue");
        }
    }*/
    private boolean isValidBatchId(int batchId) throws Exception {
        if (batchId  <= 0 || batchId > 65535) {
            return false;
        } else {
            return true;
        }

    }
    private boolean isValidQuantity(int quantity) throws Exception {
        if (quantity <= 0 || quantity > 65535) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidSpeed(int productType, int speed){
        return switch(productType){
            case 0 -> speed >= 0 && speed <= 600;  // Pilsner
            case 1 -> speed >= 0 && speed <= 300;  // Wheat
            case 2 -> speed >= 0 && speed <= 150;  // IPA
            case 3 -> speed >= 0 && speed <= 200;  // Stout
            case 4 -> speed >= 0 && speed <= 100;  // Ale
            case 5 -> speed >= 0 && speed <= 125;  // Alcohol-Free
            default -> false;
        };
    }
}