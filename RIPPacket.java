import java.util.List;
import java.util.regex.Pattern;
/**
 * @author vishnusaketh
 *
 */

/**
 * The RIP packet class is used to create RIP v2 packets which are sent and received by rovers. Each RIP packet contains
 * the information about routing tables
 */
public class RIPPacket {
    public static final String responseCommand = "1";
    public static final String requestCommand = "2";
    public static final String version = "2";

    private String command;
    public int roverId;
    private List<RoutingTableEntry> tableEntries;
    public String ipAddress;

    // Each RIPPacket has a command, routing table entries and the rover's ip and id.
    public RIPPacket(String command, List<RoutingTableEntry> tableEntries, int roverId, String ipAddress){
        this.command = command;
        this.tableEntries = tableEntries;
        this.roverId = roverId;
        this.ipAddress = ipAddress;
    }


    public String getNetworkAddress(){
        return "10.0." + roverId + ".0";
    }

    public List<RoutingTableEntry> getTableEntries(){
        return this.tableEntries;
    }


    /**
     * Converts the RIP packet to a byte array so that it can be sent in an UDP packet.
     * @return
     */
    public byte[] getRIPPacketAsByteArray(){

        int size = 4 + (this.tableEntries.size() * 20);
        byte[] message = new byte[size];
        message[0] = this.command.getBytes()[0];

        message[1] = version.getBytes()[0];

        byte[] roverIdBytes = new byte[] {
                (byte)(roverId >>> 8),
                (byte)roverId};
        message[2] = roverIdBytes[0];

        message[3] = roverIdBytes[1];

        int i = 4;
        if(this.command.equals(RIPPacket.responseCommand) || this.command.equals((RIPPacket.requestCommand))) {
            for (RoutingTableEntry routingTableEntry : this.tableEntries){

                message[i++] = (byte)(Integer.parseInt(RoutingTableEntry.addressFamilyId) >>> 8);
                message[i++] = (byte)(Integer.parseInt(RoutingTableEntry.addressFamilyId));
                message[i++] = (byte)(Integer.parseInt(RoutingTableEntry.routeTag) >>> 8);
                message[i++] = (byte)(Integer.parseInt(RoutingTableEntry.routeTag));
                String[] networkAddress =  routingTableEntry.networkAddress.split(Pattern.quote("."));
                String[] nextHop = routingTableEntry.nextHop.split(Pattern.quote("."));
                String[] subnetMask = routingTableEntry.subnetMask.split(Pattern.quote("."));
                byte[] costBytes = new byte[] {
                        (byte)(routingTableEntry.cost >>> 24),
                        (byte)(routingTableEntry.cost >>> 16),
                        (byte)(routingTableEntry.cost >>> 8),
                        (byte)routingTableEntry.cost};
                for(int j = 0; j < 4; j++){
                    message[i + j] = (byte)Integer.parseInt(networkAddress[j]);
                    message[i + 4 + j] = (byte)(Integer.parseInt(subnetMask[j]));
                    message[i + 8 + j] = (byte)Integer.parseInt(nextHop[j]);
                    message[i + 12 + j] = costBytes[j];
                }
                i += 16;
            }
        }

        return message;
    }




}
