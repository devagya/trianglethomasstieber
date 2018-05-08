package com.thomasSteiber.main.operations.main;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class home {
    static Label error = new Label("");

    public static BorderPane input(){

        VBox parameters = new VBox(15);

        double values[] = new double[8];
        String labelTitle[] = {"Porosity (Φ_a)  ", "Porosity (Φ_b)  ", "Reuss (R_a)  ", "Reuss (R_b)  ", "Gamma y  ", "X_b  ", "Y_max  ", "Y_min  "};
        String textFieldValue[] = {"0.33","0.15","20","100","0.85",null,"1","0"};
        String promptText[] = {"Enter Φ_a value b/w 0-1", "Enter Φ_b value  b/w 0-1", "Enter R_a value in API", "Enter R_b value  in API", "Enter y value", "Enter X_b value (Optional). Default is null", "Enter Y_max value ", "Enter Y_min value" };

        for (int i=0;i<labelTitle.length;++i){

            if(textFieldValue[i]==null)
                values[i]=-999999;
            else
                values[i]=Double.parseDouble(textFieldValue[i]);

            Label label = new Label(labelTitle[i]);
            label.setFont(Font.font("Open Sans", FontWeight.BOLD, 18));
            label.setPrefWidth(180);
            label.setTextFill(Color.web("#e5e5e5"));

            TextField value = new TextField(textFieldValue[i]);
            value.setPromptText(promptText[i]);
            value.setPrefColumnCount(20);
            value.setPrefHeight(30);
            int finalI = i;
            value.textProperty().addListener((observable, oldValue, newValue) -> {
                double temp;
                try{
                    newValue = newValue.replaceAll("\\s+",""); //this removes all white spaces from input
                    temp = Double.parseDouble(newValue);
                    values[finalI] = temp;
                }
                catch (Exception e){
                    value.setText(oldValue);
                    errorPopup("Invalid "+labelTitle[finalI]+"entry");
                }
            });

            HBox row = new HBox(10,label,value);
            parameters.getChildren().add(row);
        }

        Button submit = new Button("View Plot >>>");
        submit.setFont(Font.font("Open Sans", FontWeight.SEMI_BOLD, 20));
        submit.setAlignment(Pos.CENTER);
        submit.setTextFill(Color.web("e5e5e5"));
        submit.setStyle("-fx-background-color : #004c00");
        submit.setPadding(new Insets(10));
        submit.setCursor(Cursor.HAND);
        submit.setOnAction(e-> {
            boolean check = true;
            for (int i=0;i<5;++i){
                if (values[i]==-999){
                    errorPopup("Numeric Values for "+ labelTitle[i]+"required");
                    check = false;
                    break;
                }
            }
            if (check){
                graphUI ob = new graphUI();
                ob.plot(values);
            }
        });

        error.setPadding(new Insets(5,15,5,15));
        error.setFont(Font.font("Open Sans", FontWeight.SEMI_BOLD, 15));
        error.setTextFill(Color.web("#330000"));
        error.setStyle("-fx-background-color: transparent");

        VBox bottom = new VBox(15,error, submit);
        bottom.setPadding(new Insets(20,0,0,0));
        bottom.setAlignment(Pos.CENTER);

        BorderPane home = new BorderPane(parameters,null,null,bottom,null);
        home.setPadding(new Insets(20));
        return home;

    }

    public static void errorPopup(String errorText){
        error.setStyle("-fx-background-color: #ff7f7f");
        error.setText(errorText);
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(ee-> {
            error.setStyle("-fx-background-color: transparent");
            error.setText("");
        });
        new Thread(sleeper).start();
    }

}
