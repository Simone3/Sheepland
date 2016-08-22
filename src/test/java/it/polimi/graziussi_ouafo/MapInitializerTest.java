package it.polimi.graziussi_ouafo;

import it.polimi.graziussi_ouafo.main.MapInitializer;
import it.polimi.graziussi_ouafo.main.MiscConstants;
import it.polimi.graziussi_ouafo.model.Region;
import it.polimi.graziussi_ouafo.model.RegionType;
import it.polimi.graziussi_ouafo.model.Road;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class MapInitializerTest
{
	MapInitializer initializer;

	@Before
	public void testMapInitializer()
	{
		this.initializer = new MapInitializer();
	}

	@Test
	public void testReadMapFromXML()
	{
		try
		{
			System.out.println("ROAD INFO");
			System.out.println("");

			Graph<Road, DefaultEdge> graph = this.initializer.createRoadGraphFromXML();

			Assert.assertNotNull(graph);

			GraphIterator<Road, DefaultEdge> iterator = new DepthFirstIterator<Road, DefaultEdge>(graph);

			Road currentRoad;
			Region[] currentRoadRegions;
			int count = 0;

			while (iterator.hasNext())
			{
				currentRoad = iterator.next();

				Assert.assertNotNull(currentRoad);
				Assert.assertNotNull(currentRoad.getId());
				Assert.assertFalse(currentRoad.hasFence());
				Assert.assertFalse(currentRoad.hasShepherd());
				Assert.assertTrue(currentRoad.isEmpty());

				System.out.println(currentRoad.toString());

				currentRoadRegions = currentRoad.getRegions();

				Assert.assertEquals("", currentRoadRegions.length, 2);

				for (int i = 0; i < currentRoadRegions.length; i++)
				{
					Assert.assertNotNull(currentRoadRegions[i]);
					Assert.assertNotNull(currentRoadRegions[i].getId());
					Assert.assertNotNull(currentRoadRegions[i].getType());

					if (currentRoadRegions[i].getType().equals(RegionType.SHEEPSBURG))
					{
						Assert.assertEquals("", currentRoadRegions[i].getWhiteSheepNumber() + currentRoadRegions[i].getRamNumber(), 0);
						Assert.assertTrue("", currentRoadRegions[i].hasBlackSheep());
					}
					else
					{
						Assert.assertEquals("", currentRoadRegions[i].getWhiteSheepNumber() + currentRoadRegions[i].getRamNumber(), MiscConstants.STARTING_SHEEP_PER_REGION.getValue());
						Assert.assertFalse("", currentRoadRegions[i].hasBlackSheep());
					}

					System.out.println("	" + currentRoadRegions[i].toString());
				}

				count++;
			}

			System.out.println("THERE ARE " + count + " ROADS");

			System.out.println("");
			System.out.println("");
			System.out.println("ROAD CONNECTIONS");
			System.out.println("");

			GraphIterator<Road, DefaultEdge> iterator2 = new DepthFirstIterator<Road, DefaultEdge>(graph);

			List<Road> neighbors;
			Iterator<Road> itr;
			while (iterator2.hasNext())
			{
				currentRoad = iterator2.next();

				neighbors = Graphs.neighborListOf(graph, currentRoad);
				itr = neighbors.iterator();

				System.out.println("");
				System.out.print("Road " + currentRoad.getId() + " is linked with: ");
				while (itr.hasNext())
				{
					System.out.print(itr.next().getId() + ", ");
				}
			}
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
		}
	}
}
