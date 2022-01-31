package fi.utu.tech.sinktheship.game;

import java.util.ArrayList;

import fi.utu.tech.sinktheship.ships.Ship;

public class Tile {
	private boolean destroyed = false;
	private ArrayList<Ship> ships = new ArrayList<>();

	public void addShip(Ship ship) {
		ships.add(ship);
	}

	public void clear() {
		ships.clear();
		destroyed = false;
	}

	public boolean removeShip(Ship ship) {
		return ships.remove(ship);
	}

	public ArrayList<Ship> getShips() {
		return ships;
	}

	public boolean isEmpty() {
		return ships.isEmpty();
	}

	public boolean isCrowded() {
		return ships.size() > 1;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * 
	 * @return True if there was a ship
	 */
	public boolean destroy() {
		destroyed = true;
		try {
			getShip().addDamage();
			return true;
		} catch (IndexOutOfBoundsException ex) {
			// no ship
		}
		return false;
	}

	public Ship getShip() throws IndexOutOfBoundsException {
		return ships.get(0);
	}
}
