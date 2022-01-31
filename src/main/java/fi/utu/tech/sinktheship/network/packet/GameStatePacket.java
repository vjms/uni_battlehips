package fi.utu.tech.sinktheship.network.packet;

import fi.utu.tech.sinktheship.game.GameState;

public class GameStatePacket extends Packet {
	private static final long serialVersionUID = -6020738394630587702L;

	public GameState state;

	public GameStatePacket(GameState state){
		this.state = state;
	}
}
