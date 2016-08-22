package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.view.GameMapRemoteInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class SelectActionMating extends SelectActionStrategy
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
		Road shepherdRoad = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = shepherdRoad.getRegions();

		// Check if at least in a region there is at least one sheep and a ram
		return ((regions[0].getWhiteSheepNumber() > 0 && regions[0].getRamNumber() > 0) || (regions[1].getWhiteSheepNumber() > 0 && regions[1].getRamNumber() > 0));
	}

	@Override
	void showAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException
	{
		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		Player currentPlayer = gameState.getCurrentPlayer();

		// Action info
		Road roadBetween = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = roadBetween.getRegions();

		// Get client's map
		GameMapRemoteInterface gameMap = turnScheduler.getCurrentClient().getGameMap();

		// Activate white sheep panels of surrounding regions
		List<Integer> whiteSheepRegions = new ArrayList<Integer>();
		if (regions[0].getWhiteSheepNumber() > 0 && regions[0].getRamNumber() > 0)
		{
			whiteSheepRegions.add(Integer.valueOf(regions[0].getId()));
		}
		if (regions[1].getWhiteSheepNumber() > 0 && regions[1].getRamNumber() > 0)
		{
			whiteSheepRegions.add(Integer.valueOf(regions[1].getId()));
		}
		gameMap.activateWhiteSheep(whiteSheepRegions, new ActionMating());
	}
}
