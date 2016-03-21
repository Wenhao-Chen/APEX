package visualization;

import gui.Hierarchy;
import gui.UIAutomator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

public class UIAutomatorWorker extends SwingWorker<Hierarchy, String>{

	private MainPanel panel;
	
	public UIAutomatorWorker(MainPanel panel)
	{
		this.panel = panel;
	}
	
	@Override
	protected Hierarchy doInBackground() throws Exception
	{
		publish("ACTION: [uiautomator dump]...");
		ArrayList<String> log = UIAutomator.dump();
		publish(" Done.\n");
		for (String s : log)
			if (!s.equals(""))
				publish(" > " + s + "\n");
		publish("ACTION: [pull window_dump.xml]...");
		File f = UIAutomator.pullDump();
		publish(" Done.\n");
		return new Hierarchy(f);
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
	
	@Override
	protected void process(List<String> messages)
	{
		for (String s : messages)
			panel.ta_RuntimeLog.append(s);
	}

}
