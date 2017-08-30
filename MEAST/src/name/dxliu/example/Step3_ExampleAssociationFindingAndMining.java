package name.dxliu.example;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.graph.DirectedMultigraph;

import name.dxliu.agent.GraphAgent;
import name.dxliu.agent.OracleAgent;
import name.dxliu.agent.RelationChecker;
import name.dxliu.associations.AssociationFinder;
import name.dxliu.associations.AssociationTree;
import name.dxliu.bean.IntegerEdge;
import name.dxliu.bean.PairBean;
/**
 * This class demonstrate how to run the discovery and mining algorithm.
 * The result of discovery is a set of association and the association tree could be 
 * paraphrased via the canonical code.
 * The result of mining is a list of PairBean which key is the pattern's canonical code and the 
 * value is a list of support association tree.
 * 
 * @author Daxin Liu
 * 
 */

public class Step3_ExampleAssociationFindingAndMining {
	
	public static void main(String[] args) throws IOException {		
		
		AssociationFinder finder = new AssociationFinder();
		
		//read entity-relation graph.
		DirectedMultigraph<Integer, IntegerEdge> graph = new DirectedMultigraph<>(IntegerEdge.class);
		List<String> allLine = Files.readAllLines(Paths.get("example/out_id_relation_triples"), Charset.defaultCharset());
		for(String line:allLine){
			String[] spo = line.split(" ");
			Integer source = Integer.valueOf(spo[0]);
			Integer target = Integer.valueOf(spo[2]);
			
			graph.addVertex(source);
			graph.addVertex(target);
			
			graph.addEdge(source,target,new IntegerEdge(source,target, Integer.parseInt(spo[1])));
		}
		
		
		GraphAgent graphAgent = new ExampleGraphAgent(graph);
		OracleAgent oracleAgent = new ExampleOracleAgent();
		
		 Map<String,Integer> dictionary = readDictionary();
		
		//Association finding.    
		List<Integer> queryEntities = new ArrayList<>();
		queryEntities.add(dictionary.get("Alice"));   //  7-Alice
		queryEntities.add(dictionary.get("Bob"));  // 8-Bob 
		queryEntities.add(dictionary.get("Chris"));  // 9-Chris		
		int diameter = 4;
		
		List<AssociationTree> associations = finder.discovery(graphAgent, oracleAgent, diameter, queryEntities);
		
		for(AssociationTree tree:associations){
			System.out.println(tree.getCanonicalCode());
		}
		
		Map<Integer,List<Integer>> typeStatements = readExampleTypeStatements();
		RelationChecker exampleRelationChecker = new ExampleRelationChecker();
		
		//Pattern Mining
		int tau = 2;
		List<PairBean> patterns = finder.partitionAndMiningPattern(associations.iterator(), tau , typeStatements, queryEntities, exampleRelationChecker);
		
		System.out.println(patterns.toString());
		
	}
	
	/**
	 * 
	 * @return a map<instanceId,typeIds>.
	 * @throws IOException
	 */
	
	private static Map<Integer, List<Integer>> readExampleTypeStatements() throws IOException {
		Map<Integer, List<Integer>> typeStatements = new TreeMap<>();
		List<String> allLine = Files.readAllLines(Paths.get("example/out_id_type_triples"), Charset.defaultCharset());
		for(String line : allLine){
			String[] spo = line.split(" ");
			Integer instance = Integer.valueOf(spo[0]);
			Integer type = Integer.valueOf(spo[2]);
			
			List<Integer> existedTypes = typeStatements.get(instance);
			if(existedTypes ==null){
				existedTypes = new ArrayList<>();
			}
			existedTypes.add(type);
			typeStatements.put(instance, existedTypes);
		}	
		return typeStatements;
	}
	
	private static Map<String,Integer> readDictionary() throws IOException {
		 Map<String,Integer> dictionary = new TreeMap<>();
		List<String> allLine = Files.readAllLines(Paths.get("example/out_dict"), Charset.defaultCharset());
		for(String line : allLine){
			String[] spo = line.split(",");
			Integer id = Integer.valueOf(spo[0]);
			String uri = spo[1];
			dictionary.put(uri, id);
		}	
		return dictionary;
	}
	
	
	
}
