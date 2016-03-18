package gui;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Hierarchy {

	private Node hierarchy;
	private int rotation;	// 0 - portrait, 1 - landscape
	
	
	
	
	/**
	 * Create Hierarchy object from window_dump.xml by uiautomator.
	 * 
	 * The GUI nodes are wrapped inside of:
	 * 		<hierarchy rotation="0">...</hierarchy>
	 * 
	 * 
	 * 
	 * */
	public Hierarchy(File hiearchyXML)
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(hiearchyXML);
			hierarchy = doc.getFirstChild();
			rotation = Integer.parseInt(hierarchy.getAttributes().getNamedItem("rotation").getTextContent());
			System.out.println("rotation is: " + rotation);
			NodeList nodes = hierarchy.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				System.out.println(node.getNodeName());
				print(node.getAttributes());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void print(NamedNodeMap map)
	{
		for (int i = 0; i < map.getLength(); i++)
		{
			Node node = map.item(i);
			System.out.println(" " + node.getNodeName() + " " + node.getNodeValue());
		}
	}
}
