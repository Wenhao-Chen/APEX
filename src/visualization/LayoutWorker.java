package visualization;

import gui.Hierarchy;
import gui.UIAutomator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

public class LayoutWorker extends SwingWorker<LayoutWorker.LayoutInfo, String>{
	
	private MainPanel panel;
	
	public LayoutWorker(MainPanel panel)
	{
		this.panel = panel;
	}
	
	@Override
	protected LayoutInfo doInBackground() throws Exception
	{
		publish("ACTION: [uiautomator dump]...");
		ArrayList<String> log = UIAutomator.dump();
		publish(" Done.\n");
		for (String s : log)
			if (!s.equals(""))
				publish(" > " + s + "\n");
		publish("ACTION: [pull window_dump.xml]...");
		File f1 = UIAutomator.pullDump();
		publish(" Done.\n");
		publish("ACTION: [screencap]...");
		log = UIAutomator.screenCap();
		publish(" Done.\n");
		for (String s : log)
			if (!s.equals(""))
				publish(s + "\n");
		publish("ACTION: [pull screencap.png]...");
		File f2 = UIAutomator.pullScreenCap();
		publish(" Done.\n");
		return new LayoutInfo(new Hierarchy(f1), f2);
	}
	
	@Override
	protected void done()
	{
		try
		{
			LayoutInfo result = get();
			panel.data.currentHierarchy = result.h;
			panel.data.screenCapFile = result.screenCapFile;
			panel.repaint();
		} catch (Exception e)	{e.printStackTrace();}
	}
	
	@Override
	protected void process(List<String> messages)
	{
		for (String s : messages)
			panel.ta_RuntimeLog.append(s);
	}

	class LayoutInfo {
		Hierarchy h;
		File screenCapFile;
		LayoutInfo(Hierarchy h, File f)
		{
			this.h = h;
			this.screenCapFile = f;
		}
	}
	
}
