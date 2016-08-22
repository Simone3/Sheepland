package it.polimi.graziussi_ouafo.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Player implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String name;
	private PlayerColor color;
	private int dinars;

	private int chosenShepherdIndex;
	private Map<RegionType, Integer> boughtTerrainTiles;

	public Player(String name, PlayerColor color, int dinars)
	{

		this.name = name;
		this.color = color;
		this.dinars = dinars;

		this.boughtTerrainTiles = new HashMap<RegionType, Integer>();
		RegionType[] regionTypes = RegionType.getDeckTypes();
		for (int i = 0; i < regionTypes.length; i++)
		{
			Integer value = Integer.valueOf(0);
			this.boughtTerrainTiles.put(regionTypes[i], value);
		}
	}

	public String getName()
	{

		return this.name;
	}

	public PlayerColor getColor()
	{

		return this.color;
	}

	public int getDinars()
	{

		return this.dinars;

	}

	public void addDinars(int dinars)
	{

		this.dinars = this.dinars + dinars;

	}

	public void subDinars(int dinars)
	{

		this.dinars = this.dinars - dinars;

	}

	public Map<RegionType, Integer> getBoughtTerrainTiles()
	{

		return this.boughtTerrainTiles;

	}

	public void setChosenShepherdForTurn(int i)
	{

		this.chosenShepherdIndex = i;

	}

	public int getChosenShepherdForTurn()
	{

		return this.chosenShepherdIndex;
	}

	public void unsetChosenShepherdForTurn()
	{

		this.chosenShepherdIndex = -1;

	}

	public void addBoughtTerrainTile(RegionType type)
	{
		Integer cardQuantity = this.boughtTerrainTiles.get(type);
		int currentValue = cardQuantity.intValue();
		this.boughtTerrainTiles.put(type, Integer.valueOf(currentValue + 1));
	}

	public boolean hasChosenShepherdForTurn()
	{
		return (this.chosenShepherdIndex != -1);
	}
}
