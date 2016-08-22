package it.polimi.graziussi_ouafo.main;

import it.polimi.graziussi_ouafo.controller.Server;
import it.polimi.graziussi_ouafo.controller.TurnScheduler;
import it.polimi.graziussi_ouafo.controller.TurnSchedulerRemoteInterface;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.view.ClientSocket;
import it.polimi.graziussi_ouafo.view.GameMap;
import it.polimi.graziussi_ouafo.view.GameWindow;
import it.polimi.graziussi_ouafo.view.GameWindowRemoteInterface;
import it.polimi.graziussi_ouafo.view.MainMenu;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main
{
	private MainMenu mainMenu;
	private ClientSocket clientSocket;
	private final static Logger LOGGER = Logger.getAnonymousLogger();

	public Main()
	{
		// Create main menu to allow user to select game options
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Main.this.mainMenu = new MainMenu(Main.this);
				Main.this.mainMenu.initGameModeSelection();
			}
		});
	}

	public static void main(String[] args)
	{
		new Main();
	}

	public void startOfflineGame(List<String> playerNames, List<PlayerColor> playerColors)
	{
		try
		{
			// Hide main menu
			this.mainMenu.setVisible(false);

			// Create client side classes
			final GameMap gameMap = new GameMap();
			(new MapInitializer()).fillMapPanelFromXML(gameMap);
			GameWindow gameWindow = new GameWindow(gameMap);

			// Create controller's main class
			TurnScheduler turnScheduler = new TurnScheduler(true, playerNames, playerColors, gameWindow);

			// In the offline/local mode, the Controller observes directly the (only) View
			gameWindow.addObserver(turnScheduler);
			gameMap.addObserver(turnScheduler);
		}
		catch (IOException | ParserConfigurationException | SAXException | NotBoundException | IllegalArgumentException | AlreadyBoundException e)
		{
			JOptionPane.showMessageDialog(null, "Error while starting game!", "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
		catch (GameErrorException e)
		{
			JOptionPane.showMessageDialog(null, e.getError(), "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e.getStackTrace(), "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
	}

	public void startOnlineGameClient(String serverAddress, String playerName)
	{
		try
		{
			// Create socket manager
			if (this.clientSocket == null)
			{
				this.clientSocket = new ClientSocket(serverAddress);
			}

			// First thing to do in the game is to send the player name to the server
			if (!this.clientSocket.sendNameToServer(playerName))
			{
				JOptionPane.showMessageDialog(null, "This name is invalid, try another one!", "Invalid name", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else
			{
				// Show waiting message
				this.mainMenu.showWaitingMessage();

				// Wait server reply
				this.clientSocket.waitTurnSchedulerReady();

				// Hide main menu
				this.mainMenu.setVisible(false);

				// Create client side classes
				final GameMap gameMap = new GameMap();
				(new MapInitializer()).fillMapPanelFromXML(gameMap);
				GameWindow gameWindow = new GameWindow(gameMap);

				// In the online game the View is observed by the "connection handler", that sends events through socket
				gameWindow.addObserver(this.clientSocket);
				gameMap.addObserver(this.clientSocket);

				// Create registry
				try
				{
					LocateRegistry.createRegistry(1099);
				}
				catch (RemoteException e)
				{
					// Registry already created, no need to do anything
				}

				// Make this class available for remote calls
				UnicastRemoteObject.exportObject(gameMap, 0);
				GameWindowRemoteInterface stub = (GameWindowRemoteInterface) UnicastRemoteObject.exportObject(gameWindow, 0);
				Registry localRegistry = LocateRegistry.getRegistry();
				localRegistry.bind("rmi://Sheepland-Client-" + playerName, stub);

				// Add this client to the server list
				Registry serverRegistry = LocateRegistry.getRegistry(serverAddress);
				TurnSchedulerRemoteInterface turnSchedulerInterface = (TurnSchedulerRemoteInterface) serverRegistry.lookup("rmi://Sheepland-Server");
				turnSchedulerInterface.registerClientInServer(playerName, InetAddress.getLocalHost().getHostAddress());
			}
		}
		catch (UnknownHostException | ConnectException e)
		{
			JOptionPane.showMessageDialog(null, "Cannot connect to the server!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Connection error, cannot reach server (sending name or calling RMI method)", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Connection error, unable to receive server's answer!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (ParserConfigurationException | SAXException e)
		{
			JOptionPane.showMessageDialog(null, "Error while starting game!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (NotBoundException e)
		{
			JOptionPane.showMessageDialog(null, "Error while setting up remote methods!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (GameErrorException e)
		{
			JOptionPane.showMessageDialog(null, e.getError(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (AlreadyBoundException e)
		{
			JOptionPane.showMessageDialog(null, "Error: registry name already used!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void startOnlineGameServer(int port, int numberOfPlayers)
	{
		try
		{
			// Get server addresses
			List<String> serverAddresses = new ArrayList<String>();
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			Enumeration<InetAddress> inetAddresses;
			for (NetworkInterface netint : Collections.list(nets))
			{
				inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses))
				{
					serverAddresses.add(inetAddress.toString());
				}
			}

			// Show server addresses in main menu
			this.mainMenu.showServerAddresses(serverAddresses);

			// Create server
			new Server(port, numberOfPlayers);
		}
		catch (InterruptedException | IOException | ParserConfigurationException | SAXException | NotBoundException | IllegalArgumentException e)
		{
			JOptionPane.showMessageDialog(null, "Error while starting server!", "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
		catch (GameErrorException e)
		{
			JOptionPane.showMessageDialog(null, e.getError(), "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
		catch (AlreadyBoundException e)
		{
			JOptionPane.showMessageDialog(null, "RMI registry already bound!", "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Generic error!", "Error", JOptionPane.ERROR_MESSAGE);
			Main.LOGGER.log(Level.SEVERE, "Error", e);
			e.printStackTrace();
		}
	}
}
