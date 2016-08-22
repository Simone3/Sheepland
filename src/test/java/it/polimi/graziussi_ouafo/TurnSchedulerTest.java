package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.TurnScheduler;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TurnSchedulerTest
{
	GameState gameState;
	TurnScheduler turnScheduler;

	@Before
	public void createGameStateAndTurnScheduler() throws IOException, IllegalArgumentException, GameErrorException, ParserConfigurationException, SAXException, NotBoundException, AlreadyBoundException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException
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
	}
	
	

	@Test
	public void CheckEndOfGameTest()
	{
		try
		{
			// Get private method via reflection
			Method method = TurnScheduler.class.getDeclaredMethod("isGameOver");
			method.setAccessible(true);
			boolean output;

			// Game start
			output = (boolean) method.invoke(this.turnScheduler);
			Assert.assertFalse("Game start check", output);

			// Sub some fences
			System.out.println("Fences are " + this.gameState.getAvailableNormalFences());
			for (int i = 0; i < MiscConstants.STARTING_NORMAL_FENCES.getValue() - 1; i++)
			{
				this.gameState.subAvailableNormalFences();
			}

			// Check (!fences and player)
			output = (boolean) method.invoke(this.turnScheduler);
			System.out.println("Fences are " + this.gameState.getAvailableNormalFences());
			Assert.assertFalse("Game start check", output);

			// Sub last fence
			this.gameState.subAvailableNormalFences();

			// Check (fences and player)
			output = (boolean) method.invoke(this.turnScheduler);
			System.out.println("Fences are " + this.gameState.getAvailableNormalFences());
			Assert.assertTrue("Check", output);

			// Change player
			this.gameState.setCurrentPlayer(this.gameState.getNextPlayer());

			// Check (fences and !player)
			output = (boolean) method.invoke(this.turnScheduler);
			System.out.println("Fences are " + this.gameState.getAvailableNormalFences());
			Assert.assertFalse("Check", output);

			// Change current player
			this.gameState.setCurrentPlayer(this.gameState.getNextPlayer());
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}

}
