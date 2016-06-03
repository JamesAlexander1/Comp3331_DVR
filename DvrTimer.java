import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimerTask;
import java.util.Map.Entry;

public class DvrTimer extends TimerTask{

	private Dvr node;
	
	public DvrTimer(Dvr newNode){
		node = newNode;
	}
	/**
	 * @override
	 */
	public void run(){
		node.sendDV();
		//StringBuilder builder = new StringBuilder();
		
		/*try{
			for(Entry<Character, Neighbour> current : node.getNeighbours().entrySet()){
				int portNum = current.getValue().getPortNum();
				String message = builder.append(node.getName() + " on " + node.getPortNum() + " with distance: " + current.getValue().getLinkLength()).toString();
				byte[] buffer = message.getBytes();
				//InetAddress address = 127.0.0.1;
				InetAddress address = InetAddress.getLocalHost();
				DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, address, portNum);
				
				try {
					node.getSocket().send(sendPacket);
					System.out.println("message sent successfully");
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			System.out.println("sending concluded");
		}catch(UnknownHostException e){
			e.printStackTrace();
		}*/
		
	}
}
