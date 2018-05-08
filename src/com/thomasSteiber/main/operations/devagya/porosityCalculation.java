package com.thomasSteiber.main.operations.devagya;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
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

import static com.thomasSteiber.main.operations.devagya.indexes.*;

public class porosityCalculation {

    Stage stage = new Stage();
    double[][] data;
    String[][] curve;
    int curveIndex, dataSize;
    double grMin, grMax, startValue, stopValue, stepValue, nullValue;
    Label error = new Label("");
    SplitPane curves = new SplitPane();
    boolean isNphiPresent, isRxoPresent, isDRESHpresent, isMRESHpresent, isSRESHpresent;
    XYChart.Series areaSeries ;
    NumberAxis yAxis = new NumberAxis();
    String title = "";
    int depthDiv = 10;

    public void module(String vshale){
        title = vshale;
        curves.setDividerPositions(1/12);
        BorderPane layout = new BorderPane(new ScrollPane(curves), lasLoadButton(), null, null, null);
        stage.heightProperty().addListener(e->{
            curves.setMinHeight(stage.getHeight()-100);
            curves.setMaxHeight(stage.getHeight()-100);
            curves.setPrefHeight(stage.getHeight()-100);
        });
        stage.widthProperty().addListener(e-> {
            curves.setMinWidth(1.5*stage.getWidth());
            curves.setMaxWidth(1.5*stage.getWidth());
            curves.setPrefWidth(1.5*stage.getWidth());
        });
        Scene scene = new Scene(layout,600,400);
        scene.getStylesheets().add(porosityCalculation.class.getResource("../../resources/css/plot.css").toExternalForm());

        stage.setTitle("Porosity Calculation");
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
            double[] dbml = new double[0], fTemp = new double[0], Rwfor = new double[0], Rwxo = new double[0];
            curveIndex = 0;
            isNphiPresent = true; isRxoPresent= true; isDRESHpresent = true; isMRESHpresent = true; isSRESHpresent = true;
            final double[] avgShaleDensity = {0.0};

            VBox[] depthVB = new VBox[1];
            Rectangle[] rect = new Rectangle[1];
            HBox depthVb =  new HBox(0);
            XYChart.Series grSeries = new XYChart.Series();
            LineChartWithMarkers<Number, Number> lineChartGr = null;

            areaSeries = new XYChart.Series();
            modifiedAreaPlot<Number, Number> areaChartVshale = null;

            LineChart<Number, Number> lineChartRhob = null;
            XYChart.Series RhobSeries = new XYChart.Series();

            LineChart<Number, Number>[] lineChartphi = new LineChart[1];
            XYChart.Series phiESeries = new XYChart.Series();
            XYChart.Series phiTSeries = new XYChart.Series();

            LineChart<Number, Number>[] lineChartPhiNCNphi = new LineChart[1];
            XYChart.Series phiNCSeries = new XYChart.Series();
            XYChart.Series NphiSeries = new XYChart.Series();

            LineChart<Number, Number>[] lineChartPhiDCDX = new LineChart[1];
            XYChart.Series phiDCSeries = new XYChart.Series();
            XYChart.Series phiDxSeries = new XYChart.Series();

            LineChart<Number, Number>[] lineChartThomasSteiber = new LineChart[1];
            XYChart.Series vLamSeries = new XYChart.Series();
            XYChart.Series vDispSeries = new XYChart.Series();
            XYChart.Series phiTSDSeries = new XYChart.Series();

            LineChart<Number, Number>[] lineChartSf = new LineChart[1];
            XYChart.Series sFSeries = new XYChart.Series();

            LineChart<Number, Number>[] lineChartSw = new LineChart[1];
            XYChart.Series sWSeries = new XYChart.Series();

            getPorWindow2Index porObject = new getPorWindow2Index();
            getPorWindow1Index dbmlObject = new getPorWindow1Index();

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
                        data[0][0] = -999999;
                        break inner;
                    }
                    
                    if (dbmlObject.output[dbmlObject.nPhiIndex] == null)
                        isNphiPresent = false;
                    if (dbmlObject.output[dbmlObject.SRESHIndex] == null)
                        isSRESHpresent = false;
                    if (dbmlObject.output[dbmlObject.MRESHIndex] == null)
                        isMRESHpresent = false;
                    if (dbmlObject.output[dbmlObject.DRESHIndex] == null)
                        isDRESHpresent = false;
                    if (dbmlObject.output[dbmlObject.rxoIndex] == null)
                        isRxoPresent = false;

                    dataSize = (int) Math.ceil((stopValue - startValue) / stepValue) + 1;
                    data = new double[dataSize][totalIndexes];
                    dbml = new double[dataSize];
                    fTemp = new double[dataSize];
                    Rwfor = new double[dataSize];
                    Rwxo = new double[dataSize];
                    yAxis = new NumberAxis(stopValue, startValue, -100 * stepValue);

                    depthVB[0] = new VBox();
                    Label[] depthLabel = new Label[depthDiv+1];
                    double interval = (stopValue - startValue)/depthDiv;
                    for (int i = 0; i <= depthDiv; ++i) {
                        double value = startValue + i*interval;
                        depthLabel[i] = new Label(((int)value) + "");
                        depthVB[0].getChildren().add(depthLabel[i]);
                    }
                    rect[0] = new Rectangle(2, curves.getHeight() - 60);
                    rect[0].setFill(Color.BLACK);
                    rect[0].setStrokeWidth(0);
                    curves.heightProperty().addListener(e -> {
                        final double height = curves.getHeight()-60;
                        rect[0].setHeight(height);
                        depthVB[0].setSpacing((height-(depthDiv+1)*16.4)/depthDiv);
                    });
                    depthVb.getChildren().clear();
                    depthVb.getChildren().addAll(depthVB[0], rect[0]);
                    depthVb.setPadding(new Insets(30, 0, 30, 5));
                    depthVb.setMinWidth(50);
                    depthVb.setMaxWidth(50);
                    depthVb.setPrefWidth(50);

                    lineChartGr = new LineChartWithMarkers<>(new NumberAxis(), yAxis);
                    lineChartGr.setCreateSymbols(false);
                    lineChartGr.setLegendVisible(false);
                    lineChartGr.setAnimated(false);
                    lineChartGr.setTitle("GR plot");
                    lineChartGr.getYAxis().setTickLabelsVisible(false);
                    lineChartGr.getYAxis().setOpacity(0);
                    lineChartGr.getData().add(grSeries);

                    final double[] yValue = {0.0, 0.0};
                    MenuItem startDepth = new MenuItem("Add start of range");
                    MenuItem endDepth = new MenuItem("Add end of range");
                    endDepth.setDisable(true);
                    LineChartWithMarkers<Number, Number> finalLineChartGr = lineChartGr;

                    startDepth.setOnAction(e->{
                        startDepth.setDisable(true);
                        endDepth.setDisable(false);
                        Data<Number, Number> horizontalMarker = new Data<>(0, yValue[0]);
                        finalLineChartGr.addHorizontalValueMarker(horizontalMarker);
                    });

                    lineChartphi[0] = linecharts(new NumberAxis(0,1,0.1),"Phi_E - Phi_T");
                    lineChartphi[0].getData().addAll(phiESeries, phiTSeries);

                    lineChartPhiNCNphi[0] = linecharts(new NumberAxis(0,1,0.1),"Phi_NC - Nphi");
                    lineChartPhiNCNphi[0].getData().addAll(phiNCSeries, NphiSeries);

                    lineChartPhiDCDX[0] = linecharts(new NumberAxis(0,1,0.1),"Phi_DC - Phi_DX");
                    lineChartPhiDCDX[0].getData().addAll(phiDCSeries, phiDxSeries);

                    lineChartThomasSteiber[0] = linecharts(new NumberAxis(0,1,0.1),"V(Lam-Disp)-Phi(TSD)");
                    lineChartThomasSteiber[0].getData().addAll(vLamSeries, vDispSeries, phiTSDSeries);

                    lineChartSf[0] = linecharts(new NumberAxis(0,1,0.1),"Sand Facture");
                    lineChartSf[0].getData().add(sFSeries);

                    lineChartSw[0] = linecharts(new NumberAxis(0,1,0.1),"Saturation");
                    lineChartSw[0].getData().add(sWSeries);

                    double[] finalRwxo = Rwxo;
                    double[] finalRwfor = Rwfor;
                    endDepth.setOnAction(e-> {
                        startDepth.setDisable(false);
                        endDepth.setDisable(true);
                        Data<Number, Number> horizontalMarker = new Data<>(0, yValue[1]);
                        finalLineChartGr.addHorizontalValueMarker(horizontalMarker);
                        double results[] = getGRRange(yValue);
                        int startIndex = Integer.parseInt(((int)results[0])+""),
                                endIndex = Integer.parseInt(((int)results[1])+""),
                                grMinIndex = Integer.parseInt(((int)results[2])+""),
                                grMaxIndex = Integer.parseInt(((int)results[3])+"");
                        if (startIndex!=-1) {
                            error.setStyle("-fx-text-fill: blue;");
                            error.setText("Updated Vshale from "+results[0]+" to "+results[1]);

                            grMin = data[grMinIndex][grIndex];
                            grMax = data[grMaxIndex][grIndex];

                            Data<Number, Number> grMinMarker = new Data<>(grMin,0);
                            finalLineChartGr.addVerticalValueMarker(grMinMarker);
                            Data<Number, Number> grMaxMarker = new Data<>(grMax, 0);
                            finalLineChartGr.addVerticalValueMarker(grMaxMarker);

                            porObject.get(curve, curveIndex);

                            avgShaleDensity[0] = 0.5*(Double.parseDouble(porObject.output[porObject.shaleDensityLowerIndex])+Double.parseDouble(porObject.output[porObject.shaleDensityUpperIndex]));

                            int intervalDepthIndex = 0;
                            int vShaleIndex = 1;
                            int matrixDensityIndex = 2;
                            int phiDIndex = 3;
                            int phiDXIndex = 4;
                            int phiDCIndex = 5;
                            int phiNCIndex = 6;
                            int phiOIndex = 7;
                            int phiEIndex = 8;
                            int phiTIndex = 9;
                            int phiDispIndex = 10;
                            int phiMinIndex = 11;
                            int GRmaxdIndex = 12;
                            int phiDisSHIndex = 13;
                            int phiLamIndex = 14;
                            int phiStrIndex = 15;
                            int VLamIndex = 16;
                            int SandFractionIndex = 17;
                            int phiTSDIndex = 18;
                            int VDispIndex = 19;
                            int VDispSDIndex = 20;
                            int phieSDIndex = 21;
                            double[][] intervalValues = new double[data.length][22];

                            String fluidFlag = porObject.output[porObject.flagFluidIndex];
                            double sandDensity = Double.parseDouble(porObject.output[porObject.sandDensityIndex]);
                            double mudFlitrateDensity = Double.parseDouble(porObject.output[porObject.mudFiltrateDensityIndex]);
                            double reservorHCDensity = Double.parseDouble(porObject.output[porObject.reservoirHCDensityIndex]);
                            double fluidDensity = Double.parseDouble(porObject.output[porObject.fluidDensityIndex]);
                            double shalePorsity = Double.parseDouble(porObject.output[porObject.ShalePorosityIndex]);
                            double tortousityFactor = Double.parseDouble(porObject.output[porObject.tortuosityIndex]);
                            double cementArchie = Double.parseDouble(porObject.output[porObject.archieCementationIndex]);
                            double satArchie = Double.parseDouble(porObject.output[porObject.archieSaturationIndex]);

                            double vShaleMin = Double.MAX_VALUE;
                            double vShaleMax = Double.MIN_VALUE;
                            for (int i = startIndex; i < endIndex; ++i) {
                                intervalValues[i][intervalDepthIndex] = data[i][depthIndex];
                                if (data[i][grIndex]==nullValue)
                                    intervalValues[i][vShaleIndex] = nullValue;
                                else{
                                    double Igr = (data[i][grIndex] - grMin)/(grMax - grMin);
                                    Igr = Igr<=0 ? 0.020 : Igr;
                                    Igr = Igr>=1 ? 0.999 : Igr;
                                    intervalValues[i][vShaleIndex] = Igr;
                                    if (title.equals("Larionov Tertiary Rocks"))
                                        intervalValues[i][vShaleIndex] = 0.083*(Math.pow(2,3.7*Igr) -1);
                                    else if (title.equals("Steiber"))
                                        intervalValues[i][vShaleIndex] = Igr/(3-2*Igr);
                                    else if (title.equals("Clavier"))
                                        intervalValues[i][vShaleIndex] = 1.7 - Math.sqrt(3.38 - Math.pow((Igr + 0.7),2));
                                    else if (title.equals("Larionov Older Rocks"))
                                        intervalValues[i][vShaleIndex] = 0.33*(Math.pow(2,2*Igr) -1);
                                    vShaleMin = Math.min(vShaleMin, intervalValues[i][vShaleIndex]);
                                    vShaleMax = Math.max(vShaleMax, intervalValues[i][vShaleIndex]);
                                    areaSeries.getData().add(new Data(intervalValues[i][vShaleIndex], data[i][depthIndex]));
                                }

                                double currentShaleDensity = avgShaleDensity[0];
                                innerWhile: while (true){
                                    intervalValues[i][matrixDensityIndex] = nullValue;
                                    intervalValues[i][phiDIndex] = nullValue;
                                    intervalValues[i][phiDXIndex] = nullValue;
                                    intervalValues[i][phiDCIndex] = nullValue;
                                    intervalValues[i][phiNCIndex] = nullValue;
                                    if (intervalValues[i][vShaleIndex] != nullValue) {
                                        intervalValues[i][matrixDensityIndex] = intervalValues[i][vShaleIndex] * currentShaleDensity +
                                                (1 - intervalValues[i][vShaleIndex]) * sandDensity;
                                        if (isNphiPresent && data[i][nPhiIndex] != nullValue)
                                            intervalValues[i][phiNCIndex] = data[i][nPhiIndex] - intervalValues[i][vShaleIndex] * data[grMaxIndex][nPhiIndex];
                                        if (data[i][rhobIndex] != nullValue) {
                                            intervalValues[i][matrixDensityIndex] = intervalValues[i][matrixDensityIndex] < data[i][rhobIndex]
                                                    ? intervalValues[i][matrixDensityIndex] + 0.005 : intervalValues[i][matrixDensityIndex];
                                            intervalValues[i][phiDIndex] = (intervalValues[i][matrixDensityIndex] - data[i][rhobIndex]) /
                                                    (intervalValues[i][matrixDensityIndex] - fluidDensity);
                                            intervalValues[i][phiDXIndex] = (intervalValues[i][matrixDensityIndex] - data[i][rhobIndex]) /
                                                    (intervalValues[i][matrixDensityIndex] - mudFlitrateDensity);
                                            intervalValues[i][phiDCIndex] = intervalValues[i][phiDXIndex] - intervalValues[i][vShaleIndex] *
                                                    ((currentShaleDensity - data[i][rhobIndex]) / (currentShaleDensity - 1));
                                        }
                                    }
                                    if (!isNphiPresent || data[i][nPhiIndex] == nullValue || (intervalValues[i][phiNCIndex] != nullValue && intervalValues[i][phiNCIndex] > 0.4))
                                        intervalValues[i][phiOIndex] = intervalValues[i][phiDXIndex];
                                    if (isNphiPresent && data[i][nPhiIndex] < 0.4) {
                                        if (intervalValues[i][phiDCIndex] != nullValue && intervalValues[i][phiNCIndex] == nullValue)
                                            intervalValues[i][phiOIndex] = intervalValues[i][phiDCIndex];
                                        else if (intervalValues[i][phiDCIndex] == nullValue || intervalValues[i][phiDCIndex] > 0.4)
                                            intervalValues[i][phiOIndex] = intervalValues[i][phiNCIndex];
                                        else if (intervalValues[i][phiDCIndex] != nullValue && intervalValues[i][phiNCIndex] != nullValue) {
                                            intervalValues[i][phiOIndex] = fluidFlag.equals("Liquid") ? 0.5*(intervalValues[i][phiDCIndex]+intervalValues[i][phiNCIndex])
                                                    : Math.sqrt((Math.pow(intervalValues[i][phiDCIndex], 2) + Math.pow(intervalValues[i][phiNCIndex], 2)) / 2);

                                        }else
                                            intervalValues[i][phiOIndex] = nullValue;
                                    }
                                    if (intervalValues[i][phiOIndex] != nullValue) {
                                        intervalValues[i][phiOIndex] = intervalValues[i][phiOIndex] <= 0 ? 0.001 : intervalValues[i][phiOIndex];
                                        if (intervalValues[i][phiOIndex] <= 0.47)
                                            break innerWhile;
                                        if (intervalValues[i][phiOIndex] > 0.47)
                                            currentShaleDensity = currentShaleDensity - 0.05;
                                        if (currentShaleDensity < Double.parseDouble(porObject.output[porObject.shaleDensityLowerIndex])) {
                                            intervalValues[i][matrixDensityIndex] = nullValue;
                                            intervalValues[i][phiDIndex] = nullValue;
                                            intervalValues[i][phiDXIndex] = nullValue;
                                            intervalValues[i][phiDCIndex] = nullValue;
                                            intervalValues[i][phiNCIndex] = nullValue;
                                            intervalValues[i][phiOIndex] = nullValue;
                                            break innerWhile;
                                        }
                                    }
                                    else
                                        break innerWhile;
                                }

                                if (intervalValues[i][phiNCIndex]!=nullValue)
                                    phiNCSeries.getData().add(new Data(intervalValues[i][phiNCIndex], data[i][depthIndex]));
                                if (intervalValues[i][phiDCIndex]!=nullValue)
                                    phiDCSeries.getData().add(new Data(intervalValues[i][phiDCIndex], data[i][depthIndex]));
                                if (intervalValues[i][phiDXIndex]!=nullValue)
                                    phiDxSeries.getData().add(new Data(intervalValues[i][phiDXIndex], data[i][depthIndex]));

                                double Ia = 0.0;
                                if(!isRxoPresent){
                                    if (finalRwfor[i]!=nullValue) {
                                        if (!isDRESHpresent) {
                                            if (isSRESHpresent)
                                                Ia = data[i][sRESHIndex]!=nullValue ?
                                                        Math.pow(tortousityFactor *finalRwfor[i]/data[i][sRESHIndex],1/satArchie)
                                                        : nullValue;
                                            else
                                                Ia = data[i][mRESHIndex]!=nullValue ?
                                                        Math.pow(tortousityFactor *finalRwfor[i]/data[i][mRESHIndex],1/satArchie)
                                                        : nullValue;
                                        } else{
                                            Ia = data[i][dRESHIndex]!=nullValue ?
                                                    Math.pow(tortousityFactor *finalRwfor[i]/data[i][dRESHIndex],1/satArchie)
                                                    : nullValue;
                                        }
                                    }
                                    else
                                        Ia = nullValue;
                                }
                                else
                                    Ia = finalRwxo[i]!=nullValue && data[i][rxoIndex]!=nullValue ?
                                            Math.pow(tortousityFactor *finalRwxo[i]/data[i][rxoIndex],1/satArchie)
                                            : nullValue;

                                int errorVal = 1000, count = 0;
                                intervalValues[i][phiDXIndex] = intervalValues[i][phiOIndex];
                                double guessPorosity = 0;
                                innerWhile: while (count<=100 && errorVal>0.0001){
                                    ++count;
                                    guessPorosity = intervalValues[i][phiDXIndex];

                                    double porFunction = Ia!=nullValue && guessPorosity!=nullValue && intervalValues[i][matrixDensityIndex]!=nullValue && intervalValues[i][vShaleIndex]!=nullValue && data[i][rhobIndex]!=nullValue
                                        ? (mudFlitrateDensity-reservorHCDensity)*Ia*(Math.pow(guessPorosity,(1-cementArchie/satArchie)))
                                            + ((reservorHCDensity-intervalValues[i][matrixDensityIndex]) - (currentShaleDensity-intervalValues[i][matrixDensityIndex])*intervalValues[i][vShaleIndex])*guessPorosity
                                            + intervalValues[i][matrixDensityIndex] +(currentShaleDensity - intervalValues[i][matrixDensityIndex])*intervalValues[i][vShaleIndex] - data[i][rhobIndex]
                                        : nullValue;

                                    double porFunctionDerivative = Ia!=nullValue && guessPorosity!=nullValue && intervalValues[i][matrixDensityIndex]!=nullValue && intervalValues[i][vShaleIndex]!=nullValue
                                        ? (mudFlitrateDensity-reservorHCDensity)*Ia*(1-cementArchie/satArchie)*Math.pow(guessPorosity, -cementArchie/satArchie)
                                            +(reservorHCDensity-intervalValues[i][matrixDensityIndex]) - (currentShaleDensity-intervalValues[i][matrixDensityIndex])*intervalValues[i][vShaleIndex]
                                        : nullValue;

                                    guessPorosity = porFunction!=nullValue && porFunctionDerivative!=nullValue ? guessPorosity - porFunction/porFunctionDerivative : nullValue;
                                    if (guessPorosity==nullValue)
                                        break innerWhile;
                                }

                                intervalValues[i][phiTIndex] = intervalValues[i][vShaleIndex]>0.55 || (guessPorosity!=nullValue && guessPorosity<=0) ?
                                        (data[i][rhobIndex]!=nullValue ? (currentShaleDensity-data[i][rhobIndex])/(currentShaleDensity-1.0) : nullValue) : guessPorosity ;

                                intervalValues[i][phiEIndex] = (intervalValues[i][phiTIndex]!=nullValue && intervalValues[i][vShaleIndex]!=nullValue)
                                        ? intervalValues[i][phiTIndex] - shalePorsity*intervalValues[i][vShaleIndex] : nullValue;
                                intervalValues[i][phiEIndex] = intervalValues[i][phiEIndex]!=nullValue && intervalValues[i][phiEIndex]<=0
                                        ? 0.001 : intervalValues[i][phiEIndex];

                                if (intervalValues[i][phiOIndex]!=nullValue)
                                    phiESeries.getData().add(new Data(intervalValues[i][phiEIndex], intervalValues[i][intervalDepthIndex]));
                                if (intervalValues[i][phiTIndex]!=nullValue)
                                    phiTSeries.getData().add(new Data(intervalValues[i][phiTIndex], intervalValues[i][intervalDepthIndex]));
                            }

                            double zeta = grMax/(grMax-grMin);
                            double phiSD = intervalValues[grMinIndex][phiTIndex];
                            double phiSH = intervalValues[grMaxIndex][phiEIndex];
                            System.out.println("depth Rwfor DRESH phiT vshale");
                            for (int i = startIndex; i < endIndex; ++i) {

                                double vshaleVal = intervalValues[i][vShaleIndex];
                                intervalValues[i][phiDispIndex] = vshaleVal!=nullValue ? phiSD - ((vShaleMax-vshaleVal)*(1-phiSH)/zeta) : nullValue;
                                intervalValues[i][phiMinIndex] = phiSD*phiSH;
                                intervalValues[i][GRmaxdIndex] = grMin + phiSD*grMax;
                                intervalValues[i][phiDisSHIndex] = vshaleVal!=nullValue ? phiSH*(vShaleMax-vshaleVal) : nullValue;
                                intervalValues[i][phiDispIndex] = intervalValues[i][phiDispIndex]<=intervalValues[i][phiMinIndex] ?
                                        nullValue: intervalValues[i][phiDispIndex];
                                intervalValues[i][phiDisSHIndex] = intervalValues[i][phiDisSHIndex]<=intervalValues[i][phiMinIndex] ?
                                        nullValue: intervalValues[i][phiDisSHIndex];

                                intervalValues[i][phiLamIndex] = vshaleVal!=nullValue ?  vshaleVal*phiSD + (vShaleMax-vshaleVal)*phiSH : nullValue;

                                intervalValues[i][phiStrIndex] = vshaleVal!=nullValue ? phiSD + (vShaleMax-vshaleVal)*phiSH : nullValue;

                                intervalValues[i][VLamIndex] = (vshaleVal!=nullValue && intervalValues[i][phiTIndex]!=nullValue) ? (intervalValues[i][phiTIndex]-phiSD+intervalValues[i][vShaleIndex]*(1-phiSH))/(1-phiSD) : nullValue;
                                if (intervalValues[i][VLamIndex]!=nullValue){
                                    intervalValues[i][VLamIndex] = intervalValues[i][VLamIndex]<=0 ? 0.001 : intervalValues[i][VLamIndex];
                                    intervalValues[i][VLamIndex] = intervalValues[i][VLamIndex]>=1 ? 0.999 : intervalValues[i][VLamIndex];
                                }
                                if (intervalValues[i][VLamIndex]!=nullValue)
                                    vLamSeries.getData().add(new Data(intervalValues[i][VLamIndex], intervalValues[i][intervalDepthIndex]));

                                intervalValues[i][SandFractionIndex] = intervalValues[i][VLamIndex]!=nullValue ? 1- intervalValues[i][VLamIndex] : nullValue;
                                if (intervalValues[i][SandFractionIndex]!=nullValue)
                                    sFSeries.getData().add(new Data(intervalValues[i][SandFractionIndex], intervalValues[i][intervalDepthIndex]));

                                intervalValues[i][phiTSDIndex] = intervalValues[i][phiTIndex]!=nullValue && intervalValues[i][VLamIndex]!=nullValue
                                        ? (intervalValues[i][VLamIndex]>=0.65 || intervalValues[i][phiTIndex]<0 || intervalValues[i][phiTIndex]>0.45)
                                            ? intervalValues[i][phiTIndex]
                                            : (intervalValues[i][phiTIndex] - intervalValues[i][VLamIndex]*phiSH)/(1-intervalValues[i][VLamIndex])
                                        : nullValue;
                                if (intervalValues[i][phiTSDIndex]!=nullValue)
                                    phiTSDSeries.getData().add(new Data(intervalValues[i][phiTSDIndex], intervalValues[i][intervalDepthIndex]));

                                intervalValues[i][VDispIndex] = intervalValues[i][vShaleIndex]!=nullValue && intervalValues[i][VLamIndex]!=nullValue
                                        ? intervalValues[i][vShaleIndex] - intervalValues[i][VLamIndex] : nullValue;
                                intervalValues[i][VDispSDIndex] = intervalValues[i][VDispIndex]!=nullValue && intervalValues[i][VLamIndex]!=nullValue
                                        ? intervalValues[i][VDispIndex]/(1 - intervalValues[i][VLamIndex]) : nullValue;

                                if (intervalValues[i][VDispIndex]!=nullValue){
                                    intervalValues[i][VDispIndex] = intervalValues[i][VDispIndex]<=0 ? 0.001 : intervalValues[i][VDispIndex];
                                    intervalValues[i][VDispIndex] = intervalValues[i][VDispIndex]>=1 ? 0.999 : intervalValues[i][VDispIndex];
                                }
                                if (intervalValues[i][VDispSDIndex]!=nullValue){
                                    intervalValues[i][VDispSDIndex] = intervalValues[i][VDispSDIndex]<=0 ? 0.001 : intervalValues[i][VDispSDIndex];
                                    intervalValues[i][VDispSDIndex] = intervalValues[i][VDispSDIndex]>=1 ? 0.999 : intervalValues[i][VDispSDIndex];
                                }
                                if (intervalValues[i][VDispIndex]!=nullValue)
                                    vDispSeries.getData().add(new Data(intervalValues[i][VDispIndex], intervalValues[i][intervalDepthIndex]));

                                intervalValues[i][phieSDIndex] = intervalValues[i][phiTSDIndex]!=nullValue && intervalValues[i][VDispSDIndex]!=nullValue && intervalValues[i][VLamIndex]!=nullValue
                                    ? intervalValues[i][phiTSDIndex] - intervalValues[i][VDispSDIndex]*phiSH*(1-intervalValues[i][VLamIndex])
                                    : nullValue;
                                intervalValues[i][phieSDIndex] = (intervalValues[i][phieSDIndex]!=nullValue && intervalValues[i][phieSDIndex]<=0) ? 0.001 : intervalValues[i][phieSDIndex];

                                if (finalRwfor[i]!=nullValue && isDRESHpresent && data[i][dRESHIndex]!=nullValue && intervalValues[i][phiTIndex]!=nullValue){
                                    double cW = 1/finalRwfor[i];
                                    double sWa = Math.pow((tortousityFactor*finalRwfor[i]/(data[i][dRESHIndex]*
                                                    Math.pow(intervalValues[i][phiTIndex], cementArchie))),
                                            (1/satArchie));
                                    if(sWa>=0 && intervalValues[i][vShaleIndex]!=nullValue){
                                        if (sWa>1 || intervalValues[i][vShaleIndex]>0.5)
                                            sWa = 1;
                                        sWSeries.getData().add(new Data(sWa, intervalValues[i][intervalDepthIndex]));
                                    }
                                }
                                System.out.println(intervalValues[i][intervalDepthIndex]+" "+finalRwfor[i]+" "+data[i][dRESHIndex]+" "+intervalValues[i][phiTIndex]+" "+intervalValues[i][vShaleIndex]);
                            }

                            lineChartphi[0].getData().clear();
                            lineChartphi[0].getData().addAll(phiESeries, phiTSeries);
                            lineChartPhiNCNphi[0].getData().clear();
                            lineChartPhiNCNphi[0].getData().addAll(phiNCSeries, NphiSeries);
                            lineChartPhiDCDX[0].getData().clear();
                            lineChartPhiDCDX[0].getData().addAll(phiDCSeries, phiDxSeries);
                            lineChartThomasSteiber[0].getData().clear();
                            lineChartThomasSteiber[0].getData().addAll(vLamSeries, vDispSeries, phiTSDSeries);
                            lineChartSw[0].getData().clear();
                            lineChartSw[0].getData().add(sWSeries);
                            lineChartSf[0].getData().clear();
                            lineChartSf[0].getData().add(sFSeries);

                            lineChartphi[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                            phiESeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: red;");
                            phiTSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: black;");

                            lineChartPhiNCNphi[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                            phiNCSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #8e8e8a;");

                            lineChartPhiDCDX[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                            phiDCSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: green;");
                            phiDxSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: yellow;");

                            lineChartThomasSteiber[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                            vLamSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #898a90;");
                            vDispSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #cc2409;");
                            phiTSDSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #09cc59;");

                            lineChartSf[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                            sFSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #21cbbc;");

                            lineChartSw[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
                            sWSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: pink;");

                            graphUI ob = new graphUI();

                            double coordinates[][] = new double[4][2];
                            coordinates[0][0] = 0;
                            coordinates[0][1] = phiSH;

                            coordinates[1][0] = 1-phiSD;
                            coordinates[1][1] = phiSD+phiSH;

                            coordinates[2][0] = 1;
                            coordinates[2][1] = phiSD;

                            coordinates[3][0] = 1-((phiSD-phiSD*phiSH)*zeta)/(1-phiSH);
                            coordinates[3][0] = coordinates[3][0]>1 ? 1 : coordinates[3][0];
                            coordinates[3][1] = phiSD*phiSH;

                            ob.plot(coordinates, startIndex, endIndex, intervalValues, phiTIndex, vShaleIndex, nullValue);
                        }
                        else {
                            error.setStyle("-fx-text-fill: red;");
                            error.setText("Error in updating Vshale");
                        }
                    });

                    ContextMenu grMenus = new ContextMenu();
                    grMenus.getItems().addAll(startDepth, endDepth);
                    lineChartGr.setOnContextMenuRequested(e -> {
                        grMenus.show(finalLineChartGr, e.getScreenX(), e.getScreenY());
                        if (startDepth.isDisable())
                            yValue[1] = (double) finalLineChartGr.getYAxis().getValueForDisplay(e.getY()-38);
                        else
                            yValue[0] = (double) finalLineChartGr.getYAxis().getValueForDisplay(e.getY()-38);
                    });

                    areaChartVshale = new modifiedAreaPlot<>(new NumberAxis(0, 1, 0.1), yAxis, areaSeries);
                    areaChartVshale.getYAxis().setOpacity(0);
                    areaChartVshale.getYAxis().setTickLabelsVisible(false);
                    areaChartVshale.setTitle("Vshale ("+title+")");
                    areaChartVshale.setCreateSymbols(false);
                    areaChartVshale.setLegendVisible(false);
                    areaChartVshale.setPadding(new Insets(0));
                    areaChartVshale.getData().add(areaSeries);

                    lineChartRhob = linecharts(new NumberAxis(),"Rhob");
                    lineChartRhob.getData().add(RhobSeries);

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
                            data[dataRowIndex][depthIndex] = value;
                        else if (textInd == Double.parseDouble(dbmlObject.output[dbmlObject.grIndex])) {
                            data[dataRowIndex][grIndex] = value;
                            if (value != nullValue) {
                                grSeries.getData().add(new Data(value, data[dataRowIndex][depthIndex]));
                                if (grMin==nullValue){
                                    grMin = value;
                                    grMax = value;
                                }
                                else {
                                    grMin = Math.min(grMin, value);
                                    grMax = Math.max(grMax, value);
                                }
                            }
                        } else if (isNphiPresent &&  textInd == Double.parseDouble(dbmlObject.output[dbmlObject.nPhiIndex])) {
                            data[dataRowIndex][nPhiIndex] = value;
                            if (value != nullValue)
                                NphiSeries.getData().add(new Data(value, data[dataRowIndex][depthIndex]));
                        } else if (textInd == Double.parseDouble(dbmlObject.output[dbmlObject.rhobIndex])) {
                            data[dataRowIndex][rhobIndex] = value;
                            if (value != nullValue) {
                                RhobSeries.getData().add(new Data(value, data[dataRowIndex][depthIndex]));
                                if (avgShaleDensity[0] < value)
                                    avgShaleDensity[0] = value + 0.005;
                            }
                        } else if (isRxoPresent && textInd == Double.parseDouble(dbmlObject.output[dbmlObject.rxoIndex]))
                            data[dataRowIndex][rxoIndex] = value;
                        else if (isSRESHpresent && textInd == Double.parseDouble(dbmlObject.output[dbmlObject.SRESHIndex]))
                            data[dataRowIndex][sRESHIndex] = value;
                        else if (isMRESHpresent && textInd == Double.parseDouble(dbmlObject.output[dbmlObject.MRESHIndex]))
                            data[dataRowIndex][mRESHIndex] = value;
                        else if (isDRESHpresent && textInd == Double.parseDouble(dbmlObject.output[dbmlObject.DRESHIndex]))
                            data[dataRowIndex][dRESHIndex] = value;
                        textindex = indexOf + 1;
                        ++textInd;
                    }
                    Rwfor[dataRowIndex] = nullValue;
                    Rwxo[dataRowIndex] = nullValue;

                    String loacFlag = dbmlObject.output[dbmlObject.flaglocationIndex];
                    double tvd = data[dataRowIndex][Integer.parseInt(dbmlObject.output[dbmlObject.tvdIndex])];
                    double kb = Double.parseDouble(dbmlObject.output[dbmlObject.KBIndex]);
                    double wd = Double.parseDouble(dbmlObject.output[dbmlObject.WDIndex]);
                    dbml[dataRowIndex] = loacFlag.equals("1") ? tvd - kb - wd : tvd - kb;

                    String tempFlag = dbmlObject.output[dbmlObject.flagTempIndex];
                    double tGrad = Double.parseDouble(dbmlObject.output[dbmlObject.TGRADIndex]);
                    double tS = Double.parseDouble(dbmlObject.output[dbmlObject.TSIndex]);
                    double tFor = Double.parseDouble(dbmlObject.output[dbmlObject.TFORIndex]);
                    double dbmlTFor = Double.parseDouble(dbmlObject.output[dbmlObject.DBMLTFORIndex]);
                    if (tempFlag.equals("Celsius")){
                        tGrad = faren2Cel(tGrad);
                        tS = faren2Cel(tS);
                        tFor = faren2Cel(tFor);
                        dbmlTFor = faren2Cel(dbmlTFor);
                    }
                    fTemp[dataRowIndex] = tGrad == 0 ? tS + dbml[dataRowIndex] * (tFor - tS) / dbmlTFor : tS + dbml[dataRowIndex] * tGrad / 100;

                    String mudFlag = dbmlObject.output[dbmlObject.flagMudIndex];
                    if (mudFlag.equals("Water based mud")) {
                        double Rw = Double.parseDouble(dbmlObject.output[dbmlObject.RwIndex]);
                        double tempRw = Double.parseDouble(dbmlObject.output[dbmlObject.TempRwIndex]);
                        double Rmf = Double.parseDouble(dbmlObject.output[dbmlObject.RmfIndex]);
                        double tempRmf = Double.parseDouble(dbmlObject.output[dbmlObject.TempRmfIndex]);
                        if (tempFlag.equals("Celsius")){
                            tempRw = faren2Cel(tempRw);
                            tempRmf = faren2Cel(tempRmf);
                        }

                        Rwfor[dataRowIndex] = tempFlag.equals("Fahrenheit") ? Rw*(tempRw+6.77)/(fTemp[dataRowIndex]+6.77): Rw*(tempRw+21.5)/(fTemp[dataRowIndex]+21.5);
                        Rwxo[dataRowIndex] = tempFlag.equals("Fahrenheit") ? Rmf*(tempRmf+6.77)/(fTemp[dataRowIndex]+6.77): Rmf*(tempRmf+21.5)/(fTemp[dataRowIndex]+21.5);
                    } else{
                        data[dataRowIndex][rxoIndex] = isSRESHpresent ?  data[dataRowIndex][sRESHIndex] : data[dataRowIndex][mRESHIndex];
                        Rwfor[dataRowIndex] =  Rwxo[dataRowIndex] = data[dataRowIndex][rxoIndex];
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

            lineChartGr.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            grSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #6ab25f;");
            lineChartRhob.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            RhobSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: blue;");
            lineChartPhiNCNphi[0].setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
            NphiSeries.getNode().setStyle("-fx-stroke-width: 1;-fx-stroke: #e0f558;");

            stage.setMaximized(true);
            curves.getItems().clear();
            HBox hb = new HBox(0, depthVb, lineChartGr);
            hb.setPadding(new Insets(0));
            curves.getItems().addAll(hb, areaChartVshale, lineChartRhob, lineChartPhiDCDX[0], lineChartPhiNCNphi[0], lineChartphi[0], lineChartThomasSteiber[0], lineChartSf[0], lineChartSw[0]);

        }
        catch (Exception ex) {
            ex.printStackTrace();
            data[0][0] = -999999;
        }
    }

    public double[] getGRRange(double depth[]){
        double[] results = new double[4];

        Stage vrStage = new Stage();

        int numberOfGroups = 200;
        int GRUpperLimit = 200;
        int group[] = new int[numberOfGroups];
        double[][] grMinMax = new double[numberOfGroups][4];
        for (int i=0;i<numberOfGroups;++i) {
            grMinMax[i][1] = Integer.MAX_VALUE;
            grMinMax[i][3] = Integer.MIN_VALUE;
        }
        inner: for(int i=0; i<dataSize; i++){
            if(data[i][depthIndex]<depth[0])
                results[0] = i;
            else if (data[i][depthIndex]<depth[1])
                results[1] = i;
            if(data[i][depthIndex]>=depth[0] && data[i][depthIndex]<=depth[1]) {
                if (data[i][grIndex]!=nullValue) {
                    int groupNumber = (int) (data[i][grIndex] / (GRUpperLimit / numberOfGroups));
                    group[groupNumber]++;
                    if (grMinMax[groupNumber][1] > data[i][grIndex]) {
                        grMinMax[groupNumber][0] = i;
                        grMinMax[groupNumber][1] = data[i][grIndex];
                    }
                    if (grMinMax[groupNumber][3] < data[i][grIndex]) {
                        grMinMax[groupNumber][2] = i;
                        grMinMax[groupNumber][3] = data[i][grIndex];
                    }
                }
            }
            else if(data[i][depthIndex]>depth[1])
                break inner;
        }

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> barChart = new BarChart<>(xAxis,yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        xAxis.setLabel(" GR values ");
        yAxis.setLabel("frequency");
        barChart.setCategoryGap(0);
        barChart.setBarGap(0);

        XYChart.Series series1 = new XYChart.Series();
        for (int i=0;i<numberOfGroups;++i)
            series1.getData().add(new Data(i*GRUpperLimit/numberOfGroups+"-"+(i+1)*~GRUpperLimit/numberOfGroups, group[i]));

        barChart.getData().addAll(series1);
        final boolean[] firstSelected = {false};
        final Glow glow = new Glow(.8);
        for (XYChart.Series<String,Number> serie: barChart.getData()){
            for (XYChart.Data<String, Number> item: serie.getData()){
                final Node n = item.getNode();
                n.setCursor(Cursor.HAND);
                n.setEffect(null);
                n.setOnMouseEntered(e -> n.setEffect(glow));
                n.setOnMouseExited(e -> n.setEffect(null));
                n.setOnMouseClicked(e -> {
                    n.setEffect(glow);
                    String[] range = item.getXValue().split("-");
                    int groupNumber = (int) (Double.parseDouble(range[0])*numberOfGroups/GRUpperLimit);
                    n.setStyle("-fx-bar-fill: red");
                    if (!firstSelected[0]){
                        firstSelected[0] = !firstSelected[0];
                        results[2] = grMinMax[groupNumber][0];
                    }
                    else{
                        firstSelected[0] = !firstSelected[0];
                        results[3] = grMinMax[groupNumber][2];
                        vrStage.close();
                    }
                });
            }
        }

        BorderPane layout = new BorderPane(barChart);
        Scene scene = new Scene(layout, 800, 450);

        vrStage.setTitle("GR Max-Min picking");
        vrStage.setScene(scene);
        vrStage.getIcons().add(new Image(getClass().getResourceAsStream("../../resources/images/main_favicon.png")));
        vrStage.initModality(Modality.APPLICATION_MODAL);
        vrStage.showAndWait();

        return results;
    }

    private class LineChartWithMarkers<X,Y> extends LineChart {

        private ObservableList<Data<X, Y>> horizontalMarkers;
        private ObservableList<Data<X, Y>> verticalMarkers;

        public LineChartWithMarkers(NumberAxis xAxis, NumberAxis yAxis) {
            super(xAxis, yAxis);
            horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
            horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
            verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
            verticalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
        }

        public void addHorizontalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (horizontalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            horizontalMarkers.add(marker);
        }

        public void addVerticalValueMarker(Data<X, Y> marker) {
            Objects.requireNonNull(marker, "the marker must not be null");
            if (verticalMarkers.contains(marker)) return;
            Line line = new Line();
            marker.setNode(line );
            getPlotChildren().add(line);
            verticalMarkers.add(marker);
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
            for (Data<X, Y> verticalMarker : verticalMarkers) {
                Line line = (Line) verticalMarker.getNode();
                line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
                line.setEndX(line.getStartX());
                line.setStartY(0d);
                line.setEndY(getBoundsInLocal().getHeight());
                line.toFront();
            }
        }
    }

    public LineChart<Number, Number> linecharts(NumberAxis xAxis, String plotName){
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

    public double faren2Cel(double degreeC){
        return (degreeC - 32)*0.5556;
    }
}