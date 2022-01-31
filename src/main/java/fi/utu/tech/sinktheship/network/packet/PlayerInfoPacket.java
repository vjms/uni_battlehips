package fi.utu.tech.sinktheship.network.packet;

public class PlayerInfoPacket extends Packet {
	private static final long serialVersionUID = -8535008176661015219L;

	public int clientId;
	public boolean isPlayer;

	public String name;

	public PlayerInfoPacket(String name) {
		this.name = name;
	}
}
