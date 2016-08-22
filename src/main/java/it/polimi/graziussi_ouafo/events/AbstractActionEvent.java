package it.polimi.graziussi_ouafo.events;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;

import java.io.Serializable;

public abstract class AbstractActionEvent implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ActionStrategy linkedAction;

	public ActionStrategy getLinkedAction()
	{
		return this.linkedAction;
	}

	void setLinkedAction(ActionStrategy action)
	{
		this.linkedAction = action;
	}

	public String getName()
	{
		return this.getClass().getSimpleName();
	}
}
