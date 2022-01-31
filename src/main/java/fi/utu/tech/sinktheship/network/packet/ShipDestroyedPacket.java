package fi.utu.tech.sinktheship.network.packet;

import fi.utu.tech.sinktheship.game.ShipPlacement;

public class ShipDestroyedPacket extends Packet {
	private static final long serialVersionUID = 5359688474152137294L;

	public ShipPlacement shipPlacement;
	public int instigator;

	public ShipDestroyedPacket(int instigator, ShipPlacement shipPlacement) {
		this.instigator = instigator;
		this.shipPlacement = shipPlacement;
	}

}
