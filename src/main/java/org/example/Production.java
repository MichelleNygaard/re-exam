package org.example;
import com.google.common.collect.ImmutableList;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.util.concurrent.CompletableFuture;
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
//    public static final NodeId STOP_REASON_ID_NODE_ID = new NodeId(6, "::Program:Cube.Admin.StopReason");
    public static final NodeId PROD_DEFECTIVE = new NodeId(6, "::Program:Cube.Admin.ProdDefectiveCount");
    public static final NodeId PROD_SUCCESS = new NodeId(6, "::Program:Cube.Admin.ProdProcessedCount");
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

    public void quantityReached(int quantity) throws Exception {
        // Tjekker om den ønskede quantity er nået og stopper production if true.
        while (true) {
            int producedQuantity = isProducedQuantity();
            if (producedQuantity >= quantity) {
                sendCommand(3);
                System.out.println("Production finished, quantity reached");
                break;
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public void startProduction(int batchId, int productType, int quantity, int speed) throws Exception {
        logger.info("Starting with parameter: batchId={} productType={} quantity={} speed={}", batchId, productType, quantity, speed);
        //Check machine state

        UShort batchIdFloat = Unsigned.ushort(batchId);
        System.out.println("Float batchId: " + batchIdFloat);

        UShort productTypeFloat = Unsigned.ushort(productType);
        System.out.println("Float productType: " + productTypeFloat);

        UShort quantityFloat = Unsigned.ushort(quantity);
        System.out.println("Float quantity: " + quantityFloat);

        UShort speedFloat = Unsigned.ushort(speed);
        System.out.println("Float speed: " + speedFloat);

        machineReady();
        quantityReached(quantity);
        System.out.println("Current Machine State: " + readMachineState());



        //batch id checker
        if (!isValidBatchId(batchId) || !isValidQuantity(quantity) || !isValidSpeed(productType, speed)) {
            sendCommand(3);
            System.out.println("Invalid input detected, production stopped");
            return;
        }

        client.writeValues(
                ImmutableList.of(BATCH_VALUE_NODE_ID, PRODUCT_TYPE_NODE_ID, SPEED_NODE_ID, QUANTITY_VALUE_NODE_ID),
                ImmutableList.of(

                        new DataValue(new Variant(batchIdFloat)),
                        new DataValue(new Variant(productTypeFloat)),
                        new DataValue(new Variant(speedFloat)),
                        new DataValue(new Variant(quantityFloat))
                )
        ).get();



//        //Vent lidt
//        TimeUnit.MILLISECONDS.sleep(500);
//        try {
//            //tjekker at hastighed er inden for den range det specifikke produkt tillader
//            if (!isValidSpeed(productType, speed)) {
//                System.out.println("Denne type tillader ikke denne hastighed");
//                return;
//            } if (!isValidBatchId(batchId)) {
//                System.out.println("Dette BatchId er 0 eller over 65535");
//                return;
//            } if (!isValidQuantity(quantity)) {
//                System.out.println("Dette Quantity er 0 eller over 65535");
//                return;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Fejler i startproduction");
//
//        }
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
        Object valueState = dataValue.getValue().getValue();
        System.out.println("Value: " + valueState);
        return (Integer) valueState;
    }


//    private int prodSuccess() throws Exception {
//        if (client == null) {
//            System.out.println("OPC UA client is not connected or session is null.");
//            return -1;
//        }
//
//        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, PROD_SUCCESS);
//        DataValue dataValue = futureValue.get();
//        Object valueSuc = dataValue.getValue().getValue();
//        System.out.println("Value: " + valueSuc);
//        return (Integer) valueSuc;
//
//    }
//
//    private int prodFail() throws Exception {
//        if (client == null) {
//            System.out.println("OPC UA client is not connected or session is null.");
//            return -1;
//        }
//
//        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, PROD_DEFECTIVE);
//        DataValue dataValue = futureValue.get();
//        Object valueFail = dataValue.getValue().getValue();
//        System.out.println("Value: " + valueFail);
//        return (Integer) valueFail;
//    }


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

    private int isProducedQuantity() throws Exception {
        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, PRODUCED);
        DataValue dataValue = futureValue.get();
        if (dataValue.getValue().getValue() instanceof UShort) {
            UShort ushortValue = (UShort) dataValue.getValue().getValue();
            return ushortValue.intValue();
        } else {
            throw new IllegalArgumentException("Expected UShort type");
        }
    }
}