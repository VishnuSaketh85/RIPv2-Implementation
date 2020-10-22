import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author vishnusaketh
 *
 */

/**
 * The TimeoutManager thread checks if any network/rover has become unreachable after 10 seconds. It maintains a map and
 * updates it every time the rover receives a packet.
 */
public class TimeoutManager extends Thread {
    private ConcurrentHashMap<String, Long> timeoutMap = new ConcurrentHashMap<>();
    public Rover rover;
    public TimeoutManager(Rover rover){
        this.rover = rover;
    }

    /**
     * Updates the time of a given networkAddress every time we receive a packet from that rover
     * @param networkAddress
     */
    public void updateTimeoutMap(String networkAddress){
        long currTime = System.currentTimeMillis() / 1000;
        timeoutMap.put(networkAddress, currTime);
    }

    /**
     * makes all the entries in the router's table whose next hop is the input argument unreachable
     * @param nextHop
     */
    private void updateNextHop(String nextHop){
        List<RoutingTableEntry> routingTableEntries = rover.getRoutingTable();
        for(RoutingTableEntry routingTableEntry : routingTableEntries){
            if(routingTableEntry.nextHop.equals(nextHop)){
                routingTableEntry.cost = RoutingTableEntry.INFINITYCOST;
            }
        }
    }

    public void run(){
        while(true){
            try{
                sleep(10000);
                // checks for all the networkAddresses which have timed out
                List<String> elementsToRemove = new ArrayList<>();
                for(Map.Entry<String, Long> entry : timeoutMap.entrySet()){
                    long currTime = System.currentTimeMillis() / 1000;
                    if(currTime - entry.getValue() > 10){
                        List<RoutingTableEntry> routingTableEntries = rover.getRoutingTable();
                        for(RoutingTableEntry routingTableEntry : routingTableEntries){
                            if(routingTableEntry.networkAddress.equals(entry.getKey())){
                                routingTableEntry.cost = RoutingTableEntry.INFINITYCOST;
                                updateNextHop(routingTableEntry.nextHop);
                                elementsToRemove.add(entry.getKey());
                                rover.printRoutingTable();
                            }
                        }
                    }
                }
                // remove all the elements which have timed out from the map
                for(String s : elementsToRemove){
                    timeoutMap.remove(s);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
