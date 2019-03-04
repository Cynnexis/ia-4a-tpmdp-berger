package agent.planningagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.HashMapUtil;

import java.util.HashMap;

import environnement.Action;
import environnement.Etat;
import environnement.IllegalActionException;
import environnement.MDP;
import environnement.Action2D;


/**
 * Cet agent met a jour sa fonction de valeur avec value iteration 
 * et choisit ses actions selon la politique calculee.
 * @author laetitiamatignon
 *
 */
@SuppressWarnings("Duplicates")
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
		this.delta=0.0;
		
		// VOTRE CODE
		
		// Capture old V for delta
		HashMap<Etat,Double> oldV = new HashMap<>(getV());
		
		// Store all the results over the set of actions
		HashMap<Action, Double> results = new HashMap<>();
		
		for (Etat e : mdp.getEtatsAccessibles()) {
			if (!mdp.estAbsorbant(e)) {
				results.clear();
				
				for (Action a : mdp.getActionsPossibles(e)) {
					results.put(a, 0d);
					try {
						for (Etat etatSuivant : mdp.getEtatTransitionProba(e, a).keySet()) {
							if (!e.equals(etatSuivant)) {
								// Compute T
								double T = 0d;
								try {
									T = mdp.getEtatTransitionProba(e, a).get(etatSuivant);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								
								// Compute R
								double R = mdp.getRecompense(e, a, etatSuivant);
								double VsPrime = getV().getOrDefault(etatSuivant, 0d);
								
								// Compute final result
								results.put(a, results.getOrDefault(a, 0d) + T * (R + getGamma() * VsPrime));
							}
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
				getV().put(e, result);
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
		// TODO: VOTRE CODE
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
		
		List<Action> returnactions = new ArrayList<Action>();
		HashMap<Action, Double> results = new HashMap<>();
		
		if (!mdp.estAbsorbant(e)) {
			for (Action a : mdp.getActionsPossibles(e)) {
				results.put(a, 0d);
				try {
					for (Etat etatSuivant : mdp.getEtatTransitionProba(e, a).keySet()) {
						if (!e.equals(etatSuivant)) {
							// Compute T
							double T = 0d;
							try {
								T = mdp.getEtatTransitionProba(e, a).get(etatSuivant);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							// Compute R
							double R = mdp.getRecompense(e, a, etatSuivant);
							double VsPrime = getV().getOrDefault(etatSuivant, 0d);
							
							// Compute final result
							results.put(a, results.getOrDefault(a, 0d) + T * (R + getGamma() * VsPrime));
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			// Choose the best result such that it is maximized (according to a)
			double result = 0d;
			Action action = null;
			for (Action a : results.keySet()) {
				if (result < results.getOrDefault(a, 0d)) {
					action = a;
					result = results.getOrDefault(a, 0d);
				}
			}
			returnactions.add(action);
		}
		
		return returnactions;
	}
	
	@Override
	public void reset() {
		super.reset(); //reinitialise les valeurs de V
		// VOTRE CODE
		getV().clear();
		
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
		System.out.println("gamma= "+gamma);
		this.gamma = _g;
	}
}