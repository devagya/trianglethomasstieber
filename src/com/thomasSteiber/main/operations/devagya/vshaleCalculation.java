package com.thomasSteiber.main.operations.devagya;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

public class vshaleCalculation {

    Stage stage = new Stage();
    double[][] data;
    String[][] curve;
    int curveIndex, grIndex, dataSize;
    double grMin, grMax, startValue, stopValue, stepValue, nullValue;
    Label error = new Label("");
    HBox hb = new HBox(10);
    XYChart.Series[] areaSeries;
    CheckBox linear = new CheckBox("Linear");
    CheckBox larionovTer = new CheckBox("Larionov Tertiary Rocks");
    CheckBox Steiber = new CheckBox("Steiber");
    CheckBox clavier = new CheckBox("Clavier");
    CheckBox larionovOld = new CheckBox("Larionov Older Rocks");
    int depthDiv = 10;

    public void module(){

        linear.setSelected(true);
        larionovTer.setSelected(true);
        Steiber.setSelected(true);
        clavier.setSelected(true);
        larionovOld.setSelected(true);

        HBox checkboxes = new HBox(5, linear, clavier, larionovTer, Steiber, larionovOld);
        checkboxes.setPadding(new Insets(5));
        BorderPane header = new BorderPane(null, null,checkboxes, null,lasLoadButton());

        BorderPane layout = new BorderPane(hb, header, null, null, null);
        Scene scene = new Scene(layout);
        scene.getStylesheets().add(vshaleCalculation.class.getResource("../../resources/css/plot.css").toExternalForm());

        stage.setTitle("Vshale Calculation");
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
                if(data[0][0]!=-999999){
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
            data = new double[1][1];
            curve = new String[1000][4];
            curveIndex = 0;

            VBox[] depthVB = new VBox[1];
            Rectangle[] rect = new Rectangle[1];
            HBox depthVb =  new HBox(0);
            XYChart.Series grSeries = new XYChart.Series();
            LineChartWithMarkers<Number, Number>[] lineChartGr = new LineChartWithMarkers[1];

            String[] titles = {"Linear", "Clavier", "Larionov Tertiary Rocks", "Steiber", "Larionov Older Rocks"};
            areaSeries = new XYChart.Series[titles.length];
            modifiedAreaPlot<Number, Number>[] vshale = new modifiedAreaPlot[titles.length];

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

                    grIndex = getGRIndex(curve);
                    if (grIndex == -1) {
                        data[0][0] = -999999;
                        break inner;
                    }
                    dataSize = (int) Math.ceil((stopValue - startValue) / stepValue) + 1;
                    data = new double[dataSize][2];

                    depthVB[0] = new VBox();
                    Label[] depthLabel = new Label[depthDiv+1];
                    double interval = (stopValue - startValue)/depthDiv;
                    for (int i = 0; i <= depthDiv; ++i) {
                        double value = startValue + i*interval;
                        depthLabel[i] = new Label(((int)value) + "");
                        depthVB[0].getChildren().add(depthLabel[i]);
                    }
                    rect[0] = new Rectangle(2, 100);
                    rect[0].setFill(Color.BLACK);
                    rect[0].setStrokeWidth(0);
                    depthVb.getChildren().clear();
                    depthVb.getChildren().addAll(depthVB[0], rect[0]);
                    depthVb.setPadding(new Insets(30, 0, 30, 5));
                    depthVb.setMinWidth(50);
                    depthVb.setMaxWidth(50);
                    depthVb.setPrefWidth(50);

                    lineChartGr[0] = new LineChartWithMarkers<>(new NumberAxis(), new NumberAxis(stopValue, startValue, -stepValue));
                    lineChartGr[0].setCreateSymbols(false);
                    lineChartGr[0].setLegendVisible(false);
                    lineChartGr[0].setAnimated(false);
                    lineChartGr[0].setTitle("GR plot");
                    lineChartGr[0].getYAxis().setTickLabelsVisible(false);
                    lineChartGr[0].getYAxis().setOpacity(0);
                    lineChartGr[0].getData().add(grSeries);
                    lineChartGr[0].heightProperty().addListener(e -> {
                        final double height = lineChartGr[0].getHeight()-60;
                        rect[0].setHeight(height);
                        depthVB[0].setSpacing((height-(depthDiv+1)*16.6)/depthDiv);
                    });

                    final double[] yValue = {0.0, 0.0};
                    MenuItem startDepth = new MenuItem("Add start of range");
                    MenuItem endDepth = new MenuItem("Add end of range");
                    endDepth.setDisable(true);
                    LineChartWithMarkers<Number, Number> finalLineChartGr = lineChartGr[0];

                    startDepth.setOnAction(e->{
                        startDepth.setDisable(true);
                        endDepth.setDisable(false);
                        XYChart.Data<Number, Number> horizontalMarker = new XYChart.Data<>(0, yValue[0]);
                        finalLineChartGr.addHorizontalValueMarker(horizontalMarker);
                    });

                    endDepth.setOnAction(e-> {
                        startDepth.setDisable(false);
                        endDepth.setDisable(true);
                        XYChart.Data<Number, Number> horizontalMarker = new XYChart.Data<>(0, yValue[1]);
                        finalLineChartGr.addHorizontalValueMarker(horizontalMarker);
                        double results[] = getGRRange(yValue);
                        int startIndex = Integer.parseInt(((int)results[0])+""), endIndex = Integer.parseInt(((int)results[1])+"");
                        double grMin = results[2], grMax = results[3];
                        if (startIndex!=-1) {
                            error.setStyle("-fx-text-fill: blue;");
                            error.setText("Updating Vshale from "+results[0]+" to "+results[1]);
                            updateVshale(startIndex, endIndex, grMin, grMax);
                        }
                        else {
                            error.setStyle("-fx-text-fill: red;");
                            error.setText("Error in updating Vshale");
                        }
                    });

                    ContextMenu grMenus = new ContextMenu();
                    grMenus.getItems().addAll(startDepth, endDepth);
                    lineChartGr[0].setOnContextMenuRequested(e -> {
                        grMenus.show(finalLineChartGr, e.getScreenX(), e.getScreenY());
                        if (startDepth.isDisable())
                            yValue[1] = (double) finalLineChartGr.getYAxis().getValueForDisplay(e.getY()-38);
                        else
                            yValue[0] = (double) finalLineChartGr.getYAxis().getValueForDisplay(e.getY()-38);
                    });

                    for (int i = 0; i < titles.length; ++i) {
                        areaSeries[i] = new XYChart.Series();
                        vshale[i] =  new modifiedAreaPlot<>(new NumberAxis(), new NumberAxis(stopValue, startValue, -stepValue), areaSeries[i]);
                        vshale[i].getYAxis().setOpacity(0);
                        vshale[i].getYAxis().setTickLabelsVisible(false);
                        vshale[i].setTitle(titles[i]);
                        vshale[i].setCreateSymbols(false);
                        vshale[i].setLegendVisible(false);
                        vshale[i].getData().add(areaSeries[i]);
                    }

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
                        if (textInd == 0)
                            data[dataRowIndex][0] = value;
                        else if (textInd == grIndex) {
                            data[dataRowIndex][1] = value;
                            if (value!=nullValue) {
                                grSeries.getData().add(new XYChart.Data(value, data[dataRowIndex][0]));
                                if (grMin==nullValue){
                                    grMin = value;
                                    grMax = value;
                                }
                                else {
                                    grMin = Math.min(grMin, value);
                                    grMax = Math.max(grMax, value);
                                }
                            }
                        }
                        textindex = indexOf + 1;
                        ++textInd;
                    }
                } else if (Iswell) {
                    String wellTitle = text.substring(0, text.indexOf(".")).replaceAll("\\s", "");
                    String wellValue = text.substring(text.indexOf(" ", text.indexOf(".") + 1), text.indexOf(":")).trim();
                    if (wellTitle.equalsIgnoreCase("STRT"))
                        startValue = Double.parseDouble(wellValue);
                    else if (wellTitle.equalsIgnoreCase("STOP"))
                        stopValue = Double.parseDouble(wellValue);
                    else if (wellTitle.equalsIgnoreCase("STEP"))
                        stepValue = Double.parseDouble(wellValue);
                    else if (wellTitle.equalsIgnoreCase("NULL")) {
                        nullValue = Double.parseDouble(wellValue);
                        grMin = nullValue;
                        grMax = nullValue;
                    }
                } else if (Iscurve) {
                    curve[curveIndex][0] = text.substring(0, text.indexOf(".")).replaceAll("\\s", "");
                    curve[curveIndex][1] = text.substring(text.indexOf(".") + 1, text.indexOf(" ", text.indexOf(".") + 1));
                    curve[curveIndex][2] = text.substring(text.indexOf(" ", text.indexOf(".") + 1), text.indexOf(":")).trim();
                    curve[curveIndex++][3] = text.substring(text.indexOf(":") + 1).trim();
                }
            }

            lineChartGr[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            grSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #6ab25f;");

            HBox hbb = new HBox(0, depthVb, lineChartGr[0]);
            hbb.setPadding(new Insets(0));

            hb.getChildren().clear();
            hb.getChildren().addAll(hbb);
            stage.setMaximized(true);
            Platform.runLater(() ->{
                updateVshale(0, dataSize, grMin, grMax);
                hb.getChildren().addAll(vshale);
                linear.selectedProperty().addListener((ov, old_val, new_val) -> hb.getChildren().get(1).setVisible(new_val));
                larionovTer.selectedProperty().addListener((ov, old_val, new_val) -> hb.getChildren().get(2).setVisible(new_val));
                Steiber.selectedProperty().addListener((ov, old_val, new_val) -> hb.getChildren().get(3).setVisible(new_val));
                clavier.selectedProperty().addListener((ov, old_val, new_val) -> hb.getChildren().get(4).setVisible(new_val));
                larionovOld.selectedProperty().addListener((ov, old_val, new_val) -> hb.getChildren().get(5).setVisible(new_val));
            });

        }
        catch (Exception ex) {
            ex.printStackTrace();
            data[0][0] = -999999;
        }
    }

    public int getGRIndex(String curve[][]){
        Stage stage = new Stage();
        stage.setHeight(150);
        stage.setResizable(false);

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(15));

        final int[] grIndex = {0};
        String[] index = new String[curveIndex];
        String[] names = new String[curveIndex];
        for(int i=0;i<curveIndex;++i){
            index[i] = i+"";
            names[i] = curve[i][0]+" ("+curve[i][3]+")";
        }

        Label grLabel = new Label("GR Curve :");

        ChoiceBox<String> choice = new ChoiceBox<>(FXCollections.observableArrayList(names));
        choice.setValue(names[0]);
        choice.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> {
            grIndex[0] = Integer.parseInt(index[new_value.intValue()]);
        });

        Button ok = new Button("Ok");
        ok.setOnAction(e-> stage.close());
        Button cancel = new Button("Cancel");

        cancel.setOnAction(e-> {
            grIndex[0] = -1;
            stage.close();
        });

        layout.setTop(new HBox(10, grLabel, choice));
        layout.setBottom(new HBox(10, ok, cancel));

        Scene scene = new Scene(layout);
        stage.setTitle("Choose GR value");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        return grIndex[0];
    }

    public double[] getGRRange(double depth[]){
        double[] results = new double[4];

        Stage vrStage = new Stage();

        int numberOfGroups = 100;
        int GRUpperLimit = 400;
        int group[] = new int[numberOfGroups];
        inner: for(int i=0; i<dataSize; i++){
            if(data[i][0]<depth[0])
                results[0] = i;
            else if (data[i][0]<depth[1])
                results[1] = i;
            if(data[i][0]>=depth[0] && data[i][0]<=depth[1])
                group[(int)(data[i][1]/(GRUpperLimit/numberOfGroups))-1]++;
            else if(data[i][0]>depth[1])
                break inner;
        }

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> barChart = new BarChart<>(xAxis,yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        xAxis.setLabel(curve[grIndex][3]+" ("+curve[grIndex][1]+")");
        yAxis.setLabel("frequency");
        barChart.setCategoryGap(0);
        barChart.setBarGap(0);

        XYChart.Series series1 = new XYChart.Series();
        for (int i=0;i<numberOfGroups;++i)
            series1.getData().add(new XYChart.Data(i*GRUpperLimit/numberOfGroups+"-"+(i+1)*GRUpperLimit/numberOfGroups, group[i]));

        barChart.getData().addAll(series1);

        Label grMinLabel = new Label("Gr Min: ");
        TextField grMinValue = new TextField();

        Label grMaxLabel = new Label("Gr Max: ");
        TextField grMaxValue = new TextField();

        Button ok = new Button("Ok");
        ok.setOnAction(e-> {
            try{
                results[2] = Double.parseDouble(grMinValue.getText());
                results[3] = Double.parseDouble(grMaxValue.getText());
            }
            catch (Exception exp){
                results[0] = -1;
            }
            vrStage.close();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e-> {
            results[0] = -1;
            vrStage.close();
        });

        BorderPane layout = new BorderPane(barChart);
        layout.setPadding(new Insets(10));
        HBox hbb = new HBox(20,
                        new HBox(10,grMinLabel, grMinValue),
                        new HBox(10,grMaxLabel, grMaxValue),
                        new HBox(10,ok, cancel));
        hbb.setAlignment(Pos.CENTER);
        layout.setBottom(hbb);
        Scene scene = new Scene(layout, 800, 450);

        vrStage.setTitle("GR Max-Min picking");
        vrStage.setScene(scene);
        vrStage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        vrStage.initModality(Modality.APPLICATION_MODAL);
        vrStage.showAndWait();

        return results;
    }

    public void updateVshale(int startIndex, int endIndex, double grMin, double grMax){
        for (int i=startIndex; i<endIndex; ++i){
            double Igr = (data[i][1] - grMin)/(grMax - grMin);
            Igr = Igr<=0 ? 0.020 : Igr;
            Igr = Igr>=1 ? 0.999 : Igr;
            double linearVshale = Igr;
            areaSeries[0].getData().add(new XYChart.Data(linearVshale, data[i][0]));
            double larionovTerVshale = 0.083*(Math.pow(2,3.7*Igr) -1);
            areaSeries[1].getData().add(new XYChart.Data(larionovTerVshale, data[i][0]));
            double steiberVshale = Igr/(3-2*Igr);
            areaSeries[2].getData().add(new XYChart.Data(steiberVshale, data[i][0]));
            double clavierVshale = 1.7 - Math.sqrt(3.38 - Math.pow((Igr + 0.7),2));
            areaSeries[3].getData().add(new XYChart.Data(clavierVshale, data[i][0]));
            double larionovOldVshale = 0.33*(Math.pow(2,2*Igr) -1);
            areaSeries[4].getData().add(new XYChart.Data(larionovOldVshale, data[i][0]));
        }
    }

    private class LineChartWithMarkers<X,Y> extends LineChart {

        private ObservableList<Data<X, Y>> horizontalMarkers;

        public LineChartWithMarkers(NumberAxis xAxis, NumberAxis yAxis) {
            super(xAxis, yAxis);
            horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
        }

        public void addHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            horizontalMarkers.add(marker);
        }

        public void removeHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (marker.getNode() != null) {
                getPlotChildren().remove(marker.getNode());
                marker.setNode(null);
            }
            horizontalMarkers.remove(marker);
        }

        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            for (Data<X, Y> horizontalMarker : horizontalMarkers) {
                Line line = (Line) horizontalMarker.getNode();
                line.setStartX(0);
                line.setEndX(getBoundsInLocal().getWidth());
                line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
                line.setEndY(line.getStartY());
                line.toFront();
            }
        }
    }
}