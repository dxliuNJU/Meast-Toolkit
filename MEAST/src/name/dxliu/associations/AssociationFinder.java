package name.dxliu.associations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import name.dxliu.agent.GraphAgent;
import name.dxliu.agent.OracleAgent;
import name.dxliu.agent.RelationChecker;
import name.dxliu.bean.AlgLoggerBean;
import name.dxliu.bean.PairBean;
import name.dxliu.util.CombinationUtil;
import name.dxliu.util.TimeCounter;

/**
 * This class is the main entry of discovery and mining algorithm.
 * @author LiuXin
 *
 */
public class AssociationFinder {
	
	public final static long ILLEGAL_QUERYENTITIES_TIMEFLAG = -2;
	//expected association number. If the discovery progress find more association number than this,it would return immediately.
	public int associationLimit = 1000000;
	public int thingId = Integer.MAX_VALUE;
	public static Logger logger = Logger.getLogger("RunningLog");
	public AlgLoggerBean recorder=new AlgLoggerBean();
	
	private PruningStrategy runingStrategy = PruningStrategy.PRN_1;	

	public PruningStrategy getRuningStrategy() {
		return runingStrategy;
	}

	public void setRuningStrategy(PruningStrategy runingStrategy) {
		this.runingStrategy = runingStrategy;
	}

	public void resetRecorder(){recorder=new AlgLoggerBean();}
	
	/**
	 * this method firstly enumerates all the paths from different query entity,then find out their common 
	 * node. Finally accord their common nodes (in each path) to construction association tree.
	 * @param graphAgent answer the neighborhood query.
	 * @param oracle answer the distance query.
	 * @param delta  diameter constraint.
	 * @param queryEntities query entity ids.
	 * @return association tree set.
	 */
	public List<AssociationTree> discovery(GraphAgent graphAgent,OracleAgent oracleAgent,int delta,List<Integer> queryEntities){
		resetRecorder();
		recorder.runingStrategy = this.runingStrategy;
		recorder.delta = delta;
		recorder.queryEntityNumber = queryEntities.size();
		
		for(int queryEntityID:queryEntities)
			recorder.queryEntityIDs+=queryEntityID+",";		
		
		List<AssociationTree> result = new ArrayList<>();		
		
		//step 1. enumeration
		List<Map<Integer,List<Path>>> 
				allEnumeratedPathForQueryEntities = new ArrayList<>(queryEntities.size());		
		long startTimeStub = System.currentTimeMillis();		
		try{		
			for(int queryEntity:queryEntities){ 
				Map<Integer,List<Path>> pathesFromQueryEntities = new HashMap<>();				
				if(runingStrategy.equals(PruningStrategy.PRN))		
					pathesFromQueryEntities = PathEnumerator.enumeratePathesViaPRN(this.recorder,queryEntity, graphAgent, oracleAgent, delta, queryEntities);
				else if(runingStrategy.equals(PruningStrategy.PRN_1))
					pathesFromQueryEntities = PathEnumerator.enumeratePathesViaPRN_1(this.recorder,queryEntity, graphAgent, oracleAgent, delta, queryEntities);
				else if(runingStrategy.equals(PruningStrategy.BSC))
					pathesFromQueryEntities = PathEnumerator.enumeratePathesViaBSC(this.recorder,queryEntity, graphAgent, oracleAgent, delta, queryEntities);
				else{
					logger.info("Illegal Runing State setting");
					//System.err.println("Illegal Runing State setting");
					System.exit(3);
				}
				allEnumeratedPathForQueryEntities.add(pathesFromQueryEntities);	
			}
			recorder.semipathCombinationTime = System.currentTimeMillis()-startTimeStub;
		}catch(IllegalArgumentException e){
			recorder.semipathCombinationTime = ILLEGAL_QUERYENTITIES_TIMEFLAG;//setting time illegal
			return null;
		}	
		
		Set<List<Integer>> canonicalCodeSet = new HashSet<>();		
		TimeCounter tc = new TimeCounter();
		
		//step 2. find out common nodes.
		HashSet<Integer> finalCommonIds = null;		
		for(Map<Integer, List<Path>> queryEntityEntryMap : allEnumeratedPathForQueryEntities){			
			HashSet<Integer> idSetCopy = new HashSet<>();//figure out why  we have to copy instead of use itself.
			
			int totalPathesNumber = 0;
			for(Entry<Integer,List<Path>> entry: queryEntityEntryMap.entrySet()){
				idSetCopy.add(entry.getKey());
				totalPathesNumber+=entry.getValue().size();
			}								
			recorder.queryPathesNumber+=totalPathesNumber+",";
			
			if(finalCommonIds == null)finalCommonIds = idSetCopy;
			else finalCommonIds.retainAll(idSetCopy);
		}
		
		//step 3. construction.
		for(Integer instanceId : finalCommonIds){
			List<List<Path>> currentCommonIdQENodes = new ArrayList<>(queryEntities.size());			
			for(Map<Integer, List<Path>> qeNodeMap : allEnumeratedPathForQueryEntities)
				currentCommonIdQENodes.add(qeNodeMap.get(instanceId));			
			try{
				CombinationUtil.CombinateAndGenerateAssociationResult(associationLimit,tc, result,canonicalCodeSet, currentCommonIdQENodes, delta, queryEntities);	
			}catch(Throwable t){				
				if(t instanceof OutOfMemoryError){
					System.err.println("out of memory:"+result.size());
					System.exit(1);
				}
				t.printStackTrace(); System.exit(2);	
			}
		}
		
		recorder.runingTime = System.currentTimeMillis()-startTimeStub;
		recorder.validAssociationNumber = result.size();		
		recorder.checkingTime = tc.checkTime;
		recorder.constructTime = tc.constructTime;		
		return result;		
	}
	
	/**
	 * @param trees iterator of association trees.
	 * @param tau frequency threshold.
	 * @param typeStatements type statements.key is instance id, value are all the type of this instance.
	 * @param queryentities
	 * @param checker answer whether specific id is a property or no.
	 * @return frequent pattern-supporters list.
	 */
	public List<PairBean> enumerateCountAndFilterPattern(Iterator<AssociationTree> trees,int tau,Map<Integer,List<Integer>> typeStatements,List<Integer> queryentities,RelationChecker checker){
		
		HashMap<List<Integer>,List<AssociationTree>> mediaMap = new HashMap<>();
		Map<List<Integer>,int[]> statasticMap = new HashMap<List<Integer>,int[]>();
		while(trees.hasNext()){
			AssociationTree tree = trees.next();
			List<Integer> canonicalCode = tree.getCanonicalCode();			
			//step 1. enumerate all the possible pattern's combinations via canonicalCode
			List<List<Integer>> instanceTypeList = new ArrayList<>();
			for(Integer s : canonicalCode){				
				List<Integer> types = new ArrayList<>();
				
				if(s.equals(AssociationNode.$)){//"$"
					types.add(s);
				}else{			
					if(queryentities.contains(s)||checker.isIdRelation(s))  types.add(s);
					else{
						List<Integer> typesFromMap = typeStatements.get(s);						
						if(typesFromMap!=null)					
							types.addAll(typesFromMap);
						else							
							types.add((thingId));						
					}					
				}					
				instanceTypeList.add(types);					
			}		
			//Step 2.combination
			List<List<Integer>> combinationResult = CombinationUtil.getCombinateResult(instanceTypeList);
	
			for(List<Integer> patterncode : combinationResult){
				int[] presentTimes = statasticMap.get(patterncode);
				
				if(presentTimes==null){
					statasticMap.put(patterncode, new int[]{1});
				}else{
					presentTimes[0]++;
				}
				
				List<AssociationTree> lvat =  mediaMap.get(patterncode);
				if(lvat==null) lvat = new ArrayList<>();
				
				lvat.add(tree);
				mediaMap.put(patterncode, lvat);
			}		
		}
		//step 3. filter.
		List<PairBean> result = new ArrayList<>();
		for(Entry<List<Integer>, int[]> entry:statasticMap.entrySet()){
			if(entry.getValue()[0]>=tau){
				result.add( new PairBean(entry.getKey(),mediaMap.get(entry.getKey())));	
			} 
		}
		return result;
	}
	
	public List<PairBean> partitionAndMiningPattern(Iterator<AssociationTree> trees,int tau,Map<Integer,List<Integer>> map,List<Integer> idsList,RelationChecker checker){
		HashMap<List<Integer>,ArrayList<AssociationTree>> subgroupMap = new HashMap<>();
		HashMap<List<Integer>,int[]> subgroupCounter = new HashMap<>();	
		
		 long startStub = System.currentTimeMillis();
		//step 1. statistic the subgroup(by skeleton code here).
		while(trees.hasNext()){
			AssociationTree vaTree =trees.next();
			
			List<Integer> subgroupKey =  vaTree.getSubgroupKey();
			ArrayList<AssociationTree> treeGroups = subgroupMap.get(subgroupKey);
			
			if(treeGroups==null){
				treeGroups = new ArrayList<>();
			}			
			treeGroups.add(vaTree);			
			subgroupMap.put(subgroupKey, treeGroups);
			
			int[] count = subgroupCounter.get(subgroupKey);
			if(count==null) {
				subgroupCounter.put(subgroupKey, new int[]{1});
			}else{
				count[0]++;
			}			
		}		
		long checkStub = System.currentTimeMillis();		
		
		//step 2. discard unlikely frequent subgroup.
		HashSet<List<Integer>> possibleFrequentSubgroupKeys = new HashSet<>();
		for(Entry<List<Integer>,int[]> entry:subgroupCounter.entrySet()){
			if(entry.getValue()[0]>=tau) possibleFrequentSubgroupKeys.add(entry.getKey());
		}
		
		long miningStub = System.currentTimeMillis();
		
		
		//step 3.mining
		List<PairBean> finalResult = new ArrayList<>();		
		for(List<Integer> possibleFrequentSubgroupKey: possibleFrequentSubgroupKeys){						
			finalResult.addAll(
					enumerateCountAndFilterPattern(subgroupMap.get(possibleFrequentSubgroupKey).iterator(),tau,map, idsList,checker)
					);
		}	
		long endStub = System.currentTimeMillis();
		
		this.recorder.subgroupCountingTime = checkStub-startStub;
		this.recorder.frequenceCheckingTime = miningStub-checkStub;
		this.recorder.partitionMiningTime = endStub-miningStub;
		return finalResult;
	}	
}
