package name.dxliu.oracle;

import java.util.HashMap;

/**
* @author Daxin Liu
* 
* This class is a data structure that comprised by two field. One is the vertex id, another is the distance.
* The static field 'dictionary' try to cache all the VDPair that have appeared already.
*/

public class VDPair{
	
	private static HashMap<Integer,VDPair> dictionary = new HashMap<Integer, VDPair>(); //16777215 maximal allowed key	
	
	public int first;
	public byte second;	
	private VDPair(int v, byte w) {
		first = v;second=w;
	}
	
	
	public static VDPair getVDPair(int v, byte w) {
		if(v>16777214){System.out.println("Max vertex limits surpassed"); return null;}
		Integer key = v*128+w;
		VDPair value = dictionary.get(key);
		if(value == null){
			value = new VDPair(v,w);
		}
		dictionary.put(key, value);
		return value;
	}
	
	
}

	
