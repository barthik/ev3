package application.model;

import application.logic.IOOperations;
import application.model.neuralnetwork.backpropagation.NeuralNetwork;
import java.io.File;
import java.util.ArrayList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Experiment class.
 *
 * @author Dizzy
 */
public class Experiment {

    private int acceleration = 0;
    private String nnFilePath = null;
    private File experimentFile = null;
    private boolean muteSounds = false;
    private ArrayList<Integer> sensorS1DistancesMin = null;
    private ArrayList<Integer> sensorS1DistancesMax = null;
    private ArrayList<Integer> sensorS2DistancesMin = null;
    private ArrayList<Integer> sensorS2DistancesMax = null;
    private ArrayList<Integer> sensorS3DistancesMin = null;
    private ArrayList<Integer> sensorS3DistancesMax = null;
    private ArrayList<Integer> sensorS4DistancesMin = null;
    private ArrayList<Integer> sensorS4DistancesMax = null;
    private ArrayList<String> motorSettings = null;
    private int tick = 0;

    public Experiment() {
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
    }

    public String getNNFilePath() {
        return nnFilePath;
    }

    public void setNNFilePath(String nnFilePath) {
        this.nnFilePath = nnFilePath;
    }

    public boolean isMuteSounds() {
        return muteSounds;
    }

    public void setMuteSounds(boolean muteSounds) {
        this.muteSounds = muteSounds;
    }

    public String getNnFilePath() {
        return nnFilePath;
    }

    public void setNnFilePath(String nnFilePath) {
        this.nnFilePath = nnFilePath;
    }

    public ArrayList<Integer> getSensorS1DistancesMin() {
        return sensorS1DistancesMin;
    }

    public void setSensorS1DistancesMin(ArrayList<Integer> sensorS1DistancesMin) {
        this.sensorS1DistancesMin = sensorS1DistancesMin;
    }

    public ArrayList<Integer> getSensorS1DistancesMax() {
        return sensorS1DistancesMax;
    }

    public void setSensorS1DistancesMax(ArrayList<Integer> sensorS1DistancesMax) {
        this.sensorS1DistancesMax = sensorS1DistancesMax;
    }

    public ArrayList<Integer> getSensorS2DistancesMin() {
        return sensorS2DistancesMin;
    }

    public void setSensorS2DistancesMin(ArrayList<Integer> sensorS2DistancesMin) {
        this.sensorS2DistancesMin = sensorS2DistancesMin;
    }

    public ArrayList<Integer> getSensorS2DistancesMax() {
        return sensorS2DistancesMax;
    }

    public void setSensorS2DistancesMax(ArrayList<Integer> sensorS2DistancesMax) {
        this.sensorS2DistancesMax = sensorS2DistancesMax;
    }

    public ArrayList<Integer> getSensorS3DistancesMin() {
        return sensorS3DistancesMin;
    }

    public void setSensorS3DistancesMin(ArrayList<Integer> sensorS3DistancesMin) {
        this.sensorS3DistancesMin = sensorS3DistancesMin;
    }

    public ArrayList<Integer> getSensorS3DistancesMax() {
        return sensorS3DistancesMax;
    }

    public void setSensorS3DistancesMax(ArrayList<Integer> sensorS3DistancesMax) {
        this.sensorS3DistancesMax = sensorS3DistancesMax;
    }

    public ArrayList<Integer> getSensorS4DistancesMin() {
        return sensorS4DistancesMin;
    }

    public void setSensorS4DistancesMin(ArrayList<Integer> sensorS4DistancesMin) {
        this.sensorS4DistancesMin = sensorS4DistancesMin;
    }

    public ArrayList<Integer> getSensorS4DistancesMax() {
        return sensorS4DistancesMax;
    }

    public void setSensorS4DistancesMax(ArrayList<Integer> sensorS4DistancesMax) {
        this.sensorS4DistancesMax = sensorS4DistancesMax;
    }

    public ArrayList<String> getMotorSettings() {
        return motorSettings;
    }

    public void setMotorSettings(ArrayList<String> motorSettings) {
        this.motorSettings = motorSettings;
    }

    public void convertDataForSensorS1(ArrayList<TextField[]> list) {
        sensorS1DistancesMin = new ArrayList<>();
        sensorS1DistancesMax = new ArrayList<>();
        for (TextField item[] : list) {
            sensorS1DistancesMin.add(Integer.parseInt(item[0].getText()));
            sensorS1DistancesMax.add(Integer.parseInt(item[1].getText()));
        }
    }

    public void convertDataForSensorS2(ArrayList<TextField[]> list) {
        sensorS2DistancesMin = new ArrayList<>();
        sensorS2DistancesMax = new ArrayList<>();
        for (TextField item[] : list) {
            sensorS2DistancesMin.add(Integer.parseInt(item[0].getText()));
            sensorS2DistancesMax.add(Integer.parseInt(item[1].getText()));
        }
    }

    public void convertDataForSensorS3(ArrayList<TextField[]> list) {
        sensorS3DistancesMin = new ArrayList<>();
        sensorS3DistancesMax = new ArrayList<>();
        for (TextField item[] : list) {
            sensorS3DistancesMin.add(Integer.parseInt(item[0].getText()));
            sensorS3DistancesMax.add(Integer.parseInt(item[1].getText()));
        }
    }

    public void convertDataForSensorS4(ArrayList<TextField[]> list) {
        sensorS4DistancesMin = new ArrayList<>();
        sensorS4DistancesMax = new ArrayList<>();
        for (TextField item[] : list) {
            sensorS4DistancesMin.add(Integer.parseInt(item[0].getText()));
            sensorS4DistancesMax.add(Integer.parseInt(item[1].getText()));
        }
    }

    public void convertDataForMotors(ArrayList<ComboBox> list) {
        motorSettings = new ArrayList<>();
        for (ComboBox item : list) {
            motorSettings.add(String.valueOf(item.getValue()));
        }
    }

    public File getExperimentFile() {
        return experimentFile;
    }

    public void setExperimentFile(File experimentFile) {
        this.experimentFile = experimentFile;
    }

    public NeuralNetwork getNeuralNetwork() {
        return IOOperations.loadNetwork(new File(nnFilePath));
    }

}
