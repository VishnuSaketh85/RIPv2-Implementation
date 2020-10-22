import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Code originally from:
//https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
//
// edited by Sam Fryer.

/**
 * @author vishnusaketh(edited), base code(Sam Fryer)
 *
 */
public class UdpMulticastClient implements Runnable {
    public int port = 63001; // port to listen on
    public String broadcastAddress; // multicast address to listen on
    public Rover rover; // Rover object which parses incoming data
    TimeoutManager timeoutManager; // TimeoutManager thread which updates entries
   // standard constructor
   public UdpMulticastClient(int thePort, String broadcastIp, Rover rover, TimeoutManager timeoutManager) {
       port = thePort;
       broadcastAddress = broadcastIp;
       this.rover = rover;
       this.timeoutManager = timeoutManager;
   }

   // listens to the ip address and reports when a message arrived
   public void receiveUDPMessage() throws IOException {
        byte[] buffer=new byte[1024];

      // create and initialize the socket
        MulticastSocket socket=new MulticastSocket(port);
        InetAddress group=InetAddress.getByName(broadcastAddress);
        socket.joinGroup(group);

      
        while(true){
            try {
                DatagramPacket packet=new DatagramPacket(buffer,buffer.length);

	            // blocking call.... waits for next packet
                socket.receive(packet);
                byte[] incomingPacket = packet.getData();
                String inetAddress = packet.getAddress().toString().substring(1);
                String end = new String("hi"); // Dummy string

                // skip packets coming from same IP
                if(inetAddress.equals(rover.ipAddress)){
                    continue;
                }
                // Parses the incoming byte array and returns a RIP packet
                RIPPacket receivedPacket = rover.parseIncomingData(incomingPacket, inetAddress);
                // Updates the map in timeoutManager
                timeoutManager.updateTimeoutMap("10.0." + receivedPacket.roverId + ".0");
                if(rover.updateRoutingTable(receivedPacket)){
                    rover.printRoutingTable();
                    System.out.println();
                }
                // give us a way out if needed
                if("EXIT".equals(end)) {
                    break;
                }
            }catch(IOException ex){
                ex.printStackTrace();
            }
         }

        //close up ship
        socket.leaveGroup(group);
        socket.close();
   }

   // the thread runnable.  just starts listening.
   @Override
   public void run(){
       try {
           receiveUDPMessage();
       }
       catch(IOException ex){
           ex.printStackTrace();
       }
   }

}
