package application.ui.scene;

import application.logic.IOOperations;
import static application.logic.IOOperations.saveTSFile;
import application.model.ApplicationSettings;
import application.model.neuralnetwork.ART;
import application.model.neuralnetwork.TrainingSet;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * Scene for filtering TS
 *
 * @author Dizzy
 */
public class FilterTSScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private TextField vigilanceField = null;
    private Label fileLabel = null;
    private RadioButton onesCheckBox = null;
    private RadioButton zerosCheckBox = null;
    private Button filterButton = null;
    private Button selectButton = null;
    private File openedFile = null;
    private ProgressBar updProg = null;
    private int[][] tsArray = null;
    private Label progressText = null;
    private long loopDuration = 0;

    /**
     * Create scene.
     *
     */
    public FilterTSScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Filter training set");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label text = new Label("Select the file with the training set. The training set is filtered using the Adaptive Resonance Theory (ART neural network). The degree of similarity required for patterns to be assigned to the same cluster unit is controlled by a user-defined gain control, known as the vigilance parameter (the higher the vigilance is raised, the less dependent the clusters become on the order of input).");
        Label textWarn = new Label("REMEMBER! Filtering a large amounts of data takes a long time.");
        textWarn.setWrapText(true);
        text.setWrapText(true);
        content.getChildren().addAll(text, textWarn);

        GridPane settingGrid = new GridPane();
        settingGrid.setHgap(20);
        settingGrid.setVgap(10);

        selectButton = new Button("Select training set file...");
        settingGrid.add(selectButton, 0, 0);
        fileLabel = new Label("No file selected");
        settingGrid.add(fileLabel, 1, 0);

        Label vigilanceLabel = new Label("Vigilance:");
        settingGrid.add(vigilanceLabel, 0, 1);
        vigilanceField = new TextField("0.7");
        vigilanceField.setDisable(true);
        settingGrid.add(vigilanceField, 1, 1);

        Label bestPatternLabel = new Label("Set parametr of the best pattern:");
        settingGrid.add(bestPatternLabel, 0, 3);
        ToggleGroup group = new ToggleGroup();
        onesCheckBox = new RadioButton("The most of ones");
        onesCheckBox.setToggleGroup(group);
        onesCheckBox.setSelected(true);
        onesCheckBox.setDisable(true);
        settingGrid.add(onesCheckBox, 1, 3);
        zerosCheckBox = new RadioButton("The most of zeros");
        zerosCheckBox.setToggleGroup(group);
        zerosCheckBox.setDisable(true);
        settingGrid.add(zerosCheckBox, 2, 3);

        filterButton = new Button("Filter");
        filterButton.setDisable(true);
        settingGrid.add(filterButton, 0, 5);

        content.getChildren().add(settingGrid);

        updProg = new ProgressBar();
        updProg.setPrefSize(750, 25);
        updProg.setMinHeight(25);
        updProg.setVisible(false);
        content.getChildren().add(updProg);

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
        selectButton.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file");
            fileChooser.setInitialDirectory(IOOperations.getTSDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text doc(*.txt)", "*.txt"));
            openedFile = fileChooser.showOpenDialog(MainUI.getInstance().getStage());

            if (openedFile != null) {
                fileLabel.setText(openedFile.getName());

                vigilanceField.setDisable(false);
                onesCheckBox.setDisable(false);
                zerosCheckBox.setDisable(false);
                filterButton.setDisable(false);
            }
        });

        vigilanceField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue.matches("-?([0-9]+(\\.?[0-9]*)?)?")) {
                vigilanceField.setText(newValue);
            } else {
                vigilanceField.setText(oldValue);
            }
        });

        filterButton.setOnAction((ActionEvent e) -> {
            validate();
        });
    }

    /**
     * Validate input data.
     */
    private void validate() {
        Boolean valid = true;
        String errorMsg = "";
        double vigilance = 0;
        boolean bestIsOne = false;

        try {
            vigilance = Double.parseDouble(vigilanceField.getText());
        } catch (NumberFormatException e) {
            errorMsg += "Specified value of the vigilance parametr!\n";
            valid = false;
        }

        if (vigilance > 1 || vigilance < 0) {
            errorMsg += "Parameter vigilance must be in the range from 0 to 1!\n";
            valid = false;
        }

        if (onesCheckBox.isSelected()) {
            bestIsOne = true;
        }

        if (zerosCheckBox.isSelected()) {
            bestIsOne = false;
        }

        if (valid) {
            if (openedFile != null) {
                startFiltering(openedFile, vigilance, false, bestIsOne);
            } else {
                Dialogs.errorDialog("Invalid data", "Please pay attention to filling the settings.", "Specify the file of the training set!");
            }
        } else {
            Dialogs.errorDialog("Invalid data", "Please pay attention to filling the settings.", errorMsg);
        }
    }

    private void startFiltering(File openedFile, double vigilance, boolean entireSet, boolean bestIsOne) {
        TrainingSet ts = IOOperations.loadTS(openedFile);
        tsArray = ts.getEntireTS();
        
        int inputs = ts.getInputs()[0].length;
        int outputs = ts.getExpectedOutputs()[0].length;
        int patterns = ts.getInputs().length;

        updProg.setVisible(true);
        progressText.setVisible(true);
        filterButton.setDisable(true);

        ts = null; //free memory

        ART artNN = new ART(tsArray, vigilance, entireSet, bestIsOne, inputs);

        tsArray = null; //free memory
        progressText.setText("Do not close this scene! Runs filtering training set...");

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {
                artNN.shufflePatterns();

                long startLoop = System.currentTimeMillis();
                for (int i = 0; i < patterns; i++) {
                    artNN.runForVector(i);
                    updateProgress(i+1, patterns);
                }
                long endLoop = System.currentTimeMillis();

                tsArray = artNN.getFiteredTS();

                saveTSFile(tsArray, openedFile, inputs, outputs);
                loopDuration = endLoop - startLoop;

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                filterButton.setDisable(false);
                progressText.setText("The filtering took " + IOOperations.convertTime(Math.round(loopDuration/1000)) + " and training set was divided into " + artNN.getNumOfClusters() + " clusters. The file is saved in its original location...");

            }
        };

        updProg.progressProperty().bind(task.progressProperty());

        ApplicationSettings.tempService = new Service() {
            @Override
            protected Task createTask() {
                return task;
            }
        };

        ApplicationSettings.tempService.start();
    }
}
