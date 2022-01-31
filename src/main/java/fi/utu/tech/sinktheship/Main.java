package fi.utu.tech.sinktheship;

import fi.utu.tech.sinktheship.gui.javafx.GUI;

public class Main {
    /**
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            GUI.launch(GUI.class, args);
            GameInstance.quit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
