package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.main.Main;
import it.polimi.graziussi_ouafo.model.PlayerColor;

import java.util.Arrays;
import java.util.List;

public class GameWindowTest
{

	public static void main(String[] args) throws InterruptedException
	{
		String[] playerArray = new String[] { "A", "B", "C" };
		// String[] playerArray = new String[]{"A", "B"};
		List<String> players = Arrays.asList(playerArray);

		PlayerColor[] colorArray = new PlayerColor[] { PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN };
		// PlayerColor[] colorArray = new PlayerColor[]{PlayerColor.RED, PlayerColor.BLUE};
		List<PlayerColor> colors = Arrays.asList(colorArray);

		Main main = new Main();
		Thread.sleep(1000);
		main.startOfflineGame(players, colors);
	}

}
