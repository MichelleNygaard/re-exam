package org.example;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.eclipse.milo.opcua.stack.core.serialization.OpcUaBinaryStreamEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Production {
    private final OpcUaClient client;

    public Production() throws ExecutionException, InterruptedException {
        this.client = ConnectionClass.getInstance().client;
        this.client.connect().get();
    }
    public void startProduction(int batchId, int productType, int quantity, int speed)throws Exception{
        //Check machine state
        int currentState = readMachineState();
        System.out.println("Current Machine State: " + currentState);
        //batch id
        NewWriteValue(new NodeId(6, "::Program:Cube.Command.Parameter[0].Value"), batchId);
        //produkt type/id
        NewWriteValue(new NodeId(6,"::Program:Cube.Command.Parameter[1].Value"), productType);
        //Antal
        NewWriteValue(new NodeId(6, "::Program:Cube.Command.Parameter[2].Value"), quantity);
        //Hastighed
        NewWriteValue(new NodeId(6, "::Program:Cube.Command.MachSpeed"), speed);

        //Vent lidt
        TimeUnit.MILLISECONDS.sleep(500);
        try{
            //tjekker at hastighed er inden for den range det specifikke produkt tillader
            if(!isValidSpeed(productType,speed)){
                System.out.println("Denne type tillader ikke denne hastighed");
                return;
            }




        //Send command til at starte
       /* writeValue(new NodeId(2,"Command.CntrlCmd"),2);

        //vent
        TimeUnit.MILLISECONDS.sleep(200);

        writeValue(new NodeId(2, "Command.CmdChangeRequest"), true);

        TimeUnit.MILLISECONDS.sleep(500);

        int newState = readMachineState();
        System.out.println("Machine State After Command: " + newState);

        if(newState == 6){
            System.out.println("Produktion startet");
        } else{
            System.out.println("PRODUKTION ER IKKE STARTET! STATE: " + newState);
        }*/

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Fejler i startproduction");

        }
    }

    public void cmdNode() throws Exception{
        NewWriteValue(new NodeId(6,"::Program:Cube.Command.CntrlCmd"),1);
        NewWriteValue(new NodeId(6, "::Program:Cube.Command.CmdChangeRequest"), true);
        TimeUnit.MILLISECONDS.sleep(1000);

        NewWriteValue(new NodeId(6,"::Program:Cube.Command.CntrlCmd"),2);

        //vent
        TimeUnit.MILLISECONDS.sleep(200);

        NewWriteValue(new NodeId(6, "::Program:Cube.Command.CmdChangeRequest"), true);

        TimeUnit.MILLISECONDS.sleep(500);

        int newState = readMachineState();
        System.out.println("Machine State After Command: " + newState);

        if(newState == 6){
            System.out.println("Produktion startet");
        } else{
            System.out.println("PRODUKTION ER IKKE STARTET! STATE: " + newState);
        }
    }
    private int readMachineState() throws Exception{
        NodeId stateNode = new NodeId(6, "::Program:Cube.Status.StateCurrent");
        DataValue value = this.client.readValue(0, null, stateNode).get();
        try {
            if (this.client == null || this.client.getSession().get() == null) {
                System.out.println("OPC UA klient er ikke connected");
                return -1;
            }

        else if(value.getValue()==null){
                System.out.println("Machine state er null. Kan være et Node problem i OPCUA");
                return -1;
            }
            return (Integer) value.getValue().getValue();
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Fejler i ReadMAchineState)");
            return -1;
        }

    }
    private void NewWriteValue(NodeId nodeId, Object value) {
        try{
            CompletableFuture<Void> future = this.client.writeValue(nodeId, new DataValue(new Variant(value))).thenAccept(v -> System.out.println("Wrote" + value + " to " + nodeId));

            future.get();
        }catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
            System.out.println("fejler i NewWriteValue");
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
