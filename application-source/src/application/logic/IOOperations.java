package application.logic;

import application.model.neuralnetwork.backpropagation.NeuralNetwork;
import application.model.neuralnetwork.backpropagation.Synapse;
import application.model.neuralnetwork.backpropagation.Neuron;
import application.model.ApplicationSettings;
import application.model.Experiment;
import application.ui.Dialogs;
import application.model.neuralnetwork.ART;
import application.model.neuralnetwork.TrainingSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * I/O operations class.
 *
 * @author Dizzy
 */
public class IOOperations {

    private static final String outputProperty = "{http://xml.apache.org/xslt}indent-amount";

    /**
     * Generate file of the training set.
     *
     * @param inputs Number of inputs.
     * @param outputs Number of outputs.
     * @param file File
     * @param group Number of columns forming a group for generating reduced
     * @param exclude Exclude zero group in columns training set.
     */
    public static void generateTSFile(int inputs, int outputs, File file, int group, boolean exclude) {
        int numRows = (int) Math.pow(2, inputs);
        String line = "";
        BufferedWriter bw = null;
        boolean out = false;
        int counter = 1;
        int sum = 0;
        int patternsCount = 0;

        // Generate and save TS
        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write("inputs:" + inputs);
            bw.newLine();
            bw.write("outputs:" + outputs);
            bw.newLine();
            if (group > 1) {
                patternsCount = (int) Math.pow(group + 1, inputs / group);
            } else {
                patternsCount = (int) Math.pow(2, inputs);
            }
            if (exclude) {
                patternsCount = (int) Math.pow(group, inputs / group);
                if (inputs == group) {
                    patternsCount = inputs;
                }
                if (group == 1) {
                    patternsCount = 1;
                }
            }
            bw.write("patterns:" + patternsCount);
            bw.newLine();

            for (int i = 0; i < numRows; i++) {
                line = "";
                out = false;
                counter = 1;
                sum = 0;
                for (int j = 0; j < inputs + outputs; j++) {
                    if (j < inputs) { //generate only inputs
                        int val = numRows * j + i;
                        int ret = (1 & (val >>> j));
                        line += ret;

                        sum += ret; // count number of ones in inputs

                        if (sum > 1 && group >= counter) {
                            out = true;
                        }

                        if (counter >= group) { // reset counter and sum for every group
                            counter = 0;
                            if (exclude && sum == 0) { //exclude vector of zeros 
                                out = true;
                            }
                            sum = 0;
                        }
                        counter++; //increase information of position in groupe
                    } else {
                        line += 0; //value of output
                    }
                    if (j < (inputs + outputs - 1)) { //not add separator to the end of line
                        line += ","; //column separator
                    }
                }
                if (out == false) { //if is generating reduce TS write only unique patterns
                    bw.write(line);
                    bw.newLine(); //row separator
                }
            }

            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the training set file.", "", ex);
        }
    }

    /**
     * Filter file of the training set.
     *
     * @param file File
     * @param vigilance parametr for ART NN
     * @param entireSet true if filter entire TS
     * @param bestIsOne true - winner to the cluster is vector with max 1; iff
     * false with max 0
     */
    public static void filterTSFile(File file, double vigilance, boolean entireSet, boolean bestIsOne) {
        BufferedReader br = null;
        String line = "";
        int inputs = 0;
        int outputs = 0;
        int patterns = 0;
        int trainingSet[][] = null;

        try {
            br = new BufferedReader(new FileReader(file));

            line = br.readLine(); //read inputs info
            String[] value = line.split(":");
            inputs = Integer.parseInt(value[1]);

            line = br.readLine(); //read outputs info
            value = line.split(":");
            outputs = Integer.parseInt(value[1]);

            line = br.readLine(); //read patterns info
            value = line.split(":");
            patterns = Integer.parseInt(value[1]);

            trainingSet = new int[patterns][inputs + outputs];

            int i = 0;
            while ((line = br.readLine()) != null) { // read file
                if (i < patterns) {
                    value = line.split(","); // separate line
                    for (int j = 0; j < inputs + outputs; j++) {
                        trainingSet[i][j] = Integer.parseInt(value[j]);
                    }
                }
                i++;
            }
        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to open file", "Unable to open the training set file.", "", ex);
        }

        if (trainingSet != null) {
            ART artNN = new ART(trainingSet, vigilance, entireSet, bestIsOne, inputs);
            long ms = artNN.start();
            trainingSet = artNN.getFiteredTS();
            saveTSFile(trainingSet, file, inputs, outputs);
            Dialogs.alertDialog("Filtering completed", "Filtering the training set was completed.", "The training set was divided into " + artNN.getNumOfClusters() + " cluster for " + ms + "ms. The file is stored in the same place as the original.");
        }

    }

    /**
     * Open file of the training set.
     *
     * @param file TS file
     * @return TrainingSet training set
     */
    public static TrainingSet loadTS(File file) {
        BufferedReader br = null;
        String line = "";
        int inputs = 0;
        int outputs = 0;
        int patterns = 0;
        double[][] retInputs = null;
        double[][] retExpectedOutputs = null;

        try {
            br = new BufferedReader(new FileReader(file));

            line = br.readLine(); //read inputs info
            String[] value = line.split(":");
            inputs = Integer.parseInt(value[1]);

            line = br.readLine(); //read outputs info
            value = line.split(":");
            outputs = Integer.parseInt(value[1]);

            line = br.readLine(); //read patterns info
            value = line.split(":");
            patterns = Integer.parseInt(value[1]);

            retInputs = new double[patterns][inputs];
            retExpectedOutputs = new double[patterns][outputs];

            int i = 0;
            while ((line = br.readLine()) != null) { // read file
                if (i < patterns) {
                    value = line.split(","); // separate line
                    for (int j = 0; j < inputs + outputs; j++) {
                        if (j < inputs) {
                            retInputs[i][j] = Integer.parseInt(value[j]);
                        }
                        if (j >= inputs) {
                            retExpectedOutputs[i][j - inputs] = Integer.parseInt(value[j]);
                        }
                    }
                }
                i++;
            }
        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to open file", "Unable to open the training set file.", "", ex);
        }

        return new TrainingSet(retInputs, retExpectedOutputs);
    }

    /**
     * Save training set file.
     *
     * @param trainingSet TS
     * @param inputs Number of inputs.
     * @param outputs Number of outputs.
     * @param file File
     */
    public static void saveTSFile(int[][] trainingSet, File file, int inputs, int outputs) {
        String line = "";
        BufferedWriter bw = null;

        // Generate and save TS
        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write("inputs:" + inputs);
            bw.newLine();
            bw.write("outputs:" + outputs);
            bw.newLine();
            bw.write("patterns:" + trainingSet.length);
            bw.newLine();

            for (int i = 0; i < trainingSet.length; i++) {
                line = "";
                for (int j = 0; j < trainingSet[0].length; j++) {
                    line += trainingSet[i][j];
                    if (j < (trainingSet[0].length - 1)) { //not add separator to the end of line
                        line += ","; //column separator
                    }
                }
                bw.write(line);
                bw.newLine(); //row separator
            }

            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the training set file.", "", ex);
        }
    }

    /**
     * Saves the file with a neural network.
     *
     * @param neuralNetwork neural network
     * @param file File
     */
    public static void saveNetwork(NeuralNetwork neuralNetwork, File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element nRoot = doc.createElement("neuralNetwork");

            Element nDimension = doc.createElement("neuronsInLayers");
            nDimension.appendChild(doc.createTextNode("input:" + neuralNetwork.getInputLayer().size() + ",hidden:" + neuralNetwork.getHiddenLayer().size() + ",output:" + neuralNetwork.getOutputLayer().size()));
            nRoot.appendChild(nDimension);

            Element nAlfa = doc.createElement("alpha");
            nAlfa.appendChild(doc.createTextNode(Double.toString(neuralNetwork.getAlfa())));
            nRoot.appendChild(nAlfa);

            Element nMomentum = doc.createElement("momentum");
            nMomentum.appendChild(doc.createTextNode(Double.toString(neuralNetwork.getMomentum())));
            nRoot.appendChild(nMomentum);
            Element nMinLambda = doc.createElement("minLambda");
            nMinLambda.appendChild(doc.createTextNode(Double.toString(neuralNetwork.getMinLambda())));
            nRoot.appendChild(nMinLambda);
            Element nMaxLambda = doc.createElement("maxLambda");
            nMaxLambda.appendChild(doc.createTextNode(Double.toString(neuralNetwork.getMaxLambda())));
            nRoot.appendChild(nMaxLambda);

            Element nINeurons = doc.createElement("inputNeurons");
            for (Neuron item : neuralNetwork.getInputLayer()) {
                Element nNeuron = doc.createElement("neuron");

                Element nNId = doc.createElement("id");
                nNId.appendChild(doc.createTextNode(Integer.toString(item.getId())));
                nNeuron.appendChild(nNId);
                Element nNLambda = doc.createElement("lambda");
                nNLambda.appendChild(doc.createTextNode(Double.toString(item.getLambda())));
                nNeuron.appendChild(nNLambda);

                nINeurons.appendChild(nNeuron);
            }
            nRoot.appendChild(nINeurons);

            Element nHNeurons = doc.createElement("hiddenNeurons");
            for (Neuron item : neuralNetwork.getHiddenLayer()) {
                Element nNeuron = doc.createElement("neuron");

                Element nNId = doc.createElement("id");
                nNId.appendChild(doc.createTextNode(Integer.toString(item.getId())));
                nNeuron.appendChild(nNId);
                Element nNBiasConnection = doc.createElement("biasConnection");
                nNBiasConnection.appendChild(doc.createTextNode(Integer.toString(item.getBiasConnection().getId())));
                nNeuron.appendChild(nNBiasConnection);
                Element nNLambda = doc.createElement("lambda");
                nNLambda.appendChild(doc.createTextNode(Double.toString(item.getLambda())));
                nNeuron.appendChild(nNLambda);

                nHNeurons.appendChild(nNeuron);
            }
            nRoot.appendChild(nHNeurons);

            Element nONeurons = doc.createElement("outputNeurons");
            for (Neuron item : neuralNetwork.getOutputLayer()) {
                Element nNeuron = doc.createElement("neuron");

                Element nNId = doc.createElement("id");
                nNId.appendChild(doc.createTextNode(Integer.toString(item.getId())));
                nNeuron.appendChild(nNId);
                Element nNBiasConnection = doc.createElement("biasConnection");
                nNBiasConnection.appendChild(doc.createTextNode(Integer.toString(item.getBiasConnection().getId())));
                nNeuron.appendChild(nNBiasConnection);
                Element nNLambda = doc.createElement("lambda");
                nNLambda.appendChild(doc.createTextNode(Double.toString(item.getLambda())));
                nNeuron.appendChild(nNLambda);

                nONeurons.appendChild(nNeuron);
            }
            nRoot.appendChild(nONeurons);

            Element nSynapses = doc.createElement("synapses");
            for (Neuron item : neuralNetwork.getHiddenLayer()) {
                ArrayList<Synapse> connections = item.getInputConnection();
                for (Synapse con : connections) {
                    Element nSynapse = doc.createElement("synapse");

                    Element nSFrom = doc.createElement("NID");
                    nSFrom.appendChild(doc.createTextNode(Integer.toString(item.getId())));
                    nSynapse.appendChild(nSFrom);
                    Element nSTo = doc.createElement("SID");
                    nSTo.appendChild(doc.createTextNode(Integer.toString(con.getId())));
                    nSynapse.appendChild(nSTo);
                    Element nSWeight = doc.createElement("weight");
                    nSWeight.appendChild(doc.createTextNode(Double.toString(con.getWeight())));
                    nSynapse.appendChild(nSWeight);

                    nSynapses.appendChild(nSynapse);
                }
            }
            for (Neuron item : neuralNetwork.getOutputLayer()) {
                ArrayList<Synapse> connections = item.getInputConnection();
                for (Synapse con : connections) {
                    Element nSynapse = doc.createElement("synapse");

                    Element nSFrom = doc.createElement("NID");
                    nSFrom.appendChild(doc.createTextNode(Integer.toString(item.getId())));
                    nSynapse.appendChild(nSFrom);
                    Element nSTo = doc.createElement("SID");
                    nSTo.appendChild(doc.createTextNode(Integer.toString(con.getId())));
                    nSynapse.appendChild(nSTo);
                    Element nSWeight = doc.createElement("weight");
                    nSWeight.appendChild(doc.createTextNode(Double.toString(con.getWeight())));
                    nSynapse.appendChild(nSWeight);

                    nSynapses.appendChild(nSynapse);
                }
            }
            nRoot.appendChild(nSynapses);

            doc.appendChild(nRoot);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(outputProperty, "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);

            transformer.transform(source, result);

        } catch (TransformerConfigurationException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the neural network file.", "", ex);
        } catch (TransformerException | ParserConfigurationException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the neural network file.", "", ex);

        }
    }

    /**
     * Load the file with a neural network.
     *
     * @param file File
     * @return NeuralNetwork
     */
    public static NeuralNetwork loadNetwork(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            doc.getDocumentElement().normalize();

            double alpha = Double.parseDouble(doc.getElementsByTagName("alpha").item(0).getTextContent());
            double momentum = Double.parseDouble(doc.getElementsByTagName("momentum").item(0).getTextContent());
            double minLambda = Double.parseDouble(doc.getElementsByTagName("minLambda").item(0).getTextContent());
            double maxLambda = Double.parseDouble(doc.getElementsByTagName("maxLambda").item(0).getTextContent());

            ArrayList<Neuron> inputLayer = new ArrayList<>();
            ArrayList<Neuron> hiddenLayer = new ArrayList<>();
            ArrayList<Neuron> outputLayer = new ArrayList<>();
            HashMap<String, Double> synapseUpdateMap = new HashMap<>();

            Neuron.setCounter(0);
            Synapse.setCounter(0);

            Neuron biasNeuron = new Neuron();

            Node inputNeurons = doc.getElementsByTagName("inputNeurons").item(0);
            NodeList listInput = inputNeurons.getChildNodes();

            for (int i = 0; i < listInput.getLength(); i++) {
                Node node = listInput.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element neuron = (Element) node;

                    int id = Integer.parseInt(neuron.getElementsByTagName("id").item(0).getTextContent());
                    double lambda = Double.parseDouble(neuron.getElementsByTagName("lambda").item(0).getTextContent());
                    Neuron newNeuron = new Neuron();
                    newNeuron.setId(id);
                    newNeuron.setLambda(lambda);
                    inputLayer.add(newNeuron);
                }
            }

            Node hiddenNeurons = doc.getElementsByTagName("hiddenNeurons").item(0);
            NodeList listHidden = hiddenNeurons.getChildNodes();

            for (int i = 0; i < listHidden.getLength(); i++) {
                Node node = listHidden.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element neuron = (Element) node;

                    int id = Integer.parseInt(neuron.getElementsByTagName("id").item(0).getTextContent());
                    double lambda = Double.parseDouble(neuron.getElementsByTagName("lambda").item(0).getTextContent());
                    Neuron newNeuron = new Neuron();
                    newNeuron.setId(id);
                    newNeuron.setLambda(lambda);
                    newNeuron.addInputConnection(inputLayer);
                    newNeuron.addBiasConnection(biasNeuron);
                    hiddenLayer.add(newNeuron);
                }
            }

            Node outputNeurons = doc.getElementsByTagName("outputNeurons").item(0);
            NodeList listOutput = outputNeurons.getChildNodes();

            for (int i = 0; i < listOutput.getLength(); i++) {
                Node node = listOutput.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element neuron = (Element) node;

                    int id = Integer.parseInt(neuron.getElementsByTagName("id").item(0).getTextContent());
                    double lambda = Double.parseDouble(neuron.getElementsByTagName("lambda").item(0).getTextContent());
                    Neuron newNeuron = new Neuron();
                    newNeuron.setId(id);
                    newNeuron.setLambda(lambda);
                    newNeuron.addInputConnection(hiddenLayer);
                    newNeuron.addBiasConnection(biasNeuron);
                    outputLayer.add(newNeuron);
                }
            }

            Node synapses = doc.getElementsByTagName("synapses").item(0);
            NodeList listSynapses = synapses.getChildNodes();

            for (int i = 0; i < listSynapses.getLength(); i++) {
                Node node = listSynapses.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element synapse = (Element) node;

                    int from = Integer.parseInt(synapse.getElementsByTagName("NID").item(0).getTextContent());
                    int to = Integer.parseInt(synapse.getElementsByTagName("SID").item(0).getTextContent());
                    double weight = Double.parseDouble(synapse.getElementsByTagName("weight").item(0).getTextContent());

                    synapseUpdateMap.put(synapseKey(from, to), weight);
                }
            }

            // set weights of output layer
            outputLayer.stream().forEach((neuron) -> {
                ArrayList<Synapse> connections = neuron.getInputConnection();
                connections.stream().forEach((synapse) -> {
                    String key = synapseKey(neuron.getId(), synapse.getId());
                    double newWeight = synapseUpdateMap.get(key);
                    synapse.setWeight(newWeight);
                });
            });
            // set weights of hidden layer
            hiddenLayer.stream().forEach((neuron) -> {
                ArrayList<Synapse> connections = neuron.getInputConnection();
                connections.stream().forEach((synapse) -> {
                    String key = synapseKey(neuron.getId(), synapse.getId());
                    double newWeight = synapseUpdateMap.get(key);
                    synapse.setWeight(newWeight);
                });
            });

            NeuralNetwork newNetwork = new NeuralNetwork();
            newNetwork.setAlfa(alpha);
            newNetwork.setMomentum(momentum);
            newNetwork.setMinLambda(minLambda);
            newNetwork.setMaxLambda(maxLambda);
            newNetwork.setInputLayer(inputLayer);
            newNetwork.setHiddenLayer(hiddenLayer);
            newNetwork.setOutputLayer(outputLayer);
            newNetwork.setRealOutputs(new double[1][outputLayer.size()]);
            newNetwork.setNeuronsInLayers(new int[]{inputLayer.size(), hiddenLayer.size(), outputLayer.size()});

            return newNetwork;
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Dialogs.exceptionDialog("Unable to open file", "Unable to open the neural network file.", "", ex);
            return null;
        }
    }

    private static String synapseKey(int neuronID, int synapseID) {
        return "NID" + neuronID + "_SID" + synapseID;
    }

    /**
     * Check if TS dir is created. Create if not.
     *
     * @return file File
     */
    public static File getTSDir() {
        File dir = new File(ApplicationSettings.tsFileDir);

        //Check dir exists
        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir;
    }

    /**
     * Check if NN dir is created. Create if not.
     *
     * @return file File
     */
    public static File getNNDir() {
        File dir = new File(ApplicationSettings.nnFileDir);

        //Check dir exists
        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir;
    }

    /**
     * Check if experiment dir is created. Create if not.
     *
     * @return file File
     */
    public static File getExpDir() {
        File dir = new File(ApplicationSettings.expFileDir);

        //Check dir exists
        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir;
    }

    /**
     * Save file woth error list
     *
     * @param fileName File name
     * @param nn NeuralNetwork gets info from NN
     * @param trainingTime passed time in ms
     * @param maxSteps parameter
     * @param minError parameter
     */
    public static void saveErrorFile(String fileName, NeuralNetwork nn, String trainingTime, int maxSteps, double minError) {
        try {
            ArrayList errorList = nn.getErrorList();
            BufferedWriter outputWriter;

            outputWriter = new BufferedWriter(new FileWriter(new File(ApplicationSettings.nnFileDir + "/" + fileName + ".txt")));

            outputWriter.write("Training time: " + trainingTime);
            outputWriter.newLine();
            outputWriter.write("Sum of squared errors: " + nn.getLastError());
            outputWriter.newLine();
            outputWriter.write("Min error parametr: " + minError);
            outputWriter.newLine();
            outputWriter.write("Number of steps: " + nn.getLastStep());
            outputWriter.newLine();
            outputWriter.write("Max steps parametr: " + maxSteps);
            outputWriter.newLine();
            outputWriter.newLine();
            outputWriter.write("Error list:");
            outputWriter.newLine();

            for (Object object : errorList) {
                outputWriter.write(object.toString());
                outputWriter.newLine();
            }
            outputWriter.flush();
            outputWriter.close();

        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the error file.", "", ex);
        }
    }

    /**
     * Save experiment file
     *
     * @param file File
     * @param exp Experiment class
     */
    public static void saveExperiment(File file, Experiment exp) {
        String line = "";
        BufferedWriter bw = null;

        // Generate and save TS
        try {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write(exp.getNNFilePath());
            bw.newLine();
            bw.write("engineAcceleration:" + exp.getAcceleration());
            bw.newLine();
            bw.write("timeOfLoop:" + exp.getTick());
            bw.newLine();
            bw.write("muteSounds:" + exp.isMuteSounds());
            bw.newLine();
            bw.write("inputsSize:" + exp.getSensorS1DistancesMin().size());
            bw.newLine();
            bw.write("outputsSize:" + exp.getMotorSettings().size());
            bw.newLine();

            for (int i = 0; i < exp.getSensorS1DistancesMin().size(); i++) {
                line = "i" + i;
                line += ":" + exp.getSensorS1DistancesMin().get(i) + "," + exp.getSensorS1DistancesMax().get(i) + "," + exp.getSensorS2DistancesMin().get(i) + "," + exp.getSensorS2DistancesMax().get(i) + "," + exp.getSensorS3DistancesMin().get(i) + "," + exp.getSensorS3DistancesMax().get(i) + "," + exp.getSensorS4DistancesMin().get(i) + "," + exp.getSensorS4DistancesMax().get(i);
                bw.write(line);
                bw.newLine(); //row separator
            }

            for (int i = 0; i < exp.getMotorSettings().size(); i++) {
                line = "o" + i;
                line += ":" + exp.getMotorSettings().get(i);
                bw.write(line);
                bw.newLine(); //row separator
            }

            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the experiment file.", "", ex);
        }
    }

    /**
     * Open experiment file.
     *
     * @param file Experiment file
     * @return Experiment
     */
    public static Experiment loadExperimentFile(File file) {
        BufferedReader br = null;
        String line = "";
        String nnPath = "";
        int acceleration = 0;
        int loop = 0;
        boolean mute = false;
        int inputs = 0;
        int outputs = 0;
        ArrayList<Integer> sensorS1DistancesMin = new ArrayList<>();
        ArrayList<Integer> sensorS1DistancesMax = new ArrayList<>();
        ArrayList<Integer> sensorS2DistancesMin = new ArrayList<>();
        ArrayList<Integer> sensorS2DistancesMax = new ArrayList<>();
        ArrayList<Integer> sensorS3DistancesMin = new ArrayList<>();
        ArrayList<Integer> sensorS3DistancesMax = new ArrayList<>();
        ArrayList<Integer> sensorS4DistancesMin = new ArrayList<>();
        ArrayList<Integer> sensorS4DistancesMax = new ArrayList<>();
        ArrayList<String> motorSpeeds = new ArrayList<>();
        Experiment exp = null;
        String[] tempString = null;

        try {
            br = new BufferedReader(new FileReader(file));

            line = br.readLine(); //read nn file
            String[] value = line.split(":");
            nnPath = String.valueOf(value[1]);

            line = br.readLine(); //read acceleration
            value = line.split(":");
            acceleration = Integer.parseInt(value[1]);

            line = br.readLine(); //read time of loop
            value = line.split(":");
            loop = Integer.parseInt(value[1]);

            line = br.readLine(); //read mute info
            value = line.split(":");
            mute = Boolean.parseBoolean(value[1]);

            line = br.readLine(); //read inputs info
            value = line.split(":");
            inputs = Integer.parseInt(value[1]);

            line = br.readLine(); //read outputs info
            value = line.split(":");
            outputs = Integer.parseInt(value[1]);

            for (int i = 0; i < inputs; i++) {
                line = br.readLine();
                value = line.split(":"); // separate line
                tempString = value[1].split(","); // separate part of the line
                sensorS1DistancesMin.add(Integer.parseInt(tempString[0]));
                sensorS1DistancesMax.add(Integer.parseInt(tempString[1]));
                sensorS2DistancesMin.add(Integer.parseInt(tempString[2]));
                sensorS2DistancesMax.add(Integer.parseInt(tempString[3]));
                sensorS3DistancesMin.add(Integer.parseInt(tempString[4]));
                sensorS3DistancesMax.add(Integer.parseInt(tempString[5]));
                sensorS4DistancesMin.add(Integer.parseInt(tempString[6]));
                sensorS4DistancesMax.add(Integer.parseInt(tempString[7]));
            }

            for (int i = 0; i < outputs; i++) {
                line = br.readLine();
                value = line.split(":"); // separate line
                motorSpeeds.add(String.valueOf(value[1]));
            }

            exp = new Experiment();
            exp.setNNFilePath(nnPath);
            exp.setAcceleration(acceleration);
            exp.setTick(loop);
            exp.setMuteSounds(mute);
            exp.setExperimentFile(file);
            exp.setSensorS1DistancesMin(sensorS1DistancesMin);
            exp.setSensorS1DistancesMax(sensorS1DistancesMax);
            exp.setSensorS2DistancesMin(sensorS2DistancesMin);
            exp.setSensorS2DistancesMax(sensorS2DistancesMax);
            exp.setSensorS3DistancesMin(sensorS3DistancesMin);
            exp.setSensorS3DistancesMax(sensorS3DistancesMax);
            exp.setSensorS4DistancesMin(sensorS4DistancesMin);
            exp.setSensorS4DistancesMax(sensorS4DistancesMax);
            exp.setMotorSettings(motorSpeeds);

        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to open file", "Unable to open the experiment file.", "", ex);
        }

        return exp;
    }

    /**
     * Convert number of milisecond to strink format xd xh xm xs.
     *
     * @param numOfSeconds Number of seconds to convert.
     * @return String
     */
    public static String convertTime(long numOfSeconds) {
        String ret = "";
        long days = numOfSeconds / 86400;
        long hours = (numOfSeconds % 86400) / 3600;
        long minutes = ((numOfSeconds % 86400) % 3600) / 60;
        long seconds = ((numOfSeconds % 86400) % 3600) % 60;

        if (days > 0) {
            ret += days + "d ";
        }
        if (hours > 0) {
            ret += hours + "h ";
        }
        if (minutes > 0) {
            ret += minutes + "m ";
        }
        if (seconds > 0) {
            ret += seconds + "s";
        }
        if (seconds == 0) {
            ret += "< 1s";
        }
        return ret;
    }

    /**
     * Save file with simulation logs
     *
     * @param fileName File name
     * @param text Log text
     */
    public static void saveLogFile(String fileName, String text) {
        try {
            BufferedWriter outputWriter;

            outputWriter = new BufferedWriter(new FileWriter(new File(ApplicationSettings.expFileDir + "/" + fileName + ".txt"), true));

            outputWriter.write(text);
            outputWriter.newLine();

            outputWriter.flush();
            outputWriter.close();

        } catch (IOException ex) {
            Dialogs.exceptionDialog("Unable to save file", "Unable to save the log file.", "", ex);
        }
    }
}
