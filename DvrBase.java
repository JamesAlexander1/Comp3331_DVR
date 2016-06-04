import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Map.Entry;



import java.util.TimerTask;


/**
 * Class implement node in a application layer simulation of a network layer distance vector routing protocol on a single machine.
 * @author James Alexander z3459695.
 *
 */
public class DvrBase{

	private char nodeN;
	private int portN;
	private String fileN;
	private Hashtable<Character, Neighbour> neighbours;
	private DatagramSocket socket;
	private boolean hasReturnedDV;
	
	/**
	 * Constructor.
	 * @param nodeName	name of the node.
	 * @param portNum	port number on which node will recieve UDP datagrams on.
	 * @param fileName  name of config file to read from upon initialisation.
	 * @throws IOException
	 */
	public DvrBase(char nodeName, int portNum, String fileName) throws IOException{
		
		nodeN = nodeName;
		portN = portNum;
		fileN = fileName;
		neighbours = new Hashtable<Character, Neighbour>();
		hasReturnedDV = false;
		
		
		try{
			if(! loadFile()){
				System.err.println("Error loadFile failed");
			}else{
				
				
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Getter method returning reference to hashTable containing neighbouring node's data and Distance Vector for this node.
	 * @return neighbours.
	 */
	public Hashtable< Character, Neighbour> getNeighbours(){
		return neighbours;
	}
	/**
	 * Getter method returning reference to Datagram Socket upon which this instance of Dvr communicates on.
	 * @return socket.
	 */
	public DatagramSocket getSocket(){
		return socket;
	}
	/**
	 * Getter method returning reference to port number upon which this instance of Dvr communicates on, and which socket connects to.
	 * @return portN
	 */
	public int getPortNum(){
		return portN;
	}
	/**
	 * Getter method returning reference to name of node.
	 * @return nodeN
	 */
	public char getName(){
		return nodeN;
	}
	
	/**
	 * Method opening and reading config file into object.
	 * @return boolean indicating if operations on file were successful.
	 * @throws IOException
	 */
	public boolean loadFile() throws IOException{
		
		
		
		File configFile = new File(System.getProperty("user.dir") + "/" + fileN);
		//"src/" + fileN --> replace with User_dir for unix: System.getProperty("user.dir") + "/" + fileN
		if(! configFile.exists()){
			
			System.err.println("Error: File Not Found");
			return false;
			
		}else{
			
			String line = null;
			
			try{
				
				BufferedReader reader = new BufferedReader(new FileReader(configFile));
				line = reader.readLine();
				
				
				while((line = reader.readLine()) != null){
					
					String neighbourData[] = line.split(" ");
					
					//initialising distance vector with details of this node and neighbours.
					
					char neighbourName = neighbourData[0].charAt(0);
					double neighbourLink = Double.parseDouble(neighbourData[1]);
					int neighbourPortNum = Integer.parseInt(neighbourData[2]);
					neighbours.put(neighbourName, new Neighbour(neighbourName, neighbourLink, neighbourPortNum));
					
				}
				
				reader.close();
				return true;
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * Main Method for operation of Dvr object. runs until program exits.
	 * @throws IOException
	 */
	public void runDvr() throws IOException{
		
		socket = new DatagramSocket(portN);
		
		//timertask ensures messages sent every 5 seconds.
		
		TimerTask dvrTimer = new DvrTimer(this);
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(dvrTimer , 5000 , 5000);
		
		try{
			
			while(true){
				
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				//socket will wait indefinitely until a packet is received
				
				socket.receive(packet);
				String received = new String(packet.getData(), 0, packet.getLength());
				
				updateDV(received);
				
				//Methods below detail 
				
				if(checkIfStable()){
					if(! hasReturnedDV){
						printDV();
					}
							
				}else{
					
					if(hasReturnedDV){
						
						hasReturnedDV = false;
					}
				}
				
				
			}
		
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			printDV();
			socket.close();
		}
	}
	
	/**
	 * Method taking in Distance Vector messages sent to this node from neighbours and updating this node's distance vector.
	 * @param received	String representation of recieved message (distance vector).
	 * @throws IndexOutOfBoundsException
	 */
	public void updateDV(String received) throws IndexOutOfBoundsException {
		
		try{
			String[] receivedM = received.split("/");
			
			for ( String s : receivedM){
				
				if(! s.equals("")){
				
					char fromNode = receivedM[0].charAt(0);
					String[] distV = receivedM[1].split(":");
					
					//reseting 'heartbeat' message counter for checking status of neighbours.
					
					Neighbour checkNode = neighbours.get(fromNode);
					checkNode.resetHB();
					
					for(String s2 : distV){
						if(! s2.equals("")){
							
							String[] nodeValues = s2.split(" ");
							char node = nodeValues[0].charAt(0);
							Double distToNode = Double.parseDouble(nodeValues[1]);
							
							
							//if this node told another node is dead by neighbour, change data.
							
							if(distToNode == -1.0){
								Neighbour deadNode = neighbours.get(node);
								if(deadNode.DeadNode()){
									
									hasReturnedDV = false;
								}
								removeAllDeadNodes(deadNode.getName());
								
							}else{
							///pass to neighbour class // compute
								
								distToNode = distToNode + neighbours.get(fromNode).getLinkLength();
								
								if(node != nodeN){
									//case 1: node just discovered
									
									if(! neighbours.containsKey(node)){
										neighbours.put(node, new Neighbour(node, distToNode, -1));
										hasReturnedDV = false;
									}
									
									//case 2: node is known. 
									Neighbour n = neighbours.get(node);
									
									if(n.checkAndAdd(fromNode, distToNode)){
										//updateMinDist = true;
										hasReturnedDV = false;
									}
								}
							}
						}
					}
				}
			}
			
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Method called by DvrTimer class on 5 second increments - sends this node's Distance Vector to neighbours.
	 */
	public void sendDV(){
		StringBuilder builder = new StringBuilder();
		try{
			
			builder.append(this.nodeN + "/");
			int portNum = -1;
			
			//part 1: create message.
			
			for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
				Neighbour n = current.getValue();
				builder.append(":" + n.getName() + " " + n.getShortestLength());
			}
			
			//part 2: specify message destination and relevant fields.
			
			for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
				Neighbour n = current.getValue();
				portNum = n.getPortNum();
				
				//if node in DV is NOT a neighbour
				if(portNum != -1){
					//increment HB to help check neighbouring nodes are onling.
					n.incrementHB();
					if(n.getHB() > 2){
						// when a known node in the network has missed three heartbeat messages.
						n.DeadNode();
						removeAllDeadNodes(n.getName());
						hasReturnedDV = false;
						
					}
					
					//finalise and send message.
					
					String message = builder.toString();
					byte[] buffer = message.getBytes();
					InetAddress address = InetAddress.getLocalHost();
					DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, address, portNum);
					socket.send(sendPacket);
				}
			}
			
		}catch (IOException e) {
			
			e.printStackTrace();
		}
			
		
		
		
	}
	/**
	 * Print this node's stable Distance Vector to Standard Output.
	 */
	public void printDV(){
		
		for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
			Neighbour n = current.getValue();
			if(n.getShortestLength() > -1.0){
				
				System.out.print("Shortest path to node " + n.getName() + ": the next hop is ");
				Hashtable <Character, PathObject> paths = n.getPaths();
				
				for(Entry<Character, PathObject> currP : paths.entrySet()){
					
					if(currP.getValue().getDist() == n.getShortestLength()){
						System.out.print(currP.getKey());
					}
				}
				System.out.println(" and the cost is " + n.getShortestLength());
			}
		}
		hasReturnedDV = true;
	}
	/**
	 * Method used to check if this node's distance vector is stable.
	 * @return boolean indicating if Distance Vector is stable or not.
	 */
	public boolean checkIfStable(){
		
		boolean isStable = true;
		
		for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
			Neighbour n = current.getValue();
			
			//after 5 rounds of messages exchanged without change to distance vector, distance vector is considered stable.
			
			if(n.getStableCtr() < 4){
				isStable = false;
			}
		}
		
		return isStable;
		
		
	}
	
	/**
	 * Method updates Distance Vector after node has been found to have failed.
	 * @param aName
	 */
	public void removeAllDeadNodes(char aName){
		for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
			Neighbour n = current.getValue();
			n.removeDeadNodes(aName);
			Character temp = n.getName();
			
			
			if(temp.equals(aName)){
				n.DeadNode();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		
		char nodeName = args[0].charAt(0);
		int portNum = Integer.parseInt(args[1]);
		String fileName = args[2];
		
		DvrBase node = new DvrBase(nodeName, portNum, fileName);
		
		node.runDvr();
		
			
	}
}
