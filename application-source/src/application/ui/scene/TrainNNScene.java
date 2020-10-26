package application.ui.scene;

import application.logic.IOOperations;
import application.model.ApplicationSettings;
import application.model.neuralnetwork.TrainingSet;
import application.model.neuralnetwork.backpropagation.NeuralNetwork;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import application.ui.ApplicationStateBar;
import application.ui.Dialogs;
import application.ui.MainUI;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

/**
 * Scene for generationg NN
 *
 * @author Dizzy
 */
public class TrainNNScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private Label fileNNLabel = null;
    private Button selectNNButton = null;
    private Label fileTSLabel = null;
    private Button selectTSButton = null;
    private TextField alphaField = null;
    private TextField momentumField = null;
    private TextField maxStepsField = null;
    private TextField minErrorField = null;
    private Button trainButton = null;
    private File nnFile = null;
    private File tsFile = null;
    private NeuralNetwork neuralNetwork = null;
    private TrainingSet trainingSet = null;
    private Label progressText = null;
    private long loopDuration = 0;
    private int maxSteps = 0;
    private double minError = 0;
    private double alpha = 0;
    private double momentum = 0;
    private String result = "LEARNING WAS SUCCESSFUL";
    private boolean succes = true;
    private XYChart.Series<Number, Number> dataSeries;
    private LineChart<Number, Number> lineChart = null;
    private NumberAxis xAxis = null;
    private int i = 0;
    private double error = 1;

    /**
     * Create scene.
     *
     */
    public TrainNNScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Train neural network");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label text = new Label("Select a file to the neural network and training sets. The momentum parametr μ and coefficient of the learning α can be additionally adjusted. Make sure that the training set can be applied to a neural network (must match the number of inputs and outputs). If you choose a large value for the maximum number of steps, learning can take a very long time and may not be successful in the end.");
        text.setWrapText(true);
        content.getChildren().add(text);

        GridPane settingGrid = new GridPane();
        settingGrid.setHgap(20);
        settingGrid.setVgap(10);

        selectNNButton = new Button("Select neural network file...");
        settingGrid.add(selectNNButton, 0, 0);
        fileNNLabel = new Label("No file selected");
        settingGrid.add(fileNNLabel, 1, 0);

        selectTSButton = new Button("Select training set file...");
        settingGrid.add(selectTSButton, 0, 1);
        fileTSLabel = new Label("No file selected");
        settingGrid.add(fileTSLabel, 1, 1);

        Label maxStepsLabel = new Label("Maximum number of steps:");
        settingGrid.add(maxStepsLabel, 0, 3);
        maxStepsField = new TextField("5000");
        settingGrid.add(maxStepsField, 1, 3);
        maxStepsField.setDisable(true);

        Label minErrorLabel = new Label("Minimum error learning:");
        settingGrid.add(minErrorLabel, 0, 4);
        minErrorField = new TextField("0.001");
        settingGrid.add(minErrorField, 1, 4);
        minErrorField.setDisable(true);

        Label aplhaLabel = new Label("α:");
        settingGrid.add(aplhaLabel, 0, 5);
        alphaField = new TextField();
        settingGrid.add(alphaField, 1, 5);
        alphaField.setDisable(true);

        Label momentumLabel = new Label("μ:");
        settingGrid.add(momentumLabel, 0, 6);
        momentumField = new TextField();
        settingGrid.add(momentumField, 1, 6);
        momentumField.setDisable(true);

        trainButton = new Button("Train");
        trainButton.setDisable(true);
        settingGrid.add(trainButton, 0, 7);

        content.getChildren().add(settingGrid);

        progressText = new Label();
        progressText.setWrapText(true);
        progressText.setVisible(false);
        content.getChildren().add(progressText);

        lineChart = createChart();
        lineChart.setVisible(false);
        content.getChildren().add(lineChart);

        root.setTop(ApplicationMenu.getInstance().getMenu());
        root.setCenter(content);
        root.setBottom(ApplicationStateBar.getInstance().getStateBar());
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);

        addListeners();
    }

    private void addListeners() {
        trainButton.setOnAction((ActionEvent e) -> {
            validate();
        });
        selectNNButton.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file");
            fileChooser.setInitialDirectory(IOOperations.getNNDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file(*.xml)", "*.xml"));
            nnFile = fileChooser.showOpenDialog(MainUI.getInstance().getStage());

            if (nnFile != null) {
                fileNNLabel.setText(nnFile.getName());
                neuralNetwork = IOOperations.loadNetwork(nnFile);
                alphaField.setText(String.valueOf(neuralNetwork.getAlfa()));
                momentumField.setText(String.valueOf(neuralNetwork.getMomentum()));
                enableSettings();
            }
        });
        selectTSButton.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file");
            fileChooser.setInitialDirectory(IOOperations.getTSDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text doc(*.txt)", "*.txt"));
            tsFile = fileChooser.showOpenDialog(MainUI.getInstance().getStage());

            if (tsFile != null) {
                fileTSLabel.setText(tsFile.getName());
                enableSettings();
            }
        });
        maxStepsField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                maxStepsField.setText(newValue);
            } else {
                maxStepsField.setText(oldValue);
            }
        });
        minErrorField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                minErrorField.setText(newValue);
            } else {
                minErrorField.setText(oldValue);
            }
        });
        alphaField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                alphaField.setText(newValue);
            } else {
                alphaField.setText(oldValue);
            }
        });
        momentumField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                momentumField.setText(newValue);
            } else {
                momentumField.setText(oldValue);
            }
        });
    }

    private void enableSettings() {
        if (nnFile != null && tsFile != null) {
            maxStepsField.setDisable(false);
            minErrorField.setDisable(false);
            alphaField.setDisable(false);
            momentumField.setDisable(false);
            trainButton.setDisable(false);
        }
    }

    /**
     * Validate input data.
     */
    private void validate() {
        Boolean valid = true;
        String errorMsg = "";

        if ("".equals(maxStepsField.getText()) || "".equals(minErrorField.getText()) || "".equals(alphaField.getText()) || "".equals(momentumField.getText())) {
            errorMsg += "It is necessary to set all the values!\n";
            valid = false;
        }

        try {
            maxSteps = Integer.parseInt(maxStepsField.getText());
            minError = Double.parseDouble(minErrorField.getText());
            alpha = Double.parseDouble(alphaField.getText());
            momentum = Double.parseDouble(momentumField.getText());
        } catch (NumberFormatException e) {
            errorMsg += "Parametrs aplha and momentum must be a number.\n";
            valid = false;
        }

        if (!(alpha >= 0 && alpha <= 1) || !(momentum >= 0 && momentum <= 1)) {
            errorMsg += "Choose a value of alpha and momentum greater than zero or less than or equal to one!\n";
            valid = false;
        }

        if (valid) {
            trainingSet = IOOperations.loadTS(tsFile);
            neuralNetwork = IOOperations.loadNetwork(nnFile);
            neuralNetwork.setAlfa(alpha);
            neuralNetwork.setMomentum(momentum);
            neuralNetwork.setInputs(trainingSet.getInputs());
            neuralNetwork.setExpectedOutputs(trainingSet.getExpectedOutputs());
            neuralNetwork.normalizeOutputs();

            lineChart.setPrefSize(700, 150);
            lineChart.setMinHeight(150);
            dataSeries.getData().clear();
            lineChart.getXAxis().setAutoRanging(true);

            lineChart.setVisible(true);
            selectNNButton.setDisable(true);
            selectTSButton.setDisable(true);
            runTrain();
        } else {
            Dialogs.errorDialog("Invalid data", "Please pay attention to filling the settings.", errorMsg);
        }
    }

    private void runTrain() {
        trainButton.setDisable(true);
        progressText.setVisible(true);
        progressText.setText("The neural network learns the training set, please wait...");

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {
                i = 0;
                error = 1;
                succes = true;
                result = "";

                long startLoop = System.currentTimeMillis();
                for (i = 0; i < maxSteps && error > minError; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    neuralNetwork.trainOneStep();
                    error = neuralNetwork.getLastError();
                    Platform.runLater ( () -> plotChartData(i, error));
                }
                long endLoop = System.currentTimeMillis();

                loopDuration = endLoop - startLoop;

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                trainButton.setDisable(false);
                lineChart.setVisible(false);
                lineChart.setPrefSize(0, 0);
                lineChart.setMinHeight(0);
                selectNNButton.setDisable(false);
                selectTSButton.setDisable(false);

                if (neuralNetwork.getLastStep() >= maxSteps) {
                    result = "LEARNING FAILED\n(the network will not be saved)";
                    succes = false;
                }

                if (succes) {
                    IOOperations.saveNetwork(neuralNetwork, nnFile);
                }

                progressText.setText("Network training completed. The file of training results will be saved in directory with neural network file...\n"
                        + result + "\n"
                        + "Training time: " + IOOperations.convertTime(Math.round(loopDuration / 1000)) + "\n"
                        + "Sum of squared errors: " + neuralNetwork.getLastError() + "\n"
                        + "Number of steps: " + neuralNetwork.getLastStep());
                IOOperations.saveErrorFile(removeExtension(nnFile.getName()) + "-training", neuralNetwork, IOOperations.convertTime(Math.round(loopDuration / 1000)), maxSteps, minError);
            }
        };

        ApplicationSettings.tempService = new Service() {
            @Override
            protected Task createTask() {
                return task;
            }
        };

        ApplicationSettings.tempService.start();
    }

    /**
     * Plot chart of NN activity.
     *
     */
    private void plotChartData(int i, double num) {
        if ((i % 1) == 0) {

            // every steps after 100 move range 1 time
            if (i > 1000) {
                xAxis.setLowerBound(xAxis.getLowerBound() + 1);
                xAxis.setUpperBound(xAxis.getUpperBound() + 1);
            }
        }

        dataSeries.getData().add(new XYChart.Data<>(i, num));

        // after 100 steps delete old data
        if (i > 1000) {
            dataSeries.getData().remove(0);
        }
    }

    /**
     * Create chart of NN activity.
     *
     */
    private LineChart<Number, Number> createChart() {
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        xAxis = new NumberAxis(0, 100, 10);
        final LineChart<Number, Number> lc = new LineChart<>(xAxis, yAxis);
        lc.setCreateSymbols(false);
        lc.setAnimated(false);
        lc.setLegendVisible(false);
        lc.setPrefSize(0, 0);
        lc.setMinHeight(0);

        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Error");
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
