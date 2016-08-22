package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.SheepEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;

import java.rmi.RemoteException;
import java.util.Random;

public class ActionMoveSheep extends ActionStrategy
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

		// Cannot move sheep if previous action is the same
		if (ActionMoveSheep.class.getSimpleName().equals(previousAction))
		{
			return false;
		}

		// Player must move shepherd at least once during the turn
		if (playerTurn.getAvailableActions() == 1 && !playerTurn.hasMovedShepherd())
		{
			return false;
		}

		// Action info
		Road roadBetween = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = roadBetween.getRegions();

		// Different behavior for black and white sheep
		if (((SheepEvent) event).isBlackSheepMovement())
		{
			// Get black sheep position
			Region from = gameState.getBlackSheepRegion();

			// Check that the black sheep region is actually close to the current shepherd road
			if (!regions[0].equals(from) && !regions[1].equals(from))
			{
				return false;
			}

			// Check that there actually is a black sheep in the "from" region
			if (!from.hasBlackSheep())
			{
				return false;
			}
		}
		else
		{
			// Event info
			Integer fromRegionID = ((SheepEvent) event).getFromRegionID();
			Region from = null;

			// Check that the "from" region is actually close to the current shepherd road
			if (regions[0].getId().equals(fromRegionID))
			{
				from = regions[0];
			}
			else if (regions[1].getId().equals(fromRegionID))
			{
				from = regions[1];
			}
			else
			{
				return false;
			}

			// Check that there actually is a sheep in the "from" region
			if (from.getWhiteSheepNumber() < 1 && from.getRamNumber() < 1)
			{
				return false;
			}
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

		// Action info
		Road roadBetween = gameState.getShepherdPosition(currentPlayer, currentPlayer.getChosenShepherdForTurn());
		Region[] regions = roadBetween.getRegions();

		// Different behavior for black and white sheep
		if (((SheepEvent) event).isBlackSheepMovement())
		{
			// Action info
			Region from = gameState.getBlackSheepRegion();
			Region to = (regions[0].equals(from)) ? regions[1] : regions[0];

			// Move black sheep
			from.removeBlackSheep();
			to.placeBlackSheep();

			// GUI: update the region
			turnScheduler.updateGUIBlackSheep(to);
		}
		else
		{
			// Event info
			Integer fromRegionID = ((SheepEvent) event).getFromRegionID();
			Region from = null;
			Region to = null;
			if (regions[0].getId().equals(fromRegionID))
			{
				from = regions[0];
				to = regions[1];
			}
			else
			{
				from = regions[1];
				to = regions[0];
			}

			// Move white sheep or ram
			if (from.getWhiteSheepNumber() > 0 && from.getRamNumber() <= 0)
			{
				from.removeWhiteSheep();
				to.placeWhiteSheep();
			}
			else if (from.getWhiteSheepNumber() <= 0 && from.getRamNumber() > 0)
			{
				from.removeRam();
				to.placeRam();
			}
			else
			{
				Random rand = new Random();
				if (rand.nextInt(2) == 0)
				{
					from.removeWhiteSheep();
					to.placeWhiteSheep();
				}
				else
				{
					from.removeRam();
					to.placeRam();
				}
			}

			// GUI: update the two regions
			turnScheduler.updateGUIWhiteSheep(regions[0]);
			turnScheduler.updateGUIWhiteSheep(regions[1]);
		}
	}
}
