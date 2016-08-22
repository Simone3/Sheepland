package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.TurnScheduler;
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

public class BlackSheepMovementTest
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
			Region blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));

			// Call black sheep method
			method = TurnScheduler.class.getDeclaredMethod("manageBlackSheepRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			blackSheepRegion = this.gameState.getBlackSheepRegion();
			System.out.println("EMPTY ROADS: black sheep moved at " + blackSheepRegion.getId());
			Assert.assertTrue(this.regionIDsAroundSheepsburg.contains(blackSheepRegion.getId()));
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
			Region blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put fences in all surrounding roads
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				this.gameState.getRoad(ID).placeFence();
			}

			// Call black sheep method
			method = TurnScheduler.class.getDeclaredMethod("manageBlackSheepRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			blackSheepRegion = this.gameState.getBlackSheepRegion();
			System.out.println("ALL FENCES: black sheep moved at " + blackSheepRegion.getId());
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));
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
			Region blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put shepherds in all surrounding roads
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				this.gameState.getRoad(ID).placeShepherd(new Shepherd(this.gameState.getPlayers()[0], 0));
			}

			// Call black sheep method
			method = TurnScheduler.class.getDeclaredMethod("manageBlackSheepRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));
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
			Region blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));

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

			// Call black sheep method
			method = TurnScheduler.class.getDeclaredMethod("manageBlackSheepRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			blackSheepRegion = this.gameState.getBlackSheepRegion();
			System.out.println("ONE FENCE, OTHER SHEPHERDS: black sheep moved at " + blackSheepRegion.getId());
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));
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
			Region blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));

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

			// Call black sheep method
			method = TurnScheduler.class.getDeclaredMethod("manageBlackSheepRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			blackSheepRegion = this.gameState.getBlackSheepRegion();
			System.out.println("MIXED: black sheep moved at " + blackSheepRegion.getId());
			Assert.assertTrue(Arrays.asList(new Integer[] { 0, 6, 9, 15 }).contains(blackSheepRegion.getId()));
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
			Region blackSheepRegion = this.gameState.getBlackSheepRegion();
			Assert.assertTrue(blackSheepRegion.getType().equals(RegionType.SHEEPSBURG));

			// Put some obstacles
			for (Integer ID : this.roadIDsAroundSheepsburg)
			{
				if(!ID.equals(Integer.valueOf(41))) this.gameState.getRoad(ID).placeFence();
			}

			// Call black sheep method
			method = TurnScheduler.class.getDeclaredMethod("manageBlackSheepRandomMovement");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			// Check region
			blackSheepRegion = this.gameState.getBlackSheepRegion();
			System.out.println("ONE FREE, OTHER FENCES: black sheep moved at " + blackSheepRegion.getId());
			Assert.assertTrue(Arrays.asList(new Integer[] { 0, 18 }).contains(blackSheepRegion.getId()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("exception");
		}
	}
}
