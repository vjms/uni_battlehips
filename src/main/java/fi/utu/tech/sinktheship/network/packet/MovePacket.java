package fi.utu.tech.sinktheship.network.packet;

public class MovePacket extends Packet {
	private static final long serialVersionUID = 8001866026185419299L;

	public int index;

	public MovePacket(int index) {
		this.index = index;
	}
}
