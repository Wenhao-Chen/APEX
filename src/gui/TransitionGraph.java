package gui;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class TransitionGraph {

	
	public static void main(String[] args)
	{
		DirectedGraph<LayoutHierarchy, Event> g = new DefaultDirectedGraph<LayoutHierarchy, Event>(Event.class);
	}
	
}
