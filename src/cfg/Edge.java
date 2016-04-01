package cfg;


public class Edge {

	private Vertex src, tgt;
	private String label;
	private String color;
	
	public Edge(Vertex src, Vertex tgt, String label)
	{
		this.src = src;
		this.tgt = tgt;
		this.label = label;
	}
	
	public Vertex getSource()
	{
		return src;
	}
	
	public Vertex getTarget()
	{
		return tgt;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
	
	public void resetColor()
	{
		this.color = null;
	}
	
	public String toString()
	{
		String result = src.getID() + " -> " + tgt.getID() + " [ ";
		result += "label=\"" + label + "\"";
		if (color != null)
			result += " color=" + color;
		result += " ];";
		return result;
	}
	
	public boolean equals(Edge e)
	{
		return (src.equals(e.src) && tgt.equals(e.tgt) && label.equals(e.label));
	}
}
