package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.SheepEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.model.Shepherd;

import java.rmi.RemoteException;
import java.util.Random;

public class ActionShooting extends ActionStrategy
{

	private static final long serialVersionUID = 1L;

	@Override
	public boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{

		// Get model info from TurnScheduler
		GameState gameState = turnScheduler.getGameState();
		PlayerTurn playerTurn = gameState.getCurrentTurn();
		String previousAction = playerTurn.getPreviousAction();

		// Cannot shoot if previous action is the same
		if (ActionShooting.class.getSimpleName().equals(previousAction))
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

		// Check if there actually is at least one sheep or one ram
		if (region.getWhiteSheepNumber() > 0 || region.getRamNumber() > 0)
		{
			// Check if enough money to buy silence from all surrounding shepherds
			Shepherd[] shepherdsAroundRegion = gameState.getEnemyShepherdsAroundRegion(region, gameState.getCurrentPlayer());
			int shepherdSilenceCost = shepherdsAroundRegion.length * MiscConstants.SHEPHERD_SILENCE_PRICE.getValue();
			return (shepherdSilenceCost <= gameState.getCurrentPlayer().getDinars());
		}
		else
		{
			return false;
		}

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
			return;
		}

		int sheepNumber = region.getWhiteSheepNumber();
		int ramNumber = region.getRamNumber();

		int dieNumber = turnScheduler.throwDie();

		if (dieNumber == shepherdRoad.getNumber())
		{
			if (sheepNumber > 0 && ramNumber <= 0)
			{
				region.removeWhiteSheep();
			}
			else if (sheepNumber <= 0 && ramNumber > 0)
			{
				region.removeRam();
			}
			else
			{
				Random rand = new Random();
				if (rand.nextInt(2) == 0)
				{
					region.removeWhiteSheep();
				}
				else
				{
					region.removeRam();
				}
			}

			// Sub dinars if enemy shepherds witness the shooting
			Shepherd[] shepherdsAroundRegion = gameState.getEnemyShepherdsAroundRegion(region, gameState.getCurrentPlayer());
			int totalPayed = 0;
			for (int i = 0; i < shepherdsAroundRegion.length; i++)
			{
				if (turnScheduler.throwDie() >= 5)
				{
					totalPayed += MiscConstants.SHEPHERD_SILENCE_PRICE.getValue();
					gameState.getCurrentPlayer().subDinars(MiscConstants.SHEPHERD_SILENCE_PRICE.getValue());
				}
			}

			if (totalPayed > 0)
			{
				turnScheduler.showTimedMessageToCurrentPlayer("You paid " + totalPayed + " for buy the silence!");
			}

			turnScheduler.updateGUIWhiteSheep(region);
			turnScheduler.updateGUIGameInfo();
		}
		else
		{
			turnScheduler.showTimedMessageToCurrentPlayer("You missed!");
		}
	}
}
