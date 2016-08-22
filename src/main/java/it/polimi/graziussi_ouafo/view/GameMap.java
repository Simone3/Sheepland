package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.controller.ActionStrategy;
import it.polimi.graziussi_ouafo.events.RoadEvent;
import it.polimi.graziussi_ouafo.events.SheepEvent;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.Road;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameMap extends Observable implements GameMapRemoteInterface, Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int TIMED_MESSAGE_TIME = 2000;
	private static final Point TIMED_MESSAGE_POSITION = new Point(470, 40);
	private static final Point DIE_RESULT_MESSAGE_POSITION = new Point(890, 40);
	private static final Point NEW_PLAYER_MESSAGE_POSITION = new Point(600, 40);

	private RoadActionHandler roadActionHandler = new RoadActionHandler();
	private SheepActionHandler sheepActionHandler = new SheepActionHandler();

	private Map<Integer, MapPanel> roadPanels;
	private Map<Integer, MapPanel> whiteSheepPanels;
	private MapPanel blackSheepPanel;
	private MapPanel wolfPanel;

	private Map<Integer, Point> blackSheepPositions;
	private Map<Integer, Point> wolfPositions;

	private JLayeredPane layeredPane;

	private ExecutorService actionExecutor = Executors.newSingleThreadExecutor();

	public GameMap()
	{
		this.layeredPane = new JLayeredPane();

		// Maps of placed labels/coordinates for labels
		this.roadPanels = new HashMap<Integer, MapPanel>();
		this.whiteSheepPanels = new HashMap<Integer, MapPanel>();
		this.blackSheepPositions = new HashMap<Integer, Point>();
		this.wolfPositions = new HashMap<Integer, Point>();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Dimension imageSize = DimensionConstants.MAP.getValue();
				Dimension paneSize = DimensionConstants.SCREEN_CENTER.getValue();

				// JLayeredPane settings
				GameMap.this.layeredPane.setSize(paneSize);
				GameMap.this.layeredPane.setBounds(0, 0, (int) paneSize.getWidth(), (int) paneSize.getHeight());

				// Background image
				URL imagePath = this.getClass().getResource(ResourcePaths.MAP_BACKGROUND.getValue());
				ImageIcon mapImg = new ImageIcon(imagePath);
				JLabel mapLabel = new JLabel(mapImg);
				mapLabel.setSize(imageSize);
				mapLabel.setBounds((int) (paneSize.getWidth() - imageSize.getWidth()) / 2, (int) (paneSize.getHeight() - imageSize.getHeight()) / 2, (int) imageSize.getWidth(), (int) imageSize.getHeight());

				// Add background image (Integer(0) -> place to back)
				GameMap.this.layeredPane.add(mapLabel, Integer.valueOf(0), 0);
			}
		});
	}

	@Override
	public JLayeredPane getSwingComponent()
	{
		return this.layeredPane;
	}

	/**** METHODS TO ADD COMPONENTS TO THE MAP ****/

	public void addRoadPanel(final int roadID, final Point position)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			// Create panel
			MapPanel newPanel = new MapPanel(ResourcePaths.EMPTY_ROAD.getValue(), position, DimensionConstants.ROAD.getValue(), "", "");

			@Override
			public void run()
			{
				// Save created panel
				GameMap.this.roadPanels.put(Integer.valueOf(roadID), this.newPanel);

				// Add position panel (Integer(1) -> place over the background image)
				GameMap.this.layeredPane.add(this.newPanel, Integer.valueOf(1), 0);
			}
		});
	}

	public void addWhiteSheepPanel(final int regionID, final Point position) throws IOException
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Create panel
				MapPanel newPanel = new MapPanel(ResourcePaths.WHITE_SHEEP.getValue(), position, DimensionConstants.WHITE_SHEEP.getValue(), "" + MiscConstants.STARTING_SHEEP_PER_REGION.getValue(), "");

				// Save created panel
				GameMap.this.whiteSheepPanels.put(Integer.valueOf(regionID), newPanel);

				// Add position panel (Integer(1) -> place over the background image)
				GameMap.this.layeredPane.add(newPanel, Integer.valueOf(1), 0);
			}
		});
	}

	public void addBlackSheepPanel(final Point position)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Create panel
				GameMap.this.blackSheepPanel = new MapPanel(ResourcePaths.BLACK_SHEEP.getValue(), position, DimensionConstants.BLACK_SHEEP.getValue(), "", "");

				// Add position panel (Integer(1) -> place over the background image)
				GameMap.this.layeredPane.add(GameMap.this.blackSheepPanel, Integer.valueOf(1), 0);
			}
		});
	}

	public void addBlackSheepPosition(int regionID, Point position)
	{
		this.blackSheepPositions.put(Integer.valueOf(regionID), position);
	}

	public void addWolfPanel(final Point position)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Create panel
				GameMap.this.wolfPanel = new MapPanel(ResourcePaths.WOLF.getValue(), position, DimensionConstants.WOLF.getValue(), "", "");

				// Add position panel (Integer(1) -> place over the background image)
				GameMap.this.layeredPane.add(GameMap.this.wolfPanel, Integer.valueOf(1), 0);
			}
		});
	}

	public void addWolfPosition(int regionID, Point position)
	{
		this.wolfPositions.put(Integer.valueOf(regionID), position);
	}

	@Override
	public void showTimedMessage(final String text) throws RemoteException
	{
		MapPanel timedPanel = new MapPanel(ResourcePaths.TIMED_MESSAGE.getValue(), GameMap.TIMED_MESSAGE_POSITION, DimensionConstants.TIMED_MESSAGE.getValue(), text, "");
		this.showTimedPanel(timedPanel);
	}

	@Override
	public void showDieThrow(final int number) throws RemoteException
	{
		String image = ResourcePaths.valueOf("DIE"+number).getValue();
		MapPanel timedPanel = new MapPanel(image, GameMap.DIE_RESULT_MESSAGE_POSITION, DimensionConstants.DIE_RESULT_MESSAGE.getValue(), "", "");
		this.showTimedPanel(timedPanel);
	}

	@Override
	public void showNewPlayerMessage(final String message) throws RemoteException
	{
		MapPanel timedPanel = new MapPanel(ResourcePaths.TIMED_MESSAGE.getValue(), GameMap.NEW_PLAYER_MESSAGE_POSITION, DimensionConstants.TIMED_MESSAGE.getValue(), message, "");
		this.showTimedPanel(timedPanel);
	}

	private void showTimedPanel(final MapPanel panel)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Add message panel (Integer(2) -> place over everything else)
				GameMap.this.layeredPane.add(panel, Integer.valueOf(2), 0);
				GameMap.this.layeredPane.repaint();
				GameMap.this.layeredPane.revalidate();

				// Timer to make it disappear after some time
				Timer timer = new Timer(0, new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						// Hide message
						panel.setVisible(false);
					}
				});
				timer.setRepeats(false);
				timer.setCoalesce(true);
				timer.setInitialDelay(GameMap.TIMED_MESSAGE_TIME);
				timer.start();
			}
		});
	}

	/**** METHODS TO (DE)ACTIVATE COMPONENTS FOR ACTION ON THE MAP ****/

	@Override
	public void activateRoads(final List<Integer> roadIDs, final ActionStrategy strategy)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Iterator<Integer> itr = roadIDs.iterator();
				while (itr.hasNext())
				{
					MapPanel currentPanel = GameMap.this.roadPanels.get(itr.next());

					// Add listener to the roads
					currentPanel.removeMouseListener(GameMap.this.roadActionHandler);
					GameMap.this.roadActionHandler.setStrategy(strategy);
					currentPanel.addMouseListener(GameMap.this.roadActionHandler);

					// Highlight the panel
					currentPanel.highlight(true);
				}
			}
		});
	}

	@Override
	public void activateWhiteSheep(final List<Integer> regionIDs, final ActionStrategy strategy)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Iterator<Integer> itr = regionIDs.iterator();
				while (itr.hasNext())
				{
					MapPanel currentPanel = GameMap.this.whiteSheepPanels.get(itr.next());

					// Add listener to the regions
					currentPanel.removeMouseListener(GameMap.this.sheepActionHandler);
					GameMap.this.sheepActionHandler.setStrategy(strategy);
					currentPanel.addMouseListener(GameMap.this.sheepActionHandler);

					// Highlight the panel
					currentPanel.highlight(true);
				}
			}
		});
	}

	@Override
	public void activateBlackSheep(final ActionStrategy strategy)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Add listener to the region
				GameMap.this.blackSheepPanel.removeMouseListener(GameMap.this.sheepActionHandler);
				GameMap.this.sheepActionHandler.setStrategy(strategy);
				GameMap.this.blackSheepPanel.addMouseListener(GameMap.this.sheepActionHandler);

				// Highlight the panel
				GameMap.this.blackSheepPanel.highlight(true);
			}
		});
	}

	@Override
	public void deactivateAllRoadsAndRegions()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				for (MapPanel panel : GameMap.this.roadPanels.values())
				{
					panel.removeMouseListener(GameMap.this.roadActionHandler);
					panel.highlight(false);
				}

				for (MapPanel panel : GameMap.this.whiteSheepPanels.values())
				{
					panel.removeMouseListener(GameMap.this.sheepActionHandler);
					panel.highlight(false);
				}

				GameMap.this.blackSheepPanel.removeMouseListener(GameMap.this.sheepActionHandler);
				GameMap.this.blackSheepPanel.highlight(false);
			}
		});
	}

	/**** METHODS TO UPDATE COMPONENTS ON THE MAP ****/

	@Override
	public void updateRoadPanel(final Road road)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Get the road panel
				MapPanel panel = GameMap.this.roadPanels.get(Integer.valueOf(road.getId()));

				// Update the panel with the correct graphics
				if (road.hasFence())
				{
					panel.setImage(ResourcePaths.ROAD_FENCE.getValue());
					panel.setText("");
					panel.setMouseOverText(""+road.getNumber());
				}
				else if (road.hasShepherd())
				{
					PlayerColor playerColor = road.getShepherd().getOwner().getColor();

					ResourcePaths colorConstant = ResourcePaths.valueOf("ROAD_SHEPHERD_" + playerColor.toString());
					panel.setImage(colorConstant.getValue());
					panel.setText("" + road.getShepherd().getNumber());
					panel.setMouseOverText(""+road.getNumber());
				}
				else
				{
					panel.setImage(ResourcePaths.EMPTY_ROAD.getValue());
					panel.setText("");
					panel.setMouseOverText("");
				}
			}
		});
	}

	@Override
	public void updateWhiteSheepPanel(final Region region)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Get the white sheep panel
				MapPanel panel = GameMap.this.whiteSheepPanels.get(Integer.valueOf(region.getId()));
				int whiteSheepNumber = region.getWhiteSheepNumber();
				int ramNumber = region.getRamNumber();
				int lambNumber = region.getLambNumber();

				if (whiteSheepNumber > 0 || ramNumber > 0 || lambNumber > 0)
				{
					// If there are sheep, show the panel with the number(s)
					panel.setVisible(true);
					panel.setText("" + (whiteSheepNumber + ramNumber + lambNumber));
					panel.setMouseOverText(whiteSheepNumber + " sheep, " + ramNumber + " ram(s), " + lambNumber + " lamb(s)");
				}
				else
				{
					// If there are no sheep, hide the panel
					panel.setVisible(false);
					panel.setText("");
					panel.setMouseOverText("");
				}
			}
		});
	}

	@Override
	public void moveBlackSheepPanel(final Integer regionTo)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Move (with animation) the black sheep panel over this region
				Point blackSheepPosition = GameMap.this.blackSheepPositions.get(Integer.valueOf(regionTo));
				GameMap.this.blackSheepPanel.animate(blackSheepPosition);
			}
		});
	}

	@Override
	public void moveWolfPanel(final Integer regionTo)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Move (with animation) the wolf panel over this region
				Point wolfPosition = GameMap.this.wolfPositions.get(Integer.valueOf(regionTo));
				GameMap.this.wolfPanel.animate(wolfPosition);
			}
		});
	}

	/**** INNER CLASSES FOR ACTION HANDLING ****/

	private class RoadActionHandler implements MouseListener, Serializable
	{
		private static final long serialVersionUID = 1L;
		private ActionStrategy strategy;

		void setStrategy(ActionStrategy strategy)
		{
			this.strategy = strategy;
		}

		@Override
		public void mouseClicked(final MouseEvent event)
		{
			// Get clicked panel
			final MapPanel clickedPanel = (MapPanel) event.getSource();

			GameMap.this.actionExecutor.submit(new Runnable()
			{
				@Override
				public void run()
				{
					// Check if it was clicked in the non-transparent part
					if (clickedPanel.checkIfClickValid(event.getX(), event.getY()))
					{
						// Loop road panels to find the clicked one
						Integer roadID = null;
						for (Map.Entry<Integer, MapPanel> entry : GameMap.this.roadPanels.entrySet())
						{
							MapPanel panel = entry.getValue();
							if (panel.equals(clickedPanel))
							{
								roadID = entry.getKey();
								break;
							}
						}

						// Send event with the roadID
						GameMap.this.setChanged();
						GameMap.this.notifyObservers(new RoadEvent(roadID, RoadActionHandler.this.strategy));
						GameMap.this.clearChanged();
					}
				}
			});
		}

		@Override
		public void mouseEntered(MouseEvent event)
		{
			// Not used
		}

		@Override
		public void mouseExited(MouseEvent event)
		{
			// Not used
		}

		@Override
		public void mousePressed(MouseEvent event)
		{
			// Not used
		}

		@Override
		public void mouseReleased(MouseEvent event)
		{
			// Not used
		}
	}

	private class SheepActionHandler implements MouseListener, Serializable
	{
		private static final long serialVersionUID = 1L;
		private ActionStrategy strategy;

		void setStrategy(ActionStrategy strategy)
		{
			this.strategy = strategy;
		}

		@Override
		public void mouseClicked(final MouseEvent event)
		{
			// Get clicked panel
			final MapPanel clickedPanel = (MapPanel) event.getSource();

			GameMap.this.actionExecutor.submit(new Runnable()
			{
				@Override
				public void run()
				{
					// Check if it was clicked in the non-transparent part
					if (clickedPanel.checkIfClickValid(event.getX(), event.getY()))
					{
						// Check if the clickedPanel is the black sheep one
						boolean blackSheep = false;
						Integer fromRegionID = null;
						if (clickedPanel.equals(GameMap.this.blackSheepPanel))
						{
							blackSheep = true;
							fromRegionID = null;
						}
						else
						{
							// Loop white sheep panels to find the clicked one
							for (Map.Entry<Integer, MapPanel> entry : GameMap.this.whiteSheepPanels.entrySet())
							{
								MapPanel panel = entry.getValue();
								if (panel.equals(clickedPanel))
								{
									blackSheep = false;
									fromRegionID = entry.getKey();
									break;
								}
							}
						}

						// Send event with the fromRegionID
						GameMap.this.setChanged();
						GameMap.this.notifyObservers(new SheepEvent(fromRegionID, blackSheep, SheepActionHandler.this.strategy));
						GameMap.this.clearChanged();
					}
				}
			});
		}

		@Override
		public void mouseEntered(MouseEvent event)
		{
			// Not used
		}

		@Override
		public void mouseExited(MouseEvent event)
		{
			// Not used
		}

		@Override
		public void mousePressed(MouseEvent event)
		{
			// Not used
		}

		@Override
		public void mouseReleased(MouseEvent event)
		{
			// Not used
		}
	}
}
