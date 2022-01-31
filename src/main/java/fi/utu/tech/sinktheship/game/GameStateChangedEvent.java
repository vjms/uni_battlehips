package fi.utu.tech.sinktheship.game;

import fi.utu.tech.sinktheship.utility.events.Event;

public class GameStateChangedEvent extends Event {

	private GameState state;

	public GameStateChangedEvent(GameState newState) {
		state = newState;
	}

	public GameState getState() {
		return state;
	}

}
