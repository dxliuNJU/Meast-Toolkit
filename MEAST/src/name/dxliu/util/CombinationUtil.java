package name.dxliu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import name.dxliu.associations.AssociationTree;
import name.dxliu.associations.Path;
import name.dxliu.bean.IntegerEdge;
/**
 *  This util provide combination.
 * @author Daxin Liu
 *
 */
public class CombinationUtil {	
	
	public static <E> List<List<E>> getCombinateResult(List<List<E>> inputs){
		int limits = 1;
		for(List<E> eachIndexList : inputs) limits*=eachIndexList.size();		
		if(limits==0) return null;				
		List<List<E>> combinationResult = new ArrayList<>(limits);		
		for(int i = 0;i<limits;i++){
			int inputLen = inputs.size(); 
			List<E> 	eachLE = new ArrayList<>(inputLen);
			int iteratorDivider = 1 ;				
			for(int j = 0;j<inputLen;j++){
				int index = (i% (iteratorDivider*inputs.get(j).size()))/iteratorDivider;
				eachLE.add(inputs.get(j).get(index));
				iteratorDivider*=inputs.get(j).size();			
			}			
			combinationResult.add(eachLE);
		}
		return combinationResult;
	}

	/**
	 * @param associationLimits expected association number.
	 * @param timeCounter time logger
	 * @param trees association trees.
	 * @param canonicalCodeSet tree's code that have been found. 
	 * @param inputs paths enumerated from different query entities.
	 * @param delta diameter constraint.
	 * @param queryEntities query entities.
	 * @return whether the enumeration is exhausted.
	 */
	public static boolean CombinateAndGenerateAssociationResult(int associationLimits,TimeCounter timeCounter,List<AssociationTree> trees,Set<List<Integer>> canonicalCodeSet,
				List<List<Path>> inputs,int delta,List<Integer> queryEntities) {
			
		if(trees.size()>=associationLimits){
			return true;
		}		
		int limits = 1;
		for(List<Path> eachIndexList : inputs) limits*=eachIndexList.size();		
		if(limits==0) return false;	
		//List<List<E>> combinationResult = new ArrayList<>(limits);		
		for(int i = 0;i<limits;i++){			
			List<Path> 	eachLE = new ArrayList<>(inputs.size());
			int iteratorDivider = 1 ;				
			for(int j = 0;j<inputs.size();j++){
				int index = (i% (iteratorDivider*inputs.get(j).size()))/iteratorDivider;
				try{
					eachLE.add(inputs.get(j).get(index));
				}catch(ArrayIndexOutOfBoundsException e){
					e.printStackTrace();
					System.out.println(limits);
					System.out.println(i+"  "+ j);
					System.out.println(iteratorDivider+"  "+ inputs.size());
					eachLE.add(inputs.get(j).get(index));
				}
				iteratorDivider*=inputs.get(j).size();			
			}
			
			boolean prunable = false;
			if((delta&1)==1){//odd
				Path theSecondNode =  null;
				int halfDiameter = (delta+1)>>1;				
				for(Path node:eachLE){//notice all node.id are equal to the common nodeid.
					if(node.getLength()==halfDiameter){
						if(theSecondNode==null){
							theSecondNode = node.father;
						}else{
							prunable&=theSecondNode.id == node.father.id;
						}
					}
					if(prunable) break;
				}
			}
			if(prunable) continue;						
			
			UndirectedGraph<Integer, IntegerEdge> graphX = new SimpleGraph<>(IntegerEdge.class);				
			for(Path indexNode : eachLE){//for each endpoint's path					
				if(!graphX.containsVertex(indexNode.id))
					graphX.addVertex(indexNode.id);				
				Path nextNode = indexNode.father;				
				while(nextNode.id!=-1){//super node id
					if(!graphX.containsVertex(indexNode.id))
						graphX.addVertex(indexNode.id);
					if(!graphX.containsVertex(nextNode.id))
						graphX.addVertex(nextNode.id);	
					if(indexNode.relation>0)
						graphX.addEdge(nextNode.id, indexNode.id,new IntegerEdge(nextNode.id,indexNode.id,indexNode.relation));
				    else
				    	graphX.addEdge(nextNode.id, indexNode.id,new IntegerEdge(indexNode.id,nextNode.id,-indexNode.relation));
					indexNode = nextNode;
					nextNode = nextNode.father;
				}
			}
			AssociationTree treeX = AssociationTree.newInstance(timeCounter,graphX,delta,queryEntities);
			long dulp = System.currentTimeMillis();
			if(treeX==null||canonicalCodeSet.contains(treeX.getCanonicalCode())) {
				timeCounter.constructTime+=System.currentTimeMillis()-dulp;
				continue;
			}
			canonicalCodeSet.add(treeX.getCanonicalCode());		
			
			if(trees.size()>=associationLimits){
				return true;
			}
			
			trees.add(treeX);				
			timeCounter.constructTime+=System.currentTimeMillis()-dulp;
			
		}
		return false;
		}
}
