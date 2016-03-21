package gui;

import java.awt.Rectangle;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class View {

	private Node node;
	public int id;
	
	public View(Node node, int id)
	{
		this.node = node;
		this.id = id;
	}
	
	
	public Rectangle getBoundsRect()
	{
		Node boundNode = node.getAttributes().getNamedItem("bounds");
		if (boundNode != null)
		{
			String bounds = boundNode.getNodeValue();
			String start = bounds.substring(bounds.indexOf("[")+1, bounds.indexOf("]"));
			String end = bounds.substring(bounds.indexOf(start)+start.length()).replace("[", "").replace("]", "");
			String[] startXY = start.split(",");
			String[] endXY = end.split(",");
			int x0 = Integer.parseInt(startXY[0]);
			int y0 = Integer.parseInt(startXY[1]);
			int width = Integer.parseInt(endXY[0]) - x0;
			int height = Integer.parseInt(endXY[1]) - y0;
			return new Rectangle(x0, y0, width, height);
		}
		return null;
	}
	
	public void printAttributes()
	{
		System.out.print("  [");
		NamedNodeMap map = this.node.getAttributes();
		for (int i = 0; i < map.getLength(); i++)
		{
			Node node = map.item(i);
			System.out.print(node.getNodeName() + "=" + node.getNodeValue() + ", ");
		}
		System.out.println("]");
	}
	
	public String getAttributes()
	{
		String result = this.id > -1? 
				 "Attributes of View #" + id + ":\n\n"
				:"Attributes of the root View:\n\n";
		NamedNodeMap map = this.node.getAttributes();
		for (int i = 0; i < map.getLength(); i++)
		{
			Node node = map.item(i);
			result += node.getNodeName() + "\t=  " + node.getNodeValue() + "\n";
		}
		return result;
	}
	
}
