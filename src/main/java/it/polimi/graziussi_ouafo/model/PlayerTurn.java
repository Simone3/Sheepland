package it.polimi.graziussi_ouafo.model;

import it.polimi.graziussi_ouafo.main.MiscConstants;

public class PlayerTurn
{

	private String previousAction;
	private boolean hasMovedShepherd;
	private int availableActions;

	public PlayerTurn()
	{

		this.availableActions = MiscConstants.ACTIONS_PER_TURN.getValue();
		this.hasMovedShepherd = false;
		this.previousAction = "";
	}

	public String getPreviousAction()
	{

		return this.previousAction;
	}

	public void setPreviousAction(String action)
	{

		this.previousAction = action;

	}

	public boolean hasMovedShepherd()
	{

		return this.hasMovedShepherd;
	}

	public void setHasMovedShepherd()
	{

		this.hasMovedShepherd = true;

	}

	public int getAvailableActions()
	{

		return this.availableActions;

	}

	public void subAvailableActions()
	{

		this.availableActions = this.availableActions - 1;
	}

}
