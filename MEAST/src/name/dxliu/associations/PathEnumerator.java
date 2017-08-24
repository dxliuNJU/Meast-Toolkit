package name.dxliu.associations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.dxliu.agent.GraphAgent;
import name.dxliu.agent.OracleAgent;
import name.dxliu.bean.AlgLoggerBean;

/**
 * This class provide several path enumeration strategies.
 * @author Daxin Liu
 *
 */

public class PathEnumerator {
	
	/**
	 * @param queryEntity the rooted query entity from where the enumeration began.  root_id
	 * @param graphAgent provide the neighbors of a given vertex.
	 * @param oracleAgent provide distance calling service.
	 * @param delta diameter constraint
	 * @param queryEntities all the query entities.
	 * @return all the pathes enumerated from queryEntity by a BSC strategy.Since the BSC do not need oracle.
	 * the oracleAgent can be null.
	 */
	public static Map<Integer,List<Path>> enumeratePathesViaBSC(AlgLoggerBean recorder,int queryEntity,GraphAgent graphAgent, OracleAgent oracleAgent, int delta,
			List<Integer> queryEntities){
		
		Map<Integer,List<Path>> enumeratedPathesFromQueryEntity = new HashMap<>();
		
		int pathLength = (delta+1)/2;		
		Path rootPath = new Path(-1);
		
		List<Path> iterateSet = new ArrayList<>();	// Paths in each iterative round.
		iterateSet.add(rootPath);//initialize iterator.
		
		for(int currentpathLength=0;currentpathLength<=pathLength;currentpathLength++){
			List<Path> iterateTempSet = new ArrayList<>();
			for(Path tempPath : iterateSet){
				int currentPathId = tempPath.id;
				
				List<int[]> allEdges;
				if(currentpathLength == 0){
					allEdges = new ArrayList<>();
					allEdges.add(new int[]{queryEntity,-1});
				}else{
					allEdges =	graphAgent.getNeighborInfo(currentPathId);
					recorder.edgeNumber+=allEdges.size();
				}
				for(int[] ie : allEdges){
					int neighbor = ie[0];
					int interEdge = ie[1];
					if(isDuplicate(neighbor,tempPath)) continue; 
					Path nextNeighborPath=null;
					
					nextNeighborPath = new Path(neighbor, interEdge, tempPath);
					tempPath.sons.add(nextNeighborPath);
					iterateTempSet.add(nextNeighborPath);
					
					
					
					List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
					if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
					identicPathIdPaths.add(nextNeighborPath);						
					enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);	
					
				}
				
			}
			iterateSet = iterateTempSet;
		}
		return enumeratedPathesFromQueryEntity;
	}
	
	/**
	 * @param queryEntity the rooted query entity from where the enumeration began.  root_id
	 * @param graphAgent provide the neighbors of a given vertex.
	 * @param oracleAgent provide distance calling service(not null).
	 * @param delta diameter constraint
	 * @param queryEntities all the query entities.
	 * @return all the paths enumerated from queryEntity by a old PRN_1 strategy.
	 * this strategy is used in our previous version. The pruning condition is not the tightest.
	 * Because of this, this method is deprecated.
	 */
	@Deprecated
	public static Map<Integer,List<Path>> enumeratePathesViaPRN_1Deprecated(AlgLoggerBean recorder,int queryEntity,GraphAgent graphAgent, OracleAgent oracleAgent, int delta,
			List<Integer> queryEntities){
		Map<Integer,List<Path>> enumeratedPathesFromQueryEntity = new HashMap<>();
		
		int pathLength = (delta+1)/2;
		
		Path rootPath = new Path(-1);
		
		List<Path> iterateSet = new ArrayList<>();	// Paths in each iterative round.
		iterateSet.add(rootPath);
		
		for(int currentpathLength=0;currentpathLength<=pathLength;currentpathLength++){
			List<Path> iterateTempSet = new ArrayList<>();
			for(Path tempPath : iterateSet){
				int currentPathId = tempPath.id;

				List<int[]> allEdges;
				if(currentpathLength == 0){
					allEdges = new ArrayList<>();
					allEdges.add(new int[]{queryEntity,-1});
				}else{
					allEdges =	graphAgent.getNeighborInfo(currentPathId);
					recorder.edgeNumber+=allEdges.size();
				}
				for(int[] ie : allEdges){
					int neighbor = ie[0];
					int interEdge = ie[1];
					if(isDuplicate(neighbor,tempPath)) continue; 
					Path nextNeighborPath=null;
					
					
					if(currentpathLength==pathLength){//let alone the leaf
						nextNeighborPath = new Path(neighbor, interEdge, tempPath);
						tempPath.sons.add(nextNeighborPath);
						
						List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
						if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
						identicPathIdPaths.add(nextNeighborPath);						
						enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);	
						
						continue;//no more checking
					}
					
					
					boolean[] isPrunable = checkPrunableOld(recorder,neighbor,oracleAgent,queryEntity,queryEntities,currentpathLength,delta);					
					
					
					if(isPrunable[1])//condition2
						continue;//iterateTempSet.add(nextNeighborPath);											
					nextNeighborPath = new Path(neighbor, interEdge, tempPath);					
					tempPath.sons.add(nextNeighborPath);					
					iterateTempSet.add(nextNeighborPath);						
					if(!isPrunable[0]){					
						List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
						if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
						identicPathIdPaths.add(nextNeighborPath);						
						enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);		
					}
				}
				
			}
			iterateSet = iterateTempSet;
		}
		return enumeratedPathesFromQueryEntity;
	}
	
	/**
	 * @param queryEntity the rooted query entity from where the enumeration began.  root_id
	 * @param graphAgent provide the neighbors of a given vertex.
	 * @param oracleAgent provide distance calling service(not null).
	 * @param delta diameter constraint
	 * @param queryEntities all the query entities.
	 * @return all the paths enumerated from queryEntity by a PRN_1 strategy.
	 * this strategy is used in our previous version. The pruning condition is the tightest.
	 * recommend to use this.
	 */
	public static Map<Integer,List<Path>> enumeratePathesViaPRN_1(AlgLoggerBean recorder,int queryEntity,GraphAgent graphAgent, OracleAgent oracleAgent, int delta,
			List<Integer> queryEntities){
		
		Map<Integer,List<Path>> enumeratedPathesFromQueryEntity = new HashMap<>();
		
		int pathLength = (delta+1)/2;
		
		Path rootPath = new Path(-1);
		
		List<Path> iterateSet = new ArrayList<>();	
		//Path queryEntityPath = new Path(queryEntity);
		iterateSet.add(rootPath);
		
		for(int currentpathLength=0;currentpathLength<=pathLength;currentpathLength++){
			List<Path> iterateTempSet = new ArrayList<>();
			for(Path tempPath : iterateSet){
				int currentPathId = tempPath.id;

				List<int[]> allEdges;
				if(currentpathLength == 0){
					allEdges = new ArrayList<>();
					allEdges.add(new int[]{queryEntity,-1});
				}else{
					allEdges =	graphAgent.getNeighborInfo(currentPathId);
					recorder.edgeNumber+=allEdges.size();
				}
				for(int[] ie : allEdges){
					int neighbor = ie[0];
					int interEdge = ie[1];
					if(isDuplicate(neighbor,tempPath)) continue; 
					Path nextNeighborPath=null;
					
					
					if(currentpathLength==pathLength){
						nextNeighborPath = new Path(neighbor, interEdge, tempPath);
						tempPath.sons.add(nextNeighborPath);
						
						List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
						if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
						identicPathIdPaths.add(nextNeighborPath);						
						enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);	
						
						continue;
					}
					
					boolean[] isPrunable = checkPrunableTighter(recorder,neighbor,oracleAgent,queryEntity,queryEntities,currentpathLength,delta);					
				
					
					if(!isPrunable[0]){
						nextNeighborPath = new Path(neighbor, interEdge, tempPath);	
						List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
						if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
						identicPathIdPaths.add(nextNeighborPath);						
						enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);		
					}
					
					if(isPrunable[1])//condition2
						continue;//iterateTempSet.add(nextNeighborPath);		
					nextNeighborPath = new Path(neighbor, interEdge, tempPath);	
					tempPath.sons.add(nextNeighborPath);					
					iterateTempSet.add(nextNeighborPath);	
				}
				
			}
			iterateSet = iterateTempSet;
		}
		return enumeratedPathesFromQueryEntity;
	}
	
	
	/**
	 * @param queryEntity the rooted query entity from where the enumeration began.  root_id
	 * @param graphAgent provide the neighbors of a given vertex.
	 * @param oracleAgent provide distance calling service(not null).
	 * @param delta diameter constraint
	 * @param queryEntities all the query entities.
	 * @return all the paths enumerated from queryEntity by a PRN strategy.
	 * this strategy is used in our previous version. The pruning layer is too deep and the
	 * oracle query time surpassed the saving time. Slower than BSC
	 */
	@Deprecated
	public static Map<Integer,List<Path>> enumeratePathesViaPRN(AlgLoggerBean recorder,int queryEntity,GraphAgent graphAgent, OracleAgent oracleAgent, int delta,
			List<Integer> queryEntities){
		Map<Integer,List<Path>> enumeratedPathesFromQueryEntity = new HashMap<>();
		
		int pathLength = (delta+1)/2;
		
		Path rootPath = new Path(-1);
		
		List<Path> iterateSet = new ArrayList<>();	
		//Path queryEntityPath = new Path(queryEntity);
		iterateSet.add(rootPath);
		
		for(int currentpathLength=0;currentpathLength<=pathLength;currentpathLength++){
			List<Path> iterateTempSet = new ArrayList<>();
			for(Path tempPath : iterateSet){
				int currentPathId = tempPath.id;
				
				List<int[]> allEdges;
				if(currentpathLength == 0){
					allEdges = new ArrayList<>();
					allEdges.add(new int[]{queryEntity,-1});
				}else{
					allEdges =	graphAgent.getNeighborInfo(currentPathId);
					recorder.edgeNumber+=allEdges.size();
				}
				for(int[] ie : allEdges){
					int neighbor = ie[0];
					int interEdge = ie[1];
					if(isDuplicate(neighbor,tempPath)) continue; 					
					
					Path nextNeighborPath=null;
					
					if(currentpathLength!=pathLength){
						boolean[] isPrunable = checkPrunableOld(recorder,neighbor,oracleAgent,queryEntity,queryEntities,currentpathLength,delta);	
						if(currentpathLength!=pathLength){
							if(isPrunable[1]){//condition2
								continue;//iterateTempSet.add(nextNeighborPath);
							}
						}						
						nextNeighborPath = new Path(neighbor, interEdge, tempPath);
						tempPath.sons.add(nextNeighborPath);					
						iterateTempSet.add(nextNeighborPath);						

						if(!isPrunable[0]){
							
							List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
							if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
							identicPathIdPaths.add(nextNeighborPath);						
							enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);							
						}
					}else{//in the last layer, only do condiion1's pruning.
						nextNeighborPath = new Path(neighbor, interEdge, tempPath);
						tempPath.sons.add(nextNeighborPath);					
						iterateTempSet.add(nextNeighborPath);	
						
						if(!checkPrunableConditionOneOnly(recorder,neighbor,oracleAgent,queryEntity,queryEntities,currentpathLength,pathLength)){
							List<Path> identicPathIdPaths =  enumeratedPathesFromQueryEntity.get(neighbor);
							if(identicPathIdPaths == null ) identicPathIdPaths = new ArrayList<>();
							identicPathIdPaths.add(nextNeighborPath);						
							enumeratedPathesFromQueryEntity.put(neighbor, identicPathIdPaths);		
						}
					}
				}
				
			}
			iterateSet = iterateTempSet;
		}
		return enumeratedPathesFromQueryEntity;
	}
	
	
	
	/**
	 * @param id specific id.
	 * @param Path paths pointer
	 * @return  whether there is duplication in this path.
	 */
	private static boolean isDuplicate(int id,Path Path){
		Path temp = Path;
		while(temp!=null){
			if(temp.id==id) return true;
			temp=temp.father;
		}
		return false;
	}
	

	/**
	 * @param currentEntityI currently processing vertex in path
	 * @param oracleAgent provide distance calling service(not null).
	 * @param queryEntity the rooted query entity from where the enumeration began.  root_id
	 * @param queryEntities all the query entities.
	 * @param currentpathLength current enumerated path length.
	 * @param totalpathLength the maximal expected enumeration's step length
	 * @return checkPrunable[0] pruning condition 1，checkPrunable[1] pruning condition 2
	 */
	@Deprecated
	private static boolean[] checkPrunableOld(AlgLoggerBean recorder,int currentEntityI ,OracleAgent oracleAgent,int queryEntity,List<Integer> queryEntities,int currentpathLength,int delta){
		int totalpathLength = (delta+1)/2;
		boolean[] result ={false,false};		
		for(int queryEntityJ : queryEntities){//for each queryEntity 
			if(queryEntityJ==queryEntity) continue;//the source Path which expanded from.
			int dis = oracleAgent.queryDistance(currentEntityI, queryEntityJ);		
			recorder.oracleQueryTime++;			
			
			if(dis>totalpathLength){ //satisfy condition 1. discard current path.
				result[0]=true;
				if(currentpathLength+dis > 2*totalpathLength){//satisfy condition 1. discard branch.
					result[1]=true;
					return result;//all condition satisfied, no longer checking.
					}
			}
		}		
		return result;
	}
	
	/**
	 * @param currentEntityI currently processing vertex in path
	 * @param oracleAgent provide distance calling service(not null).
	 * @param queryEntity the rooted query entity from where the enumeration began.  root_id
	 * @param queryEntities all the query entities.
	 * @param currentpathLength current enumerated path length.
	 * @param totalpathLength the maximal expected enumeration's step length
	 * @return checkPrunable[0] new pruning condition 1，new checkPrunable[1] pruning condition 2.
	 * tighter conditions.
	 */
	private static boolean[] checkPrunableTighter(AlgLoggerBean recorder,int currentEntityI ,OracleAgent oracleAgent,int queryEntity,List<Integer> queryEntities,int currentpathLength,int delta){
		int totalpathLength = (delta+1)/2;
		boolean[] result ={false,false};		
		for(int queryEntityJ : queryEntities){//for each queryEntity 
			if(queryEntityJ==queryEntity) continue;//the source Path which expanded from.
			int dis = oracleAgent.queryDistance(currentEntityI, queryEntityJ);		
			recorder.oracleQueryTime++;			
	
			if(dis>totalpathLength) result[0]=true;
			if(currentpathLength+dis > delta) result[1]=true;// condition 2				
			if(result[0]&&result[1]) return result;
			
		}		
		return result;
	}
	
	
	/**
	 * This method is used in PRN strategy.
	 * @param recorder
	 * @param currentEntityI
	 * @param oracleAgent
	 * @param queryEntity
	 * @param queryEntities
	 * @param currentpathLength
	 * @param totalpathLength
	 * @return
	 */
	@Deprecated
	private static boolean checkPrunableConditionOneOnly(AlgLoggerBean recorder,int currentEntityI ,OracleAgent oracleAgent,int queryEntity,List<Integer> queryEntities,int currentpathLength,int totalpathLength){
		for(int queryEntityJ : queryEntities){//for each queryEntity 
			if(queryEntityJ==queryEntity) continue;//the source Path which expanded from.
			int dis = oracleAgent.queryDistance(currentEntityI, queryEntityJ);		
			recorder.oracleQueryTime++;			
			if(dis>totalpathLength){
				return true;
			}
		}
		return false;
	}	
	
}
