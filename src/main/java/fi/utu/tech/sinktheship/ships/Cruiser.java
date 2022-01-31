package fi.utu.tech.sinktheship.ships;

public class Cruiser extends Ship {
	public static final int length = 3;

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public ShipType getType() {
		return ShipType.CRUISER;
	}
}
