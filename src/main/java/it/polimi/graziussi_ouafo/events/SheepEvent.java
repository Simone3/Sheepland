package it.polimi.graziussi_ouafo.events;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;

public class SheepEvent extends AbstractActionEvent
{
	private static final long serialVersionUID = 1L;
	Integer fromRegionID;
	boolean blackSheep;

	public SheepEvent(Integer fromRegionID, boolean blackSheep, ActionStrategy strategy)
	{
		this.fromRegionID = fromRegionID;
		this.blackSheep = blackSheep;
		this.setLinkedAction(strategy);
	}

	public Integer getFromRegionID()
	{
		return this.fromRegionID;
	}

	public boolean isBlackSheepMovement()
	{
		return this.blackSheep;
	}
}
