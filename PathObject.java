
public class PathObject {

	private char viaNode;
	private int distance;
	
	public PathObject(char node, int d){
		viaNode = node;
		distance = d;
	}
	public char getViaNode(){
		return viaNode;
	}
	public int getDist(){
		return distance;
	}
	public void setViaNode(char node){
		viaNode = node;
	}
	public void setDist(int d){
		distance = d;
	}
}
