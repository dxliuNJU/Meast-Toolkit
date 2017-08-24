package name.dxliu.agent;

public interface OracleAgent {
	/**
	 * 
	 * @author Daxin Liu
	 * This interface is an enclosure of distance oracle(from database or memory). Given two vertexes, any class implements this should answer
	 * the distance between these vertexes.
	 * 
	 */
	public int queryDistance(int source,int target);
}