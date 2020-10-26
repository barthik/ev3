package application.ui.scene;

import application.logic.IOOperations;
import static application.logic.IOOperations.saveTSFile;
import application.model.ApplicationSettings;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import application.ui.ApplicationStateBar;
import application.ui.Dialogs;
import application.ui.MainUI;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * Scene for generationg TS
 *
 * @author Dizzy
 */
public class GenerateTSScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private TextField inputField = null;
    private TextField outputField = null;
    private TextField reduceField = null;
    private CheckBox reduceCheckBox = null;
    private CheckBox excludeCheckBox = null;
    private Text patternsText = null;
    private Label progressText = null;
    private long loopDuration = 0;
    private Button generateButton = null;

    /**
     * Create scene.
     *
     */
    public GenerateTSScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Generate training set");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        Label text = new Label("Please select the settings for the training set. The generated file will contain the complete or reduced training set. If you want to devide the measurement sensor area into multiple sections (one sensor will be represented by multiple input neurons), use the option for a reduced training set (you can also exclude the vector of zeros). In the file, you can then edit the output values.");
        Label textWarn = new Label("Generating complete training set with a large number of values may take a while!");
        text.setWrapText(true);
        textWarn.setWrapText(true);
        content.getChildren().addAll(text, textWarn);

        GridPane settingGrid = new GridPane();
        settingGrid.setHgap(20);
        settingGrid.setVgap(10);
        Label inputLabel = new Label("Number of inputs:");
        settingGrid.add(inputLabel, 0, 0);
        inputField = new TextField("0");
        settingGrid.add(inputField, 1, 0);
        Label outputLabel = new Label("Number of outputs:");
        settingGrid.add(outputLabel, 0, 1);
        outputField = new TextField("4");
        settingGrid.add(outputField, 1, 1);
        Label patternsLabel = new Label("Number of patterns:");
        settingGrid.add(patternsLabel, 0, 2);
        patternsText = new Text("---");
        settingGrid.add(patternsText, 1, 2);
        reduceCheckBox = new CheckBox("Create a reduced set");
        reduceCheckBox.setSelected(false);
        settingGrid.add(reduceCheckBox, 0, 4);
        Label groupLabel = new Label("Grouping input columns by:");
        settingGrid.add(groupLabel, 0, 5);
        reduceField = new TextField("1");
        settingGrid.add(reduceField, 1, 5);
        reduceField.setDisable(true);
        excludeCheckBox = new CheckBox("Exclude zero group in columns");
        excludeCheckBox.setSelected(false);
        settingGrid.add(excludeCheckBox, 0, 6);
        excludeCheckBox.setDisable(true);

        generateButton = new Button("Generate file...");
        settingGrid.add(generateButton, 0, 8);

        content.getChildren().add(settingGrid);

        progressText = new Label();
        progressText.setWrapText(true);
        progressText.setVisible(false);
        content.getChildren().add(progressText);

        inputField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            computeNumberOfPatterns();
        });

        reduceField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            computeNumberOfPatterns();
        });

        generateButton.setOnAction((ActionEvent e) -> {
            validate();
        });

        reduceCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (reduceCheckBox.isSelected()) {
                reduceField.setDisable(false);
                excludeCheckBox.setDisable(false);
            } else {
                reduceField.setDisable(true);
                excludeCheckBox.setDisable(true);
            }
            computeNumberOfPatterns();
        });

        excludeCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            computeNumberOfPatterns();
        });

        root.setTop(ApplicationMenu.getInstance().getMenu());
        root.setCenter(content);
        root.setBottom(ApplicationStateBar.getInstance().getStateBar());
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);
    }

    private void computeNumberOfPatterns() {
        int inputs = 0;
        int patternsCount = 0;
        try {
            inputs = Integer.parseInt(inputField.getText());
            if (reduceCheckBox.isSelected()) {
                int group = Integer.parseInt(reduceField.getText());
                if (group < 1) {
                    patternsText.setText("Can not be devided!");
                } else {
                    if (inputs % group != 0) {
                        patternsText.setText("Can not be devided!");
                    } else {
                        if (excludeCheckBox.isSelected()) {
                            patternsCount = (int) Math.pow(group, inputs / group);
                            if (inputs == group) {
                                patternsCount = inputs;
                            } 
                            if (group == 1) {
                                patternsCount = 1;
                            } 
                            patternsText.setText(String.valueOf(patternsCount));
                        } else {
                            patternsText.setText(String.valueOf((int) Math.pow(group + 1, inputs / group)));
                        }
                    }
                }
            } else {
                patternsText.setText(String.valueOf((int) Math.pow(2, inputs)));
            }
        } catch (NumberFormatException e) {
            patternsText.setText("---");
        }
    }

    /**
     * Validate input data.
     */
    private void validate() {
        Boolean valid = true;
        String errorMsg = "";
        int inputs = 0;
        int outputs = 0;
        int group = 0;

        if ("".equals(inputField.getText()) || "".equals(outputField.getText())) {
            errorMsg += "Set number of inputs and outputs!\n";
            valid = false;
        }

        try {
            inputs = Integer.parseInt(inputField.getText());
        } catch (NumberFormatException e) {
            errorMsg += "Specified number of inputs value is not a number!\n";
            valid = false;
        }

        try {
            outputs = Integer.parseInt(outputField.getText());
        } catch (NumberFormatException e) {
            errorMsg += "Specified number of outputs value is not a number!\n";
            valid = false;
        }

        if (inputs < 0 || outputs < 0) {
            errorMsg += "Choose a value of inputs and outputs greater than zero!\n";
            valid = false;
        }

        if (reduceCheckBox.isSelected()) {
            try {
                group = Integer.parseInt(reduceField.getText());
            } catch (NumberFormatException e) {
                errorMsg += "Specified number of grouping columns value is not a number!\n";
                valid = false;
            }
            if (group < 1 || group > inputs) {
                errorMsg += "Choose a value of grouping columns greater than one and less than number of inputs!\n";
                valid = false;
            } else {
                if (inputs % group != 0) {
                    errorMsg += "Inputs can not be evenly devided into " + group + " groups!\n";
                    valid = false;
                }
            }
        }

        if (valid) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save file");
            fileChooser.setInitialDirectory(IOOperations.getTSDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text doc(*.txt)", "*.txt"));
            fileChooser.setInitialFileName("training-set.txt");
            File savedFile = fileChooser.showSaveDialog(MainUI.getInstance().getStage());

            if (savedFile != null) {
                if (reduceCheckBox.isSelected()) {
                    runGenerating(inputs, outputs, savedFile, group, excludeCheckBox.isSelected());
                } else {
                    runGenerating(inputs, outputs, savedFile, 1, false);
                }
            }
        } else {
            Dialogs.errorDialog("Invalid data", "Please pay attention to filling the settings.", errorMsg);
        }
    }

    private void runGenerating(int inputs, int outputs, File savedFile, int group, boolean exclude) {
        generateButton.setDisable(true);
        progressText.setVisible(true);
        progressText.setText("The file is generated, please wait...");

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {

                long startLoop = System.currentTimeMillis();
                IOOperations.generateTSFile(inputs, outputs, savedFile, group, exclude);
                long endLoop = System.currentTimeMillis();

                loopDuration = endLoop - startLoop;

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                generateButton.setDisable(false);
                progressText.setText("The training set was created. The file generation took " + IOOperations.convertTime(Math.round(loopDuration / 1000)) + ".");

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
