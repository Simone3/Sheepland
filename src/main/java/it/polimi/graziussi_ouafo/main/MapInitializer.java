package it.polimi.graziussi_ouafo.main;

import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Road;
import it.polimi.graziussi_ouafo.view.GameMap;
import it.polimi.graziussi_ouafo.view.ResourcePaths;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MapInitializer
{
	private static final String ROAD_TAG_NAME = "road";
	private static final String REGION_TAG_NAME = "region";

	private String file;
	private Document document;

	private GameMap gameMap;

	private Graph<Road, DefaultEdge> roadGraphTempReference;
	private Map<Integer, Region> tempRegionMap = new HashMap<Integer, Region>();
	private Map<Integer, Road> tempRoadMap = new HashMap<Integer, Road>();

	public MapInitializer()
	{
		this.file = ResourcePaths.MAP_XML.getValue();
	}

	public Graph<Road, DefaultEdge> createRoadGraphFromXML() throws ParserConfigurationException, SAXException, IOException, IllegalArgumentException
	{
		this.roadGraphTempReference = new ListenableDirectedGraph<Road, DefaultEdge>(DefaultEdge.class);

		// Create document
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		InputStream inputStream = this.getClass().getResourceAsStream(this.file);
		this.document = documentBuilder.parse(inputStream);

		// Create map
		this.createRegions();
		this.createRoads();
		this.connectRoads();

		// Return road graph
		return this.roadGraphTempReference;
	}

	public void fillMapPanelFromXML(GameMap gameMap) throws ParserConfigurationException, SAXException, IOException
	{
		this.gameMap = gameMap;

		// Create document
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		InputStream inputStream = this.getClass().getResourceAsStream(this.file);
		this.document = documentBuilder.parse(inputStream);

		// Create map
		this.createRegionPanels();
		this.createRoadPanels();
	}

	private void createRegions() throws IOException
	{
		// Get nodes "region"
		NodeList elements = this.document.getElementsByTagName(MapInitializer.REGION_TAG_NAME);

		// Loop all "region" nodes
		Node currentNode;
		Element currentElement;
		for (int i = 0; i < elements.getLength(); i++)
		{
			currentNode = elements.item(i);

			// If it's an element node (no sub-nodes)
			if (currentNode.getNodeType() == Node.ELEMENT_NODE)
			{
				// The element is the node
				currentElement = (Element) currentNode;

				// Create region object
				RegionType regionType = Enum.valueOf(RegionType.class, this.getTextValue(currentElement, "type"));
				boolean hasBlackSheep = (regionType.equals(RegionType.SHEEPSBURG)) ? true : false;
				boolean hasWolf = (regionType.equals(RegionType.SHEEPSBURG)) ? true : false;
				int whiteSheepNumber = 0;
				int ramNumber = 0;
				Random rand = new Random();
				if (!regionType.equals(RegionType.SHEEPSBURG))
				{
					int randomNumber = rand.nextInt(2);
					if (randomNumber == 0)
					{
						whiteSheepNumber = MiscConstants.STARTING_SHEEP_PER_REGION.getValue();
					}
					else if (randomNumber == 1)
					{
						ramNumber = MiscConstants.STARTING_SHEEP_PER_REGION.getValue();
					}
				}
				Region tempRegion = new Region(Integer.valueOf(this.getIntAttribute(currentElement, "id")), regionType, hasBlackSheep, whiteSheepNumber, ramNumber, hasWolf);

				// Add region to temporary map (will be used later to connect a road with two regions)
				this.tempRegionMap.put(Integer.valueOf(this.getIntAttribute(currentElement, "id")), tempRegion);
			}
		}
	}

	private void createRegionPanels() throws IOException
	{
		// Get nodes "region"
		NodeList elements = this.document.getElementsByTagName(MapInitializer.REGION_TAG_NAME);

		// Loop all "region" nodes
		Node currentNode;
		Element currentElement;
		for (int i = 0; i < elements.getLength(); i++)
		{
			currentNode = elements.item(i);

			// If it's an element node (no sub-nodes)
			if (currentNode.getNodeType() == Node.ELEMENT_NODE)
			{
				// The element is the node
				currentElement = (Element) currentNode;

				// Get some info
				RegionType regionType = Enum.valueOf(RegionType.class, this.getTextValue(currentElement, "type"));
				boolean hasBlackSheep = (regionType.equals(RegionType.SHEEPSBURG)) ? true : false;
				boolean hasWolf = (regionType.equals(RegionType.SHEEPSBURG)) ? true : false;

				// Add white sheep panels to all regions
				this.gameMap.addWhiteSheepPanel(this.getIntAttribute(currentElement, "id"), new Point(this.getIntValue(currentElement, "whiteSheepXcoord"), this.getIntValue(currentElement, "whiteSheepYcoord")));

				// Since there is only one black sheep and only one wolf, save a map with all positions inside the region to place them when moved
				this.gameMap.addBlackSheepPosition(this.getIntAttribute(currentElement, "id"), new Point(this.getIntValue(currentElement, "blackSheepXcoord"), this.getIntValue(currentElement, "blackSheepYcoord")));
				this.gameMap.addWolfPosition(this.getIntAttribute(currentElement, "id"), new Point(this.getIntValue(currentElement, "wolfXcoord"), this.getIntValue(currentElement, "wolfYcoord")));

				// Add the black sheep and wolf panels only to the region that has them at the beginning
				if (hasBlackSheep)
				{
					this.gameMap.addBlackSheepPanel(new Point(this.getIntValue(currentElement, "blackSheepXcoord"), this.getIntValue(currentElement, "blackSheepYcoord")));
				}
				if (hasWolf)
				{
					this.gameMap.addWolfPanel(new Point(this.getIntValue(currentElement, "wolfXcoord"), this.getIntValue(currentElement, "wolfYcoord")));
				}
			}
		}
	}

	private void createRoads() throws IOException
	{
		// Get nodes "road"
		NodeList elements = this.document.getElementsByTagName(MapInitializer.ROAD_TAG_NAME);

		// Loop all "road" nodes
		Node currentNode;
		Element currentElement;
		for (int i = 0; i < elements.getLength(); i++)
		{
			currentNode = elements.item(i);

			// If it's an element node (no sub-nodes)
			if (currentNode.getNodeType() == Node.ELEMENT_NODE)
			{
				// The element is the node
				currentElement = (Element) currentNode;

				// Create road object
				Region region1 = this.tempRegionMap.get(Integer.valueOf(this.getIntAttribute((Element) currentElement.getElementsByTagName("region1").item(0), "idref")));
				Region region2 = this.tempRegionMap.get(Integer.valueOf(this.getIntAttribute((Element) currentElement.getElementsByTagName("region2").item(0), "idref")));
				Road road = new Road(Integer.valueOf(this.getIntAttribute(currentElement, "id")), this.getIntValue(currentElement, "number"), new Region[] { region1, region2 });

				// Add road to temporary map (will be used later to connect the roads in the graph)
				this.tempRoadMap.put(Integer.valueOf(this.getIntAttribute(currentElement, "id")), road);

				// Add road to graph
				this.roadGraphTempReference.addVertex(road);
			}
		}
	}

	private void createRoadPanels() throws IOException
	{
		// Get nodes "road"
		NodeList elements = this.document.getElementsByTagName(MapInitializer.ROAD_TAG_NAME);

		// Loop all "road" nodes
		Node currentNode;
		Element currentElement;
		for (int i = 0; i < elements.getLength(); i++)
		{
			currentNode = elements.item(i);

			// If it's an element node (no sub-nodes)
			if (currentNode.getNodeType() == Node.ELEMENT_NODE)
			{
				// The element is the node
				currentElement = (Element) currentNode;

				// Add panel to map
				this.gameMap.addRoadPanel(this.getIntAttribute(currentElement, "id"), new Point(this.getIntValue(currentElement, "Xcoord"), this.getIntValue(currentElement, "Ycoord")));
			}
		}
	}

	private void connectRoads() throws IllegalArgumentException
	{
		// Get nodes "roadLink"
		NodeList elements = this.document.getElementsByTagName("roadLink");

		// Loop all "roadLink" nodes
		Node currentNode;
		Element currentElement;
		for (int i = 0; i < elements.getLength(); i++)
		{
			currentNode = elements.item(i);

			// If it's an element node (no sub-nodes)
			if (currentNode.getNodeType() == Node.ELEMENT_NODE)
			{
				// The element is the node
				currentElement = (Element) currentNode;

				// Add connection between roads in graph
				this.roadGraphTempReference.addEdge(this.tempRoadMap.get(Integer.valueOf(this.getIntAttribute((Element) currentElement.getElementsByTagName("road1").item(0), "idref"))), this.tempRoadMap.get(Integer.valueOf(this.getIntAttribute((Element) currentElement.getElementsByTagName("road2").item(0), "idref"))));
			}
		}
	}

	// Shorthand to get a String value
	private String getTextValue(Element element, String tag)
	{
		return element.getElementsByTagName(tag).item(0).getTextContent();
	}

	// Shorthand to get an integer value
	private int getIntValue(Element element, String tag)
	{
		return Integer.valueOf(element.getElementsByTagName(tag).item(0).getTextContent()).intValue();
	}

	// Shorthand to get an integer attribute
	private int getIntAttribute(Element element, String attr)
	{
		return Integer.valueOf(element.getAttribute(attr)).intValue();
	}
}
