package it.polimi.graziussi_ouafo.model;

import java.awt.Color;
import java.io.Serializable;

public enum PlayerColor implements Serializable
{
	RED(Color.RED), BLUE(Color.BLUE), GREEN(Color.GREEN), ORANGE(Color.ORANGE);

	Color color;

	PlayerColor(Color color)
	{
		this.color = color;
	}

	public Color getColor()
	{
		return this.color;
	}

	@Override
	public String toString()
	{
		return this.name();
	}
}
