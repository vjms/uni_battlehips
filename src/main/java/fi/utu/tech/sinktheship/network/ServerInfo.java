package fi.utu.tech.sinktheship.network;

import java.net.InetAddress;

public class ServerInfo {
	public String name;
	public int playerCount;
	public int spectatorCount;
	public int ping;
	public InetAddress address;
	public int port;

	public ServerInfo(String name, int playerCount, int spectatorCount, int ping, InetAddress address, int port) {
		this.name = name;
		this.playerCount = playerCount;
		this.spectatorCount = spectatorCount;
		this.ping = ping;
		this.address = address;
		this.port = port;
	}

}
