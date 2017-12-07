package nxcs;

import java.awt.*;
import java.util.List;


/**
 * The parameters of an NXCS system. These are much the same as the ones used in
 * XCS and thus the comments regarding their purpose are mostly taken from
 * Martin Butz's XCSJava. This class is designed to be mutable so as to allow creation of instances
 * which can be passed to multiple concurrent NXCS instances which may be running on different environments
 * with different parameters.
 *
 */
public class NXCSParameters {
	
	/**
	 * The number of bits in the state generated by this environment
	 */
	public int stateLength = 5;
	
	/**
	 * The number of actions the system can output
	 */
	public int numActions = 2;
	
	/**
	 * The minimum number of classifiers in the match set before covering occurs
	 */
	public int thetaMNA = 2;
	
	/**
	 * The initial value of the policy parameter in NXCS
	 */
	public double initialOmega = 0;

	/**
	 * The initial prediction value when generating a new classifier (e.g in
	 * covering).
	 */
	public double initialPrediction = 5;

	/**
	 * The initial prediction error value when generating a new classifier (e.g
	 * in covering).
	 */
	public double initialError = 0.01;

	/**
	 * The initial prediction value when generating a new classifier (e.g in
	 * covering).
	 */
	public double initialFitness = 0.01;

	/**
	 * The probability of using a don't care symbol in an allele when covering.
	 */
	public double pHash = 0.3;

	/**
	 * The discount rate in multi-step problems.
	 */
	public double gamma = 0.99;//0.71

	/**
	 * The fall of rate in the fitness evaluation.
	 */
	public double alpha = 0.1;

	/**
	 * The learning rate for updating fitness, prediction, prediction error, and
	 * action set size estimate in XCS's classifiers.
	 */
	public double beta = 0.2;

	/**
	 * Specifies the exponent in the power function for the fitness evaluation.
	 */
	public double nu = 5;

	/**
	 * Specifies the maximal number of micro-classifiers in the population.
	 */
	public int N = 500;

	/**
	 * The error threshold under which the accuracy of a classifier is set to
	 * one.
	 */
	public double e0 = 0.01;

	/**
	 * Specified the threshold over which the fitness of a classifier may be
	 * considered in its deletion probability.
	 */
	public int thetaDel = 25;

	/**
	 * The fraction of the mean fitness of the population below which the
	 * fitness of a classifier may be considered in its vote for deletion.
	 */
	public double delta = 0.1;

	/**
	 * The experience of a classifier required to be a subsumer.
	 */
	public int thetaSub = 20;
	
	/**
     * The threshold for the GA application in an action set.
     */
	public int thetaGA = 25;

	/**
	 * The probability of applying crossover in an offspring classifier (chi in
	 * literature, pX in XCSJava).
	 */
	public double crossoverRate = 0.8;

	/**
	 * The probability of mutating one allele and the action in an offspring
	 * classifier (mu in literature, pM in XCSJava).
	 */
	public double mutationRate = 0.04;

	/**
	 * Specifies if GA subsumption should be executed.
	 */
	public boolean doGASubsumption = true;

	/**
	 * Specifies if action set subsumption should be executed.
	 */
	public boolean doActionSetSubsumption = true;
	
	/**
	 * The maximum reward possible from the Environment. Used for scaling learning
	 * rates by the reward scheme
	 */
	public double rho0 = 1000;
	
	
	/**
	 * The weights is a two dimension array，each weight is a number pair (0.1,0.9)
	 */
	public List<Point> weights;

	public int[] obj1;
	
	

	 
}
