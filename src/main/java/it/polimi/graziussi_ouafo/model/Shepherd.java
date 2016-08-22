package it.polimi.graziussi_ouafo.model;

import java.io.Serializable;

public class Shepherd implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Player owner;
	private int number;

	public Shepherd(Player owner, int number)
	{
		this.owner = owner;
		this.number = number;
	}

	public Player getOwner()
	{
		return this.owner;
	}

	public int getNumber()
	{
		return this.number;
	}

	@Override
	public String toString()
	{
		return this.owner.getName() + "|" + this.number;
	}
}