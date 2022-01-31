package fi.utu.tech.sinktheship.game;

import java.util.ArrayList;

public class Player {
	private boolean ready = false;
	private Grid grid = null;

	private ArrayList<Integer> moves = new ArrayList<>();

	public int getMovesCount() {
		return moves.size();
	}

	public void reset() {
		moves = new ArrayList<>();
	}

	/**
	 * 
	 * @param index move index
	 * @return true if the move was valid (cannot make the same move twice)
	 */
	public boolean addMove(int index) {
		if (!moves.contains(index) && grid.isValidIndex(index)) {
			moves.add(index);
			return true;
		}
		return false;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean getReady() {
		return ready;
	}

}
