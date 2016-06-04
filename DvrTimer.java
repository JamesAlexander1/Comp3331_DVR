import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.TimerTask;
import java.util.Map.Entry;

/**
 * Method extending TimerTask for purposes of sending out Distance Vector to neighbours at 5 second intervals.
 * @author James Alexander z3459695
 *
 */
public class DvrTimer extends TimerTask{

	private DvrBase node;
	
	/**
	 * Constructor.
	 * @param newNode is current node.
	 */
	public DvrTimer(DvrBase newNode){
		node = newNode;
	}
	/**
	 * @override
	 */
	public void run(){
		
		node.sendDV();
		
		
	}
}
