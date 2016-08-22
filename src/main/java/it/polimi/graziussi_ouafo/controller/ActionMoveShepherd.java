package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.RoadEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.Road;

import java.rmi.RemoteException;

public class ActionMoveShepherd extends ActionStrategy
{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Event info
		Integer roadToID = ((RoadEvent) event).getRoadID();

		// Action info
		Road roadTo = gameState.getRoad(roadToID);
		Road roadFrom = gameState.getShepherdPosition(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getChosenShepherdForTurn());

		// Check that the two roads are not the same
		if (roadTo.equals(roadFrom))
		{
			return false;
		}
		
		// Check if road has fence
		else if (roadTo.hasFence())
		{
			return false;
		}
		
		// Check if road has shepherd
		else if (roadTo.hasShepherd())
		{
			return false;
		}
		
		// Check if adjacent
		else if (gameState.getAdjacentRoads(roadFrom).contains(roadTo))
		{
			return true;
		}
		else
		{
			// Check if enough money
			return (currentPlayer.getDinars() >= MiscConstants.SHEPHERD_LONG_JOURNEY_PRICE.getValue());
		}
	}

	@Override
	void performAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException
	{
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Event info
		Integer roadToID = ((RoadEvent) event).getRoadID();

		// Action info
		Road roadTo = gameState.getRoad(roadToID);
		Road roadFrom = gameState.getShepherdPosition(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getChosenShepherdForTurn());

		// Place shepherd in "to" road
		roadTo.placeShepherd(roadFrom.getShepherd());

		// remove shepherd from "from" road
		roadFrom.removeShepherd();

		// Place fence in "from" region
		gameState.subAvailableNormalFences();
		roadFrom.placeFence();

		// If road not adjacent, sub dinars
		if (!gameState.getAdjacentRoads(roadFrom).contains(roadTo))
		{
			currentPlayer.subDinars(MiscConstants.SHEPHERD_LONG_JOURNEY_PRICE.getValue());
		}

		// Tell turn scheduler that the player has moved the shepherd at least once in this turn
		turnScheduler.getGameState().getCurrentTurn().setHasMovedShepherd();

		// GUI: update the two roads
		turnScheduler.updateGUIRoad(roadFrom);
		turnScheduler.updateGUIRoad(roadTo);
	}

}