package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;

import java.rmi.RemoteException;

public class SelectActionShepherdStartingPosition extends SelectActionStrategy
{
	private static final long serialVersionUID = 1L;

	@Override
	boolean canShowAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException
	{
		return !(turnScheduler.areAllShepherdsPlaced());
	}

	@Override
	void showAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException
	{
		new SelectActionMoveShepherd().showAction(event, turnScheduler);
	}
}
