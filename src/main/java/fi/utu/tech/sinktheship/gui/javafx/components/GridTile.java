package fi.utu.tech.sinktheship.gui.javafx.components;

import fi.utu.tech.sinktheship.GameInstance;
import fi.utu.tech.sinktheship.gui.javafx.ResourceLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Visual representation of the tiles on the grid.
 */
public class GridTile extends StackPane {
	static public final String styleClass = "tile";
	static public final String styleClassInteractive = "tile-interactive";

	public GridTile() {
		getStyleClass().add(styleClass);
	}

	/**
	 * Set interactive style. Mouse hovering effects etc.
	 * 
	 * @param newValue style is interactive.
	 */
	public void setInteractive(boolean newValue) {
		if (newValue) {
			getStyleClass().add(styleClassInteractive);
		} else {
			getStyleClass().remove(styleClassInteractive);
		}
	}

	/**
	 * Add an image to the tile to represent whether a ship was destroyed, and play
	 * a sound. (playsound should be moved elsewhere)
	 * 
	 * @param hasShip pick the picture based on this
	 */
	public void destroy(boolean hasShip) {
		addImage(hasShip ? "images/explosion.png" : "images/water.png");
		GameInstance.getGui().playAudioClip(hasShip ? "audio/pam.wav" : "audio/plumps.wav");
	}

	/**
	 * Helper method to add an image to the tile.
	 * 
	 * @param source where the image is located
	 */
	public void addImage(String source) {
		var img = new Image(ResourceLoader.image(source));
		var rect = new Rectangle();
		final var scale = 0.75;
		rect.setHeight(getHeight() * scale);
		rect.setWidth(getWidth() * scale);
		System.out.println(rect.getHeight() + " " + rect.getWidth());
		rect.setFill(new ImagePattern(img));
		getChildren().add(rect);
	}

}
