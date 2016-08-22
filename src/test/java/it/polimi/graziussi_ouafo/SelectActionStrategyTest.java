package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.SelectActionBuyTerrainTile;
import it.polimi.graziussi_ouafo.controller.SelectActionMating;
import it.polimi.graziussi_ouafo.controller.SelectActionMoveSheep;
import it.polimi.graziussi_ouafo.controller.SelectActionMoveShepherd;
import it.polimi.graziussi_ouafo.controller.SelectActionShooting;
import it.polimi.graziussi_ouafo.controller.TurnScheduler;
import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.ActionNotAllowedException;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.model.Shepherd;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectActionStrategyTest
{
	GameState gameState;
	TurnScheduler turnScheduler;

	@Before
	public void createModelAndController()
	{
		try
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

			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			this.gameState = (GameState) method.invoke(this.turnScheduler);

			this.gameState.setCurrentPlayer(this.gameState.getPlayers()[0]);

			this.gameState.getRoad(0).placeShepherd(new Shepherd(this.gameState.getPlayers()[0], 0));
			this.gameState.getRoad(1).placeShepherd(new Shepherd(this.gameState.getPlayers()[1], 0));
			this.gameState.getRoad(2).placeShepherd(new Shepherd(this.gameState.getPlayers()[2], 0));

			method = TurnScheduler.class.getDeclaredMethod("setAllShepherdsPlaced");
			method.setAccessible(true);
			method.invoke(this.turnScheduler);

			this.gameState.setCurrentTurn(new PlayerTurn());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("Exception");
		}
	}

	@After
	public void deleteModelAndController()
	{
		this.gameState = null;
		this.turnScheduler = null;
	}

	@Test
	public void buyTerrainTile()
	{
		try
		{
			SelectActionEvent event = new SelectActionEvent(new SelectActionBuyTerrainTile());

			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			GameState previousGameState = (GameState) method.invoke(this.turnScheduler);

			method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(previousGameState, this.gameState);

			this.gameState.getCurrentTurn().setPreviousAction("ActionBuyTerrainTile");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}

			this.gameState.getCurrentTurn().setPreviousAction("");

			this.gameState.getCurrentPlayer().subDinars(this.gameState.getCurrentPlayer().getDinars());

			for (RegionType type : RegionType.getDeckTypes())
			{
				this.gameState.getDeck(type).increaseCurrentCardPrice();
			}

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("Exception");
		}
	}

	@Test
	public void moveSheep()
	{
		try
		{
			SelectActionEvent event = new SelectActionEvent(new SelectActionMoveSheep());

			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			GameState previousGameState = (GameState) method.invoke(this.turnScheduler);

			method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(previousGameState, this.gameState);

			this.gameState.getCurrentTurn().setPreviousAction("ActionMoveSheep");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}

			this.gameState.getCurrentTurn().setPreviousAction("");

			Road roadBetween = this.gameState.getRoad(new Integer(0));
			Region[] regions = roadBetween.getRegions();

			if (regions[0].getWhiteSheepNumber() > 0)
			{
				regions[0].removeWhiteSheep();
			}
			else if (regions[0].getRamNumber() > 0)
			{
				regions[0].removeRam();
			}
			if (regions[1].getWhiteSheepNumber() > 0)
			{
				regions[1].removeWhiteSheep();
			}
			else if (regions[1].getRamNumber() > 0)
			{
				regions[1].removeRam();
			}

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("Exception");
		}
	}

	@Test
	public void moveShepherd()
	{
		try
		{
			SelectActionEvent event = new SelectActionEvent(new SelectActionMoveShepherd());

			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			GameState previousGameState = (GameState) method.invoke(this.turnScheduler);

			method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(previousGameState, this.gameState);

			this.gameState.getCurrentTurn().setPreviousAction("ActionMoveShepherd");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.assertTrue(true);
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.fail();
				}
				else
				{
					Assert.fail("Exception");
				}
			}

			this.gameState.getRoad(1).placeFence();
			this.gameState.getRoad(26).placeFence();
			this.gameState.getCurrentPlayer().subDinars(this.gameState.getCurrentPlayer().getDinars());

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.assertTrue(true);
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.fail();
				}
				else
				{
					Assert.fail("Exception");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("Exception");
		}
	}

	@Test
	public void mating()
	{
		try
		{
			Road roadBetween = this.gameState.getRoad(new Integer(0));
			Region[] regions = roadBetween.getRegions();

			if (regions[0].getWhiteSheepNumber() > 0)
			{
				regions[0].placeRam();
			}
			else if (regions[0].getRamNumber() > 0)
			{
				regions[0].placeWhiteSheep();
			}

			SelectActionEvent event = new SelectActionEvent(new SelectActionMating());

			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			GameState previousGameState = (GameState) method.invoke(this.turnScheduler);

			method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(previousGameState, this.gameState);

			this.gameState.getCurrentTurn().setPreviousAction("ActionMating");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}

			if (regions[0].getWhiteSheepNumber() > 0)
			{
				regions[0].removeWhiteSheep();
			}
			else if (regions[0].getRamNumber() > 0)
			{
				regions[0].removeRam();
			}
			if (regions[1].getWhiteSheepNumber() > 0)
			{
				regions[1].removeWhiteSheep();
			}
			else if (regions[1].getRamNumber() > 0)
			{
				regions[1].removeRam();
			}

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("Exception");
		}

	}

	@Test
	public void shooting()
	{
		try
		{
			Road roadBetween = this.gameState.getRoad(new Integer(0));
			Region[] regions = roadBetween.getRegions();

			SelectActionEvent event = new SelectActionEvent(new SelectActionShooting());

			Method method = TurnScheduler.class.getDeclaredMethod("getGameState");
			method.setAccessible(true);
			GameState previousGameState = (GameState) method.invoke(this.turnScheduler);

			method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(previousGameState, this.gameState);

			this.gameState.getCurrentTurn().setPreviousAction("ActionShooting");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}

			if (regions[0].getWhiteSheepNumber() > 0)
			{
				regions[0].removeWhiteSheep();
			}
			else if (regions[0].getRamNumber() > 0)
			{
				regions[0].removeRam();
			}
			if (regions[1].getWhiteSheepNumber() > 0)
			{
				regions[1].removeWhiteSheep();
			}
			else if (regions[1].getRamNumber() > 0)
			{
				regions[1].removeRam();
			}

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageSelectActionEvent", SelectActionEvent.class);
				method.setAccessible(true);
				method.invoke(this.turnScheduler, event);
				Assert.fail();
			}
			catch (InvocationTargetException e)
			{
				if (e.getCause() instanceof ActionNotAllowedException)
				{
					Assert.assertTrue(true);
				}
				else
				{
					Assert.fail("Exception");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail("Exception");
		}
	}
}
