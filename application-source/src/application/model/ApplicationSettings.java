package application.model;

import application.model.ev3.IRDevice;
import javafx.animation.Timeline;
import javafx.concurrent.Service;
import javafx.stage.Stage;

/**
 * Application settings class.
 *
 * @author Dizzy
 */
public class ApplicationSettings {

    public static int WINDOW_WIDTH = 850; // application window width parametr
    public static int WINDOW_HEIGHT = 650; // aplication window height parametr
    public static int POPUP_WINDOW_WIDTH = 400; // application popup window width parametr
    public static int POPUP_WINDOW_HEIGHT = 300; // aplication popup window height parametr

    public static IRDevice connectedEV3 = null; // actual connected device
    public static Experiment loadedExperiment = null; // actual loaded experiment
    public static Stage experimentStage = null; // experiment window
    public static Stage aboutStage = null; // about window
    public static Service tempService = null; // temp thread for computing etc.
    public static Timeline experimentTimeline = null; // experiment timeline
    public static long maxEV3Speed = 600;

    public static String tsFileDir = "./training-sets"; //folder for store the generated training sets
    public static String tsFileExtension = ".txt"; // <filename>.extension
    public static String nnFileDir = "./neural-networks"; //folder for store the NNs
    public static String nnFileExtension = ".xml"; // <filename>.extension
    public static String expFileDir = "./experiments"; //folder for store the experiments settings
    public static String applicationIconPath = "/img/icon.png"; //path to icon (without .)
}
