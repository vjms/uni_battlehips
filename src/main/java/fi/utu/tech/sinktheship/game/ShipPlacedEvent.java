package fi.utu.tech.sinktheship.game;

import fi.utu.tech.sinktheship.ships.Ship;
import fi.utu.tech.sinktheship.utility.events.Event;

public class ShipPlacedEvent extends Event {
	public final Ship ship;
	public final int index;

	public ShipPlacedEvent(Ship ship, int index) {
		this.ship = ship;
		this.index = index;
	}
}
