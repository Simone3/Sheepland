package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.EndOfGameManager;
import it.polimi.graziussi_ouafo.controller.TurnScheduler;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EndOfGameManagerTest
{
	TurnScheduler turnScheduler;

	@Before
	public void setUp() throws Exception
	{
		String[] playerNamesA = new String[] { "A", "B", "C" };
		PlayerColor[] playerColorsA = new PlayerColor[] { PlayerColor.RED, PlayerColor.BLUE, PlayerColor.GREEN };
		List<String> playerNames = Arrays.asList(playerNamesA);
		List<PlayerColor> playerColors = Arrays.asList(playerColorsA);

		List<TerrainTileDeck> decks = new ArrayList<TerrainTileDeck>();
		RegionType[] regionTypes = RegionType.getDeckTypes();
		for (RegionType type : regionTypes)
		{
			decks.add(new TerrainTileDeck(type));
		}

		FakeGameWindow client = new FakeGameWindow(new FakeGameMap());

		this.turnScheduler = new TurnScheduler(true, playerNames, playerColors, client);
	}

	@Test
	public void testGetRank()
	{
		try
		{
			// Get game state
			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			GameState gameState = (GameState) method.invoke(this.turnScheduler);
			Player[] players = gameState.getPlayers();

			// Get player random tile
			RegionType[] randomTile = new RegionType[players.length];
			for (int i = 0; i < players.length; i++)
			{
				for (Map.Entry<RegionType, Integer> entry : players[i].getBoughtTerrainTiles().entrySet())
				{
					if (entry.getValue().equals(new Integer(1)))
					{
						randomTile[i] = entry.getKey();
						break;
					}
				}
			}

			// Add some dinars
			int[] addDinars = new int[] { 20, 50, 10 };

			// Add some dinars
			for (int i = 0; i < players.length; i++)
			{
				players[i].addDinars(addDinars[i]);
			}

			// Add some terrain tiles
			players[0].addBoughtTerrainTile(RegionType.DESERT);
			players[0].addBoughtTerrainTile(RegionType.DESERT);
			players[0].addBoughtTerrainTile(RegionType.HILL);
			players[1].addBoughtTerrainTile(RegionType.HILL);
			players[1].addBoughtTerrainTile(RegionType.MOUNTAIN);
			players[2].addBoughtTerrainTile(RegionType.PASTURE);
			players[2].addBoughtTerrainTile(RegionType.PASTURE);
			players[2].addBoughtTerrainTile(RegionType.PASTURE);

			// Move some sheep around
			Region[] regions = gameState.getRoad(32).getRegions();
			regions[0].removeWhiteSheep(); // mountain->pasture
			regions[1].placeWhiteSheep();
			System.out.println("Move white sheep from " + regions[0].getId().intValue() + " (" + regions[0].getType() + ", WSN=" + regions[0].getWhiteSheepNumber() + ") to " + regions[1].getId().intValue() + " (" + regions[1].getType() + ", WSN=" + regions[1].getWhiteSheepNumber() + ")");
			regions = gameState.getRoad(24).getRegions();
			regions[0].removeWhiteSheep(); // mountain->pasture
			regions[1].placeWhiteSheep();
			System.out.println("Move white sheep from " + regions[0].getId().intValue() + " (" + regions[0].getType() + ", WSN=" + regions[0].getWhiteSheepNumber() + ") to " + regions[1].getId().intValue() + " (" + regions[1].getType() + ", WSN=" + regions[1].getWhiteSheepNumber() + ")");
			regions = gameState.getRoad(17).getRegions(); // swamp -> desert
			regions[0].removeWhiteSheep();
			regions[1].placeWhiteSheep();
			System.out.println("Move white sheep from " + regions[0].getId().intValue() + " (" + regions[0].getType() + ", WSN=" + regions[0].getWhiteSheepNumber() + ") to " + regions[1].getId().intValue() + " (" + regions[1].getType() + ", WSN=" + regions[1].getWhiteSheepNumber() + ")");
			regions = gameState.getRoad(40).getRegions(); // hill->forest
			regions[0].removeWhiteSheep();
			regions[1].placeWhiteSheep();
			System.out.println("Move white sheep from " + regions[0].getId().intValue() + " (" + regions[0].getType() + ", WSN=" + regions[0].getWhiteSheepNumber() + ") to " + regions[1].getId().intValue() + " (" + regions[1].getType() + ", WSN=" + regions[1].getWhiteSheepNumber() + ")");

			// Place black sheep
			System.out.println("Move black sheep from " + gameState.getBlackSheepRegion().getId().intValue() + " (" + gameState.getBlackSheepRegion().getType() + ") to " + regions[0].getId().intValue() + " (" + regions[0].getType() + ")");
			gameState.getBlackSheepRegion().removeBlackSheep();
			regions[0].placeBlackSheep(); // hill

			// Compute values manually for testing
			Map<RegionType, Integer> sheepNumberTerrain = new HashMap<RegionType, Integer>();
			sheepNumberTerrain.put(RegionType.MOUNTAIN, new Integer(MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue()));
			sheepNumberTerrain.put(RegionType.HILL, new Integer(2 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue() + MiscConstants.BLACK_SHEEP_FINAL_VALUE.getValue()));
			sheepNumberTerrain.put(RegionType.PASTURE, new Integer(5 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue()));
			sheepNumberTerrain.put(RegionType.FOREST, new Integer(4 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue()));
			sheepNumberTerrain.put(RegionType.DESERT, new Integer(4 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue()));
			sheepNumberTerrain.put(RegionType.SWAMP, new Integer(2 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue()));

			// Create manager
			EndOfGameManager manager = new EndOfGameManager();
			Field field = EndOfGameManager.class.getDeclaredField("turnScheduler");
			field.setAccessible(true);
			field.set(manager, this.turnScheduler);

			// Add dinars from tiles
			method = EndOfGameManager.class.getDeclaredMethod("addDinarsFromTerrainTiles");
			method.setAccessible(true);
			method.invoke(manager);

			// Check dinar values
			Assert.assertEquals(MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue() + sheepNumberTerrain.get(randomTile[0]).intValue() + addDinars[0] + 10 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue() + MiscConstants.BLACK_SHEEP_FINAL_VALUE.getValue(), players[0].getDinars());
			Assert.assertEquals(MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue() + sheepNumberTerrain.get(randomTile[1]).intValue() + addDinars[1] + 3 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue() + MiscConstants.BLACK_SHEEP_FINAL_VALUE.getValue(), players[1].getDinars());
			Assert.assertEquals(MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue() + sheepNumberTerrain.get(randomTile[2]).intValue() + addDinars[2] + 15 * MiscConstants.WHITE_SHEEP_FINAL_VALUE.getValue(), players[2].getDinars());

			// Get player final chart
			method = EndOfGameManager.class.getDeclaredMethod("getRank", Player[].class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<Player> chart = (ArrayList<Player>) method.invoke(manager, new Object[] { gameState.getPlayers() });

			// Check chart values
			Assert.assertTrue(chart != null);
			Assert.assertTrue(chart.size() == players.length);
			for (int i = 0; i < chart.size() - 1; i++)
			{
				Assert.assertTrue(chart.get(i).getDinars() >= chart.get(i + 1).getDinars());
			}

			// Print chart
			for (int i = 0; i < chart.size(); i++)
			{
				System.out.println((i + 1) + ") " + chart.get(i).getName() + " (" + chart.get(i).getDinars() + ")");
			}
		}
		catch (InvocationTargetException e)
		{
			e.getTargetException().printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}

	}
}
