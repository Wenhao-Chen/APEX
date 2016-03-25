package gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LayoutHierarchy {

	private Node hierarchy;
	private int rotation;	// 0 - portrait, 1 - landscape
	private List<View> leafViews;
	private View rootView;	// all other views on screen are children of this rootView
	
	private int leafIndex;
	
	
	public LayoutHierarchy(File hiearchyXML)
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
	
	private LayoutHierarchy()
	{
		rotation = 0;
		leafIndex = 0;
	}
	
	public LayoutHierarchy clone()
	{
		LayoutHierarchy result = new LayoutHierarchy();
		result.hierarchy = hierarchy.cloneNode(true);
		result.rotation = rotation;
		result.rootView = new View(result.hierarchy.getFirstChild(), -1);
		result.grabLeafViews(hierarchy);
		result.leafIndex = -1;
		return result;
	}
	
	public boolean isSameLayout(LayoutHierarchy h)
	{
		//TODO ignoring rotation for now
		
		// 1. compare root view
		if (!this.getRootView().isEquivalent((h.getRootView())))
			return false;
		
		// 2. compare each leaf view
		for (View v1 : this.getLeafViews())
		{
			boolean foundMatch = false;
			for (View v2 : h.getLeafViews())
			{
				if (v1.isEquivalent(v2))
				{
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch)
				return false;
		}
		
		return true;
	}
	
	public boolean hasEdgesOut()
	{
		for (View v : this.getClickableLeafViews())
			if (v.changesGUIState())
				return true;
		return false;
	}
	
	public boolean hasUnclickedView()
	{
		for (View v : this.getClickableLeafViews())
			if (!v.clicked())
				return true;
		return false;
	}
	
	public int getUnclickedViewCount()
	{
		int result = 0;
		for (View v : this.getClickableLeafViews())
			if (!v.clicked())
				result++;
		return result;
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
	
	public List<View> getClickableLeafViews()
	{
		List<View> results = new ArrayList<View>();
		for (View v : leafViews)
			if (v.isClickable())
				results.add(v);
		return results;
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
