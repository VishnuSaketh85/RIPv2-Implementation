import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// Code originally from:
//https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
//
// edited by Sam Fryer.

/**
 * @author vishnusaketh(edited), base code(Sam Fryer)
 *
 */
public class UdpMulticastSender implements Runnable  {

   public int port = 63001; // port to send on
   public String broadcastAddress; // multicast address to send on
   public int node = 0; // the arbitrary node number of this executable
   public Rover rover; // Rover object
   // standard constructor
   public UdpMulticastSender(int thePort, String broadcastIp, Rover rover)
   {
      port = thePort;
      broadcastAddress = broadcastIp;
      this.rover = rover;
   }
  
   // Send the UDP Multicast message
   public void sendUdpMessage(RIPPacket ripPacket) throws IOException {
       // Converts the RIPPacket to a byte array
       byte[] message = ripPacket.getRIPPacketAsByteArray();
       DatagramSocket socket = new DatagramSocket();
       InetAddress group = InetAddress.getByName(broadcastAddress);
      

       DatagramPacket packet = new DatagramPacket(message, message.length, group, port);


       socket.send(packet);
       socket.close();
   }

   // the thread runnable.  Starts sending packets every 500ms.
   @Override
   public void run(){
      while (true)
      {
        try {
            // Creates a RIPPacket with request command if it has no entries in it's routing table else it creates
            // RIPPacket with response command.
            RIPPacket ripPacket = null;
            if (rover.getRoutingTable().size() == 1) {
                ripPacket = new RIPPacket(RIPPacket.requestCommand, rover.getRoutingTable(), rover.getRoverId(),
                        rover.ipAddress);
            }
            else {
                ripPacket = new RIPPacket(RIPPacket.responseCommand, rover.getRoutingTable(), rover.getRoverId(),
                        rover.ipAddress);
            }
            sendUdpMessage(ripPacket);
            Thread.sleep(500);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
      }
   }
}
