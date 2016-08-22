package it.polimi.graziussi_ouafo.events;

import it.polimi.graziussi_ouafo.controller.ActionChooseShepherd;

public class ChooseShepherdEvent extends AbstractActionEvent
{
	private static final long serialVersionUID = 1L;
	Integer shepherdNumber;

	public ChooseShepherdEvent(Integer shepherdNumber)
	{
		this.shepherdNumber = shepherdNumber;
		this.setLinkedAction(new ActionChooseShepherd());
	}

	public Integer getShepherdNumber()
	{
		return this.shepherdNumber;
	}
}
