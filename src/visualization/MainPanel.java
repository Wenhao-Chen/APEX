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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class MainPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	
	protected JButton b1;
	protected JTextArea t1;
	protected Worker worker;
	protected Data data;
	private MainPanel me;
	
	private static int offsetX = 20;
	private static int offsetY = 20;
	private static int shrink_factor = 4;
	

	public static void main(String[] args)
	{
		init();
	}
	
	public MainPanel()
	{
		this.data = new Data();
		me = this;
		setBorder(BorderFactory.createLineBorder(Color.black));
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		///////////// The right half of the panel  ///////////////////////
		JPanel p1 = new JPanel();
		add(p1);
		b1 = new JButton("Start");
		p1.add(b1);
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				worker = new Worker(me);
				worker.execute();
			}
		});
		
		t1 = new JTextArea();
		t1.setRows(20);
		t1.setColumns(40);
		t1.setText("Click on a view and its attributes will show here.");
		p1.add(t1);
		////////////////////////////////////////////////////////////////////
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// check if any leaf view is clicked
				boolean leafViewChecked = false;
				if (data.currentHierarchy == null)
					return;
				
				Rectangle rect = data.currentHierarchy.getRootView().getBoundsRect();
				int x = rect.x/shrink_factor + offsetX;
				int y = rect.y/shrink_factor + offsetY;
				int width = rect.width/shrink_factor;
				int height = rect.height/shrink_factor;
				Rectangle actualRect = new Rectangle(x, y, width, height);
				if (!actualRect.contains(e.getX(), e.getY()))
					return;
				
				for (View view: data.currentHierarchy.getLeafViews())
				{
					rect = view.getBoundsRect();
					x = rect.x/shrink_factor + offsetX;
					y = rect.y/shrink_factor + offsetY;
					width = rect.width/shrink_factor;
					height = rect.height/shrink_factor;
					actualRect = new Rectangle(x, y, width, height);
					if (actualRect.contains(e.getX(), e.getY()))
					{
						data.selectedView = view;
						leafViewChecked = true;
						break;
					}
				}
				if (!leafViewChecked)
					data.selectedView = null;
				if (data.selectedView != null)
					t1.setText(data.selectedView.getAttributes());
				else
					t1.setText("Click on a view and its attributes will show here.");
				repaint();
			}
		});
		
		
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
		return new Dimension(1000, 800);
	}
	
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
				
		if (this.data.currentHierarchy != null)
		{
			//1. Draw Screen Border
			drawViewRect(this.data.currentHierarchy.getRootView(), g);
			//2. Draw Each Leaf View
			for (View view : this.data.currentHierarchy.getLeafViews())
			{
				drawViewRect(view, g);
			}
		}
	}
	
	private void drawViewRect(View view, Graphics g)
	{
		Rectangle rect = view.getBoundsRect();
		int x = rect.x/shrink_factor + offsetX;
		int y = rect.y/shrink_factor + offsetY;
		int width = rect.width/shrink_factor;
		int height = rect.height/shrink_factor;
		if (data.selectedView != null && data.selectedView.equals(view))
		{
			Color oldColor = g.getColor();
			g.setColor(Color.green);
			g.fillRect(x, y, width, height);
			g.setColor(oldColor);
		}
		else
		{
			g.drawRect(x, y, width, height);
		}
		g.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		if (view.id > -1)
		{
			g.drawString("#" + view.id, x + 12, y + 12);
		}
	}

	
}
