package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;

import java.io.Serializable;
import java.rmi.RemoteException;

public abstract class SelectActionStrategy implements Serializable
{
	private static final long serialVersionUID = 1L;

	abstract boolean canShowAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException;

	abstract void showAction(SelectActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, RemoteException;
}
