package fi.utu.tech.sinktheship.game;

import java.io.Serializable;

import fi.utu.tech.sinktheship.ships.Battleship;
import fi.utu.tech.sinktheship.ships.Carrier;
import fi.utu.tech.sinktheship.ships.Cruiser;
import fi.utu.tech.sinktheship.ships.Destroyer;
import fi.utu.tech.sinktheship.ships.Submarine;

/**
 * Game rules data wrapper.
 * 
 */
public class GameRules implements Serializable {
	private static final long serialVersionUID = -2696052256976937986L;

	// grid size only 1D
	public int gridSize = 0;

	public int carrierCount = 0;
	public int battleshipCount = 0;
	public int cruiserCount = 0;
	public int submarineCount = 0;
	public int destroyerCount = 0;

	public int timeLimit = 0;
	public boolean fastMode = false;

	public GameRules() {

	}

	public GameRules(GameRules rules) {
		gridSize = rules.gridSize;
		carrierCount = rules.carrierCount;
		battleshipCount = rules.battleshipCount;
		cruiserCount = rules.cruiserCount;
		submarineCount = rules.submarineCount;
		destroyerCount = rules.destroyerCount;
		timeLimit = rules.timeLimit;
		fastMode = rules.fastMode;
	}

	public boolean rulesOk() {
		return gridSize2D() > totalShipArea() * 2 && getShipCount() > 0;
	}

	public int getShipCount() {
		return carrierCount + battleshipCount + cruiserCount + submarineCount + destroyerCount;
	}

	public int gridSize2D() {
		return gridSize * gridSize;
	}

	public int totalShipArea() {
		return carrierCount * Carrier.length + battleshipCount * Battleship.length + cruiserCount * Cruiser.length
				+ submarineCount * Submarine.length + destroyerCount * Destroyer.length;
	}

	public void printRules() {
		// System.out.println("Grid size: " + gridSize);
		// System.out.println("Carriers: " + carrierCount);
		// System.out.println("Battleships: " + battleshipCount);
		// System.out.println("Cruisers: " + cruiserCount);
		// System.out.println("Submarines: " + submarineCount);
		// System.out.println("Destroyers: " + destroyerCount);
		// System.out.println("Fastmode: " + fastMode);
		// System.out.println("Time limit: " + timeLimit);
	}
}
