package application.ui;

import application.logic.EV3Operations;
import application.model.ApplicationSettings;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * Statebar of application.
 *
 * @author Dizzy
 */
public class ApplicationStateBar {

    private static ApplicationStateBar instance;
    private HBox stateBar = null;

    private ApplicationStateBar() {
        stateBar = new HBox();
        stateBar.setPadding(new Insets(5, 20, 5, 20));
        stateBar.setSpacing(10);
        stateBar.setAlignment(Pos.CENTER_LEFT);
        Text text = new Text("Idle...");
        stateBar.getChildren().add(text);
    }

    public static ApplicationStateBar getInstance() {
        if (instance == null) {
            instance = new ApplicationStateBar();
        }
        return instance;
    }

    /**
     * Set stateBar to red status.
     *
     */
    public void setRedStateBar() {
        stateBar.getChildren().clear();
        stateBar.setStyle("-fx-background-color: #b20000;");

        Text text = new Text("Currently no device is connected.");
        text.setStyle("-fx-font-weight: bold;");
        text.setFill(Paint.valueOf("white"));

        Button buttonConnect = new Button("Connect...");
        buttonConnect.setPrefSize(100, 20);
        stateBar.getChildren().addAll(text, buttonConnect);

        buttonConnect.setOnAction((ActionEvent event) -> {
            buttonConnect.setDisable(true);
            String ip = Dialogs.connectDialog();
            if (ip != null) {
                EV3Operations.connectionProcess(ip);
                if (ApplicationSettings.connectedEV3 != null) {
                    if (ApplicationSettings.connectedEV3.isConnected()) {
                        setGreenStateBar();
                    } else {
                        buttonConnect.setDisable(false);
                    }
                } else {
                    buttonConnect.setDisable(false);
                }
            } else {
                buttonConnect.setDisable(false);
            }
        });
    }

    /**
     * Set stateBar to gren status.
     *
     */
    public void setGreenStateBar() {
        stateBar.getChildren().clear();
        stateBar.setStyle("-fx-background-color: #00b200;");

        Text text = new Text("Device " + ApplicationSettings.connectedEV3.getEv3().getName() + " is connected.");
        text.setStyle("-fx-font-weight: bold;");
        text.setFill(Paint.valueOf("white"));

        Button buttonConnect = new Button("Disconnect...");
        buttonConnect.setPrefSize(100, 20);
        stateBar.getChildren().addAll(text, buttonConnect);

        buttonConnect.setOnAction((ActionEvent event) -> {
            buttonConnect.setDisable(true);
            ButtonType option = Dialogs.confirmDialog("Disconnect device", "The EV3 device " + ApplicationSettings.connectedEV3.getEv3().getName() + " will be disconnected from the application.", "Unsaved data simulation and settings will be lost. If you are sure about your choice, confirm.");
            if (option == ButtonType.OK) {
                EV3Operations.disconnectProcess();
                setRedStateBar();
            } else {
                buttonConnect.setDisable(false);
            }
        });
    }

    /**
     * Return statebar.
     *
     * @return HBox statebar
     */
    public HBox getStateBar() {
        if (ApplicationSettings.connectedEV3 != null) {
            if (ApplicationSettings.connectedEV3.isConnected()) {
                setGreenStateBar();
            } else {
                setRedStateBar();
            }
        } else {
            setRedStateBar();
        }
        return stateBar;
    }

    /**
     * Update statebar to red or green state.
     *
     */
    public void updateStateBar() {
        if (ApplicationSettings.connectedEV3 != null) {
            if (ApplicationSettings.connectedEV3.isConnected()) {
                setGreenStateBar();
            } else {
                setRedStateBar();
            }
        } else {
            setRedStateBar();
        }
    }

}
