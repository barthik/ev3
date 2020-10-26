/**
 *
 * @author Dizzy
 */
package application.model.neuralnetwork.backpropagation;

import java.util.*;

/**
 * Neuron class represents the elementary unit of neural networks - neuron.
 */
public class Neuron {

    private static int counter = 0; // autoincrement id
    private int id;
    private Synapse biasConnection; // synapse to still active fictitious neuron
    private double bias = 1; // value of the still active fictitious neuron
    private double lambda = 1; // λ, slope parametr
    private double output;
    private ArrayList<Synapse> inputConnection = new ArrayList<>();
    private HashMap<Integer, Synapse> outputConnection = new HashMap<>(); // to backpropagation

    public Neuron() {
        this.id = Neuron.counter;
        Neuron.counter++;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        Neuron.counter = counter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public Synapse getBiasConnection() {
        return biasConnection;
    }

    public void setBiasConnection(Synapse biasConnection) {
        this.biasConnection = biasConnection;
    }

    public ArrayList<Synapse> getInputConnection() {
        return inputConnection;
    }

    public void setInputConnection(ArrayList<Synapse> Inconnections) {
        this.inputConnection = Inconnections;
    }

    public HashMap<Integer, Synapse> getOutputConnection() {
        return outputConnection;
    }

    public void setOutputConnection(HashMap<Integer, Synapse> connectionLookup) {
        this.outputConnection = connectionLookup;
    }

    /**
     * Creates a synapse to the fictitious still active neuron
     *
     * @param neuron fictitious still active neuron
     */
    public void addBiasConnection(Neuron neuron) {
        Synapse newSynapse = new Synapse(neuron, this);

        this.biasConnection = newSynapse;
        this.inputConnection.add(newSynapse);
    }

    /**
     * Creates a synapse between neurons and list of the input neurons and also
     * sets information for backpropagation.
     *
     * @param inputNeurons list of input neurons
     */
    public void addInputConnection(ArrayList<Neuron> inputNeurons) {
        inputNeurons.stream().forEach((neuron) -> {
            Synapse newSynapse = new Synapse(neuron, this);

            inputConnection.add(newSynapse);
            outputConnection.put(neuron.getId(), newSynapse);
        });
    }

    public Synapse findOutputConnection(int neuronIndex) {
        return outputConnection.get(neuronIndex);
    }

    /**
     * Calculate output neuron Y = S(∑(wij * xij) + w0j * Θ).
     *
     * S = transfer function (sigmoidFunction) Θ = threshold of the neuron
     * (bias)
     */
    public void calculateOutput() {
        double y = 0;

        for (Synapse synapse : this.inputConnection) {
            y += (synapse.getWeight() * synapse.getFromNeuron().getOutput());
        }

        y += (this.biasConnection.getWeight() * this.bias);

        this.output = sigmoidFunction(y);
    }

    private double sigmoidFunction(double x) {
        return (1.0 / (1.0 + (Math.exp(-this.lambda * x))));
    }
}
