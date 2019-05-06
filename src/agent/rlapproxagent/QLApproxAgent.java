package agent.rlapproxagent;

import agent.rlagent.QLearningAgent;
import agent.rlagent.RLAgent;
import environnement.Action;
import environnement.Environnement;
import environnement.Etat;
import org.jetbrains.annotations.NotNull;
import pacman.elements.StateGamePacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static pacman.Utils.*;

/**
 * Agent qui apprend avec QLearning en utilisant approximation de la Q-valeur : 
 * approximation lineaire de fonctions caracteristiques 
 * 
 * @author laetitiamatignon
 *
 */
public class QLApproxAgent extends QLearningAgent {
	
	private FeatureFunction featureFunction;
	private double[] weights;
	
	// Hiding qvaleurs
	protected Object qvaleurs = null;
	
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
	public void setQValeur(Etat e, Action a, double d) {
		throw new RuntimeException("The method `setQValeur` cannot be called within `QLApproxAgent`.\n" +
				"Arguments:\n" +
				"\te = " + e + "\n" +
				"\ta = " + a + "\n" +
				"\td = " + d);
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
		
		if (DISPETAT) {
			if (e instanceof StateGamePacman) {
				String etat = String.format("%3d", ((StateGamePacman) e).getStep());
				System.out.println("\tvfeature(" + etat + ", " + directionCodeToString(a.ordinal()) + ") = (" + (int) phi[0] + ", " + (int) phi[1] + " ghost" + (phi[1] > 1 ? 's' : ' ') + ", " + (phi[2] == 1 ? "⚫" : "⚪") + ", " + phi[3] + "m" + ")");
			}
			/*else
				System.out.println("\tvfeature(e, " + directionCodeToString(a.ordinal()) + ") = (" + Arrays.toString(phi).replaceAll("[\\[\\]]", "") + ")");*/
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		
		// VOTRE CODE
		
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
			throw new IllegalArgumentException("Cannot apply dot product on vectors with different size: dim(a) = " + a.length + " ; dim(b) = " + b.length);
		
		double result = 0.;
		
		for (int i = 0; i < a.length; i++)
			result += a[i] * b[i];
		
		return result;
	}
	
	//region GETTERS & SETTERS
	
	@NotNull
	public FeatureFunction getFeatureFunction() {
		return featureFunction;
	}
	
	public void setFeatureFunction(@NotNull FeatureFunction featureFunction) {
		this.featureFunction = featureFunction;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	protected void setWeights(double[] weights) {
		this.weights = weights;
	}
	
	//endregion
}
