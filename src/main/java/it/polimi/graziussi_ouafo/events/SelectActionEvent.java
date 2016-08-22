package it.polimi.graziussi_ouafo.events;

import it.polimi.graziussi_ouafo.controller.SelectActionStrategy;

import java.io.Serializable;

public class SelectActionEvent implements Serializable
{
	private static final long serialVersionUID = 1L;
	private SelectActionStrategy linkedSelectAction;

	public SelectActionEvent(SelectActionStrategy linkedSelectAction)
	{
		this.linkedSelectAction = linkedSelectAction;
	}

	public SelectActionStrategy getLinkedAction()
	{
		return this.linkedSelectAction;
	}
}
