package fi.utu.tech.sinktheship.ships;

public class Battleship extends Ship {
	public static final int length = 4;

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public ShipType getType() {
		return ShipType.BATTLESHIP;
	}
}
