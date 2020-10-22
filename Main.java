import java.net.InetAddress;

// Main.java
// (C) 2019 Sam Fryer
// https://github.com/ProfFryer/MulticastTestingEnvironment
// Starts the UdpMulticastClient and the UdpMulticastSender threads
// and then just sits and waits forever.

/**
 * @author vishnusaketh(edited), base code(Sam Fryer)
 *
 */

class Main {
	/**
	 * The main program takes in roverId as an input argument, creates a rover object and starts three threads, the
	 * first thread is for broadcasting the routing tables of the rover as a UDP packet, the second thread for listening
	 * for packets coming from other reachable rovers and third thread is a timeout thread to check if any rovers have
	 * become unreachable.
	 * @param args
	 */
	public static void main(String[] args){
		if (args.length > 0) {
			int roverId = Integer.parseInt(args[0]);
			System.out.println("I'm node " + roverId);
	        try{
	        	InetAddress localhost = InetAddress.getLocalHost();
				String address = (localhost.getHostAddress()).trim();
				Rover rover = new Rover(roverId, address);
				System.out.println("Initial table");
				rover.printRoutingTable();
				System.out.println("Broadcasting from: " + address);

				// Starts the timeoutThread
				TimeoutManager timeoutManager = new TimeoutManager(rover);
				timeoutManager.start();

				// Starting Multicast Receiver with rover and timeoutManager
				System.out.println("Starting Multicast Receiver...");
				Thread client=new Thread(new UdpMulticastClient(63001,"230.230.230.230", rover,
						timeoutManager));
				client.start();


				// Starting Multicast Sender with rover
				System.out.println("Starting Multicast Sender...");
				Thread sender=new Thread(new UdpMulticastSender(63001,"230.230.230.230",rover));
				sender.start();

				while(true){
					Thread.sleep(1000);
				}
	        }
			catch(Exception ex) {
	          ex.printStackTrace();
			}
		}
		else
			System.out.println("No input args! Must specify Node Number!");
	}
}
