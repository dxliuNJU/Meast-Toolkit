package name.dxliu.example;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import name.dxliu.agent.RelationChecker;

/**
 * This class is an implementation of the example relation checker. It reads the id range's information from previous step.
 *  @author Daxin Liu
 *
 */

public class ExampleRelationChecker implements RelationChecker{
	private String propertyRange;
	private int min,max;
	
	public ExampleRelationChecker(){
		try{
			List<String> allLine = Files.readAllLines(Paths.get("example/out_id_range"), Charset.defaultCharset());
			propertyRange = allLine.get(0).split(":")[1];
			
			String[] min_max = propertyRange.split("-");
			min = Integer.parseInt(min_max[0]);
			max = Integer.parseInt(min_max[1]);
					
		}catch (IOException e) {
			System.err.println("failed to load type range file.");
		}		
	}
	public static void main(String[] args) {
		System.out.println(new ExampleRelationChecker().toString());
		
		
	}
	
	@Override
	public boolean isIdRelation(int id) {
		return min<=id&&id<=max;
	}

	@Override
	public boolean isIdRelation(String id) {
		return isIdRelation(Integer.parseInt(id));
	}
	@Override
	public String toString() {
		return "ExampleRelationChecker [propertyRange=" + propertyRange + ", min=" + min + ", max=" + max + "]";
	}

}
