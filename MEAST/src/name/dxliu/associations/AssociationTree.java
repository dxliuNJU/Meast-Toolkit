package name.dxliu.associations;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.traverse.ClosestFirstIterator;

import name.dxliu.bean.IntegerEdge;
import name.dxliu.jgrapht.CheckerDepthFirstIterator;
import name.dxliu.util.TimeCounter;
/**
 * This is an enclosure of association node,meanwhile it provides several operation on a association tree.
 * @author Daxin Liu
 *
 */

public class AssociationTree {	
	
	private	 AssociationNode startNodes;//root node
	private List<Integer> canonicalCode;
	private int diameter;
	
	public AssociationNode getStartNodes(){return startNodes;}
	public void setStartNodes(AssociationNode startNodes){this.startNodes = startNodes;}	
	public List<Integer> getCanonicalCode(){return canonicalCode;	}
	public int getDiameter() {return diameter;	}
	
	/**
	 * given a root of a association node,this methods enclose it.
	 * @param root a rooted node.
	 * @return a enclosed association tree
	 */
	public static AssociationTree newInstance(AssociationNode root){
		AssociationTree tree = new AssociationTree();
		tree.startNodes = root;	
		tree.canonicalCode = tree.startNodes.getCanonicalCode();
		tree.diameter=-1;//Notice;
		return tree;
	}
	
	
	@Override
	public String toString() {
		return "AssociationTree [startNodes=" + startNodes + ", canonicalCode="
				+ canonicalCode + ", diameter=" + diameter + "]";
	}
	
	/**
	 * @param timeCounter time logger
	 * @param graphAssociation a graph composed by enumerated paths from different query entities.
	 * @param delta	 diameter constraint.
	 * @param queryEntities  query entities
	 * @return a association tree or null(if input graph is not a tree).
	 */
	public static AssociationTree newInstance(TimeCounter timeCounter,UndirectedGraph<Integer, IntegerEdge> graphAssociation,int delta,List<Integer> queryEntities){
		
		long startStub = System.currentTimeMillis();
		
		CheckerDepthFirstIterator<Integer, IntegerEdge> iterator=null;
		try{
			iterator = new CheckerDepthFirstIterator<>(graphAssociation,queryEntities.get(0));// a seed node.
		}catch(IndexOutOfBoundsException e){e.printStackTrace();return null;}
		
		int traversedNodeCount = 0;
		while(iterator.hasNext()){
			traversedNodeCount++;//traversed vertexes number.
			int nodeId = iterator.next();
			//degree of any query entity must be 1.
			if(!iterator.isConnectedGraghATree() ||  (graphAssociation.degreeOf(nodeId)==1&&!queryEntities.contains(nodeId)) ){ 
				timeCounter.checkTime+=System.currentTimeMillis()-startStub;
				return null;
			}							
		}
		if(traversedNodeCount!=graphAssociation.vertexSet().size()  ){ 
			timeCounter.checkTime+=System.currentTimeMillis()-startStub;
			return null;
		}
		//compute tree's diameter.
		ClosestFirstIterator<Integer, IntegerEdge> closeFirstIterator = null;
		try{
			closeFirstIterator= new ClosestFirstIterator<Integer, IntegerEdge>(graphAssociation,queryEntities.get(0));
		}catch(IllegalArgumentException e){
			Set<Integer> vertexes = graphAssociation.vertexSet();
			System.out.println(queryEntities.toString());
			System.out.println(vertexes.size());
			System.exit(1);
		}
		
		while(closeFirstIterator.hasNext())closeFirstIterator.next();
		int furthestPointA = 0;double maxDistance = 0.0;
		for(Integer i : graphAssociation.vertexSet()){
			double tempDistacne =closeFirstIterator.getShortestPathLength(i); 
			if(tempDistacne>maxDistance){
				maxDistance = tempDistacne;furthestPointA=i;
			}
		}		
		closeFirstIterator = new ClosestFirstIterator<Integer, IntegerEdge>(graphAssociation,furthestPointA);
		while(closeFirstIterator.hasNext())closeFirstIterator.next();	
		maxDistance = 0.0;		
		for(Integer i : graphAssociation.vertexSet()){
			double tempDistacne =closeFirstIterator.getShortestPathLength(i); 
			if(tempDistacne>maxDistance){
				maxDistance = tempDistacne;//furthestPointB=i;
			}
		}
		int treeDiameter = new Double(maxDistance).intValue();
		if(delta<treeDiameter &&delta!=-1){
			timeCounter.checkTime+=System.currentTimeMillis()-startStub;
			return null; //  condition 2.
		}
		
		timeCounter.checkTime+=System.currentTimeMillis()-startStub;
		startStub = System.currentTimeMillis();
		
		
		//else construct the tree.
		AssociationTree tree = new AssociationTree();
		tree.startNodes = AssociationNode.constructNode(graphAssociation,AssociationNode.SupperRootID, queryEntities.get(0));
		tree.canonicalCode=tree.startNodes.getCanonicalCode();
		tree.diameter=treeDiameter;		
		
		timeCounter.constructTime = System.currentTimeMillis()-startStub;		
		return tree;		
	}	

	/**
	 * @return the skeleton code of a tree.
	 */
	public List<Integer> getSubgroupKey(){		
		return startNodes.skeletonCode;	
	}
	
	public Iterator<AssociationNode> getDFSIterator(){
		return startNodes.DFSTraversalList().iterator();		
	}
	
	
	
}
