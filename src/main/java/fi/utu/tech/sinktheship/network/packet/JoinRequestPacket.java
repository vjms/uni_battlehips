package fi.utu.tech.sinktheship.network.packet;

public class JoinRequestPacket extends Packet {
	private static final long serialVersionUID = -2801989307654106227L;

	public String name;
	public int id;
	public boolean spectator;

	public JoinRequestPacket(int id, String name, boolean spectator) {
		this.id = id;
		this.name = name;
		this.spectator = spectator;
	}

}
