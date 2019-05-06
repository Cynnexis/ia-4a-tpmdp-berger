package agent.rlapproxagent;

import javafx.util.Pair;
import pacman.elements.ActionPacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;
import pacman.environnementRL.Distance;
import pacman.environnementRL.EnvironnementPacmanMDPClassic;
import environnement.Action;
import environnement.Etat;

import java.util.ArrayList;
import java.util.Arrays;

import static pacman.Utils.*;
/**
 * Vecteur de fonctions caracteristiques pour jeu de pacman: 4 fonctions phi_i(s,a)
 *  
 * @author laetitiamatignon
 *
 */
public class FeatureFunctionPacman implements FeatureFunction {
	
	private double[] vfeatures;
	
	/**
	 * 5 avec NONE possible pour pacman, 4 sinon
	 * --> doit etre coherent avec EnvironnementPacmanRL::getActionsPossibles
	 */
	private static int NBACTIONS = 4;
	
	public FeatureFunctionPacman() {
		vfeatures = new double[4];
	}

	@Override
	public int getFeatureNb() {
		return vfeatures.length;
	}

	@Override
	public double[] getFeatures(Etat e, Action a) {
		reset();
		StateGamePacman state;
		//EnvironnementPacmanMDPClassic envipacmanmdp = (EnvironnementPacmanMDPClassic) e;

		//calcule pacman resulting position a partir de Etat e
		if (!(e instanceof StateGamePacman)) {
			System.out.println("erreur dans FeatureFunctionPacman::getFeatures n'est pas un StateGamePacman");
			return vfeatures;
		}
		
		state = (StateGamePacman) e;
		StateAgentPacman nextPacman= state.movePacmanSimu(0, new ActionPacman(a.ordinal()));
		 
		// VOTRE CODE
		
		// Bias
		vfeatures[0] = 1.;
		
		// Number of ghost that can reach pacman in one step at the next iteration
		vfeatures[1] = 0;
		for (int i = 0; i < state.getNumberOfGhosts(); i++) {
			StateAgentPacman ghost = state.getGhostState(i);
			
			if (computeDistance(nextPacman, ghost, Distance.MANHATTAN) <= 1)
				vfeatures[1]++;
		}
		
		// Is there a pacdot in pacman's position at next iteration?
		ArrayList<StateAgentPacman> foods = convertBooleanArrayToStates(getFoods(state));
		// Get the closest food and its distance from pacman
		Pair<StateAgentPacman, Double> closestFoodDistance = getClosestAgent(nextPacman, foods, Distance.MANHATTAN);
		StateAgentPacman closestFood = closestFoodDistance.getKey();
		double distancePacdot = closestFoodDistance.getValue();
		// phi[2] is a boolean
		vfeatures[2] = distancePacdot == 0 ? 1. : 0.;
		
		// If pacman eat a pacdot at next iteration, search for next closest pacdot
		/*if (distancePacdot == 0) {
			foods.remove(closestFood);
			distancePacdot = getClosestAgent(nextPacman, foods, Distance.MANHATTAN).getValue();
		}*/
		
		vfeatures[3] = distancePacdot / (double) (state.getMaze().getSizeX() * state.getMaze().getSizeY()/* - convertBooleanArrayToStates(getWalls(state)).size()*/);
		
		return vfeatures;
	}
	
	public void reset() {
		Arrays.fill(vfeatures, 0.);
	}
}
