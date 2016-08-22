package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.controller.SelectActionBuyTerrainTile;
import it.polimi.graziussi_ouafo.controller.SelectActionMating;
import it.polimi.graziussi_ouafo.controller.SelectActionMoveSheep;
import it.polimi.graziussi_ouafo.controller.SelectActionMoveShepherd;
import it.polimi.graziussi_ouafo.controller.SelectActionShooting;
import it.polimi.graziussi_ouafo.events.ChooseShepherdEvent;
import it.polimi.graziussi_ouafo.events.DeckEvent;
import it.polimi.graziussi_ouafo.events.SelectActionEvent;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.TerrainTileDeck;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class GameWindow extends Observable implements GameWindowRemoteInterface
{
	private static final long serialVersionUID = 1L;

	private GameMapRemoteInterface gameMap;

	private BorderLayout layoutManager;

	private TextArea currentPlayerReminder;

	private JPanel gameInfo;

	private JPanel selectActionPanel;
	private List<SelectActionButton> selectActionButtons;

	private Map<RegionType, JButton> deckButtons;

	private SelectActionHandler selectActionHandler = new SelectActionHandler();

	private DeckActionHandler deckActionHandler = new DeckActionHandler();

	private JFrame windowFrame;

	private ExecutorService actionExecutor = Executors.newSingleThreadExecutor();

	public GameWindow(GameMap gameMap) throws IOException, GameErrorException
	{
		this.windowFrame = new JFrame("Sheepland");

		this.gameMap = gameMap;

		// Create layout for the frame
		this.layoutManager = new BorderLayout();
		this.layoutManager.setHgap(2);
		this.layoutManager.setVgap(2);
		GameWindow.this.windowFrame.setLayout(this.layoutManager);

		// Frame settings
		GameWindow.this.windowFrame.setSize(DimensionConstants.WINDOW.getValue());
		GameWindow.this.windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create deck buttons for all regions but Sheepsburg
		this.deckButtons = new HashMap<RegionType, JButton>();
		RegionType[] allDeckTypes = RegionType.getDeckTypes();
		String buttonText = "" + MiscConstants.STARTING_TERRAIN_TILE_PRICE.getValue();
		for (int i = 0; i < allDeckTypes.length; i++)
		{
			URL imagePath = this.getClass().getResource(ResourcePaths.valueOf("DECK_" + allDeckTypes[i].name()).getValue());
			if (imagePath == null)
			{
				throw new GameErrorException("Cannot load deck image!");
			}
			JButton button = new JButton(buttonText, new ImageIcon(imagePath));

			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setVerticalTextPosition(SwingConstants.CENTER);
			button.addActionListener(this.deckActionHandler);
			button.setEnabled(false);
			this.deckButtons.put(allDeckTypes[i], button);
		}

		// Create the select action buttons
		GameWindow.this.initializeSelectActionButtons();

		// Add the game map
		GameWindow.this.windowFrame.add(GameWindow.this.gameMap.getSwingComponent(), BorderLayout.CENTER);

		// Add a reminder about who is the current player (for now empty)
		this.currentPlayerReminder = new TextArea("Sheepland", DimensionConstants.PLAYER_INFO.getValue());
		GameWindow.this.windowFrame.add(this.currentPlayerReminder.getComponent(), BorderLayout.NORTH);

		// Add buttons to select an action and the available actions count down
		this.selectActionPanel = new JPanel();
		this.selectActionPanel.setLayout(new GridLayout(1, this.selectActionButtons.size() + 1));
		Iterator<SelectActionButton> itr = this.selectActionButtons.iterator();
		while (itr.hasNext())
		{
			SelectActionButton currentButton = itr.next();
			currentButton.addActionListener(this.selectActionHandler);
			currentButton.setEnabled(false);
			this.selectActionPanel.add(currentButton);
		}
		GameWindow.this.windowFrame.add(this.selectActionPanel, BorderLayout.SOUTH);

		// Add buttons to buy a terrain tile
		JPanel decksPanel = new JPanel();
		decksPanel.setSize(DimensionConstants.DECK_BUTTONS.getValue());
		decksPanel.setPreferredSize(DimensionConstants.DECK_BUTTONS.getValue());
		decksPanel.setLayout(new GridLayout(this.deckButtons.size(), 1));
		for (JButton button : this.deckButtons.values())
		{
			decksPanel.add(button);
		}
		GameWindow.this.windowFrame.add(decksPanel, BorderLayout.WEST);

		// Add some info in the right panel
		this.gameInfo = new JPanel();
		GameWindow.this.windowFrame.add(this.gameInfo, BorderLayout.EAST);

		// Finally, set the frame visible
		GameWindow.this.windowFrame.setVisible(true);
	}

	private void initializeSelectActionButtons()
	{
		// Available buttons
		SelectActionButton[] buttons = new SelectActionButton[] { new SelectActionButton("Move Sheep", new SelectActionMoveSheep()), new SelectActionButton("Move Shepherd", new SelectActionMoveShepherd()), new SelectActionButton("Buy Terrain Tile", new SelectActionBuyTerrainTile()), new SelectActionButton("Shooting", new SelectActionShooting()), new SelectActionButton("Mating", new SelectActionMating()) };
		this.selectActionButtons = Arrays.asList(buttons);
	}

	@Override
	public GameMapRemoteInterface getGameMap()
	{
		return this.gameMap;
	}

	/**** METHODS TO (DE)ACTIVATE COMPONENTS ON THE WINDOW ****/

	@Override
	public void setSelectActionButtonsVisibility(final boolean visibility)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Iterator<SelectActionButton> itr = GameWindow.this.selectActionButtons.iterator();
				while (itr.hasNext())
				{
					SelectActionButton currentButton = itr.next();
					currentButton.setEnabled(visibility);
				}
			}
		});
	}

	@Override
	public void activateAvailableDecks(final List<RegionType> availableDeckTypes, final ActionStrategy strategy)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Iterator<RegionType> itr = availableDeckTypes.iterator();
				while (itr.hasNext())
				{
					JButton currentButton = GameWindow.this.deckButtons.get(itr.next());
					currentButton.removeActionListener(GameWindow.this.deckActionHandler);
					GameWindow.this.deckActionHandler.setStrategy(strategy);
					currentButton.addActionListener(GameWindow.this.deckActionHandler);
					currentButton.setEnabled(true);
				}
			}
		});
	}

	@Override
	public void deactivateAllActionComponents() throws InvocationTargetException, InterruptedException, RemoteException
	{
		// Remove highlight from items in map
		this.gameMap.deactivateAllRoadsAndRegions();

		// Set terrain tile buttons unclickable
		for (JButton button : this.deckButtons.values())
		{
			button.setEnabled(false);
		}
	}

	/**** METHODS TO UPDATE INFORMATION ON WINDOW ****/

	@Override
	public void updateDeckButton(final TerrainTileDeck deckToUpdate)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JButton currentButton = GameWindow.this.deckButtons.get(deckToUpdate.getType());

				int price = deckToUpdate.getCurrentCardPrice();
				String toWrite = (price <= MiscConstants.MAX_TERRAIN_TILE_PRICE.getValue()) ? "" + price : "X";
				currentButton.setText(toWrite);
			}
		});
	}

	@Override
	public void updateGameInfo(final String currentPlayerMessage, final Color currentPlayerColor, final int playerDinars, final int availableNormalFences, final Map<RegionType, Integer> boughtTerrainTiles, final String availableActionsMessage)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Update player reminder
				GameWindow.this.updateCurrentPlayerInfo(currentPlayerMessage, currentPlayerColor);

				// Delete all components in the game info panel
				GameWindow.this.gameInfo.removeAll();

				// Text area for the info + add dinar message
				TextArea textArea = new TextArea("\n\n\n\nYour dinars: " + playerDinars, DimensionConstants.GAME_INFO.getValue());

				// Add fence message
				String fenceMessage = (availableNormalFences > 0) ? "Fences until the end of game: " + availableNormalFences : "Final phase of the game! When all players complete their turn in this round the game will be over!";
				textArea.appendText("\n\n\n" + fenceMessage);

				// Add bought cards message
				textArea.appendText("\n\n\nBought terrain tiles:");
				for (Map.Entry<RegionType, Integer> entry : boughtTerrainTiles.entrySet())
				{
					RegionType regionType = entry.getKey();
					Integer quantity = entry.getValue();
					textArea.appendText("\n" + regionType.name() + ": " + quantity.intValue());
				}

				// Available actions
				textArea.appendText("\n\n\n" + availableActionsMessage, currentPlayerColor);
				
				// Add area to the panel
				gameInfo.add(textArea.getComponent());
				
				// Repaint frame
				GameWindow.this.windowFrame.revalidate();
				GameWindow.this.windowFrame.repaint();
			}
		});
	}

	@Override
	public void updateCurrentPlayerInfo(final String message, final Color color)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				GameWindow.this.currentPlayerReminder.setText(message, color);
			}
		});
	}

	// Shows the final stats of the game. Requires the ordered array of the players (= first player is the winner)
	@Override
	public void showEndOfGameInfo(final Player[] finalChart)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Delete everything from game panel
				GameWindow.this.emptyFrame();

				// Create text area
				TextArea area = new TextArea("", DimensionConstants.WINDOW.getValue());

				// Add game winner info
				area.appendText("\n\n" + finalChart[0].getName() + " wins the game with " + finalChart[0].getDinars() + " dinars!", finalChart[0].getColor().getColor());

				// Add other players info
				area.appendText("\n\nOther players:");
				for (int i = 1; i < finalChart.length; i++)
				{
					area.appendText("\n" + (i + 1) + ") " + finalChart[i].getName() + ", " + finalChart[i].getDinars() + " dinars");
				}

				// Add text area to the frame
				GameWindow.this.windowFrame.add(area.getComponent(), BorderLayout.CENTER);
				GameWindow.this.windowFrame.revalidate();
				GameWindow.this.windowFrame.repaint();
			}
		});
	}

	/**** MISC METHODS ****/

	@Override
	public void showShepherdChoiceOptions(final int shepherdsPerPlayer)
	{
		Integer selectedOption = null;

		String[] options = new String[shepherdsPerPlayer];
		for (int i = 0; i < shepherdsPerPlayer; i++)
		{
			options[i] = i + "";
		}

		JPanel panel = new JPanel();
		JLabel label = new JLabel("Choose which shepherd you want to use this turn:");
		panel.add(label);
		int result = JOptionPane.showOptionDialog(null, panel, "Shepherd choice", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		selectedOption = Integer.valueOf(result);
		if (selectedOption == null)
		{
			selectedOption = Integer.valueOf(0);
		}

		GameWindow.this.setChanged();
		GameWindow.this.notifyObservers(new ChooseShepherdEvent(selectedOption));
		GameWindow.this.clearChanged();
	}

	private void emptyFrame()
	{
		Component component;

		component = this.layoutManager.getLayoutComponent(BorderLayout.CENTER);
		if (component != null)
		{
			this.windowFrame.remove(component);
		}

		component = this.layoutManager.getLayoutComponent(BorderLayout.EAST);
		if (component != null)
		{
			this.windowFrame.remove(component);
		}

		component = this.layoutManager.getLayoutComponent(BorderLayout.WEST);
		if (component != null)
		{
			this.windowFrame.remove(component);
		}

		component = this.layoutManager.getLayoutComponent(BorderLayout.SOUTH);
		if (component != null)
		{
			this.windowFrame.remove(component);
		}

		component = this.layoutManager.getLayoutComponent(BorderLayout.NORTH);
		if (component != null)
		{
			this.windowFrame.remove(component);
		}
	}

	@Override
	public void showPopup(String title, String message)
	{
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

	/**** INNER CLASSES FOR ACTION HANDLING ****/

	private class DeckActionHandler implements ActionListener, Serializable
	{
		private static final long serialVersionUID = 1L;
		private ActionStrategy strategy;

		void setStrategy(ActionStrategy strategy)
		{
			this.strategy = strategy;
		}

		@Override
		public void actionPerformed(final ActionEvent event)
		{
			final JButton clickedButton = (JButton) event.getSource();

			GameWindow.this.actionExecutor.submit(new Runnable()
			{
				@Override
				public void run()
				{
					// Loop deck buttons to find the clicked one
					RegionType deckType = null;
					for (Map.Entry<RegionType, JButton> entry : GameWindow.this.deckButtons.entrySet())
					{
						JButton button = entry.getValue();
						if (button.equals(clickedButton))
						{
							deckType = entry.getKey();
							break;
						}
					}

					// Send event with the deck type
					GameWindow.this.setChanged();
					GameWindow.this.notifyObservers(new DeckEvent(deckType, DeckActionHandler.this.strategy));
					GameWindow.this.clearChanged();
				}
			});
		}
	}

	private class SelectActionHandler implements ActionListener, Serializable
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent event)
		{
			GameWindow.this.actionExecutor.submit(new Runnable()
			{
				@Override
				public void run()
				{
					// Get clicked button
					SelectActionButton clickedButton = (SelectActionButton) event.getSource();

					// Notify observers
					GameWindow.this.setChanged();
					GameWindow.this.notifyObservers(new SelectActionEvent(clickedButton.getSelectActionStrategy()));
					GameWindow.this.clearChanged();
				}
			});

		}
	}
}
