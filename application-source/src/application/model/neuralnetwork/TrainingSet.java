package application.model.neuralnetwork;

/**
 * Class representate training set.
 *
 * @author Dizzy
 */
public class TrainingSet {

    private double[][] inputs = null;
    private double[][] expectedOutputs = null;

    public TrainingSet(double[][] inputs, double[][] expectedOutputs) {
        this.inputs = inputs;
        this.expectedOutputs = expectedOutputs;
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
    }
    
    public int[][] getEntireTS() {
        int patterns = inputs.length;
        int in = inputs[0].length;
        int out = expectedOutputs[0].length;
        int trainingSet[][] = new int[patterns][in + out];

            for (int i = 0; i < patterns; i++) {
                for (int j = 0; j < in; j++) {
                    trainingSet[i][j] = (int)inputs[i][j];
                }
                for (int j = 0; j < out; j++) {
                    trainingSet[i][j+in] = (int)expectedOutputs[i][j];
                }
            }
        return trainingSet;
    }
}
