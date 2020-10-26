/**
 *
 * @author Dizzy
 */
package application.model.neuralnetwork.backpropagation;

import application.model.ApplicationSettings;
import java.util.*;

/**
 * NeuralNetwork class represents a neural network.
 */
public class NeuralNetwork {

    private String name = null;
    private ArrayList<Neuron> inputLayer = new ArrayList<>();
    private ArrayList<Neuron> hiddenLayer = new ArrayList<>();
    private ArrayList<Neuron> outputLayer = new ArrayList<>();
    private int[] neuronsInLayers = null; // the number of neurons in layers; Index: 0 = input, 1 = hidden, 2 = output
    private Neuron biasNeuron = null; // fictitious still active neuron
    private double alfa; // α, learning coefficient 
    private double momentum; // μ, parameter momentum (measure of the influence of prior learning)
    private double minLambda; // λ, slope coefficient, if minλ different from maxλ network is not homogeneous
    private double maxLambda;
    private ArrayList<Double> errorList; // change of the error
    private double lastError; // the last recorded error
    private int lastStep; // the last step of learning
    private double inputs[][] = null; // input
    private double expectedOutputs[][] = null; // expected output
    private double realOutputs[][] = null; // real output

    public NeuralNetwork() {
        this.minLambda = 1;
        this.maxLambda = 1;
        this.alfa = 0.7;
        this.momentum = 0.9;
        this.errorList = new ArrayList();

        // reset counters
        Neuron.setCounter(0);
        Synapse.setCounter(0);

        this.biasNeuron = new Neuron();
    }

    /**
     * Creates a network structure.
     *
     * @param inputNeurons number of input neurons
     * @param hiddenNeurons number of hidden neurons
     * @param outputNeurons number of output neurons
     */
    public void createNetwork(int inputNeurons, int hiddenNeurons, int outputNeurons) {
        this.neuronsInLayers = new int[]{inputNeurons, hiddenNeurons, outputNeurons}; // set the number of neurons in layers

        for (int j = 0; j < neuronsInLayers[0]; j++) // input layer
        {
            Neuron neuron = new Neuron(); // create neuron
            neuron.setLambda(randomDoubleBetween(minLambda, maxLambda)); // set lambda
            inputLayer.add(neuron); // add to layer
        }

        for (int j = 0; j < neuronsInLayers[1]; j++) // hidden layer
        {
            Neuron neuron = new Neuron(); // create neuron
            neuron.setLambda(randomDoubleBetween(minLambda, maxLambda)); // set lambda
            neuron.addInputConnection(inputLayer); // create synapse
            neuron.addBiasConnection(biasNeuron); // create synapse of the threshold
            hiddenLayer.add(neuron); // add to layer
        }

        for (int j = 0; j < neuronsInLayers[2]; j++) // output layer
        {
            Neuron neuron = new Neuron(); // create neuron
            neuron.setLambda(randomDoubleBetween(minLambda, maxLambda)); // set lambda
            neuron.addInputConnection(hiddenLayer); // create synapse
            neuron.addBiasConnection(biasNeuron); // create synapse of the threshold
            outputLayer.add(neuron); // add to layer
        }

        // set random weights
        hiddenLayer.stream().map((neuron) -> neuron.getInputConnection()).forEach((connections) -> {
            connections.stream().forEach((synapse) -> {
                synapse.setWeight(randomDoubleBetween(-1, 1));
            });
        });

        outputLayer.stream().map((neuron) -> neuron.getInputConnection()).forEach((connections) -> {
            connections.stream().forEach((conn) -> {
                conn.setWeight(randomDoubleBetween(-1, 1));
            });
        });
    }

    /**
     * Starts training network until it reaches the maximum number of steps the
     * network or the global error is equal to or less than the minimum of the
     * global error.
     *
     * @param maxSteps the maximum number of steps in learning
     * @param minError the value of the minimum errors
     */
    public void train(int maxSteps, double minError) {
        int i;
        double error = 1;
        double output[];
        errorList.clear();
        lastStep = 0;
        lastError = 0;

        for (i = 0; i < maxSteps && error > minError; i++) // main loop
        {
            error = 0;
            for (int j = 0; j < inputs.length; j++) // number of patterns
            {
                setInput(inputs[j]); // set pattern
                activateNeurons(); // activate neurons
                output = getOutput(); // obtaining the output neurons
                realOutputs[j] = output;  // storing real output
                for (int k = 0; k < expectedOutputs[j].length; k++) // number of outputs
                {
                    error += Math.pow(output[k] - expectedOutputs[j][k], 2); // square error
                }
                backpropagate(expectedOutputs[j]); // apply the BP to the pattern
            }
            errorList.add(error); // add error to the list
        }
        lastError = error; // record the last error
        lastStep = i; // store the number of the latest step
    }

    /**
     * Train NN only one epoch.
     *
     */
    public void trainOneStep() {
        double error = 0;
        double output[];

        for (int j = 0; j < inputs.length; j++) // number of patterns
        {
            setInput(inputs[j]); // set pattern
            activateNeurons(); // activate neurons
            output = getOutput(); // obtaining the output neurons
            realOutputs[j] = output;  // storing real output
            for (int k = 0; k < expectedOutputs[j].length; k++) // number of outputs
            {
                error += Math.pow(output[k] - expectedOutputs[j][k], 2); // square error
            }
            backpropagate(expectedOutputs[j]); // apply the BP to the pattern
        }
        errorList.add(error); // add error to the list
        lastError = error; // record the last error
        lastStep++; // store the number of the latest step
    }

    /**
     * Activates neurons and calculates their potential.
     *
     * @return Return number of excited neurons.
     */
    public long activateNeurons() {
        long excited = 0;

        for (Neuron neuron : hiddenLayer) {
            neuron.calculateOutput();
            if (neuron.getOutput() > neuron.getBiasConnection().getWeight()) {
                excited++;
            }
        }
        for (Neuron neuron : outputLayer) {
            neuron.calculateOutput();
            if (Math.round(neuron.getOutput()) > neuron.getBiasConnection().getWeight()) {
                excited++;
            }
        }

        return excited;
    }

    /**
     * Performs back-propagation errors.
     *
     * @param expectedOutput the desired pattern
     */
    public void backpropagate(double expectedOutput[]) {

        // input - hidden layer
        int i = 0;
        for (Neuron neuron : outputLayer) {
            ArrayList<Synapse> connections = neuron.getInputConnection();
            for (Synapse synapse : connections) {
                double yi = neuron.getOutput();
                double xi = synapse.getFromNeuron().getOutput();
                double deltaWeight = alfa * (expectedOutput[i] - yi) * neuron.getLambda() * yi * (1 - yi) * xi; // weight gain

                synapse.setPrevDeltaWeight(); // set the previous increment
                synapse.setDeltaWeight(deltaWeight); // set the current increment
                synapse.setWeight(synapse.getWeight() + deltaWeight + momentum * synapse.getPrevDeltaWeight()); // add increment to weight
            }
            i++;
        }

        // hidden - output layer
        hiddenLayer.stream().forEach((neuron) -> {
            ArrayList<Synapse> connections = neuron.getInputConnection();
            connections.stream().forEach((synapse) -> {
                double zj = neuron.getOutput();
                double xi = synapse.getFromNeuron().getOutput();
                double sum = 0;
                int j = 0;

                for (Neuron outputNeuron : outputLayer) {
                    double wi = outputNeuron.findOutputConnection(neuron.getId()).getWeight();
                    double yi = outputNeuron.getOutput();

                    sum += ((expectedOutput[j] - yi) * outputNeuron.getLambda() * yi * (1 - yi) * wi);
                    j++;
                }

                double deltaWeight = alfa * sum * (neuron.getLambda() * zj * (1 - zj) * xi); // weight gain
                synapse.setPrevDeltaWeight(); // set the previous increment
                synapse.setDeltaWeight(deltaWeight); // set the current increment
                synapse.setWeight(synapse.getWeight() + deltaWeight + momentum * synapse.getPrevDeltaWeight()); // add increment to weight
            });
        });
    }

    /**
     * Set the pattern to the network
     *
     * @param inputs patterns
     */
    public void setInput(double inputs[]) {
        for (int i = 0; i < inputLayer.size(); i++) {
            inputLayer.get(i).setOutput(inputs[i]);
        }
    }

    /**
     * Returns the value of the output neurons.
     *
     * @return Returns the output neurons double[]
     */
    public double[] getOutput() {
        double[] outputs = new double[outputLayer.size()];
        for (int i = 0; i < outputLayer.size(); i++) {
            outputs[i] = outputLayer.get(i).getOutput();
        }
        return outputs;
    }

    /**
     * Activates the network, perform the forward propagation and sets the real
     * output of the network.
     *
     * @return Return number of excited neurons.
     */
    public long activate() {
        double output[];
        long excited = 0;

        for (int j = 0; j < inputs.length; j++) // across all patterns
        {
            setInput(inputs[j]); // set the pattern
            excited = activateNeurons(); // activate neurons
            output = getOutput(); // obtaining the output neurons
            realOutputs[j] = output; // set real output of the network
        }

        return excited;
    }

    /**
     * Normalizes the real output of the network to the values {0, 1} using
     * Math.round()
     */
    public void normalizeRealOutput() {
        for (double[] realOutput : realOutputs) {
            for (int j = 0; j < realOutputs[0].length; j++) {
                realOutput[j] = Math.round(realOutput[j]);
            }
        }
    }

    /**
     * Generates a random value.
     *
     * @param fromNumber generate from value
     * @param toNumber generate to value
     * @return returns a real number in the range
     */
    private static double randomDoubleBetween(double fromNumber, double toNumber) {
        return (fromNumber + ((new Random().nextDouble()) * (toNumber - fromNumber)));
    }

    public double getLastError() {
        return lastError;
    }

    public int getLastStep() {
        return lastStep;
    }

    public ArrayList<Double> getErrorList() {
        return errorList;
    }

    public double[][] getRealOutputs() {
        return realOutputs;
    }

    public double getMinLambda() {
        return minLambda;
    }

    public void setMinLambda(double minLambda) {
        this.minLambda = minLambda;
    }

    public double getMaxLambda() {
        return maxLambda;
    }

    public void setMaxLambda(double maxLambda) {
        this.maxLambda = maxLambda;
    }

    public double getMomentum() {
        return momentum;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public double[][] getInputs() {
        return inputs;
    }

    public void setInputs(double[][] inputs) {
        this.inputs = inputs;
    }

    public double[][] getExpectedOutputs() {
        return expectedOutputs;
    }

    public void setExpectedOutputs(double[][] expectedOutputs) {
        this.expectedOutputs = expectedOutputs;
        this.realOutputs = new double[expectedOutputs.length][expectedOutputs[0].length];
    }
    
    /**
     * Normalize outputs value in TS to the interval [0,1] for training NN.
     */
    public void normalizeOutputs() {
        double value = 0;
        for (int i = 0; i < expectedOutputs.length; i++) {
            for (int j = 0; j < expectedOutputs[0].length; j++) {
                value = expectedOutputs[i][j] / ApplicationSettings.maxEV3Speed;
                if(value > 1) value = 1;
                if(value < 0) value = 0;
                expectedOutputs[i][j] = value;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAlfa() {
        return alfa;
    }

    public void setAlfa(double alfa) {
        this.alfa = alfa;
    }

    public ArrayList<Neuron> getInputLayer() {
        return inputLayer;
    }

    public void setInputLayer(ArrayList<Neuron> inputLayer) {
        this.inputLayer = inputLayer;
    }

    public ArrayList<Neuron> getHiddenLayer() {
        return hiddenLayer;
    }

    public void setHiddenLayer(ArrayList<Neuron> hiddenLayer) {
        this.hiddenLayer = hiddenLayer;
    }

    public ArrayList<Neuron> getOutputLayer() {
        return outputLayer;
    }

    public void setOutputLayer(ArrayList<Neuron> outputLayer) {
        this.outputLayer = outputLayer;
    }

    public int[] getNeuronsInLayers() {
        return neuronsInLayers;
    }

    public void setNeuronsInLayers(int[] neuronsInLayers) {
        this.neuronsInLayers = neuronsInLayers;
    }

    public Neuron getBiasNeuron() {
        return biasNeuron;
    }

    public void setBiasNeuron(Neuron biasNeuron) {
        this.biasNeuron = biasNeuron;
    }

    public void setErrorList(ArrayList<Double> errorList) {
        this.errorList = errorList;
    }

    public void setLastError(double lastError) {
        this.lastError = lastError;
    }

    public void setLastStep(int lastStep) {
        this.lastStep = lastStep;
    }

    public void setRealOutputs(double[][] realOutputs) {
        this.realOutputs = realOutputs;
    }
}
