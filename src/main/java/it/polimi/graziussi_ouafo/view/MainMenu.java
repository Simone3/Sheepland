package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.main.Main;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.PlayerColor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MainMenu extends JFrame
{
	private static final long serialVersionUID = 1L;
	private final Main main;

	private BorderLayout frameLayout = new BorderLayout();

	private ExecutorService actionExecutor = Executors.newSingleThreadExecutor();

	public MainMenu(Main main)
	{
		super("Sheepland");
		this.main = main;

		// Create layout for the frame
		MainMenu.this.setLayout(this.frameLayout);

		// Frame settings
		MainMenu.this.setSize(DimensionConstants.MAIN_MENU.getValue());
		MainMenu.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Title
		String sd = ResourcePaths.LOGO.getValue();
		URL imagePath = this.getClass().getResource(sd);
		JLabel titleLabel = new JLabel(new ImageIcon(imagePath));
		MainMenu.this.add(titleLabel, BorderLayout.NORTH);

		// Side bars
		JPanel sidebar = new JPanel();
		sidebar.setPreferredSize(DimensionConstants.MENU_SIDE_BAR.getValue());
		MainMenu.this.add(sidebar, BorderLayout.EAST);
		sidebar = new JPanel();
		sidebar.setPreferredSize(DimensionConstants.MENU_SIDE_BAR.getValue());
		MainMenu.this.add(sidebar, BorderLayout.WEST);

		// Finally, set the frame visible
		MainMenu.this.setVisible(true);
	}

	// Shows first menu, in which the user selects the game mode
	public void initGameModeSelection()
	{
		List<JButton> options;
		JPanel optionsPanel;

		// Remove central content, if any
		Component centralComponent = this.frameLayout.getLayoutComponent(BorderLayout.CENTER);
		if (centralComponent != null)
		{
			MainMenu.this.remove(centralComponent);
		}

		// Create option buttons
		options = new ArrayList<JButton>();
		JButton offlineButton = new JButton("OFFLINE");
		offlineButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				// Show player selection
				MainMenu.this.showOfflinePlayerSelection();
			}
		});
		options.add(offlineButton);
		JButton onlineButton = new JButton("ONLINE");
		onlineButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				// Show choice between client or server
				MainMenu.this.showOnlineModeChoice();
			}
		});
		options.add(onlineButton);

		// Option panel
		optionsPanel = new JPanel();
		optionsPanel.setSize(100, 100);
		optionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridLayout layoutManager = new GridLayout(options.size(), 1);
		layoutManager.setHgap(10);
		layoutManager.setVgap(10);
		optionsPanel.setLayout(layoutManager);

		// Add buttons to panel
		Iterator<JButton> itr = options.iterator();
		while (itr.hasNext())
		{
			optionsPanel.add(itr.next());
		}

		// Add options to the frame
		MainMenu.this.add(optionsPanel, BorderLayout.CENTER);
	}

	// To insert all players info in a single player match
	private void showOfflinePlayerSelection()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Remove content, if any
				MainMenu.this.emptyFrame();

				// Player choice panel
				final JPanel playerSelectionPanel = new JPanel();
				playerSelectionPanel.setOpaque(false);
				playerSelectionPanel.setLayout(new GridLayout(MiscConstants.MAX_PLAYERS.getValue() + 2, 1));

				// Info label
				TextArea infoLabel = new TextArea("Choose player colors and names. Remember: Sheepland can be played in " + MiscConstants.MIN_PLAYERS.getValue() + "-" + MiscConstants.MAX_PLAYERS.getValue() + " players, so you can leave some fields empty.", new Dimension(0, 0));
				playerSelectionPanel.add(infoLabel.getComponent());

				// Create player fields
				final List<JTextField> playerNameFields = new ArrayList<JTextField>();
				final List<JComboBox<PlayerColor>> colorChoices = new ArrayList<JComboBox<PlayerColor>>();
				for (int i = 0; i < MiscConstants.MAX_PLAYERS.getValue(); i++)
				{
					JPanel playerPanel = new JPanel();

					// Player name
					JTextField playerName = new JTextField("", 15);
					playerNameFields.add(playerName);

					// Player color
					JComboBox<PlayerColor> colorChoice = new JComboBox<PlayerColor>(PlayerColor.values());
					colorChoice.setSelectedItem(null);
					colorChoices.add(colorChoice);

					// Add components
					playerPanel.add(new JLabel("Player " + i + ": "));
					playerPanel.add(colorChoice);
					playerPanel.add(playerName);

					// Add panel
					playerSelectionPanel.add(playerPanel);
				}

				// Confirm button
				JButton confirmButton = new JButton("Confirm");
				playerSelectionPanel.add(confirmButton);
				confirmButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent evt)
					{
						Iterator<JTextField> nameItr = playerNameFields.iterator();
						Iterator<JComboBox<PlayerColor>> colorItr = colorChoices.iterator();

						final List<String> playerNames = new ArrayList<String>();
						final List<PlayerColor> playerColors = new ArrayList<PlayerColor>();

						while (nameItr.hasNext() && colorItr.hasNext())
						{
							JTextField nameField = nameItr.next();
							JComboBox<PlayerColor> colorBox = colorItr.next();

							if (nameField != null && colorBox != null)
							{
								String name = nameField.getText();
								PlayerColor color = (PlayerColor) colorBox.getSelectedItem();

								if (!"".equals(name) && color != null)
								{
									if (!playerNames.contains(name) && !playerColors.contains(color))
									{
										playerNames.add(name);
										playerColors.add(color);
									}
									else
									{
										JOptionPane.showMessageDialog(null, "Color and names must be unique!", "Error", JOptionPane.ERROR_MESSAGE);
										return;
									}
								}
							}
						}

						// Check if enough data has been given
						if (playerNames.size() >= MiscConstants.MIN_PLAYERS.getValue())
						{
							MainMenu.this.actionExecutor.submit(new Runnable()
							{
								@Override
								public void run()
								{
									// Start game
									MainMenu.this.main.startOfflineGame(playerNames, playerColors);
								}
							});
						}
						else
						{
							// Error
							JOptionPane.showMessageDialog(null, "You must insert at least " + MiscConstants.MIN_PLAYERS.getValue() + " players!", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				});

				// Add form to the frame
				MainMenu.this.add(playerSelectionPanel, BorderLayout.CENTER);

				// Fit new content in frame
				MainMenu.this.revalidate();
			}
		});
	}

	private void showOnlineModeChoice()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Remove content, if any
				Component component = MainMenu.this.frameLayout.getLayoutComponent(BorderLayout.CENTER);
				if (component != null)
				{
					MainMenu.this.remove(component);
				}

				// Game mode choice panel
				final JPanel modeSelectionPanel = new JPanel();
				modeSelectionPanel.setLayout(new GridLayout(3, 1));

				// Info area
				TextArea infoLabel = new TextArea("\n\nChoose if you want to start the server or a client:", new Dimension(0, 0));
				modeSelectionPanel.add(infoLabel.getComponent());

				// Create option buttons
				List<JButton> options = new ArrayList<JButton>();
				JButton serverButton = new JButton("SERVER");
				serverButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent evt)
					{
						// Ask how many players
						List<String> optionList = new ArrayList<String>();
						for (int i = MiscConstants.MIN_PLAYERS.getValue(); i <= MiscConstants.MAX_PLAYERS.getValue(); i++)
						{
							optionList.add(i + "");
						}
						String[] options = optionList.toArray(new String[optionList.size()]);
						JPanel panel = new JPanel();
						JLabel label = new JLabel("How many players this match?");
						panel.add(label);
						int result = JOptionPane.showOptionDialog(null, panel, "Shepherd choice", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						Integer selectedOption = Integer.valueOf(result + MiscConstants.MIN_PLAYERS.getValue());
						if (selectedOption == null || selectedOption < MiscConstants.MIN_PLAYERS.getValue())
						{
							selectedOption = Integer.valueOf(MiscConstants.MIN_PLAYERS.getValue());
						}
						final int numberOfPlayers = selectedOption.intValue();
						MainMenu.this.actionExecutor.submit(new Runnable()
						{
							@Override
							public void run()
							{
								// Start server
								MainMenu.this.main.startOnlineGameServer(MiscConstants.SERVER_PORT.getValue(), numberOfPlayers);
							}
						});
					}
				});
				options.add(serverButton);
				JButton clientButton = new JButton("CLIENT");
				clientButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent evt)
					{
						MainMenu.this.showOnlineClientInfoSelection();
					}
				});
				options.add(clientButton);

				// Option panel
				JPanel optionsPanel = new JPanel();
				optionsPanel.setSize(100, 100);
				optionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
				GridLayout layoutManager = new GridLayout(options.size(), 1);
				layoutManager.setHgap(10);
				layoutManager.setVgap(10);
				optionsPanel.setLayout(layoutManager);

				// Add buttons to panel
				Iterator<JButton> itr = options.iterator();
				while (itr.hasNext())
				{
					optionsPanel.add(itr.next());
				}

				// Add form to the frame
				modeSelectionPanel.add(optionsPanel);
				MainMenu.this.add(modeSelectionPanel, BorderLayout.CENTER);

				// Fit new content in frame
				MainMenu.this.revalidate();
			}
		});
	}

	protected void showOnlineClientInfoSelection()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Remove content, if any
				MainMenu.this.emptyFrame();

				// Player info panel
				final JPanel clientInfoPanel = new JPanel();
				clientInfoPanel.setLayout(new GridLayout(4, 1));

				// Info label
				TextArea infoLabel = new TextArea("\n\nInsert your name and the server you want to connect to:", new Dimension(0, 0));
				clientInfoPanel.add(infoLabel.getComponent());

				// Create player field
				JPanel namePanel = new JPanel();
				JLabel label = new JLabel("Name: ");
				final JTextField playerName = new JTextField("", 15);
				namePanel.add(label);
				namePanel.add(playerName);
				clientInfoPanel.add(namePanel);

				// Create server address panel
				JPanel serverPanel = new JPanel();
				JLabel serverLabel = new JLabel("Server IP address: ");
				final JTextField serverAddress = new JTextField("", 15);
				serverPanel.add(serverLabel);
				serverPanel.add(serverAddress);
				clientInfoPanel.add(serverPanel);

				// Confirm button
				JButton confirmButton = new JButton("Confirm");
				clientInfoPanel.add(confirmButton);
				confirmButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent evt)
					{
						if (playerName == null || "".equals(playerName.getText()))
						{
							JOptionPane.showMessageDialog(null, "Name must have a value!", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						if (serverAddress == null || "".equals(serverAddress.getText()))
						{
							JOptionPane.showMessageDialog(null, "You must enter a valid IP address!", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						MainMenu.this.actionExecutor.submit(new Runnable()
						{
							@Override
							public void run()
							{
								// Start game
								MainMenu.this.main.startOnlineGameClient(serverAddress.getText(), playerName.getText());
							}
						});
					}
				});

				// Add form to the frame
				MainMenu.this.add(clientInfoPanel, BorderLayout.CENTER);

				// Fit new content in frame
				MainMenu.this.revalidate();
			}
		});
	}

	public void showServerAddresses(final List<String> serverAddresses)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Remove content, if any
				MainMenu.this.emptyFrame();

				// Server info panel
				final JPanel serverInfoPanel = new JPanel();
				serverInfoPanel.setLayout(new GridLayout(1, 1));

				// Text
				TextArea infoLabel = new TextArea("\nServer running! Server addresses:\n", new Dimension(0, 0));
				Iterator<String> itr = serverAddresses.iterator();
				String currentAddress;
				while (itr.hasNext())
				{
					currentAddress = itr.next();
					if (currentAddress != null && !"".equals(currentAddress))
					{
						infoLabel.appendText("\n" + currentAddress);
					}
				}
				serverInfoPanel.add(infoLabel.getComponent());

				// Add info to the frame
				MainMenu.this.add(serverInfoPanel, BorderLayout.CENTER);

				// Fit new content in frame
				MainMenu.this.revalidate();
				MainMenu.this.repaint();
			}
		});
	}

	public void showWaitingMessage()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Remove content, if any
				MainMenu.this.emptyFrame();

				// Info panel
				final JPanel infoPanel = new JPanel();
				infoPanel.setLayout(new GridLayout(1, 1));

				// Text
				TextArea infoLabel = new TextArea("\n\n\n\n\nWaiting for other players...", new Dimension(0, 0));
				infoPanel.add(infoLabel.getComponent());

				// Add info to the frame
				MainMenu.this.add(infoPanel, BorderLayout.CENTER);

				// Fit new content in frame
				MainMenu.this.revalidate();
				MainMenu.this.repaint();
			}
		});
	}

	private void emptyFrame()
	{
		Component component;

		component = this.frameLayout.getLayoutComponent(BorderLayout.CENTER);
		if (component != null)
		{
			this.remove(component);
		}

		component = this.frameLayout.getLayoutComponent(BorderLayout.EAST);
		if (component != null)
		{
			this.remove(component);
		}

		component = this.frameLayout.getLayoutComponent(BorderLayout.WEST);
		if (component != null)
		{
			this.remove(component);
		}

		component = this.frameLayout.getLayoutComponent(BorderLayout.SOUTH);
		if (component != null)
		{
			this.remove(component);
		}
	}
}
