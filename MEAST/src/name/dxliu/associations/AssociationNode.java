package name.dxliu.associations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jgrapht.UndirectedGraph;

import name.dxliu.bean.IntegerEdge;

/**
 * This is the basic data structure of a association tree. It provide method that construct a association node(tree)
 * by checked graph(tree structure) or a standardized canonical code iteratively.
 * @author Daxin Liu
 *
 */
public class  AssociationNode{
	
	//a number never used in dictionary as the code's separator.
	public static final Integer $ = Integer.MIN_VALUE;
	//a number no used as node id(topically node ids are positive number).
	public static final Integer SupperRootID = -1;
	
	protected List<Integer> canonicalCode = null;
	protected List<Integer> skeletonCode=null;
	
	//pointer to the father node.used in generating canonical code.
	private AssociationNode father=null;
	//the edge point to the father node.positive means this edge is from father node to this node and negative means the opposite.
	protected int relation=0;
	
	//id is setting to SupperRootID by default.
	protected int id=SupperRootID;
	//record the successors.
	protected ArrayList<AssociationNode> sons=new ArrayList<AssociationNode>();
	
	//agent that record current node's minimal successor query entity.Used in shifting.
	protected int minimalsuccessorNodeId = 0;
	
	//order defined on association node.
	public static Comparator<AssociationNode> compMinimalSucesserNodeId=new Comparator<AssociationNode>() {
		@Override
		public int compare(AssociationNode o1, AssociationNode o2) {
			return o1.minimalsuccessorNodeId-o2.minimalsuccessorNodeId;
		}
	};
	
	AssociationNode(int Id){
		id=Id;
		minimalsuccessorNodeId = Id;//leaf node by default.
		canonicalCode = new ArrayList<>();
		skeletonCode = new ArrayList<>();
	}
	
	
	public int getId() {return id;	}
	public void setId(int id) {this.id = id;}
	public int getRelation() {return relation;}
	public void setRelation(int relation) {this.relation = relation;}
	
	
	public List<AssociationNode> DFSTraversalList(){	
	 List<AssociationNode> result = new ArrayList<>();
		if(sons.size()==0){
			result.add(this);
		}else{
			result.add(this);
			for(AssociationNode son : sons){
				son.setFather(this);
				result.addAll(son.DFSTraversalList());
			}
		}	
		return result;
	}	
	public List<Integer> getCanonicalCode() {
		return canonicalCode;
	}
	/** 
	 * @param graphAssociation graphAssociation a graph composed by enumerated paths from different query entities.
	 * @param superNodeId 
	 * @param currentNodeId
	 * @return the root node of a association node(tree).
	 */
	public static AssociationNode constructNode(UndirectedGraph<Integer, IntegerEdge> graphAssociation,int superNodeId, int currentNodeId) {
		AssociationNode node = constructNodeRecursiveAdvanced(graphAssociation,superNodeId,currentNodeId);
		return node;
	}
	
	/** 
	 * @param graphAssociation graphAssociation a graph composed by enumerated paths from different query entities.
	 * @param superNodeId 
	 * @param currentNodeId
	 * @return the root node of a association node(tree).
	 */
	private static AssociationNode constructNodeRecursiveAdvanced(UndirectedGraph<Integer, IntegerEdge> graphAssociation,int superNodeId, int currentNodeId) {
		
		AssociationNode node = new AssociationNode(currentNodeId);
		
		if(superNodeId!=SupperRootID){	//no rooted node
			IntegerEdge ie = graphAssociation.getEdge(superNodeId, currentNodeId);
			if(ie.getSource()==superNodeId)
				node.setRelation(ie.getEdge());
			else
				node.setRelation(-ie.getEdge());
		}
		if(graphAssociation.degreeOf(currentNodeId)==1&&superNodeId!=SupperRootID){//leaf node
			node.canonicalCode.add((currentNodeId));node.canonicalCode.add($);
			node.skeletonCode.add((node.minimalsuccessorNodeId));node.skeletonCode.add($);//
		}
		else{//no leaf node
			
			for(IntegerEdge ie : graphAssociation.edgesOf(currentNodeId)){
				int currentNeighborNodeId = ie.getSource()==currentNodeId?ie.getTarget():ie.getSource();
				if(currentNeighborNodeId!=superNodeId){
					node.sons.add(constructNodeRecursiveAdvanced(graphAssociation,currentNodeId,currentNeighborNodeId));
					java.util.Collections.sort(node.sons,compMinimalSucesserNodeId);		
				}								
			}
			node.minimalsuccessorNodeId = node.sons.get(0).minimalsuccessorNodeId;
			
			node.canonicalCode.add((currentNodeId));
			node.skeletonCode.add((node.minimalsuccessorNodeId));
			//result.append(id);
			for(AssociationNode son:node.sons){
				node.canonicalCode.add((son.relation));
				node.skeletonCode.add((son.relation));
				node.canonicalCode.addAll(son.canonicalCode);
				node.skeletonCode.addAll(son.skeletonCode);
			}
			node.canonicalCode.add($);
			node.skeletonCode.add($);//
			//result.append(" $");			
		}
		return node;
	}
	
	
	/**
	 * This method provide how to construct a association tree by canonical code.Strictly speaking, it try
	 *  to recreate the structure of a tree. It can reconstruct a pattern tree too, but in this case, it won't
	 *  generate information like diameter during the processing.
	 * @param codes
	 * @return association tree or null.
	 */
	@Deprecated
	public static AssociationNode constructViaCanonicalCode(Integer[] codes){
		List<Integer> als = (List<Integer>) Arrays.asList(codes);
		return constructViaCanonicalCode(als);
	}
	/**
	 * This method provide how to construct a association tree by canonical code.Strictly speaking, it try
	 *  to recreate the structure of a tree. It can reconstruct a pattern tree too, but in this case, it won't
	 *  generate information like diameter during the processing.
	 * @param codes
	 * @return association tree or null.
	 */
	@Deprecated
	public static AssociationNode constructViaCanonicalCode(List<Integer> codes){
		AssociationNode node = constructViaCanonicalCodeRecursive(codes);
		return node;
	}
	
	
	/**
	 * This method provide how to construct a association tree by canonical code.Strictly speaking, it try
	 *  to recreate the structure of a tree. It can reconstruct a pattern tree too, but in this case, it won't
	 *  generate information like diameter during the processing.
	 * @param codes
	 * @return association tree or null.
	 */
	@Deprecated
	private static AssociationNode constructViaCanonicalCodeRecursive(List<Integer> codes){
		if(codes.size()<3)return null;
		try{
			AssociationNode superRoot = new AssociationNode(SupperRootID);
			AssociationNode root = new AssociationNode(codes.get(0));
			int i =1;root.setFather(superRoot);
			
			AssociationNode currentNode = root;
			while(currentNode!=superRoot){
				
				if(codes.get(i).equals($)){
					if(!currentNode.sons.isEmpty()){
						int minimal = Integer.MAX_VALUE;
						for(AssociationNode son:currentNode.sons)
							minimal = minimal>son.minimalsuccessorNodeId?son.minimalsuccessorNodeId:minimal;
						currentNode.minimalsuccessorNodeId = minimal;
					}
					
					currentNode = currentNode.getFather();
					i+=1;
				}else{
					int edge = codes.get(i);
					int nodeId = codes.get(i+1);				
					
					AssociationNode temp = new AssociationNode(nodeId);
					temp.relation = edge;
					temp.setFather(currentNode);
					currentNode.sons.add(temp);
					currentNode = temp;
					i+=2;				
				}			
			}		
			root.setFather(null);
			root.canonicalCode = codes;
			return root;	
	}catch(NumberFormatException|IndexOutOfBoundsException e){e.printStackTrace();}
		return null;
	}
	
	
	public AssociationNode getFather() {
		return father;
	}


	public void setFather(AssociationNode father) {
		this.father = father;
	}

	public static void main(String[] args) {
		
		Integer[] codes =//{1,-11,4,-11,9,$,-11,2,$,$,$,$};
		
		//{858030, 495, 652505, -558, 3141200, -2147483648, -119, 1126469, 558, 1666613, -640, 4249892, -2147483648, -2147483648, -2147483648, -2147483648, -2147483648};
		{858030, 495, 652505, -558, 3141200, $, -119, 1126469, 558, 1666613, -640, 4249892, $, $, $, $, $};
		AssociationNode root = AssociationNode.constructViaCanonicalCode(codes);
		System.out.println(root.toString());

	}
}
