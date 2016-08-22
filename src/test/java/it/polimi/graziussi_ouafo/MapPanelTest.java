package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.view.DimensionConstants;
import it.polimi.graziussi_ouafo.view.MapPanel;
import it.polimi.graziussi_ouafo.view.ResourcePaths;

import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.JFrame;

public class MapPanelTest
{

	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		frame.setLayout(new GridLayout(1, 1));

		MapPanel panel = new MapPanel(ResourcePaths.WHITE_SHEEP.getValue(), new Point(0, 0), DimensionConstants.WHITE_SHEEP.getValue(), "" + MiscConstants.STARTING_SHEEP_PER_REGION.getValue(), "Mouse over text");
		panel.highlight(true);

		frame.add(panel);

		frame.setVisible(true);
	}

}
