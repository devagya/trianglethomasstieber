package com.thomasSteiber.main.operations.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class main extends Application {

    public static void main(String args[])
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane layout = home.input();

        Scene scene = new Scene(layout,470,500);
        scene.getStylesheets().add(main.class.getResource("../../resources/css/main.css").toExternalForm());

        primaryStage.setTitle("Sandy Shale Lamination using Thomas Steiber Method");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        primaryStage.show();
        primaryStage.setResizable(false);

    }
}