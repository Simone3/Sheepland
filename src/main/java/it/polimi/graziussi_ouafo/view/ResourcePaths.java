package it.polimi.graziussi_ouafo.view;

public enum ResourcePaths
{
	/***** IMAGES *****/
	LOGO("/images/Logo.jpg"), MAP_BACKGROUND("/images/GameMap.jpg"),

	EMPTY_ROAD("/images/RoadEmpty.png"), ROAD_FENCE("/images/RoadFence.png"), ROAD_SHEPHERD_RED("/images/RoadShepherdRed.png"), ROAD_SHEPHERD_BLUE("/images/RoadShepherdBlue.png"), ROAD_SHEPHERD_GREEN("/images/RoadShepherdGreen.png"), ROAD_SHEPHERD_ORANGE("/images/RoadShepherdOrange.png"),

	DECK_MOUNTAIN("/images/TerrainMountain.png"), DECK_DESERT("/images/TerrainDesert.png"), DECK_SWAMP("/images/TerrainSwamp.png"), DECK_PASTURE("/images/TerrainPasture.png"), DECK_FOREST("/images/TerrainForest.png"), DECK_HILL("/images/TerrainHill.png"),

	WHITE_SHEEP("/images/SheepWhite.png"), BLACK_SHEEP("/images/SheepBlack.png"), WOLF("/images/Wolf.png"),

	TIMED_MESSAGE("/images/TimedMessage.png"),
	
	DIE1("/images/Die1.png"), DIE2("/images/Die2.png"), DIE3("/images/Die3.png"), DIE4("/images/Die4.png"), DIE5("/images/Die5.png"), DIE6("/images/Die6.png"),

	/***** FILES *****/
	MAP_XML("/xml/GameMapInfo.xml");

	String name;

	ResourcePaths(String imageName)
	{
		this.name = imageName;
	}

	public String getValue()
	{
		return this.name;
	}
}
