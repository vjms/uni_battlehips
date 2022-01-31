package fi.utu.tech.sinktheship.network.packet;

import fi.utu.tech.sinktheship.game.MoveStatus;

public class MoveResponsePacket extends MovePacket {
	private static final long serialVersionUID = -8116540641267263121L;

	public MoveStatus status;
	public int instigator;

	public MoveResponsePacket(int instigator, int index, MoveStatus status) {
		super(index);
		this.instigator = instigator;
		this.status = status;
	}
}
