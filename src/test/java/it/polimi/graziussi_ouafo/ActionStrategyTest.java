package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.ActionBuyTerrainTile;
import it.polimi.graziussi_ouafo.controller.ActionMating;
import it.polimi.graziussi_ouafo.controller.ActionMoveSheep;
import it.polimi.graziussi_ouafo.controller.ActionMoveShepherd;
import it.polimi.graziussi_ouafo.controller.ActionShooting;
import it.polimi.graziussi_ouafo.controller.TurnScheduler;
import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.DeckEvent;
import it.polimi.graziussi_ouafo.events.RoadEvent;
import it.polimi.graziussi_ouafo.events.SheepEvent;
import it.polimi.graziussi_ouafo.main.ActionNotAllowedException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
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

public class ActionStrategyTest
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
			RegionType type = RegionType.PASTURE;
			AbstractActionEvent event = new DeckEvent(type, new ActionBuyTerrainTile());
			TerrainTileDeck deck = this.gameState.getDeck(type);
			PlayerTurn turn = this.gameState.getCurrentTurn();

			int dinarsBefore = this.gameState.getCurrentPlayer().getDinars();
			deck.increaseCurrentCardPrice();
			int priceBefore = deck.getCurrentCardPrice();

			Assert.assertEquals(deck.getCurrentCardPrice(), MiscConstants.STARTING_TERRAIN_TILE_PRICE.getValue() + 1);
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue());
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "");

			Method method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(deck.getCurrentCardPrice(), priceBefore + 1);
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue() - 1);
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "ActionBuyTerrainTile");
			Assert.assertEquals(this.gameState.getCurrentPlayer().getDinars(), dinarsBefore - 1);

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
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
			Road roadBetween = this.gameState.getRoad(new Integer(0));
			Region[] regions = roadBetween.getRegions();
			AbstractActionEvent event = new SheepEvent(new Integer(regions[0].getId()), false, new ActionMoveSheep());
			PlayerTurn turn = this.gameState.getCurrentTurn();

			Assert.assertEquals(regions[0].getWhiteSheepNumber() + regions[0].getRamNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue());
			Assert.assertEquals(regions[1].getWhiteSheepNumber() + regions[1].getRamNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue());
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue());
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "");

			Method method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(regions[0].getWhiteSheepNumber() + regions[0].getRamNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() - 1);
			Assert.assertEquals(regions[1].getWhiteSheepNumber() + regions[1].getRamNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue() + 1);
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue() - 1);
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "ActionMoveSheep");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
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
			int roadToID = 12;
			AbstractActionEvent event = new RoadEvent(new Integer(this.gameState.getRoad(roadToID).getId()), new ActionMoveShepherd());
			PlayerTurn turn = this.gameState.getCurrentTurn();
			Player player = this.gameState.getCurrentPlayer();
			Road previousRoad = this.gameState.getShepherdPosition(player, 0);

			player.subDinars(MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue() - MiscConstants.SHEPHERD_LONG_JOURNEY_PRICE.getValue());

			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue());
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "");

			Method method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			Assert.assertEquals(this.gameState.getRoad(new Integer(roadToID)), this.gameState.getShepherdPosition(player, 0));
			Assert.assertEquals(previousRoad.getShepherd(), null);
			Assert.assertEquals(player.getDinars(), 0);
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue() - 1);
			Assert.assertEquals(turn.hasMovedShepherd(), true);
			Assert.assertEquals(turn.getPreviousAction(), "ActionMoveShepherd");

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
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
	public void mating()
	{
		try
		{
			Road roadBetween = this.gameState.getRoad(new Integer(0));
			Region[] regions = roadBetween.getRegions();
			AbstractActionEvent event = new SheepEvent(new Integer(regions[0].getId()), false, new ActionMating());
			PlayerTurn turn = this.gameState.getCurrentTurn();

			if (regions[0].getWhiteSheepNumber() > 0)
			{
				regions[0].placeRam();
			}
			else if (regions[0].getRamNumber() > 0)
			{
				regions[0].placeWhiteSheep();
			}

			Assert.assertEquals(regions[0].getWhiteSheepNumber(), 1);
			Assert.assertEquals(regions[0].getRamNumber(), 1);
			Assert.assertEquals(regions[0].getLambNumber(), 0);
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue());
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "");

			Method method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			if (regions[0].getLambNumber() > 0)
			{
				System.out.println("MATING successful");

				Assert.assertEquals(regions[0].getWhiteSheepNumber(), 1);
				Assert.assertEquals(regions[0].getRamNumber(), 1);
				Assert.assertEquals(regions[0].getLambNumber(), 1);
				Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue() - 1);
				Assert.assertEquals(turn.hasMovedShepherd(), false);
				Assert.assertEquals(turn.getPreviousAction(), "ActionMating");

				method = TurnScheduler.class.getDeclaredMethod("growUpLambs");
				method.setAccessible(true);
				method.invoke(this.turnScheduler);

				Assert.assertTrue((regions[0].getWhiteSheepNumber() == 2 && regions[0].getRamNumber() == 1) || (regions[0].getWhiteSheepNumber() == 1 && regions[0].getRamNumber() == 2));
				Assert.assertEquals(regions[0].getLambNumber(), 0);
			}
			else
			{
				System.out.println("MATING was NOT successful");
			}

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
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
			AbstractActionEvent event = new SheepEvent(new Integer(regions[0].getId()), false, new ActionShooting());
			PlayerTurn turn = this.gameState.getCurrentTurn();

			int previousNumber = regions[0].getWhiteSheepNumber() + regions[0].getRamNumber();
			Assert.assertEquals(previousNumber, 1);
			Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue());
			Assert.assertEquals(turn.hasMovedShepherd(), false);
			Assert.assertEquals(turn.getPreviousAction(), "");

			Assert.assertEquals(this.gameState.getCurrentPlayer().getDinars(), MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue());
			this.gameState.getCurrentPlayer().subDinars(MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue() - 2 * MiscConstants.SHEPHERD_SILENCE_PRICE.getValue() + 1);
			Assert.assertEquals(this.gameState.getCurrentPlayer().getDinars(), 2 * MiscConstants.SHEPHERD_SILENCE_PRICE.getValue() - 1);
			try
			{
				Method method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
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
			this.gameState.getCurrentPlayer().addDinars(1000);

			Method method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
			method.setAccessible(true);
			method.invoke(this.turnScheduler, event);

			int currentNumber = regions[0].getWhiteSheepNumber() + regions[0].getRamNumber();
			if (currentNumber != previousNumber)
			{
				System.out.println("SHOOTING successful");

				Assert.assertEquals(currentNumber, previousNumber - 1);
				Assert.assertEquals(turn.getAvailableActions(), MiscConstants.ACTIONS_PER_TURN.getValue() - 1);
				Assert.assertEquals(turn.hasMovedShepherd(), false);
				Assert.assertEquals(turn.getPreviousAction(), "ActionShooting");
			}
			else
			{
				System.out.println("SHOOTING was NOT successful");
			}

			try
			{
				method = TurnScheduler.class.getDeclaredMethod("manageActionEvent", AbstractActionEvent.class);
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
