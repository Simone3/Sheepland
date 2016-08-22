package it.polimi.graziussi_ouafo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public enum RegionType implements Serializable
{
	SHEEPSBURG, MOUNTAIN, DESERT, SWAMP, PASTURE, FOREST, HILL;

	public static RegionType[] getDeckTypes()
	{
		List<RegionType> temp = new ArrayList<RegionType>();

		RegionType[] values = RegionType.values();
		for (int i = 0; i < values.length; i++)
		{
			if (!values[i].equals(RegionType.SHEEPSBURG))
			{
				temp.add(values[i]);
			}
		}

		return temp.toArray(new RegionType[temp.size()]);
	}
}