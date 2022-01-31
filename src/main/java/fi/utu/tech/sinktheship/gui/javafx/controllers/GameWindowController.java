package fi.utu.tech.sinktheship.gui.javafx.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import fi.utu.tech.sinktheship.GameInstance;
import fi.utu.tech.sinktheship.game.GameRules;
import fi.utu.tech.sinktheship.game.GameState;
import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.network.packet.ChatPacket;
import fi.utu.tech.sinktheship.network.packet.ForfeitPacket;
import fi.utu.tech.sinktheship.network.packet.GameOverPacket;
import fi.utu.tech.sinktheship.network.packet.SetupReadyPacket;
import fi.utu.tech.sinktheship.network.packet.ShipDestroyedPacket;
import fi.utu.tech.sinktheship.network.packet.StartGamePacket;
import fi.utu.tech.sinktheship.network.packet.TurnPacket;
import fi.utu.tech.sinktheship.network.packet.GameStatePacket;
import fi.utu.tech.sinktheship.network.packet.GameStateRequestPacket;
import fi.utu.tech.sinktheship.network.packet.MoveResponsePacket;
import fi.utu.tech.sinktheship.network.packet.NewGamePacket;
import fi.utu.tech.sinktheship.network.packet.PlayerInfoPacket;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Game Window controller for the fxml based game window.
 */
public class GameWindowController implements Initializable {
	@FXML
	private StackPane root;
	@FXML
	private StackPane dragPane;

	@FXML
	private Button startGameButton;
	@FXML
	private Button forfeitButton;
	@FXML
	private Button readyButton;
	@FXML
	private Button shuffleButton;

	@FXML
	private GridController myGridController;
	@FXML
	private GridController enemyGridController;

	@FXML
	private TextField playerNameInput;
	@FXML
	private Label enemyName;
	@FXML
	private Label enemyReady;

	@FXML
	private TextArea chatArea;
	@FXML
	private TextField chatInput;

	@FXML
	private Label gameStateLabel;
	@FXML
	private Label gameInfoLabel;
	@FXML
	private Label victoryLabel;
	@FXML
	private Label victorySubLabel;

	@FXML
	private Button newGameButton;

	@FXML
	private VBox infoVBox;
	@FXML
	private VBox victoryVBox;

	@FXML
	private GameRulesController rulesController;

	private SimpleBooleanProperty noAuthorityProperty = new SimpleBooleanProperty(true);
	private SimpleBooleanProperty gameRunning = new SimpleBooleanProperty(false);
	private SimpleObjectProperty<GameState> gameStateProperty;
	private Boolean isMyTurn = false;
	private Client client;
	private GameRules finalizedRules = null;

	/**
	 * Bindings etc.
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		root.parentProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				GameInstance.shutdownServer();
				client.disconnect();
			}
		});

		playerNameInput.setText(GameInstance.getPlayerName());

		gameStateProperty = new SimpleObjectProperty<>(GameState.NEW_GAME);
		startGameButton.disableProperty().bind(rulesController.rulesOkProperty().not().or(noAuthorityProperty));
		startGameButton.visibleProperty().bind(gameRunning.not());

		newGameButton.visibleProperty().bind(gameStateProperty.isEqualTo(GameState.END));
		newGameButton.disableProperty().bind(noAuthorityProperty);

		infoVBox.visibleProperty().bind(newGameButton.visibleProperty().not());

		victoryVBox.visibleProperty().bind(newGameButton.visibleProperty());

		readyButton.visibleProperty().bind(gameStateProperty.isEqualTo(GameState.SETUP));
		readyButton.disableProperty().bind(myGridController.griOkProperty().not());
		readyButton.textProperty()
				.bind(Bindings.when(myGridController.shipMoveDisabledProperty()).then("Peruuta").otherwise("Valmis"));

		shuffleButton.visibleProperty().bind(readyButton.visibleProperty());
		shuffleButton.disableProperty().bind(myGridController.shipMoveDisabledProperty());

		forfeitButton.visibleProperty()
				.bind(gameStateProperty.isEqualTo(GameState.SETUP).or(gameStateProperty.isEqualTo(GameState.PLAY)));

		myGridController.bindGridSizeProperty(rulesController.gridSizeProperty());
		enemyGridController.bindGridSizeProperty(rulesController.gridSizeProperty());

		updateGameInfo();

		// Only the server can change the rules, and while the game is not running.
		rulesController.bindDisable(gameRunning.or(noAuthorityProperty));
	}

	/**
	 * Helper method to get the current game state as a readable string. Used as the
	 * game state label.
	 * 
	 * @return game state string
	 */
	public String gameStateToString() {
		if (client == null) {
			return "";
		}
		switch (gameStateProperty.get()) {
			case END:
				return "Peli päättynyt";
			case NEW_GAME:
				return client.hasAuthority() ? "Säädä pelin asetukset" : "Odotetaan asetuksia";
			case PLAY:
				return finalizedRules != null && finalizedRules.fastMode ? "Pikapeli"
						: isMyTurn ? "Sinun vuorosi" : "Vastustajan vuoro";
			case SETUP:
				return "Aseta aluksesi";
			case STARTING:
				return "Peli alkaa hetken kuluttua";
		}
		return "";
	}

	/**
	 * Helper method to get the current game state's additional info, if present.
	 * 
	 * @return
	 */
	public String gameInfoToString() {
		switch (gameStateProperty.get()) {
			case SETUP:
				return "Paina valmis kun alukset ovat paikoillaan";
			case PLAY:
				return finalizedRules != null && finalizedRules.fastMode
						? "Voittaja on vähimmillä siirroilla alukset tuhonut pelaaja"
						: "";
			default:
				return "";
		}
	}

	/**
	 * Updates the game state labels.
	 */
	private void updateGameInfo() {
		Platform.runLater(() -> {
			gameStateLabel.setText(gameStateToString());
			gameInfoLabel.setText(gameInfoToString());
		});
	}

	/**
	 * Updates the game state, sets and resets things on the window.
	 */
	private void updateGameState() {
		Platform.runLater(() -> {
			switch (gameStateProperty.get()) {
				case NEW_GAME:
					myGridController.reset();
					enemyGridController.reset();
					gameRunning.set(false);
					break;
				case SETUP:
					myGridController.setShipMoveDisabled(false);
					enemyGridController.createGrid(rulesController.getRules());
					myGridController.createGrid(rulesController.getRules());
					myGridController.addShips();
					break;
				case STARTING:
					break;
				case PLAY:
					break;
				case END:
					break;
			}
		});

	}

	/**
	 * Updates the text on the victory label.
	 * 
	 * @param winner am I the winner?
	 */
	private void updateVictoryLabel(boolean winner) {
		if (winner) {
			victoryLabel.setText("Sinä voitit!");
			victorySubLabel.setText("Onneksi olkoon!");
		} else {
			victoryLabel.setText("Sinä hävisit!");
			victorySubLabel.setText("Parempi tuuri ensi kerralla!");
		}

	}

	/**
	 * Sets the network client and binds a packet listener to it.
	 * 
	 */
	public void setClient(Client client) {
		this.client = client;
		client.transmit(new GameStateRequestPacket());
		client.transmit(new PlayerInfoPacket(GameInstance.getPlayerName()));
		rulesController.setClient(client);
		noAuthorityProperty.set(!client.hasAuthority());
		enemyGridController.setClient(client);
		myGridController.setClient(client);
		client.packetReceived.addListener(event -> {
			var packet = event.getValue();

			// Game is starting, get the rules from the server.
			if (packet instanceof StartGamePacket) {
				finalizedRules = ((StartGamePacket) packet).rules;
				Platform.runLater(() -> {
					gameRunning.set(true);
				});
			}

			// The game state has changed on the server.
			else if (packet instanceof GameStatePacket) {
				var state = ((GameStatePacket) packet).state;
				gameStateProperty.set(state);
				updateGameInfo();
				updateGameState();
			}

			// Tells the game whose turn it is.
			else if (packet instanceof TurnPacket) {
				var tp = (TurnPacket) packet;
				isMyTurn = tp.clientId == client.getId();
				enemyGridController.setInteractive(isMyTurn);
				updateGameInfo();
			}

			// Game finished, who won?
			else if (packet instanceof GameOverPacket) {
				var gop = (GameOverPacket) packet;
				Platform.runLater(() -> updateVictoryLabel(gop.winnerClientId == client.getId()));
			}

			// A ship has been destroyed, display it on the opponents grid
			else if (packet instanceof ShipDestroyedPacket) {
				var sdp = (ShipDestroyedPacket) packet;
				if (sdp.instigator == client.getId()) {
					Platform.runLater(() -> {
						enemyGridController.addShip(sdp.shipPlacement);
					});
				}
			}

			// Was the move I made legal? What happened? React to it.
			else if (packet instanceof MoveResponsePacket) {
				var mrp = (MoveResponsePacket) event.getValue();
				boolean myMove = client.getId() == mrp.instigator;
				System.out.println("mrp " + mrp.instigator + " : " + mrp.index + " : " + mrp.status);
				Platform.runLater(() -> {
					switch (mrp.status) {
						case HitEmpty:
							if (myMove) {
								enemyGridController.registerHit(mrp.index, false);
							} else {
								myGridController.registerHit(mrp.index, false);
							}
							break;
						case HitShip:
							if (myMove) {
								enemyGridController.registerHit(mrp.index, true);
							} else {
								myGridController.registerHit(mrp.index, true);
							}
							break;
						case InvalidIndex:
							if (myMove) {
								myGridController.setInteractive(true);
							}
							break;
						case NotMyTurn:
							if (myMove) {
								myGridController.setInteractive(false);
							}
							break;
					}
				});
			}

			// Update the chat window.
			else if (packet instanceof ChatPacket) {
				var cp = (ChatPacket) packet;
				Platform.runLater(() -> chatArea.appendText("\n" + cp.text));
			}

			// Get player info from the server and set things accordingly.
			else if (packet instanceof PlayerInfoPacket) {
				var pif = (PlayerInfoPacket) packet;
				if (pif.isPlayer && pif.clientId != client.getId()) {
					Platform.runLater(() -> enemyName.setText(pif.name));
				}
			}
		});

	}

	/**
	 * Start the game button. Only the authorative client can do this. There must be
	 * two players for the server to react currently.
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onStartButtonAction(ActionEvent event) {
		client.transmit(new StartGamePacket(rulesController.getRules()));
	}

	/**
	 * Forfeit.
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onForfeitButtonAction(ActionEvent event) {
		client.transmit(new ForfeitPacket());
	}

	/**
	 * I am ready button. The game starts when both players are ready.
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onReadyButtonAction(ActionEvent event) {
		myGridController.setShipMoveDisabled(!myGridController.isShipMoveDisabled());
		boolean ready = myGridController.isShipMoveDisabled();
		client.transmit(new SetupReadyPacket(ready, myGridController.getShipPlacements()));
	}

	/**
	 * Shuffle the ships on the board randomly
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onShuffleButtonAction(ActionEvent event) {
		myGridController.shuffle();
	}

	/**
	 * Start a new game after a game has been finished
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onNewGameButtonAction(ActionEvent event) {
		client.transmit(new NewGamePacket());
	}

	/**
	 * Send a chat message to the server that has been written on the chat box.
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onChatInputAction(ActionEvent event) {
		var text = chatInput.getText();
		chatInput.clear();
		client.transmit(new ChatPacket(text));
	}

	/**
	 * Change the player name and send it to the server.
	 * 
	 * @param event from JavafX
	 */
	@FXML
	private void onPlayerNameInputAction(ActionEvent event) {
		GameInstance.setPlayerName(playerNameInput.getText());
		root.requestFocus();
		client.transmit(new PlayerInfoPacket(playerNameInput.getText()));
	}
}
