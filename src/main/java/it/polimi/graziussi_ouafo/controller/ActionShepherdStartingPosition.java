package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.RoadEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.model.Shepherd;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

public class ActionShepherdStartingPosition extends ActionStrategy
{
	private static final long serialVersionUID = 1L;

	public ActionShepherdStartingPosition()
	{
		// This action doesn't decrease the action count
		this.doesNotDecreaseActionCount();
	}

	@Override
	public boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{
		// Model info
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Event info
		Integer roadToID = ((RoadEvent) event).getRoadID();

		// Action info
		Road roadTo = gameState.getRoad(roadToID);
		int shepherdNumber = turnScheduler.getCurrentToPlaceShepherdNumber();
		int sheperdsPerPlayer = turnScheduler.getShepherdsPerPlayerNumber();

		// Check that we are at the game beginning
		if (!turnScheduler.areAllShepherdsPlaced())
		{
			// Check if valid shepherd number
			if (shepherdNumber >= sheperdsPerPlayer)
			{
				return false;
			}
			else
			{
				// Check that there isn't already a shepherd with that number
				if (gameState.isShepherdPlaced(currentPlayer, shepherdNumber))
				{
					return false;
				}
				else
				{
					// Check that road is empty
					return roadTo.isEmpty();
				}
			}
		}
		else
		{
			return false;
		}
	}

	@Override
	void performAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, InvocationTargetException, InterruptedException, RemoteException
	{
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Event info
		Integer roadToID = ((RoadEvent) event).getRoadID();
		int shepherdNumber = turnScheduler.getCurrentToPlaceShepherdNumber();

		// Action info
		Road roadTo = gameState.getRoad(roadToID);

		// Place new shepherd in the road
		roadTo.placeShepherd(new Shepherd(currentPlayer, shepherdNumber));

		// GUI: update the road
		turnScheduler.updateGUIRoad(roadTo);

		// Check what to do next
		shepherdNumber++;
		int sheperdsPerPlayer = turnScheduler.getShepherdsPerPlayerNumber();

		// Deactivate action components
		turnScheduler.getCurrentClient().deactivateAllActionComponents();

		// If there are other shepherds of the same player to be placed, allow user to do so
		if (shepherdNumber < sheperdsPerPlayer)
		{
			turnScheduler.increaseCurrentToPlaceShepherdNumber();
			turnScheduler.allowPlayerToChooseShepherdInitialPositon();
		}
		else
		{
			Player[] players = gameState.getPlayers();

			// If last player, set all shepherds as placed
			if (currentPlayer.equals(players[players.length - 1]))
			{
				turnScheduler.setAllShepherdsPlaced();
			}

			// Start new turn (method will decide if a "real" or a "fake" one with allShepherdsPlaced value)
			turnScheduler.startNewTurn();
		}
	}
}