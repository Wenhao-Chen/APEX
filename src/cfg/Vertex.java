package cfg;

import model.ApexCodeBlock;
import model.ApexStmt;

public class Vertex {

	private ApexStmt s;
	private ApexCodeBlock block;
	private String color;
	
	public Vertex(ApexStmt s)
	{
		this.s = s;
	}
	
	public Vertex(ApexCodeBlock block)
	{
		this.block = block;
	}
		
	public ApexStmt getApexStmt()
	{
		return s;
	}
	
	public ApexCodeBlock getCodeBlock()
	{
		return block;
	}
	
	public void setColor(String color)
	{
		this.color = color;
	}
	
	public void resetColor()
	{
		this.color = null;
	}
	
	public int getID()
	{
		return (s != null)? 
				s.getID() : block.getApexStmts().get(0).getID();
	}
	
	public String getLabel()
	{
		return (s != null)? 
				s.toDotGraphString() : block.toDotGraphString();
	}
	
	public String toString()
	{
		int id = getID();
		String label = getLabel();
		String result = id + " [ label=\"" + label + "\"";
		if (color != null)
			result += " color="+color + " fontcolor=" + color;
		result += " ];";
		return result;
	}
	
}
