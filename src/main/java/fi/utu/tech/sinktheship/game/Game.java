package fi.utu.tech.sinktheship.game;

/**
 * 
 * 
 */

public class Game {
	private GameRules rules = new GameRules();
	private Player[] players = new Player[2];
	private GameState gameState = GameState.NEW_GAME;

	public Game() {
		players[0] = new Player();
		players[1] = new Player();
	}

	public Player[] getPlayers() {
		return players;
	}

	public void setGameState(GameState newState) {
		gameState = newState;
		if (gameState == GameState.NEW_GAME) {
			players[0] = new Player();
			players[1] = new Player();
		}
	}

	public GameState getGameState() {
		return gameState;
	}

	public boolean playersReady() {
		return players[0].getReady() && players[1].getReady();
	}

	public void setRules(GameRules newRules) {
		rules = newRules;
	}

	public GameRules getRules() {
		return rules;
	}

}
