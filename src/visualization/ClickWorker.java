package visualization;

import gui.Input;
import gui.View;

import java.util.List;

import javax.swing.SwingWorker;

public class ClickWorker extends SwingWorker<Void, String>{

	private MainPanel panel;
	
	public ClickWorker(MainPanel panel)
	{
		this.panel = panel;
	}
	
	@Override
	protected Void doInBackground() throws Exception
	{
		View view = panel.data.selectedView;
		int x = (int) view.getBoundsRect().getCenterX();
		int y = (int) view.getBoundsRect().getCenterY();
		Input.tap(x, y);
		publish("ACTION: [input tap] at " + x + "," + y + "\n");
		new LayoutWorker(panel).execute();
		return null;
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
