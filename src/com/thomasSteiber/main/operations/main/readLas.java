package com.thomasSteiber.main.operations.main;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Objects;

import static com.thomasSteiber.main.operations.main.indexes.*;

public class readLas {

    double[][] data;
    String[][] header;
    String[][] curve;
    int wellIndex, curveIndex;
    double startValue, stopValue, stepValue, nullValue;
    NumberAxis yAxis = new NumberAxis();
    HBox curves;
    double[] slidersPosition;
    XYChart.Series[] areaSeries ;
    int depthDiv;
    boolean isNphiPresent;

    public double[][] readFile(File lasFile){
        BufferedReader bufferedReader;
        inner: try {

            bufferedReader = new BufferedReader(new FileReader(lasFile));
            String text;
            boolean Isversion = false, Iswell = false, Iscurve = false, Isother = false, Isdata = false;
            int textInd = 0, dataRowIndex = 0;
            int indexArray[] = {};
            int[] grMaxMinIndex = new int[2];
            data = new double[1][1];
            header = new String[500][4];
            curve = new String[1000][4];
            wellIndex = 0;
            curveIndex = 0;
            slidersPosition = new double[100];
            curves = new HBox();
            areaSeries = new XYChart.Series[2];
            depthDiv = 10;
            isNphiPresent = true;
            double avgShaleDensity = 0.0;

            LineChartWithMarkers<Number, Number> lineChartGr = null;
            XYChart.Series grSeries = new XYChart.Series();

            LineChart<Number, Number> lineChartNphi = null;
            XYChart.Series NPhiSeries = new XYChart.Series();

            LineChart<Number, Number> lineChartRhob = null;
            XYChart.Series RhobSeries = new XYChart.Series();

            LineChart<Number, Number> lineChartRes = null;
            XYChart.Series ResSeries = new XYChart.Series();

            getIndex ob = new getIndex();

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
                    indexArray = ob.get(curve, curveIndex);
                    if (indexArray[0] == -1) {
                        data[0][0] = -999999;
                        break inner;
                    }
                    if (indexArray[ob.getnPhiIndex()] == -1)
                        isNphiPresent = false;

                    avgShaleDensity = 0.5*(ob.values[ob.getShaleDensityLowerIndex()]+ob.values[ob.getShaleDensityUpperIndex()]);
                    data = new double[(int) Math.ceil((stopValue - startValue) / stepValue) + 1][totalIndexes];
                    yAxis = new NumberAxis(stopValue, startValue, -100 * stepValue);

                    HBox depthVb = new HBox(0);
                    GridPane depthGrid = new GridPane();
                    depthGrid.setVgap(0);
                    for (int i = 0; i <= depthDiv; ++i) {
                        Label depthLabel = new Label((startValue + (i / depthDiv) * (stopValue - startValue)) + "");
                        depthGrid.add(depthLabel, 0, i);
                    }
                    depthGrid.setPrefHeight(depthVb.getHeight() - 60);

                    Rectangle rect = new Rectangle(2, depthVb.getHeight() - 60);
                    rect.setFill(Color.BLACK);
                    rect.setStrokeWidth(0);
                    depthVb.heightProperty().addListener(e -> {
                        rect.setHeight(depthVb.getHeight());
                        depthGrid.setPrefHeight(depthVb.getHeight() - 60);
                    });

                    depthVb.getChildren().addAll(depthGrid, rect);
                    depthVb.setPadding(new Insets(30, 0, 30, 5));
                    curves.getChildren().add(depthVb);

                    lineChartGr = new LineChartWithMarkers<>(new NumberAxis(), yAxis);
                    lineChartGr.setCreateSymbols(false);
                    lineChartGr.setLegendVisible(false);
                    lineChartGr.setAnimated(false);
                    lineChartGr.setTitle("GR");
                    lineChartGr.getYAxis().setTickLabelsVisible(false);
                    lineChartGr.getYAxis().setOpacity(0);
                    lineChartGr.setPadding(new Insets(0));
                    lineChartGr.getData().add(grSeries);

                    HBox sliders = new HBox(0);

                    final int[] sliderIndex = {-1};
                    MenuItem addSlider = new MenuItem("Add Slider");
                    LineChartWithMarkers<Number, Number> finalLineChartGr = lineChartGr;
                    addSlider.setOnAction(e -> {
                        final int currentSlider = ++sliderIndex[0];
                        XYChart.Data<Number, Number> horizontalMarker = new XYChart.Data<>(0, 0.5 * (stopValue - startValue));
                        finalLineChartGr.addHorizontalValueMarker(horizontalMarker);
                        Slider horizontalMarkerSlider = new Slider(startValue, stopValue, 0);
                        horizontalMarkerSlider.setOrientation(Orientation.VERTICAL);
                        horizontalMarkerSlider.setShowTickLabels(false);
                        horizontalMarkerSlider.setShowTickMarks(false);
                        horizontalMarkerSlider.setCursor(Cursor.HAND);
                        horizontalMarkerSlider.setTooltip(new Tooltip("Mark boundary of regions"));
                        horizontalMarkerSlider.setRotate(180);
                        horizontalMarkerSlider.valueProperty().bindBidirectional(horizontalMarker.YValueProperty());
                        horizontalMarkerSlider.setPadding(new Insets(30, 0, 30, 5));
                        sliders.getChildren().add(horizontalMarkerSlider);
                        slidersPosition[currentSlider] = (double) horizontalMarker.getYValue();

                        MenuItem removeSlider = new MenuItem("Remove");
                        removeSlider.setOnAction(ee -> {
                            sliders.getChildren().remove(horizontalMarkerSlider);
                            finalLineChartGr.removeHorizontalValueMarker(horizontalMarker);
                            slidersPosition[currentSlider] = nullValue;
                        });
                        ContextMenu sliderMenus = new ContextMenu();
                        sliderMenus.getItems().addAll(removeSlider);
                        horizontalMarkerSlider.setOnContextMenuRequested(ee -> sliderMenus.show(horizontalMarkerSlider, ee.getScreenX(), ee.getScreenY()));
                    });

                    MenuItem updateVshale = new MenuItem("Update Vshale");
                    updateVshale.setOnAction(e -> {
                        System.out.println("Wait");
                    });

                    ContextMenu grMenus = new ContextMenu();
                    grMenus.getItems().addAll(addSlider, updateVshale);
                    lineChartGr.setOnContextMenuRequested(e -> grMenus.show(finalLineChartGr, e.getScreenX(), e.getScreenY()));

                    BorderPane borderPane = new BorderPane(lineChartGr, null, null, null, sliders);
                    curves.getChildren().add(borderPane);

                    NumberAxis xVshaleAxis = new NumberAxis(0, 1, 0.1);
                    for (int i = 0; i < areaSeries.length; ++i)
                        areaSeries[i] = new XYChart.Series();
                    modifiedAreaPlot<Number, Number> areaChartVshale = new modifiedAreaPlot<>(xVshaleAxis, yAxis, areaSeries);
                    areaChartVshale.getYAxis().setOpacity(0);
                    areaChartVshale.getYAxis().setTickLabelsVisible(false);
                    areaChartVshale.setTitle("Vshale");
                    areaChartVshale.setCreateSymbols(false);
                    areaChartVshale.setLegendVisible(false);
                    curves.getChildren().add(areaChartVshale);
                    for (int i = 0; i < areaSeries.length; ++i)
                        areaChartVshale.getData().add(areaSeries[i]);

                    lineChartNphi = linecharts("Nphi");
                    curves.getChildren().add(lineChartNphi);
                    lineChartNphi.getData().add(NPhiSeries);

                    lineChartRhob = linecharts("Rhob");
                    curves.getChildren().add(lineChartRhob);
                    lineChartRhob.getData().add(RhobSeries);

                    lineChartRes = linecharts("Resistivity");
                    curves.getChildren().add(lineChartRes);
                    lineChartRes.getData().add(ResSeries);

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
                        if (textInd == indexArray[ob.getDepthIndex()])
                            data[dataRowIndex][depthIndex] = value;
                        else if (textInd == indexArray[ob.getGrIndex()]) {
                            data[dataRowIndex][grIndex] = value;
                            if (value != nullValue) {
                                grSeries.getData().add(new XYChart.Data(value, data[dataRowIndex][depthIndex]));
                                if (grMaxMinIndex[0] == (int) nullValue) {
                                    grMaxMinIndex[0] = dataRowIndex;
                                    grMaxMinIndex[1] = dataRowIndex;
                                } else {
                                    grMaxMinIndex[0] = data[grMaxMinIndex[0]][grIndex] > value ? dataRowIndex : grMaxMinIndex[0];
                                    grMaxMinIndex[1] = data[grMaxMinIndex[1]][grIndex] < value ? dataRowIndex : grMaxMinIndex[1];
                                }
                            } else
                                data[dataRowIndex][vshaleIndex] = nullValue;
                        } else if (textInd == indexArray[ob.getnPhiIndex()]) {
                            data[dataRowIndex][nPhiIndex] = value;
                            if (value != nullValue)
                                NPhiSeries.getData().add(new XYChart.Data(value, data[dataRowIndex][depthIndex]));
                        } else if (textInd == indexArray[ob.getRhobIndex()]) {
                            data[dataRowIndex][rhobIndex] = value;
                            if (value != nullValue) {
                                RhobSeries.getData().add(new XYChart.Data(value, data[dataRowIndex][depthIndex]));
                                if (avgShaleDensity < value)
                                    avgShaleDensity = value + 0.005;
                            }
                        } else if (textInd == indexArray[ob.getResIndex()]) {
                            data[dataRowIndex][resIndex] = value;
                            if (value != nullValue)
                                ResSeries.getData().add(new XYChart.Data(value, data[dataRowIndex][depthIndex]));
                        }
                        textindex = indexOf + 1;
                        ++textInd;
                    }
                } else if (Iswell) {
                    header[wellIndex][0] = text.substring(0, text.indexOf(".")).replaceAll("\\s", "");
                    header[wellIndex][1] = text.substring(text.indexOf(".") + 1, text.indexOf(" ", text.indexOf(".") + 1));
                    header[wellIndex][2] = text.substring(text.indexOf(" ", text.indexOf(".") + 1), text.indexOf(":")).trim();
                    if (header[wellIndex][0].equalsIgnoreCase("STRT"))
                        startValue = Double.parseDouble(header[wellIndex][2]);
                    else if (header[wellIndex][0].equalsIgnoreCase("STOP"))
                        stopValue = Double.parseDouble(header[wellIndex][2]);
                    else if (header[wellIndex][0].equalsIgnoreCase("STEP"))
                        stepValue = Double.parseDouble(header[wellIndex][2]);
                    else if (header[wellIndex][0].equalsIgnoreCase("NULL")) {
                        nullValue = Double.parseDouble(header[wellIndex][2]);
                        grMaxMinIndex[0] = (int) nullValue;
                        grMaxMinIndex[1] = (int) nullValue;
                    }
                    header[wellIndex++][3] = text.substring(text.indexOf(":") + 1).trim();
                } else if (Iscurve) {
                    curve[curveIndex][0] = text.substring(0, text.indexOf(".")).replaceAll("\\s", "");
                    curve[curveIndex][1] = text.substring(text.indexOf(".") + 1, text.indexOf(" ", text.indexOf(".") + 1));
                    curve[curveIndex][2] = text.substring(text.indexOf(" ", text.indexOf(".") + 1), text.indexOf(":")).trim();
                    curve[curveIndex++][3] = text.substring(text.indexOf(":") + 1).trim();
                }
            }

            lineChartGr.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            grSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: red;");
            lineChartNphi.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            NPhiSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: red;");
            lineChartRhob.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            RhobSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: red;");
            lineChartRes.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            ResSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: red;");

            plotVshale(startValue, stopValue, data[grMaxMinIndex[0]][grIndex], data[grMaxMinIndex[1]][grIndex]);

            LineChart<Number, Number> lineChartphiO = new LineChart<>(new NumberAxis(), new NumberAxis(4200,3500,-1));
            lineChartphiO.setCreateSymbols(false);
            lineChartphiO.setLegendVisible(false);
            lineChartphiO.setAnimated(false);
            lineChartphiO.setTitle("PHI-O");
            lineChartphiO.setPadding(new Insets(0));
            XYChart.Series phiOSeries = new XYChart.Series();
            lineChartphiO.getData().add(phiOSeries);
            lineChartphiO.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            phiOSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: red;");
            Stage tempStage = new Stage();
            Scene scene = new Scene(new BorderPane(lineChartphiO));
            tempStage.setScene(scene);
            tempStage.show();

            int matrixDensityIndex = 0;
            int phiDIndex = 1;
            int phiDXIndex = 2;
            int phiDCIndex = 3;
            int phiNCIndex = 4;
            int phiOIndex = 5;
            double[][] calculatedValues = new double[data.length][6];
            for (int i = 0; i < data.length; ++i) {
                double currentShaleDensity = avgShaleDensity;
                innerWhile: while (true){
                    calculatedValues[i][matrixDensityIndex] = nullValue;
                    calculatedValues[i][phiDIndex] = nullValue;
                    calculatedValues[i][phiDXIndex] = nullValue;
                    calculatedValues[i][phiDCIndex] = nullValue;
                    calculatedValues[i][phiNCIndex] = nullValue;
                    if (data[i][vshaleIndex] != nullValue) {
                        calculatedValues[i][matrixDensityIndex] = data[i][vshaleIndex] * currentShaleDensity +
                                (1 - data[i][vshaleIndex]) * ob.values[ob.getSandDensityIndex()];
                        if (isNphiPresent && data[i][nPhiIndex] != nullValue)
                            calculatedValues[i][phiNCIndex] = data[i][nPhiIndex] - data[i][vshaleIndex] * data[grMaxMinIndex[1]][nPhiIndex];
                        if (data[i][rhobIndex] != nullValue) {
                            calculatedValues[i][matrixDensityIndex] = calculatedValues[i][matrixDensityIndex] < data[i][rhobIndex]
                                    ? calculatedValues[i][matrixDensityIndex] + 0.005 : calculatedValues[i][matrixDensityIndex];
                            calculatedValues[i][phiDIndex] = (calculatedValues[i][matrixDensityIndex] - data[i][rhobIndex]) /
                                    (calculatedValues[i][matrixDensityIndex] - ob.values[ob.getFluidDensityIndex()]);
                            calculatedValues[i][phiDXIndex] = (calculatedValues[i][matrixDensityIndex] - data[i][rhobIndex]) /
                                    (calculatedValues[i][matrixDensityIndex] - ob.values[ob.getMudFiltrateDensityIndex()]);
                            calculatedValues[i][phiDCIndex] = calculatedValues[i][phiDXIndex] - data[i][vshaleIndex] *
                                    ((currentShaleDensity - data[i][rhobIndex]) / (currentShaleDensity - 1));
                        }
                    }
                    if (!isNphiPresent || data[i][nPhiIndex] == nullValue || (calculatedValues[i][phiNCIndex] != nullValue && calculatedValues[i][phiNCIndex] > 0.4))
                        calculatedValues[i][phiOIndex] = calculatedValues[i][phiDXIndex];
                    if (isNphiPresent && data[i][nPhiIndex] < 0.4) {
                        if (calculatedValues[i][phiDCIndex] != nullValue && calculatedValues[i][phiNCIndex] == nullValue)
                            calculatedValues[i][phiOIndex] = calculatedValues[i][phiDCIndex];
                        else if (calculatedValues[i][phiDCIndex] == nullValue || calculatedValues[i][phiDCIndex] > 0.4)
                            calculatedValues[i][phiOIndex] = calculatedValues[i][phiNCIndex];
                        else if (calculatedValues[i][phiDCIndex] != nullValue && calculatedValues[i][phiNCIndex] != nullValue)
                            calculatedValues[i][phiOIndex] = Math.sqrt((Math.pow(calculatedValues[i][phiDCIndex], 2) + Math.pow(calculatedValues[i][phiNCIndex], 2)) / 2);
                        else
                            calculatedValues[i][phiOIndex] = nullValue;
                    }
                    if (calculatedValues[i][phiOIndex] != nullValue) {
                        calculatedValues[i][phiOIndex] = calculatedValues[i][phiOIndex] <= 0 ? 0.001 : calculatedValues[i][phiOIndex];
                        if (calculatedValues[i][phiOIndex] <= 0.47) {
                            System.out.println("**depth: " + data[i][depthIndex] + " grMin: " + data[grMaxMinIndex[0]][grIndex] + " grMax: " + data[grMaxMinIndex[1]][grIndex] + " Vsh: " + data[i][vshaleIndex] + " matrixDenity: " + calculatedValues[i][matrixDensityIndex] + " phiDX: " + calculatedValues[i][phiDXIndex] + " phiDC: " + calculatedValues[i][phiDCIndex] + " phiNC: " + calculatedValues[i][phiNCIndex] + " ShaleDensity: " + currentShaleDensity + " phio: " + calculatedValues[i][phiOIndex]);
                            phiOSeries.getData().add(new XYChart.Data(calculatedValues[i][phiOIndex], data[i][depthIndex]));
                            break innerWhile;
                        }
                        if (calculatedValues[i][phiOIndex] > 0.47)
                            currentShaleDensity = currentShaleDensity - 0.05;
                        if (currentShaleDensity < ob.values[ob.getShaleDensityLowerIndex()]) {
                            System.out.println("depth: " + data[i][depthIndex] + " grMin: " + data[grMaxMinIndex[0]][grIndex] + " grMax: " + data[grMaxMinIndex[1]][grIndex] + " Vsh: " + data[i][vshaleIndex] + " matrixDenity: " + calculatedValues[i][matrixDensityIndex] + " phiDX: " + calculatedValues[i][phiDXIndex] + " phiDC: " + calculatedValues[i][phiDCIndex] + " phiNC: " + calculatedValues[i][phiNCIndex] + " ShaleDensity: " + currentShaleDensity + " phio: " + calculatedValues[i][phiOIndex]);
                            calculatedValues[i][matrixDensityIndex] = nullValue;
                            calculatedValues[i][phiDIndex] = nullValue;
                            calculatedValues[i][phiDXIndex] = nullValue;
                            calculatedValues[i][phiDCIndex] = nullValue;
                            calculatedValues[i][phiNCIndex] = nullValue;
                            calculatedValues[i][phiOIndex] = nullValue;
                            break innerWhile;
                        }
                    }
                    else
                        break innerWhile;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            data[0][0] = -999999;
        }
        return data;
    }

    public void plot(){
        Stage plotStage = new Stage();
        Scene scene = new Scene(curves);
        plotStage.setScene(scene);
        plotStage.show();
        plotStage.setMaximized(true);
    }

    public HBox losLoad(Stage stage) {

        HBox lasHb = new HBox(10);
        lasHb.setPadding(new Insets(10));

        Label error = new Label("");
        error.setFont(new Font("Arial", 11));
        error.setStyle("-fx-text-fill: red;");
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

        Button loadFile = new Button("Load las");
        loadFile.setPadding(new Insets(10));

        loadFile.setOnAction(e->{
            FileChooser loadlasdirectory = new FileChooser();
            loadlasdirectory.getExtensionFilters().add(new FileChooser.ExtensionFilter("LAS Files", "*.las"));
            loadlasdirectory.setTitle("Load LAS file for thomas steiber");
            File selectedlas =  loadlasdirectory.showOpenDialog(stage);

            if(selectedlas != null){
                double data[][] = readFile(selectedlas);
                if(data[0][0]!=-999999){
                    error.setStyle("-fx-text-fill: green;");
                    error.setText(selectedlas.getName()+" loaded successfully.");
                    sleeper.setOnSucceeded(event-> error.setText(""));
                    new Thread(sleeper).start();
                    plot();
                }
                else{
                    error.setStyle("-fx-text-fill: red;");
                    error.setText("Error reading las file");
                    sleeper.setOnSucceeded(event-> error.setText(""));
                    new Thread(sleeper).start();
                }
            }
        });

        lasHb.getChildren().addAll(loadFile, error);
        return lasHb;
    }

    public LineChart<Number, Number> linecharts(String plotName){
        NumberAxis xAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setTitle(plotName);
        lineChart.getYAxis().setTickLabelsVisible(false);
        lineChart.getYAxis().setOpacity(0);
        lineChart.setPadding(new Insets(0));
        return lineChart;
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

    void plotVshale(double startingDepth,double endingDepth, double grMin, double grMax){
        int len = data.length;
        inner: for (int i=0;i<len;++i){
            double depthValue = data[i][depthIndex];
            if (depthValue < startingDepth)
                continue;
            if (depthValue > endingDepth)
                break inner;
            if (depthValue == startingDepth)
                areaSeries[1].getData().add(new XYChart.Data(1, startingDepth));
            if (depthValue == endingDepth)
                areaSeries[1].getData().add(new XYChart.Data(1, endingDepth));
            double grValue = data[i][grIndex];
            if (grValue!=nullValue){
                double Igr = (grValue - grMin)/(grMax - grMin);
                Igr = Igr<=0 ? 0.020 : Igr;
                Igr = Igr>=1 ? 0.999 : Igr;
                double vshale = 1.7 - Math.sqrt(3.38 - Math.pow((Igr + 0.7),2));
                data[i][vshaleIndex] = vshale;
                areaSeries[0].getData().add(new XYChart.Data(vshale, depthValue));
            }
        }
    }
}
