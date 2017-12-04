package nxcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.stream.IntStream;

/**
 * A classifier in the NXCS system. Note that this is only a small change from 
 * a classifier in XCS in that we have only added the `theta` field below. Note
 * that most of the methods in this class have a default access modifier - package private
 * for nicer encapsulation.
 *
 */
public class Classifier implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The global ID of classifiers. This is used to give 
	 * each classifier an individual "name"
	 */
	public static int GLOBAL_ID = 1;
	
	/**
	 * The ID of this classifier
	 */
	public int id;
	
	/**
	 * The action this classifier recommends
	 */
	public int action;
	
	/**
	 * The reward prediction of a classifier
	 */
	public double[] prediction = new double[2];//TOCHECK
	
	/**
	 * The reward error prediction of a classifier
	 */
	public double[] error = new double[2];
	
	/**
	 * The fitness of the classifier
	 */
	public double[] fitnessArray= new double[2];
	
	public double fitness;
	
	/**
	 * The policy parameter of the classifier
	 */
	public double omega;
	
	/**
	 * The experience (in timesteps) of this classifier. 
	 */
	public int experience;
	
	/**
	 * The timestamp of the last time the GA was run on a set this classifier was in
	 */
	public int timestamp;
	
	/**
	 * The average size of the action set this classifier was in
	 */
	public double averageSize;
	
	/** 
	 * The numerosity of the classifier. This is the number of micro-classifier this macro-classifier represents.
	 */
	public int numerosity;
	
	/**
	 * The condition of this classifier, made up a binary string with '#' wildcards
	 */
	public String condition;
	
	/**
	 * Construct a classifier with the default values, building a random condition
	 * @param params The parameters to use when building the classifier
	 */
	public Classifier(NXCSParameters params){
		id = GLOBAL_ID;
		GLOBAL_ID ++;
		
		//Set up the default settings
		action = XienceMath.randomInt(params.numActions);
		prediction[0] = params.initialPrediction;
		error[0] = params.initialError;
		fitnessArray[0] = params.initialFitness;
		prediction[1] = params.initialPrediction;
		error[1] = params.initialError;
		fitnessArray[1] = params.initialFitness;
		omega = params.initialOmega;
		experience = 0;
		timestamp = 0;
		averageSize = 1;
		numerosity = 1;
		
		//Build the condition
		StringBuilder build = new StringBuilder();
		for(int i = 0;i < params.stateLength;i ++){
			if(XienceMath.random() < params.pHash){
				build.append('#');
			}
			else if(XienceMath.random() < 0.5){
				build.append('0');
			}
			else{
				build.append('1');
			}
		}
		condition = build.toString();
	}
	
	/**
	 * Constructs a classifier with the default values, building the condition
	 * from the given state (For covering)
	 * @param params The parameters to use when building the classifier
	 * @param state The state that the condition of this classifier should match
	 */
	public Classifier(NXCSParameters params, String state){
		id = GLOBAL_ID;
		GLOBAL_ID ++;

		//Set up the default settings
		action = XienceMath.randomInt(params.numActions);
		prediction[0] = params.initialPrediction; //TODO:CHECK
		error[0] = params.initialError;
		fitnessArray[0] = params.initialFitness;
		prediction[1] = params.initialPrediction;
		error[1] = params.initialError;
		fitnessArray[1] = params.initialFitness;
		omega = params.initialOmega;
		experience = 0;
		timestamp = 0;
		averageSize = 1;
		numerosity = 1;
		
		//Build from the state
		StringBuilder build = new StringBuilder();
		for(int i = 0;i < params.stateLength;i ++){
			if(XienceMath.random() < params.pHash){
				build.append('#');
			}
			else{
				build.append(state.charAt(i));
			}
		}
		condition = build.toString();
	}
	
	/**
	 * Mutates this classifier based on the given values, reconstructing the condition 
	 * based on the given state and possibly changing the action. 
	 * 
	 * @see NXCSParameters#mutationRate
	 * @see NXCSParameters#numActions
	 * 
	 * @param state The state to mutate with. This mutation ensures that the condition
	 * of this classifier still matches this state
	 * @param numActions The number of actions in the system, so that we can choose a new one
	 * if necessary
	 */
	void mutate(String state, double mutationRate, int numActions){
		StringBuilder build = new StringBuilder();
		for(int i = 0;i < state.length();i ++){
			if(XienceMath.random() < mutationRate){
				if(condition.charAt(i) == '#'){
					build.append(state.charAt(i));
				}
				else{
					build.append('#');
				}
			}
			else{
				build.append(condition.charAt(i));
			}
		}
		
		condition = build.toString();
		
		if(XienceMath.random() < mutationRate){
			action = XienceMath.randomInt(numActions);
		}
	}
	
	/**
	 * Calculates the vote for this classifier to be deleted
	 * @see NXCSParameters#thetaDel
	 * @see NXCSParameters#delta
	 * 
	 * @param averageFitness The average fitness in the population
	 * of classifiers 
	 * @return The vote from this classifier for its deletion
	 */
	double deleteVote(double averageFitness, int thetaDel, double delta){
		double vote = averageSize * numerosity;
		if(experience > thetaDel && (fitnessArray[0]+fitnessArray[1])/2 / numerosity < delta * averageFitness){
			return vote * averageFitness / ((fitnessArray[0]+fitnessArray[1])/2 / numerosity);
		}
		return vote;
	}
	
	/**
	 * Returns whether this classifier has the requirements to subsume others
	 * @see NXCSParameters#thetaSub
	 * @see NXCSParameters#e0
	 * @return True if this classifier can subsume others, false otherwise
	 */
	boolean couldSubsume(double thetaSub, double e0){
		return experience > thetaSub && (error[0]+error[1])/2 < e0;
	}
	
	/**
	 * Returns whether this classifier is more general than the other. That is,
	 * it has more wildcards, and their conditions match.
	 * @param other The classifier to check this classifier is more general than
	 * @return True if this classifier is more general than the other
	 */
	boolean isMoreGeneral(Classifier other){
		long selfWildcards = condition.chars().filter(c -> c == '#').count();
		long otherWildcards = other.condition.chars().filter(c -> c == '#').count();
		
		if(selfWildcards <= otherWildcards){
			return false;
		}
		
		return IntStream.range(0, condition.length()).allMatch(i -> condition.charAt(i) == '#' || condition.charAt(i) == other.condition.charAt(i));
	}
	
	/**
	 * Returns whether this classifier can subsume the given one. That is, it has the ability to subsume,
	 * and it is more general than the other.
	 * @param other The classifier to check that this can subsume
	 * @see NXCSParameters#thetaSub
	 * @see NXCSParameters#e0
	 * @return True if this classifier can subsume the other, false otherwise
	 */
	boolean doesSubsume(Classifier other, int thetaSub, double e0){
		return action == other.action && couldSubsume(thetaSub, e0) && isMoreGeneral(other);
	}
	
	/**
	 * Performs a deepclone of this Classifier, returning the new Classifier
	 * @return The classifier which is an exact clone of this (barring the ID)
	 */
	Classifier deepcopy() {
		//Basically we serialize this and then deserialize into a new object
	    try {
	        final ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
	        final ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(this);
	        oos.close();

	        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
	        Classifier cl = (Classifier)ois.readObject();
	        cl.id = GLOBAL_ID;
	        GLOBAL_ID ++;
	        return cl;
	    }
	    catch (final Exception e) {
	        throw new RuntimeException("Cloning failed");
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode(){
		return id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object other){
		if(other == null)return false;
		if(!(other instanceof Classifier))return false;
		Classifier clas = (Classifier)other;
		
		return clas.id == id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString(){
		StringBuilder build = new StringBuilder();
		build.append(String.format("Classifier [%s = %d, Numerosity: %d]", condition, action,  numerosity));
		for (int i=0;i<error.length;i++){//TODO:
			build.append(String.format("Classifier [ Fitness: %3.2f, Error: %3.2f, Prediction: %3.2f]",  fitnessArray[i], error[i], prediction[i]));
		}
		
		
		return build.toString();
	}
}
