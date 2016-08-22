package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.DeckEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.rmi.RemoteException;

public class ActionBuyTerrainTile extends ActionStrategy
{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
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

		// Event info
		RegionType deckType = ((DeckEvent) event).getDeckType();

		// Action info
		TerrainTileDeck deck = gameState.getDeck(deckType);

		// Check that deck isn't empty
		if (deck.getCurrentCardPrice() > MiscConstants.MAX_TERRAIN_TILE_PRICE.getValue())
		{
			return false;
		}

		// Check if player has enough money
		if (deck.getCurrentCardPrice() > currentPlayer.getDinars())
		{
			return false;
		}

		// Check that deck type is the same as (at least) one of the shepherd's regions
		Road shepherdRoad = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = shepherdRoad.getRegions();
		if (!regions[0].getType().equals(deckType) && !regions[1].getType().equals(deckType))
		{
			return false;
		}

		// If all previous checks are OK, action allowed
		return true;
	}

	@Override
	void performAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Event info
		RegionType deckType = ((DeckEvent) event).getDeckType();

		// Action info
		TerrainTileDeck deck = gameState.getDeck(deckType);

		// Add the card in the current player's "inventory"
		currentPlayer.addBoughtTerrainTile(deck.getType());

		// Remove player's dinars
		currentPlayer.subDinars(deck.getCurrentCardPrice());

		// Increase deck price
		deck.increaseCurrentCardPrice();

		// GUI: update the deck button and the game info (bought cards list)
		turnScheduler.updateGUIDeckButton(deck);
		turnScheduler.updateGUIGameInfo();
	}
}
