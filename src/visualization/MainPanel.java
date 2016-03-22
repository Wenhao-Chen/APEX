package visualization;

import gui.Hierarchy;
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
import java.io.File;
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

public class MainPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private MainPanel me;
	
	protected JButton b_RefreshScreen, b_DoClick, b_ClearScreen;
	protected JRadioButton rb_ShowLayoutBounds, rb_ShowScreenCapAll, rb_ShowScreenCapLeaf;
	protected ButtonGroup bg_LayoutDisplayOptions;
	protected JTextArea ta_ViewAttr, t2, ta_RuntimeLog;
	protected JScrollPane sp_ViewAttr, sp_RuntimeLog;
	protected Data data;
	protected ClickWorker clickWorker;
	protected LayoutWorker layoutWorker;
	
	protected enum PaintCommand {NONE, LAYOUTBOUNDS, SCREENCAP_ALL, SCREENCAP_LEAF_ONLY};
	protected PaintCommand paintCommand = PaintCommand.LAYOUTBOUNDS;
	
	private static int offsetX = 40;
	private static int offsetY = 40;
	private static int shrink_factor = 3;
	private static int reservedWidth = 700;
	private static int reservedHeight = 500;
		
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
	
	public Dimension getPreferredSize()
	{
		return new Dimension(1400, 1000);
	}
	
	private void initWidgets()
	{

		b_RefreshScreen = new JButton("Retrieve Current GUI");
		b_RefreshScreen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				updatePaintCommand();
				if (layoutWorker == null || layoutWorker.isDone() || layoutWorker.isCancelled())
				{
					layoutWorker = new LayoutWorker(me);
					layoutWorker.execute();
				}
			}
		});
		
		b_DoClick = new JButton("Tap On Selected View");
		b_DoClick.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				updatePaintCommand();
				if (data.selectedView == null)
					return;
				if (clickWorker == null || clickWorker.isDone() || clickWorker.isCancelled())
				{
					clickWorker = new ClickWorker(me);
					clickWorker.execute();
				}
			}
		});
		
		b_ClearScreen = new JButton("Clear Screen");
		b_ClearScreen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				paintCommand = PaintCommand.NONE;
				repaint();
			}
		});
		
		rb_ShowLayoutBounds = new JRadioButton("Show Layout Bounds");
		rb_ShowLayoutBounds.setSelected(true);
		rb_ShowLayoutBounds.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				paintCommand = PaintCommand.LAYOUTBOUNDS;
				repaint();
			}
		});
		
		
		rb_ShowScreenCapAll = new JRadioButton("Show Screen Cap (Whole Screen)");
		rb_ShowScreenCapAll.setSelected(false);
		rb_ShowScreenCapAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				paintCommand = PaintCommand.SCREENCAP_ALL;
				repaint();
			}
		});
		
		rb_ShowScreenCapLeaf = new JRadioButton("Show Screen Cap (Leaf Views Only)");
		rb_ShowScreenCapLeaf.setSelected(false);
		rb_ShowScreenCapLeaf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				paintCommand = PaintCommand.SCREENCAP_LEAF_ONLY;
				repaint();
			}
		});

		
		bg_LayoutDisplayOptions = new ButtonGroup();
		bg_LayoutDisplayOptions.add(rb_ShowLayoutBounds);
		bg_LayoutDisplayOptions.add(rb_ShowScreenCapAll);
		bg_LayoutDisplayOptions.add(rb_ShowScreenCapLeaf);
		
		
		ta_ViewAttr = new JTextArea(20, 40);
		ta_ViewAttr.setText("Click on a view and its attributes will show here.");
		ta_ViewAttr.setEditable(false);
		sp_ViewAttr = new JScrollPane(ta_ViewAttr);
		
		t2 = new JTextArea(10, 40);
		t2.setLineWrap(true);
		t2.setEditable(false);
		
		ta_RuntimeLog = new JTextArea(10, 40);
		ta_RuntimeLog.setEditable(false);
		sp_RuntimeLog = new JScrollPane(ta_RuntimeLog);
		
	}
	
	private void organizeLayout()
	{
		FlowLayout controlLayout = new FlowLayout(FlowLayout.LEADING);
		JPanel controlPanel = new JPanel(controlLayout);
		controlPanel.setPreferredSize(new Dimension(500, 800));
		controlPanel.setMinimumSize(new Dimension(500, 800));
		controlPanel.add(b_RefreshScreen);
		controlPanel.add(b_ClearScreen);
		controlPanel.add(new FillerLabel(450, 1));
		controlPanel.add(rb_ShowLayoutBounds);
		controlPanel.add(new FillerLabel(450, 1));
		controlPanel.add(rb_ShowScreenCapAll);
		controlPanel.add(new FillerLabel(450, 1));
		controlPanel.add(rb_ShowScreenCapLeaf);
		controlPanel.add(new FillerLabel(450, 1));
		controlPanel.add(b_DoClick);
		controlPanel.add(new FillerLabel(450, 1));
		controlPanel.add(new JLabel("Runtime log:"));
		controlPanel.add(sp_RuntimeLog);
		controlPanel.add(new FillerLabel(450, 15));
		controlPanel.add(new JLabel("View Attributes:"));
		controlPanel.add(sp_ViewAttr);
		controlPanel.add(new FillerLabel(450, 15));
		controlPanel.add(new JLabel("Unused text area:"));
		controlPanel.add(t2);
		
		FlowLayout layout = new FlowLayout(FlowLayout.TRAILING);
		setLayout(layout);
		add(new FillerLabel(reservedWidth + offsetX, reservedHeight + offsetY + 10));
		add(new JLabel("Test1"));
		add(new JLabel("Test2"));
		add(new JLabel("Test3"));
		add(controlPanel);
	}
	

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (paintCommand == PaintCommand.NONE || this.data.currentHierarchy == null)
		{
			g.drawRect(offsetX, offsetY, reservedWidth, reservedHeight);
			g.drawString("Device screen layout will be shown here.", offsetX+10, offsetY+10);
		}
		else if (paintCommand == PaintCommand.LAYOUTBOUNDS)
		{
			paintHierarchy(this.data.currentHierarchy, g);
		}
		else if (paintCommand == PaintCommand.SCREENCAP_ALL)
		{
			drawScreenCapAll(this.data.currentHierarchy, this.data.screenCapFile, g);
		}
		else if (paintCommand == PaintCommand.SCREENCAP_LEAF_ONLY)
		{
			drawScreenCapLeaves(this.data.currentHierarchy, this.data.screenCapFile, g);
		}
	}
	
	private void drawScreenCapAll(Hierarchy h, File imgFile, Graphics g)
	{
		if (h == null || imgFile == null || !imgFile.exists())
			return;
		try
		{
			BufferedImage img = ImageIO.read(imgFile);
			drawImage(h.getRootView(), img, g);
			drawViewRect(data.selectedView, g, Color.green, false, false);
			for (View view : data.currentHierarchy.getLeafViews())
			{
				if (!view.equals(data.selectedView))
					drawViewRect(view, g, Color.pink, false, false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void drawScreenCapLeaves(Hierarchy h, File imgFile, Graphics g)
	{
		if (h == null || imgFile == null || !imgFile.exists())
			return;
		try
		{
			BufferedImage img = ImageIO.read(imgFile);
			drawViewRect(h.getRootView(), g, Color.black, false, false);
			for (View view : h.getLeafViews())
			{
				drawImage(view, img, g);
			}
			
			drawViewRect(data.selectedView, g, Color.green, false, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void drawImage(View view, BufferedImage img, Graphics g)
	{
		Rectangle srcR = view.getBoundsRect();
		Rectangle dstR = getActualRect(srcR);
		g.drawImage(img,
				dstR.x, dstR.y, dstR.x+dstR.width, dstR.y+dstR.height,
				srcR.x, srcR.y, srcR.x+srcR.width, srcR.y+srcR.height, null);
	}
	
	
	private void paintHierarchy(Hierarchy h, Graphics g)
	{
		if (h == null)
			return;
		drawViewRect(h.getRootView(), g, Color.gray, true, true);
		drawViewRect(data.selectedView, g, Color.magenta, true, true);
		for (View view : h.getLeafViews())
		{
			if (!view.equals(data.selectedView))
				drawViewRect(view, g, Color.magenta, true, true);
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
	
	private void drawViewRect(View view, Graphics g, Color color, boolean fillSelected, boolean textID)
	{
		if (view == null)
			return;
		Color oldColor = g.getColor();
		g.setColor(color);
		Rectangle rect = getActualRect(view.getBoundsRect());
		if (data.selectedView != null && data.selectedView.equals(view) && fillSelected)
		{
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
		}
		else
		{
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		g.setColor(oldColor);
		g.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		if (view.id > -1 && textID)
		{
			g.drawString("#" + view.id, rect.x + 2, rect.y + 12);
		}
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
