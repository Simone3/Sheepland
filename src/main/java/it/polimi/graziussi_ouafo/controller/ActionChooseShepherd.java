package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.ChooseShepherdEvent;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerTurn;

import java.rmi.RemoteException;

public class ActionChooseShepherd extends ActionStrategy
{
	private static final long serialVersionUID = 1L;

	public ActionChooseShepherd()
	{
		// This action doesn't decrease the action count
		this.doesNotDecreaseActionCount();
	}

	@Override
	boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler)
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		PlayerTurn playerTurn = gameState.getCurrentTurn();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Check that we are at the turn beginning
		if (!currentPlayer.hasChosenShepherdForTurn() && playerTurn.getAvailableActions() == MiscConstants.ACTIONS_PER_TURN.getValue())
		{
			// Event info
			Integer shepherdNumber = ((ChooseShepherdEvent) event).getShepherdNumber();

			// Check that it's a valid shepherd number
			return (shepherdNumber.intValue() < turnScheduler.getShepherdsPerPlayerNumber());
		}
		else
		{
			return false;
		}
	}

	@Override
	void performAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws RemoteException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Event info
		Integer shepherdNumber = ((ChooseShepherdEvent) event).getShepherdNumber();

		// Set the chosen shepherd index
		currentPlayer.setChosenShepherdForTurn(shepherdNumber.intValue());

		// Allow user to start the turn
		turnScheduler.allowUserToChooseAction();
	}
}
