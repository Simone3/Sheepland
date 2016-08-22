package it.polimi.graziussi_ouafo.controller;

import it.polimi.graziussi_ouafo.main.GameErrorException;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TurnSchedulerRemoteInterface extends Remote
{
	public void registerClientInServer(String playerName, String playerAddress) throws RemoteException, GameErrorException, NotBoundException;
}
