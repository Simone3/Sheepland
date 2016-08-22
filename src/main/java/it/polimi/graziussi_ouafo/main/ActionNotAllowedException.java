package it.polimi.graziussi_ouafo.main;

public class ActionNotAllowedException extends Exception
{
	private static final long serialVersionUID = 1L;
	final private String text;

	public ActionNotAllowedException(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return this.text;
	}
}
