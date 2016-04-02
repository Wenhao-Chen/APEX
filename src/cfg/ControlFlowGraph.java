package cfg;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import model.ApexCodeBlock;
import model.ApexMethod;
import model.ApexStmt;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;


public class ControlFlowGraph{


	private ApexMethod m;
	private DirectedGraph<ApexStmt, String> jgrapht;
	private ArrayList<Vertex> vertices, blockVertices;
	private ArrayList<Edge> edges, blockEdges;
	
	
	public ControlFlowGraph(ApexMethod m)
	{
		this.m = m;
		initVertices();
		initEdges();
	}
	
	
	private void initVertices()
	{
		vertices = new ArrayList<Vertex>();
		blockVertices = new ArrayList<Vertex>();
		for (ApexStmt s : m.getStatements())
		{
			vertices.add(new Vertex(s));
		}
		for (ApexCodeBlock block : m.getCodeBlocks())
		{
			blockVertices.add(new Vertex(block));
		}
	}
	
	private void initEdges()
	{
		edges = new ArrayList<Edge>();
		blockEdges = new ArrayList<Edge>();
		for (ApexStmt s : m.getStatements())
		{
			if (s.isReturnStmt() || s.isThrowStmt())
			{
				continue;
			}
			if (s.isGotoStmt())
			{
				edges.add(createEdge(s, s.getGotoTargetStmt(), "goto"));
				blockEdges.add(createBlockEdge(s, s.getGotoTargetStmt(), "goto"));
			}
			else if (s.isIfStmt())
			{
				edges.add(createEdge(s, s.getFlowThroughStmt(), "flowthrough"));
				blockEdges.add(createBlockEdge(s, s.getFlowThroughStmt(), "flowthrough"));
				edges.add(createEdge(s, s.getIfJumpTargetStmt(), "jump"));
				blockEdges.add(createBlockEdge(s, s.getIfJumpTargetStmt(), "jump"));
			}
			else if (s.isSwitchStmt())
			{
				edges.add(createEdge(s, s.getFlowThroughStmt(), "flowthrough"));
				blockEdges.add(createBlockEdge(s, s.getFlowThroughStmt(), "flowthrough"));
				Map<Integer, String> switchMap = s.getSwitchMap();
				for (Map.Entry<Integer, String> entry : switchMap.entrySet())
				{
					Edge e = createEdge(s, m.getFirstStmtOfBlock(entry.getValue()), "jump");
					if (!edges.contains(e))
						edges.add(e);
					e = createBlockEdge(s, m.getFirstStmtOfBlock(entry.getValue()), "jump");
					if (!blockEdges.contains(e))
						blockEdges.add(e);
				}
			}
			else
			{
				ApexStmt nextS = s.getFlowThroughStmt();
				if (nextS != null)
				{
					edges.add(createEdge(s, nextS, "flowthrough"));
					if (!s.getBlockName().equals(nextS.getBlockName()))
						blockEdges.add(createBlockEdge(s, nextS, "flowthrough"));
				}
			}
		}
	}
	
	private Edge createEdge(ApexStmt src, ApexStmt tgt, String label)
	{
		return new Edge(getVertex(src), getVertex(tgt), label);
	}
	
	private Edge createBlockEdge(ApexStmt src, ApexStmt tgt, String label)
	{
		return new Edge(getBlockVertex(src), getBlockVertex(tgt), label);
	}
	
	public Vertex getVertex(ApexStmt s)
	{
		for (Vertex v : vertices)
			if (v.getApexStmt().equals(s))
				return v;
		return null;
	}
	
	public Vertex getBlockVertex(ApexStmt s)
	{
		for (Vertex v : blockVertices)
			if (v.getCodeBlock().contains(s))
				return v;
		return null;
	}
	
	public Edge getEdge(Vertex src, Vertex tgt)
	{
		for (Edge e : edges)
			if (e.getSource().equals(src) && e.getTarget().equals(tgt))
				return e;
		return null;
	}
	
	public ArrayList<Vertex> getVertices()
	{
		return vertices;
	}
	
	public ArrayList<Vertex> getBlockVertices()
	{
		return blockVertices;
	}
	
	public ArrayList<Edge> getEdges()
	{
		return edges;
	}
	
	
	public DirectedGraph<ApexStmt, String> getJGrapht()
	{

		DirectedGraph<ApexStmt, String> jgrapht = new DefaultDirectedGraph<ApexStmt, String>(String.class);
		for (Vertex v : vertices)
		{
			jgrapht.addVertex(v.getApexStmt());
		}
		for (Edge e : edges)
		{
			jgrapht.addEdge(e.getSource().getApexStmt(), e.getTarget().getApexStmt(), e.getLabel());
		}
		return jgrapht;
	}
	
	public DirectedGraph<ApexCodeBlock, String> getBlockJGrapht()
	{
		DirectedGraph<ApexCodeBlock, String> jgrapht = new DefaultDirectedGraph<ApexCodeBlock, String>(String.class);
		for (Vertex v : blockVertices)
		{
			jgrapht.addVertex(v.getCodeBlock());
		}
		for (Edge e : edges)
		{
			jgrapht.addEdge(e.getSource().getCodeBlock(), e.getTarget().getCodeBlock(), e.getLabel());
		}
		return jgrapht;
	}
	
	public String getDotGraph()
	{
		String result = "digraph G {\n";
		for (Vertex v : vertices)
		{
			result += "\t" + v.toString() + "\n";
		}
		for (Edge e : edges)
		{
			result += "\t" + e.toString() + "\n";
		}
		result += "}\n";
		return result;
	}
	
	public String getBlockDotGraph()
	{
		String result = "digraph G {\n";
		result += "\tnode [shape=box]\n";
		for (Vertex v : blockVertices)
		{
			result += "\t" + v.toString() + "\n";
		}
		for (Edge e : blockEdges)
		{
			result += "\t" + e.toString() + "\n";
		}
		result += "}\n";
		return result;
	}
	
}
