package it.polimi.graziussi_ouafo.view;

import java.awt.Dimension;

public enum DimensionConstants
{
	// Window
	WINDOW(new Dimension(880, 725)),

	// Center
	SCREEN_CENTER(new Dimension(500, 675)), MAP(new Dimension(500, 675)),

	// North
	PLAYER_INFO(new Dimension(880, 25)),

	// East
	GAME_INFO(new Dimension(225, 675)),

	// West
	DECK_BUTTONS(new Dimension(130, 675)),

	// Map components
	ROAD(new Dimension(30, 30)), WHITE_SHEEP(new Dimension(40, 40)), BLACK_SHEEP(new Dimension(40, 40)), WOLF(new Dimension(40, 40)), TIMED_MESSAGE(new Dimension(120, 54)), DIE_RESULT_MESSAGE(new Dimension(50, 50)),
	OFFSET(new Dimension(-445, -28)),

	// Main Menu
	MAIN_MENU(new Dimension(500, 500)), MENU_TITLE(new Dimension(500, 100)), MENU_SIDE_BAR(new Dimension(100, 400));

	Dimension dimension;

	DimensionConstants(Dimension dimension)
	{
		this.dimension = dimension;
	}

	public Dimension getValue()
	{
		return this.dimension;
	}
}
