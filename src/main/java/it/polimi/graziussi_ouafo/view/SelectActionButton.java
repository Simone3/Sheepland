package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.controller.SelectActionStrategy;

import javax.swing.JButton;

public class SelectActionButton extends JButton
{
	private static final long serialVersionUID = 1L;
	private SelectActionStrategy strategy;

	public SelectActionButton(String text, SelectActionStrategy strategy)
	{
		super(text);
		this.strategy = strategy;
	}

	public SelectActionStrategy getSelectActionStrategy()
	{
		return this.strategy;
	}
}
