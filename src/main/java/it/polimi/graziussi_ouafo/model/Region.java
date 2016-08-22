package it.polimi.graziussi_ouafo.model;

import java.io.Serializable;

public class Region implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final Integer id;

	private RegionType type;

	private boolean hasBlackSheep;
	private boolean hasWolf;

	private int whiteSheepNumber;
	private int ramNumber;
	private int lambNumber;

	public Region(Integer id, RegionType type, boolean hasBlackSheep, int whiteSheepNumber, int ramNumber, boolean hasWolf)
	{
		this.type = type;
		this.hasBlackSheep = hasBlackSheep;
		this.whiteSheepNumber = whiteSheepNumber;
		this.id = id;
		this.hasWolf = hasWolf;
		this.ramNumber = ramNumber;
		this.lambNumber = 0;
	}

	public RegionType getType()
	{

		return this.type;
	}

	public boolean hasBlackSheep()
	{

		return this.hasBlackSheep;
	}

	public void placeBlackSheep()
	{

		this.hasBlackSheep = true;
	}

	public void placeWolf()
	{

		this.hasWolf = true;
	}

	public void removeBlackSheep()
	{

		this.hasBlackSheep = false;
	}

	public void removeWolf()
	{

		this.hasWolf = false;
	}

	public int getLambNumber()
	{
		return this.lambNumber;
	}

	public void removeLamb()
	{
		this.lambNumber--;
	}

	public void placeLamb()
	{
		this.lambNumber++;
	}

	public Integer getId()
	{
		return this.id;
	}

	public boolean hasWolf()
	{
		return this.hasWolf;
	}

	public int getRamNumber()
	{
		return this.ramNumber;
	}

	public void removeRam()
	{
		this.ramNumber--;
	}

	public void placeRam()
	{
		this.ramNumber++;
	}

	public int getWhiteSheepNumber()
	{
		return this.whiteSheepNumber;
	}

	public void removeWhiteSheep()
	{
		this.whiteSheepNumber--;
	}

	public void placeWhiteSheep()
	{
		this.whiteSheepNumber++;
	}

	@Override
	public String toString()
	{
		return "Region " + this.id + ": type=" + this.type.toString() + ", black sheep=" + this.hasBlackSheep + ", white sheep=" + this.whiteSheepNumber + ", has wolf=" + this.hasWolf;
	}
}
