/**
 *
 * @author Dizzy
 */
package application.model.neuralnetwork.backpropagation;

/**
 * Class representing a synapse - the connection of neurons.
 */
public class Synapse {

    private static int counter = 0; // autoincrement id
    private int id;
    private Neuron fromNeuron; // from
    private Neuron toNeuron; // to
    private double weight = 0; // synaptic weight
    private double prevDeltaWeight = 0; // previous weight; information for the μ (momentum)
    private double deltaWeight = 0; // weight gain

    public Synapse(Neuron fromNeuron, Neuron toNeuron) {
        this.id = Synapse.counter;
        Synapse.counter++;

        this.fromNeuron = fromNeuron;
        this.toNeuron = toNeuron;
    }

    public Synapse(int id, Neuron fromNeuron, Neuron toNeuron, double weight) {
        this.id = id;
        this.fromNeuron = fromNeuron;
        this.toNeuron = toNeuron;
        this.weight = weight;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        Synapse.counter = counter;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Neuron getFromNeuron() {
        return fromNeuron;
    }

    public void setFromNeuron(Neuron fromNeuron) {
        this.fromNeuron = fromNeuron;
    }

    public Neuron getToNeuron() {
        return toNeuron;
    }

    public void setToNeuron(Neuron toNeuron) {
        this.toNeuron = toNeuron;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getPrevDeltaWeight() {
        return prevDeltaWeight;
    }

    public void setPrevDeltaWeight() {
        this.prevDeltaWeight = this.deltaWeight;
    }

    public double getDeltaWeight() {
        return deltaWeight;
    }

    public void setDeltaWeight(double deltaWeight) {
        this.deltaWeight = deltaWeight;
    }
}
