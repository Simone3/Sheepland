package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EndOfGameManager
{
	TurnScheduler turnScheduler;

	void manageEndOfGame(TurnScheduler turnScheduler) throws RemoteException
	{
		this.turnScheduler = turnScheduler;

		this.addDinarsFromTerrainTiles();

		turnScheduler.updateGUIEndOfGame(this.getRank(turnScheduler.getGameState().getPlayers()));
	}

	private void addDinarsFromTerrainTiles()
	{

		GameState gameState = this.turnScheduler.getGameState();
		Player[] playerList = gameState.getPlayers();
		Set<Region> regionSet = gameState.getAllRegions();
		Iterator<Region> itr;
		Map<Player, Integer> playersScore = new HashMap<Player, Integer>();
		Map<RegionType, Integer> terrainsCounter;
		Map<RegionType, Integer> sheepNumberTerrain = new HashMap<RegionType, Integer>();
		RegionType[] regionTypes = RegionType.getDeckTypes();

		// Initialize map with sheep numbers
		for (int i = 0; i < regionTypes.length; i++)
		{
			sheepNumberTerrain.put(regionTypes[i], Integer.valueOf(0));
		}

		// Loop all regions (without duplicates) to compute sheep number
		itr = regionSet.iterator();
		while (itr.hasNext())
		{
			int previousCount;
			int sheepCount;
			Region region = itr.next();

			if (Arrays.asList(regionTypes).contains(region.getType()))
			{
				sheepCount = region.getWhiteSheepNumber() * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue() + region.getRamNumber() * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue();

				if (region.hasBlackSheep())
				{
					sheepCount = sheepCount + MiscConstants.BLACK_SHEEP_FINAL_VALUE.getValue();
				}

				previousCount = sheepNumberTerrain.get(region.getType()).intValue();
				sheepNumberTerrain.put(region.getType(), Integer.valueOf(sheepCount + previousCount));

			}
		}

		// Compute player scores with the previously built map
		for (int i = 0; i < playerList.length; i++)
		{
			int tempScore = 0;
			terrainsCounter = playerList[i].getBoughtTerrainTiles();
			Iterator<RegionType> itr1 = terrainsCounter.keySet().iterator();

			while (itr1.hasNext())
			{
				RegionType key = itr1.next();
				Integer val = terrainsCounter.get(key).intValue();
				tempScore = tempScore + val * (sheepNumberTerrain.get(key));
			}

			playersScore.put(playerList[i], Integer.valueOf(tempScore));
		}

		// Add dinars to each player with the previously computed map
		for (int i = 0; i < playerList.length; i++)
		{

			int val = playersScore.get(playerList[i]).intValue();
			playerList[i].addDinars(val);

		}

	}

	private List<Player> getRank(Player[] playerList)
	{
		List<Player> tempList = new ArrayList<Player>(Arrays.asList(playerList));
		List<Player> rank = new ArrayList<Player>();

		// il ciclo FOR che segue crea l'arrayList rank che è la classifica dei Player
		while (!tempList.isEmpty())
		{
			Player topPlayer = tempList.get(0);

			Iterator<Player> itr = tempList.iterator();
			while (itr.hasNext())
			{
				Player currentPlayer = itr.next();
				if ((currentPlayer.getDinars() > topPlayer.getDinars()))
				{
					topPlayer = currentPlayer;
				}
			}

			rank.add(topPlayer);
			tempList.remove(topPlayer);
		}

		return rank;
	}
}
