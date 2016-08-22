package it.polimi.graziussi_ouafo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.polimi.graziussi_ouafo.main.GameErrorException;
import it.polimi.graziussi_ouafo.main.MapInitializer;
import it.polimi.graziussi_ouafo.model.GameState;
import it.polimi.graziussi_ouafo.model.Player;
import it.polimi.graziussi_ouafo.model.PlayerColor;
import it.polimi.graziussi_ouafo.model.Shepherd;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class GameStateTest
{

	@Test
	public void testGetNextPlayerAndSetCurrentPlayer() throws GameErrorException, IllegalArgumentException, ParserConfigurationException, SAXException, IOException
	{
		Player[] testPlayers = new Player[] { new Player("A", PlayerColor.RED, 10), new Player("B", PlayerColor.BLUE, 10), new Player("C", PlayerColor.GREEN, 10) };
		GameState tester = new GameState(true, testPlayers, null, (new MapInitializer()).createRoadGraphFromXML());

		assertEquals(tester.getNumberOfPlayers(), 3);
		
		tester.setCurrentPlayer(testPlayers[0]);
		assertEquals(testPlayers[0], tester.getCurrentPlayer());

		tester.setCurrentPlayer(tester.getNextPlayer());
		assertEquals(testPlayers[1], tester.getCurrentPlayer());
		
		tester.setCurrentPlayer(tester.getNextPlayer());
		assertEquals(testPlayers[2], tester.getCurrentPlayer());
		
		tester.setCurrentPlayer(tester.getNextPlayer());
		assertEquals(testPlayers[0], tester.getCurrentPlayer());
		
		assertFalse(tester.isFinalPhase());
		tester.startFinalPhase();
		assertTrue(tester.isFinalPhase());
		
		
		tester.getRoad(25).getRegions()[1].placeBlackSheep();
		assertEquals(tester.getBlackSheepRegion(), tester.getRoad(25).getRegions()[1]);
		
		
		tester.getRoad(34).getRegions()[0].placeWolf();
		assertEquals(tester.getWolfRegion(), tester.getRoad(34).getRegions()[0]);
		
		
		
		assertFalse(tester.isShepherdPlaced(testPlayers[0], 0));
		tester.getRoad(12).placeShepherd(new Shepherd(testPlayers[0], 0));
		assertTrue(tester.isShepherdPlaced(testPlayers[0], 0));
	}

}
