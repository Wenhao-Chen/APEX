package visualization;

import gui.Hierarchy;
import gui.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private MainPanel me;
	
	protected JButton b_RefreshScreen, b_DoClick, b_ClearScreen;
	protected JTextArea ta_ViewAttr, t2, ta_RuntimeLog;
	protected JScrollPane sp1;
	protected Data data;
	
	private enum PaintCommand {None, Regular, HighlightClickable};
	private static PaintCommand paintCommand = PaintCommand.Regular;
	
	private static int offsetX = 80;
	private static int offsetY = 40;
	private static int shrink_factor = 3;
	
	private static int screenAreaWidth = 800;
	
	public static void main(String[] args)
	{
		init();
	}

	
	public MainPanel()
	{
		this.data = new Data();
		me = this;
		
		initWidgets();
		organizeLayout();
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// check if any leaf view is clicked
				boolean leafViewChecked = false;
				if (data.currentHierarchy == null)
					return;
				
				Rectangle rect = data.currentHierarchy.getRootView().getBoundsRect();
				Rectangle actualRect = getActualRect(rect);
				if (!actualRect.contains(e.getX(), e.getY()))
					return;
				
				List<View> selectedViews = new ArrayList<View>();
				for (View view: data.currentHierarchy.getLeafViews())
				{
					actualRect = getActualRect(view.getBoundsRect());
					if (actualRect.contains(e.getX(), e.getY()))
					{
						selectedViews.add(view);
						leafViewChecked = true;
					}
				}
				if (leafViewChecked)
				{
					data.selectedView = selectedViews.get(0);
					for (View v : selectedViews)
					{
						if (data.selectedView.getBoundsRect().contains(v.getBoundsRect()))
							data.selectedView = v;
					}
					ta_ViewAttr.setText(data.selectedView.getAttributes());
				}
				else
				{
					data.selectedView = null;
					ta_ViewAttr.setText(data.currentHierarchy.getRootView().getAttributes());
				}
				repaint();
			}
		});		
	}
	
	private void initWidgets()
	{

		b_RefreshScreen = new JButton("Display Layout Hierarchy");
		b_RefreshScreen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				paintCommand = PaintCommand.Regular;
				new UIAutomatorWorker(me).execute();
			}
		});
		
		b_DoClick = new JButton("Tap On Selected View");
		b_DoClick.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				paintCommand = PaintCommand.Regular;
				if (data.selectedView != null)
					new ClickWorker(me, data.selectedView).execute();
			}
		});
		b_ClearScreen = new JButton("Clear Screen");
		b_ClearScreen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				paintCommand = PaintCommand.None;
				repaint();
			}
		});
		
		ta_ViewAttr = new JTextArea(20, 30);
		ta_ViewAttr.setText("Click on a view and its attributes will show here.");
		ta_ViewAttr.setEditable(false);
		
		t2 = new JTextArea(10, 30);
		t2.setLineWrap(true);
		t2.setEditable(false);
		
		ta_RuntimeLog = new JTextArea(10, 30);
		ta_RuntimeLog.setEditable(false);
		sp1 = new JScrollPane(ta_RuntimeLog);
		
	}
	
	private void organizeLayout()
	{
		FlowLayout controlLayout = new FlowLayout(FlowLayout.LEADING);
		//controlLayout.setVgap(10);
		JPanel controlPanel = new JPanel(controlLayout);
		controlPanel.setPreferredSize(new Dimension(400, 800));
		controlPanel.setMinimumSize(new Dimension(400, 800));
		controlPanel.add(b_RefreshScreen);
		controlPanel.add(b_ClearScreen);
		controlPanel.add(b_DoClick);
		controlPanel.add(new FillerLabel(350, 1));
		controlPanel.add(new JLabel("Runtime log:"));
		controlPanel.add(sp1);
		controlPanel.add(new FillerLabel(350, 15));
		controlPanel.add(new JLabel("View Attributes:"));
		controlPanel.add(ta_ViewAttr);
		controlPanel.add(new FillerLabel(350, 15));
		controlPanel.add(new JLabel("Unused text area:"));
		controlPanel.add(t2);
		
		FlowLayout layout = new FlowLayout(FlowLayout.TRAILING);
		setLayout(layout);
		add(new FillerLabel(700, 500));
		add(controlPanel);
	}
	
	public static void init()
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				JFrame f = new JFrame("APEX");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setResizable(false);
				f.add(new MainPanel());
				f.pack();
				f.setVisible(true);
			}
		});
		
	}

	
	public Dimension getPreferredSize()
	{
		return new Dimension(1200, 1000);
	}
	
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (paintCommand == PaintCommand.None || this.data.currentHierarchy == null)
		{
			g.drawRect(20, 20, 700, 600);
			g.drawString("Device screen layout will be shown here.", offsetX+10, offsetY+10);
		}
		else if (paintCommand == PaintCommand.Regular)
		{
			paintHierarchy(this.data.currentHierarchy, g);
		}
		else if (paintCommand == PaintCommand.HighlightClickable)
		{
			//TODO
		}
	}
	
	private void paintHierarchy(Hierarchy h, Graphics g)
	{
		drawViewRect(h.getRootView(), g, Color.gray);
		drawViewRect(data.selectedView, g, Color.magenta);
		for (View view : h.getLeafViews())
		{
			if (!view.equals(data.selectedView))
				drawViewRect(view, g, Color.magenta);
		}
	}
	
	private Rectangle getActualRect(Rectangle rect)
	{
		int x = rect.x/shrink_factor + offsetX;
		int y = rect.y/shrink_factor + offsetY;
		int width = rect.width/shrink_factor;
		int height = rect.height/shrink_factor;
		return new Rectangle(x, y, width, height);
	}
	
	private void drawViewRect(View view, Graphics g, Color color)
	{
		if (view == null)
			return;
		Color oldColor = g.getColor();
		g.setColor(color);
		Rectangle rect = getActualRect(view.getBoundsRect());
		if (data.selectedView != null && data.selectedView.equals(view))
		{
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
		else
		{
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		g.setColor(oldColor);
		g.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		if (view.id > -1)
		{
			g.drawString("#" + view.id, rect.x + 2, rect.y + 12);
		}
	}

	class FillerLabel extends JLabel
	{
		private static final long serialVersionUID = 1L;

		FillerLabel(int width, int height)
		{
			super("");
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
		}
	}
	
}
