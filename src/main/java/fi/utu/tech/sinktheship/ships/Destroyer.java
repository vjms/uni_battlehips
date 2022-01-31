package fi.utu.tech.sinktheship.ships;

public class Destroyer extends Ship {
	public static final int length = 2;

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public ShipType getType() {
		return ShipType.DESTROYER;
	}
}
