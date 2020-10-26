package application.ui;

import application.logic.EV3Operations;
import application.model.ApplicationSettings;
import application.ui.scene.InitScene;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main class of the application
 *
 * @author Dizzy
 */
public class MainUI extends Application {

    private static MainUI instance;
    private Stage stage;

    public MainUI() {
        instance = this;
    }

    public static MainUI getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("EV3 Neural Network Control");
        stage.getIcons().add(new Image(ApplicationSettings.applicationIconPath));
        ApplicationMenu.getInstance().disableDeviceOperation();
        ApplicationMenu.getInstance().disableExpSettings();
        stage.setOnCloseRequest(event -> {
            event.consume();
            closeProgram();
        });

        primaryStage.setScene(new InitScene().getScene());
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Close program properly with disconection of device.
     */
    private void closeProgram() {
        if (ApplicationSettings.experimentStage != null) {
            ApplicationSettings.experimentStage.close();
        }
        if (ApplicationSettings.aboutStage != null) {
            ApplicationSettings.aboutStage.close();
        }
        if(ApplicationSettings.experimentTimeline != null) {
            ApplicationSettings.experimentTimeline.stop();
        }
        if(ApplicationSettings.tempService != null) {
            ApplicationSettings.tempService.cancel();
        }
        EV3Operations.disconnectProcess();
        stage.close();
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
