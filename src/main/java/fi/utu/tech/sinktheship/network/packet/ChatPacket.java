package fi.utu.tech.sinktheship.network.packet;

public class ChatPacket extends Packet {
	private static final long serialVersionUID = 1888342378419843282L;

	public String text;

	public ChatPacket(String text) {
		this.text = text;
	}

}
