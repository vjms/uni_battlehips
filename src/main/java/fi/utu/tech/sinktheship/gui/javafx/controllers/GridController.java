package fi.utu.tech.sinktheship.gui.javafx.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import fi.utu.tech.sinktheship.gui.javafx.components.GridTile;
import fi.utu.tech.sinktheship.gui.javafx.components.ShipRectangle;
import fi.utu.tech.sinktheship.network.Client;
import fi.utu.tech.sinktheship.network.packet.MovePacket;
import fi.utu.tech.sinktheship.game.GameRules;
import fi.utu.tech.sinktheship.game.Grid;
import fi.utu.tech.sinktheship.game.ShipPlacement;
import fi.utu.tech.sinktheship.ships.Ship;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;

/**
 * Grid controller for the fxml based grid window.
 */
public class GridController implements Initializable {
	@FXML
	private Pane root;
	@FXML
	private TilePane tileGrid;
	@FXML
	private Pane shipsPane;

	private int clickedTileIndex;
	private int lastMouseOverGridIndex;
	private int currentMouseOverGridIndex;
	private boolean interactive = false;

	private SimpleBooleanProperty gridOkProperty = new SimpleBooleanProperty();

	private GameRules rules = null;
	private Grid grid;
	private Client client = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setShipMoveDisabled(true);
		tileGrid.setPrefHeight(root.getPrefHeight());
		tileGrid.setPrefWidth(root.getPrefWidth());
		shipsPane.setPrefHeight(root.getPrefHeight());
		shipsPane.setPrefWidth(root.getPrefWidth());

		root.addEventFilter(MouseEvent.ANY, event -> {
			currentMouseOverGridIndex = getTileIndexUnderMouseEvent(event);
			if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.getButton() == MouseButton.PRIMARY) {
				clickedTileIndex = currentMouseOverGridIndex;
			}
			if (interactive && event.getEventType() == MouseEvent.MOUSE_CLICKED) {
				fireAt(currentMouseOverGridIndex);
			}
		});

		tileGrid.prefColumnsProperty().addListener((observable, oldValue, newValue) -> {
			refreshGrid(newValue.intValue());
		});
	}

	/**
	 * the network client is needed everywhere...
	 * 
	 * @param client network client
	 */
	public void setClient(Client client) {
		this.client = client;

	}

	/**
	 * Ask the tile on the index to change is appearance.
	 * 
	 * @param index     the index of the tile.
	 * @param destroyed was there a ship?
	 */
	public void registerHit(int index, boolean destroyed) {
		System.out.println(this + " " + index + " " + tileGrid.getChildren().size());
		if (isValidTileIndex(index)) {
			((GridTile) tileGrid.getChildren().get(index)).destroy(destroyed);
		}
	}

	/**
	 * Reset the grid by clearing the tiles and ships and regenerating everything.
	 */
	public void reset() {
		shipsPane.getChildren().clear();
		setShipMoveDisabled(true);
		if (rules != null) {
			createGrid(rules);
			refreshGrid(rules.gridSize);
		}
	}

	/**
	 * Creates the grid based on the rules.
	 * 
	 * @param rules new rules
	 */
	public void createGrid(GameRules rules) {
		this.rules = rules;
		grid = new Grid(rules);

		grid.shipPlacedEventDispatcher.addListener(event -> {
			Platform.runLater(() -> {
				var rect = getShipRectangle(event.ship);
				rect.setIndex(event.index);
				moveShip(rect, event.index, 0);
				gridOkProperty.set(grid.isOk());
				refreshOverlappingShips();
			});
		});
	}

	/**
	 * Add ships to the grid based on the rules that were previously received.
	 */
	public void addShips() {
		shipsPane.getChildren().clear();
		for (var ship : grid.getShips().keySet()) {
			var rect = addShip(ship);
			var index = grid.findFirstCanPlaceSlot(rect.getShip());
			if (index != -1) {
				grid.placeShip(rect.getShip(), index);
				rect.addEventHandler(MouseEvent.ANY, new ShipDragHandler(rect));
			}
			refreshOverlappingShips();
		}
	}

	/**
	 * Shuffle the ships on the grid. Currently the grid sends events on ship
	 * placement that are used to place the ships on the grid, but this should maybe
	 * be refactored if this project were to be continued.
	 */
	public void shuffle() {
		if (grid != null) {
			grid.shuffleShips();
		}
	}

	/**
	 * Sends a message to the server that we want to shoot at the tile on the given
	 * index.
	 * 
	 * @param index index of the tile
	 */
	public void fireAt(int index) {
		client.transmit(new MovePacket(index));
	}

	/**
	 * Show all ships
	 * 
	 * @param newVisible yes or no
	 */
	public void setShipsVisible(boolean newVisible) {
		for (var child : shipsPane.getChildren()) {
			child.setVisible(newVisible);
		}
	}

	/**
	 * returns a read only property that can be used to check if the placed ships
	 * are following the rules of the game.
	 * 
	 * @return grid ok property
	 */
	public ReadOnlyBooleanProperty griOkProperty() {
		return gridOkProperty;
	}

	/**
	 * returns a read only property that can be used to check if the ship can be
	 * moved on the grid.
	 * 
	 * @return Ships can be moved property
	 */
	public ReadOnlyBooleanProperty shipMoveDisabledProperty() {
		return shipsPane.mouseTransparentProperty();
	}

	/**
	 * Set the rules of the game
	 * 
	 * @param rules new rules
	 */
	public void setRules(GameRules rules) {
		this.rules = rules;
	}

	/**
	 * External entities control the grid size. Expose the property this way.
	 * 
	 * @param property property to bind to.
	 */
	public void bindGridSizeProperty(ReadOnlyIntegerProperty property) {
		tileGrid.prefColumnsProperty().unbind();
		tileGrid.prefColumnsProperty().bind(property);
	}

	/**
	 * Extracts the tile index under the mouse during a mouse event. Used with
	 * tilegrid filters and handlers to set the current tile.
	 * 
	 * @param event From JavaFX
	 * @return tile index under mouse or last index if mouse were not in the grid.
	 */
	public int getTileIndexUnderMouseEvent(MouseEvent event) {
		// Mouse over grid check
		if ((event.getX() > 0.0 && event.getX() < tileGrid.getWidth())
				&& (event.getY() > 0.0 && event.getY() < tileGrid.getHeight())) {
			var x = (int) (event.getX() / getTileSize());
			var y = (int) (event.getY() / getTileSize());
			var index = x + y * tileGrid.getPrefColumns();
			return lastMouseOverGridIndex = index;
		}
		return lastMouseOverGridIndex;
	}

	/**
	 * Sets a new gridsize and redraws the grid. Clears all existing data.
	 * 
	 * @param gridSize new gridsize
	 */
	public void refreshGrid(int gridSize) {
		var cellCount = gridSize * gridSize;
		shipsPane.getChildren().clear();

		tileGrid.getChildren().clear();

		for (int i = 0; i < cellCount; i++) {
			tileGrid.getChildren().add(new GridTile());
		}

		var tileSize = tileGrid.getPrefHeight() / gridSize - tileGrid.vgapProperty().doubleValue();
		tileGrid.setPrefTileHeight(tileSize);
		tileGrid.setPrefTileWidth(tileSize);
	}

	/**
	 * Set the grid tiles to be interactive.
	 * 
	 * @param newValue is interactive?
	 */
	public void setInteractive(boolean newValue) {
		interactive = newValue;
		for (var child : tileGrid.getChildren()) {
			((GridTile) child).setInteractive(newValue);
		}
	}

	public boolean isShipMoveDisabled() {
		return shipsPane.mouseTransparentProperty().get();
	}

	/**
	 * Disable ship drag and drop. Useful when the game is running.
	 * 
	 * @param newValue should move be disabled?
	 */
	public void setShipMoveDisabled(boolean newValue) {
		shipsPane.setMouseTransparent(newValue);
		if (newValue && root.getChildren().get(0).equals(tileGrid)
				|| !newValue && root.getChildren().get(0).equals(shipsPane)) {
			var children = FXCollections.observableArrayList(root.getChildren());
			Collections.swap(children, 0, 1);
			root.getChildren().setAll(children);
		}
	}

	/**
	 * Adds a ship rectangle to the tile grid to the first place it can.
	 * 
	 * @param ship Ship data
	 */
	private ShipRectangle addShip(Ship ship) {
		var rect = new ShipRectangle(ship, getTileSize());
		shipsPane.getChildren().add(rect);
		return rect;
	}

	public void addShip(ShipPlacement placement) {
		var rect = addShip(Ship.fromType(placement.ship));
		if (placement.horizontal) {
			rect.rotate();
		}
		moveShip(rect, placement.index, 0);
	}

	/**
	 * Handles ship dragging.
	 */
	private class ShipDragHandler implements EventHandler<MouseEvent> {

		private ShipRectangle rect;
		private int moveOffset = 0;
		private boolean dragging = false;

		ShipDragHandler(ShipRectangle rect) {
			this.rect = rect;
		}

		@Override
		public void handle(MouseEvent event) {
			if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
				if (event.getButton() == MouseButton.PRIMARY) {
					dragging = false;
					commitPlacement(rect, currentMouseOverGridIndex, moveOffset);
					rect.setOpacity(1);
				}
			} else if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
				if (event.getButton() == MouseButton.PRIMARY) {
					rect.setOpacity(0.3);
					dragging = true;
					rect.setIsOverlapping(false);
					if (rect.horizontal()) {
						moveOffset = grid.getColumn(clickedTileIndex) - grid.getColumn(rect.getIndex());
					} else {
						moveOffset = grid.getRow(clickedTileIndex) - grid.getRow(rect.getIndex());
					}
				}
			} else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				if (dragging) {
					Event.fireEvent(tileGrid, event);
					if (currentMouseOverGridIndex >= 0) {
						moveShip(rect, currentMouseOverGridIndex, moveOffset);
					}
				}
			} else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
				if (event.getButton() == MouseButton.SECONDARY) {
					rect.rotate();
					if (!dragging) {
						commitPlacement(rect, rect.getIndex(), 0);
					} else {
						moveShip(rect, currentMouseOverGridIndex, moveOffset);
					}
				}
			}
		}
	}

	/**
	 * Places the ship to the grid position
	 * 
	 * @param rect   Ship to place
	 * @param index  index of the tile that the ship was dropped to
	 * @param offset offset index
	 */
	private void commitPlacement(ShipRectangle rect, int index, int offset) {
		int dropIndex = grid.getShipValidPlacementOffsetIndex(rect.getShip(), grid.getColumn(index), grid.getRow(index),
				offset);
		grid.placeShip(rect.getShip(), dropIndex);
	}

	/**
	 * Tells the ships whether they are overlapping or not.
	 */
	private void refreshOverlappingShips() {
		final var badships = grid.findOverlappingShips();
		for (var child : shipsPane.getChildren()) {
			var rect = (ShipRectangle) child;
			rect.setIsOverlapping(badships.contains(rect.getShip()));
		}

	}

	/**
	 * Move the ship within the grid, to the pointed index.
	 * 
	 * @param rect    Ship rectangle to move
	 * @param toIndex Must be a valid index
	 * @return index where the ship was moved to
	 */
	private int moveShip(ShipRectangle rect, int toIndex, int moveOffset) {
		toIndex = grid.getShipValidPlacementOffsetIndex(rect.getShip(), grid.getColumn(toIndex), grid.getRow(toIndex),
				moveOffset);
		final var tile = getGridTile(toIndex);
		final var targetX = tile.getLocalToParentTransform().getTx();
		final var targetY = tile.getLocalToParentTransform().getTy();
		final var gridTransform = shipsPane.getLocalToParentTransform();
		final var tileSize = getTileSize();
		final var gridScaleX = gridTransform.getMxx();
		final var gridScaleY = gridTransform.getMyy();

		var offset = 0.0;
		if (rect.horizontal()) {
			final var halfSize = (double) rect.getShipLength() / 2;
			offset = (halfSize - 0.5) * tileSize;
		}

		rect.setTranslateX((targetX + offset) * gridScaleX);
		rect.setTranslateY((targetY - offset) * gridScaleY);
		return toIndex;
	}

	public double getTileSize() {
		return tileGrid.getPrefTileHeight();
	}

	public int getGridTileIndex(GridTile tile) {
		return tileGrid.getChildren().indexOf(tile);
	}

	public GridTile getGridTile(int index) {
		return (GridTile) tileGrid.getChildren().get(index);
	}

	public ShipRectangle getShipRectangle(Ship ship) {
		for (var child : shipsPane.getChildren()) {
			if (((ShipRectangle) child).getShip() == ship) {
				return (ShipRectangle) child;
			}
		}
		return null;
	}

	public ArrayList<ShipPlacement> getShipPlacements() {
		return grid.getShipPlacements();
	}

	public boolean isValidTileIndex(int index) {
		return tileGrid.getChildren().size() > index && index >= 0;

	}
}
