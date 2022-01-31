package fi.utu.tech.sinktheship;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import fi.utu.tech.sinktheship.gui.javafx.GUI;
import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.network.Server;

/**
 * Class to handle all static data. Cannot be instanced.
 */
public final class GameInstance {
	private static Server server = null;
	private static GUI gui = null;
	private static Client client = null;
	private static String playerName = null;

	public static void setClient(Client client) {
		GameInstance.client = client;
	}

	public static Server createServer() {
		shutdownServer();
		server = new Server("Testi");
		server.start();
		return server;
	}

	public static void shutdownServer() {
		if (server != null) {
			try {
				server.interrupt();
				server.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		server = null;
	}

	public static void setGui(GUI gui) {
		GameInstance.gui = gui;
	}

	public static GUI getGui() {
		return gui;
	}

	public static void setPlayerName(String playerName) {
		GameInstance.playerName = playerName;
		try {
			var writer = new BufferedWriter(new FileWriter("name.txt"));
			writer.write(playerName);
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static String getPlayerName() {
		if (playerName == null) {
			try {
				var reader = new BufferedReader(new FileReader("name.txt"));
				playerName = reader.readLine();
				reader.close();
			} catch (Exception ex) {
				playerName = "";
			}
		}
		return playerName;
	}

	public static void quit() {
		gui.quit();
		shutdownServer();
		if (client != null) {
			client.disconnect();
		}
	}

	private GameInstance() {

	}

}
