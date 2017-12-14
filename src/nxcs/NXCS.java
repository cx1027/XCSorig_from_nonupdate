package nxcs;

import nxcs.testbed.maze1_weighted_sum;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import xcs.PredictionArray;

/**
 * The main class of NXCS. This class stores the data of the current state of
 * the system, as well as the environment it is operating on. We opt to provide
 * a method for users to run a single iteration of the learning process,
 * allowing more fine grained control over inter-timestep actions such as
 * logging and stopping the process.
 *
 */
public class NXCS {
	/**
	 * The parameters of this system.
	 */
	private final NXCSParameters params;

	/**
	 * The Environment the system is acting on
	 */
	private final Environment env;

	/**
	 * The current population of this system
	 */
	private final List<Classifier> population;

	/**
	 * The current timestamp in this system
	 */
	private int timestamp;

	/**
	 * The action performed in the previous timestep of this system
	 */
	private int previousAction;

	/**
	 * The reward received in the previous timestep of this system
	 */
	private ActionPareto reward;

	/**
	 * The state this system was in in the previous timestep of this system
	 */
	private String previousState;

    private final int INVALID_ACTION = -1;

	/**
	 * Constructs an NXCS instance, operating on the given environment with the
	 * given parameters
	 * 
	 * @param _env
	 *            The environment this system is to operate on
	 * @param _params
	 *            The parameters this system is to use
	 */
	public NXCS(Environment _env, NXCSParameters _params) {
		if (_env == null)
			throw new IllegalArgumentException("Cannot operate on null environment");
		if (_params == null)
			throw new IllegalArgumentException("Cannot operate with null parameters");

		env = _env;
		params = _params;
		population = new ArrayList<Classifier>();
		timestamp = 0;
	}

	/**
	 * Prints the current population of this system to stdout
	 */
	public void printPopulation() {
		for (Classifier clas : population) {
			System.out.println(clas);
		}
	}

	/**
	 * Classifies the given state using the current knowledge of the system
	 * 
	 * @param state
	 *            The state to classify
	 * @return The class the system classifies the given state into
	 */
	public int classify(String state, Point weight) {
		if (state.length() != params.stateLength)
			throw new IllegalArgumentException(
					String.format("The given state (%s) is not of the correct length", state));
		List<Classifier> matchSet = population.stream().filter(c -> stateMatches(c.condition, state))
				.collect(Collectors.toList());
        double[] predictions = generateTotalPredictions_Norm(matchSet, weight);
        return getActionDeterministic(predictions);
	}

    //TODO:return the PA1[action]
    public double getSelectPA(int action, String state) {
        List<Classifier> matchSet = population.stream().filter(c -> stateMatches(c.condition, state))
                .collect(Collectors.toList());
        double[] predictions_reward = generatePredictions(matchSet, 1);
        return predictions_reward[action];
    }

	/**
	 * calculate hyper volume of current state
	 * 
	 * @param state
	 *            the state to classifier
	 **/
	// public double[] calHyper(String state) {
	// HyperVolumn hypervolumn = new HyperVolumn();
	// double[] hyper = { 0, 0, 0, 0 };
	// List<Classifier> C = generateMatchSet(state);
	// //loop each action
	// for (int action = 0; action < params.numActions; action++) {
	// final int act = action;
	// List<Classifier> A = C.stream().filter(b -> b.action ==
	// act).collect(Collectors.toList());
	//
	// Collections.sort(A, new Comparator<Classifier>() {
	// @Override
	// public int compare(Classifier o1, Classifier o2) {
	// return o1.fitness == o2.fitness ? 0 : (o1.fitness > o2.fitness ? 1 : -1);
	// }
	// });
	// if (A.size() == 0) {
	// hyper[act] = 0;
	// } else {
	// double hyperP = 0;//TODO:hypervolumn.calcHyperVolumn(A.get(A.size() -
	// 1).getV(), new Qvector(-10, -10));
	// hyper[act] = hyperP;
	// }
	// // System.out.println(hyperP);
	// }
	// return hyper;
	// }

	/**
	 * Runs a single iteration of the learning process of this NXCS instance
	 */
    public void runIteration(int finalStateCount, String previousState, Point weight, int i, double firstreward, maze1_weighted_sum maze) {
        // action
        int action = INVALID_ACTION;

		/* form [M] */
		List<Classifier> matchSet = generateMatchSet(previousState);
		/* select a */
		if (XienceMath.randomInt(params.numActions) <= 1) {
			double[] predictions = generateTotalPredictions(matchSet, weight);
			// select best actiton, not just explore
			action = getActionDeterministic(predictions);
		} else {
			action = XienceMath.randomInt(params.numActions);
		}

//		if (i < 40) {
//			action = 2;
//			System.out.println("action: " + action);
//		} else {
//			action = 1;
//			System.out.println("action: " + action);
//		}

		// System.out.println("****action:" + action);
		// if (i == 2) {
		// action = 1;
		//
		// }
		// if (i == 3) {
		// action = 1;
		//
		// }
		// if (i == 4) {
		// action = 1;
		//
		// }
		// if (i == 5) {
		// action = 1;
		//
		// }
		// if (i == 6) {
		// action = 1;
		//
		// }
		// if (i == 7) {
		// action = 0;
		//
		// }
		// if (i == 8) {
		// action = 0;
		//
		// }
		// if (i == 9) {
		// action = 0;
		//
		// }
		// if (i == 10) {
		// action = 0;
		//
		// }
		// if (i == 11) {
		// action = 0;
		//
		// }
		// if (i == 12) {
		// action = 0;
		//
		// }
		// if (i == 13) {
		// action = 0;
		//
		// }
		// if (i == 14) {
		// action = 0;
		//
		// }
		// if (i == 15) {
		// action = 0;
		//
		// }
		// if (i == 16) {
		// action = 0;
		// }
		// if (i == 17) {
		// action = 0;
		// }
		// if (i == 18) {
		// action = 0;
		// }
		// if (i == 19) {
		// action = 0;
		// }
		// if (i == 20) {
		// action = 0;
		// }

		/* get immediate reward */
		reward = env.getReward(previousState, action, firstreward);
		if (reward.getAction() == 5) { /*
										 * ???which means cant find F in 100,
										 * then reset in getReward()
										 */
			previousState = null;
            System.out.println("get stucked:classfiers:");

//			env.printOpenLocationClassifiers(finalStateCount, maze, this, weight, params.obj1[0]);
        }
		/* get current state */
		String curState = env.getState();

		/* if previous State!null, update [A]-1 and run ga */
		if (previousState != null) {
			/* updateSet include P calculation */
			List<Classifier> setA = updateSet(previousState, curState, action, reward);
			 runGA(setA, previousState);
		}

		/* update a-1=a */
		previousAction = action;
		/* update s-1=s */
		previousState = curState;
		/* update timestamp */
		timestamp = timestamp + 1;

	}

	/**
	 * Generates a set of classifiers that match the given state. Looks first
	 * for already generates ones in the population, but if the number of
	 * matches is less than thetaMNA, generates new classifiers with random
	 * actions and adds them to the match set. Reference: Page 7 'An Algorithmic
	 * Description of XCS'
	 * 
	 * @see NXCSParameters#thetaMNA
	 * @param state
	 *            the state to generate a match set for
	 * @return The set of classifiers that match the given state
	 */
	public List<Classifier> generateMatchSet(String state) {
		assert (state != null && state.length() == params.stateLength) : "Invalid state";
		List<Classifier> setM = new ArrayList<Classifier>();
		while (setM.size() == 0) {
			setM = population.stream().filter(c -> stateMatches(c.condition, state)).collect(Collectors.toList());
			if (setM.size() < params.thetaMNA) {
				Classifier clas = generateCoveringClassifier(state, setM);
				insertIntoPopulation(clas);
				deleteFromPopulation();
				setM.clear();
			}
		}

		assert (setM.size() >= params.thetaMNA);
		return setM;
	}

	/**
	 * Deletes a random classifier in the population, with probability of being
	 * deleted proportional to the fitness of that classifier. Reference: Page
	 * 14 'An Algorithmic Description of XCS'
	 */
	private void deleteFromPopulation() {
		int numerositySum = population.stream().collect(Collectors.summingInt(c -> c.numerosity));
		if (numerositySum <= params.N) {
			return;
		}

		double averageFitness = population.stream().collect(Collectors.summingDouble(c -> c.fitness)) / numerositySum;
		double[] votes = population.stream()
				.mapToDouble(c -> c.deleteVote(averageFitness, params.thetaDel, params.delta)).toArray();
		double voteSum = Arrays.stream(votes).sum();
		votes = Arrays.stream(votes).map(d -> d / voteSum).toArray();

		Classifier choice = XienceMath.choice(population, votes);
		if (choice.numerosity > 1) {
			choice.numerosity--;
		} else {
			population.remove(choice);
		}
	}

	/**
	 * Insert the given classifier into the population, checking first to see if
	 * any classifier already in the population is more general. If a more
	 * general classifier is found with the same action, that classifiers num is
	 * incremented. Else the given classifer is added to the population.
	 * Reference: Page 13 'An Algorithmic Description of XCS'
	 * 
	 * @param classifier
	 *            The classifier to add
	 */
	private void insertIntoPopulation(Classifier clas) {
		assert (clas != null) : "Cannot insert null classifier";
		Optional<Classifier> same = population.stream()
				.filter(c -> c.action == clas.action && c.condition.equals(clas.condition)).findFirst();
		if (same.isPresent()) {
			same.get().numerosity++;
		} else {
			population.add(clas);
		}
	}

	/**
	 * Generates a classifier with the given state as the condition and a random
	 * action not covered by the given set of classifiers Reference: Page 8 'An
	 * Algorithmic Description of XCS'
	 * 
	 * @param state
	 *            The state to use as the condition for the new classifier
	 * @param matchSet
	 *            The current covering classifiers
	 * @return The generated classifier
	 */
	private Classifier generateCoveringClassifier(String state, List<Classifier> matchSet) {
		assert (state != null && matchSet != null) : "Invalid parameters";
		assert (state.length() == params.stateLength) : "Invalid state length";

		Classifier clas = new Classifier(params, state);
		Set<Integer> usedActions = matchSet.stream().map(c -> c.action).distinct().collect(Collectors.toSet());
		Set<Integer> unusedActions = IntStream.range(0, params.numActions).filter(i -> !usedActions.contains(i)).boxed()
				.collect(Collectors.toSet());
		clas.action = unusedActions.iterator().next();
		clas.timestamp = timestamp;

		return clas;

	}

	/**
	 * Generates a normalized prediction array from the given match set, based
	 * on the softmax function.
	 * 
	 * @param setM
	 *            The match set to use consider for these predictions
	 * @return The prediction array calculated
	 */
	public double[] generatePredictions1(List<Classifier> setM, Point weight) {
		double[] predictions0 = new double[params.numActions];
		double[] fitnessSum0 = new double[params.numActions];
		if (setM.size() == 0)
			return predictions0;

		// Sum the policy parameter for each action
		for (Classifier clas : setM) {
			predictions0[clas.action] += clas.fitnessArray[0] * clas.prediction[0];
			fitnessSum0[clas.action] += clas.fitnessArray[0];
		}

		// sum:Take the exponential of each value??????
		double sum0 = 0;
		// for(int i = 0;i < predictions.length;i ++){
		// predictions[i] = XienceMath.clamp(predictions[i], -10, 10);
		// predictions[i] = Math.exp(predictions[i]);
		//
		// }

		// prediciton
		for (int i = 0; i < predictions0.length; i++) {
			predictions0[i] /= fitnessSum0[i];
			sum0 += predictions0[i];
		}
		// Normalize
		for (int i = 0; i < predictions0.length; i++) {
			predictions0[i] /= sum0;
		}

		assert (predictions0.length == params.numActions) : "Predictions are missing?";
		assert (Math.abs(Arrays.stream(predictions0).sum() - 1) <= 0.0001) : "Predictions not normalized";

		double[] predictions1 = new double[params.numActions];
		double[] fitnessSum1 = new double[params.numActions];
		if (setM.size() == 0)
			return predictions1;

		// Sum the policy parameter for each action
		for (Classifier clas : setM) {
			predictions1[clas.action] += clas.fitnessArray[0] * clas.prediction[0];
			fitnessSum1[clas.action] += clas.fitnessArray[0];
		}

		// sum:Take the exponential of each value??????
		double sum1 = 0;
		// for(int i = 0;i < predictions.length;i ++){
		// predictions[i] = XienceMath.clamp(predictions[i], -10, 10);
		// predictions[i] = Math.exp(predictions[i]);
		//
		// }

		// prediciton
		for (int i = 0; i < predictions1.length; i++) {
			predictions1[i] /= fitnessSum1[i];
			sum1 += predictions1[i];
		}
		// Normalize
		for (int i = 0; i < predictions1.length; i++) {
			predictions1[i] /= sum1;
		}

		assert (predictions1.length == params.numActions) : "Predictions are missing?";
		assert (Math.abs(Arrays.stream(predictions1).sum() - 1) <= 0.0001) : "Predictions not normalized";

		return getTotalPrediciton(weight, predictions0, predictions1);
	}

    public double[] generateTotalPredictions_Norm(List<Classifier> setM, Point weight) {
        double[] predictions0 = generatePredictions(setM, 0);
        // if(predictions0[0]==Double.NaN){
        // System.out.println("!!!!!!!!!!!!!NAN");
        // }
        double[] predictions1 = generatePredictions(setM, 1);
        // if(predictions1[0]==Double.NaN){
        // System.out.println("!!!!!!!!!!!!!NAN");
        // }

        double[] predictions0_nor = new double[4];
        double[] predictions1_nor = new double[4];
        //normalisation
        for (int i = 0; i < predictions0.length; i++) {
            predictions0_nor[i] = stepNor(predictions0[i], 100);
        }
        for (int i = 0; i < predictions1.length; i++) {
            predictions1_nor[i] = rewardNor(predictions1[i], 1000, 0);
        }

        double[] aaa = getTotalPrediciton(weight, predictions0_nor, predictions1_nor);
        if (aaa[0] == Double.NaN) {
            System.out.println("!!!!!!!!!!!!!NAN");
        }

        return aaa;
    }


	public double[] generateTotalPredictions(List<Classifier> setM, Point weight) {
		double[] predictions0 = generatePredictions(setM, 0);
		// if(predictions0[0]==Double.NaN){
		// System.out.println("!!!!!!!!!!!!!NAN");
		// }
		double[] predictions1 = generatePredictions(setM, 1);
		// if(predictions1[0]==Double.NaN){
		// System.out.println("!!!!!!!!!!!!!NAN");
		// }

		double[] aaa = getTotalPrediciton(weight, predictions0, predictions1);
		if (aaa[0] == Double.NaN) {
			System.out.println("!!!!!!!!!!!!!NAN");
		}

		return aaa;
	}

	public double[] generatePredictions(List<Classifier> setM, int obj) {
		double[] predictions = new double[params.numActions];
		double[] fitnessSum = new double[params.numActions];
		double sum = 0;
		if (setM.size() == 0)
			return predictions;

		// Sum the policy parameter for each action
		for (Classifier clas : setM) {
			predictions[clas.action] += clas.fitnessArray[obj] * clas.prediction[obj];
			fitnessSum[clas.action] += clas.fitnessArray[obj];
		}

		// prediciton
		for (int i = 0; i < predictions.length; i++) {
			predictions[i] /= fitnessSum[i];
			sum += predictions[i];
		}
		// Normalize
//		for (int i = 0; i < predictions.length; i++) {
//			predictions[i] /= sum;
//		}

		assert (predictions.length == params.numActions) : "Predictions are missing?";
//		assert (Math.abs(Arrays.stream(predictions).sum() - 1) <= 0.0001) : "Predictions not normalized";

		return predictions;
	}

	public double[] getTotalPrediciton(Point weight, double[] pred0, double[] pred1) {
		double[] totalPre = new double[pred0.length];
		for (int i = 0; i < pred0.length; i++) {
			totalPre[i] = weight.getX() * pred0[i] + weight.getY() * pred1[i];
		}
		return totalPre;
	}

	// return action with max PA
	public int getActionDeterministic(double[] PA) {
		return getMaxIndext(PA, getMaxPrediction(PA));
	}

	// return action with max PA
	public int getMaxIndext(double[] PA, double max) {
		int ret = -1;
		for (int i = 0; i < PA.length; i++)
			if (PA[i] == max) {
				ret = i;
				return ret;
			}
		return ret;
	}

	// return max PA
	public double getMaxPrediction(List<Classifier> setM, int obj) {
		double[] PA = generatePredictions(setM, obj);
		// return max PA
		// List b = Arrays.asList(PA);
		// return (double) Collections.max(b);
//		for (int i=0; i<PA.length;i++
//			 ) {
//			System.out.println(String.format("PA[%d] = %f",i,  PA[i]));
//		}
        return getMaxPrediction(PA);
	}

	// return max PA
	public double getMaxPrediction(double[] PA) {
		// List b = Arrays.asList(PA);
		// return (double) Collections.max(b);
        double max = Arrays.stream(PA).filter(d -> !Double.isNaN(d)).max().getAsDouble();
        return max;
	}

	// return min PA
	public double getMinPrediction(List<Classifier> setM, int obj) {
		double[] PA = generatePredictions(setM, obj);
		// return max PA
		// List b = Arrays.asList(PA);
		// return (double) Collections.max(b);
		return getMinPrediction(PA);
	}

	// return min PA
	public double getMinPrediction(double[] PA) {
		// List b = Arrays.asList(PA);
		// return (double) Collections.max(b);
		double max = Arrays.stream(PA).min().getAsDouble();
		return max;
	}

	/**
	 * Selects an action, stochastically, using the given predictions as
	 * probabilities for each action
	 * 
	 * @param predictions
	 *            The predictions to use to select the action
	 * @return The action selected
	 */
	// private int selectAction(double[] predictions){
	// return (int) XienceMath.choice(IntStream.range(0,
	// params.numActions).boxed().toArray(), predictions);
	// }

	private int selectAction(double[] predictions) {
		if (Math.random() > 0.5) {
			return getActionDeterministic(predictions);
		} else {
			return (int) XienceMath.choice(IntStream.range(0, params.numActions).boxed().toArray(), predictions);
		}
	}

	private int selectBestAction(double[] predictions) {
		if (Math.random() > 0.5) {
			return getActionDeterministic(predictions);
		} else {
			return (int) XienceMath.choice(IntStream.range(0, params.numActions).boxed().toArray(), predictions);
		}
	}

	/**
	 * Estimates the value for a state matched by the given match set
	 * 
	 * @param setM
	 *            The match set to estimate for
	 * @return The estimated maximum value of the state
	 */
	// private double valueFunctionEstimation(List<Classifier> setM){
	// double[] PA = generatePredictions(setM);
	// double ret = 0;
	// for(int i = 0;i < params.numActions;i ++){
	// final int index = i;
	// List<Classifier> setAA = setM.stream().filter(c -> c.action ==
	// index).collect(Collectors.toList());
	// double fitnessSum = setAA.stream().mapToDouble(c -> c.fitness).sum();
	// double predictionSum = setAA.stream().mapToDouble(c -> c.prediction *
	// c.fitness).sum();
	//
	// if(fitnessSum != 0)ret += PA[i] * predictionSum / fitnessSum;
	// }
	//
	// assert(!Double.isNaN(ret) && !Double.isInfinite(ret));
	//
	// return ret;
	// }

	/**
	 * Updates the match set/action set of the previous state
	 * 
	 * @see NXCSParameters#gamma
	 * @see NXCSParameters#rho0
	 * @see NXCSParameters#e0
	 * @see NXCSParameters#nu
	 * @see NXCSParameters#alpha
	 * @see NXCSParameters#beta
	 * @see NXCSParameters#doActionSetSubsumption
	 * @see Classifier#averageSize
	 * @see Classifier#error
	 * @see Classifier#prediction
	 * @see Classifier#fitness
	 * @see Classifier#omega
	 * @param previousState
	 *            The previous state of the system
	 * @param currentState
	 *            The current state of the system
	 * @param action
	 *            The action performed in the previous state of the system
	 * @param reward
	 *            The reward received from performing the given action in the
	 *            given previous state
	 * @return The action set of the previous state, with subsumption (possibly)
	 *         applied
	 */
	private List<Classifier> updateSet(String previousState, String currentState, int action, ActionPareto reward) {
		List<Classifier> previousMatchSet = generateMatchSet(previousState);

		/*
		 * for steps, to min Q Q=Q+beta(count-Q) if goal achieved Q=Q+beta(100
		 * or min+1 -Q) if goal not achieved
		 * 
		 * for reward 1 or 10 if goal achieved 0 if goal not achieved
		 * 
		 * narmalization
		 * 
		 */

		double[] P = new double[2]; // STEPS AND REWARDS
		// 0T:10 T0:1
		if (env.isEndOfProblem(currentState)) {
			// P = reward.getPareto().get(0)*w[0] +
			// Math.abs(reward.getPareto().get(1))*w[1];
			P[0] = 1;
			P[1] = reward.getPareto().get(1);
		} else {

			// update valueFunctionEstimation as max PA???
			double Qplus1 = 1 + params.gamma * getMinPrediction(generateMatchSet(currentState), 0);
			if (Qplus1 < 5) {// TODO:hardcode initial Q
				P[0] = Qplus1;
			} else {
				P[0] = 5;
			}

			P[1] = reward.getPareto().get(1) + params.gamma * getMaxPrediction(generateMatchSet(currentState), 1);
			// =1!!!!
			// System.out.println("temp:" + temp);
			// P = reward.getPareto().get(0)*w[0] +
			// Math.abs(reward.getPareto().get(1))*w[1] + params.gamma *
			// getMaxPrediction(generateMatchSet(currentState));
			// System.out.println("P:" + P);
		}

		// double deltaT = P - valueFunctionEstimation(previousMatchSet);
		List<Classifier> actionSet = previousMatchSet.stream().filter(cl -> cl.action == action)
				.collect(Collectors.toList());
		int setNumerosity = actionSet.stream().mapToInt(cl -> cl.numerosity).sum();

		// Update standard parameters
		for (Classifier clas : actionSet) {
			clas.experience++;
			for (int i = 0; i < clas.prediction.length; i++) {
				if (clas.experience < 1. / params.beta) {
					clas.prediction[i] = clas.prediction[i] + (P[i] - clas.prediction[i]) / clas.experience;

					// averageSize calculate should be just once
					if (i == 0) {
						clas.averageSize = clas.averageSize + (setNumerosity - clas.numerosity) / clas.experience;
					}
					clas.error[i] = clas.error[i]
							+ (Math.abs(P[i] - clas.prediction[i]) - clas.error[i]) / clas.experience;

//					 normalisation
//					if (i < 1) {
//						clas.prediction[i] = stepNor(clas.prediction[i], 100);
//					} else {
//						clas.prediction[i] = rewardNor(clas.prediction[i], 100, 0);
//					}

				} else {
					clas.prediction[i] = clas.prediction[i] + (P[i] - clas.prediction[i]) * params.beta;
					if (i == 0) {
						clas.averageSize = clas.averageSize + (setNumerosity - clas.numerosity) * params.beta;
					}
					clas.error[i] = clas.error[i] + (Math.abs(P[i] - clas.prediction[i]) - clas.error[i]) * params.beta;
					// normalisation
//					if (i < 1) {
//						clas.prediction[i] = stepNor(clas.prediction[i], 100);
//					} else {
//						clas.prediction[i] = rewardNor(clas.prediction[i], 100, 0);
//					}

				}
			}

		}

		// Update Fitness
		Map<Classifier, Double> kappa0 = actionSet.stream().collect(Collectors.toMap(cl -> cl,
				cl -> (cl.error[0] < params.e0) ? 1 : params.alpha * Math.pow(cl.error[0] / params.e0, -params.nu)));
		double accuracySum0 = kappa0.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().numerosity).sum();
		actionSet.forEach(cl -> cl.fitnessArray[0] += params.beta
				* (kappa0.get(cl) * cl.numerosity / accuracySum0 - cl.fitnessArray[0]));

		Map<Classifier, Double> kappa1 = actionSet.stream().collect(Collectors.toMap(cl -> cl,
				cl -> (cl.error[1] < params.e0) ? 1 : params.alpha * Math.pow(cl.error[1] / params.e0, -params.nu)));
		double accuracySum1 = kappa1.entrySet().stream()
				.mapToDouble(entry -> entry.getValue() * entry.getKey().numerosity).sum();
		actionSet.forEach(cl -> cl.fitnessArray[1] += params.beta
				* (kappa1.get(cl) * cl.numerosity / accuracySum1 - cl.fitnessArray[1]));

		actionSet.forEach(cl -> cl.fitness = (cl.fitnessArray[0] + cl.fitnessArray[1]) / 2); // TODO:

		if (params.doActionSetSubsumption) {
			return actionSetSubsumption(actionSet);
		}
		return actionSet;
	}

	// normalisation about steps
	public double stepNor(double q, double max) {
		return Math.abs((max + 1 - q) / max);
	}

	// normalisaton for final reward
	public double rewardNor(double q, double max, double min) {
		return (q - min) / (max - min);
	}

	/**
	 * Performs an action set subsumption, subsuming the action set into the
	 * most general of the classifiers. Reference: Page 15 'An Algorithmic
	 * Description of XCS'
	 * 
	 * @param setAA
	 *            The action set to subsume
	 * @return The updated action set
	 */
	private List<Classifier> actionSetSubsumption(List<Classifier> setA) {
		Classifier cl = setA.stream().reduce(null, (cl1, cl2) -> (!cl2.couldSubsume(params.thetaSub, params.e0)) ? cl1
				: (cl1 == null) ? cl2 : (cl1.isMoreGeneral(cl2) ? cl1 : cl2));

		if (cl != null) {
			List<Classifier> toRemove = new ArrayList<Classifier>();
			for (Classifier clas : setA) {
				if (cl.isMoreGeneral(clas)) {
					cl.numerosity = cl.numerosity + clas.numerosity;
					toRemove.add(clas);
				}
			}

			setA.removeAll(toRemove);
			population.removeAll(toRemove);
		}

		return setA;
	}

	/**
	 * Runs the genetic algorithm (assuming enough time has passed) in order to
	 * make new classifiers based on the ones currently in the action set
	 * Reference: Page 11 'An Algorithmic Description of XCS'
	 * 
	 * @see NXCSParameters#thetaGA
	 * @see NXCSParameters#mu
	 * @see NXCSParameters#chi
	 * @see NXCSParameters#doGASubsumption
	 * @param currentActionSet
//	 *            The current action set in this timestep
	 * @param state
	 *            The current state from the environment
	 */
	private void runGA(List<Classifier> setA, String state) {
		assert (setA != null && state != null) : "Invalid parameters";
		// assert(setA.size() > 0) : "No action set";
		if (setA.size() == 0)
			return;
		assert (state.length() == params.stateLength) : "Invalid state";
		if (timestamp - XienceMath.average(setA.stream().mapToDouble(cl -> cl.timestamp).toArray()) > params.thetaGA) {
			for (Classifier clas : setA) {
				clas.timestamp = timestamp;
			}

			double fitnessSum = setA.stream().mapToDouble(cl -> cl.fitness).sum();
			double[] p = setA.stream().mapToDouble(cl -> cl.fitness / fitnessSum).toArray();
			Classifier parent1 = XienceMath.choice(setA, p);
			Classifier parent2 = XienceMath.choice(setA, p);
			Classifier child1 = parent1.deepcopy();
			Classifier child2 = parent2.deepcopy();

			child1.numerosity = child2.numerosity = 1;
			child1.experience = child2.experience = 0;

			if (XienceMath.random() < params.crossoverRate) {
				crossover(child1, child2);
				for (int i = 0; i < 2; i++) {
					child1.prediction[i] = child2.prediction[i] = (parent1.prediction[i] + parent2.prediction[i]) / 2;
					child1.error[i] = child2.error[i] = 0.25 * (parent1.error[i] + parent2.error[i]) / 2;
					child1.fitnessArray[i] = child2.fitnessArray[i] = 0.1
							* (parent1.fitnessArray[i] + parent2.fitnessArray[i]) / 2;
				}
			}

			Classifier[] children = new Classifier[] { child1, child2 };
			for (Classifier child : children) {
				child.mutate(state, params.mutationRate, params.numActions);
				if (params.doGASubsumption) {
					if (parent1.doesSubsume(child, params.thetaSub, params.e0)) {
						parent1.numerosity++;
					} else if (parent2.doesSubsume(child, params.thetaSub, params.e0)) {
						parent2.numerosity++;
					} else {
						insertIntoPopulation(child);
					}
				} else {
					insertIntoPopulation(child);
				}
				deleteFromPopulation();
			}
		}
	}

	/**
	 * Checks whether the given condition matches the given state
	 * 
	 * @param condition
	 *            The condition to check
	 * @param state
	 *            The state to check against
	 * @return if condition[i] is '#' or state[i] for all i
	 */
	private boolean stateMatches(String condition, String state) {
		assert (condition != null && condition.length() == params.stateLength) : "Invalid condition";
		assert (state != null && state.length() == params.stateLength) : "Invalid state";
		return IntStream.range(0, condition.length())
				.allMatch(i -> condition.charAt(i) == '#' || condition.charAt(i) == state.charAt(i));
	}

	/**
	 * Performs a crossover between the two given conditions, updating both.
	 * Swaps a random number of bits between the two conditions.
	 * 
	 * @see NXCSParameters#chi
	 * @param child1
	 *            The first child to cross over
	 * @param child2
	 *            The second child to cross over
	 */
	private void crossover(Classifier child1, Classifier child2) {
		assert (child1 != null && child2 != null) : "Cannot crossover null child";
		int x = XienceMath.randomInt(params.stateLength);
		int y = XienceMath.randomInt(params.stateLength);
		if (x > y) {
			int tmp = x;
			x = y;
			y = tmp;
		}

		StringBuilder child1Build = new StringBuilder();
		StringBuilder child2Build = new StringBuilder();
		for (int i = 0; i < params.stateLength; i++) {
			if (i < x || i >= y) {
				child1Build.append(child1.condition.charAt(i));
				child2Build.append(child2.condition.charAt(i));
			} else {
				child1Build.append(child2.condition.charAt(i));
				child2Build.append(child1.condition.charAt(i));
			}
		}

		child1.condition = child1Build.toString();
		child2.condition = child2Build.toString();
	}
}
