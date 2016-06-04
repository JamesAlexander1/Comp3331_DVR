import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

public class Neighbour {

	private char name;
	private int shortestLength; //originally link
	private int directLink;
	private int secondLength;
	private int portN;
	private int heartbeat;
	private int stableCtr;
	
	//private Hashtable<Character, ArrayList<PathObject>> paths;
	private Hashtable<Character, PathObject> paths;
	
	public Neighbour(char aName, int aLength, int aPortN){
		
		this(aName, aLength, -1, aPortN);
	}
	
	public Neighbour(char aName, int aLength, int sLength, int aPortN){
		
		name = aName;
		shortestLength = aLength;
		directLink = aLength;
		secondLength = sLength;
		portN = aPortN;
		paths = new Hashtable<Character, PathObject>();
		paths.put(aName, new PathObject(aName, aLength));
		heartbeat = 0;
		stableCtr = 0;
		//paths.get(aName).put(aName, new PathObject(aName, aLength));
	}
	
	public char getName(){
		return name;
	}
	public int getLinkLength(){
		return directLink;
	}
	public int getShortestLength(){
		return shortestLength;
	}
	public int getPortNum(){
		return portN;
	}
	public Hashtable<Character, PathObject> getPaths(){
		return paths;
	}
	
	public boolean checkAndAdd(char node, int length){
		
		boolean isNewShortestLink = false;
		//case 1: no path Object from current node to node past as argument.
		//int distanceCheck = length + directLink;
		if(! paths.containsKey(node)){
			paths.put(node, new PathObject(node, length));
			
			if(length < shortestLength){
				shortestLength = length;
				isNewShortestLink = true;
			}
			if(shortestLength != -1){
				stableCtr = 0;
			}
			//stableCtr = 0;
			
		}else{
		//case 2: path Object from current node to argument node exists:
			//int distanceCheck = length + directLink;
			PathObject currentPath = paths.get(node);
			//case 2.1 : path from current node to argument node is shorter than dist. currently stored.
			
			if(length < currentPath.getDist()){
				currentPath.setDist(length);
				if(length < shortestLength){
					shortestLength = length;
					isNewShortestLink = true;
					
					//stableCtr = 0;
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
	public boolean DeadNode(){
		if(shortestLength != -1){
			paths.clear();
			shortestLength = -1;
			portN = -1;
			System.out.println("ctr:" + stableCtr);
			return true;
		}else{
			return false;
		}
	}
	
	public void incrementHB(){
		heartbeat ++;
	}
	public void resetHB(){
		heartbeat = 0;
	}
	public int getHB(){
		return heartbeat;
	}
	public int getStableCtr(){
		return stableCtr;
	}
	public void removeDeadNodes(char aName){
		/*for( Entry<Character, PathObject> current: paths.entrySet()){
			PathObject p = current.getValue();
			
				
			Character temp = p.getViaNode();
			if(temp.equals(aName)){
			 paths.r
			}
		}*/
		paths.remove(aName);
		replaceShortestDist();
		
	}
	public void replaceShortestDist(){
		int shortestDistance = 0;
		for( Entry<Character, PathObject> current: paths.entrySet()){
			PathObject p = current.getValue();
			if(shortestDistance == 0){
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
