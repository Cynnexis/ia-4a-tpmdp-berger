package agent.planningagent;

import environnement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cet agent met a jour sa fonction de valeur avec value iteration 
 * et choisit ses actions selon la politique calculee.
 * @author laetitiamatignon
 *
 */
//@SuppressWarnings("Duplicates")
public class ValueIterationAgent extends PlanningValueAgent{
	/**
	 * discount facteur
	 */
	protected double gamma;

	/**
	 * fonction de valeur des etats
	 */
	protected HashMap<Etat,Double> V;
	
	/**
	 * 
	 * @param gamma
	 * @param mdp
	 */
	public ValueIterationAgent(double gamma, MDP mdp) {
		super(mdp);
		this.gamma = gamma;
		
		// Initialisation
		this.V = new HashMap<Etat, Double>();
		for (Etat etat : this.mdp.getEtatsAccessibles())
			V.put(etat, 0d);
	}
	public ValueIterationAgent(MDP mdp) {
		this(0.9,mdp);
	}
	
	/**
	 * Compute a part of V(e) by calculating all the sum of states according to a given action `a`.
	 * @param results The list that maps from an action to the result of doing this action. It is passed by reference,
	 *                which means it is the result of the function.
	 * @param e The state `e` in V(e).
	 * @return Return the best result contained in `results`.
	 */
	private double computeVActions(HashMap<Action, Double> results, Etat e) {
		for (Action a : mdp.getActionsPossibles(e)) {
			results.put(a, 0d);
			try {
				Map<Etat, Double> proba = mdp.getEtatTransitionProba(e, a);
				for (Etat etatSuivant : proba.keySet()) {
					assert(proba.getOrDefault(etatSuivant, -1d) > 0);
					
					// Compute T, R and V_{k-1}(s')
					double T = proba.get(etatSuivant);
					double R = mdp.getRecompense(e, a, etatSuivant);
					double VsPrime = getV().getOrDefault(etatSuivant, 0d);
					
					// Compute final result
					results.put(a, results.getOrDefault(a, 0d) + T * (R + getGamma() * VsPrime));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		// Choose the best result such that it is maximized (according to a)
		double result = 0d;
		for (Action a : results.keySet())
			if (result < results.getOrDefault(a, 0d))
				result = results.getOrDefault(a, 0d);
			
		return result;
	}
	
	/**
	 * 
	 * Mise a jour de V: effectue UNE iteration de value iteration (calcule V_k(s) en fonction de V_{k-1}(s'))
	 * et notifie ses observateurs.
	 * Ce n'est pas la version inplace (qui utilise la nouvelle valeur de V pour mettre a jour ...)
	 */
	@Override
	public void updateV(){
		//delta est utilise pour detecter la convergence de l'algorithme
		//Dans la classe mere, lorsque l'on planifie jusqu'a convergence, on arrete les iterations        
		//lorsque delta < epsilon 
		//Dans cette classe, il  faut juste mettre a jour delta
		this.delta = 0.0;
		
		// VOTRE CODE
		
		// Capture old V for delta
		HashMap<Etat,Double> oldV = new HashMap<>(getV());
		
		// Store all the results over the set of actions
		HashMap<Action, Double> results = new HashMap<>();
		
		for (Etat e : mdp.getEtatsAccessibles()) {
			if (!mdp.estAbsorbant(e)) {
				results.clear();
				
				getV().put(e, computeVActions(results, e));
			}
		}
		
		// Compute delta
		for (Etat e : mdp.getEtatsAccessibles()) {
			double r = Math.abs(getV().getOrDefault(e, 0d) - oldV.getOrDefault(e, 0d));
			if (this.delta < r)
				this.delta = r;
		}
		
		System.out.println("updateV> delta = " + this.delta);
		
		//mise a jour de vmax et vmin utilise pour affichage du gradient de couleur:
		//vmax est la valeur max de V pour tout s
		//vmin est la valeur min de V pour tout s
		
		vmax = 0d;
		vmin = 0d;
		for (Etat e : mdp.getEtatsAccessibles()) {
			if (vmax < getV().getOrDefault(e, 0d))
				vmax = getV().getOrDefault(e, 0d);
			
			if (vmin > getV().getOrDefault(e, 0d))
				vmin = getV().getOrDefault(e, 0d);
		}
		
		/* ****************** laisser cette notification a la fin de la methode	****************** */
		this.notifyObs();
	}
	
	
	/**
	 * renvoi l'action executee par l'agent dans l'etat e 
	 * Si aucune actions possibles, renvoi Action2D.NONE
	 */
	@Override
	public Action getAction(Etat e) {
		// VOTRE CODE
		List<Action> actions = getPolitique(e);
		
		if (actions != null) {
			if (actions.size() > 0)
				return actions.get(0);
			else
				return Action2D.NONE;
		}
		else
			return Action2D.NONE;
	}


	@Override
	public double getValeur(Etat e) {
		//Renvoie la valeur de l'Etat e, c'est juste un getter, ne calcule pas la valeur ici
        //(la valeur est calculée dans updateV)
		// VOTRE CODE
		
		return getV().getOrDefault(e, 0d);
	}
	/**
	 * renvoi action(s) de plus forte(s) valeur(s) dans etat 
	 * (plusieurs actions sont renvoyees si valeurs identiques, liste vide si aucune action n'est possible)
	 */
	@Override
	public List<Action> getPolitique(Etat e) {
		// VOTRE CODE
		
		// retourne action de meilleure valeur dans e selon V,
		// retourne liste vide si aucune action legale (etat absorbant)
		
		List<Action> returnActions = new ArrayList<Action>();
		HashMap<Action, Double> results = new HashMap<>();
		
		if (!mdp.estAbsorbant(e)) {
			
			double result = computeVActions(results, e);
			
			// Now that we know the best value, search for every actions with that same result
			for (Action a : results.keySet())
				if (a != null && results.getOrDefault(a, 0d) == result)
					returnActions.add(a);
		}
		
		return returnActions;
	}
	
	@Override
	public void reset() {
		super.reset(); //reinitialise les valeurs de V
		// VOTRE CODE
		getV().clear();
		for (Etat e : mdp.getEtatsAccessibles())
			getV().put(e, 0d);
		
		this.notifyObs();
	}
	
	public HashMap<Etat,Double> getV() {
		return V;
	}
	
	public double getGamma() {
		return gamma;
	}
	
	@Override
	public void setGamma(double _g){
		this.gamma = _g;
		System.out.println("gamma= "+gamma);
	}
}