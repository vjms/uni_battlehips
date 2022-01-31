package fi.utu.tech.sinktheship.game;

import java.io.Serializable;

import fi.utu.tech.sinktheship.ships.ShipType;

public class ShipPlacement implements Serializable {
	private static final long serialVersionUID = 6589145193863978521L;

	public ShipType ship;
	public Integer index;
	public Boolean horizontal;

	public ShipPlacement(ShipType ship, Integer index, Boolean horizontal) {
		this.ship = ship;
		this.index = index;
		this.horizontal = horizontal;
	}
}
