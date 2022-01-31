package fi.utu.tech.sinktheship.network.packet;

import fi.utu.tech.sinktheship.game.GameRules;

public class StartGamePacket extends GameRulesPacket {
	public StartGamePacket(GameRules rules) {
		super(rules);
	}

	private static final long serialVersionUID = 3578673967165243338L;

}
