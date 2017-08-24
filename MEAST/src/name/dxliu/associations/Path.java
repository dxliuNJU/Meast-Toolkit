package name.dxliu.associations;


import java.util.ArrayList;
/**
 * Path. the father field is the previous node of a path while the sons field keep all the possible 
 * succeeding node.
 * @author LiuXin
 *
 */
public class Path {

	public 	Path father=null;
	public 	int id;	
	public ArrayList<Path> sons=new ArrayList<Path>();	
	public int relation=0;//the relation lays on the current path.	
	
	
	public Path(int i){ id=i;}
	public Path(int i,Path father){id=i;this.father=father;}
	public Path(int id,int relate,Path father){relation = relate;this.id=id;this.father=father;}

	public Path clone(){Path n=new Path(id);n.relation = relation;n.father=father;n.sons=sons;return n;}

	public int getLength(){
		if(father==null)
			return 0;
		else{
			return father.getLength()+1;
		}
	}
	
}
