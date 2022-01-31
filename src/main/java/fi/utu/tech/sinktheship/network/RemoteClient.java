package fi.utu.tech.sinktheship.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import fi.utu.tech.sinktheship.GameInstance;
import fi.utu.tech.sinktheship.network.packet.JoinRequestPacket;
import fi.utu.tech.sinktheship.network.packet.JoinRequestResponse;

public class RemoteClient extends Client {

	// Stored for reconnecting purposes
	// private InetAddress serverAddress;
	// Stored for reconnecting purposes
	// private int serverPort;

	private RemoteClient(InetAddress serverAddress, int serverPort, boolean spectator) {
		// this.serverAddress = serverAddress;
		// this.serverPort = serverPort;

		// Tell the game instance that we now have a client. If the app is closed we can
		// kill the client.
		GameInstance.setClient(this);
	}

	@Override
	public boolean hasAuthority() {
		return getId() == 0;
	}

	/**
	 * Try to create a client and connect it to a game server with the info. Returns
	 * a client that already has threads running for client server messaging.
	 * 
	 * @param serverAddress Server InetAddress
	 * @param serverPort    Server port
	 * @param spectator     True if only spectating the class
	 * @return new client or null if server refused or was not found.
	 */
	static public Client connect(InetAddress serverAddress, int serverPort, boolean spectator) {
		try {
			RemoteClient client = new RemoteClient(serverAddress, serverPort, spectator);
			client.setSocket(new Socket(serverAddress, serverPort));
			client.createStreams();
			if (client.joinGameRequest()) {
				client.start();
				return client;
			} else {
				client.closeSocket();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a client and connects it to a local server.
	 * 
	 * @return
	 */
	public static Client createLocal() {
		try {
			return RemoteClient.connect(InetAddress.getLocalHost(), Server.defaultPort, false);
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Ask the server if we can join the game.
	 * 
	 * @return true if the server accepted our request to join.
	 */
	private boolean joinGameRequest() {
		try {
			var jgrequest = new JoinRequestPacket(getId(), GameInstance.getPlayerName(), false);
			getOutput().writeObject(jgrequest);
			var response = (JoinRequestResponse) getInput().readObject();
			var success = (response.getId() == jgrequest.getId()) && response.clientId >= 0;
			if (success) {
				setId(response.clientId);
				return success;
			}
		} catch (ClassNotFoundException | ClassCastException | NullPointerException | IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
