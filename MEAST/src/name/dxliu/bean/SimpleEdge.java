package name.dxliu.bean;

import org.jgrapht.graph.DefaultEdge;

/** 
 * 
 * Customized edge used as undirected-graph edge.
 * @author Daxin Liu
 *
 */
public class SimpleEdge extends DefaultEdge{
	private static final long serialVersionUID = -1429243029201058076L;

	@Override
	public Object getSource() {
		return super.getSource();
	}

	@Override
	public Object getTarget() {
		return super.getTarget();
	}
}