package application.ui.scene;

import application.model.ApplicationSettings;
import application.ui.ApplicationMenu;
import application.ui.ApplicationScene;
import application.ui.ApplicationStateBar;
import application.ui.Dialogs;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lejos.remote.ev3.RMIRegulatedMotor;

/**
 * Device scene.
 *
 * @author Dizzy
 */
public class DeviceScene extends ApplicationScene {

    private BorderPane root = new BorderPane();
    private ComboBox soundsList = null;
    private ScrollBar speedScroll = null;
    private ScrollBar accelerationScroll = null;
    private Text speedValue = null;
    private Text accelerationValue = null;
    private Text portS1Text = null;
    private Text portS2Text = null;
    private Text portS3Text = null;
    private Text portS4Text = null;
    private Timeline timeline = null;

    /**
     * Create scene.
     *
     */
    public DeviceScene() {
        VBox content = new VBox();
        content.setPadding(new Insets(25, 25, 25, 25));
        content.setSpacing(10);
        Label sceneTitle = new Label("Device");
        sceneTitle.setFont(Font.font(20));
        content.getChildren().add(sceneTitle);

        HBox deviceRow = new HBox(10);
        Label deviceLabel = new Label("Connected device:");
        deviceLabel.setFont(Font.font(14));
        Text deviceName = new Text(ApplicationSettings.connectedEV3.getEv3().getName());
        deviceName.setFont(Font.font(14));
        deviceRow.getChildren().addAll(deviceLabel, deviceName);
        content.getChildren().add(deviceRow);

        HBox batteryRow = new HBox(10);
        Label batteryLabel = new Label("Estimating battery:");
        batteryLabel.setFont(Font.font(14));
        Text dbatteryValue = new Text(Math.round(ApplicationSettings.connectedEV3.getBattery()) + "%");
        dbatteryValue.setFont(Font.font(14));
        batteryRow.getChildren().addAll(batteryLabel, dbatteryValue);
        content.getChildren().add(batteryRow);

        content.getChildren().add(new Label());

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(20);
        settingsGrid.setVgap(10);
        Label soundsLabel = new Label("Sounds option:");
        soundsList = new ComboBox(FXCollections.observableArrayList("Mute", "Unmute"));
        settingsGrid.add(soundsLabel, 0, 0);
        settingsGrid.add(soundsList, 2, 0);

        settingsGrid.setHgap(20);
        Label speedLabel = new Label("Speed (degrees/s):");
        speedScroll = new ScrollBar();
        speedScroll.setMin(0);
        speedScroll.setMax(ApplicationSettings.maxEV3Speed);
        speedScroll.setValue(400);
        speedValue = new Text(String.valueOf(Math.round(speedScroll.getValue())));
        settingsGrid.add(speedLabel, 0, 1);
        settingsGrid.add(speedValue, 1, 1);
        settingsGrid.add(speedScroll, 2, 1);

        Label accelerationLabel = new Label("Acceleration (degrees/s/s):");
        accelerationScroll = new ScrollBar();
        accelerationScroll.setMin(0);
        accelerationScroll.setMax(ApplicationSettings.maxEV3Speed);
        accelerationScroll.setValue(400);
        accelerationValue = new Text(String.valueOf(Math.round(accelerationScroll.getValue())));
        settingsGrid.add(accelerationLabel, 0, 2);
        settingsGrid.add(accelerationValue, 1, 2);
        settingsGrid.add(accelerationScroll, 2, 2);
        content.getChildren().add(settingsGrid);

        content.getChildren().add(new Label());

        GridPane sensorsGrid = new GridPane();
        sensorsGrid.setHgap(20);
        sensorsGrid.setVgap(10);
        Label portS1Label = new Label("Port S1:");
        sensorsGrid.add(portS1Label, 0, 0);
        portS1Text = new Text("idle");
        sensorsGrid.add(portS1Text, 0, 1);
        Label portS2Label = new Label("Port S2:");
        sensorsGrid.add(portS2Label, 1, 0);
        portS2Text = new Text("idle");
        sensorsGrid.add(portS2Text, 1, 1);
        Label portS3Label = new Label("Port S3:");
        sensorsGrid.add(portS3Label, 2, 0);
        portS3Text = new Text("idle");
        sensorsGrid.add(portS3Text, 2, 1);
        Label portS4Label = new Label("Port S4:");
        sensorsGrid.add(portS4Label, 3, 0);
        portS4Text = new Text("idle");
        sensorsGrid.add(portS4Text, 3, 1);
        content.getChildren().add(sensorsGrid);

        content.getChildren().add(new Label());

        Label textLabel = new Label("To control the device use the keyboard.");
        textLabel.setFont(Font.font(14));

        GridPane keysGrid = new GridPane();
        keysGrid.setHgap(20);
        keysGrid.setVgap(10);
        Label forwardLabel = new Label("Move forward:");
        keysGrid.add(forwardLabel, 0, 0);
        Text forwardKey = new Text("[W, I]");
        keysGrid.add(forwardKey, 1, 0);
        Label backwardLabel = new Label("Move backward:");
        keysGrid.add(backwardLabel, 0, 1);
        Text backwardKey = new Text("[S, K]");
        keysGrid.add(backwardKey, 1, 1);
        Label leftLabel = new Label("Turn left:");
        keysGrid.add(leftLabel, 0, 2);
        Text leftKey = new Text("[A, J]");
        keysGrid.add(leftKey, 1, 2);
        Label rightLabel = new Label("Turn right:");
        keysGrid.add(rightLabel, 0, 3);
        Text rightKey = new Text("[D, L]");
        keysGrid.add(rightKey, 1, 3);
        content.getChildren().addAll(textLabel, keysGrid);

        loadActual();

        root.setTop(ApplicationMenu.getInstance().getMenu());
        root.setCenter(content);
        root.setBottom(ApplicationStateBar.getInstance().getStateBar());
        scene = new Scene(root, ApplicationSettings.WINDOW_WIDTH, ApplicationSettings.WINDOW_HEIGHT);

        addListeners();
        loadSensorData();
    }

    private void loadSensorData() {
        timeline = new Timeline(new KeyFrame(
                Duration.millis(1000), ae -> {
                    if (ApplicationSettings.connectedEV3 == null && timeline != null) {
                        timeline.stop();
                    } else {
                        portS1Text.setText(Math.round(ApplicationSettings.connectedEV3.getSensorS1Data()) + "cm");
                        portS2Text.setText(Math.round(ApplicationSettings.connectedEV3.getSensorS2Data()) + "cm");
                        portS3Text.setText(Math.round(ApplicationSettings.connectedEV3.getSensorS3Data()) + "cm");
                        portS4Text.setText(Math.round(ApplicationSettings.connectedEV3.getSensorS4Data()) + "cm");
                    }
                }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void addListeners() {
        RMIRegulatedMotor motorA = ApplicationSettings.connectedEV3.getMotorA();
        RMIRegulatedMotor motorB = ApplicationSettings.connectedEV3.getMotorB();

        speedScroll.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            ApplicationSettings.connectedEV3.setMotorSpeed(newValue.intValue());
            speedValue.setText(String.valueOf(Math.round(newValue.intValue())));
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

        scene.setOnKeyPressed((KeyEvent event) -> {
            try {
                if (event.getCode() == KeyCode.I || event.getCode() == KeyCode.W) {

                    motorA.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorA.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    motorB.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorB.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    ApplicationSettings.connectedEV3.moveForward();

                }
                if (event.getCode() == KeyCode.K || event.getCode() == KeyCode.S) {

                    motorA.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorA.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    motorB.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorB.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    ApplicationSettings.connectedEV3.moveBackward();

                }
                if (event.getCode() == KeyCode.L || event.getCode() == KeyCode.D) {

                    motorA.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorA.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    motorB.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorB.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    motorA.backward();
                    motorB.forward();

                }
                if (event.getCode() == KeyCode.J || event.getCode() == KeyCode.A) {

                    motorA.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorA.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    motorB.setSpeed(ApplicationSettings.connectedEV3.getMotorSpeed());
                    motorB.setAcceleration(ApplicationSettings.connectedEV3.getMotorAcceleration());
                    motorA.forward();
                    motorB.backward();

                }
            } catch (Exception ex) {
                Dialogs.exceptionDialog("Unable to move", "Unable to perform the movement.", "Check that the port A, B is connected to a large engines. You may need to restart the EV3 device.", ex);
            }
        });
        scene.setOnKeyReleased((KeyEvent event) -> {
            try {
                motorA.stop(true);
                motorB.stop(true);
            } catch (Exception ex) {
                Dialogs.exceptionDialog("Unable to stop", "Unable to perform the stop movement.", "Check that the port A, B is connected to a large engines. You may need to restart the EV3 device.", ex);
            }
        });
    }

    private void loadActual() {
        speedScroll.setValue(ApplicationSettings.connectedEV3.getMotorSpeed());
        speedValue.setText(String.valueOf(Math.round(ApplicationSettings.connectedEV3.getMotorSpeed())));
        accelerationScroll.setValue(ApplicationSettings.connectedEV3.getMotorAcceleration());
        accelerationValue.setText(String.valueOf(ApplicationSettings.connectedEV3.getMotorAcceleration()));
        if (ApplicationSettings.connectedEV3.isMuteSound()) {
            soundsList.setValue("Mute");
        } else {
            soundsList.setValue("Unmute");
        }
    }
}
