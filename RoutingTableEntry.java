public class RoutingTableEntry {
    public static final String addressFamilyId = "2";
    public static final String routeTag = "1";
    public static final int INFINITYCOST = 16;
    String subnetMask = "255.255.255.0";

    public String nextHop;
    public String networkAddress;
    public int cost;
    public int roverId;

    public RoutingTableEntry(String nextHop, String networkAddress, int cost, int roverId){
        this.nextHop = nextHop;
        this.networkAddress = networkAddress;
        this.cost = cost;
        this.roverId = roverId;
    }

    public void printEntry(){
        String entry = networkAddress + "/24     " + nextHop + "(" + roverId + ")" + "       " + cost;
        System.out.println(entry);
    }

}
