package fi.utu.tech.sinktheship.network.packet;

public class GameOverPacket extends Packet {
	private static final long serialVersionUID = -5363118807331533586L;
	
	public int winnerClientId;

	public GameOverPacket(int winner) {
		winnerClientId = winner;
	}

}
