package application.logic;

import application.ui.MainUI;
import application.model.ApplicationSettings;
import application.model.enums.MotorPort;
import application.model.enums.SensorPort;
import application.model.ev3.IRDevice;
import application.ui.ApplicationMenu;
import application.ui.scene.InitScene;

/**
 *
 * @author Dizzy
 */
public class EV3Operations {

    /**
     * Connect EV3 device and opens ports.
     *
     * @param ip IP address.
     */
    public static void connectionProcess(String ip) {
        ApplicationSettings.connectedEV3 = new IRDevice(ip);
        if (ApplicationSettings.connectedEV3 != null) {
            if (ApplicationSettings.connectedEV3.isConnected()) {

                ApplicationSettings.connectedEV3.setLoading();

                ApplicationSettings.connectedEV3.openMotor(MotorPort.A);
                ApplicationSettings.connectedEV3.openMotor(MotorPort.B);

                ApplicationSettings.connectedEV3.openSensor(SensorPort.S1);
                ApplicationSettings.connectedEV3.openSensor(SensorPort.S2);
                ApplicationSettings.connectedEV3.openSensor(SensorPort.S3);
                ApplicationSettings.connectedEV3.openSensor(SensorPort.S4);

                ApplicationSettings.connectedEV3.setReady();

                ApplicationMenu.getInstance().enableDeviceOperation();
            }
        }
    }

    /**
     * Disconnect EV3 device and closes ports.
     *
     */
    public static void disconnectProcess() {
        if (ApplicationSettings.connectedEV3 != null) {
            if (ApplicationSettings.connectedEV3.isConnected()) {
                ApplicationSettings.connectedEV3.setLoading();

                ApplicationSettings.connectedEV3.closeMotor(MotorPort.A);
                ApplicationSettings.connectedEV3.closeMotor(MotorPort.B);

                ApplicationSettings.connectedEV3.closeSensor(SensorPort.S1);
                ApplicationSettings.connectedEV3.closeSensor(SensorPort.S2);
                ApplicationSettings.connectedEV3.closeSensor(SensorPort.S3);
                ApplicationSettings.connectedEV3.closeSensor(SensorPort.S4);

                ApplicationSettings.connectedEV3.setOff();

                ApplicationMenu.getInstance().disableDeviceOperation();
                ApplicationMenu.getInstance().disableExpSettings();
                closeRunningTasks();
                MainUI.getInstance().getStage().setScene(new InitScene().getScene());
            }
        }
    }

    /**
     * Close running tasks properly after disconection of device.
     */
    private static void closeRunningTasks() {
        if (ApplicationSettings.experimentStage != null) {
            ApplicationSettings.experimentStage.close();
        }
        if (ApplicationSettings.experimentTimeline != null) {
            ApplicationSettings.experimentTimeline.stop();
        }
        if (ApplicationSettings.tempService != null) {
            ApplicationSettings.tempService.cancel();
        }
        ApplicationSettings.loadedExperiment = null;
        ApplicationSettings.connectedEV3 = null;
    }
}
