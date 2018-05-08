package com.thomasSteiber.main.operations.devagya;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class newHome extends Application {

    public static void main(String args[])
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Button dbmlModule = new Button("DBML Calculation");
        dbmlModule.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        dbmlModule.setOnAction(e-> new dbmlCalculation().module());

        Button VshModule = new Button("Vshale Calculation");
        VshModule.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        VshModule.setOnAction(e-> new vshaleCalculation().module());

        Button porosityModule = new Button("Porosity Calculation");
        porosityModule.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        porosityModule.setOnAction(e-> new porosityCalculation().module("Clavier")); // {"Linear", "Larionov Tertiary Rocks", "Steiber", "Clavier","Larionov Older Rocks"};

        Button saturationModule = new Button("Saturation Calculation");
        saturationModule.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        saturationModule.setOnAction(e-> new saturationCalculation().module());

        HBox buttons = new HBox(5, dbmlModule, VshModule, porosityModule, saturationModule);
        buttons.setAlignment(Pos.CENTER);

        BorderPane layout = new BorderPane(buttons);
        layout.setMinHeight(200);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(newHome.class.getResource("../../resources/css/main.css").toExternalForm());

        primaryStage.setTitle("Sandy Shale Lamination using Thomas Steiber Method");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        primaryStage.show();

    }

}
