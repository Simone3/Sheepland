package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;
import it.polimi.graziussi_ouafo.view.GameWindowRemoteInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectActionBuyTerrainTile extends SelectActionStrategy
{
	private static final long serialVersionUID = 1L;

	@Override
	boolean canShowAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		PlayerTurn playerTurn = gameState.getCurrentTurn();
		String previousAction = playerTurn.getPreviousAction();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Cannot buy tiles if previous action is the same
		if (ActionBuyTerrainTile.class.getSimpleName().equals(previousAction))
		{
			return false;
		}

		// Player must move shepherd at least once during the turn
		if (playerTurn.getAvailableActions() == 1 && !playerTurn.hasMovedShepherd())
		{
			return false;
		}

		// Get shepherd regions: player can buy only from the decks with the regions' types
		Road shepherdRoad = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = shepherdRoad.getRegions();
		List<TerrainTileDeck> deckList = new ArrayList<TerrainTileDeck>();
		if (Arrays.asList(RegionType.getDeckTypes()).contains(regions[0].getType()))
		{
			deckList.add(gameState.getDeck(regions[0].getType()));
		}
		if (Arrays.asList(RegionType.getDeckTypes()).contains(regions[1].getType()))
		{
			deckList.add(gameState.getDeck(regions[1].getType()));
		}
		TerrainTileDeck[] decks = deckList.toArray(new TerrainTileDeck[deckList.size()]);

		// To buy a terrain tile, there must be at least one available deck (max price not reached) the player can buy (doesn't cost too much)
		int dinars = currentPlayer.getDinars();
		return ( (decks[0].isAvailable() && dinars >= decks[0].getCurrentCardPrice()) || (decks[1].isAvailable() && dinars >= decks[1].getCurrentCardPrice()) );
	}

	@Override
	void showAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Get client's window
		GameWindowRemoteInterface gameWindow = turnScheduler.getCurrentClient();

		// Get available deck types based on current shepherd position
		int dinars = currentPlayer.getDinars();
		Road shepherdRoad = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = shepherdRoad.getRegions();
		List<TerrainTileDeck> deckList = new ArrayList<TerrainTileDeck>();
		if (Arrays.asList(RegionType.getDeckTypes()).contains(regions[0].getType()))
		{
			deckList.add(gameState.getDeck(regions[0].getType()));
		}
		if (Arrays.asList(RegionType.getDeckTypes()).contains(regions[1].getType()))
		{
			deckList.add(gameState.getDeck(regions[1].getType()));
		}
		List<RegionType> types = new ArrayList<RegionType>();
		for (TerrainTileDeck deck : deckList)
		{
			if (deck.isAvailable() && dinars >= deck.getCurrentCardPrice())
			{
				types.add(deck.getType());
			}
		}

		// Activate the buttons of available decks
		gameWindow.activateAvailableDecks(types, new ActionBuyTerrainTile());
	}
}
