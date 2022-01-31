package fi.utu.tech.sinktheship.gui.javafx.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import fi.utu.tech.sinktheship.gui.javafx.components.ServerEntry;
import fi.utu.tech.sinktheship.gui.javafx.tasks.ConnectTask;
import fi.utu.tech.sinktheship.gui.javafx.tasks.ScanLANIPsTask;
import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.network.RemoteClient;
import fi.utu.tech.sinktheship.network.Server;
import fi.utu.tech.sinktheship.network.ServerInfo;
import fi.utu.tech.sinktheship.GameInstance;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/**
 * Server window controller for the fxml based server window.
 */
public class ServerWindowController implements Initializable {
	@FXML
	private ListView<ServerEntry> serverListView;
	@FXML
	private StackPane header;
	@FXML
	private Button joinButton;
	@FXML
	private Button spectateButton;
	@FXML
	private Button refreshButton;
	@FXML
	private Button addressConnectButton;
	@FXML
	private TextField addressField;

	private Service<ArrayList<ServerInfo>> lanIpScanService = new Service<>() {
		@Override
		protected Task<ArrayList<ServerInfo>> createTask() {
			return new ScanLANIPsTask(100);
		}
	};

	private boolean directConnect = false;

	private Service<Client> connectService = new Service<>() {

		@Override
		protected Task<Client> createTask() {
			if (directConnect) {
				return new ConnectTask(addressField.getText(), Server.defaultPort, false);
			} else {
				var selected = serverListView.getSelectionModel().getSelectedItem();
				if (selected != null) {
					return new ConnectTask(selected.getInfo().address.getHostAddress(), selected.getInfo().port, false);
				}
			}
			return null;
		}
	};

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		var headercontent = new ServerEntry("Palvelin", "Ping", "Pelaajia", "Katsojia");
		header.getChildren().add(headercontent);
		StackPane.setAlignment(headercontent, Pos.CENTER);

		lanIpScanService.valueProperty().addListener((observable, oldValue, newValue) -> {
			updateServerList(newValue);
		});

		refreshButton.disableProperty().bind(lanIpScanService.runningProperty());
		refreshButton.textProperty()
				.bind(Bindings.when(lanIpScanService.runningProperty()).then("Haetaan...").otherwise("Päivitä"));

		addressConnectButton.disableProperty().bind(connectService.runningProperty());

		connectService.stateProperty().addListener((observable, oldvalue, newvalue) -> {
			switch (newvalue) {
				case SUCCEEDED:
					if (connectService.getValue() != null) {
						GameInstance.getGui().loadGame(connectService.getValue());
					}
					break;
				default:
					break;

			}
		});

		joinButton.disableProperty().bind(serverListView.getSelectionModel().selectedItemProperty().isNull()
				.or(connectService.runningProperty()));
		spectateButton.disableProperty().bind(serverListView.getSelectionModel().selectedItemProperty().isNull());
	}

	private void updateServerList(ArrayList<ServerInfo> infos) {
		serverListView.getItems().clear();
		if (infos != null) {
			for (var info : infos) {
				serverListView.getItems().add(new ServerEntry(info));
			}
		}
	}

	@FXML
	private void onRefreshAction(ActionEvent event) {
		if (!lanIpScanService.isRunning()) {
			lanIpScanService.restart();
		}
	}

	@FXML
	private void onJoinAction(ActionEvent event) {
		if (!connectService.isRunning()) {
			directConnect = false;
			connectService.restart();
		}
	}

	@FXML
	private void onSpectateAction(ActionEvent event) {
		var selected = serverListView.getSelectionModel().getSelectedItem();
		if (selected != null) {
			GameInstance.getGui()
					.loadGame(RemoteClient.connect(selected.getInfo().address, selected.getInfo().port, true));
		}
	}

	@FXML
	private void onAddressFieldAction(ActionEvent event) {
		header.requestFocus();
	}

	@FXML
	private void onAddressConnectAction(ActionEvent event) {
		if (!connectService.isRunning()) {
			directConnect = true;
			connectService.restart();
		}
	}
}
