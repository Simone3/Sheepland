package it.polimi.graziussi_ouafo.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class TextArea
{
	private JScrollPane component;
	private Style style;
	private StyledDocument document;
	private StyleContext context = new StyleContext();

	public TextArea(String text, Dimension dimension)
	{
		this(text, Color.BLACK, dimension);
	}

	public TextArea(String text, Color color, Dimension dimension)
	{
		this.document = new DefaultStyledDocument(this.context);

		// Text style
		this.style = this.document.getStyle("mystyle" + color.toString());
		if (this.style == null)
		{
			this.style = this.createStyle(color);
		}

		// Set text
		this.setText(text);

		// Crate TextPane
		JTextPane pane = new JTextPane(this.document);
		pane.setEditable(false);
		pane.setOpaque(false);

		// Create ScrollPane, setting the area dimension
		this.component = new JScrollPane(pane);
		this.component.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.component.setPreferredSize(dimension);
		this.component.setMinimumSize(new Dimension(10, 10));
		this.component.setOpaque(false);
		this.component.setBorder(null);
	}

	public JScrollPane getComponent()
	{
		return this.component;
	}

	public void setText(String text, Color color)
	{
		this.removeAllText();
		this.appendText(text, color);
	}

	public void setText(String text)
	{
		this.setText(text, Color.BLACK);
	}

	public void appendText(String text, Color color)
	{
		this.style = this.document.getStyle("mystyle" + color.toString());
		if (this.style == null)
		{
			this.style = this.createStyle(color);
		}

		try
		{
			this.document.insertString(this.document.getLength(), text, this.style);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void appendText(String text)
	{
		this.appendText(text, Color.BLACK);
	}

	public void removeAllText()
	{
		try
		{
			this.document.remove(0, this.document.getLength());
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	Style createStyle(Color color)
	{
		Style tempStyle = this.context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setAlignment(tempStyle, StyleConstants.ALIGN_CENTER);
		StyleConstants.setFontSize(tempStyle, 14);
		StyleConstants.setForeground(tempStyle, color);

		return tempStyle;
	}
}
