package name.dxliu.agent;

import java.util.List;

/**
 * 
 * @author Daxin Liu
 * This interface is an enclosure of entity-relation graph(from database or memory). Given an vertex-id, any class implements this should answer
 * the neighborhood information of that vertex.
 * 
 */
public interface GraphAgent {
	/**
	 * @param id vertex id
	 * @return A array of length 2. int[0] represent the neighbor's id. the int[1] represent the id of the edge which link the Node_id and it's neighbor.
	 * Note that edges taking the specific vertex as in-node or out-node should both returned.
	 */
	public List<int[]> getNeighborInfo(Integer id);
}
