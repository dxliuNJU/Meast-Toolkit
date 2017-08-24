package name.dxliu.oracle;

import java.util.Comparator;
/**
 * 
 * @author Daxin Liu
 * 
 * This class is a data structure that comprised by two field. One is the degree of a vertex, another is the vertexId.
 * The static field 'dvpCmp' try to provide a order of this data structure.
 */

public class DegVPair {
	public float dregree;
	public int vertexId;
	DegVPair(float f,int i){dregree=f;vertexId=i;}
	public DegVPair(){}
	public static Comparator<DegVPair> dvpCmp =new Comparator<DegVPair>() {		
		@Override
		public int compare(DegVPair arg0, DegVPair arg1) {
			return arg0.dregree>arg1.dregree?1:-1;
		}
	};
	
	
}
