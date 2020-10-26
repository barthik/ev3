package application.ui.scene;

import application.logic.IOOperations;
import application.model.ApplicationSettings;
import application.model.neuralnetwork.backpropagation.NeuralNetwork;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Scene for simulation settings.
 *
 * @author Dizzy
 */
public class RunExperimentScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private XYChart.Series<Number, Number> dataSeries;
    private NumberAxis xAxis;
    private double time = 0;
    private LocalDateTime startTime = null;
    private LocalDateTime runTime = null;
    private Text elapsedField = null;
    private Text loopsField = null;
    private Text msLoopField = null;
    private Text msNNField = null;
    private Text batteryField = null;
    private Text speedAField = null;
    private Text speedBField = null;
    private Text s1Field = null;
    private Text s2Field = null;
    private Text s3Field = null;
    private Text s4Field = null;
    private TextArea loggingArea = null;
    private Button runButton = null;
    private Button pauseButton = null;
    private Button logButton = null;
    private long actualActiveNeurons = 0;
    private int actualASpeed = 0;
    private int actualBSpeed = 0;
    private float actualS1Distance = 0;
    private float actualS2Distance = 0;
    private float actualS3Distance = 0;
    private float actualS4Distance = 0;
    private long passedTime = 0;
    private NeuralNetwork neuralNetwork = null;

    /**
     * Create scene.
     *
     */
    public RunExperimentScene() {
        //init
        neuralNetwork = ApplicationSettings.loadedExperiment.getNeuralNetwork();

        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Run experiment");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        GridPane dataGrid = new GridPane();
        dataGrid.setHgap(20);
        dataGrid.setVgap(10);

        Label elapsedLabel = new Label("Passed time:");
        dataGrid.add(elapsedLabel, 0, 0);
        elapsedField = new Text("idle");
        dataGrid.add(elapsedField, 1, 0);

        Label loopsLabel = new Label("Passed loops:");
        dataGrid.add(loopsLabel, 0, 1);
        loopsField = new Text("idle");
        dataGrid.add(loopsField, 1, 1);

        Label msLoopLabel = new Label("Execution time (ms/loop):");
        dataGrid.add(msLoopLabel, 2, 0);
        msLoopField = new Text("idle");
        dataGrid.add(msLoopField, 3, 0);

        Label msNNLabel = new Label("Network response (ms/loop):");
        dataGrid.add(msNNLabel, 2, 1);
        msNNField = new Text("idle");
        dataGrid.add(msNNField, 3, 1);

        Label speedALabel = new Label("Engine speed A (d/s):");
        dataGrid.add(speedALabel, 4, 0);
        speedAField = new Text("idle");
        dataGrid.add(speedAField, 5, 0);

        Label speedBLabel = new Label("Engine speed B (d/s):");
        dataGrid.add(speedBLabel, 4, 1);
        speedBField = new Text("idle");
        dataGrid.add(speedBField, 5, 1);

        Label batteryLabel = new Label("Estimated battery (%):");
        dataGrid.add(batteryLabel, 6, 0);
        batteryField = new Text("idle");
        dataGrid.add(batteryField, 7, 0);

        Label s1Label = new Label("Sensor S1:");
        dataGrid.add(s1Label, 0, 3);
        s1Field = new Text("idle");
        dataGrid.add(s1Field, 1, 3);

        Label s2Label = new Label("Sensor S2:");
        dataGrid.add(s2Label, 2, 3);
        s2Field = new Text("idle");
        dataGrid.add(s2Field, 3, 3);

        Label s3Label = new Label("Sensor S3:");
        dataGrid.add(s3Label, 4, 3);
        s3Field = new Text("idle");
        dataGrid.add(s3Field, 5, 3);

        Label s4Label = new Label("Sensor S4:");
        dataGrid.add(s4Label, 6, 3);
        s4Field = new Text("idle");
        dataGrid.add(s4Field, 7, 3);

        content.getChildren().add(dataGrid);

        content.getChildren().add(createChart());

        Label logLabel = new Label("Experiment log:");
        loggingArea = new TextArea("Ready to run experiment...");
        loggingArea.setPrefSize(750, 130);
        loggingArea.setMinHeight(130);
        loggingArea.setEditable(false);
        content.getChildren().addAll(logLabel, loggingArea);

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(10);

        runButton = new Button("Run");
        buttonGrid.add(runButton, 0, 3);
        runButton.setOnAction((ActionEvent e) -> {
            runButton.setDisable(true);
            pauseButton.setDisable(false);
            logButton.setDisable(true);

            runTime = LocalDateTime.now();

            if (startTime == null) {
                startTime = LocalDateTime.now();
                loggingArea.clear();
                loggingArea.appendText("INFO: Experiment started at " + startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n");
            } else {
                loggingArea.appendText("INFO: Experiment is continued after stopping at " + runTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n");
            }
            ApplicationSettings.experimentTimeline.play();
        });

        pauseButton = new Button("Pause");
        pauseButton.setDisable(true);
        buttonGrid.add(pauseButton, 1, 3);
        pauseButton.setOnAction((ActionEvent e) -> {
            stopEngines();
            runButton.setDisable(false);
            logButton.setDisable(false);
            pauseButton.setDisable(true);
            ApplicationSettings.experimentTimeline.stop();
            passedTime += java.time.Duration.between(runTime, LocalDateTime.now()).getSeconds();
            loggingArea.appendText("INFO: Experiment has been stopped at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n");
        });

        logButton = new Button("Save log");
        logButton.setDisable(true);
        buttonGrid.add(logButton, 2, 3);
        logButton.setOnAction((ActionEvent e) -> {
            logButton.setDisable(true);
            IOOperations.saveLogFile(removeExtension(ApplicationSettings.loadedExperiment.getExperimentFile().getName()) + "-logs", loggingArea.getText());
            loggingArea.clear();
            loggingArea.appendText("INFO: Experiment log was saved at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n");
        });

        content.getChildren().add(buttonGrid);

        root.setCenter(content);
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);
        ApplicationSettings.experimentStage.setOnCloseRequest(event -> {
            event.consume();
            ApplicationMenu.getInstance().enableExpRun();
            ApplicationSettings.experimentTimeline.stop();
            ApplicationSettings.experimentStage.close();
            ApplicationSettings.experimentTimeline = null;
            stopEngines();
            loggingArea.appendText("INFO: Experiment ended at " + startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n");
            IOOperations.saveLogFile(removeExtension(ApplicationSettings.loadedExperiment.getExperimentFile().getName()) + "-logs", loggingArea.getText());
        });

        setAnimation();
    }

    /**
     * Set animation (main loop)
     *
     */
    private void setAnimation() {
        DecimalFormat df = new DecimalFormat("#.###");
        ApplicationSettings.experimentTimeline = new Timeline();
        ApplicationSettings.experimentTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(ApplicationSettings.loadedExperiment.getTick()), (ActionEvent actionEvent) -> {
            long loopStart = System.currentTimeMillis();

            time++;
            loadData(); //get data from sensors and battery and plot

            neuralNetwork.setInputs(assambleInput()); //assemble input and set to NN

            long nnStart = System.nanoTime();
            actualActiveNeurons = neuralNetwork.activate(); //activate NN and set num of excited neurons
            long nnEnd = System.nanoTime();

            setSpeed();

            speedAField.setText(String.valueOf(actualASpeed));
            speedBField.setText(String.valueOf(actualBSpeed));

            msNNField.setText(df.format((nnEnd - nnStart) * 0.000001));

            plotChartData(); //plot data to chart

            long loopEnd = System.currentTimeMillis();
            msLoopField.setText(String.valueOf(loopEnd - loopStart));

            String log = "RUN: t" + Math.round(time) + ": NN response=" + df.format((nnEnd - nnStart) * 0.000001) + "ms, execution time=" + (loopEnd - loopStart) + "ms";
            log += "\n\tSENSOR DATA (cm):\n";
            log += "\t\tS1: " + actualS1Distance + ", S2:" + actualS2Distance + ", S3:" + actualS3Distance + ", S4:" + actualS4Distance;
            log += "\n\tENGINE DATA (d/s):\n";
            log += "\t\tA: " + actualASpeed + ", B:" + actualBSpeed;
            log += "\n\tOTHER:\n";
            log += "\t\tBattery: " + batteryField.getText() + "%\n";
            loggingArea.appendText(log);

            if (time % 100 == 0) { // save log file after 100 loops
                IOOperations.saveLogFile(removeExtension(ApplicationSettings.loadedExperiment.getExperimentFile().getName()) + "-logs", loggingArea.getText());
                loggingArea.clear();
                loggingArea.appendText("INFO: Experiment log was auto-saved at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + "\n");
            }
        }));
        ApplicationSettings.experimentTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private void stopEngines() {
        try {
            ApplicationSettings.connectedEV3.getMotorA().setSpeed(0);
            ApplicationSettings.connectedEV3.getMotorA().stop(true);
            ApplicationSettings.connectedEV3.getMotorB().setSpeed(0);
            ApplicationSettings.connectedEV3.getMotorB().stop(true);
        } catch (RemoteException ex) {
            speedAField.setText(String.valueOf("engine failure"));
            speedBField.setText(String.valueOf("engine failure"));
            loggingArea.appendText("ERROR:  Can not stop engines (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + ")\n");
        }
    }

    /**
     * Convert real output of NN to simulation engine speeds.
     *
     */
    private void setSpeed() {
        double[][] realOutputs = neuralNetwork.getRealOutputs();
        int speed = 0;
        int tempForwardA = 0;
        int tempForwardB = 0;
        int tempBackwardA = 0;
        int tempBackwardB = 0;
        actualASpeed = 0;
        actualBSpeed = 0;

        for (int i = 0; i < realOutputs.length; i++) { // get speed data with priority for forward value
            for (int j = 0; j < realOutputs[0].length; j++) {
                speed = (int) Math.round(realOutputs[i][j] * ApplicationSettings.maxEV3Speed); //normalize output for motors from [0,1] to int    
                if (ApplicationSettings.loadedExperiment.getMotorSettings().get(j).equals("Forward A") && speed > tempForwardA) {
                    tempForwardA = speed;
                }
                if (ApplicationSettings.loadedExperiment.getMotorSettings().get(j).equals("Backward A") && speed > tempBackwardA) {
                    tempBackwardA = speed;
                }
                if (ApplicationSettings.loadedExperiment.getMotorSettings().get(j).equals("Forward B") && speed > tempForwardB) {
                    tempForwardB = speed;
                }
                if (ApplicationSettings.loadedExperiment.getMotorSettings().get(j).equals("Backward B") && speed > tempBackwardB) {
                    tempBackwardB = speed;
                }
            }
        }
        try {
            if (tempForwardA >= tempBackwardA) {
                actualASpeed = tempForwardA;
                ApplicationSettings.connectedEV3.getMotorA().setSpeed(actualASpeed);
                ApplicationSettings.connectedEV3.getMotorA().forward();
            } else {
                actualASpeed = tempBackwardA;
                ApplicationSettings.connectedEV3.getMotorA().setSpeed(actualASpeed);
                ApplicationSettings.connectedEV3.getMotorA().backward();
                actualASpeed = -actualASpeed;
            }
            if (tempForwardB >= tempBackwardB) {
                actualBSpeed = tempForwardB;
                ApplicationSettings.connectedEV3.getMotorB().setSpeed(actualBSpeed);
                ApplicationSettings.connectedEV3.getMotorB().forward();
            } else {
                actualBSpeed = tempBackwardB;
                ApplicationSettings.connectedEV3.getMotorB().setSpeed(actualBSpeed);
                ApplicationSettings.connectedEV3.getMotorB().backward();
                actualBSpeed = -actualBSpeed;
            }
        } catch (RemoteException ex) {
            speedAField.setText(String.valueOf("engine failure"));
            speedBField.setText(String.valueOf("engine failure"));
            loggingArea.appendText("ERROR: Can not adjust the engine speed (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) + ")\n");
        }
    }

    /**
     * Assambles input from sensor data.
     *
     * @retrun double[][] Return assembled input.
     */
    private double[][] assambleInput() {
        int inputsNeurons = neuralNetwork.getInputLayer().size();
        double inputs[][] = new double[1][inputsNeurons];
        boolean validation = false;

        for (int i = 0; i < inputsNeurons; i++) { //for all inputs
            validation = false;

            if (actualS1Distance >= ApplicationSettings.loadedExperiment.getSensorS1DistancesMin().get(i) && actualS1Distance <= ApplicationSettings.loadedExperiment.getSensorS1DistancesMax().get(i)) { //chceck if S1 distance is greater or equal than in list
                if (actualS2Distance >= ApplicationSettings.loadedExperiment.getSensorS2DistancesMin().get(i) && actualS2Distance <= ApplicationSettings.loadedExperiment.getSensorS2DistancesMax().get(i)) {
                    if (actualS3Distance >= ApplicationSettings.loadedExperiment.getSensorS3DistancesMin().get(i) && actualS3Distance <= ApplicationSettings.loadedExperiment.getSensorS3DistancesMax().get(i)) {
                        if (actualS4Distance >= ApplicationSettings.loadedExperiment.getSensorS4DistancesMin().get(i) && actualS4Distance <= ApplicationSettings.loadedExperiment.getSensorS4DistancesMax().get(i)) {
                            validation = true;
                        }
                    }
                }
            }

            if (validation) { //if the values match the settings of input activate it
                inputs[0][i] = 1;
            } else {
                inputs[0][i] = 0;
            }
        }

        return inputs;
    }

    /**
     * Plot chart of NN activity.
     *
     */
    private void plotChartData() {
        if ((time % 1) == 0) {

            // every steps after 100 move range 1 time
            if (time > 100) {
                xAxis.setLowerBound(xAxis.getLowerBound() + 1);
                xAxis.setUpperBound(xAxis.getUpperBound() + 1);
            }
        }

        dataSeries.getData().add(new XYChart.Data<>(time, actualActiveNeurons));

        // after 100 steps delete old data
        if (time > 100) {
            dataSeries.getData().remove(0);
        }
    }

    /**
     * Load actual data to scene.
     *
     */
    private void loadData() {
        actualS1Distance = ApplicationSettings.connectedEV3.getSensorS1Data();
        actualS2Distance = ApplicationSettings.connectedEV3.getSensorS2Data();
        actualS3Distance = ApplicationSettings.connectedEV3.getSensorS3Data();
        actualS4Distance = ApplicationSettings.connectedEV3.getSensorS4Data();

        elapsedField.setText(IOOperations.convertTime(java.time.Duration.between(runTime, LocalDateTime.now()).getSeconds() + passedTime));
        loopsField.setText(String.valueOf(Math.round(time)));
        if (time % 100 == 0 || time == 1) { // every 100 loops get actual battery
            batteryField.setText(String.valueOf(Math.round(ApplicationSettings.connectedEV3.getBattery())));
        }

        s1Field.setText(String.valueOf(actualS1Distance));
        s2Field.setText(String.valueOf(actualS2Distance));
        s3Field.setText(String.valueOf(actualS3Distance));
        s4Field.setText(String.valueOf(actualS4Distance));
    }

    /**
     * Create chart of NN activity.
     *
     */
    private LineChart<Number, Number> createChart() {
        xAxis = new NumberAxis(0, 100, 10);
        final NumberAxis yAxis = new NumberAxis(0, neuralNetwork.getHiddenLayer().size() + neuralNetwork.getOutputLayer().size(), 0); // set max y axis value to size of NN
        final LineChart<Number, Number> lc = new LineChart<>(xAxis, yAxis);
        lc.setCreateSymbols(false);
        lc.setAnimated(false);
        lc.setLegendVisible(false);
        lc.setTitle("Neural network activity");
        lc.setPrefSize(700, 200);
        lc.setMinHeight(200);

        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Excited neurons");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, ""));

        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");

        lc.getData().add(dataSeries);
        return lc;
    }

    private static String removeExtension(String s) {
        String separator = System.getProperty("file.separator");
        String filename;

        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }
}
