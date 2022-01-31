package fi.utu.tech.sinktheship.gui.javafx.components;

import fi.utu.tech.sinktheship.gui.javafx.ResourceLoader;
import fi.utu.tech.sinktheship.ships.Ship;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Ship visual representation component.
 */
public class ShipRectangle extends Rectangle {
	private Ship ship;
	private int index = -1;

	private Image image;

	public ShipRectangle(Ship newShip, double tileSize) {
		ship = newShip;
		ship.rotatedEventDispatcher.addListener(event -> updateRotation());
		setNewTileSize(tileSize);
		// setStyle("-fx-fill: #ffffff50; -fx-stroke: black; -fx-stroke-width: 1;
		// -fx-stroke-radius: 4");
		String imgLoc = null;
		switch (newShip.getType()) {
			case BATTLESHIP:
				imgLoc = ResourceLoader.image("images/ph_2t.png");
				break;
			case CARRIER:
				imgLoc = ResourceLoader.image("images/ph_2t.png");
				break;
			case CRUISER:
				imgLoc = ResourceLoader.image("images/ph_2t.png");
				break;
			case DESTROYER:
				imgLoc = ResourceLoader.image("images/ph_2t.png");
				break;
			case SUBMARINE:
				imgLoc = ResourceLoader.image("images/ph_2t.png");
				break;
		}
		image = new Image(imgLoc);
		setNormalImage();
	}

	/**
	 * Creates and sets a modified image from the ship image for representing a
	 * faulty placement. (the resulting image could be cached, but performance is
	 * non-issue)
	 */
	private void setOverlayImage() {
		if (image == null) {
			return;
		}
		WritableImage wimage = new WritableImage(image.getPixelReader(), (int) image.getWidth(),
				(int) image.getHeight());
		var writer = wimage.getPixelWriter();
		var reader = wimage.getPixelReader();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				var color = reader.getColor(x, y);
				if (color.getOpacity() > 0.1) {
					writer.setColor(x, y, color.interpolate(Color.RED, 0.35));
				}
			}
		}
		setFill(new ImagePattern(wimage));
	}

	/**
	 * sets the basic image that was created on construction
	 */
	private void setNormalImage() {
		if (image == null) {
			return;
		}
		setFill(new ImagePattern(image));

	}

	/**
	 * Sets the ship index.
	 * 
	 * @param index new index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Gets the ship index.
	 * 
	 * @return ship index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Adjust the size of the ship
	 * 
	 * @param tileSize one dimentional tilesize
	 */
	public void setNewTileSize(double tileSize) {
		setHeight(ship.getLength() * tileSize);
		setWidth(tileSize);
	}

	/**
	 * returns the underlying ship data
	 * 
	 * @return Ship
	 */
	public Ship getShip() {
		return ship;
	}

	/**
	 * easy access method for ship length
	 * 
	 * @return ship length
	 */
	public int getShipLength() {
		return ship.getLength();
	}

	/**
	 * is the ship horizontally placed?
	 * 
	 * @return is horizontally placed
	 */
	public boolean horizontal() {
		return ship.isHorizontal();
	}

	/**
	 * is the ship vertically placed?
	 * 
	 * @return is vertically placed
	 */
	public boolean vertical() {
		return !horizontal();
	}

	/**
	 * rotate the ship, vertical -> horizontal, vice versa
	 */
	public void rotate() {
		ship.rotate();
		updateRotation();
	}

	/**
	 * gets and the rotation from the ship data.
	 */
	public void updateRotation() {
		setRotate(ship.isHorizontal() ? 90 : 0);
	}

	/**
	 * sets a modified image to indicate overlapping
	 * 
	 * @param newValue is overlapping?
	 */
	public void setIsOverlapping(boolean newValue) {
		if (newValue) {
			setOverlayImage();
		} else {
			setNormalImage();
		}
	}
}
