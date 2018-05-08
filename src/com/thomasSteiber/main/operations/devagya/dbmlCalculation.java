package com.thomasSteiber.main.operations.devagya;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class dbmlCalculation {

    Stage stage = new Stage();
    double[][] data = new double[0][2];
    String[][] curve;
    int curveIndex, dataSize;
    double startValue, stopValue, stepValue, nullValue;
    Label error = new Label("");
    HBox hb = new HBox(10);

    public void module(){
        BorderPane layout = new BorderPane(hb, lasLoadButton(), null, null, null);
        layout.widthProperty().addListener(e-> {
            hb.setPrefWidth(layout.getPrefWidth());
            hb.setMinWidth(layout.getMinWidth());
            hb.setMaxWidth(layout.getMaxWidth());
        });
        Scene scene = new Scene(layout,600,400);
        scene.getStylesheets().add(dbmlCalculation.class.getResource("../../resources/css/plot.css").toExternalForm());

        stage.setTitle("DBML Calculation");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        stage.show();
    }

    public HBox lasLoadButton(){

        Button loadLas = new Button("Load LAS");
        loadLas.setPadding(new Insets(5));

        error.setFont(new Font("Arial", 11));
        error.setStyle("-fx-text-fill: #6ab25f;");
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        loadLas.setOnAction(e->{
            FileChooser loadlasdirectory = new FileChooser();
            loadlasdirectory.getExtensionFilters().add(new FileChooser.ExtensionFilter("LAS Files", "*.las"));
            loadlasdirectory.setTitle("Load LAS file for Vshale Calculation");
            File selectedlas =  loadlasdirectory.showOpenDialog(stage);

            if(selectedlas != null){
                readFile(selectedlas);
                if(data[0][1]!=-999999){
                    error.setStyle("-fx-text-fill: green;");
                    error.setText(selectedlas.getName()+" loaded successfully.");
                }
                else{
                    error.setStyle("-fx-text-fill: red;");
                    error.setText("Error reading las file");
                    sleeper.setOnSucceeded(event-> error.setText(""));
                    new Thread(sleeper).start();
                }
            }
        });

        HBox hb = new HBox(10, loadLas, error);
        hb.setPadding(new Insets(10));
        return hb;
    }


    public void readFile(File lasFile){
        error.setStyle("-fx-text-fill: blue;");
        error.setText("Loading "+lasFile.getName()+"......");

        BufferedReader bufferedReader;
        inner: try {

            bufferedReader = new BufferedReader(new FileReader(lasFile));
            String text;
            boolean Isversion = false, Iswell = false, Iscurve = false, Isother = false, Isdata = false;
            int textInd = 0, dataRowIndex = 0;
            curve = new String[1000][4];
            curveIndex = 0;
            String loacFlag = null;
            ScatterChart<Number, Number> scatterChart = null;
            XYChart.Series dbmlSeries = new XYChart.Series();

            getDBMLIndex dbmlObject = new getDBMLIndex();

            while ((text = bufferedReader.readLine()) != null) {
                if (text.replaceAll("\\s", "").length() == 0 || text.replaceAll("\\s", "").charAt(0) == '#')
                    continue;
                if (text.replaceAll("\\s", "").substring(0, 2).equalsIgnoreCase("~V")) {
                    Isversion = true;
                    Iswell = false;
                    Iscurve = false;
                    Isother = false;
                    Isdata = false;
                    continue;
                } else if (text.replaceAll("\\s", "").substring(0, 2).equalsIgnoreCase("~W")) {
                    Isversion = false;
                    Iswell = true;
                    Iscurve = false;
                    Isother = false;
                    Isdata = false;
                    continue;
                } else if (text.replaceAll("\\s", "").substring(0, 2).equalsIgnoreCase("~C")) {
                    Isversion = false;
                    Iswell = false;
                    Iscurve = true;
                    Isother = false;
                    Isdata = false;
                    continue;
                } else if (text.replaceAll("\\s", "").substring(0, 2).equalsIgnoreCase("~A")) {
                    Isversion = false;
                    Iswell = false;
                    Iscurve = false;
                    Isother = false;
                    Isdata = true;

                    dbmlObject.get(curve, curveIndex);
                    if (dbmlObject.output[dbmlObject.tvdIndex]==null) {
                        data[0][1] = -999999;
                        break inner;
                    }
                    loacFlag = dbmlObject.output[dbmlObject.flaglocationIndex];

                    dataSize = (int) Math.ceil((stopValue - startValue) / stepValue) + 1;
                    data = new double[dataSize][2];

                    NumberAxis xAxis = new NumberAxis();
                    NumberAxis yAxis = new NumberAxis();
                    scatterChart = new ScatterChart<>(xAxis, yAxis);
                    scatterChart.setLegendVisible(false);
                    scatterChart.setAnimated(false);
                    scatterChart.setTitle("DBML plot");
                    xAxis.setLabel(" Original depth ");
                    yAxis.setLabel("Depth below mud line");
                    scatterChart.getData().add(dbmlSeries);

                    continue;
                } else if (text.replaceAll("\\s", "").substring(0, 2).equalsIgnoreCase("~P") || text.replaceAll("\\s", "").charAt(0) == '~') {
                    Isversion = false;
                    Iswell = false;
                    Iscurve = false;
                    Isother = false;
                    Isdata = false;
                    continue;
                }

                if (Isversion || Isother) {
                } else if (Isdata) {
                    if (textInd == curveIndex) {
                        textInd = 0;
                        ++dataRowIndex;
                    }

                    text += " ";
                    text = (text.replaceAll("[ ]+", " ")).substring(1);
                    int textindex = 0;

                    while (text.indexOf(" ", textindex) > 0) {
                        int indexOf = text.indexOf(" ", textindex);
                        double value = Double.parseDouble(text.substring(textindex, indexOf));
                        if (textInd == Double.parseDouble(dbmlObject.output[dbmlObject.tvdIndex]))
                            data[dataRowIndex][0] = value;
                        textindex = indexOf + 1;
                        ++textInd;
                    }

                    double tvd = data[dataRowIndex][0];
                    double kb = Double.parseDouble(dbmlObject.output[dbmlObject.KBIndex]);
                    double wd = Double.parseDouble(dbmlObject.output[dbmlObject.WDIndex]);
                    data[dataRowIndex][1] = loacFlag.equals("1") ? tvd - kb - wd : tvd - kb;
                    dbmlSeries.getData().add(new XYChart.Data(data[dataRowIndex][0], data[dataRowIndex][1]));

                } else if (Iswell) {
                    String wellTitle = text.substring(0, text.indexOf(".")).replaceAll("\\s", "");
                    String wellValue = text.substring(text.indexOf(" ", text.indexOf(".") + 1), text.indexOf(":")).trim();
                    if (wellTitle.equalsIgnoreCase("STRT"))
                        startValue = Double.parseDouble(wellValue);
                    else if (wellTitle.equalsIgnoreCase("STOP"))
                        stopValue = Double.parseDouble(wellValue);
                    else if (wellTitle.equalsIgnoreCase("STEP"))
                        stepValue = Double.parseDouble(wellValue);
                    else if (wellTitle.equalsIgnoreCase("NULL"))
                        nullValue = Double.parseDouble(wellValue);
                } else if (Iscurve) {
                    curve[curveIndex][0] = text.substring(0, text.indexOf(".")).replaceAll("\\s", "");
                    curve[curveIndex][1] = text.substring(text.indexOf(".") + 1, text.indexOf(" ", text.indexOf(".") + 1));
                    curve[curveIndex][2] = text.substring(text.indexOf(" ", text.indexOf(".") + 1), text.indexOf(":")).trim();
                    curve[curveIndex++][3] = text.substring(text.indexOf(":") + 1).trim();
                }
            }

            stage.setMaximized(true);
            hb.getChildren().clear();
            hb.getChildren().addAll(scatterChart);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            data[0][1] = -999999;
        }
    }
}
