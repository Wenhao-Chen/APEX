package gui;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Hierarchy {

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
			NodeList nodes = doc.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				System.out.println(node);
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
