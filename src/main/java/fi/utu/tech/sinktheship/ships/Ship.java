package fi.utu.tech.sinktheship.ships;

import fi.utu.tech.sinktheship.utility.events.EventDispatcher;
import fi.utu.tech.sinktheship.utility.events.NullEvent;

public abstract class Ship {
	public abstract int getLength();

	public final EventDispatcher<NullEvent> rotatedEventDispatcher = new EventDispatcher<>();

	private int damageTaken = 0;
	private boolean horizontal = false;

	public void rotate() {
		setHorizontal(!horizontal);
	}

	public void setHorizontal(boolean newValue) {
		horizontal = newValue;
		rotatedEventDispatcher.dispatch(new NullEvent());
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	public int getDamageTaken() {
		return damageTaken;
	}

	public void addDamage() {
		damageTaken++;
	}

	public boolean isDestroyed() {
		return damageTaken >= getLength();
	}

	public abstract ShipType getType();

	/**
	 * Factory for ship creation from type
	 * 
	 * @return ship
	 */
	public static Ship fromType(ShipType type) {
		switch (type) {
			case BATTLESHIP:
				return new Battleship();
			case CARRIER:
				return new Carrier();
			case CRUISER:
				return new Cruiser();
			case DESTROYER:
				return new Destroyer();
			case SUBMARINE:
				return new Submarine();
		}
		return null;
	}
}
