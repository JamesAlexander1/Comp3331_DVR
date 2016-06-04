
/**
 * Class used as part of Distance Vector implementation. Representing the individual values (cells) in DV.
 * @author James Alexander z3459695.
 *
 */
public class PathObject {

	private char viaNode;
	private Double distance;
	
	/**
	 * Constructor.
	 * @param node 	name of node corresponding to this entry in distance vector.
	 * @param d		distance to node.
	 */
	public PathObject(char node, Double d){
		viaNode = node;
		distance = d;
	}
	/**
	 * Getter method returning reference node corresponding to this entry in distance vector.
	 * @return viaNode.
	 */
	public char getViaNode(){
		return viaNode;
	}
	/**
	 * Getter method returning reference distance to correspoding node (i.e represents specific value in DV).
	 * @return distance.
	 */
	public Double getDist(){
		return distance;
	}
	/**
	 * Setter method to change value of node variable.
	 * @param node	new name of node.
	 */
	public void setViaNode(char node){
		viaNode = node;
	}
	/**
	 * Setter method to change value of distance variable.
	 * @param d	new distance to node.
	 */
	public void setDist(Double d){
		distance = d;
	}
}
