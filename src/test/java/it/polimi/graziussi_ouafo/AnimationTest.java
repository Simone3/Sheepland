package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.view.DimensionConstants;
import it.polimi.graziussi_ouafo.view.MapPanel;
import it.polimi.graziussi_ouafo.view.ResourcePaths;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

public class AnimationTest
{

	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setLayout(new BorderLayout());

		MapPanel panel = new MapPanel(ResourcePaths.BLACK_SHEEP.getValue(), new Point(0, 0), DimensionConstants.BLACK_SHEEP.getValue(), "", "");
		MapPanel panel2 = new MapPanel(ResourcePaths.BLACK_SHEEP.getValue(), new Point(0, 0), DimensionConstants.BLACK_SHEEP.getValue(), "", "");
		panel.setBounds(111, 111, panel.getWidth(), panel.getHeight());
		panel2.setBounds(0, 150, panel2.getWidth(), panel2.getHeight());
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.add(panel, new Integer(0));
		layeredPane.add(panel2, new Integer(0));
		frame.add(layeredPane, BorderLayout.CENTER);
		Dimension offset = DimensionConstants.OFFSET.getValue();
		panel.animate(new Point((int) (0 - offset.getWidth()), (int) (0 - offset.getHeight())));
		panel2.animate(new Point((int) (100 - offset.getWidth()), (int) (100 - offset.getHeight())));
		panel.animate(new Point((int) (200 - offset.getWidth()), (int) (200 - offset.getHeight())));
		frame.setVisible(true);
	}

}
