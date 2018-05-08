package com.thomasSteiber.main.operations.main;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class graphUI {

    double coordinates[][] = new double[4][2];
    Stage window;
    static Label error = new Label("");

    public void plot(double values[]){
        window = new Stage();

        getCoordinates(values);

        for (int i=0;i<4;++i)
            System.out.println(coordinates[i][0]+", "+coordinates[i][1]);

        readLas ob = new readLas();
        BorderPane layout = new BorderPane(null,ob.losLoad(window),parameter(),null,lineGraph());

        Scene scene = new Scene(layout,500,500);

        window.setTitle("Graphical representation");
        window.setScene(scene);
        window.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        window.setMinWidth(400);
        window.setMinHeight(400);
        window.initModality(Modality.APPLICATION_MODAL);
        window.showAndWait();
    }

    public void getCoordinates(double values[]){

        // Y_min
        coordinates[0][0] = values[7];
        // Y_max
        coordinates[2][0] = values[6];

        //line A both Porosity index   Φ_lam = Y*Φ_a + (1-Y)*Φ_b
        coordinates[0][1] = values[7]*values[0] + (1-values[7])*values[1];  // Φ_min at Y_min
        coordinates[2][1] = values[6]*values[0] + (1-values[6])*values[1];  // Φ_max at Y_max

        //line B for lower index
        coordinates[3][1] = values[0]*values[1];  // Φ_min = Φ_a * Φ_b
        if (values[5]==-999999 || values[5] < values[0])  // X_a == null || X_a < Φ_a
            coordinates[3][0] = 1 - (values[0]-values[0]*values[1])/((1-values[1])*(1- values[2]/values[3]));  // Y_min
        else
            coordinates[3][0] = 1- values[0];

        //line D for upper index
        coordinates[1][0] = 1- values[0];  // Y_min
        coordinates[1][1] = values[0] + (1-values[7])*values[1];  // Φ_min = Φ_a + (1-Y)*Φ_b

    }

    public BorderPane lineGraph(){

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Gamma y");
        yAxis.setLabel("Porosity Φ");
        final LineChart<Number,Number> lineChart = new LineChart<>(xAxis,yAxis);

        lineChart.setTitle("Plot of Gamma Vs Porosity");
        lineChart.setAnimated(false);

        XYChart.Series<Number, Number> series1 = new XYChart.Series();
        series1.setName("Line A");
        XYChart.Data l11 = new XYChart.Data<>(coordinates[0][0], coordinates[0][1]);
        series1.getData().add(l11);
        XYChart.Data l12 = new XYChart.Data<>(coordinates[2][0], coordinates[2][1]);
        series1.getData().add(l12);

        XYChart.Series<Number, Number> series2 = new XYChart.Series();
        series2.setName("Line B");
        XYChart.Data l21 = new XYChart.Data<>(coordinates[2][0], coordinates[2][1]);
        series2.getData().add(l21);
        XYChart.Data l22 = new XYChart.Data<>(coordinates[3][0], coordinates[3][1]);
        series2.getData().add(l22);

        XYChart.Series<Number, Number> series3 = new XYChart.Series();
        series3.setName("Line C");
        XYChart.Data l31 = new XYChart.Data<>(coordinates[3][0], coordinates[3][1]);
        series3.getData().add(l31);
        XYChart.Data l32 = new XYChart.Data<>(coordinates[0][0], coordinates[0][1]);
        series3.getData().add(l32);

        XYChart.Series<Number, Number> series4 = new XYChart.Series();
        series4.setName("Line D");
        XYChart.Data l41 = new XYChart.Data<>(coordinates[1][0], coordinates[1][1]);
        series4.getData().add(l41);
        XYChart.Data l42 = new XYChart.Data<>(coordinates[2][0], coordinates[2][1]);
        series4.getData().add(l42);

        lineChart.getData().addAll(series1, series2, series3, series4);

        Node cleanShale = l32.getNode();
        cleanShale.setCursor(Cursor.HAND);
        cleanShale.setOnMouseDragged(e -> {
            Point2D pointInScene = new Point2D(e.getSceneX(), e.getSceneY());
            double xAxisLoc = xAxis.sceneToLocal(pointInScene).getX();
            double yAxisLoc = yAxis.sceneToLocal(pointInScene).getY();
            Number x = xAxis.getValueForDisplay(xAxisLoc);
            Number y = yAxis.getValueForDisplay(yAxisLoc);
//            System.out.println("---->  "+x+"  "+x.intValue()+"   "+x.doubleValue()+);
            if(x.doubleValue()>=0 && x.doubleValue()<=1 && y.doubleValue()>=0 && y.doubleValue()<=1) {
                l32.setXValue(x);
                l11.setXValue(x);
                l32.setYValue(y);
                l11.setYValue(y);
            }
        });

        Node phiMin = l31.getNode();
        phiMin.setCursor(Cursor.HAND);
        phiMin.setOnMouseDragged(e -> {
            Point2D pointInScene = new Point2D(e.getSceneX(), e.getSceneY());
            double xAxisLoc = xAxis.sceneToLocal(pointInScene).getX();
            double yAxisLoc = yAxis.sceneToLocal(pointInScene).getY();
            Number x = xAxis.getValueForDisplay(xAxisLoc);
            Number y = yAxis.getValueForDisplay(yAxisLoc);
            if(x.doubleValue()>=0 && x.doubleValue()<=1 && y.doubleValue()>=0 && y.doubleValue()<=1) {
                l31.setXValue(x);
                l22.setXValue(x);
                l31.setYValue(y);
                l22.setYValue(y);
            }
        });

        Node cleanSand = l42.getNode();
        cleanSand.setCursor(Cursor.HAND);
        cleanSand.setOnMouseDragged(e -> {
            Point2D pointInScene = new Point2D(e.getSceneX(), e.getSceneY());
            double xAxisLoc = xAxis.sceneToLocal(pointInScene).getX();
            double yAxisLoc = yAxis.sceneToLocal(pointInScene).getY();
            Number x = xAxis.getValueForDisplay(xAxisLoc);
            Number y = yAxis.getValueForDisplay(yAxisLoc);
            if(x.doubleValue()>=0 && x.doubleValue()<=1 && y.doubleValue()>=0 && y.doubleValue()<=1) {
                l42.setXValue(x);
                l12.setXValue(x);
                l21.setXValue(x);
                l42.setYValue(y);
                l12.setYValue(y);
                l21.setYValue(y);
            }
        });

        Node satPorosity = l41.getNode();
        satPorosity.setCursor(Cursor.HAND);
        satPorosity.setOnMouseDragged(e -> {
            Point2D pointInScene = new Point2D(e.getSceneX(), e.getSceneY());
            double xAxisLoc = xAxis.sceneToLocal(pointInScene).getX();
            double yAxisLoc = yAxis.sceneToLocal(pointInScene).getY();
            Number x = xAxis.getValueForDisplay(xAxisLoc);
            Number y = yAxis.getValueForDisplay(yAxisLoc);
            if(x.doubleValue()>=0 && x.doubleValue()<=1 && y.doubleValue()>=0 && y.doubleValue()<=1) {
                l41.setXValue(x);
                l41.setYValue(y);
            }
        });

        BorderPane layout = new BorderPane(lineChart);
        window.widthProperty().addListener(e-> layout.setPrefWidth(window.getWidth()*0.6));
        return layout;
    }

    public BorderPane parameter(){

        VBox parameters = new VBox(20);

        String labelTitle[] = {"Line A Division", "Line B Division"};
        for (int i=0;i<2;++i){
            Label label = new Label(labelTitle[i]);
            label.setFont(Font.font("Open Sans", FontWeight.NORMAL, 15));
            label.setPrefWidth(100);
            label.setWrapText(true);

            TextField value = new TextField();
            value.setPrefColumnCount(15);
            value.setPrefHeight(30);
            int finalI = i;
            value.textProperty().addListener((observable, oldValue, newValue) -> {
                double temp;
                try{
                    newValue = newValue.replaceAll("\\s+",""); //this removes all white spaces from input
                    temp = Double.parseDouble(newValue);
                    value.setText(temp+"");
                }
                catch (Exception e){
                    value.setText(oldValue);
                    errorPopup("Invalid "+labelTitle[finalI]+" entry");
                }
            });

            Button submit = new Button("Divide");
            submit.setFont(Font.font("Open Sans", FontWeight.SEMI_BOLD, 15));
            submit.setAlignment(Pos.CENTER);
            submit.setTextFill(Color.web("e5e5e5"));
            submit.setStyle("-fx-background-color : #004c00");
            submit.setPadding(new Insets(5));
            submit.setCursor(Cursor.HAND);
            submit.setOnAction(e-> {
                if (Integer.parseInt(value.getText())<=0)
                    errorPopup("Positive Numeric Values required");

            });

            HBox divideRow = new HBox(10, label, value, submit);
            parameters.getChildren().add(divideRow);
        }



        error.setPadding(new Insets(5,15,5,15));
        error.setFont(Font.font("Open Sans", FontWeight.SEMI_BOLD, 15));
        error.setTextFill(Color.web("#330000"));
        error.setStyle("-fx-background-color: transparent");

        parameters.getChildren().add(error);

        BorderPane layout = new BorderPane(parameters);
        window.widthProperty().addListener(e-> layout.setPrefWidth(window.getWidth()*0.4));
        return layout;

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
