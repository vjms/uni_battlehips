package fi.utu.tech.sinktheship.gui.javafx.tasks;

import java.net.InetAddress;

import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.network.RemoteClient;
import javafx.concurrent.Task;

public class ConnectTask extends Task<Client> {

	private String address;
	private int port;
	private boolean spectator;

	public ConnectTask(String address, int port, boolean spectator) {
		this.address = address;
		this.port = port;
		this.spectator = spectator;
	}

	@Override
	protected Client call() throws Exception {
		try {
			return RemoteClient.connect(InetAddress.getByName(address), port, spectator);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
