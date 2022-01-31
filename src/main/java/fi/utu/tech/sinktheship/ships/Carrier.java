package fi.utu.tech.sinktheship.ships;

public class Carrier extends Ship {
	public static final int length = 5;

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public ShipType getType() {
		return ShipType.CARRIER;
	}

}
