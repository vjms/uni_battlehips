package fi.utu.tech.sinktheship.network.packet;

public class HandShakePacket extends Packet {
	private static final long serialVersionUID = 2987825970416935200L;

	public String serverName = "";
	public Integer playerCount = 0;
	public Integer spectatorCount = 0;

}
