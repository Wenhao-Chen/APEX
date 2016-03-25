package visualization;

import gui.Communication;
import gui.Event;
import gui.Input;
import gui.LayoutHierarchy;
import gui.View;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import model.ApexAppBuilder;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class BlackBoxTest extends SwingWorker<ArrayList<String>, String>{

	private Data data;
	private MainPanel panel;
	//public ArrayList<String> traverseLog = new ArrayList<String>();
	private List<LayoutHierarchy> layoutList = new ArrayList<LayoutHierarchy>();
	private DirectedGraph<LayoutHierarchy, Event> gTG = new DefaultDirectedGraph<LayoutHierarchy, Event>(Event.class);
	
	public BlackBoxTest(Data data, MainPanel panel)
	{
		this.data = data;
		this.panel = panel;
	}
	
	@Override
	protected ArrayList<String> doInBackground() throws Exception
	{
		if (data.app == null)
		{
			publish("ACTION: [build ApexApp]. Might take some time...");
			data.app = ApexAppBuilder.fromAPK(data.apkPath);
			publish(" Done.\n");
		}
		
		//explore1();
		explore();
		return null;
	}
	
	private int counter = 1;
	
	private void firstRun() throws Exception
	{
		gTG = new DefaultDirectedGraph<LayoutHierarchy, Event>(Event.class);
		publish("!@#$- Run " + counter++ + "\n");
		publish("ACTION: [install app]..\n");
		Process p = Communication.installApp(data.app.getAPKPath());
		publishOutput(p);
		// start main activity
		publish("ACTION: [am start MainActivity]..\n");
		p = Communication.startActivity(data.app.getPackageName(), data.app.getMainActivity().getJavaName());
		publishOutput(p);
		LayoutHierarchy h = getCurrentLayoutHierarchy();
		gTG.addVertex(h);
		if (h.hasUnclickedView())
		{
			
		}
	}
	
	private void explore() throws Exception
	{
		gTG = new DefaultDirectedGraph<LayoutHierarchy, Event>(Event.class);
		publish("!@#$- Run " + counter++ + "\n");
		publish("ACTION: [install app]..\n");
		Process p = Communication.installApp(data.app.getAPKPath());
		publishOutput(p);
		// start main activity
		publish("ACTION: [am start MainActivity]..\n");
		p = Communication.startActivity(data.app.getPackageName(), data.app.getMainActivity().getJavaName());
		publishOutput(p);
		LayoutHierarchy h = getCurrentLayoutHierarchy();
		gTG.addVertex(h);
		
		/**
		 * depthFirst()
		 * {
			 * while (exists unclicked events)
			 * {
			 *		pick an layout;
			 *		go to that layout;
			 *		explore from that layout, click until no more events on current layout
			 * }
		 * }
		 * */
	}
	
	private void explore1() throws Exception
	{
		int counter = 1;
		LayoutHierarchy currentH = null;
		do
		{
			// force install app
			publish("!@#$- Run " + counter++ + "\n");
			publish("ACTION: [install app]..\n");
			Process p = Communication.installApp(data.app.getAPKPath());
			publishOutput(p);
			// start main activity
			publish("ACTION: [am start MainActivity]..\n");
			p = Communication.startActivity(data.app.getPackageName(), data.app.getMainActivity().getJavaName());
			publishOutput(p);
			
			// 1. get the correct LayoutHierarchy object
			currentH = getCurrentLayoutHierarchy();
			LayoutHierarchy h = this.findMatchFromLog(currentH);
			if (h == null)
			{
				publish("!@#$New Layout.\n");
				layoutList.add(currentH);
			}
			else
			{
				currentH = h;
			}
			
			while (currentH.hasUnclickedView() || currentH.hasEdgesOut())
			{
				publish("!@#$On Layout: " + currentH.hashCode() + 
						"(" + currentH.getUnclickedViewCount() + "/" + currentH.getClickableLeafViews().size() + ")" + "\n");
				View v = getAUnclickedView(currentH);
				LayoutHierarchy newH = click(v);
				v.setIsClicked(true);
				if (newH.isSameLayout(currentH))
				{
					continue;
				}
				else
				{
					LayoutHierarchy existingH = this.findMatchFromLog(newH);
					if (existingH == null)
					{
						// new layout discovered
						this.layoutList.add(newH);
						currentH = newH;
						continue;
					}
					else
					{
						// previously discovered layout
						currentH = existingH;
						continue;
					}
				}
			}
			
		}
		while (this.hasUnclickedViews());
		//depthFirstExploration(h);
	}
	
	private View getAUnclickedView(LayoutHierarchy h)
	{
		View result = null;
		for (View v : h.getClickableLeafViews())
			if (!v.clicked())
				return v;
			else if (v.changesGUIState())
				result = v;
		return result;
	}
	
	@Override
	protected void done()
	{
		
	}
	
	@Override
	protected void process(List<String> messages)
	{
		for (String s : messages)
			if (s.startsWith("!@#$"))
				panel.ta_TraverseLog.append(s.replace("!@#$", ""));
			else
				panel.ta_RuntimeLog.append(s);
	}
	
	private LayoutHierarchy getCurrentLayoutHierarchy() throws Exception
	{
		waitForWorker(panel.layoutWorker);
		panel.layoutWorker = new LayoutWorker(panel);
		panel.layoutWorker.execute();
		return panel.layoutWorker.get().h;
	}
	
	private LayoutHierarchy click(View v) throws Exception
	{
		waitForWorker(panel.clickWorker);
		panel.clickWorker = new ClickWorker(panel, v);
		panel.clickWorker.execute();
		publish("!@#$ Clicked " + v.getTextOrID() + "\n");
		return panel.clickWorker.get();
	}
	
	
	private LayoutHierarchy findMatchFromLog(LayoutHierarchy h)
	{
		for (LayoutHierarchy lh : layoutList)
			if (lh.isSameLayout(h))
				return lh;
		return null;
	}
	
	private void depthFirstExploration(LayoutHierarchy h) throws Exception
	{
		publish("!@#$On Layout: " + h.hashCode() + 
				"(" + h.getUnclickedViewCount() + "/" + h.getClickableLeafViews().size() + ")" + "\n");
		for (View v : h.getClickableLeafViews())
		{
			if (!v.clicked())
			{
				LayoutHierarchy tgtH = click(v);
				if (!tgtH.getRootView().getPackageName().equals(data.app.getPackageName()))
				{
					// drag it back
					Input.hitBack();
					Thread.sleep(1000);
					continue;
				}
				if (!tgtH.isSameLayout(h))
				{
					publish("Layout Changed to ");
					LayoutHierarchy hh = findMatchFromLog(tgtH);
					if (hh == null)
					{
						publish("a newly discovered layout.\n");
						publish("!@#$Layout changed to " + tgtH.hashCode() + 
								"(" + tgtH.getUnclickedViewCount() + "/" + tgtH.getClickableLeafViews().size() + ")" + "\n");
						data.layoutList.add(tgtH);
						depthFirstExploration(tgtH);
					}
					else
					{
						publish("a previously explored layout.\ns");
						publish("!@#$Layout changed to " + hh.hashCode() + 
								"(" + hh.getUnclickedViewCount() + "/" + hh.getClickableLeafViews().size() + ")" + "\n");
						depthFirstExploration(hh);
					}
				}
				else
				{
					publish("Layout remains the same.\n");
				}
			}
		}
	}
	
	private boolean hasUnclickedViews()
	{
		for (LayoutHierarchy h : layoutList)
			if (h.hasUnclickedView())
				return true;
		return false;
	}
	
	private void publishOutput(Process p)
	{
		for (String s: Communication.readOutStream(p))
			if (!s.equals(""))
				publish("  >" + s + "\n");
		for (String s: Communication.readErrorStream(p))
			if (!s.equals(""))
				publish(" >" + s + "\n");
	}
	
	
	@SuppressWarnings("rawtypes")
	private void waitForWorker(SwingWorker worker) throws Exception
	{
		if (worker == null)
			return;
		while (!worker.isDone())
		{
			Thread.sleep(100);
		}
	}
	
}


