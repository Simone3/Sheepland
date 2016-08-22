package it.polimi.graziussi_ouafo.model;

import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MiscConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

public class GameState
{
	private final boolean offlineGame;
	private boolean gameStarted = false;

	private final Player[] playerList;
	private Player currentPlayer;
	private PlayerTurn currentTurn;
	private int availableNormalFences;
	private final List<TerrainTileDeck> terrainTileDecks;
	private boolean finalPhase;
	private final Graph<Road, DefaultEdge> roadGraph;

	public GameState(boolean offline, Player[] players, List<TerrainTileDeck> decks, Graph<Road, DefaultEdge> roadGraph)
	{
		this.offlineGame = offline;
		this.playerList = Arrays.copyOf(players, players.length);
		this.availableNormalFences = MiscConstants.STARTING_NORMAL_FENCES.getValue();
		this.terrainTileDecks = decks;
		this.finalPhase = false;
		this.roadGraph = roadGraph;
	}

	public void setGameStarted()
	{
		this.gameStarted = true;
	}

	public boolean isGameStarted()
	{
		return this.gameStarted;
	}

	public boolean isOffline()
	{
		return this.offlineGame;
	}

	public int getNumberOfPlayers()
	{
		return this.playerList.length;
	}

	public Player getCurrentPlayer()
	{
		return this.currentPlayer;
	}

	public void setCurrentPlayer(Player currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}

	public PlayerTurn getCurrentTurn()
	{
		return this.currentTurn;
	}

	public void setCurrentTurn(PlayerTurn turn)
	{
		this.currentTurn = turn;
	}

	public int getAvailableNormalFences()
	{
		return this.availableNormalFences;
	}

	public void subAvailableNormalFences()
	{
		this.availableNormalFences--;
	}

	public boolean isFinalPhase()
	{
		return this.finalPhase;
	}

	public void startFinalPhase()
	{
		this.finalPhase = true;
	}

	public Player getNextPlayer() throws IllegalStateException
	{
		for (int i = 0; i < this.playerList.length; i++)
		{
			if (this.playerList[i] == this.currentPlayer)
			{
				if (i == this.playerList.length - 1)
				{
					return this.playerList[0];
				}
				else
				{
					return this.playerList[i + 1];
				}
			}
		}

		// If next player hasn't been found for some reason, throw exception
		throw new IllegalStateException();
	}

	public List<TerrainTileDeck> getTerrainTileDecks()
	{
		return this.terrainTileDecks;
	}

	public Region getBlackSheepRegion() throws GameErrorException
	{
		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();
			Region[] currentRoadRegions = currentRoad.getRegions();

			for (int i = 0; i < currentRoadRegions.length; i++)
			{
				if (currentRoadRegions[i].hasBlackSheep())
				{
					return currentRoadRegions[i];
				}
			}
		}

		throw new GameErrorException("Error! Black sheep has not been found!");
	}

	public Region getWolfRegion() throws GameErrorException
	{
		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();
			Region[] currentRoadRegions = currentRoad.getRegions();

			for (int i = 0; i < currentRoadRegions.length; i++)
			{
				if (currentRoadRegions[i].hasWolf())
				{
					return currentRoadRegions[i];
				}
			}
		}

		throw new GameErrorException("Wolf has not been found!");
	}

	public Road getShepherdPosition(Player player, Integer index) throws GameErrorException
	{
		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();
			Shepherd currentRoadShepherd = currentRoad.getShepherd();

			if (currentRoadShepherd != null && currentRoadShepherd.getOwner().equals(player) && currentRoadShepherd.getNumber() == index.intValue())
			{
				return currentRoad;
			}
		}

		throw new GameErrorException("Error! Shepherd with owner " + player.getName() + " and number " + index.intValue() + " has not been found!");
	}

	public boolean isShepherdPlaced(Player player, Integer index)
	{
		try
		{
			this.getShepherdPosition(player, index);
			return true;
		}
		catch (GameErrorException e)
		{
			return false;
		}
	}

	public Player[] getPlayers()
	{
		return this.playerList;
	}

	public Road[] getEmptyRoadsAroundRegion(Region region)
	{
		Set<Road> returnSet = new HashSet<Road>();

		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();

			// Road must be empty
			if (currentRoad.isEmpty())
			{
				Region[] currentRoadRegions = currentRoad.getRegions();

				for (int i = 0; i < currentRoadRegions.length; i++)
				{
					// Road must be adjacent to the region
					if (currentRoadRegions[i].equals(region))
					{
						returnSet.add(currentRoad);
					}
				}
			}
		}

		return returnSet.toArray(new Road[returnSet.size()]);
	}

	public Shepherd[] getEnemyShepherdsAroundRegion(Region region, Player player)
	{
		Set<Shepherd> returnSet = new HashSet<Shepherd>();

		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();

			// Road must have
			if (currentRoad.hasShepherd() && !currentRoad.getShepherd().getOwner().equals(player))
			{
				Region[] currentRoadRegions = currentRoad.getRegions();

				for (int i = 0; i < currentRoadRegions.length; i++)
				{
					// Road must be adjacent to the region
					if (currentRoadRegions[i].equals(region))
					{
						returnSet.add(currentRoad.getShepherd());
					}
				}
			}
		}

		return returnSet.toArray(new Shepherd[returnSet.size()]);
	}

	public Road[] getWolfRoadsAroundRegion(Region region)
	{
		Set<Road> returnSet = new HashSet<Road>();

		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();

			// Road must not contain any shepherd
			if (!currentRoad.hasShepherd())
			{
				Region[] currentRoadRegions = currentRoad.getRegions();

				for (int i = 0; i < currentRoadRegions.length; i++)
				{
					// Road must be adjacent to the region
					if (currentRoadRegions[i].equals(region))
					{
						returnSet.add(currentRoad);
					}
				}
			}
		}

		return returnSet.toArray(new Road[returnSet.size()]);
	}

	public Graph<Road, DefaultEdge> getRoadGraph()
	{
		return this.roadGraph;
	}

	public Road getRoad(Integer roadID) throws GameErrorException
	{
		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();
			if (roadID.equals(currentRoad.getId()))
			{
				return currentRoad;
			}
		}

		throw new GameErrorException("Error! Road with ID " + roadID.intValue() + " has not been found!");
	}

	public TerrainTileDeck getDeck(RegionType deckType) throws GameErrorException
	{
		Iterator<TerrainTileDeck> iterator = this.terrainTileDecks.iterator();

		while (iterator.hasNext())
		{
			TerrainTileDeck currentDeck = iterator.next();
			if (deckType.equals(currentDeck.getType()))
			{
				return currentDeck;
			}
		}

		throw new GameErrorException("Error! Deck with type " + deckType.toString() + " has not been found!");
	}

	public List<Road> getAdjacentRoads(Road road)
	{
		return Graphs.neighborListOf(this.roadGraph, road);
	}

	public List<Integer> getAllEmptyRoadIDs()
	{
		List<Integer> returnList = new ArrayList<Integer>();

		GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(this.roadGraph);

		while (iterator.hasNext())
		{
			Road currentRoad = iterator.next();

			if (currentRoad.isEmpty())
			{
				returnList.add(currentRoad.getId());
			}
		}

		return returnList;
	}

	public Set<Region> getAllRegions()
	{
		Road[] roadArray;
		Region[] regionArray;
		Set<Region> regionSet = new HashSet<Region>();

		// Get all roads from graph
		Set<Road> vertices = this.roadGraph.vertexSet();
		roadArray = vertices.toArray(new Road[vertices.size()]);

		// Get all adjacent regions for each road and save them in set (no duplicates)
		for (int i = 0; i < roadArray.length; i++)
		{
			regionArray = roadArray[i].getRegions();
			for (int j = 0; j < regionArray.length; j++)
			{
				regionSet.add(regionArray[j]);
			}

		}

		return regionSet;
	}
}
