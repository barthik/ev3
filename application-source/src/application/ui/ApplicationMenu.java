package application.ui;

import application.logic.IOOperations;
import application.model.ApplicationSettings;
import application.ui.scene.*;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Main menu of the application.
 *
 * @author Dizzy
 */
public class ApplicationMenu {

    private static ApplicationMenu instance;
    private MenuBar menuBar = null;

    private Menu deviceMenu = null;
    private MenuItem connectedDevMenuItem = null;

    private Menu experimentMenu = null;
    private MenuItem createExpMenuItem = null;
    private MenuItem loadExpMenuItem = null;
    private MenuItem settingsExpMenuItem = null;
    private MenuItem runExpMenuItem = null;

    private Menu resoursesMenu = null;
    private Menu neuralNetrworkMenu = null;
    private MenuItem createNNMenuItem = null;
    private MenuItem trainNNMenuItem = null;

    private Menu trainingSetMenu = null;
    private MenuItem generateTSMenuItem = null;
    private MenuItem filterTSMenuItem = null;
    
    private Menu aboutMenu = null;
    private MenuItem aboutMenuItem = null;

    private ApplicationMenu() {
        menuBar = new MenuBar();

        deviceMenu = new Menu("Device");
        connectedDevMenuItem = new MenuItem("Connected device");
        deviceMenu.getItems().addAll(connectedDevMenuItem);

        experimentMenu = new Menu("Experiment");
        createExpMenuItem = new MenuItem("Create");
        loadExpMenuItem = new MenuItem("Load...");
        settingsExpMenuItem = new MenuItem("Settings");
        runExpMenuItem = new MenuItem("Run");
        experimentMenu.getItems().addAll(createExpMenuItem, loadExpMenuItem, settingsExpMenuItem, new SeparatorMenuItem(), runExpMenuItem);

        resoursesMenu = new Menu("Resources");
        neuralNetrworkMenu = new Menu("Neural network");
        createNNMenuItem = new MenuItem("Create");
        trainNNMenuItem = new MenuItem("Train");
        neuralNetrworkMenu.getItems().addAll(createNNMenuItem, trainNNMenuItem);
        trainingSetMenu = new Menu("Training set");
        generateTSMenuItem = new MenuItem("Generate");
        filterTSMenuItem = new MenuItem("Filter");
        trainingSetMenu.getItems().addAll(generateTSMenuItem, filterTSMenuItem);
        resoursesMenu.getItems().addAll(neuralNetrworkMenu, trainingSetMenu);
        
        aboutMenu = new Menu("About");
        aboutMenuItem = new MenuItem("View about application");
        aboutMenu.getItems().addAll(aboutMenuItem);

        menuBar.getMenus().addAll(deviceMenu, experimentMenu, resoursesMenu, aboutMenu);


        aboutMenuItem.setOnAction((ActionEvent e) -> {
            ApplicationSettings.aboutStage = new Stage();
            ApplicationSettings.aboutStage.setTitle("About application EV3 Neural Control");
            ApplicationSettings.aboutStage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));
            ApplicationSettings.aboutStage.setScene(new AboutScene().getScene());
            ApplicationSettings.aboutStage.setX(MainUI.getInstance().getStage().getX() + 50);
            ApplicationSettings.aboutStage.setY(MainUI.getInstance().getStage().getY() + 50);
            ApplicationSettings.aboutStage.show();
        });
        
        connectedDevMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new DeviceScene().getScene());
        });
        generateTSMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new GenerateTSScene().getScene());
        });
        filterTSMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new FilterTSScene().getScene());
        });
        createNNMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new GenerateNNScene().getScene());
        });
        trainNNMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new TrainNNScene().getScene());
        });
        createExpMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new CreateExperimentScene().getScene());
        });
        settingsExpMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            MainUI.getInstance().getStage().setScene(new ExperimentSettingsScene().getScene());
        });
        loadExpMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open file");
            fileChooser.setInitialDirectory(IOOperations.getExpDir());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Txt doc(*.txt)", "*.txt"));
            File simFile = fileChooser.showOpenDialog(MainUI.getInstance().getStage());

            if (simFile != null) {
                ApplicationSettings.loadedExperiment = IOOperations.loadExperimentFile(simFile);
                if (ApplicationSettings.loadedExperiment != null) {
                    ApplicationMenu.getInstance().enableExpSettings();
                    MainUI.getInstance().getStage().setScene(new ExperimentSettingsScene().getScene());
                }
            }
        });
        runExpMenuItem.setOnAction((ActionEvent e) -> {
            closeServices();
            ApplicationSettings.experimentStage = new Stage();
            ApplicationSettings.experimentStage.setTitle("Run Experiment");
            ApplicationSettings.experimentStage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));
            ApplicationSettings.experimentStage.setScene(new RunExperimentScene().getScene());
            ApplicationSettings.experimentStage.setX(MainUI.getInstance().getStage().getX() + 50);
            ApplicationSettings.experimentStage.setY(MainUI.getInstance().getStage().getY() + 50);
            ApplicationSettings.experimentStage.show();
            runExpMenuItem.setDisable(true);
        });
    }

    private void closeServices() {
        if(ApplicationSettings.tempService !=null) {
            ApplicationSettings.tempService.cancel();
            ApplicationSettings.tempService = null;
        }
    }
    
    public static ApplicationMenu getInstance() {
        if (instance == null) {
            instance = new ApplicationMenu();
        }
        return instance;
    }

    public MenuBar getMenu() {
        return menuBar;
    }

    /**
     * Set device menu operation disabled.
     *
     */
    public void disableDeviceOperation() {
        connectedDevMenuItem.setDisable(true);
        createExpMenuItem.setDisable(true);
        loadExpMenuItem.setDisable(true);
    }

    /**
     * Set device menu operation enabled.
     *
     */
    public void enableDeviceOperation() {
        connectedDevMenuItem.setDisable(false);
        createExpMenuItem.setDisable(false);
        loadExpMenuItem.setDisable(false);
    }

    /**
     * Set experiment menu operation enabled.
     *
     */
    public void enableExpSettings() {
        settingsExpMenuItem.setDisable(false);
        runExpMenuItem.setDisable(false);
    }

    /**
     * Set experiment menu operation disabled.
     *
     */
    public void disableExpSettings() {
        settingsExpMenuItem.setDisable(true);
        runExpMenuItem.setDisable(true);
    }

    /**
     * Set run experiment menu enable.
     *
     */
    public void enableExpRun() {
        runExpMenuItem.setDisable(false);
    }

    /**
     * Set run experiment menu disable.
     *
     */
    public void disableExpRun() {
        runExpMenuItem.setDisable(true);
    }
}
