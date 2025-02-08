package org.example;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Production {
    private final OpcUaClient client;

    public Production(){
        this.client = ConnectionClass.getInstance().client;
    }
    public void startProduction(int batchId, int productType, int quantity, int speed){
        try{
            //tjekker at hastighed er inden for den range det specifikke produkt tillader
            if(!isValidSpeed(productType,speed)){
                System.out.println("Denne type tillader ikke denne hastighed");
                return;
            }

            //Check machine state
            int currentState = readMachineState();
            System.out.println("Current Machine State: " + currentState);
        //batch id
        writeValue(new NodeId(2, "Command.Parameter[0]"), batchId);
        //produkt type/id
        writeValue(new NodeId(2,"Command.Parameter[1]"), productType);
        //Antal
        writeValue(new NodeId(2, "Command.Parameter[2]"), quantity);
        //Hastighed
        writeValue(new NodeId(2, "Command.MachSpeed"), speed);

        //Vent lidt
        TimeUnit.MILLISECONDS.sleep(500);

        //Send command til at starte
        writeValue(new NodeId(2,"Command.CntrlCmd"),2);

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
        }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int readMachineState(){
        try{
            if(client==null || !client.getSession().isPresent()){
                System.out.println("OPC UA klient er ikke connected");
                return -1;
            }
            NodeId stateNode = new NodeId(2, "Status.StateCurrent");
            DataValue value = client.readValue(0, null, stateNode).get();

            if(value.getValue()==null){
                System.out.println("Machine state er null. Kan være et Node problem i OPCUA");
                return -1;
            }
            return (Integer) value.getValue().getValue();
        } catch(Exception e){
            e.printStackTrace();
            return -1;
        }

    }
    private void writeValue(NodeId nodeId, Object value) {
        try{
            CompletableFuture<Void> future = client.writeValue(nodeId, new DataValue(new Variant(value))).thenAccept(v -> System.out.println("Wrote" + value + " to " + nodeId));

            future.get();
        }catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
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
