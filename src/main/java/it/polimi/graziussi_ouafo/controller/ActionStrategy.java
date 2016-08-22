package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

public abstract class ActionStrategy implements Serializable
{
	private static final long serialVersionUID = 1L;
	private boolean decreasesActionCount = true;

	abstract boolean canPerformAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException;

	abstract void performAction(AbstractActionEvent event, TurnScheduler turnScheduler) throws GameErrorException, InvocationTargetException, InterruptedException, RemoteException;

	boolean getDecreasesActionCount()
	{
		return this.decreasesActionCount;
	}

	void doesNotDecreaseActionCount()
	{
		this.decreasesActionCount = false;
	}
}
