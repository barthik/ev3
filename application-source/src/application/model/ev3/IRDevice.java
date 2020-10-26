package application.model.ev3;

import application.model.enums.SensorPort;
import application.model.enums.MotorPort;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import lejos.hardware.port.Port;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import application.ui.Dialogs;

/**
 * Class working with a robotic device.
 *
 * @author Dizzy
 */
public class IRDevice {

    private RemoteEV3 ev3 = null;

    private IRSensor sensorS1 = null;
    private IRSensor sensorS2 = null;
    private IRSensor sensorS3 = null;
    private IRSensor sensorS4 = null;

    private RMIRegulatedMotor motorA = null;
    private RMIRegulatedMotor motorB = null;
    private RMIRegulatedMotor motorC = null;
    private RMIRegulatedMotor motorD = null;

    private int motorSpeed = 400;
    private int motorAcceleration = 600;

    private boolean muteSound = false;

    /**
     * Connects to EV3.
     *
     * @param ip IP address of the EV3 device.
     */
    public IRDevice(String ip) {
        try {
            ev3 = new RemoteEV3(ip);
            ev3.setDefault();
            setStandBy();
            ev3.getGraphicsLCD().clear();
            ev3.getGraphicsLCD().drawString("CONTROLLED", 0, 40, 0);
            ev3.getGraphicsLCD().drawString("REMOTELY...", 0, 60, 0);
        } catch (RemoteException | MalformedURLException | NotBoundException ex) {
            Dialogs.exceptionDialog("Connection failed", "Connection to the device failed.", "Make sure your device is turned on, Bluetooth is activated on the host computer and EV3 connected via PAN. Finally, check the IP address of the device (default IP address is: 10.0.1.1)", ex);
        }
    }

    public boolean isMuteSound() {
        return muteSound;
    }

    /**
     * Shuts making the sound signals when adjusting device status.
     *
     */
    public void muteSound() {
        this.muteSound = true;
    }

    /**
     * Turns making the sound signals when adjusting device status.
     *
     */
    public void unmuteSound() {
        this.muteSound = false;
    }

    public RemoteEV3 getEv3() {
        return ev3;
    }

    public IRSensor getSensorS1() {
        return sensorS1;
    }

    public IRSensor getSensorS2() {
        return sensorS2;
    }

    public IRSensor getSensorS3() {
        return sensorS3;
    }

    public IRSensor getSensorS4() {
        return sensorS4;
    }

    public RMIRegulatedMotor getMotorA() {
        return motorA;
    }

    public RMIRegulatedMotor getMotorB() {
        return motorB;
    }

    public RMIRegulatedMotor getMotorC() {
        return motorC;
    }

    public RMIRegulatedMotor getMotorD() {
        return motorD;
    }

    public float getSensorS1Data() {
        return calculateSensorData(sensorS1);
    }

    public float getSensorS2Data() {
        return calculateSensorData(sensorS2);
    }

    public float getSensorS3Data() {
        return calculateSensorData(sensorS3);
    }

    public float getSensorS4Data() {
        return calculateSensorData(sensorS4);
    }

    public int getMotorSpeed() {
        return motorSpeed;
    }

    public void setMotorSpeed(int motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

    public int getMotorAcceleration() {
        return motorAcceleration;
    }

    public void setMotorAcceleration(int motorAcceleration) {
        this.motorAcceleration = motorAcceleration;
    }

    /**
     * Opens infrared sensor in the port.
     *
     * @param port SensorPort
     */
    public void openSensor(SensorPort port) {
        try {
            if (port == SensorPort.S1) {
                if (sensorS1 == null) {
                    Port p = ev3.getPort(port.toString());
                    sensorS1 = new IRSensor(p);
                }
            }
            if (port == SensorPort.S2) {
                if (sensorS2 == null) {
                    Port p = ev3.getPort(port.toString());
                    sensorS2 = new IRSensor(p);
                }
            }
            if (port == SensorPort.S3) {
                if (sensorS3 == null) {
                    Port p = ev3.getPort(port.toString());
                    sensorS3 = new IRSensor(p);
                }
            }
            if (port == SensorPort.S4) {
                if (sensorS4 == null) {
                    Port p = ev3.getPort(port.toString());
                    sensorS4 = new IRSensor(p);
                }
            }
        } catch (Exception ex) {
            setError();
            Dialogs.exceptionDialog("Unable to open the port", "Unable to open the port " + port.toString() + " for infrared sensor.", "Check that the port " + port.toString() + " is connected to an infrared sensor. You may need to restart the EV3 device.", ex);
        }
    }

    /**
     * Opens a big engine in the port.
     *
     * @param port MotorPort
     */
    public void openMotor(MotorPort port) {
        try {
            if (port == MotorPort.A) {
                if (motorA == null) {
                    motorA = ev3.createRegulatedMotor(port.toString(), 'L');
                    motorA.setSpeed(motorSpeed);
                    motorA.setAcceleration(motorAcceleration);
                    motorA.resetTachoCount();
                    motorA.rotateTo(0);
                }
            }
            if (port == MotorPort.B) {
                if (motorB == null) {
                    motorB = ev3.createRegulatedMotor(port.toString(), 'L');
                    motorB.setSpeed(motorSpeed);
                    motorB.setAcceleration(motorAcceleration);
                    motorB.resetTachoCount();
                    motorB.rotateTo(0);
                }
            }
            if (port == MotorPort.C) {
                if (motorC == null) {
                    motorC = ev3.createRegulatedMotor(port.toString(), 'L');
                    motorC.setSpeed(motorSpeed);
                    motorC.setAcceleration(motorAcceleration);
                    motorC.resetTachoCount();
                    motorC.rotateTo(0);
                }
            }
            if (port == MotorPort.D) {
                if (motorD == null) {
                    motorD = ev3.createRegulatedMotor(port.toString(), 'L');
                    motorD.setSpeed(motorSpeed);
                    motorD.setAcceleration(motorAcceleration);
                    motorD.resetTachoCount();
                    motorD.rotateTo(0);
                }
            }

        } catch (Exception ex) {
            setError();
            Dialogs.exceptionDialog("Unable to open the port", "Unable to open the port " + port.toString() + " for large engine.", "Check that the port " + port.toString() + " is connected to a large engine. You may need to restart the EV3 device.", ex);
        }
    }

    /**
     * Closes big engine in the port.
     *
     * @param port MotorPort
     */
    public void closeMotor(MotorPort port) {
        try {
            if (port == MotorPort.A) {
                if (motorA != null) {
                    motorA.close();
                    motorA = null;
                }
            }
            if (port == MotorPort.B) {
                if (motorB != null) {
                    motorB.close();
                    motorB = null;
                }
            }
            if (port == MotorPort.C) {
                if (motorC != null) {
                    motorC.close();
                    motorC = null;
                }
            }
            if (port == MotorPort.D) {
                if (motorD != null) {
                    motorD.close();
                    motorD = null;
                }
            }

        } catch (RemoteException ex) {
            setError();
            Dialogs.exceptionDialog("Unable to close the port", "Unable to close the port " + port.toString() + " for large engine.", "Check that the port " + port.toString() + " is connected to a large engine. You may need to restart the EV3 device.", ex);
        }
    }

    /**
     * Opens large engines in ports A, B.
     */
    public void openMotors() {

        setLoading();
        openMotor(MotorPort.A);
        openMotor(MotorPort.B);
        setStandBy();

    }

    public void moveForward() throws InterruptedException {
        try {

            motorA.forward();
            motorB.forward();
        } catch (RemoteException ex) {
            setError();
            Dialogs.exceptionDialog("Unable to move", "Unable to perform the movement.", "Check that the port A, B is connected to a large engines. You may need to restart the EV3 device.", ex);
        }
    }

    public void moveBackward() throws InterruptedException {
        try {

            motorA.backward();
            motorB.backward();
        } catch (RemoteException ex) {
            setError();
            Dialogs.exceptionDialog("Unable to move", "Unable to perform the movement.", "Check that the port A, B is connected to a large engines. You may need to restart the EV3 device.", ex);
        }
    }

    public void stop() throws InterruptedException {
        try {

            motorA.stop(true);
            motorB.stop(true);
        } catch (RemoteException ex) {
            setError();
            Dialogs.exceptionDialog("Unable to stop", "Unable to perform the stop movement.", "Check that the port A, B is connected to a large engines. You may need to restart the EV3 device.", ex);
        }
    }

    /**
     * Closes large engines in ports A, B.
     */
    public void closeMotors() {

        setLoading();
        closeMotor(MotorPort.A);
        closeMotor(MotorPort.B);
        setStandBy();

    }

    /**
     * Runs infrared sensors ports S1, S2, S3, S4.
     */
    public void openSensors() {

        setLoading();
        openSensor(SensorPort.S1);
        openSensor(SensorPort.S2);
        openSensor(SensorPort.S3);
        openSensor(SensorPort.S4);
        setStandBy();

    }

    /**
     * Infrared sensors correctly closed in ports S1, S2, S3, S4.
     *
     */
    public void closeSensors() {

        setLoading();
        closeSensor(SensorPort.S1);
        closeSensor(SensorPort.S2);
        closeSensor(SensorPort.S3);
        closeSensor(SensorPort.S4);
        setStandBy();
    }

    /**
     * Closes infrared sensor in the port.
     *
     * @param port SensorPort
     */
    public void closeSensor(SensorPort port) {
        if (port == SensorPort.S1) {
            if (sensorS1 != null) {
                sensorS1.close();
                sensorS1 = null;
            }
        }
        if (port == SensorPort.S2) {
            if (sensorS2 != null) {
                sensorS2.close();
                sensorS2 = null;
            }
        }
        if (port == SensorPort.S3) {
            if (sensorS3 != null) {
                sensorS3.close();
                sensorS3 = null;
            }
        }
        if (port == SensorPort.S4) {
            if (sensorS4 != null) {
                sensorS4.close();
                sensorS4 = null;
            }
        }
    }

    /**
     * Converts the sensor data into the distance in centimeters.
     *
     * @param sensor IRSensor
     * @return Distance of the object from the sensor.
     */
    private float calculateSensorData(IRSensor sensor) {
        float ret = 0;
        SampleProvider distance = sensor.getDistanceMode(); // distance mod
        SampleProvider average = new MeanFilter(distance, 5); //filter on x measurements
        float[] sample = new float[average.sampleSize()];
        average.fetchSample(sample, 0);
        if (sample != null) {
            ret = sample[0];
        }
        if (ret > 100) {
            ret = 100;
        }
        return ret;
    }

    /**
     * Returns true if the device is connected.
     *
     * @return boolean
     */
    public boolean isConnected() {
        return ev3 != null;
    }

    /**
     * Returns the status of battery power percentage.
     *
     * @return float
     */
    public float getBattery() {
        return (float) ev3.getPower().getVoltageMilliVolt() * 100 / 9000; //9000 is max
    }

    /**
     * Sets the error status to EV3. The device beeps and LED signal.
     *
     */
    public void setError() {
        if (!muteSound) {
            ev3.getAudio().systemSound(4);
        }
        ev3.getLED().setPattern(2);
    }

    /**
     * Sets the stand by status to EV3. The device beeps and LED signal.
     *
     */
    public void setStandBy() {
        ev3.getLED().setPattern(3);
    }

    /**
     * Sets the ready status to EV3. The device beeps and LED signal.
     *
     */
    public void setReady() {
        if (!muteSound) {
            ev3.getAudio().systemSound(2);
        }
        ev3.getLED().setPattern(1);
    }

    /**
     * Sets the loading status to EV3. The device beeps and LED signal.
     *
     */
    public void setLoading() {
        ev3.getLED().setPattern(6);
    }

    /**
     * Sets the off status to EV3. The device beeps and LED signal.
     *
     */
    public void setOff() {
        if (!muteSound) {
            ev3.getAudio().systemSound(3);
        }
        ev3.getLED().setPattern(0);
    }

}
