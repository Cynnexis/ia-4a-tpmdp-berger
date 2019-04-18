package agent.rlapproxagent;

import agent.rlagent.QLearningAgent;
import agent.rlagent.RLAgent;
import environnement.Action;
import environnement.Environnement;
import environnement.Etat;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Agent qui apprend avec QLearning en utilisant approximation de la Q-valeur : 
 * approximation lineaire de fonctions caracteristiques 
 * 
 * @author laetitiamatignon
 *
 */
public class QLApproxAgent extends QLearningAgent{
	
	private FeatureFunction featureFunction;
	private double[] weights;
	
	public QLApproxAgent(double alpha, double gamma, @NotNull Environnement env, @NotNull FeatureFunction featureFunction) {
		super(alpha, gamma, env);
		
		// VOTRE CODE
		setFeatureFunction(featureFunction);
		
		// Init weights array
		weights = new double[featureFunction.getFeatureNb()];
		
		// Set all weights to zero
		Arrays.fill(weights, 0.);
	}
	
	@Override
	public double getQValeur(Etat e, Action a) {
		// VOTRE CODE
		return dotProduct(getFeatureFunction().getFeatures(e, a), getWeights());
	}
	
	@Override
	public void endStep(Etat e, Action a, Etat esuivant, double reward) {
		if (RLAgent.DISPRL)
			System.out.println("QL: mise a jour poids pour etat \n"+e+" action "+a+" etat' \n"+esuivant+ " r "+reward);
		//inutile de verifier si e etat absorbant car dans runEpisode et threadepisode 
		//arrete episode lq etat courant absorbant	
		
		// VOTRE CODE
		double[] phi = featureFunction.getFeatures(e, a);
		double max = getValeur(esuivant);
		double qvalue = getQValeur(e, a);
		
		for (int k = 0; k < weights.length; k++)
			weights[k] += getAlpha() * (reward + getGamma() * max - qvalue) * phi[k];
	}
	
	@Override
	public void reset() {
		super.reset();
		this.qvaleurs.clear();
		
		// Set all weights to zero
		Arrays.fill(weights, 0.);
		
		this.episodeNb =0;
		this.notifyObs();
	}
	
	@Override
	public void endEpisode() {
		super.endEpisode();
		System.out.println("Number of states: " + getFeatureFunction().getFeatureNb());
	}
	
	public double dotProduct(@NotNull double[] a, @NotNull double[] b) {
		if (a.length != b.length)
			throw new IllegalArgumentException("Cannot apply dot product on vector with different size: dim(a) = " + a.length + " ; dim(b) = " + b.length);
		
		double result = 0;
		
		for (int i = 0; i < a.length; i++)
			result += a[i] * b[i];
		
		return result;
	}
	
	/* GETTERS & SETTERS */
	
	@NotNull
	public FeatureFunction getFeatureFunction() {
		return featureFunction;
	}
	
	@NotNull
	public void setFeatureFunction(FeatureFunction featureFunction) {
		this.featureFunction = featureFunction;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	public void setWeights(double[] weights) {
		this.weights = weights;
	}
}
