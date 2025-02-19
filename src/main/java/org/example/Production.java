package org.example;
import com.google.common.collect.ImmutableList;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
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
            System.out.println("Machine is idle, and ready to start");; // go
            TimeUnit.MILLISECONDS.sleep(500);
        } else {
            logger.info("Machine is not in idle. Resetting...");
            sendCommand(1); // Reset

        }
      readMachineState();
   }


    public void startProduction(int batchId, int productType, int quantity, int speed) throws Exception {


        // The nodeWrite method is called. Which needs nodeId and Variant value as inputs.
        // The user-inputted values "batchId, productType etc." are then cast as floats to the node
        nodeWrite(BATCH_VALUE_NODE_ID, new Variant((float) batchId));
        nodeWrite(PRODUCT_TYPE_NODE_ID, new Variant((float) productType));
        nodeWrite(QUANTITY_VALUE_NODE_ID, new Variant((float) quantity));
        nodeWrite(SPEED_NODE_ID, new Variant((float) speed));




        logger.info("Starting with parameter: batchId={} productType={} quantity={} speed={}", batchId, productType, quantity, speed);
        System.out.println("Current Machine State: " + readMachineState());

        //If the CLI version of the application is not being used, this method can also be used to write the values
        //by calling startProduction(batchid, producttype, quantity, speed) in main().

        //sendCommand(2); // used for testing along with above comment.
    }


    public void sendCommand(int command) throws Exception {
         //the client writes the values from "command" to the node "CNTRL_CMD_NODE_ID"
         //this method can be called at anytime to send commands such as reset, start without the users input.
         //this method is also called in the Main method when user writes a command.
        client.writeValues(
                ImmutableList.of(CNTRL_CMD_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(command), null, null))
        ).get();

     //the client writes the value "true" to the "CMD_CHANGE_REQUEST_NODE_ID" to change the value of "status" in the OPC UA server to
     //    true.
        client.writeValues(
                ImmutableList.of(CMD_CHANGE_REQUEST_NODE_ID),
                ImmutableList.of(new DataValue(new Variant(true), null, null))
        ).get();
    }

    private int readMachineState() throws Exception {
        if (client == null) {
            System.out.println("OPC UA client is not connected or session is null.");
            return -1;
        };

        CompletableFuture<DataValue> futureValue = client.readValue(0, TimestampsToReturn.Both, CURRENT_STATE_NODE_ID);
        DataValue dataValue = futureValue.get();
        Object valueState = dataValue.getValue().getValue();
        Optional<ExpandedNodeId> dataType = dataValue.getValue().getDataType();
        return (Integer) valueState;
    }



    public boolean isValidBatchId(int batchId) throws Exception {
        // checks if the given batchId is within the acceptable range of the beer machines parameters.
        if (batchId  <= 0 || batchId > 65535) {
            return false;
        } else {
            return true;
        }

    }
    public boolean isValidQuantity(int quantity) throws Exception {
        //checks if the given quantity is within the acceptable range of the beer machine parameters.
        if (quantity <= 0 || quantity > 65535) {
            return false;
        } else {
            return true;
        }
    }
    public boolean isValidProductType (int productType) {
        //checks if the product type is within the acceptable range of the beer machine parameters.
        if (productType < 0 || productType >5) {
            return false;
        } else {
            return true;
        }
    };
    public boolean isValidSpeed(int productType, int speed){
        //checks if speed is valid with the given productType.
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


    private void nodeWrite(NodeId nodeId, Variant value) throws Exception {
        //this method writes the value of the inputted batchId, productType etc. to a Variant
        //and writes it to a given nodeId. This method is called in startProduction.
        client.writeValue(nodeId, DataValue.valueOnly(value));
        System.out.println("NodeId:" + nodeId + "\n" + "value:" + value);

    }
}