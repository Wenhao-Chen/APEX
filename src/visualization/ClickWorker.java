package visualization;

import gui.Input;
import gui.LayoutHierarchy;
import gui.View;

import java.util.List;

import javax.swing.SwingWorker;

public class ClickWorker extends SwingWorker<LayoutHierarchy, String>{

	private MainPanel panel;
	private View view;
	
	public ClickWorker(MainPanel panel, View view)
	{
		this.panel = panel;
		this.view = view;
	}
	
	@Override
	protected LayoutHierarchy doInBackground() throws Exception
	{
		int x = (int) view.getBoundsRect().getCenterX();
		int y = (int) view.getBoundsRect().getCenterY();
		Input.tap(x, y);
		publish("ACTION: [tap view ] at " + x + "," + y + "\n");
		Thread.sleep(1000);
		while (panel.layoutWorker != null && !(panel.layoutWorker.isDone() || panel.layoutWorker.isCancelled()))
		{
			Thread.sleep(100);
		}
		panel.layoutWorker = new LayoutWorker(panel);
		panel.layoutWorker.execute();
		return panel.layoutWorker.get().h;
	}

	
	@Override
	protected void done()
	{
		
	}
	
	@Override
	protected void process(List<String> messages)
	{
		for (String s : messages)
			panel.ta_RuntimeLog.append(s);
	}
	
}
