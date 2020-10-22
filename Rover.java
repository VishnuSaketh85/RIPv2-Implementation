import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author vishnusaketh
 *
 */
public class Rover {
    /**
     * A rover class which has unique identifier roverId, it's IpAddress and the networkAddress(based on its roverId)
     */
    public int roverId;
    private String networkAddress;
    public String ipAddress;
    List<RoutingTableEntry> routingTable = Collections.synchronizedList(new ArrayList<>());

    // Standard constructor which also adds the first entry into it's routing table, it's own routing entry with cost 0
    public Rover(int roverId, String ipAddress){
        this.roverId = roverId;
        this.networkAddress = "10.0." + roverId + ".0";
        this.ipAddress = ipAddress;
        RoutingTableEntry selfEntry = new RoutingTableEntry(ipAddress, networkAddress, 0, roverId);
        routingTable.add(selfEntry);
    }

    public List<RoutingTableEntry> getRoutingTable(){
        return routingTable;
    }

    public int getRoverId(){
        return this.roverId;
    }

    /**
     * The rover receives a RIP packet and updates the it's routing tables according to the Bellman-Ford Algorithm
     * @param ripPacket
     * @return
     */
    public boolean updateRoutingTable(RIPPacket ripPacket){
        boolean updated = isPresent(ripPacket);

        List<RoutingTableEntry> newRoutingTableEntries = ripPacket.getTableEntries();
        for(RoutingTableEntry incomingRoutingTableEntry: newRoutingTableEntries){
            boolean contains = false;
            for(RoutingTableEntry currRoutingTableEntry : routingTable){
                if(currRoutingTableEntry.networkAddress.equals(incomingRoutingTableEntry.networkAddress)){
                    contains = true;
                    if(currRoutingTableEntry.nextHop.equals(ripPacket.ipAddress)){
                        int newCost = Math.min(incomingRoutingTableEntry.cost + 1, RoutingTableEntry.INFINITYCOST);
                        if(currRoutingTableEntry.cost != newCost) {
                            currRoutingTableEntry.cost = newCost;
                            updated = true;
                        }
                    }
                    else{
                        if(1 + incomingRoutingTableEntry.cost < currRoutingTableEntry.cost){
                            currRoutingTableEntry.cost = 1 + incomingRoutingTableEntry.cost;
                            currRoutingTableEntry.nextHop = ripPacket.ipAddress;
                            currRoutingTableEntry.roverId = ripPacket.roverId;
                            updated = true;
                        }
                    }
                }
            }

            if(!contains){
                routingTable.add(new RoutingTableEntry(ripPacket.ipAddress, incomingRoutingTableEntry.networkAddress,
                        incomingRoutingTableEntry.cost + 1, ripPacket.roverId));
                updated = true;
            }


        }

        return updated;
    }

    /**
     * Checks if the incoming networkAddress is in our routing table, if not then adds it to routing table with a cost
     * of 1 and returns if the table was updated or not.
     * @param ripPacket
     * @return
     */
    private boolean isPresent(RIPPacket ripPacket){
        boolean isPresent = false;
        boolean updated = false;
        // first check if the networkAddress is in our routing table
        for(RoutingTableEntry routingTableEntry : routingTable){
            if(routingTableEntry.networkAddress.equals(ripPacket.getNetworkAddress())){
                isPresent = true;
                routingTableEntry.nextHop = ripPacket.ipAddress;
                routingTableEntry.roverId = ripPacket.roverId;
                if(routingTableEntry.cost != 1) {
                    routingTableEntry.cost = 1;
                    updated = true;
                }
            }
        }
        if(!isPresent){
            routingTable.add(new RoutingTableEntry(ripPacket.ipAddress,
                    ripPacket.getNetworkAddress(), 1, ripPacket.roverId));
            updated = true;
        }
        return updated;
    }

    /**
     * Parses incoming data. Takes in a byte array and return a corresponding RIP packet.
     * @param message
     * @param inetAddress
     * @return
     */
    public RIPPacket parseIncomingData(byte[] message, String inetAddress){
        byte[] commandBytes = new byte[]{message[0]};
        String command  = new String(commandBytes, StandardCharsets.UTF_8);

        byte[] roverIdBytes = new byte[]{(byte)(0), (byte)(0), message[2], message[3]};
        int roverId = ByteBuffer.wrap(roverIdBytes).getInt();

        List<RoutingTableEntry> newEntries = new ArrayList<>();
        int index = 4;

        while(index < message.length){
            index += 4;
            byte[] networkAddressBytes = new byte[4];
            byte[] nextHopBytes = new byte[4];
            byte[] subnetMaskBytes = new byte[4];
            byte[] costBytes = new byte[4];
            for(int j = 0; j < 4; j++){
                networkAddressBytes[j] = message[index + j];
                subnetMaskBytes[j] = message[index + j + 4];
                nextHopBytes[j] = message[index + j + 8];
                costBytes[j] = message[index + j + 12];
            }
            String networkAddress = bytesToStringIP(networkAddressBytes);
            String nextHop = bytesToStringIP(nextHopBytes);
            String subnetMask = bytesToStringIP(subnetMaskBytes);
            int cost = ByteBuffer.wrap(costBytes).getInt();
            if(!networkAddress.equals("0.0.0.0"))
                newEntries.add(new RoutingTableEntry(nextHop, networkAddress, cost, roverId));
            index += 16;
        }

        return new RIPPacket(command, newEntries, roverId, inetAddress);
    }

    /**
     * converts an array on bytes to string
     * @param bytes
     * @return
     */
    public String bytesToStringIP(byte[] bytes){
        return (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "."  +
                (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
    }

    /**
     * Prints the routing table
     */
    public void printRoutingTable(){
        System.out.println("Address         Next Hop        Cost");
        System.out.println("====================================");
        for(RoutingTableEntry routingTableEntry : routingTable){
            routingTableEntry.printEntry();
        }
    }


}
