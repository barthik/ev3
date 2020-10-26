package application.ui.scene;

import application.logic.IOOperations;
import application.model.ApplicationSettings;
import application.model.neuralnetwork.backpropagation.NeuralNetwork;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import application.ui.ApplicationStateBar;
import application.ui.Dialogs;
import application.ui.MainUI;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
public class GenerateNNScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private TextField inputField = null;
    private TextField hiddenField = null;
    private TextField outputField = null;
    private TextField alphaField = null;
    private TextField momentumField = null;
    private TextField minLambdaField = null;
    private TextField maxLambdaField = null;
    private NeuralNetwork nn = null;
        private Label progressText = null;
    private long loopDuration = 0;
    private Button saveButton = null;

    /**
     * Create scene.
     *
     */
    public GenerateNNScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Generate neural network");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label text = new Label("To create a network, please set all the parameters of the neural network. If the minimum value of the slope coeficient λ is different from the maximum, will be created inhomogeneous network with different neurons. Select a value of the coefficient learning α and momentum μ (measure of the Influence of prior learning) in the range of 0-1.");
        text.setWrapText(true);
        content.getChildren().add(text);

        GridPane settingGrid = new GridPane();
        settingGrid.setHgap(20);
        settingGrid.setVgap(10);
        Label inputLabel = new Label("Input layer:");
        settingGrid.add(inputLabel, 0, 0);
        inputField = new TextField("0");
        settingGrid.add(inputField, 1, 0);
        Label hiddenLabel = new Label("Hidden layer:");
        settingGrid.add(hiddenLabel, 0, 1);
        hiddenField = new TextField("0");
        settingGrid.add(hiddenField, 1, 1);
        Label outputLabel = new Label("Output layer:");
        settingGrid.add(outputLabel, 0, 2);
        outputField = new TextField("4");
        settingGrid.add(outputField, 1, 2);
        Label aplhaLabel = new Label("α:");
        settingGrid.add(aplhaLabel, 0, 3);
        alphaField = new TextField("0.85");
        settingGrid.add(alphaField, 1, 3);
        Label momentumLabel = new Label("μ:");
        settingGrid.add(momentumLabel, 0, 4);
        momentumField = new TextField("0.9");
        settingGrid.add(momentumField, 1, 4);
        Label minLambdaLabel = new Label("Minimum λ:");
        settingGrid.add(minLambdaLabel, 0, 5);
        minLambdaField = new TextField("1");
        settingGrid.add(minLambdaField, 1, 5);
        Label maxLambdaLabel = new Label("Maximum λ:");
        settingGrid.add(maxLambdaLabel, 0, 6);
        maxLambdaField = new TextField("1");
        settingGrid.add(maxLambdaField, 1, 6);

        saveButton = new Button("Save network...");
        settingGrid.add(saveButton, 0, 8);

        saveButton.setOnAction((ActionEvent e) -> {
            validate();
        });

        content.getChildren().add(settingGrid);
        
        progressText = new Label();
        progressText.setWrapText(true);
        progressText.setVisible(false);
        content.getChildren().add(progressText);

        root.setTop(ApplicationMenu.getInstance().getMenu());
        root.setCenter(content);
        root.setBottom(ApplicationStateBar.getInstance().getStateBar());
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);

        addListeners();
    }

    private void addListeners() {
        inputField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                inputField.setText(newValue);
            } else {
                inputField.setText(oldValue);
            }
        });
        hiddenField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                hiddenField.setText(newValue);
            } else {
                hiddenField.setText(oldValue);
            }
        });
        outputField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                outputField.setText(newValue);
            } else {
                outputField.setText(oldValue);
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
        minLambdaField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                minLambdaField.setText(newValue);
            } else {
                minLambdaField.setText(oldValue);
            }
        });
        maxLambdaField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                maxLambdaField.setText(newValue);
            } else {
                maxLambdaField.setText(oldValue);
            }
        });
    }

    /**
     * Validate input data.
     */
    private void validate() {
        Boolean valid = true;
        String errorMsg = "";
        int inputs = 0;
        int hiddens = 0;
        int outputs = 0;
        double alpha = 0;
        double momentum = 0;
        double minLambda = 0;
        double maxLambda = 0;

        if ("".equals(inputField.getText()) || "".equals(hiddenField.getText()) || "".equals(outputField.getText())) {
            errorMsg += "Set number of input, hidden and output neurons!\n";
            valid = false;
        }

        try {
            inputs = Integer.parseInt(inputField.getText());
            hiddens = Integer.parseInt(hiddenField.getText());
            outputs = Integer.parseInt(outputField.getText());
            alpha = Double.parseDouble(alphaField.getText());
            momentum = Double.parseDouble(momentumField.getText());
            minLambda = Double.parseDouble(minLambdaField.getText());
            maxLambda = Double.parseDouble(maxLambdaField.getText());
        } catch (NumberFormatException e) {
            errorMsg += "Number of neurons must be an integer! Network settings can be decimal.\n";
            valid = false;
        }

        if (inputs < 0 || hiddens < 0 || outputs < 0) {
            errorMsg += "Choose a value of input, hidden and output neurons greater than zero!\n";
            valid = false;
        }

        if (!(alpha >= 0 && alpha <= 1) || !(momentum >= 0 && momentum <= 1)) {
            errorMsg += "Choose a value of alpha and momentum greater than zero or less than or equal to one!\n";
            valid = false;
        }

        if (minLambda > maxLambda) {
            errorMsg += "Unable to select minimum lambda greater than the maximum.\n";
            valid = false;
        }

        if (valid) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save file");
            fileChooser.setInitialDirectory(IOOperations.getNNDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML file (*.xml)", "*.xml"));
            fileChooser.setInitialFileName("neural-network.xml");
            File savedFile = fileChooser.showSaveDialog(MainUI.getInstance().getStage());

            if (savedFile != null) {
                nn = new NeuralNetwork();
                nn.setAlfa(alpha);
                nn.setMomentum(momentum);
                nn.setMinLambda(minLambda);
                nn.setMaxLambda(maxLambda);
                nn.createNetwork(inputs, hiddens, outputs);
                runGenerating(nn, savedFile);
            }
        } else {
            Dialogs.errorDialog("Invalid data", "Please pay attention to filling the settings.", errorMsg);
        }
    }
    
    private void runGenerating(NeuralNetwork nn, File savedFile) {
        saveButton.setDisable(true);
        progressText.setVisible(true);
        progressText.setText("The file is generated, please wait...");

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {

                long startLoop = System.currentTimeMillis();
                IOOperations.saveNetwork(nn, savedFile);
                long endLoop = System.currentTimeMillis();

                loopDuration = endLoop - startLoop;

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                saveButton.setDisable(false);
                progressText.setText("The neural network was created. The file generation took " + IOOperations.convertTime(Math.round(loopDuration / 1000)) + ".");

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
}
