package fi.utu.tech.sinktheship.network.packet;

import fi.utu.tech.sinktheship.game.GameRules;

public class GameRulesPacket extends Packet {
	private static final long serialVersionUID = 3548013172253714928L;
	
	public GameRules rules;

	public GameRulesPacket(GameRules rules) {
		this.rules = rules;
	}
}
