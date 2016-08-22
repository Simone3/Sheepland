package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.model.PlayerColor;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Server extends Observable
{
	private Map<ClientConnectionThread, String> clientConnectionThreads;

	private ServerSocket serverSocket;
	private ExecutorService executor;
	private CountDownLatch waitPlayersReady;
	private CountDownLatch waitTurnSchedulerReady;

	private TurnScheduler turnScheduler;

	public Server(int port, int numberOfPlayers) throws IOException, InterruptedException, GameErrorException, ParserConfigurationException, SAXException, NotBoundException, IllegalArgumentException, AlreadyBoundException
	{
		// Create server socket
		this.serverSocket = new ServerSocket(port);

		// Create array of client's sockets
		this.clientConnectionThreads = new HashMap<ClientConnectionThread, String>();

		// Create executor for socket threads
		this.executor = Executors.newFixedThreadPool(numberOfPlayers);

		// Create a countdown to synchronize threads
		this.waitPlayersReady = new CountDownLatch(numberOfPlayers);
		this.waitTurnSchedulerReady = new CountDownLatch(1);

		// Wait for players
		this.waitForPlayers(numberOfPlayers);
	}

	// Wait for clients
	private void waitForPlayers(int numberOfPlayers) throws IOException, InterruptedException, GameErrorException, ParserConfigurationException, SAXException, NotBoundException, IllegalArgumentException, AlreadyBoundException
	{
		// Wait for players and create a connection thread for each one
		for (int i = 0; i < numberOfPlayers; i++)
		{
			// Get connected Client socket
			Socket clientSocket = this.serverSocket.accept();

			// Execute thread to manage connection
			ClientConnectionThread thread = new ClientConnectionThread(clientSocket, i);
			this.executor.execute(thread);
		}

		// Wait for all players to be ready to play
		this.waitPlayersReady.await();

		// When all players are connected, start game by creating the main controller class
		List<PlayerColor> allGameColors = Arrays.asList(PlayerColor.values());
		List<PlayerColor> thisGameColors = allGameColors.subList(0, this.clientConnectionThreads.size());
		List<String> playerNames = new ArrayList<String>();
		playerNames.addAll(this.clientConnectionThreads.values());

		this.turnScheduler = new TurnScheduler(false, playerNames, thisGameColors, null);

		// In an online match, the TurnScheduler observes this class to get the user events
		this.addObserver(this.turnScheduler);

		// The turn scheduler is ready: send a notification to each thread
		this.waitTurnSchedulerReady.countDown();
	}

	// Thread for each player that receives event through socket
	private class ClientConnectionThread implements Runnable
	{
		private Socket socket;
		private ObjectInputStream input;
		private ObjectOutputStream output;
		private int clientNumber;

		public ClientConnectionThread(Socket clientSocket, int clientNumber) throws IOException
		{
			this.socket = clientSocket;
			this.clientNumber = clientNumber;

			// Input/output
			OutputStream out = this.socket.getOutputStream();
			this.output = new ObjectOutputStream(out);
			InputStream in = this.socket.getInputStream();
			this.input = new ObjectInputStream(in);
		}

		@Override
		public void run()
		{
			try
			{
				// First thing is to wait for the player's name
				Object playerName = null;
				boolean nameIsValid = false;
				while (!nameIsValid)
				{
					playerName = this.input.readObject();
					if (!(playerName instanceof String))
					{
						continue;
					}

					// Check if name isn't already taken
					Collection<String> names = Server.this.clientConnectionThreads.values();
					if (playerName != null && !("".equals(playerName)) && !names.contains(playerName))
					{
						nameIsValid = true;
					}

					// Send client a notification
					this.output.writeObject(Boolean.valueOf(nameIsValid));
					this.output.flush();
				}

				// Save player name, linking this thread with it
				Server.this.clientConnectionThreads.put(this, (String) playerName);

				// Signal server that this client is ready
				Server.this.waitPlayersReady.countDown();

				// Wait for the turn scheduler to be ready
				Server.this.waitTurnSchedulerReady.await();

				// Send client a notification (with small delay to avoid multiple window creation at once in "local online" games)
				Thread.sleep(this.clientNumber * 500);
				this.output.writeObject(Boolean.valueOf(true));
				this.output.flush();

				// Loop to get client choice during the game
				while (!Server.this.turnScheduler.isGameOver())
				{
					// Get client's input
					Object clientEvent = this.input.readObject();

					// Get current player's socket thread
					String currentPlayerName = Server.this.turnScheduler.getGameState().getCurrentPlayer().getName();
					ClientConnectionThread currentPlayerThread = null;
					for (Map.Entry<ClientConnectionThread, String> entry : Server.this.clientConnectionThreads.entrySet())
					{
						if (entry.getValue().equals(currentPlayerName))
						{
							currentPlayerThread = entry.getKey();
							break;
						}
					}

					// Send event to turn scheduler only if this thread is the current player's one
					if (this.equals(currentPlayerThread))
					{
						// Notify turn scheduler
						Server.this.setChanged();
						Server.this.notifyObservers(clientEvent);
						Server.this.clearChanged();
					}
				}
			}
			catch (ClassNotFoundException | IOException | InterruptedException e)
			{
				turnScheduler.manageGameError("Error while receiving event from client!", e);
			}
			finally
			{
				// Close Socket
				try
				{
					this.socket.close();
				}
				catch (IOException e)
				{
					// Error while closing socket in "finally" block, not much I can do...
				}

				// Close threads
				Server.this.executor.shutdownNow();
			}
		}
	}
}
