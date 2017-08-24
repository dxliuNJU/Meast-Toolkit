package name.dxliu.jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.traverse.DepthFirstIterator;

/**
 * This class extends from a traverser from jgrapht. Here, it is responsible for checking
 *  a graph whether is a tree.
 * @author Daxin Liu
 *
 * @param <V> vertex class.
 * @param <E> edge class
 */
public class CheckerDepthFirstIterator<V,E> extends DepthFirstIterator<V,E>{
	
	private boolean isGraphATree=true;
	private V lastEncounterVertex=null;
	
	public CheckerDepthFirstIterator(Graph<V,E> g) {
		super(g);
	}

	public CheckerDepthFirstIterator(Graph<V,E> g, V startVertex) {
		super(g, startVertex);
	}

	@Override
	protected void encounterVertex(V vertex, E edge) {
		lastEncounterVertex = vertex;
		super.encounterVertex(vertex, edge);
	}
	
	@Override
	protected void encounterVertexAgain(V vertex, E edge) {
		if(vertex!=lastEncounterVertex&&getSeenData(vertex)==VisitColor.WHITE)
			isGraphATree = false;			
		super.encounterVertexAgain(vertex, edge);
	}
	public boolean isConnectedGraghATree(){
		return isGraphATree;
	}
	
	
}
