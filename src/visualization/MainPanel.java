package visualization;

import gui.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;

public class MainPanel extends JPanel{

	
	private static final long serialVersionUID = 1L;
	protected DisplayPanel leftPanel;
	protected ControlPanel rightPanel;
	
	public static final int mainWidth = 1300;
	public static final int mainHeight = 850;
	public static final int mainHGap = 20;
	
	// Display Panel takes 700*700, the top 700*500 is used
	// to show screen, the bottom 700 * 190 show labels
	public static final int displayPanelWidth = 700;
	public static final int deviceAreaHeight = 650;
	public static final int displayPanelVGap = 10;
	public static final int shrink_factor = 3;
		
	// Control Panel takes 500 * 700
	public static final int controlPanelWidth = 500;
	public static final int controlPanelHGap = 10;
	public static final int controlPanelVGap = 1;
	
	protected enum PaintCommand {NONE, LAYOUTBOUNDS, SCREENCAP_ALL, SCREENCAP_LEAF_ONLY, SCREENCAP_CLICKABLE_ONLY};
	protected PaintCommand paintCommand = PaintCommand.LAYOUTBOUNDS;
	
	private MainPanel mainPanel;
	
	protected JButton b_RefreshScreen, b_DoClick, b_ClearScreen, b_Traverse;
	protected JRadioButton rb_ShowLayoutBounds, rb_ShowScreenCapAll, rb_ShowScreenCapLeaf, rb_ShowScreenCapClickable;
	protected ButtonGroup bg_LayoutDisplayOptions;
	protected JTextArea ta_ViewAttr, ta_RuntimeLog, ta_TraverseLog;
	protected JScrollPane sp_ViewAttr, sp_RuntimeLog, sp_TraverseLog;
	protected LayoutWorker layoutWorker;
	protected ClickWorker clickWorker;
	protected BlackBoxTest traverseWorker;
	protected Data data;
	
	public static void main(String[] args)
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
	
	public MainPanel()
	{
		mainPanel = this;
		setPreferredSize(new Dimension(mainWidth, mainHeight));
		FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
		layout.setHgap(mainHGap);
		setLayout(layout);
		leftPanel = new DisplayPanel();
		rightPanel = new ControlPanel();
		add(leftPanel);
		add(rightPanel);
		data = new Data();
	}
	
	private void updatePaintCommand()
	{
		if (this.rb_ShowLayoutBounds.isSelected())
			this.paintCommand = PaintCommand.LAYOUTBOUNDS;
		else if (this.rb_ShowScreenCapAll.isSelected())
			this.paintCommand = PaintCommand.SCREENCAP_ALL;
		else if (this.rb_ShowScreenCapLeaf.isSelected())
			this.paintCommand = PaintCommand.SCREENCAP_LEAF_ONLY;
	}
	
	private void drawRect(Graphics g, Rectangle r, Color color)
	{
		Color oldColor = g.getColor();
		g.setColor(color);
		Rectangle rect = shrinkRect(r);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(oldColor);
	}
	
	private void fillRect(Graphics g, Rectangle r, Color color)
	{
		Color oldColor = g.getColor();
		g.setColor(color);
		Rectangle rect = shrinkRect(r);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(oldColor);
	}
	
	private void drawString(Graphics g, String text, Rectangle r, Color color)
	{
		Color oldColor = g.getColor();
		g.setColor(color);
		g.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		Rectangle rect = shrinkRect(r);
		g.drawString(text, rect.x, rect.y+12);
		g.setColor(oldColor);
	}
	
	private void drawImage(Graphics g, BufferedImage img, Rectangle r)
	{
		Rectangle rect = shrinkRect(r);
		g.drawImage(
				img, 
				rect.x, rect.y, rect.x+rect.width, rect.y+rect.height, 
				r.x, r.y, r.x+r.width, r.y+r.height, 
				null);
	}
	
	private Rectangle shrinkRect(Rectangle rect)
	{
		return new Rectangle(rect.x/shrink_factor, rect.y/shrink_factor,
							rect.width/shrink_factor, rect.height/shrink_factor);
	}
	
	class DisplayPanel extends JPanel {
		

		private static final long serialVersionUID = 1L;
		

		DisplayPanel()
		{
			setPreferredSize(new Dimension(displayPanelWidth, mainHeight));
			setMinimumSize(new Dimension(displayPanelWidth, mainHeight));
			FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
			layout.setVgap(displayPanelVGap);
			setLayout(layout);
			add(new Filler(displayPanelWidth, deviceAreaHeight));
			b_Traverse = new JButton("Start Traversing");
			b_Traverse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (traverseWorker == null || traverseWorker.isDone() || traverseWorker.isCancelled())
					{
						traverseWorker = new BlackBoxTest(data, mainPanel);
						traverseWorker.execute();
					}
					else if (traverseWorker.getState() == StateValue.STARTED)
					{
						traverseWorker.cancel(true);
						ta_TraverseLog.setText("");
					}
				}
			});
			add(b_Traverse);
			ta_TraverseLog = new JTextArea(10, 30);
			//ta_TraverseLog.setEditable(false);
			sp_TraverseLog = new JScrollPane(ta_TraverseLog);
			add(new JLabel("Traverse Log:"));
			add(sp_TraverseLog);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					
					// check if any leaf view is clicked
					boolean leafViewChecked = false;
					if (data.currentHierarchy == null)
						return;
					
					Rectangle rect = data.currentHierarchy.getRootView().getBoundsRect();
					Rectangle actualRect = shrinkRect(rect);
					if (!actualRect.contains(e.getX(), e.getY()))
						return;
					
					List<View> selectedViews = new ArrayList<View>();
					for (View view: data.currentHierarchy.getLeafViews())
					{
						actualRect = shrinkRect(view.getBoundsRect());
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
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (paintCommand == PaintCommand.NONE || data.currentHierarchy == null)
			{
				g.drawRect(0,0,displayPanelWidth-100, deviceAreaHeight-100);
				g.drawString("Device screen layout will be shown here.", 0, 12);
			}
			else if (paintCommand == PaintCommand.LAYOUTBOUNDS)
			{
				if (data.currentHierarchy != null)
				{
					View rootView = data.currentHierarchy.getRootView();
					drawRect(g, rootView.getBoundsRect(), Color.black);
					for (View view : data.currentHierarchy.getLeafViews())
					{
						if (data.selectedView != null && view.equals(data.selectedView))
						{
							fillRect(g, view.getBoundsRect(), Color.magenta);
						}
						else
						{
							drawRect(g, view.getBoundsRect(), Color.magenta);
						}
						drawString(g, "#"+view.id, view.getBoundsRect(), Color.blue);
					}
				}
			}
			else if (paintCommand == PaintCommand.SCREENCAP_ALL)
			{
				if (data.currentHierarchy != null && 
						data.screenCapFile != null &&
						data.screenCapFile.exists())
				{
					View rootView = data.currentHierarchy.getRootView();
					try
					{
						drawImage(g, ImageIO.read(data.screenCapFile), rootView.getBoundsRect());
					} catch (IOException e) {e.printStackTrace();}
					drawRect(g, rootView.getBoundsRect(), Color.magenta);
					for (View view : data.currentHierarchy.getLeafViews())
					{
						if (data.selectedView != null && view.equals(data.selectedView))
						{
							drawRect(g, view.getBoundsRect(), Color.green);
						}
						else
						{
							drawRect(g, view.getBoundsRect(), Color.magenta);
						}
					}
				}
			}
			else if (paintCommand == PaintCommand.SCREENCAP_LEAF_ONLY)
			{
				View rootView = data.currentHierarchy.getRootView();
				drawRect(g, rootView.getBoundsRect(), Color.magenta);
				try
				{
					BufferedImage img = ImageIO.read(data.screenCapFile);
					for (View view : data.currentHierarchy.getLeafViews())
					{
						drawImage(g, img, view.getBoundsRect());
						if (data.selectedView != null && view.equals(data.selectedView))
						{
							drawRect(g, view.getBoundsRect(), Color.green);
						}
						else
						{
							drawRect(g, view.getBoundsRect(), Color.magenta);
						}
					}
				} catch (Exception e) {e.printStackTrace();}
			}
			else if (paintCommand == PaintCommand.SCREENCAP_CLICKABLE_ONLY)
			{
				View rootView = data.currentHierarchy.getRootView();
				drawRect(g, rootView.getBoundsRect(), Color.magenta);
				try
				{
					BufferedImage img = ImageIO.read(data.screenCapFile);
					for (View view : data.currentHierarchy.getClickableLeafViews())
					{
						drawImage(g, img, view.getBoundsRect());
						if (data.selectedView != null && view.equals(data.selectedView))
						{
							drawRect(g, view.getBoundsRect(), Color.green);
						}
						else
						{
							drawRect(g, view.getBoundsRect(), Color.magenta);
						}
					}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
	}
	
	class ControlPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		ControlPanel()
		{
			setPreferredSize(new Dimension(controlPanelWidth, mainHeight));
			setMinimumSize(new Dimension(controlPanelWidth, mainHeight));
			FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
			layout.setVgap(controlPanelVGap);
			layout.setHgap(controlPanelHGap);
			setLayout(layout);
			
			b_RefreshScreen = new JButton("Retrieve Current GUI");
			b_RefreshScreen.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					updatePaintCommand();
					if (layoutWorker == null || layoutWorker.isDone() || layoutWorker.isCancelled())
					{
						layoutWorker = new LayoutWorker(mainPanel);
						layoutWorker.execute();
					}
				}
			});
			
			b_ClearScreen = new JButton("Clear Screen");
			b_ClearScreen.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					paintCommand = PaintCommand.NONE;
					leftPanel.repaint();
				}
			});
			
			b_DoClick = new JButton("Send Click Event on Selected View");
			b_DoClick.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					updatePaintCommand();
					if (data.selectedView == null)
						return;
					if (clickWorker == null || clickWorker.isDone() || clickWorker.isCancelled())
					{
						clickWorker = new ClickWorker(mainPanel, data.selectedView);
						clickWorker.execute();
					}
				}
			});
			
			rb_ShowLayoutBounds = new JRadioButton("Show Layout Bounds");
			rb_ShowLayoutBounds.setSelected(true);
			rb_ShowLayoutBounds.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					paintCommand = PaintCommand.LAYOUTBOUNDS;
					leftPanel.repaint();
				}
			});
			
			rb_ShowScreenCapAll = new JRadioButton("Show Screen Cap (Whole Screen)");
			rb_ShowScreenCapAll.setSelected(false);
			rb_ShowScreenCapAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					paintCommand = PaintCommand.SCREENCAP_ALL;
					leftPanel.repaint();
				}
			});
			
			rb_ShowScreenCapLeaf = new JRadioButton("Show Screen Cap (Leaf Views Only)");
			rb_ShowScreenCapLeaf.setSelected(false);
			rb_ShowScreenCapLeaf.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					paintCommand = PaintCommand.SCREENCAP_LEAF_ONLY;
					leftPanel.repaint();
				}
			});
			
			rb_ShowScreenCapClickable = new JRadioButton("Show Screen Cap (Clickables Only)");
			rb_ShowScreenCapClickable.setSelected(false);
			rb_ShowScreenCapClickable.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					paintCommand = PaintCommand.SCREENCAP_CLICKABLE_ONLY;
					leftPanel.repaint();
				}
			});

			bg_LayoutDisplayOptions = new ButtonGroup();
			bg_LayoutDisplayOptions.add(rb_ShowLayoutBounds);
			bg_LayoutDisplayOptions.add(rb_ShowScreenCapAll);
			bg_LayoutDisplayOptions.add(rb_ShowScreenCapLeaf);
			bg_LayoutDisplayOptions.add(rb_ShowScreenCapClickable);

			ta_ViewAttr = new JTextArea(20, 40);
			ta_ViewAttr.setText("Click on a view and its attributes will show here.");
			ta_ViewAttr.setEditable(false);
			sp_ViewAttr = new JScrollPane(ta_ViewAttr);
			
			ta_RuntimeLog = new JTextArea(20, 40);
			ta_RuntimeLog.setEditable(false);
			sp_RuntimeLog = new JScrollPane(ta_RuntimeLog);
			
			add(b_RefreshScreen);
			add(b_ClearScreen);
			add(new Filler(controlPanelWidth, 1));
			add(rb_ShowLayoutBounds);
			add(new Filler(controlPanelWidth, 1));
			add(rb_ShowScreenCapLeaf);
			add(new Filler(controlPanelWidth, 1));
			add(rb_ShowScreenCapAll);
			add(new Filler(controlPanelWidth, 1));
			add(rb_ShowScreenCapClickable);
			add(new Filler(controlPanelWidth, 1));
			add(b_DoClick);
			add(new Filler(controlPanelWidth, 5));
			add(new JLabel("Runtime log:"));
			add(sp_RuntimeLog);
			add(new Filler(controlPanelWidth, 5));
			add(new JLabel("View Attributes:"));
			add(sp_ViewAttr);
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.setColor(Color.green);
			g.drawRect(0, 0, controlPanelWidth - 10, mainHeight - 10);
		}
	}
	
	class Filler extends JLabel {

		private static final long serialVersionUID = 1L;

		Filler(int width, int height)
		{
			super("");
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
		}
	}
	
}
