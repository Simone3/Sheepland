package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.view.TextArea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TextAreaTest
{

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JFrame frame = new JFrame();
				frame.setSize(300, 300);
				frame.setLayout(new GridLayout(1, 1));

				TextArea panel = new TextArea("Text", Color.WHITE, new Dimension(300, 300));

				panel.setText("asdslnla sdfjnsdfjl dsfskfjdsfd", Color.PINK);

				frame.add(panel.getComponent());

				panel.appendText(". Other text, other text.", Color.YELLOW);

				panel.appendText(" Blah blah blah", Color.GREEN);

				panel.appendText(" a a a a a aa a a a a a aa a a a a a \na a a a a a a a a a a a a a a a a a a a a a a ");

				frame.setVisible(true);
			}
		});
	}

}
