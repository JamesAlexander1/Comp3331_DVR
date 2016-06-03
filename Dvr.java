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
import java.util.Hashtable;
import java.util.Timer;
import java.util.Map.Entry;



import java.util.TimerTask;

public class Dvr{

	private char nodeN;
	private int portN;
	private String fileN;
	private Hashtable<Character, Neighbour> neighbours;
	private DatagramSocket socket;
	
	public Dvr(char nodeName, int portNum, String fileName) throws IOException{
		
		nodeN = nodeName;
		portN = portNum;
		fileN = fileName;
		neighbours = new Hashtable<Character, Neighbour>();
		
		try{
			if(! loadFile()){
				System.err.println("Error loadFile failed");
			}else{
				
				for(Entry<Character, Neighbour> current : neighbours.entrySet()){
					System.out.println("neighbour:" + current.getKey() + " : " + current.getValue().getLinkLength() + " : " + current.getValue().getPortNum());
				
				}
				System.out.println("Constructor closing");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public Hashtable<Character, Neighbour> getNeighbours(){
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
			while(temp < 10){
				
				//sending out data to neighbours
				//if(commands.equals("send")){
					//sendDV();
				//}
				/*commands = fromStdin.readLine();
				if(commands.equals("end")){
					break;
				}*/
				byte[] buffer = new byte[256];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				
				//socket will wait indefinitely until a packet is received
				System.out.println("waiting on packets....");
				socket.receive(packet);
				String received = new String(packet.getData(), 0, packet.getLength());
				String[] receivedM = received.split("/");
				for ( String s : receivedM){
					System.out.println("message recieved from: "  + s);
				}
				//System.out.println("message recieved from: "  + received);
				temp ++;
			}
			socket.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	
	public void sendDV(){
		StringBuilder builder = new StringBuilder();
		try{
			//String message = "/" + this.nodeN;
			builder.append("/" + this.nodeN);
			int portNum = -1;
			for(Entry<Character, Neighbour> current : this.getNeighbours().entrySet()){
				portNum = current.getValue().getPortNum();
				//message = builder.append("/" + nodeN + " on " + portN + " with distance " + current.getValue().getLinkLength()).toString() + " to " + current.getValue().getName() + " | ";
				//builder.append(" ->" + current.getValue().getName());
				String message = builder.toString();
				byte[] buffer = message.getBytes();
				//InetAddress address = 127.0.0.1;
				InetAddress address = InetAddress.getLocalHost();
				DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, address, portNum);
				socket.send(sendPacket);
				System.out.println("message sent successfully");
			}	
		}catch (IOException e) {
			
			e.printStackTrace();
		}
			
		System.out.println("sending concluded");
		
		
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
