package fi.utu.tech.sinktheship.network.packet;

import java.util.ArrayList;

import fi.utu.tech.sinktheship.game.ShipPlacement;

public class SetupReadyPacket extends SetupPacket {
	private static final long serialVersionUID = -8742670838956809054L;

	public boolean ready;

	public SetupReadyPacket(boolean ready, ArrayList<ShipPlacement> shipPlacements) {
		super(shipPlacements);
		this.ready = ready;
	}
}
