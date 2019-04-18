package agent.rlapproxagent;

import environnement.Action;
import environnement.Etat;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Vecteur de fonctions caracteristiques phi_i(s,a): autant de fonctions caracteristiques que de paire (s,a),
 * <li> pour chaque paire (s,a), un seul phi_i qui vaut 1  (vecteur avec un seul 1 et des 0 sinon).
 * <li> pas de biais ici
 * @author laetitiamatignon
 */
public class FeatureFunctionIdentity implements FeatureFunction {
	
	// VOTRE CODE
	
	/**
	 * HashMap that maps the couple (etat, action) to an index where the '1' is in the vector
	 */
	private HashMap<Pair<Etat, Action>, Integer> indexes;
	
	/**
	 * The maximum index where '1' is so far in `indexes`
	 */
	private int maxIndex;
	
	private int nbDim;
	
	public FeatureFunctionIdentity(int _nbEtat, int _nbAction){
		// VOTRE CODE
		maxIndex = 0;
		//nbDim = (int) Math.ceil((double) (_nbEtat * _nbAction) / 2.);
		nbDim = _nbEtat * _nbAction;
		System.out.println("FeatureFunctionIdentity> nbDim = " + nbDim);
		indexes = new HashMap<>(nbDim);
	}
	
	@Override
	public int getFeatureNb() {
		// VOTRE CODE
		return nbDim;
	}

	@Override
	public double[] getFeatures(Etat e, Action a){
		// VOTRE CODE
		double[] features = new double[nbDim];
		
		Arrays.fill(features, 0);
		
		int index = getIndex(e, a);
		if (index >= features.length)
			throw new RuntimeException("Array length: " + features.length + ", index: " + index);
		
		features[index] = 1;
		return features;
	}
	
	/**
	 * Fetch the index where the '1' is in the vector for the given `e` and `a`. If there is no index, generate it such
	 * that it is unique.
	 * @param e Etat
	 * @param a Action
	 * @return Return the index.
	 */
	private int getIndex(Etat e, Action a) {
		Pair<Etat, Action> couple = new Pair<>(e, a);
		
		if (indexes.containsKey(couple))
			return indexes.get(couple);
		else {
			// Generate unique index
			indexes.put(couple, maxIndex++);
			return indexes.get(couple);
		}
	}
}
