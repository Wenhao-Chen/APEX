package gui;

import java.awt.Rectangle;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class View {

	private Node node;
	public int id;
	private boolean clicked;
	private boolean changeGUI;
	
	public View(Node node, int id)
	{
		this(node, id, false);
	}
	
	private View(Node node, int id, boolean clicked)
	{
		this.node = node;
		this.id = id;
		this.clicked = clicked;
		this.changeGUI = false;
	}
	
	public View clone()
	{
		return new View(node.cloneNode(true), id, clicked);
	}
	
	public boolean changesGUIState()
	{
		return this.changeGUI;
	}
	
	public void setChangeGUIState(boolean change)
	{
		this.changeGUI = change;
	}
	
	public boolean isEquivalent(View v)
	{
		if (!this.getPackageName().equals(v.getPackageName()))
			return false;
		if (!this.getBoundsRect().equals(v.getBoundsRect()))
			return false;
		if (!this.getNodeID().equals(v.getNodeID()))
			return false;
		return true;
	}
	
	public String getTextOrID()
	{
		Node textNode = node.getAttributes().getNamedItem("text");
		if (textNode != null && !textNode.getNodeValue().equals(""))
			return "\"" + textNode.getNodeValue() + "\"";
		String nodeID = getNodeID();
		return nodeID.equals("")? "#"+id : "\"" + nodeID + "\"";
	}
	
	public String getNodeID()
	{
		Node idNode = node.getAttributes().getNamedItem("resource-id");
		return idNode == null? "" : idNode.getNodeValue();
	}
	
	public Node getNode()
	{
		return this.node;
	}
	
	public String getPackageName()
	{
		Node packageNode = node.getAttributes().getNamedItem("package");
		return packageNode == null? "" : packageNode.getNodeValue();
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
	
	public boolean isClickable()
	{
		Node clickableNode = this.node.getAttributes().getNamedItem("clickable");
		if (clickableNode == null)
			return false;
		return clickableNode.getNodeValue().equals("true");
	}
	
	public void setIsClicked(boolean clicked)
	{
		this.clicked = clicked;
	}
	
	public boolean clicked()
	{
		return this.clicked;
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
