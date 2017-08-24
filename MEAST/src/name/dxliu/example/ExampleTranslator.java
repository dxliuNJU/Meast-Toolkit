package name.dxliu.example;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import name.dxliu.associations.AssociationNode;

/**
 * 
 * This class shows how to use the code to generate an association tree and translate the association tree to triples.
 * This example is based on the example entity-relation graph. Before running this example,you should have generated the dictionary file.
 * @author LiuXin
 *
 */
public class ExampleTranslator {
	public static void main(String[] args) throws IOException {
		
//		AssociationTree tree = new AssociationTree();
//		AssociationNode root = tree.getStartNodes();
		
		Integer[] codes =				
		{8, 0, 14, 6, 10, 5, 12, AssociationNode.$, -2, 16, AssociationNode.$, AssociationNode.$, AssociationNode.$, AssociationNode.$};
		AssociationNode root = AssociationNode.constructViaCanonicalCode(codes);		
		Map<Integer,String> dictionary = readDictionary() ;
		
		List<AssociationNode> traversedNodes = root.DFSTraversalList();
		for(AssociationNode node:traversedNodes){
			AssociationNode father = node.getFather();
			if(father==null)continue;
			String subject,predicate,object;
			
			if(node.getRelation()>0){//from father to node.
				subject = dictionary.get(father.getId());
				predicate = dictionary.get(node.getRelation());
				object = dictionary.get(node.getId());
			}else{
				subject = dictionary.get(node.getId());
				predicate = dictionary.get(-node.getRelation());
				object = dictionary.get(father.getId());
			}
			System.out.println(subject+" "+predicate+" "+object);
		}	
	}
	
	
	private static Map<Integer,String> readDictionary() throws IOException{
		List<String> allLine = Files.readAllLines(Paths.get("example/out_dict"), Charset.defaultCharset());
		Map<Integer,String> dict = new HashMap<>();
		for(String line:allLine){
			String[] entry = line.split(",");
			dict.put(Integer.valueOf(entry[0]), entry[1]);
		}
		return dict;		
	}
	
}
