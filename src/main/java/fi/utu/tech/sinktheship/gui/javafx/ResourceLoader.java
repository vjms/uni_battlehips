package fi.utu.tech.sinktheship.gui.javafx;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ResourceLoader {
	//
	public static <P extends Parent, C> ParentControllerPair<P, C> FXML(String fileName) throws IOException {
		FXMLLoader loader = new FXMLLoader(ResourceLoader.class.getResource(fileName));
		return new ParentControllerPair<P, C>(loader.load(), loader.getController());
	}

	// finds images both outside and inside jars
	public static String image(String fileName) {
		return ResourceLoader.class.getResource(fileName).toExternalForm();
	}

	// finds stylesheets both outside and inside jars
	public static String stylesheet(String fileName) {
		return ResourceLoader.class.getResource(fileName).toExternalForm();
	}

	public static String audioClip(String fileName) {
		return ResourceLoader.class.getResource(fileName).toExternalForm();
	}
}
