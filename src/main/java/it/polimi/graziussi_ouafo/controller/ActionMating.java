package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.SheepEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;

import java.rmi.RemoteException;

public class ActionMating extends ActionStrategy
{

	private static final long serialVersionUID = 1L;

	@Override
	public boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{

		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		PlayerTurn playerTurn = gameState.getCurrentTurn();
		String previousAction = playerTurn.getPreviousAction();

		// Cannot mate if previous action is the same
		if (ActionMating.class.getSimpleName().equals(previousAction))
		{
			return false;
		}

		// Player must move shepherd at least once during the turn
		if (playerTurn.getAvailableActions() == 1 && !playerTurn.hasMovedShepherd())
		{
			return false;
		}

		// Action info
		Road shepherdRoad = gameState.getShepherdPosition(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getChosenShepherdForTurn());
		Region[] regions = shepherdRoad.getRegions();

		// Event info
		Integer fromRegionID = ((SheepEvent) event).getFromRegionID();
		Region region = null;

		// Check that the "from" region is actually close to the current shepherd road
		if (regions[0].getId().equals(fromRegionID))
		{
			region = regions[0];
		}
		else if (regions[1].getId().equals(fromRegionID))
		{
			region = regions[1];
		}
		else
		{
			return false;
		}

		int sheepNumber = region.getWhiteSheepNumber();
		int ramNumber = region.getRamNumber();

		return (sheepNumber > 0 && ramNumber > 0);
	}

	@Override
	void performAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();

		// Action info
		Integer fromRegionID = ((SheepEvent) event).getFromRegionID();
		Region region = null;
		Road shepherdRoad = gameState.getShepherdPosition(gameState.getCurrentPlayer(), gameState.getCurrentPlayer().getChosenShepherdForTurn());
		Region[] regions = shepherdRoad.getRegions();

		// Get "from" region
		if (regions[0].getId().equals(fromRegionID))
		{
			region = regions[0];
		}
		else if (regions[1].getId().equals(fromRegionID))
		{
			region = regions[1];
		}
		else
		{
			return;
		}

		int dieNumber = turnScheduler.throwDie();

		if (dieNumber == shepherdRoad.getNumber())
		{

			region.placeLamb();
			turnScheduler.updateGUIWhiteSheep(region);
		}
		else
		{
			turnScheduler.showTimedMessageToCurrentPlayer("It didn't work!");
		}
	}
}
