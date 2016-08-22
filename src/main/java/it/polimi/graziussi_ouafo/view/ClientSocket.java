package it.polimi.graziussi_ouafo.view;

import it.polimi.graziussi_ouafo.main.MiscConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

public class ClientSocket implements Observer
{
	private Socket socket;
	private ObjectInputStream input;
	private ObjectOutputStream output;

	public ClientSocket(String serverAddress) throws UnknownHostException, IOException
	{
		// Create Socket
		this.socket = new Socket(serverAddress, MiscConstants.SERVER_PORT.getValue());

		// Input/output
		OutputStream out = this.socket.getOutputStream();
		this.output = new ObjectOutputStream(out);
		InputStream in = this.socket.getInputStream();
		this.input = new ObjectInputStream(in);
	}

	public boolean sendNameToServer(String name) throws IOException, ClassNotFoundException
	{
		// Send name
		this.output.writeObject(name);
		this.output.flush();

		// Wait for answer
		Object serverAnswer = this.input.readObject();
		if (!(serverAnswer instanceof Boolean))
		{
			return false;
		}
		return ((Boolean) serverAnswer).booleanValue();
	}

	public void waitTurnSchedulerReady() throws ClassNotFoundException, IOException
	{
		boolean isReady = false;
		Object serverAnswer;
		while (!isReady)
		{
			serverAnswer = this.input.readObject();
			if (!(serverAnswer instanceof Boolean))
			{
				continue;
			}
			isReady = ((Boolean) serverAnswer).booleanValue();
		}
	}

	@Override
	public void update(Observable observable, Object event)
	{
		// Send event to the controller
		try
		{
			this.output.writeObject(event);
			this.output.flush();
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Connection error, cannot reach server!", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}
}
