package it.polimi.graziussi_ouafo.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;

public class MapPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private JLabel textAndImageLabel;
	private ImageIcon image;

	private long animationStart;
	private static final int ANIMATION_RUN_TIME = 1000;
	private static final int ANIMATION_TIME_UNIT = 40;
	
	private Timer timer;

	public MapPanel(String imageFileName, Point position, Dimension dimensions, String text, String mouseOverText)
	{
		super();
		super.setLayout(new BorderLayout());

		// Set dimensions
		this.setSize(dimensions);

		// Set position
		this.setPosition(position);

		// Create label for text and image
		this.textAndImageLabel = new JLabel();

		// Add panel image
		if (imageFileName != null && !"".equals(imageFileName))
		{
			this.setImage(imageFileName);
		}

		// Add text
		this.setText(text);

		// Add text displayed when mouse over
		ToolTipManager.sharedInstance().setInitialDelay(0);
		this.setMouseOverText(mouseOverText);

		// Add label
		this.add(this.textAndImageLabel, BorderLayout.CENTER);

		// Panel background transparent
		this.setOpaque(false);
	}

	final public void setPosition(Point target)
	{
		Dimension offset = DimensionConstants.OFFSET.getValue();
		this.setBounds((int) (target.getX() + offset.getWidth()), (int) (target.getY() + offset.getHeight()), this.getWidth(), this.getHeight());
	}

	synchronized public void animate(Point target)
	{
		// If the current panel is moving right now, stop it and start the new animation
		if(timer!=null && timer.isRunning()) timer.stop();
		
		// Starting and final positions
		Dimension offset = DimensionConstants.OFFSET.getValue();
		final Rectangle from = new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		final Rectangle to = new Rectangle((int) (target.getX() + offset.getWidth()), (int) (target.getY() + offset.getHeight()), this.getWidth(), this.getHeight());

		// Create Swing Timer
		timer = new Timer(MapPanel.ANIMATION_TIME_UNIT, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Compute progress
				long duration = System.currentTimeMillis() - MapPanel.this.animationStart;
				double progress = (double) duration / (double) MapPanel.ANIMATION_RUN_TIME;

				// Stop animation when we reach the goal
				if (progress > 1f)
				{
					progress = 1f;
					((Timer) e.getSource()).stop();
				}

				// New positions for this step
				int newX = (int) (from.getX() + progress * (to.getX() - from.getX()));
				int newY = (int) (from.getY() + progress * (to.getY() - from.getY()));

				// Move panel
				Rectangle target = new Rectangle(newX, newY, MapPanel.this.getWidth(), MapPanel.this.getHeight());
				MapPanel.this.setBounds(target);
			}
		});
		timer.setRepeats(true);
		timer.setCoalesce(true);
		timer.setInitialDelay(0);
		this.animationStart = System.currentTimeMillis();
		timer.start();
	}

	final public void setText(String text)
	{
		this.textAndImageLabel.setText(text);
		this.textAndImageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		this.textAndImageLabel.setVerticalTextPosition(SwingConstants.CENTER);
	}

	final public void setMouseOverText(String mouseOverText)
	{
		this.setToolTipText(mouseOverText);
	}

	final public void setImage(String fileName)
	{
		URL imagePath = this.getClass().getResource(fileName);
		this.image = new ImageIcon(imagePath);
		this.textAndImageLabel.setIcon(this.image);
	}

	public boolean checkIfClickValid(int x, int y)
	{
		return true;
	}

	final public void highlight(boolean show)
	{
		if (show)
		{
			this.setBorder(new LineBorder(Color.YELLOW, 2));
		}
		else
		{
			this.setBorder(null);
		}
	}
}
