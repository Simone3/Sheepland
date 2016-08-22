package it.polimi.graziussi_ouafo.main;

public class GameErrorException extends Exception
{
	final private String error;
	private static final long serialVersionUID = 1L;

	public GameErrorException(String gameError)
	{
		this.error = gameError;
	}

	public String getError()
	{
		return this.error;
	}
}
