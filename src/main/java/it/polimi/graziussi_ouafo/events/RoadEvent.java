package it.polimi.graziussi_ouafo.events;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;

public class RoadEvent extends AbstractActionEvent
{
	private static final long serialVersionUID = 1L;
	Integer roadID;

	public RoadEvent(Integer roadID, ActionStrategy strategy)
	{
		this.roadID = roadID;
		this.setLinkedAction(strategy);
	}

	public Integer getRoadID()
	{
		return this.roadID;
	}
}
