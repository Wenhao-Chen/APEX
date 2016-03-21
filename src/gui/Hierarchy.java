package gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Hierarchy {

	private Node hierarchy;
	private int rotation;	// 0 - portrait, 1 - landscape
	private List<View> leafViews;
	private View rootView;
	
	private int leafIndex;
	
	
	public Hierarchy(File hiearchyXML)
	{
		rotation = 0;
		leafIndex = 0;
		leafViews = new ArrayList<View>();
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(hiearchyXML);
			hierarchy = doc.getFirstChild();
			rotation = Integer.parseInt(hierarchy.getAttributes().getNamedItem("rotation").getNodeValue());
			this.rootView = new View(hierarchy.getFirstChild(), -1);
			grabLeafViews(hierarchy);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void grabLeafViews(Node node)
	{
		if (node.getChildNodes().getLength() > 0)
		{
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				grabLeafViews(children.item(i));
			}
		}
		else
		{
			View view = new View(node, leafIndex++);
			this.leafViews.add(view);
		}
	}
	
	public View getRootView()
	{
		return rootView;
	}
	
	public List<View> getLeafViews()
	{
		return leafViews;
	}
	
	public int getRotation()
	{
		return this.rotation;
	}
	
	@SuppressWarnings("unused")
	private void print(Node node)
	{
		System.out.println(node.getNodeName());
		print(node.getAttributes());
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			print(child);
		}
	}
	
	private void print(NamedNodeMap map)
	{
		System.out.print("  [");
		for (int i = 0; i < map.getLength(); i++)
		{
			Node node = map.item(i);
			System.out.print(node.getNodeName() + "=" + node.getNodeValue() + ", ");
		}
		System.out.println("]");
	}
	
}
