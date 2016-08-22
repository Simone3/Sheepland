package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;
import it.polimi.graziussi_ouafo.view.GameMapRemoteInterface;
import it.polimi.graziussi_ouafo.view.GameWindowRemoteInterface;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class FakeGameWindow implements GameWindowRemoteInterface
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GameMapRemoteInterface gameMap;

	public FakeGameWindow(FakeGameMap map)
	{
		this.gameMap = map;
	}

	@Override
	public GameMapRemoteInterface getGameMap() throws RemoteException
	{

		return this.gameMap;
	}

	@Override
	public void setSelectActionButtonsVisibility(boolean visibility) throws RemoteException
	{

	}

	@Override
	public void activateAvailableDecks(List<RegionType> availableDeckTypes, ActionStrategy strategy) throws RemoteException
	{

	}

	@Override
	public void deactivateAllActionComponents() throws InvocationTargetException, InterruptedException, RemoteException
	{

	}

	@Override
	public void updateDeckButton(TerrainTileDeck deckToUpdate) throws RemoteException
	{

	}

	@Override
	public void updateGameInfo(String currentPlayerName, Color currentPlayerColor, int playerDinars, int availableNormalFences, Map<RegionType, Integer> boughtTerrainTiles, String actionsAvailableMessage) throws RemoteException
	{

	}

	@Override
	public void updateCurrentPlayerInfo(String name, Color color) throws RemoteException
	{

	}

	@Override
	public void showShepherdChoiceOptions(int shepherdsPerPlayer) throws RemoteException
	{

	}

	@Override
	public void showEndOfGameInfo(Player[] finalChart) throws RemoteException
	{

	}

	@Override
	public void showPopup(String title, String message) throws RemoteException
	{

	}

}
