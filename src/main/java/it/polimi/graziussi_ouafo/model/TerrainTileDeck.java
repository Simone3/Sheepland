package it.polimi.graziussi_ouafo.model;

import it.polimi.graziussi_ouafo.main.MiscConstants;

import java.io.Serializable;

public class TerrainTileDeck implements Serializable
{
	private static final long serialVersionUID = 1L;

	final private RegionType type;
	private int currentCardPrice;

	public TerrainTileDeck(RegionType type)
	{
		this.type = type;
		this.currentCardPrice = MiscConstants.STARTING_TERRAIN_TILE_PRICE.getValue();
	}

	public RegionType getType()
	{
		return this.type;
	}

	public int getCurrentCardPrice()
	{
		return this.currentCardPrice;
	}

	public void increaseCurrentCardPrice()
	{
		this.currentCardPrice += MiscConstants.TERRAIN_TILE_INCREMENT.getValue();
	}

	public boolean isAvailable()
	{
		return (this.currentCardPrice <= MiscConstants.MAX_TERRAIN_TILE_PRICE.getValue());
	}
}
