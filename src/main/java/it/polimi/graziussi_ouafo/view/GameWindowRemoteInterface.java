package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface GameWindowRemoteInterface extends Remote, Serializable
{
	public GameMapRemoteInterface getGameMap() throws RemoteException;

	public void setSelectActionButtonsVisibility(final boolean visibility) throws RemoteException;

	public void activateAvailableDecks(final List<RegionType> availableDeckTypes, final ActionStrategy strategy) throws RemoteException;

	public void deactivateAllActionComponents() throws InvocationTargetException, InterruptedException, RemoteException;

	public void updateDeckButton(final TerrainTileDeck deckToUpdate) throws RemoteException;

	public void updateGameInfo(final String currentPlayerName, final Color currentPlayerColor, final int playerDinars, final int availableNormalFences, final Map<RegionType, Integer> boughtTerrainTiles, final String actionsAvailableMessage) throws RemoteException;

	public void updateCurrentPlayerInfo(final String name, final Color color) throws RemoteException;

	public void showShepherdChoiceOptions(final int shepherdsPerPlayer) throws RemoteException;

	public void showEndOfGameInfo(Player[] finalChart) throws RemoteException;

	public void showPopup(String title, String message) throws RemoteException;
}
