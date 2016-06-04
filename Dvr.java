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

public class Dvr{

	private char nodeN;
	private int portN;
	private String fileN;
	private Hashtable<Character, Neighbour> neighbours;
	//private Hashtable<Character, Integer> minDist;
	private DatagramSocket socket;
	private boolean hasReturnedDV;
	
	public Dvr(char nodeName, int portNum, String fileName) throws IOException{
		
		nodeN = nodeName;
		portN = portNum;
		fileN = fileName;
		neighbours = new Hashtable<Character, Neighbour>();
		hasReturnedDV = false;
		//minDist = new Hashtable<Character, Integer>();
		
		try{
			if(! loadFile()){
				System.err.println("Error loadFile failed");
			}else{
				
				//for(Entry<Character, Neighbour> current : neighbours.entrySet()){
					//System.out.println("neighbour:" + current.getValue().getName() + " : " + current.getValue().getLinkLength() + " : " + current.getValue().getPortNum());
				
				//}
				//System.out.println("Constructor closing");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public Hashtable< Character, Neighbour> getNeighbours(){
		return neighbours;
	}
	public DatagramSocket getSocket(){
		return socket;
	}
	public int getPortNum(){
		return portN;
	}
	public char getName(){
		return nodeN;
	}
	
	public boolean loadFile() throws IOException{
		
		//create seperate method for poison reverse (i.e 2 values);
		
		File configFile = new File("src/" + fileN);
		//"src/" + fileN --> replace with fileN
		if(! configFile.exists()){
			
			System.err.println("Error: File Not Found");
			return false;
			
		}else{
			
			String line = null;
			
			try{
				
				BufferedReader reader = new BufferedReader(new FileReader(configFile));
				line = reader.readLine();
				System.out.println("number of neighbours = 2");
				
				while((line = reader.readLine()) != null){
					
					String neighbourData[] = line.split(" ");
					
					//for(String temp : neighbourData){
						//System.out.println(temp);
					//}		
					
					char neighbourName = neighbourData[0].charAt(0);
					int neighbourLink = Integer.parseInt(neighbourData[1]);
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
	
	public void runDvr() throws IOException{
		
		socket = new DatagramSocket(portN);
		BufferedReader fromStdin = new BufferedReader(new InputStreamReader(System.in));
		TimerTask dvrTimer = new DvrTimer(this);
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(dvrTimer , 10000 , 10000);
		try{
			String commands;
			int temp = 0;
			while(true){
				
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				//socket will wait indefinitely until a packet is received
				//System.out.println("waiting on packets....");
				socket.receive(packet);
				String received = new String(packet.getData(), 0, packet.getLength());
				//System.out.println("recieved ");
				updateDV(received);
				//printDV();
				if(checkIfStable()){
					if(! hasReturnedDV){
						printDV();
					}
							//printDV();
				}else{
					//System.out.println("not stable, has returned");
					if(hasReturnedDV){
						//System.out.println("not stable, has returned");
						//printDV();
						hasReturnedDV = false;
					}
				}
				
				//temp ++;
			}
			//printDV();
			
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			printDV();
			socket.close();
		}
	}
	
	public void updateDV(String received) throws IndexOutOfBoundsException {
		
		try{
			String[] receivedM = received.split("/");
			//boolean updateMinDist = false;
			for ( String s : receivedM){
				
				if(! s.equals("")){
				//System.out.println("message received from: "  + s);
					char fromNode = receivedM[0].charAt(0);
					String[] distV = receivedM[1].split(":");
					
					//reseting 'heartbeat' message counter for checking status of neighbours.
					Neighbour checkNode = neighbours.get(fromNode);
					checkNode.resetHB();
					//
					for(String s2 : distV){
						if(! s2.equals("")){
							//System.out.println("s2 = " + s2);
							String[] nodeValues = s2.split(" ");
							char node = nodeValues[0].charAt(0);
							int distToNode = Integer.parseInt(nodeValues[1]);
							//System.out.println(node + ":" + distToNode);
							
							//if this node told another node is dead by neighbour, change data.
							if(distToNode == -1){
								Neighbour deadNode = neighbours.get(node);
								if(deadNode.DeadNode()){
									
									hasReturnedDV = false;
								}
								removeAllDeadNodes(deadNode.getName());
								//System.out.println("recieved -1");
								//hasReturnedDV = false;
							}else{
							///pass to neighbour class // compute
								distToNode = distToNode + neighbours.get(fromNode).getLinkLength();
								
								//boolean isNewShortest = false;
								if(node != nodeN){
									//case 1: node just discovered
									if(! neighbours.containsKey(node)){
										neighbours.put(node, new Neighbour(node, distToNode, -1));
										hasReturnedDV = false;
									}
									//case 2: node is known. /// node
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
			//System.out.println("update complete");
		}catch(IndexOutOfBoundsException e){
			e.printStackTrace();
		}
	}
	
	public void sendDV(){
		StringBuilder builder = new StringBuilder();
		try{
			//String message = "/" + this.nodeN;
			builder.append(this.nodeN + "/");
			int portNum = -1;
			
			for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
				Neighbour n = current.getValue();
				builder.append(":" + n.getName() + " " + n.getShortestLength());
			}											//n.getLinkLength
			for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
				Neighbour n = current.getValue();
				portNum = n.getPortNum();
				//n.incrementHB();
				//if node in DV is NOT a neighbour
				if(portNum != -1){
					//increment HB to help check neighbouring nodes are onling.
					n.incrementHB();
					if(n.getHB() > 2){
						//System.out.println("heartbeat message counter detected neighbouring node failure : " + n.getName());
						n.DeadNode();
						removeAllDeadNodes(n.getName());
						hasReturnedDV = false;
						
					}
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
			
		//System.out.println("sending concluded");
		
		
	}
	public void printDV(){
		for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
			Neighbour n = current.getValue();
			System.out.print(n.getName());
			Hashtable <Character, PathObject> paths = n.getPaths();
			for(Entry<Character, PathObject> currP : paths.entrySet()){
				System.out.print(" via " + currP.getKey() + " : " + currP.getValue().getDist() + "|");
			}
			System.out.println("|shortest: " + n.getShortestLength());
			
		}
		hasReturnedDV = true;
	}
	public boolean checkIfStable(){
		boolean isStable = true;
		for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
			Neighbour n = current.getValue();
			//System.out.print( n.getName() + ":" + n.getStableCtr());
			if(n.getStableCtr() < 4){
				isStable = false;
			}
		}
		//System.out.println();
		return isStable;
		
		
	}
	public void removeAllDeadNodes(char aName){
		for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
			Neighbour n = current.getValue();
			n.removeDeadNodes(aName);
			Character temp = n.getName();
			
			//just added
			if(temp.equals(aName)){
				n.DeadNode();
			}
		}
	}
	public static void main(String[] args) throws IOException{
		
		char nodeName = args[0].charAt(0);
		int portNum = Integer.parseInt(args[1]);
		String fileName = args[2];
		//System.out.println(nodeName + " " + portNum + " " + fileName);
		Dvr node = new Dvr(nodeName, portNum, fileName);
		//Dvr node = new Dvr(nodeName, portNum, fileName);
		
		//System.out.println("running dvr");
		node.runDvr();
		
			
	}
}
