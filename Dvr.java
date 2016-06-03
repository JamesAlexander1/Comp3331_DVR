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
	
	
	public Dvr(char nodeName, int portNum, String fileName) throws IOException{
		
		nodeN = nodeName;
		portN = portNum;
		fileN = fileName;
		neighbours = new Hashtable<Character, Neighbour>();
		//minDist = new Hashtable<Character, Integer>();
		
		try{
			if(! loadFile()){
				System.err.println("Error loadFile failed");
			}else{
				
				for(Entry<Character, Neighbour> current : neighbours.entrySet()){
					System.out.println("neighbour:" + current.getValue().getName() + " : " + current.getValue().getLinkLength() + " : " + current.getValue().getPortNum());
				
				}
				System.out.println("Constructor closing");
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
					
					for(String temp : neighbourData){
						System.out.println(temp);
					}		
					
					char neighbourName = neighbourData[0].charAt(0);
					int neighbourLink = Integer.parseInt(neighbourData[1]);
					int neighbourPortNum = Integer.parseInt(neighbourData[2]);
					neighbours.put(neighbourName, new Neighbour(neighbourName, neighbourLink, neighbourPortNum));
					//minDist.put(neighbourName, neighbourLink);
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
			while(temp < 5){
				
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				//socket will wait indefinitely until a packet is received
				System.out.println("waiting on packets....");
				socket.receive(packet);
				String received = new String(packet.getData(), 0, packet.getLength());
				System.out.println("recieved: " + received);
				updateDV(received);
				
				temp ++;
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
			boolean updateMinDist = false;
			for ( String s : receivedM){
				
				if(! s.equals("")){
				//System.out.println("message received from: "  + s);
					char fromNode = receivedM[0].charAt(0);
					String[] distV = receivedM[1].split(":");
					for(String s3 : distV){
					//	System.out.print(s3 + ".");
						//System.out.println();
					}
					System.out.println();
					//char fromNode = distV[0].charAt(0);
					//System.out.println("node that sent this datagram: " + fromNode);
					for(String s2 : distV){
						if(! s2.equals("")){
							//System.out.println("s2 = " + s2);
							String[] nodeValues = s2.split(" ");
							char node = nodeValues[0].charAt(0);
							int distToNode = Integer.parseInt(nodeValues[1]);
							//System.out.println(node + ":" + distToNode);
							
							///pass to neighbour class // compute
							distToNode = distToNode + neighbours.get(fromNode).getLinkLength();
							
							boolean isNewShortest = false;
							if(node != nodeN){
								//case 1: node just discovered
								if(! neighbours.containsKey(node)){
									neighbours.put(node, new Neighbour(node, distToNode, -1));
								}
								//case 2: node is known.
								Neighbour n = neighbours.get(node);
								//int distToNode = distToNode + n)
								//isNewShortest = 
								if(isNewShortest = n.checkAndAdd(node, distToNode)){
									updateMinDist = true;
								}
							}
						}
					}
				}
			}
			System.out.println("update complete");
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
				
				//if node in DV is NOT a neighbour
				if(portNum != -1){
						
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
			
		System.out.println("sending concluded");
		
		
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
	}
	
	public static void main(String[] args) throws IOException{
		
		char nodeName = args[0].charAt(0);
		int portNum = Integer.parseInt(args[1]);
		String fileName = args[2];
		System.out.println(nodeName + " " + portNum + " " + fileName);
		Dvr node = new Dvr(nodeName, portNum, fileName);
		//Dvr node = new Dvr(nodeName, portNum, fileName);
		
		System.out.println("running dvr");
		node.runDvr();
		
		
		
		
	}
}
