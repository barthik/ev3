package application.model.neuralnetwork;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ART NN for filtering pattern.
 *
 * @author Dizzy
 */
public class ART {

    private int n = 0;   // Number of components in an input vector
    private int m = 0;  // Max number of clusters to be formed
    private int tempN = 0;   // Number if filter entire set
    private int patterns = 0; // Number of patterns
    private double vigilance = 0.7;
    private final double L = 2; // Constant, excelling unit; recommended value: L = 2
    private int pattern[][] = null; // Training patterns
    private double bw[][] = null;    //Bottom-up weights
    private double tw[][] = null;    //Top-down weights

    private int f1a[] = null;    //Input layer
    private int f1b[] = null;    //Interface layer
    private double f2[] = null;

    private boolean bestIsOne = true; // Winner to the cluster is vector with max 1; iff false with max 0
    private int clustersWinner[][] = null; // Winners of the comparasion  (of pattern) between each cluster components
    private int formedClusters = 0;

    private boolean entireTS = false;

    /**
     * Create ART NN and prepare it for filtering.
     *
     * @param ts pattern array
     * @param vigilance parameter vigilance
     * @param entireTS true for filtering entire TS (include outputs to
     * filtering)
     * @param bestIsOne true - winner to the cluster is vector with max 1; iff
     * false with max 0
     * @param inputs number of inputs
     */
    public ART(int[][] ts, double vigilance, boolean entireTS, boolean bestIsOne, int inputs) {
        this.pattern = ts;
        this.vigilance = vigilance;
        this.bestIsOne = bestIsOne;
        this.entireTS = entireTS;

        if (entireTS) {
            tempN = pattern[0].length;
        } else {
            tempN = inputs;
        }

        patterns = pattern.length;
        m = pattern.length;
        n = pattern[0].length;
        clustersWinner = new int[m][n];

        int defaultValue = 0;
        if (bestIsOne) {
            defaultValue = -1;
        } else {
            defaultValue = 1;
        }
        // Initialize cluster matrix
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < tempN; j++) {
                clustersWinner[i][j] = defaultValue;
            }
        }

        // Initialize bottom-up weight matrix
        bw = new double[m][tempN];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < tempN; j++) {
                bw[i][j] = 1.0 / (1.0 + tempN);
            }
        }

        // Initialize top-down weight matrix
        tw = new double[m][tempN];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < tempN; j++) {
                tw[i][j] = 1.0;
            }
        }

        f1a = new int[tempN];
        f1b = new int[tempN];
        f2 = new double[m];
    }

    public void shufflePatterns() {
        shuffleArray(pattern);
    }

    private void shuffleArray(int[][] array) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--)
        {
          int index = rnd.nextInt(i + 1);
          int[] a = array[index];
          array[index] = array[i];
          array[i] = a;
        }
    }

    private int patternNumOfOne(int patternNum) {
        int sum = 0;

        for (int i = 0; i < tempN; i++) {
            if (pattern[patternNum][i] == 1) {
                sum++;
            }
        }

        return sum;
    }

    private int patternNumOfZero(int patternNum) {
        int sum = 0;

        for (int i = 0; i < tempN; i++) {
            if (pattern[patternNum][i] == 0) {
                sum++;
            }
        }

        return sum;
    }

    private int clusterNumOfOne(int clusterNum) {
        int sum = 0;

        for (int i = 0; i < tempN; i++) {
            if (clustersWinner[clusterNum][i] == 1) {
                sum++;
            }
        }

        return sum;
    }

    private int clusterNumOfZero(int clusterNum) {
        int sum = 0;

        for (int i = 0; i < tempN; i++) {
            if (clustersWinner[clusterNum][i] == 0) {
                sum++;
            }
        }

        return sum;
    }

    private int vectorSum(int[] nodeArray) {
        int sum = 0;

        // Compute sum of input pattern.
        for (int i = 0; i < tempN; i++) {
            sum += nodeArray[i];
        }

        return sum;
    }

    private void updateWeights(int activationSum, int f2Max) {
        //Update bw(f2Max)
        for (int i = 0; i < tempN; i++) {
            bw[f2Max][i] = (L * (double) f1b[i]) / (L - 1.0 + (double) activationSum);
        }
        //Update tw(f2Max)
        for (int i = 0; i < tempN; i++) {
            tw[f2Max][i] = f1b[i];
        }
    }

    private boolean testForReset(int activationSum, int inputSum, int f2Max) {
        if ((double) activationSum / (double) inputSum >= vigilance) {
            return false; // Candidate is accepted.
        } else {
            f2[f2Max] = -1.0; // Inhibit.
            return true; // Candidate is rejected.
        }
    }

    private int maximum(double[] nodeArray) {
        int winner = 0;
        boolean foundNewWinner = false;
        boolean next = true;

        while (next) {
            foundNewWinner = false;
            for (int i = 0; i < m; i++) {
                if (i != winner) {             // Avoid self-comparison.
                    if (nodeArray[i] > nodeArray[winner]) {
                        winner = i;
                        foundNewWinner = true;
                    }
                }
            }

            if (foundNewWinner == false) {
                next = false;
            }
        }
        return winner;
    }

    /**
     * Launche the ART NN and compute output with one vector.
     *
     * @param vector Vector of TS
     */
    public void runForVector(int vector) {
        int inputSum = 0;
        int activationSum = 0;
        int f2Max = 0;
        boolean reset = true;

        reset = true;

        // Initialize f2 layer activations to 0.0
        for (int i = 0; i < m; i++) {
            f2[i] = 0.0;
        }

        // Input pattern() to f1 layer
        for (int i = 0; i < tempN; i++) {
            f1a[i] = pattern[vector][i];
        }

        // Compute sum of input pattern
        inputSum = vectorSum(f1a);

        if (inputSum != 0) { // Include zero vector

            // Compute activations for each node in the f1 layer
            // Send input signal from f1a to the fF1b layer
            for (int i = 0; i < tempN; i++) {
                f1b[i] = f1a[i];
            }

            // Compute net input for each node in the f2 layer.
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < tempN; j++) {
                    f2[i] += bw[i][j] * (double) f1a[j];
                }
            }

            while (reset == true) {
                // Determine the largest value of the f2 nodes
                f2Max = maximum(f2);

                // Recompute the f1a to f1b activations (perform AND function)
                for (int i = 0; i < tempN; i++) {
                    f1b[i] = f1a[i] * (int) Math.floor(tw[f2Max][i]);
                }

                // Compute sum of input pattern
                activationSum = vectorSum(f1b);

                reset = testForReset(activationSum, inputSum, f2Max);

            }

            // Use number of patterns for training
            if (vector < patterns) {
                updateWeights(activationSum, f2Max);
            }

        }

        if (f2Max > formedClusters) {
            formedClusters++;
        }

        if (bestIsOne) { // best is with more 1
            if (patternNumOfOne(vector) > clusterNumOfOne(f2Max)) {
                for (int i = 0; i < n; i++) { // save output TS
                    clustersWinner[f2Max][i] = pattern[vector][i];
                }
            }
        }
        if (!bestIsOne) { // best is with more 0
            if (patternNumOfZero(vector) > clusterNumOfZero(f2Max)) {
                for (int i = 0; i < n; i++) { // save output TS
                    clustersWinner[f2Max][i] = pattern[vector][i];
                }
            }
        }
    }

    /**
     * Launches the ART NN and compute output TS.
     *
     * @return Retrun running time in ms.
     */
    public long start() {
        int inputSum = 0;
        int activationSum = 0;
        int f2Max = 0;
        boolean reset = true;

        long loopStart = System.currentTimeMillis();

        for (int vector = 0; vector < patterns; vector++) {
            reset = true;

            // Initialize f2 layer activations to 0.0
            for (int i = 0; i < m; i++) {
                f2[i] = 0.0;
            }

            // Input pattern() to f1 layer
            for (int i = 0; i < tempN; i++) {
                f1a[i] = pattern[vector][i];
            }

            // Compute sum of input pattern
            inputSum = vectorSum(f1a);

            if (inputSum != 0) { // Include zero vector

                // Compute activations for each node in the f1 layer
                // Send input signal from f1a to the fF1b layer
                for (int i = 0; i < tempN; i++) {
                    f1b[i] = f1a[i];
                }

                // Compute net input for each node in the f2 layer.
                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < tempN; j++) {
                        f2[i] += bw[i][j] * (double) f1a[j];
                    }
                }

                while (reset == true) {
                    // Determine the largest value of the f2 nodes
                    f2Max = maximum(f2);

                    // Recompute the f1a to f1b activations (perform AND function)
                    for (int i = 0; i < tempN; i++) {
                        f1b[i] = f1a[i] * (int) Math.floor(tw[f2Max][i]);
                    }

                    // Compute sum of input pattern
                    activationSum = vectorSum(f1b);

                    reset = testForReset(activationSum, inputSum, f2Max);

                }

                // Use number of patterns for training
                if (vector < patterns) {
                    updateWeights(activationSum, f2Max);
                }

            }

            if (f2Max > formedClusters) {
                formedClusters++;
            }

            if (bestIsOne) { // best is with more 1
                if (patternNumOfOne(vector) > clusterNumOfOne(f2Max)) {
                    for (int i = 0; i < n; i++) { // save output TS
                        clustersWinner[f2Max][i] = pattern[vector][i];
                    }
                }
            }
            if (!bestIsOne) { // best is with more 0
                if (patternNumOfZero(vector) > clusterNumOfZero(f2Max)) {
                    for (int i = 0; i < n; i++) { // save output TS
                        clustersWinner[f2Max][i] = pattern[vector][i];
                    }
                }
            }

            //System.out.println("Filtered "+vector+" from "+patterns);
        }

        long loopEnd = System.currentTimeMillis();

        //free memory
        pattern = null;
        bw = null;
        tw = null;
        f1a = null;
        f1b = null;
        f2 = null;

        return loopEnd - loopStart;
    }

    /**
     * Return number of formed clusters after filtering TS.
     *
     * @return int
     */
    public int getNumOfClusters() {
        return formedClusters + 1;
    }

    /**
     * Return final training set after filtering TS
     *
     * @return int[][] TS
     */
    public int[][] getFiteredTS() {
        int[][] temp = new int[formedClusters + 1][n];
        for (int i = 0; i < formedClusters + 1; i++) {
            for (int j = 0; j < n; j++) {
                temp[i][j] = clustersWinner[i][j];

            }
        }

        return temp;
    }
}
