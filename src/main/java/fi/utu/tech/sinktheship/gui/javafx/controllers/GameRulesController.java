package fi.utu.tech.sinktheship.gui.javafx.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import fi.utu.tech.sinktheship.game.GameRules;
import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.network.packet.GameRulesPacket;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * Game rules controller for the fxml based rules window.
 */
public class GameRulesController implements Initializable {
	@FXML
	private Pane root;
	@FXML
	private GridPane grid;

	@FXML
	private Slider gridSizeSlider;
	@FXML
	private Label gridSizeIndicator;

	@FXML
	private Slider carrierCountSlider;
	@FXML
	private Label carrierCountIndicator;
	@FXML
	private Slider battleshipCountSlider;
	@FXML
	private Label battleshipCountIndicator;
	@FXML
	private Slider cruiserCountSlider;
	@FXML
	private Label cruiserCountIndicator;
	@FXML
	private Slider submarineCountSlider;
	@FXML
	private Label submarineCountIndicator;
	@FXML
	private Slider destroyerCountSlider;
	@FXML
	private Label destroyerCountIndicator;

	@FXML
	private CheckBox fastGameModeCheckBox;

	@FXML
	private Slider timeLimitSlider;
	@FXML
	private Label timeLimitIndicator;

	@FXML
	private Label rulesOkLabel;

	private SimpleIntegerProperty gridSizeProperty = new SimpleIntegerProperty();
	private SimpleIntegerProperty carrierCountProperty = new SimpleIntegerProperty();
	private SimpleIntegerProperty battleshipCountProperty = new SimpleIntegerProperty();
	private SimpleIntegerProperty cruiserCountProperty = new SimpleIntegerProperty();
	private SimpleIntegerProperty submarineCountProperty = new SimpleIntegerProperty();
	private SimpleIntegerProperty destroyerCountProperty = new SimpleIntegerProperty();
	private SimpleBooleanProperty rulesOkProperty = new SimpleBooleanProperty();

	private GameRules rules = new GameRules();

	private Client client = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Create bindings
		gridSizeProperty.bind(gridSizeSlider.valueProperty());
		carrierCountProperty.bind(carrierCountSlider.valueProperty());
		battleshipCountProperty.bind(battleshipCountSlider.valueProperty());
		cruiserCountProperty.bind(cruiserCountSlider.valueProperty());
		submarineCountProperty.bind(submarineCountSlider.valueProperty());
		destroyerCountProperty.bind(destroyerCountSlider.valueProperty());

		gridSizeIndicator.textProperty().bind(Bindings.format("%d x %d", gridSizeProperty, gridSizeProperty));
		carrierCountIndicator.textProperty().bind(carrierCountProperty.asString());
		battleshipCountIndicator.textProperty().bind(battleshipCountProperty.asString());
		cruiserCountIndicator.textProperty().bind(cruiserCountProperty.asString());
		submarineCountIndicator.textProperty().bind(submarineCountProperty.asString());
		destroyerCountIndicator.textProperty().bind(destroyerCountProperty.asString());

		timeLimitSlider.valueProperty().addListener((Observable, oldValue, newValue) -> {
			var value = newValue.intValue();
			var rounding = Math.round((value % 5 + 0.5) * 0.2) * 5;
			timeLimitIndicator.setText(String.format("0:%02d", (value - value % 5) + rounding));
		});

		rulesOkLabel.textProperty().bind(Bindings.createStringBinding(
				() -> rules.rulesOk() ? "" : rules.getShipCount() == 0 ? "Vähintään 1 alus" : "Liian monta alusta",
				rulesOkProperty));

		// Bind slider values to game state
		gridSizeProperty.addListener((observable, oldValue, newValue) -> {
			updateGridSizeRule(newValue.intValue());
		});
		carrierCountProperty.addListener((observable, oldValue, newValue) -> {
			updateCarrierCountRule(newValue.intValue());
		});
		battleshipCountProperty.addListener((observable, oldValue, newValue) -> {
			updateBattleshipCountRule(newValue.intValue());
		});
		cruiserCountProperty.addListener((observable, oldValue, newValue) -> {
			updateCruiserCountRule(newValue.intValue());
		});
		submarineCountProperty.addListener((observable, oldValue, newValue) -> {
			updateSubmarineCountRule(newValue.intValue());
		});
		destroyerCountProperty.addListener((observable, oldValue, newValue) -> {
			updateDestroyerCountRule(newValue.intValue());
		});

		forceUpdate();
	}

	/**
	 * Sets the network client for data transmission
	 * 
	 * @param client the network client
	 */
	public void setClient(Client client) {
		this.client = client;
		client.packetReceived.addListener(event -> {
			if (event.getValue() instanceof GameRulesPacket) {
				var packet = (GameRulesPacket) event.getValue();
				Platform.runLater(() -> {
					gridSizeSlider.setValue(packet.rules.gridSize);
					carrierCountSlider.setValue(packet.rules.carrierCount);
					battleshipCountSlider.setValue(packet.rules.battleshipCount);
					cruiserCountSlider.setValue(packet.rules.cruiserCount);
					submarineCountSlider.setValue(packet.rules.submarineCount);
					destroyerCountSlider.setValue(packet.rules.destroyerCount);
				});
			}
		});
	}

	/**
	 * When the game is running, disable editing the game rules settings. Should
	 * also disable when acting as a client. Only server can modify the rules.
	 * 
	 * @param property
	 */
	public void bindDisable(BooleanBinding property) {
		root.disableProperty().bind(property);

	}

	public ReadOnlyIntegerProperty gridSizeProperty() {
		return gridSizeProperty;
	}

	public ReadOnlyBooleanProperty rulesOkProperty() {
		return rulesOkProperty;
	}

	/**
	 * Transmit the rules to the server if the client has authority.
	 */
	private void transmitRules() {
		if (client != null && client.hasAuthority()) {
			client.transmit(new GameRulesPacket(new GameRules(rules)));
		}
	}

	/**
	 * gets the rules
	 * 
	 * @return rules
	 */
	public GameRules getRules() {
		return new GameRules(rules);
	}

	/**
	 * Force rule updates.
	 */
	private void forceUpdate() {
		updateGridSizeRule(gridSizeProperty.get());
		updateCarrierCountRule(carrierCountProperty.get());
		updateBattleshipCountRule(battleshipCountProperty.get());
		updateCruiserCountRule(cruiserCountProperty.get());
		updateSubmarineCountRule(submarineCountProperty.get());
		updateDestroyerCountRule(destroyerCountProperty.get());
	}

	private void updateGridSizeRule(int newValue) {
		rules.gridSize = newValue;
		rulesOkProperty.set(rules.rulesOk());
		transmitRules();
	}

	private void updateCarrierCountRule(int newValue) {
		rules.carrierCount = newValue;
		rulesOkProperty.set(rules.rulesOk());
		transmitRules();
	}

	private void updateBattleshipCountRule(int newValue) {
		rules.battleshipCount = newValue;
		rulesOkProperty.set(rules.rulesOk());
		transmitRules();
	}

	private void updateCruiserCountRule(int newValue) {
		rules.cruiserCount = newValue;
		rulesOkProperty.set(rules.rulesOk());
		transmitRules();
	}

	private void updateSubmarineCountRule(int newValue) {
		rules.submarineCount = newValue;
		rulesOkProperty.set(rules.rulesOk());
		transmitRules();
	}

	private void updateDestroyerCountRule(int newValue) {
		rules.destroyerCount = newValue;
		rulesOkProperty.set(rules.rulesOk());
		transmitRules();
	}
}
