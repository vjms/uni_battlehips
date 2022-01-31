package fi.utu.tech.sinktheship.gui.javafx.controllers;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ResourceBundle;

import fi.utu.tech.sinktheship.GameInstance;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Controller used for mainWindow.fxml
 */
public class MainWindowController implements Initializable {
	@FXML
	private StackPane root;
	@FXML
	private StackPane appRegion;
	@FXML
	private HBox topBar;
	@FXML
	private Button iconifyButton;
	@FXML
	private Button fullscreenButton;
	@FXML
	private Button exitButton;
	@FXML
	private Button backButton;
	@FXML
	private Label titleLabel;
	@FXML
	private Rectangle backgroundimg;
	@FXML
	private Pane background;
	@FXML
	private Button soundButton;

	private final double resizeAreaWidth = 3.0;

	private Deque<Parent> backTrace = new ArrayDeque<>();

	private SimpleBooleanProperty soundEnabledProperty = new SimpleBooleanProperty(true);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		root.addEventFilter(MouseEvent.ANY, new MouseMoveAndResizeFilter());
		backButton.setVisible(false);
		background.opacityProperty().bind(Bindings.when(backButton.visibleProperty()).then(0.85).otherwise(0.3));
		soundButton.textProperty().bind(Bindings.when(soundEnabledProperty).then("🔊").otherwise("🔇"));
	}

	public void setBackground(String source) {
		backgroundimg.setFill(new ImagePattern(new Image(source)));
	}

	public void setView(Parent parent) {
		backTrace.add(parent);

		changeView(parent);
	}

	private void changeView(Parent parent) {
		backButton.setVisible(backTrace.size() > 1);

		appRegion.getChildren().clear();
		appRegion.getChildren().add(parent);

		// Window scaling
		parent.scaleXProperty().bind(appRegion.widthProperty().divide(appRegion.getMinWidth()));
		parent.scaleYProperty().bind(appRegion.heightProperty().divide(appRegion.getMinHeight()));

		root.requestFocus();

	}

	public void close(Parent parent) {
		if (appRegion.getChildren().get(0).equals(parent)) {
			goBack();
		}
	}

	public boolean soundEnabled() {
		return soundEnabledProperty.get();
	}

	@FXML
	private void onSoundButtonAction(ActionEvent event) {
		soundEnabledProperty.set(!soundEnabledProperty.get());
	}

	/**
	 * Set title on the top bar.
	 * 
	 * @param title new title
	 */
	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	/**
	 * Handles the iconifying of the window.
	 * 
	 * @param event from JavaFX
	 */
	@FXML
	private void onIconifyButtonPressed(ActionEvent event) {
		var stage = getStage();
		stage.setIconified(true);
	}

	/**
	 * Handles the minimizing and maximizing of the window.
	 * 
	 * @param event from JavaFX
	 */
	@FXML
	private void onFullscreenButtonAction(ActionEvent event) {
		toggleFullscreen();
	}

	/**
	 * Quits the application.
	 * 
	 * @param event from JavaFX
	 */
	@FXML
	private void onExitButtonAction(ActionEvent event) {
		GameInstance.quit();
	}

	@FXML
	private void onBackButtonAction(ActionEvent event) {
		goBack();
	}

	private void goBack() {
		backTrace.removeLast();
		changeView(backTrace.peek());

	}

	/**
	 * Handles moving the window. Press locations are provided by the
	 * MouseMoveAndResizeFilter inner class.
	 * 
	 * @param event from JavaFX
	 */
	@FXML
	private void onTopBarMouseDragged(MouseEvent event) {
		var stage = getStage();
		if (stage.isFullScreen()) {
			stage.setFullScreen(false);
		}
		if (event.getButton() == MouseButton.PRIMARY) {
			stage.setX(event.getScreenX() - mousePressSceneLocX);
			stage.setY(event.getScreenY() - mousePressSceneLocY);
		}
	}

	@FXML
	private void onTopBarMouseClicked(MouseEvent event) {
		if (event.getClickCount() > 1) {
			toggleFullscreen();
		}
	}

	private void toggleFullscreen() {
		var stage = getStage();
		stage.setFullScreen(!stage.isFullScreen());
	}

	private double mousePressSceneLocX = .0;
	private double mousePressSceneLocY = .0;
	private double mousePressScreenLocX = .0;
	private double mousePressScreenLocY = .0;
	private double mousePressSceneWidth = .0;
	private double mousePressSceneHeight = .0;

	private enum ResizeDirection {
		NONE, TOP_LEFT, TOP, TOP_RIGHT, LEFT, RIGHT, BOT_LEFT, BOT, BOT_RIGHT
	}

	/**
	 * Used to handle resizing the window. Consumes mouse events when mouse is in
	 * the resize regions.
	 * 
	 * Top bar move also uses the scene locations that this inner class provides.
	 */
	private class MouseMoveAndResizeFilter implements EventHandler<MouseEvent> {
		private ResizeDirection resizeDirection = ResizeDirection.NONE;

		@Override
		public void handle(MouseEvent event) {
			var stage = getStage();
			if (stage.isFullScreen()) {
				return;
			}

			if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				// Save various variables that can be used later.
				mousePressSceneLocX = event.getSceneX();
				mousePressSceneLocY = event.getSceneY();
				mousePressScreenLocX = event.getScreenX();
				mousePressScreenLocY = event.getScreenY();
				mousePressSceneWidth = root.getWidth();
				mousePressSceneHeight = root.getHeight();
			} else if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
				// We want to constantly check whether mouse is in the area on mouse movement.
				inAreaCheck(event);
			} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && handleResize(event)) {
				// We don't want anyone to use this event later if we are resizing
				event.consume();
			} else if (inResizeArea() && (event.getEventType() == MouseEvent.MOUSE_PRESSED
					|| event.getEventType() == MouseEvent.MOUSE_CLICKED
					|| event.getEventType() == MouseEvent.MOUSE_RELEASED)) {
				// Also when the cursor is in the resize area, we shouldn't allow the events
				// mentioned above to propagate.
				event.consume();
			}
		}

		/**
		 * Checks if the cursor is at the resize regions, and changes the cursor
		 * accordingly.
		 * 
		 * @param event Mouse event should be MOUSE_MOVED
		 */
		private void inAreaCheck(MouseEvent event) {
			Cursor cursor = Cursor.DEFAULT;
			boolean left = event.getSceneX() < resizeAreaWidth;
			boolean right = event.getSceneX() > (root.getScene().getWidth() - resizeAreaWidth);
			boolean top = event.getSceneY() < resizeAreaWidth;
			boolean bot = event.getSceneY() > (root.getScene().getHeight() - resizeAreaWidth);
			if (top) {
				if (left) {
					resizeDirection = ResizeDirection.TOP_LEFT;
					cursor = Cursor.NW_RESIZE;
				} else if (right) {
					resizeDirection = ResizeDirection.TOP_RIGHT;
					cursor = Cursor.NE_RESIZE;
				} else {
					resizeDirection = ResizeDirection.TOP;
					cursor = Cursor.V_RESIZE;
				}
			} else if (bot) {
				if (left) {
					resizeDirection = ResizeDirection.BOT_LEFT;
					cursor = Cursor.NE_RESIZE;
				} else if (right) {
					resizeDirection = ResizeDirection.BOT_RIGHT;
					cursor = Cursor.NW_RESIZE;
				} else {
					resizeDirection = ResizeDirection.BOT;
					cursor = Cursor.V_RESIZE;
				}
			} else if (right) {
				resizeDirection = ResizeDirection.RIGHT;
				cursor = Cursor.H_RESIZE;
			} else if (left) {
				resizeDirection = ResizeDirection.LEFT;
				cursor = Cursor.H_RESIZE;
			} else {
				resizeDirection = ResizeDirection.NONE;
			}
			root.getScene().setCursor(cursor);
		}

		/**
		 * Helper method for clarity of code.
		 * 
		 * @return is in the resize area
		 */
		private boolean inResizeArea() {
			return resizeDirection != ResizeDirection.NONE;
		}

		/**
		 * Handles the resizing of the window.
		 * 
		 * @param event Mouse event should be MOUSE_DRAGGED
		 * @return was handled.
		 */
		private boolean handleResize(MouseEvent event) {
			// No need to resize.
			if (resizeDirection == ResizeDirection.NONE) {
				return false;
			}
			double deltaX = 0.0;
			double deltaY = 0.0;
			double newWidth = event.getSceneX();
			double newHeight = event.getSceneY();

			// Calculate the delta distance from the point where mouse was pressed.
			if (resizeDirection == ResizeDirection.TOP_LEFT || resizeDirection == ResizeDirection.BOT_LEFT
					|| resizeDirection == ResizeDirection.LEFT) {
				deltaX = mousePressScreenLocX - event.getScreenX();
				newWidth = mousePressSceneWidth + deltaX;
			}
			if (resizeDirection == ResizeDirection.TOP_LEFT || resizeDirection == ResizeDirection.TOP_RIGHT
					|| resizeDirection == ResizeDirection.TOP) {
				deltaY = mousePressScreenLocY - event.getScreenY();
				newHeight = mousePressSceneHeight + deltaY;
			}

			Stage stage = getStage();

			// The stage allows resizing even when min size is set, so we define our own
			// constraint here.
			if (resizeHorizontally() && root.getMinWidth() < newWidth) {

				// We need to move the screen to the mouse location if the resize was done from
				// the left or top sides to make it appear as if we were only resizing.
				// Otherwise it would try to resize from the right and it doesn't make sense.
				if (deltaX != 0.0)
					stage.setX(event.getScreenX());
				stage.setWidth(newWidth);
			}
			// Do the same stuff for vertical resizing.
			if (resizeVertically() && root.getMinHeight() < newHeight) {
				if (deltaY != 0.0)
					stage.setY(event.getScreenY());
				stage.setHeight(newHeight);
			}
			return true;
		}

		/**
		 * Check if the resize direction is directional
		 * 
		 * @return is diagonal
		 */
		private boolean diagonal() {
			return resizeDirection == ResizeDirection.TOP_LEFT || resizeDirection == ResizeDirection.BOT_LEFT
					|| resizeDirection == ResizeDirection.BOT_RIGHT || resizeDirection == ResizeDirection.TOP_RIGHT;
		}

		/**
		 * Check whether we should resize horizontally or not.
		 * 
		 * @return shuld resize
		 */
		private boolean resizeHorizontally() {
			return diagonal() || resizeDirection == ResizeDirection.LEFT || resizeDirection == ResizeDirection.RIGHT;
		}

		/**
		 * Check whether we should resize vertically or not.
		 * 
		 * @return should resize
		 */
		private boolean resizeVertically() {
			return diagonal() || resizeDirection == ResizeDirection.TOP || resizeDirection == ResizeDirection.BOT;
		}
	}

	/**
	 * Helper method to get the stage of the root.
	 * 
	 * @return the stage of the window
	 */
	private Stage getStage() {
		return (Stage) root.getScene().getWindow();

	}
}
