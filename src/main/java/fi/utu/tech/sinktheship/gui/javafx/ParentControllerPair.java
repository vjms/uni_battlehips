package fi.utu.tech.sinktheship.gui.javafx;

import javafx.scene.Parent;

public class ParentControllerPair<P extends Parent, C> {
	public final P parent;
	public final C controller;

	ParentControllerPair(P parent, C controller) {
		this.parent = parent;
		this.controller = controller;

	}

}
