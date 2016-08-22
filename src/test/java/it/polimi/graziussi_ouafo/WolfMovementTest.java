package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.TurnScheduler;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Shepherd;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WolfMovementTest
{
	TurnScheduler turnScheduler;
	GameState gameState;

	List<Integer> regionIDsAroundSheepsburg = Arrays.asList(new Integer[] { 3, 6, 9, 12, 15, 18 });
	Integer[] roadIDsAroundSheepsburg = new Integer[] { 39, 38, 36, 34, 33, 41 };

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

		// Get game state
		Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
		method.setAccessible(true);
		this.gameState = (GameState) method.invoke(this.turnScheduler);
	}

	@Test
	public void emptyRoads()
	{
		try
		{
			Method method;

			// Check starting position
			Region wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));

			// Call wolf method
			method = TurnScheduler.class.getDeclaredMethod("manageWolfRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			wolfRegion = this.gameState.getWolfRegion();
			System.out.println("EMPTY ROADS: wolf moved at " + wolfRegion.getId());
			Assert.assertTrue(this.regionIDsAroundSheepsburg.contains(wolfRegion.getId()));
			Assert.assertEquals(wolfRegion.getWhiteSheepNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}

	@Test
	public void allFences()
	{
		try
		{
			Method method;

			// Check starting position
			Region wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put fences in all surrounding roads
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				this.gameState.getRoad(ID).placeFence();
			}

			// Call wolf method
			method = TurnScheduler.class.getDeclaredMethod("manageWolfRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			wolfRegion = this.gameState.getWolfRegion();
			System.out.println("ALL FENCES: wolf moved at " + wolfRegion.getId());
			Assert.assertTrue(this.regionIDsAroundSheepsburg.contains(wolfRegion.getId()));
			Assert.assertEquals(wolfRegion.getWhiteSheepNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}

	@Test
	public void allShepherds()
	{
		try
		{
			Method method;

			// Check starting position
			Region wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put shepherds in all surrounding roads
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				this.gameState.getRoad(ID).placeShepherd(new Shepherd(this.gameState.getPlayers()[0], 0));
			}

			// Call wolf method
			method = TurnScheduler.class.getDeclaredMethod("manageWolfRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}

	@Test
	public void oneFenceOtherShepherd()
	{
		try
		{
			Method method;

			// Check starting position
			Region wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put shepherds or fences in all surrounding roads
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				if (ID.equals(39))
				{
					this.gameState.getRoad(ID).placeFence();
				}
				else
				{
					this.gameState.getRoad(ID).placeShepherd(new Shepherd(this.gameState.getPlayers()[0], 0));
				}
			}

			// Call wolf method
			method = TurnScheduler.class.getDeclaredMethod("manageWolfRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			wolfRegion = this.gameState.getWolfRegion();
			System.out.println("ONE FENCE, OTHER SHEPHERDS: wolf moved at " + wolfRegion.getId());
			Assert.assertEquals(wolfRegion.getId(), new Integer(3));
			Assert.assertEquals(wolfRegion.getWhiteSheepNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}

	@Test
	public void mixed()
	{
		try
		{
			Method method;

			// Check starting position
			Region wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put some obstacles
			List<Integer> roadsWithFence = Arrays.asList(new Integer[] { 39, 34 });
			List<Integer> roadsWithShepherd = Arrays.asList(new Integer[] { 41 });

			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				if (roadsWithFence.contains(ID))
				{
					this.gameState.getRoad(ID).placeFence();
				}
				else if (roadsWithShepherd.contains(ID))
				{
					this.gameState.getRoad(ID).placeShepherd(new Shepherd(this.gameState.getPlayers()[0], 0));
				}
			}

			// Call wolf method
			method = TurnScheduler.class.getDeclaredMethod("manageWolfRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			wolfRegion = this.gameState.getWolfRegion();
			System.out.println("MIXED: wolf moved at " + wolfRegion.getId());
			Assert.assertTrue(Arrays.asList(new Integer[] { 6, 9, 15 }).contains(wolfRegion.getId()));
			Assert.assertEquals(wolfRegion.getWhiteSheepNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}

	@Test
	public void oneFreeOtherFences()
	{
		try
		{
			Method method;

			// Check starting position
			Region wolfRegion = this.gameState.getWolfRegion();
			Assert.assertTrue(wolfRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put some obstacles
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				if(!ID.equals(Integer.valueOf(41))) this.gameState.getRoad(ID).placeFence();
			}

			// Call wolf method
			method = TurnScheduler.class.getDeclaredMethod("manageWolfRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			wolfRegion = this.gameState.getWolfRegion();
			System.out.println("ONE FREE, OTHER FENCES: wolf moved at " + wolfRegion.getId());
			Assert.assertEquals(Integer.valueOf(18), wolfRegion.getId());
			Assert.assertEquals(wolfRegion.getWhiteSheepNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() - 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}
}
