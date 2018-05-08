package com.thomasSteiber.main.operations.devagya;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class saturationCalculation {

    public void module(){

        BorderPane layout = new BorderPane();
        Scene scene = new Scene(layout);

        Stage stage = new Stage();
        stage.setTitle("Saturation Calculation");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        stage.show();

    }

}
