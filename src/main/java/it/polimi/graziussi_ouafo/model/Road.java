package it.polimi.graziussi_ouafo.model;

import java.io.Serializable;
import java.util.Arrays;

public class Road implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final int number;
	private boolean hasFence;
	private Shepherd shepherd;
	private final Region[] regions;
	private final Integer id;

	public Road(Integer id, int number, Region[] regions)
	{
		this.number = number;
		this.regions = Arrays.copyOf(regions, regions.length);
		this.id = id;
	}

	public boolean hasFence()
	{

		return this.hasFence;

	}

	public void placeFence()
	{

		this.hasFence = true;
	}

	public void removeShepherd()
	{

		this.shepherd = null;
	}

	public void placeShepherd(Shepherd shepherd)
	{

		this.shepherd = shepherd;
	}

	public Region[] getRegions()
	{

		return this.regions;
	}

	public Shepherd getShepherd()
	{
		return this.shepherd;
	}

	public int getNumber()
	{
		return this.number;
	}

	// Checks if the road has ANY shepherd
	public boolean hasShepherd()
	{
		return (this.shepherd != null);
	}

	public Integer getId()
	{
		return this.id;
	}

	public boolean isEmpty()
	{
		return (!this.hasFence() && !this.hasShepherd());
	}

	@Override
	public String toString()
	{
		String shepherdInfo = (this.shepherd == null) ? "/" : this.shepherd.toString();
		return "Road " + this.id + ": number=" + this.number + ", fence=" + this.hasFence + ", shepherd=" + shepherdInfo;
	}
}
