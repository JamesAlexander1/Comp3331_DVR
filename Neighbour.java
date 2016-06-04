import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * Class constituting part of a Distance Vector for a instance of Dvr.
 * @author James Alexander z3459695.
 *
 */
public class Neighbour {

	
	private char name;
	private Double shortestLength; //originally link
	private Double directLink;
	private Double secondLength;
	private int portN;
	private int heartbeat;
	private int stableCtr;
	
	
	private Hashtable<Character, PathObject> paths;
	
	public Neighbour(char aName, Double aLength, int aPortN){
		
		this(aName, aLength, (double) -1, aPortN);
	}
	
	/**
	 * Constructor.
	 * @param aName		name of neighbouring node in row of Distance Vector.
	 * @param aLength	distance from current node to neighbouring node.
	 * @param sLength	not used.
	 * @param aPortN	port number on which neighbouring node can receive UDP datagrams.
	 */
	public Neighbour(char aName, Double aLength, Double sLength, int aPortN){
		
		name = aName;
		shortestLength = aLength;
		directLink = aLength;
		secondLength = sLength;
		portN = aPortN;
		paths = new Hashtable<Character, PathObject>();
		paths.put(aName, new PathObject(aName, aLength));
		heartbeat = 0;
		stableCtr = 0;
		
	}
	
	/**
	 * Getter method returning reference to neighbouring node's name
	 * @return name.
	 */
	public char getName(){
		return name;
	}
	/**
	 * Getter method returning reference to link length between neighbouring node and instance of Dvr.
	 * @return directLink.
	 */
	public Double getLinkLength(){
		return directLink;
	}
	/**
	 * Getter method returning reference to shortest distance(minDist) to this node (whether its a neighbour or not) from instance of Dvr.
	 * @return shortestLength.
	 */
	public Double getShortestLength(){
		return shortestLength;
	}
	/**
	 * Getter method returning reference to port number on which to communicate with node.
	 * @return
	 */
	public int getPortNum(){
		return portN;
	}
	/**
	 * Getter method returning reference to row of distance vector corresponding to this node in the network.
	 * @return paths.
	 */
	public Hashtable<Character, PathObject> getPaths(){
		return paths;
	}
	/**
	 * Method adding values from recieved distance vector to this Dvr's distance vector.
	 * @param node	name of node which corresponds to distance past into this method.
	 * @param length distance to specified node + link distance to neigbouring node for this instance of Dvr.
	 * @return boolean indicating if new minDist(shortest path) was found.
	 */
	public boolean checkAndAdd(char node, Double length){
		
		boolean isNewShortestLink = false;
		//case 1: no path Object from current node to node past as argument.
		
		if(! paths.containsKey(node)){
			paths.put(node, new PathObject(node, length));
			
			if(length < shortestLength){
				shortestLength = length;
				isNewShortestLink = true;
			}
			if(shortestLength != -1){
				stableCtr = 0;
			}
			
			
		}else{
		//case 2: path Object from current node to argument node exists:
			
			PathObject currentPath = paths.get(node);
			//case 2.1 : path from current node to argument node is shorter than dist. currently stored.
			
			if(length < currentPath.getDist()){
				currentPath.setDist(length);
				if(length < shortestLength){
					shortestLength = length;
					isNewShortestLink = true;
					
					
				}
				if(shortestLength != -1){
					stableCtr = 0;
				}
			}else{
				//case 2.2: do nothing.
				if(stableCtr < 4){
					stableCtr ++;
				}
			}
			
		}
			
		return isNewShortestLink;
	}
	/**
	 * Method cleaning Distance Vector after known node in network has been found to have failed.
	 * @return boolean indicating if distance vector has already been found to have failed.
	 */
	public boolean DeadNode(){
		if(shortestLength != -1.0){
			paths.clear();
			shortestLength = -1.0;
			portN = -1;
			
			return true;
		}else{
			return false;
		}
	}
	/**
	 * Method used to increment number of 'heartbeat' messages not recieved by node.
	 */
	public void incrementHB(){
		heartbeat ++;
	}
	/**
	 * Method used to reset  number of 'heartbeat' messages not recieved by node - i.e when node does finally respond.
	 */
	public void resetHB(){
		heartbeat = 0;
	}
	/**
	 *  Getter method returning reference to 'heartbeat' counter.
	 * @return
	 */
	public int getHB(){
		return heartbeat;
	}
	/**
	 * Getter method returning reference to number of messages recieved that have not changed the distance vector.
	 * @return stableCtr;
	 */
	public int getStableCtr(){
		return stableCtr;
	}
	/**
	 * Another method used to adjust entire Distance Vector after node failure.
	 * @param aName name of failed node in network.
	 */
	public void removeDeadNodes(char aName){
		
		paths.remove(aName);
		replaceShortestDist();
		
	}
	/**
	 * Method called during process of removing failed node from Distance Vector. 
	 * Used to reset minDist Vector entry to next shortest valid distance.
	 */
	public void replaceShortestDist(){
		
		Double shortestDistance = 0.0;
		for( Entry<Character, PathObject> current: paths.entrySet()){
			PathObject p = current.getValue();
			if(shortestDistance == 0.0){
				shortestDistance = p.getDist();
			}else{
				if(p.getDist() < shortestDistance){
					shortestDistance = p.getDist();
				}
			}
		}
		shortestLength = shortestDistance;
	}
}
