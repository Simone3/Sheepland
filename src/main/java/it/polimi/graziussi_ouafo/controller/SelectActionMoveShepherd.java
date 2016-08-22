package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.view.GameMapRemoteInterface;

import java.rmi.RemoteException;
import java.util.List;

public class SelectActionMoveShepherd extends SelectActionStrategy
{
	private static final long serialVersionUID = 1L;

	@Override
	boolean canShowAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Get current shepherd location
		Road shepherdRoad = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());

		// If there are adjacent roads, action allowed since player doesn't have to pay
		if (!gameState.getAdjacentRoads(shepherdRoad).isEmpty())
		{
			return true;
		}
		else
		{
			// If no adjacent roads, need to check if there are other roads and if player has enough money to travel
			return (!gameState.getAllEmptyRoadIDs().isEmpty() && currentPlayer.getDinars() >= MiscConstants.SHEPHERD_LONG_JOURNEY_PRICE.getValue());
		}
	}

	@Override
	void showAction(SelectActionEvent event, TurnScheduler turnScheduler) throws RemoteException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();

		// Get client's map
		GameMapRemoteInterface gameMap = turnScheduler.getCurrentClient().getGameMap();

		// Activate all available roads
		List<Integer> emptyRoads = gameState.getAllEmptyRoadIDs();
		gameMap.activateRoads(emptyRoads, new ActionMoveShepherd());
	}
}
