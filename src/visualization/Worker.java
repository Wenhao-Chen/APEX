package visualization;

import gui.Hierarchy;
import gui.UIAutomator;

import java.io.File;

import javax.swing.SwingWorker;

public class Worker extends SwingWorker<Hierarchy, Void>{

	private MainPanel panel;
	
	public Worker(MainPanel panel)
	{
		this.panel = panel;
	}
	
	@Override
	protected Hierarchy doInBackground() throws Exception
	{
		File hierarchyXML = UIAutomator.dumpWindowXML();
		return new Hierarchy(hierarchyXML);
	}
	
	@Override
	protected void done()
	{
		try
		{
			panel.data.currentHierarchy = get();
			panel.repaint();
		} catch (Exception e)	{e.printStackTrace();}
	}

}
