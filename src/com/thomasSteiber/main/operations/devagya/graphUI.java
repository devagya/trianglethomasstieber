package com.thomasSteiber.main.operations.devagya;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class graphUI {

    double coordinates[][] = new double[4][2];
    LineChart<Number,Number> lineChart = null;
    Stage window;

    public void plot(double newCord[][], int startIndex,int endIndex, double intervalVal[][], int phiTindex, int vShaleIndex, double nullValue){
        window = new Stage();

        coordinates = newCord;

        NumberAxis xAxis = new NumberAxis(-0.1,1.1,0.1);
        NumberAxis yAxis = new NumberAxis(-0.1,1.1,0.1);
        xAxis.setLabel("Vshale ");
        yAxis.setLabel("Porosity Φ");
        lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setTitle("Plot of Vshale Vs Porosity");
        lineChart.setAnimated(false);

        displayValues(startIndex, endIndex, intervalVal, phiTindex, vShaleIndex, nullValue);
        Scene scene = new Scene(lineGraph(),500,500);
        scene.getStylesheets().add(graphUI.class.getResource("../../resources/css/graphUI.css").toExternalForm());

        window.setTitle("Graphical representation");
        window.setScene(scene);
        window.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        window.setMinWidth(400);
        window.setMinHeight(400);
        window.showAndWait();
    }

    public BorderPane lineGraph(){

        XYChart.Series<Number, Number> series0 = new XYChart.Series();
        series0.setName("Laminated");
        XYChart.Data l11 = new XYChart.Data<>(coordinates[0][0], coordinates[0][1]);
        series0.getData().add(l11);
        XYChart.Data l12 = new XYChart.Data<>(coordinates[2][0], coordinates[2][1]);
        series0.getData().add(l12);

        XYChart.Series<Number, Number> series1 = new XYChart.Series();
        series1.setName("Dispersed");
        XYChart.Data l21 = new XYChart.Data<>(coordinates[2][0], coordinates[2][1]);
        series1.getData().add(l21);
        XYChart.Data l22 = new XYChart.Data<>(coordinates[3][0], coordinates[3][1]);
        series1.getData().add(l22);

        XYChart.Series<Number, Number> series2 = new XYChart.Series();
        XYChart.Data l31 = new XYChart.Data<>(coordinates[3][0], coordinates[3][1]);
        series2.getData().add(l31);
        XYChart.Data l32 = new XYChart.Data<>(coordinates[0][0], coordinates[0][1]);
        series2.getData().add(l32);

        XYChart.Series<Number, Number> series3 = new XYChart.Series();
        series3.setName("Structural");
        XYChart.Data l41 = new XYChart.Data<>(coordinates[1][0], coordinates[1][1]);
        series3.getData().add(l41);
        XYChart.Data l42 = new XYChart.Data<>(coordinates[2][0], coordinates[2][1]);
        series3.getData().add(l42);

        lineChart.getData().addAll(series0, series1, series3, series2);

        Node cleanShale = l32.getNode();
        cleanShale.setCursor(Cursor.HAND);
        cleanShale.setOnMouseDragged(e -> {
            Point2D pointInScene = new Point2D(e.getSceneX(), e.getSceneY());
            double xAxisLoc = lineChart.getXAxis().sceneToLocal(pointInScene).getX();
            double yAxisLoc = lineChart.getYAxis().sceneToLocal(pointInScene).getY();
            Number x = lineChart.getXAxis().getValueForDisplay(xAxisLoc);
            Number y = lineChart.getYAxis().getValueForDisplay(yAxisLoc);
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
            double xAxisLoc = lineChart.getXAxis().sceneToLocal(pointInScene).getX();
            double yAxisLoc = lineChart.getYAxis().sceneToLocal(pointInScene).getY();
            Number x = lineChart.getXAxis().getValueForDisplay(xAxisLoc);
            Number y = lineChart.getYAxis().getValueForDisplay(yAxisLoc);
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
            double xAxisLoc = lineChart.getXAxis().sceneToLocal(pointInScene).getX();
            double yAxisLoc = lineChart.getYAxis().sceneToLocal(pointInScene).getY();
            Number x = lineChart.getXAxis().getValueForDisplay(xAxisLoc);
            Number y = lineChart.getYAxis().getValueForDisplay(yAxisLoc);
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
            double xAxisLoc = lineChart.getXAxis().sceneToLocal(pointInScene).getX();
            double yAxisLoc = lineChart.getYAxis().sceneToLocal(pointInScene).getY();
            Number x = lineChart.getXAxis().getValueForDisplay(xAxisLoc);
            Number y = lineChart.getYAxis().getValueForDisplay(yAxisLoc);
            if(x.doubleValue()>=0 && x.doubleValue()<=1 && y.doubleValue()>=0 && y.doubleValue()<=1) {
                l41.setXValue(x);
                l41.setYValue(y);
            }
        });

        BorderPane layout = new BorderPane(lineChart);
        window.widthProperty().addListener(e-> layout.setPrefWidth(window.getWidth()*0.6));
        return layout;
    }

    public void displayValues(int startIndex,int endIndex, double intervalVal[][], int phiTindex, int vShaleIndex, double nullValue){
        XYChart.Series<Number, Number> series4 = new XYChart.Series();
        series4.setName("1-0.75");
        XYChart.Series<Number, Number> series5 = new XYChart.Series();
        series5.setName("0.75-0.5");
        XYChart.Series<Number, Number> series6 = new XYChart.Series();
        series6.setName("0.5-0.25");
        XYChart.Series<Number, Number> series7 = new XYChart.Series();
        series7.setName("0.25-0");
        for (int i=startIndex;i<endIndex;++i){
            double vshale = intervalVal[i][vShaleIndex];
            if (intervalVal[i][phiTindex]!=nullValue && vshale!=nullValue && intervalVal[i][phiTindex]<=1){
                if (vshale<=1 && vshale>=0.75)
                    series4.getData().add(new XYChart.Data(intervalVal[i][vShaleIndex], intervalVal[i][phiTindex]));
                else if (vshale<=0.75 && vshale>=0.5)
                    series5.getData().add(new XYChart.Data(intervalVal[i][vShaleIndex], intervalVal[i][phiTindex]));
                else if (vshale<=0.5 && vshale>=0.25)
                    series6.getData().add(new XYChart.Data(intervalVal[i][vShaleIndex], intervalVal[i][phiTindex]));
                else if (vshale<=0.25 && vshale>=0)
                    series7.getData().add(new XYChart.Data(intervalVal[i][vShaleIndex], intervalVal[i][phiTindex]));
            }
        }
        lineChart.getData().addAll(series4, series5,series6, series7);
    }
}