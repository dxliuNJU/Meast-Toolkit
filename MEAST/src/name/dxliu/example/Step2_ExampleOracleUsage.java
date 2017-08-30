package name.dxliu.example;

import name.dxliu.oracle.DefaultOracle;

/**
 * 
 * @author Daxin Liu
 * This class demonstrate how to build an oracle for a given undirected-graph and store it for further usage.
 */
public class Step2_ExampleOracleUsage {
	
	public static void main(String[] args) {
		
		DefaultOracle oracle = new DefaultOracle();
		//construct
		oracle.ConstructIndex("example/out_undirected_graph");
		
		//store
		oracle.StoreIndex("example/oracle");//("src/oracle");//(args[1]);//	//"src/oracle"
		
		
		//load
		oracle=new DefaultOracle();
		oracle.LoadIndex("example/oracle");
		
		//query
		// 7 for Alice and 8 for Bob in example graph.
		int distance  = oracle.Query(7, 8);
		System.out.println("distance:"+distance);

		
	}
}
