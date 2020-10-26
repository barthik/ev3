package application.ui.scene;

import application.logic.IOOperations;
import application.model.ApplicationSettings;
import application.model.Experiment;
import application.model.neuralnetwork.backpropagation.NeuralNetwork;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import application.ui.ApplicationStateBar;
import application.ui.Dialogs;
import application.ui.MainUI;
import java.io.File;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * Scene for simulation creation.
 *
 * @author Dizzy
 */
public class CreateExperimentScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private Label fileNNLabel = null;
    private Button selectNNButton = null;
    private File nnFile = null;
    private NeuralNetwork neuralNetwork = null;
    private VBox content = null;
    private HBox optionRow = null;
    GridPane outputOptionsGrid = null;
    GridPane inputOptionsGrid = null;
    private ArrayList<TextField[]> inputS1List = null;
    private ArrayList<TextField[]> inputS2List = null;
    private ArrayList<TextField[]> inputS3List = null;
    private ArrayList<TextField[]> inputS4List = null;
    private ArrayList<ComboBox> outputList = null;
    private ScrollPane scrollPane = null;
    private ScrollBar accelerationScroll = null;
    private Text accelerationValue = null;
    private ComboBox soundsList = null;
    private TextField loopField = null;

    /**
     * Create scene.
     *
     */
    public CreateExperimentScene() {
        scrollPane = new ScrollPane();
        content = new VBox();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Create experiment");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label text = new Label("To create a experiment set its parameters and resources. After selecting a neural network can be defined speed rules. This approach does not allow you to combine different outputs (i.e. more active engine speed rules at the same time), but each input and output of the neural network can be set various parameters. Engine speed is measured in units of degree per second from the range 0 - "+ApplicationSettings.maxEV3Speed+" d/s. It is necessary to set the time in milliseconds for one loop, during the loop is activated neural network and instructions for EV3 device.");
        text.setWrapText(true);
        content.getChildren().add(text);

        GridPane settingGrid = new GridPane();
        settingGrid.setHgap(20);
        settingGrid.setVgap(10);
        selectNNButton = new Button("Select neural network file...");
        settingGrid.add(selectNNButton, 0, 0);
        fileNNLabel = new Label("No file selected");
        settingGrid.add(fileNNLabel, 1, 0);

        Label soundsLabel = new Label("Sounds option:");
        soundsList = new ComboBox(FXCollections.observableArrayList("Mute", "Unmute"));
        settingGrid.add(soundsLabel, 0, 1);
        settingGrid.add(soundsList, 2, 1);

        Label accelerationLabel = new Label("Acceleration (degrees/s/s):");
        accelerationScroll = new ScrollBar();
        accelerationScroll.setMin(0);
        accelerationScroll.setMax(ApplicationSettings.maxEV3Speed);
        accelerationScroll.setValue(400);
        accelerationValue = new Text(String.valueOf(Math.round(accelerationScroll.getValue())));
        settingGrid.add(accelerationLabel, 0, 2);
        settingGrid.add(accelerationValue, 1, 2);
        settingGrid.add(accelerationScroll, 2, 2);

        Label loopLabel = new Label("Time of loop (ms):");
        loopField = new TextField("1000");
        settingGrid.add(loopLabel, 0, 3);
        settingGrid.add(loopField, 1, 3);

        content.getChildren().add(settingGrid);

        loadActual();

        Button saveButton = new Button("Save settings...");
        content.getChildren().add(saveButton);

        saveButton.setOnAction((ActionEvent e) -> {
            validate();
        });

        root.setTop(ApplicationMenu.getInstance().getMenu());
        root.setCenter(scrollPane);
        root.setBottom(ApplicationStateBar.getInstance().getStateBar());
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);

        addListeners();
    }

    private void addListeners() {
        loopField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                loopField.setText(newValue);
            } else {
                loopField.setText(oldValue);
            }
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
                generateOptionsFields();
            }
        });

        accelerationScroll.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            ApplicationSettings.connectedEV3.setMotorAcceleration(newValue.intValue());
            accelerationValue.setText(String.valueOf(Math.round(newValue.intValue())));
        });

        soundsList.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if ("Mute".equals(newValue)) {
                    ApplicationSettings.connectedEV3.muteSound();
                }
                if ("Unmute".equals(newValue)) {
                    ApplicationSettings.connectedEV3.unmuteSound();
                }
            }
        });
    }

    /**
     * Generate output options for each motor.
     *
     */
    private void generateOptionsFields() {
        int TEXTAREA_PREF_SIZE = 30;
        clear();
        inputS1List = new ArrayList<>();
        inputS2List = new ArrayList<>();
        inputS3List = new ArrayList<>();
        inputS4List = new ArrayList<>();
        outputList = new ArrayList<>();

        optionRow = new HBox();
        optionRow.setSpacing(40);

        inputOptionsGrid = new GridPane();
        inputOptionsGrid.setHgap(20);
        inputOptionsGrid.setVgap(10);
        inputOptionsGrid.add(new Label("NN input:"), 0, 0);
        inputOptionsGrid.add(new Label("S1 Min:"), 1, 0);
        inputOptionsGrid.add(new Label("S1 Max:"), 2, 0);
        inputOptionsGrid.add(new Label("S2 Min:"), 3, 0);
        inputOptionsGrid.add(new Label("S2 Max:"), 4, 0);
        inputOptionsGrid.add(new Label("S3 Min:"), 5, 0);
        inputOptionsGrid.add(new Label("S3 Max:"), 6, 0);
        inputOptionsGrid.add(new Label("S4 Min:"), 7, 0);
        inputOptionsGrid.add(new Label("S4 Max:"), 8, 0);

        for (int i = 0; i < neuralNetwork.getInputLayer().size(); i++) {
            Label label = new Label("Input #" + i);
            inputOptionsGrid.add(label, 0, i + 1);

            TextField fieldS1Min = new TextField("0");
            fieldS1Min.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS1Min, 1, i + 1);
            TextField fieldS1Max = new TextField("100");
            fieldS1Max.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS1Max, 2, i + 1);
            inputS1List.add(new TextField[]{fieldS1Min, fieldS1Max});

            TextField fieldS2Min = new TextField("0");
            fieldS2Min.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS2Min, 3, i + 1);
            TextField fieldS2Max = new TextField("100");
            fieldS2Max.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS2Max, 4, i + 1);
            inputS2List.add(new TextField[]{fieldS2Min, fieldS2Max});

            TextField fieldS3Min = new TextField("0");
            fieldS3Min.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS3Min, 5, i + 1);
            TextField fieldS3Max = new TextField("100");
            fieldS3Max.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS3Max, 6, i + 1);
            inputS3List.add(new TextField[]{fieldS3Min, fieldS3Max});

            TextField fieldS4Min = new TextField("0");
            fieldS4Min.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS4Min, 7, i + 1);
            TextField fieldS4Max = new TextField("100");
            fieldS4Max.setPrefWidth(TEXTAREA_PREF_SIZE);
            inputOptionsGrid.add(fieldS4Max, 8, i + 1);
            inputS4List.add(new TextField[]{fieldS4Min, fieldS4Max});
        }
        optionRow.getChildren().add(inputOptionsGrid);

        outputOptionsGrid = new GridPane();
        outputOptionsGrid.setHgap(20);
        outputOptionsGrid.setVgap(10);
        outputOptionsGrid.add(new Label("NN output:"), 0, 0);
        outputOptionsGrid.add(new Label("Direction:"), 1, 0);

        for (int i = 0; i < neuralNetwork.getOutputLayer().size(); i++) {
            Label label = new Label("Output #" + i);
            outputOptionsGrid.add(label, 0, i + 1);

            ComboBox comboBox = new ComboBox(FXCollections.observableArrayList("Forward A", "Backward A", "Forward B", "Backward B"));
            comboBox.setValue("Forward A");
            outputOptionsGrid.add(comboBox, 1, i + 1);

            outputList.add(comboBox);

        }
        optionRow.getChildren().add(outputOptionsGrid);

        content.getChildren().add(optionRow);
    }

    private void clear() {
        if (outputOptionsGrid != null) {
            outputOptionsGrid.getChildren().clear();
        }
        if (inputOptionsGrid != null) {
            inputOptionsGrid.getChildren().clear();
        }
        if (outputList != null) {
            outputList.clear();
        }
        if (inputS1List != null) {
            inputS1List.clear();
        }
        if (inputS2List != null) {
            inputS2List.clear();
        }
        if (inputS3List != null) {
            inputS3List.clear();
        }
        if (inputS4List != null) {
            inputS4List.clear();
        }
    }

    private void loadActual() {
        accelerationScroll.setValue(ApplicationSettings.connectedEV3.getMotorAcceleration());
        accelerationValue.setText(String.valueOf(ApplicationSettings.connectedEV3.getMotorAcceleration()));
        if (ApplicationSettings.connectedEV3.isMuteSound()) {
            soundsList.setValue("Mute");
        } else {
            soundsList.setValue("Unmute");
        }
    }

    private void validate() {
        Boolean valid = true;
        String errorMsg = "";
        int num = 0;
        int acceleration = 0;
        int loop = 0;

        try {
            for (TextField item[] : inputS1List) {
                for (int i = 0; i < item.length; i++) {
                    num = Integer.parseInt(item[i].getText());
                    if (!(num >= 0 && num <= 100)) {
                        errorMsg = "It is necessary to select the distance value from 0 to 100!\n";
                        valid = false;
                    }
                }
            }
            for (TextField item[] : inputS2List) {
                for (int i = 0; i < item.length; i++) {
                    num = Integer.parseInt(item[i].getText());
                    if (!(num >= 0 && num <= 100)) {
                        errorMsg = "It is necessary to select the distance value from 0 to 100!\n";
                        valid = false;
                    }
                }
            }
            for (TextField item[] : inputS3List) {
                for (int i = 0; i < item.length; i++) {
                    num = Integer.parseInt(item[i].getText());
                    if (!(num >= 0 && num <= 100)) {
                        errorMsg = "It is necessary to select the distance value from 0 to 100!\n";
                        valid = false;
                    }
                }
            }
           for (TextField item[] : inputS4List) {
                for (int i = 0; i < item.length; i++) {
                    num = Integer.parseInt(item[i].getText());
                    if (!(num >= 0 && num <= 100)) {
                        errorMsg = "It is necessary to select the distance value from 0 to 100!\n";
                        valid = false;
                    }
                }
            }
        } catch (NumberFormatException e) {
            errorMsg += "Speed and distance range must be an integer!\n";
            valid = false;
        }

        try {
            acceleration = Integer.parseInt(String.valueOf(Math.round(accelerationScroll.getValue())));
            loop = Integer.parseInt(loopField.getText());
        } catch (NumberFormatException e) {
            errorMsg += "Acceleration and time of loop must be an integer!\n";
            valid = false;
        }

        if (valid) {
            Experiment exp = new Experiment();
            exp.setNNFilePath(nnFile.getAbsolutePath());
            exp.setAcceleration(acceleration);
            exp.setTick(loop);
            exp.convertDataForSensorS1(inputS1List);
            exp.convertDataForSensorS2(inputS2List);
            exp.convertDataForSensorS3(inputS3List);
            exp.convertDataForSensorS4(inputS4List);
            exp.convertDataForMotors(outputList);
            exp.setMuteSounds(ApplicationSettings.connectedEV3.isMuteSound());

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save file");
            fileChooser.setInitialDirectory(IOOperations.getExpDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Txt doc (*.txt)", "*.txt"));
            fileChooser.setInitialFileName("experiment.txt");
            File savedFile = fileChooser.showSaveDialog(MainUI.getInstance().getStage());

            if (savedFile != null) {
                IOOperations.saveExperiment(savedFile, exp);
                exp.setExperimentFile(savedFile);
                ApplicationSettings.loadedExperiment = exp;
                ApplicationMenu.getInstance().enableExpSettings();
                MainUI.getInstance().getStage().setScene(new ExperimentSettingsScene().getScene());
            }
        } else {
            Dialogs.errorDialog("Invalid data", "Please pay attention to filling the settings.", errorMsg);
        }
    }
}
