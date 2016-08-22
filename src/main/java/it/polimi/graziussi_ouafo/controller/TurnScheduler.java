package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.events.AbstractActionEvent;
import it.polimi.graziussi_ouafo.events.RoadEvent;
import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.ActionNotAllowedException;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MapInitializer;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.PlayerTurn;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;
import it.polimi.graziussi_ouafo.view.GameWindowRemoteInterface;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.xml.sax.SAXException;

public class TurnScheduler implements Observer, TurnSchedulerRemoteInterface
{
	private GameState gameState;

	private Map<Player, GameWindowRemoteInterface> clientViews;

	private boolean allShepherdsPlaced = false;
	private int currentToPlaceShepherdNumber = 0;

	public TurnScheduler(boolean offline, List<String> playerNames, List<PlayerColor> playerColors, GameWindowRemoteInterface offlineClient) throws GameErrorException, ParserConfigurationException, SAXException, IOException, NotBoundException, IllegalArgumentException, AlreadyBoundException
	{
		this.clientViews = new HashMap<Player, GameWindowRemoteInterface>();

		// In an online match turn scheduler must make itself accessible as a remote object
		if (!offline)
		{
			// Create registry
			try
			{
				LocateRegistry.createRegistry(1099);
			}
			catch (RemoteException e)
			{
				// Registry already created, no need to do anything
			}

			// Make this class remotely accessible
			TurnSchedulerRemoteInterface stub = (TurnSchedulerRemoteInterface) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("rmi://Sheepland-Server", stub);
		}

		// Initialize model
		this.initializeGameState(offline, playerNames, playerColors, offlineClient);
	}

	GameState getGameState()
	{
		return this.gameState;
	}

	GameWindowRemoteInterface getCurrentClient()
	{
		if (this.gameState.isOffline())
		{
			return this.clientViews.get(null);
		}
		else
		{
			return this.clientViews.get(this.gameState.getCurrentPlayer());
		}
	}

	int getShepherdsPerPlayerNumber()
	{
		return (this.gameState.getNumberOfPlayers() <= MiscConstants.NUM_OF_PLAYER_THRESHOLD.getValue()) ? MiscConstants.SHEPHERDS_PER_PLAYER_UNDER_OR_EQUALS_THRESHOLD.getValue() : MiscConstants.SHEPHERDS_PER_PLAYER_OVER_THRESHOLD.getValue();
	}

	int throwDie() throws RemoteException
	{
		// Get a random number between 1 and 6
		Random rand = new Random();
		int number = rand.nextInt(6) + 1;

		// Update GUI
		this.getCurrentClient().getGameMap().showDieThrow(number);

		return number;
	}

	/**** METHODS TO MANAGE GAME INITIALIZATION/FIRST PHASES ****/

	private void initializeGameState(boolean offline, List<String> playerNames, List<PlayerColor> playerColors, GameWindowRemoteInterface offlineClient) throws ParserConfigurationException, SAXException, IOException, NotBoundException, IllegalArgumentException, GameErrorException
	{
		// Create roads and regions
		Graph<Road, DefaultEdge> roadGraph = (new MapInitializer()).createRoadGraphFromXML();

		// Create players
		int playerDinars = (playerNames.size() > MiscConstants.NUM_OF_PLAYER_THRESHOLD.getValue()) ? MiscConstants.STARTING_DINARS_OVER_THRESHOLD.getValue() : MiscConstants.STARTING_DINARS_UNDER_OR_EQUALS_THRESHOLD.getValue();
		Player[] players = new Player[playerNames.size()];
		String[] playerNamesA = playerNames.toArray(new String[playerNames.size()]);
		PlayerColor[] playerColorsA = playerColors.toArray(new PlayerColor[playerColors.size()]);
		for (int i = 0; i < players.length; i++)
		{
			players[i] = new Player(playerNamesA[i], playerColorsA[i], playerDinars);
		}

		// Create decks
		List<TerrainTileDeck> decks = new ArrayList<TerrainTileDeck>();
		RegionType[] regionTypes = RegionType.getDeckTypes();
		for (RegionType type : regionTypes)
		{
			decks.add(new TerrainTileDeck(type));
		}

		// Create model's main class
		this.gameState = new GameState(offline, players, decks, roadGraph);

		// Save reference to the client view if it's an offline match (for online ones the clients themselves will call the server remote method to add their view to the map)
		if (offline)
		{
			this.saveWindow(null, offlineClient);
		}
	}

	final public void saveWindow(String playerName, GameWindowRemoteInterface clientInterface) throws RemoteException, GameErrorException
	{
		// Error if the game is already started
		if (this.gameState.isGameStarted())
		{
			throw new GameErrorException("Cannot add client view when the game is already started!");
		}

		if (this.gameState.isOffline())
		{
			// For offline matches there is only one view
			this.clientViews.put(null, clientInterface);
		}
		else
		{
			// Get player who called the method
			for (Player player : this.gameState.getPlayers())
			{
				if (player.getName().equals(playerName))
				{
					// Add client interface in the map
					this.clientViews.put(player, clientInterface);
				}
			}

			// Exit method if this isn't the last player
			if (this.clientViews.size() != this.gameState.getPlayers().length)
			{
				return;
			}
		}

		// We reach this code only when all clients are connected and ready: start game
		this.manageGameStart();
	}

	@Override
	public void registerClientInServer(String playerName, String playerAddress) throws RemoteException, GameErrorException, NotBoundException
	{
		Registry registry = LocateRegistry.getRegistry(playerAddress);
		GameWindowRemoteInterface windowInterface = (GameWindowRemoteInterface) registry.lookup("rmi://Sheepland-Client-" + playerName);
		this.saveWindow(playerName, windowInterface);
	}

	private void manageGameStart() throws RemoteException, GameErrorException
	{
		// Give each player a random terrain tile
		Player[] players = this.gameState.getPlayers();
		RegionType[] deckTypes = RegionType.getDeckTypes();
		Collections.shuffle(Arrays.asList(deckTypes));
		for (int i = 0; i < players.length; i++)
		{
			players[i].addBoughtTerrainTile(deckTypes[i]);
		}

		// Set game as started
		this.gameState.setGameStarted();

		// Update regions to show correct number of ram/sheep/lamb in each one
		this.updateGUIAllRegionWhiteSheep();

		// Start first turn
		this.startNewTurn();
	}

	/**** METHODS TO MANAGE PLAYERS' EVENTS ****/

	void manageActionEvent(AbstractActionEvent event) throws ActionNotAllowedException, GameErrorException, InvocationTargetException, InterruptedException, RemoteException
	{
		ActionStrategy strategy = event.getLinkedAction();

		// Check if action is allowed
		if (strategy.canPerformAction(event, this))
		{
			// Perform the action
			strategy.performAction(event, this);

			// Update GUI: all action components (sheep, decks,...) must be inactive until player chooses another action
			this.getCurrentClient().deactivateAllActionComponents();

			// Set previous action in player turn and decrease available actions
			PlayerTurn currentTurn = this.gameState.getCurrentTurn();
			currentTurn.setPreviousAction(event.getLinkedAction().getClass().getSimpleName());
			if (strategy.getDecreasesActionCount())
			{
				currentTurn.subAvailableActions();
			}

			// Check if player turn is over
			if (currentTurn.getAvailableActions() == 0)
			{
				// Start new turn
				this.startNewTurn();
			}
			else
			{
				// Update GUI: allow user to select a new action
				this.allowUserToChooseAction();
			}

			// GUI: update game info
			this.updateGUIGameInfo();
		}
		else
		{
			throw new ActionNotAllowedException("This action is not allowed!");
		}
	}

	void manageSelectActionEvent(SelectActionEvent event) throws ActionNotAllowedException, GameErrorException, RemoteException
	{
		SelectActionStrategy strategy = event.getLinkedAction();

		// Check if action is allowed
		if (strategy.canShowAction(event, this))
		{
			// Update GUI: all select action buttons must be inactive until current action is done
			this.getCurrentClient().setSelectActionButtonsVisibility(false);

			// Perform the action
			strategy.showAction(event, this);
		}
		else
		{
			throw new ActionNotAllowedException("This action is not allowed!");
		}
	}

	private void manageShepherdChoice() throws RemoteException
	{
		// Shepherd number
		int shepherdsPerPlayer = this.getShepherdsPerPlayerNumber();

		// If there is only one shepherd, no need to choose
		if (shepherdsPerPlayer == 1)
		{
			this.gameState.getCurrentPlayer().setChosenShepherdForTurn(0);
			this.allowUserToChooseAction();
		}
		else
		{
			// Allow user to choose a shepherd and wait for event (in the "update" method)
			this.getCurrentClient().showShepherdChoiceOptions(shepherdsPerPlayer);
		}
	}

	/**** METHODS TO MANAGE TURNS ****/

	void startNewTurn() throws GameErrorException, RemoteException
	{
		// Get new player
		Player newPlayer = (this.gameState.getCurrentPlayer() == null) ? this.gameState.getPlayers()[0] : this.gameState.getNextPlayer();

		// Change current player
		this.gameState.setCurrentPlayer(newPlayer);
		
		// Update GUI: show new player notification
		this.showNewPlayerNotification();

		// Check if the game is over
		if (!this.isGameOver())
		{
			// If all shepherds have a starting position, this is a "real" turn
			if (this.allShepherdsPlaced)
			{
				// Create new PlayerTurn object, overwriting the previous one
				this.gameState.setCurrentTurn(new PlayerTurn());

				// Update game info
				this.updateGUIGameInfo();

				// If this is the first player, perform some actions
				if (this.gameState.getCurrentPlayer().equals(this.gameState.getPlayers()[0]))
				{
					this.performStartingRoundActions();
				}

				// Perform starting turn actions
				this.performStartingTurnActions();
			}

			// If not all shepherds have been placed, this is a "fake" turn just to choose their starting positions
			else
			{
				this.currentToPlaceShepherdNumber = 0;

				// Show message
				this.updateGUIShowShepherdInitialPositionMessage();

				// Allow user to select his shepherd(s) initial position(s)
				this.allowPlayerToChooseShepherdInitialPositon();
			}
		}
		else
		{
			// Manage end of game
			(new EndOfGameManager()).manageEndOfGame(this);
		}
	}

	boolean isGameOver()
	{
		// Check if normal fences are all used and if the current player is the first one in the turn order
		if (this.gameState.getAvailableNormalFences() <= 0 && this.gameState.getCurrentPlayer().equals(this.gameState.getPlayers()[0]))
		{
			return true;
		}

		return false;
	}

	// Actions performed at each player's starting turn
	private void performStartingTurnActions() throws GameErrorException, RemoteException
	{
		// Shepherd choice for this turn
		this.gameState.getCurrentPlayer().unsetChosenShepherdForTurn();
		this.manageShepherdChoice();

		// Black sheep
		this.manageBlackSheepRandomMovement();

		// Wolf
		this.manageWolfRandomMovement();
	}

	// Actions performed at the round start (when it's the first player in the list turn)
	private void performStartingRoundActions() throws GameErrorException, RemoteException
	{
		// Make the lambs grow up
		this.growUpLambs();
	}

	private void manageBlackSheepRandomMovement() throws GameErrorException, RemoteException
	{
		// Get the sheep location
		Region from = this.gameState.getBlackSheepRegion();

		// Get the roads that the sheep can cross
		Road[] crossableRoads = this.gameState.getEmptyRoadsAroundRegion(from);

		// If it cannot reach any region, stays in current position (so no need to throw a die)
		if (crossableRoads.length == 0)
		{
			return;
		}

		// Throw a die
		int dieResult = this.throwDie();

		// Check if there is a free road with the die result
		Road roadToCross = null;
		for (int i = 0; i < crossableRoads.length; i++)
		{
			if (dieResult == crossableRoads[i].getNumber() && !crossableRoads[i].hasFence() && !crossableRoads[i].hasShepherd())
			{
				roadToCross = crossableRoads[i];
				break;
			}
		}

		// If no free road with the die result, the sheep stays in current position
		if (roadToCross == null)
		{
			return;
		}

		// Get the region reachable crossing that road
		Region[] regions = roadToCross.getRegions();
		Region to = (regions[0] == from) ? regions[1] : regions[0];

		// Move sheep
		from.removeBlackSheep();
		to.placeBlackSheep();

		// GUI: update the region
		this.updateGUIBlackSheep(to);
	}

	private void manageWolfRandomMovement() throws GameErrorException, RemoteException
	{
		int dieResult;
		Road roadToCross = null;
		boolean isCorrect = false;

		// Get the wolf location
		Region from = this.gameState.getWolfRegion();

		// Get the roads that the wolf can cross
		Road[] crossableRoads = this.gameState.getWolfRoadsAroundRegion(from);

		// If no crossable roads (= all shepherds around), no movement
		if (crossableRoads.length == 0)
		{
			return;
		}

		// If only one road is without shepherd (with or without fence), choose this without throwing a die
		if (crossableRoads.length == 1)
		{
			roadToCross = crossableRoads[0];
		}
		else
		{
			// Check if there is only one road without a fence
			boolean onlyOneWithoutFence = false;
			for (Road road : crossableRoads)
			{
				if (!road.hasFence())
				{
					if (onlyOneWithoutFence)
					{
						onlyOneWithoutFence = false;
						break;
					}

					onlyOneWithoutFence = true;
				}
			}

			// If only one road without fence, move in that direction without throwing a die
			if (onlyOneWithoutFence)
			{
				for (Road road : crossableRoads)
				{
					if (!road.hasFence())
					{
						roadToCross = road;
						break;
					}
				}
			}
			else
			{
				// Check if all roads are with a fence
				boolean allRoadsWithFence = true;
				for (Road road : crossableRoads)
				{
					if (!road.hasFence())
					{
						allRoadsWithFence = false;
						break;
					}
				}

				// If there are only fences, pick the first that comes out with the die
				if (allRoadsWithFence)
				{
					isCorrect = false;
					while (!isCorrect)
					{
						dieResult = this.throwDie();

						for (Road road : crossableRoads)
						{
							if (road.getNumber() == dieResult)
							{
								roadToCross = road;
								isCorrect = true;
								break;
							}
						}
					}
				}
				else
				{
					// At this point there must be at least two free roads (no fence and no shepherd) so throw die until you get one
					isCorrect = false;
					while (!isCorrect)
					{
						dieResult = this.throwDie();

						for (Road road : crossableRoads)
						{
							if (road.getNumber() == dieResult && !road.hasFence())
							{
								roadToCross = road;
								isCorrect = true;
								break;
							}
						}
					}
				}
			}
		}

		// Error if I didn't find a road (if no road available should return before this, so here it must have found one)
		if (roadToCross == null)
		{
			throw new GameErrorException("Error while getting wolf road!");
		}

		// Get the region reachable crossing that road
		Region[] regions = roadToCross.getRegions();
		Region to = (regions[0].equals(from)) ? regions[1] : regions[0];

		// Move wolf
		from.removeWolf();
		to.placeWolf();

		// Wolf eats one sheep/ram, if there is at least one
		if (to.getWhiteSheepNumber() > 0)
		{
			to.removeWhiteSheep();
		}
		else if (to.getRamNumber() > 0)
		{
			to.removeRam();
		}

		// GUI: update the region
		this.updateGUIWolf(to);
		this.updateGUIWhiteSheep(to);
	}

	// Method used to make all lambs grow up (becoming either white sheep or ram)
	private void growUpLambs() throws RemoteException
	{
		Random rand = new Random();
		Set<Region> regions = this.gameState.getAllRegions();

		// Loop all regions
		int lambNumber;
		for (Region region : regions)
		{
			lambNumber = region.getLambNumber();

			// If there is at least one lamb, remove and add a sheep or ram
			if (lambNumber > 0)
			{
				for (int j = 0; j < lambNumber; j++)
				{
					region.removeLamb();
					if (rand.nextInt(2) == 0)
					{
						region.placeWhiteSheep();
					}
					else
					{
						region.placeRam();
					}
				}

				// Update GUI
				this.updateGUIWhiteSheep(region);
			}
		}
	}

	void allowUserToChooseAction() throws RemoteException
	{
		GameWindowRemoteInterface currentClient = this.getCurrentClient();
		currentClient.setSelectActionButtonsVisibility(true);
	}

	/**** METHODS FOR SHEPHERD INITIAL POSITIONING ****/

	void allowPlayerToChooseShepherdInitialPositon() throws GameErrorException, RemoteException
	{
		try
		{
			this.manageSelectActionEvent(new SelectActionEvent(new SelectActionShepherdStartingPosition()));
		}
		catch (ActionNotAllowedException e)
		{
			throw new GameErrorException("Error! Select shepherd initial position not allowed!");
		}
	}

	boolean areAllShepherdsPlaced()
	{
		return this.allShepherdsPlaced;
	}

	int getCurrentToPlaceShepherdNumber()
	{
		return this.currentToPlaceShepherdNumber;
	}

	void increaseCurrentToPlaceShepherdNumber()
	{
		this.currentToPlaceShepherdNumber++;
	}

	void setAllShepherdsPlaced()
	{
		this.allShepherdsPlaced = true;
	}

	/**** METHODS TO INTERACT WITH PLAYERS' VIEW(S) ****/

	void sendMessageToCurrentPlayer(String title, String message) throws RemoteException
	{
		this.getCurrentClient().showPopup(title, message);
	}

	void showTimedMessageToCurrentPlayer(String message) throws RemoteException
	{
		this.getCurrentClient().getGameMap().showTimedMessage(message);
	}
	
	void showNewPlayerNotification() throws RemoteException
	{
		GameWindowRemoteInterface view;
		Player player;
		String message;
		for(Map.Entry<Player, GameWindowRemoteInterface> entity : this.clientViews.entrySet())
		{
			player = entity.getKey();
			view = entity.getValue();
			if (this.gameState.isOffline())
			{
				player = this.gameState.getCurrentPlayer();
			}
		
			if(player.equals(gameState.getCurrentPlayer()))
			{
				message = player.getName()+", it's your turn!";
			}
			else
			{
				message = "It's "+gameState.getCurrentPlayer().getName()+" turn!";
			}
			
			view.getGameMap().showNewPlayerMessage(message);
		}
	}

	void manageGameError(String error, Exception e)
	{
		try
		{
			this.getCurrentClient().showPopup("Error!", error);
			e.printStackTrace();
			System.exit(0);
		}
		catch (RemoteException e1)
		{
			// Cannot even reach the player, so just exit
			System.exit(0);
		}
	}

	void updateGUIGameInfo() throws RemoteException
	{
		for (Map.Entry<Player, GameWindowRemoteInterface> entity : this.clientViews.entrySet())
		{
			Player player = entity.getKey();
			GameWindowRemoteInterface view = entity.getValue();

			// In an offline match there is only one view, so player is null. Set it equals to the current one
			if (this.gameState.isOffline())
			{
				player = this.gameState.getCurrentPlayer();
			}

			String currentPlayerMessage = "";
			String actionsAvailableMessage = "";
			if (player.equals(this.gameState.getCurrentPlayer()))
			{
				currentPlayerMessage = player.getName() + ", it's your turn!";
				actionsAvailableMessage = "You must perform other " + this.gameState.getCurrentTurn().getAvailableActions() + " action(s)";
			}
			else
			{
				currentPlayerMessage = "The current player is " + this.gameState.getCurrentPlayer().getName() + ", wait for your turn!";
				actionsAvailableMessage = this.gameState.getCurrentPlayer().getName() + " still has " + this.gameState.getCurrentTurn().getAvailableActions() + " action(s) to perform";
			}

			view.updateGameInfo(currentPlayerMessage, this.gameState.getCurrentPlayer().getColor().getColor(), player.getDinars(), this.gameState.getAvailableNormalFences(), player.getBoughtTerrainTiles(), actionsAvailableMessage);
		}
	}

	void updateGUIDeckButton(TerrainTileDeck deck) throws RemoteException
	{
		Iterator<GameWindowRemoteInterface> itr = this.clientViews.values().iterator(); // in an offline match there will be only one client
		GameWindowRemoteInterface currentView;
		while (itr.hasNext())
		{
			currentView = itr.next();

			currentView.updateDeckButton(deck);
		}
	}

	void updateGUIWhiteSheep(Region region) throws RemoteException
	{
		Iterator<GameWindowRemoteInterface> itr = this.clientViews.values().iterator(); // in an offline match there will be only one client
		GameWindowRemoteInterface currentView;
		while (itr.hasNext())
		{
			currentView = itr.next();

			currentView.getGameMap().updateWhiteSheepPanel(region);
		}
	}

	void updateGUIBlackSheep(Region region) throws RemoteException, GameErrorException
	{
		if (!region.hasBlackSheep())
		{
			throw new GameErrorException("Error! Trying to move black sheep panel where there is no black sheep!");
		}

		Iterator<GameWindowRemoteInterface> itr = this.clientViews.values().iterator(); // in an offline match there will be only one client
		GameWindowRemoteInterface currentView;
		while (itr.hasNext())
		{
			currentView = itr.next();

			currentView.getGameMap().moveBlackSheepPanel(region.getId());
		}
	}

	void updateGUIWolf(Region region) throws RemoteException, GameErrorException
	{
		if (!region.hasWolf())
		{
			throw new GameErrorException("Error! Trying to move wolf panel where there is no wolf!");
		}

		Iterator<GameWindowRemoteInterface> itr = this.clientViews.values().iterator(); // in an offline match there will be only one client
		GameWindowRemoteInterface currentView;
		while (itr.hasNext())
		{
			currentView = itr.next();

			currentView.getGameMap().moveWolfPanel(region.getId());
		}
	}

	void updateGUIRoad(Road road) throws RemoteException
	{
		Iterator<GameWindowRemoteInterface> itr = this.clientViews.values().iterator(); // in an offline match there will be only one client
		GameWindowRemoteInterface currentView;
		while (itr.hasNext())
		{
			currentView = itr.next();

			currentView.getGameMap().updateRoadPanel(road);
		}
	}

	// Show initial game message
	void updateGUIShowShepherdInitialPositionMessage() throws RemoteException
	{
		for (Map.Entry<Player, GameWindowRemoteInterface> entity : this.clientViews.entrySet())
		{
			Player player = entity.getKey();
			GameWindowRemoteInterface view = entity.getValue();

			// In an offline match there is only one view, so player is null. Set it equals to the current one
			if (this.gameState.isOffline())
			{
				player = this.gameState.getCurrentPlayer();
			}

			String currentPlayerMessage = "";
			if (player == null || player.equals(this.gameState.getCurrentPlayer()))
			{
				currentPlayerMessage = "Game started! " + player.getName() + ", it's your turn to choose your shepherd(s) initial position(s)!";
			}
			else
			{
				currentPlayerMessage = "Game started! It's " + this.gameState.getCurrentPlayer().getName() + " turn to choose his/her shepherd(s) initial position(s). Wait for your turn!";
			}

			view.updateCurrentPlayerInfo(currentPlayerMessage, this.gameState.getCurrentPlayer().getColor().getColor());
		}
	}

	void updateGUIEndOfGame(List<Player> chart) throws RemoteException
	{
		Iterator<GameWindowRemoteInterface> itr = this.clientViews.values().iterator(); // in an offline match there will be only one client
		GameWindowRemoteInterface currentView;
		while (itr.hasNext())
		{
			currentView = itr.next();

			currentView.showEndOfGameInfo(chart.toArray(new Player[chart.size()]));
		}
	}

	private void updateGUIAllRegionWhiteSheep() throws RemoteException
	{
		Set<Region> regions = this.gameState.getAllRegions();

		for (Region region : regions)
		{
			this.updateGUIWhiteSheep(region);
		}
	}

	/**** OBSERVABLE/OBSERVER PATTERN ****/

	@Override
	public void update(Observable arg0, Object event)
	{
		// Manage the event
		try
		{
			// Special event: choose starting shepherd(s) position(s)
			if (!this.allShepherdsPlaced && event instanceof RoadEvent)
			{
				ActionShepherdStartingPosition action = new ActionShepherdStartingPosition();
				if (action.canPerformAction((RoadEvent) event, this))
				{
					action.performAction((RoadEvent) event, this);
				}
			}
			else if (event instanceof SelectActionEvent)
			{
				this.manageSelectActionEvent((SelectActionEvent) event);
			}
			else if (event instanceof AbstractActionEvent)
			{
				this.manageActionEvent((AbstractActionEvent) event);
			}
			else
			{
				return;
			}
		}
		catch (ActionNotAllowedException e)
		{
			try
			{
				TurnScheduler.this.sendMessageToCurrentPlayer("Action not allowed!", e.getText());
			}
			catch (RemoteException e1)
			{
				TurnScheduler.this.manageGameError("Remote error: cannot send message to current player!", e1);
			}
		}
		catch (GameErrorException e)
		{
			TurnScheduler.this.manageGameError(e.getError(), e);
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			TurnScheduler.this.manageGameError("Error while updating GUI!", e);
		}
		catch (RemoteException e)
		{
			TurnScheduler.this.manageGameError("Remote error: cannot send message to current player!", e);
		}
	}
}
