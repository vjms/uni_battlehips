package fi.utu.tech.sinktheship.network.packet;

import java.util.ArrayList;

import fi.utu.tech.sinktheship.game.ShipPlacement;

public class SetupPacket extends Packet {
	private static final long serialVersionUID = 706572109088611073L;

	public ArrayList<ShipPlacement> shipPlacements;

	public SetupPacket(ArrayList<ShipPlacement> shipPlacements) {
		this.shipPlacements = shipPlacements;
	}
}
