package it.polimi.graziussi_ouafo.events;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.model.RegionType;

public class DeckEvent extends AbstractActionEvent
{
	private static final long serialVersionUID = 1L;
	RegionType deckType;

	public DeckEvent(RegionType deckType, ActionStrategy strategy)
	{
		this.deckType = deckType;
		this.setLinkedAction(strategy);
	}

	public RegionType getDeckType()
	{
		return this.deckType;
	}
}
