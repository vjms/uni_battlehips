package fi.utu.tech.sinktheship.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fi.utu.tech.sinktheship.ships.Battleship;
import fi.utu.tech.sinktheship.ships.Carrier;
import fi.utu.tech.sinktheship.ships.Cruiser;
import fi.utu.tech.sinktheship.ships.Destroyer;
import fi.utu.tech.sinktheship.ships.Ship;
import fi.utu.tech.sinktheship.ships.Submarine;
import fi.utu.tech.sinktheship.utility.events.EventDispatcher;

public class Grid {
	private ArrayList<Tile> tiles;
	private GameRules rules;
	private HashMap<Ship, Integer> ships = new HashMap<>();
	public final EventDispatcher<ShipPlacedEvent> shipPlacedEventDispatcher = new EventDispatcher<>();

	private void generateTiles() {
		tiles = new ArrayList<>(rules.gridSize);
		for (int i = 0; i < rules.gridSize2D(); i++) {
			tiles.add(new Tile());
		}
	}

	public Grid(GameRules rules, ArrayList<ShipPlacement> placements) {
		this.rules = rules;
		generateTiles();
		for (var p : placements) {
			Ship ship = null;
			switch (p.ship) {
				case BATTLESHIP:
					ship = new Battleship();
					break;
				case CARRIER:
					ship = new Carrier();
					break;
				case CRUISER:
					ship = new Cruiser();
					break;
				case DESTROYER:
					ship = new Destroyer();
					break;
				case SUBMARINE:
					ship = new Submarine();
					break;
			}
			if (ship != null) {
				ship.setHorizontal(p.horizontal);
				placeShip(ship, p.index);
			}
		}
	}

	public Grid(GameRules rules) {
		this.rules = rules;
		generateTiles();
		for (int i = 0; i < rules.carrierCount; i++) {
			ships.put(new Carrier(), -1);
		}
		for (int i = 0; i < rules.battleshipCount; i++) {
			ships.put(new Battleship(), -1);
		}
		for (int i = 0; i < rules.cruiserCount; i++) {
			ships.put(new Cruiser(), -1);
		}
		for (int i = 0; i < rules.submarineCount; i++) {
			ships.put(new Submarine(), -1);
		}
		for (int i = 0; i < rules.destroyerCount; i++) {
			ships.put(new Destroyer(), -1);
		}
	}

	public boolean allShipsDestroyed() {
		for (var ship : ships.keySet()) {
			if (!ship.isDestroyed()) {
				return false;
			}
		}
		return true;
	}

	public ShipPlacement getShipPlacement(Ship ship) {
		return new ShipPlacement(ship.getType(), ships.get(ship), ship.isHorizontal());
	}

	public Ship shootAt(int index) {
		if (tiles.get(index).destroy()) {
			return tiles.get(index).getShip();
		}
		return null;
	}

	public GameRules getRules() {
		return rules;
	}

	public ArrayList<ShipPlacement> getShipPlacements() {
		var ret = new ArrayList<ShipPlacement>();
		ships.forEach((ship, index) -> ret.add(new ShipPlacement(ship.getType(), index, ship.isHorizontal())));
		return ret;
	}

	public Map<Ship, Integer> getShips() {
		return ships;
	}

	/**
	 * finds random locations for all ships.
	 */
	public void shuffleShips() {
		removeShips();
		ships.forEach((ship, index) -> {
			for (int i = 0; i < 10; i++) {
				if (tryRandomPlacement(ship)) {
					break;
				}
			}
		});
	}

	public Tile getTile(int index) throws IndexOutOfBoundsException {
		return tiles.get(index);
	}

	public ArrayList<Tile> getTiles() {
		return tiles;
	}

	/**
	 * 
	 * @return no overlapping ships (or maybe other weird things later?)
	 */
	public boolean isOk() {
		for (var tile : tiles) {
			if (tile.isCrowded()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * 
	 * @param ship   the ship data
	 * @param column coordinate where it is intended to place the ship.
	 * @param row    coordinate where it is intended to place the ship.
	 * @param offset offset
	 * @return index that has enough space next to it to fit the whole ship.
	 */

	public int getShipValidPlacementOffsetIndex(Ship ship, int column, int row, int offset) {
		if (ship.isHorizontal()) {
			column = getValidToDirectionOffset(ship, column - offset);
		} else {
			row = getValidToDirectionOffset(ship, row - offset);
		}
		return toIndex(column, row);
	}

	private int getValidToDirectionOffset(Ship ship, int dir) {
		if (dir < 0) {
			dir -= dir % rules.gridSize;
		} else if (dir + ship.getLength() > rules.gridSize) {
			dir -= (ship.getLength() + dir) % rules.gridSize;
		}
		return dir;
	}

	/**
	 * Place ship to index. Has to fit.
	 * 
	 * @param ship
	 * @param index Top left corner of the ship always.
	 */
	public void placeShip(Ship ship, int index) {
		ships.put(ship, index);

		removeShip(ship);
		int tmp = index;
		for (int i = 0; i < ship.getLength(); i++) {
			tiles.get(index).addShip(ship);
			index = ship.isHorizontal() ? getRightIndex(index) : getBelowIndex(index);
		}
		shipPlacedEventDispatcher.dispatch(new ShipPlacedEvent(ship, tmp));
	}

	/**
	 * Checks if there is enough space below or right of the index to add the ship.
	 * Can enable empty check for the tiles.
	 * 
	 * @param rect        Ship to test
	 * @param index       First index
	 * @param mustBeEmpty Enable tile empty check.
	 * @throws IndexOutOfBoundsException Sanity not ok.
	 */
	public void shipPlacementSanityCheck(Ship ship, int index, boolean mustBeEmpty) throws IndexOutOfBoundsException {
		for (int i = 0; i < ship.getLength(); i++) {
			if (!isValidIndex(index) || (mustBeEmpty && !getTile(index).isEmpty())) {
				throw new IndexOutOfBoundsException(index + " is not a valid index");
			}
			index = ship.isHorizontal() ? getRightIndex(index) : getBelowIndex(index);
		}
	}

	/**
	 * Finds the first index of the grid, where a ship can be inserted. Currently
	 * doesn't take rotation into account. Perhaps that will not even be necessary
	 * if we only use this when we first create the ships. They can be vertical
	 * during that phase. If the grid were to be reset, the rotations should be
	 * reset aswell.
	 * 
	 * @param ship ship to find a place for
	 * @return index of the location or -1 if unable to find a suitable location
	 */
	public int findFirstCanPlaceSlot(Ship ship) {
		for (int i = 0; i < tiles.size(); i++) {
			try {
				shipPlacementSanityCheck(ship, i, true);
				return i;
			} catch (IndexOutOfBoundsException ex) {

			}
		}
		return -1;

	}

	/**
	 * 
	 * @return list of ships that overlap.
	 */
	public final ArrayList<Ship> findOverlappingShips() {
		var ret = new ArrayList<Ship>();
		for (var tile : tiles) {
			if (tile.isCrowded()) {
				for (var ship : tile.getShips()) {
					if (!ret.contains(ship)) {
						ret.add(ship);
					}
				}
			}
		}
		return ret;
	}

	private Random rand = new Random();

	/**
	 * finds a random location to place the ship that doesn't already contain a
	 * ship.
	 * 
	 * @param ship ship to place
	 * @return true on placement succeeded
	 */
	private boolean tryRandomPlacement(Ship ship) {
		final int riBeg = randomValidIndex();
		ship.setHorizontal(rand.nextBoolean());
		int ri = riBeg;
		while ((ri = (ri + riBeg) % tiles.size()) != riBeg) {
			try {
				int tmp = getShipValidPlacementOffsetIndex(ship, getColumn(ri), getRow(ri), 0);
				shipPlacementSanityCheck(ship, tmp, true);
				placeShip(ship, tmp);
				return true;
			} catch (IndexOutOfBoundsException ex) {

			}
		}
		return false;
	}

	/**
	 * 
	 * @return random index on the grid
	 */
	private int randomValidIndex() {
		return rand.nextInt(tiles.size());
	}

	public void removeShip(Ship ship) {
		for (var tile : tiles) {
			tile.removeShip(ship);
		}
	}

	public void removeShips() {
		for (var tile : tiles) {
			tile.clear();
		}
	}

	public int getRightIndex(int index) {
		int tmp = index + 1;
		return (getColumn(tmp) != 0) ? tmp : -1;
	}

	public int getLeftIndex(int index) {
		return (getColumn(index) != 0) ? index - 1 : -1;
	}

	public int getAboveIndex(int index) {
		return index - rules.gridSize;
	}

	public int getBelowIndex(int index) {
		int tmp = index + rules.gridSize;
		return tmp < tiles.size() ? tmp : -1;
	}

	public boolean isValidIndex(int index) {
		return index >= 0 && index < tiles.size();
	}

	public int getColumn(int index) {
		return index % rules.gridSize;
	}

	public int getRow(int index) {
		return index / rules.gridSize;
	}

	public int toIndex(int column, int row) {
		return column + row * rules.gridSize;
	}

}
