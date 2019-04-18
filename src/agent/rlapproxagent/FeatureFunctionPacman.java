package agent.rlapproxagent;

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
public class FeatureFunctionPacman implements FeatureFunction{
	private double[] vfeatures;
	
	/**
	 * 5 avec NONE possible pour pacman, 4 sinon
	 * --> doit etre coherent avec EnvironnementPacmanRL::getActionsPossibles
	 */
	private static int NBACTIONS = 4;
	
	public FeatureFunctionPacman() {
	}

	@Override
	public int getFeatureNb() {
		return 4;
	}

	@Override
	public double[] getFeatures(Etat e, Action a) {
		vfeatures = new double[4];
		StateGamePacman stategamepacman ;
		//EnvironnementPacmanMDPClassic envipacmanmdp = (EnvironnementPacmanMDPClassic) e;

		//calcule pacman resulting position a partir de Etat e
		if (e instanceof StateGamePacman)
			stategamepacman = (StateGamePacman) e;
		else{
			System.out.println("erreur dans FeatureFunctionPacman::getFeatures n'est pas un StateGamePacman");
			return vfeatures;
		}
	
		StateAgentPacman pacmanstate_next= stategamepacman.movePacmanSimu(0, new ActionPacman(a.ordinal()));
		 
		// TODO: VOTRE CODE
		// Bias
		vfeatures[0] = 1;
		
		// Number of ghost that can reach pacman in one step at the next iteration
		vfeatures[1] = 0;
		for (int i = 0; i < stategamepacman.getNumberOfGhosts(); i++) {
			StateAgentPacman ghost = stategamepacman.getGhostState(i);
			
			if (computeDistance(pacmanstate_next, ghost, Distance.MANHATTAN) <= 1)
				vfeatures[1]++;
		}
		
		// Is there a pacdot in pacman's position at next iteration?
		ArrayList<StateAgentPacman> foods = convertBooleanArrayToStates(getFoods(stategamepacman));
		double distancePacdot = getClosestAgent(pacmanstate_next, foods, Distance.MANHATTAN).getValue();
		vfeatures[2] = distancePacdot == 0 ? 1. : 0.;
		vfeatures[3] = distancePacdot / (double) (stategamepacman.getMaze().getSizeX() * stategamepacman.getMaze().getSizeY());
		
		System.out.println("\tvfeature = (" + Arrays.toString(vfeatures) + ")");
		
		return vfeatures;
	}

	public void reset() {
		vfeatures = new double[4];
	}
}
