package name.dxliu.bean;

import name.dxliu.associations.PruningStrategy;
/**
 *  An logger that records the running info of discovering and mining
 * @author Daxin Liu
 *
 */
public class AlgLoggerBean{	
	
	public PruningStrategy runingStrategy;	
	public int delta;  //diameter
	public int queryEntityNumber;//
	public String queryEntityIDs;//separated by ‘,’
	public int validAssociationNumber; //all the associations that found.
	
	public int edgeNumber;	// how many edges been traversed.
	public String queryPathesNumber; //how many pathes been enumerated.
	public int oracleQueryTime; //number of time querying oracle
	
	//--for associationFinding
	public long runingTime;
	
	
	public long semipathCombinationTime;// time cost on enumeration
	public long checkingTime; // time cost on check pruning condition
	public long constructTime; // time cost on construct association tree. 
	
	public long subgroupCountingTime; //time cost on counting subgroup (mining phrase)
	public long frequenceCheckingTime;//time cost on checking frequency(mining phrase)
	public long partitionMiningTime; //time cost the prime call(mining phrase)
	
	public boolean surpassLimit=false;
	
	//--for partition pattern Mining
	
	@Override
	public AlgLoggerBean clone() throws CloneNotSupportedException {
		AlgLoggerBean result = new AlgLoggerBean();
		result.runingStrategy = this.runingStrategy;
		result.delta = this.delta;		
		result.queryEntityNumber = this.queryEntityNumber;		
		result.queryEntityIDs = this.queryEntityIDs;
		result.validAssociationNumber = this.validAssociationNumber;
		
		
		result.edgeNumber = this.edgeNumber;
		result.queryPathesNumber = this.queryPathesNumber;
		result.oracleQueryTime = this.oracleQueryTime;
		
		result.runingTime = this.runingTime;
		
		result.semipathCombinationTime = this.semipathCombinationTime;
		result.checkingTime = this.checkingTime;
		result.constructTime = this.constructTime;
		result.surpassLimit = this.surpassLimit;
		return result;
	}



	
	
	public AlgLoggerBean(){
		runingStrategy = PruningStrategy.BSC;
		delta=0;queryEntityNumber=0;validAssociationNumber=0;
		edgeNumber=0;oracleQueryTime=0;
		queryEntityIDs="";queryPathesNumber="";
		runingTime=0;
		surpassLimit= false;
	}

	public static AlgLoggerBean getTimeOutBean(){
		AlgLoggerBean bean = new AlgLoggerBean();
		
		
		return bean;
	}
	
	

	@Override
	public String toString() {
		return "AlgLoggerBean [runingStrategy=" + runingStrategy + ", delta=" + delta + ", queryEntityNumber="
				+ queryEntityNumber + ", queryEntityIDs=" + queryEntityIDs + ", validAssociationNumber="
				+ validAssociationNumber + ", edgeNumber=" + edgeNumber + ", queryPathesNumber=" + queryPathesNumber
				+ ", oracleQueryTime=" + oracleQueryTime + ", runingTime=" + runingTime + ", semipathCombinationTime="
				+ semipathCombinationTime + ", checkingTime=" + checkingTime + ", constructTime=" + constructTime
				+ ", surpassLimit=" + surpassLimit + ", subgroupCountingTime=" + subgroupCountingTime
				+ ", frequenceCheckingTime=" + frequenceCheckingTime + ", partitionMiningTime=" + partitionMiningTime
				+ "]";
	}	
}
