package name.dxliu.bean;

import java.util.List;

/**
 * Data structure used to stored pattern-association_supporters pair
 * @author LiuXin
 *
 */

public class PairBean {	
	private List<Integer> key;
	private List value;
	
	
	public PairBean(){}	
	public PairBean(List<Integer> key,List t1){
		this.key=key;
		value = t1;
	}
	public List<Integer> getKey() {
		return key;
	}
	public void setKey(List<Integer> key) {
		this.key = key;
	}
	public List<Object> getValue() {
		return value;
	}
	public void setValue(List value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "PairBean [key=" + key.toString() + ", value=" + value.toString() + "]";
	}
	
	
}
