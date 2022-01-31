package fi.utu.tech.sinktheship.gui.javafx.components;

import fi.utu.tech.sinktheship.network.ServerInfo;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Utility component to display server info on a horizontal box
 */
public class ServerEntry extends HBox {

	private Label serverNameLabel;
	private Label pingLabel;
	private Label playerCountLabel;
	private Label spectatorCountLabel;

	private ServerInfo serverInfo;

	public ServerEntry(ServerInfo serverInfo) {
		this(serverInfo.name, Integer.toString(serverInfo.ping) + " ms",
				Integer.toString(serverInfo.playerCount) + " / 2", Integer.toString(serverInfo.spectatorCount));
		this.serverInfo = serverInfo;
	}

	public ServerEntry(String name, String ping, String playerCount, String spectatorCount) {
		serverNameLabel = new Label(name);
		pingLabel = new Label(ping);
		playerCountLabel = new Label(playerCount);
		spectatorCountLabel = new Label(spectatorCount);

		getChildren().addAll(serverNameLabel, pingLabel, playerCountLabel, spectatorCountLabel);

	}

	@Override
	protected void layoutChildren() {
		final int numberLabelSize = 60;
		spectatorCountLabel.setMinWidth(numberLabelSize);
		spectatorCountLabel.setAlignment(Pos.CENTER);
		playerCountLabel.setMinWidth(numberLabelSize);
		playerCountLabel.setAlignment(Pos.CENTER);
		pingLabel.setMinWidth(numberLabelSize);
		pingLabel.setAlignment(Pos.CENTER);

		var parent = (Region) getParent();
		serverNameLabel.setMinWidth(parent.getWidth() - numberLabelSize * 3 - 20);

		setAlignment(Pos.CENTER);

		super.layoutChildren();
	}

	public ServerInfo getInfo() {
		return serverInfo;
	}

}
