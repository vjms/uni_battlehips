package fi.utu.tech.sinktheship.network.packet;

public class TurnPacket extends Packet {
	private static final long serialVersionUID = 2516328400419744983L;

	public int clientId;

	public TurnPacket(int clientId) {
		this.clientId = clientId;
	}
}
