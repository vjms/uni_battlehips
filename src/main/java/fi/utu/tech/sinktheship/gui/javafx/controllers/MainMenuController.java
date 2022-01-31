package fi.utu.tech.sinktheship.gui.javafx.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import fi.utu.tech.sinktheship.GameInstance;
import fi.utu.tech.sinktheship.network.RemoteClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Main menu controller for the fxml based main menu window.
 */
public class MainMenuController implements Initializable {
	@FXML
	private Button createGameButton;
	@FXML
	private Button joinGameButton;
	@FXML
	private Button exitGameButton;
	@FXML
	private TextField nameField;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		nameField.setText(GameInstance.getPlayerName());
	}

	/**
	 * Create a game server and join it. Switch windows.
	 * 
	 * @param event From JavaFX
	 */
	@FXML
	private void onCreateGameAction(ActionEvent event) {
		updateName();
		GameInstance.createServer();
		var client = RemoteClient.createLocal();
		GameInstance.getGui().loadGame(client);
	}

	/**
	 * Open the server menu.
	 * 
	 * @param event From JavaFX
	 */
	@FXML
	private void onJoinGameAction(ActionEvent event) {
		updateName();
		GameInstance.getGui().loadServerView();
	}

	/**
	 * Exit the game, functions similarly as to pressing the X on the window.
	 * 
	 * @param event From JavaFX
	 */
	@FXML
	private void onExitGameAction(ActionEvent event) {
		GameInstance.quit();
	}

	/**
	 * Change name.
	 * 
	 * @param event From JavaFX
	 */
	@FXML
	private void onNameFieldAction(ActionEvent event) {
		updateName();
	}

	/**
	 * Give the name to the game instance.
	 * 
	 * @param event From JavaFX
	 */
	private void updateName() {
		GameInstance.setPlayerName(nameField.getText());
		createGameButton.getParent().requestFocus();
	}
}
