package name.dxliu.oracle;

/**
 * @author Daxin Liu
 *  An abstract oracle.
 */
public abstract class AbstractOracle implements Oracle{
	
	public abstract byte Query(int source, int target);	
}
