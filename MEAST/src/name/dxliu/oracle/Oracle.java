package name.dxliu.oracle;

/**
 * @author Daxin Liu
 * This interface represent the main function of a oracle. Any specific oralce implements this interface should provide how to 
 *  answer the distance query.
 */

public interface Oracle {
	public byte Query(int source,int target);
}
