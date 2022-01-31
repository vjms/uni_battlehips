package fi.utu.tech.sinktheship.network.packet;

import java.io.Serializable;

public abstract class Packet implements Serializable {
	private static final long serialVersionUID = 3670077203281888380L;

	private static long idRunning = 0;
	protected long id = 0;

	protected Packet() {
		id = idRunning++;
	}

	public long getId() {
		return id;
	}

	public void copyId(Packet packet) {
		this.id = packet.id;
	}
}
