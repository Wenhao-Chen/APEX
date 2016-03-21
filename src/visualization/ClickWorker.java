package visualization;

import gui.Input;
import gui.View;

import java.util.List;

import javax.swing.SwingWorker;

public class ClickWorker extends SwingWorker<Void, String>{

	private MainPanel panel;
	private View view;
	
	public ClickWorker(MainPanel panel, View view)
	{
		this.panel = panel;
		this.view = view;
	}
	
	@Override
	protected Void doInBackground() throws Exception
	{
		int x = (int) this.view.getBoundsRect().getCenterX();
		int y = (int) this.view.getBoundsRect().getCenterY();
		Input.tap(x, y);
		publish("ACTION: [tap] at " + x + "," + y + "\n");
		new UIAutomatorWorker(panel).execute();
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
