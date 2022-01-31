package fi.utu.tech.sinktheship.gui.javafx;

import java.io.IOException;

import fi.utu.tech.sinktheship.gui.javafx.controllers.GameWindowController;
import fi.utu.tech.sinktheship.gui.javafx.controllers.MainMenuController;
import fi.utu.tech.sinktheship.gui.javafx.controllers.MainWindowController;
import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.GameInstance;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUI extends Application {
    public static final String title = "Laivanupotuspeli";

    private Scene mainScene;
    private ParentControllerPair<Parent, MainWindowController> mainWindow;
    private ParentControllerPair<Parent, MainMenuController> mainMenu;

    public GUI() {
        GameInstance.setGui(this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainWindow = ResourceLoader.FXML("views/mainWindow.fxml");
        mainMenu = ResourceLoader.FXML("views/mainMenu.fxml");

        mainWindow.controller.setTitle(title);
        mainWindow.controller.setView(mainMenu.parent);
        mainWindow.controller.setBackground(ResourceLoader.image("images/background.png"));

        mainScene = new Scene(mainWindow.parent);
        mainScene.getStylesheets().add(ResourceLoader.stylesheet("styles/mainStyle.css"));

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle(title);

        primaryStage.show();
        mainWindow.parent.requestFocus();
    }

    public void changeView(Parent parent) {
        mainWindow.controller.setView(parent);
    }

    public void loadGame(Client client) throws NullPointerException {
        if (client == null) {
            throw new NullPointerException("Client not initialized");
        }
        try {
            var gameWindow = ResourceLoader.<Parent, GameWindowController>FXML("views/gameWindow.fxml");
            gameWindow.controller.setClient(client);
            changeView(gameWindow.parent);
            client.clientClosed
                    .addListener(event -> Platform.runLater(() -> mainWindow.controller.close(gameWindow.parent)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadServerView() {
        try {
            var serverView = ResourceLoader.<Parent, Object>FXML("views/serverWindow.fxml");
            changeView(serverView.parent);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void quit() {
        Platform.exit();
    }

    public void playAudioClip(String source) {
        if (mainWindow.controller.soundEnabled()) {
            var audio = new AudioClip(ResourceLoader.audioClip(source));
            audio.play();

        }
    }
}
