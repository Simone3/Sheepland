package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.view.GameMapRemoteInterface;

import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JLayeredPane;

public class FakeGameMap implements GameMapRemoteInterface
{

	@Override
	public void activateRoads(List<Integer> roadIDs, ActionStrategy strategy) throws RemoteException
	{

	}

	@Override
	public void activateWhiteSheep(List<Integer> regionIDs, ActionStrategy strategy) throws RemoteException
	{

	}

	@Override
	public void activateBlackSheep(ActionStrategy strategy) throws RemoteException
	{

	}

	@Override
	public void deactivateAllRoadsAndRegions() throws RemoteException
	{

	}

	@Override
	public void updateRoadPanel(Road road) throws RemoteException
	{

	}

	@Override
	public JLayeredPane getSwingComponent() throws RemoteException
	{

		return new JLayeredPane();
	}

	@Override
	public void showTimedMessage(String name) throws RemoteException
	{

	}

	@Override
	public void updateWhiteSheepPanel(Region region) throws RemoteException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void moveBlackSheepPanel(Integer regionTo) throws RemoteException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void moveWolfPanel(Integer regionTo) throws RemoteException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void showDieThrow(int number) throws RemoteException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void showNewPlayerMessage(String playerName) throws RemoteException
	{
		// TODO Auto-generated method stub

	}

}
