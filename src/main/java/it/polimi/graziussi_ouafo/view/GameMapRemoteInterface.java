package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.JLayeredPane;

public interface GameMapRemoteInterface extends Remote
{
	public void activateRoads(final List<Integer> roadIDs, final ActionStrategy strategy) throws RemoteException;

	public void activateWhiteSheep(final List<Integer> regionIDs, final ActionStrategy strategy) throws RemoteException;

	public void activateBlackSheep(final ActionStrategy strategy) throws RemoteException;

	public void deactivateAllRoadsAndRegions() throws RemoteException;

	public void updateRoadPanel(final Road road) throws RemoteException;

	public void updateWhiteSheepPanel(final Region region) throws RemoteException;

	public void moveBlackSheepPanel(final Integer regionTo) throws RemoteException;

	public void moveWolfPanel(final Integer regionTo) throws RemoteException;

	public JLayeredPane getSwingComponent() throws RemoteException;

	public void showTimedMessage(final String name) throws RemoteException;

	public void showDieThrow(final int number) throws RemoteException;

	public void showNewPlayerMessage(final String message) throws RemoteException;
}
