package agent.rlagent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javafx.util.Pair;
import environnement.Action;
import environnement.Environnement;
import environnement.Etat;

/**
 * Renvoi 0 pour valeurs initiales de Q
 *
 * @author laetitiamatignon
 */
public class QLearningAgent extends RLAgent {
	/**
	 * format de memorisation des Q valeurs: utiliser partout setQValeur car cette methode notifie la vue
	 */
	protected HashMap<Etat, HashMap<Action, Double>> qvaleurs;
	
	//AU CHOIX: vous pouvez utiliser une Map avec des Pair pour clés si vous préférez
	//protected HashMap<Pair<Etat,Action>,Double> qvaleurs;
	
	/**
	 * @param alpha
	 * @param gamma
	 * @param _env
	 */
	public QLearningAgent(double alpha, double gamma, Environnement _env) {
		super(alpha, gamma, _env);
		qvaleurs = new HashMap<>();
	}
	
	/**
	 * renvoi action(s) de plus forte(s) valeur(s) dans l'etat e
	 * (plusieurs actions sont renvoyees si valeurs identiques)
	 * renvoi liste vide si aucunes actions possibles dans l'etat (par ex. etat absorbant)
	 */
	@Override
	public List<Action> getPolitique(Etat e) {
		// retourne action de meilleures valeurs dans e selon Q : utiliser getQValeur()
		// retourne liste vide si aucune action legale (etat terminal)
		List<Action> actions = new ArrayList<>();
		
		if (this.getActionsLegales(e).size() == 0) {//etat  absorbant; impossible de le verifier via environnement
			System.out.println("aucune action legale");
			return actions;
		}
		
		// VOTRE CODE
		double result = getValeur(e);
		
		for (Action a : this.getActionsLegales(e)) {
			if (a != null && getQValeur(e, a) >= result)
				actions.add(a);
		}
		
		return actions;
	}
	
	@Override
	public double getValeur(Etat e) {
		// VOTRE CODE
		
		double max = 0.;
		HashMap<Action, Double> submap = qvaleurs.getOrDefault(e, new HashMap<>());
		
		// For all action in `submap` (given by e1)
		for (Action a : submap.keySet()) {
			// If the given key leads to a null value, delete it
			if (submap.getOrDefault(a, null) == null)
				submap.remove(a);
			// Else, compare to max
			else {
				if (submap.get(a) > max)
					max = submap.get(a);
			}
		}
		
		return max;
	}
	
	@Override
	public double getQValeur(Etat e, Action a) {
		// VOTRE CODE
		if (!qvaleurs.containsKey(e))
			qvaleurs.put(e, new HashMap<>());
		
		if (!qvaleurs.get(e).containsKey(a)) {
			qvaleurs.get(e).put(a, 0.);
			return 0.;
		}
		
		return qvaleurs.get(e).get(a);
	}
	
	
	@Override
	public void setQValeur(Etat e, Action a, double d) {
		// VOTRE CODE
		
		// Get the sub-hashmap within qvaleurs(e). If it does not exist, instantiate it
		HashMap<Action, Double> submap = qvaleurs.getOrDefault(e, new HashMap<>());
		
		// Add the new value
		submap.put(a, d);
		
		// Add it to the hashmap qvaleurs
		qvaleurs.put(e, submap);
		
		// mise a jour vmax et vmin pour affichage du gradient de couleur:
		// vmax est la valeur de max pour tout s de V
		// vmin est la valeur de min pour tout s de V
		this.vmin = this.vmax = 0;
		
		// For all state in `qvaleurs`
		for (Etat e1 : qvaleurs.keySet()) {
			// If the given key leads to a null value, delete it
			if (qvaleurs.getOrDefault(e1, null) == null)
				qvaleurs.remove(e1);
			// Else, iterate over the action on the given submap
			else {
				submap = qvaleurs.get(e1);
				
				// For all action in `submap` (given by e1)
				for (Action a1 : submap.keySet()) {
					// If the given key leads to a null value, delete it
					if (submap.getOrDefault(a1, null) == null)
						submap.remove(a1);
					// Else, compare to vmin and vmax
					else {
						if (submap.get(a1) < this.vmin)
							this.vmin = submap.get(a1);
						
						if (submap.get(a1) > this.vmax)
							this.vmax = submap.get(a1);
					}
				}
			}
		}
		
		this.notifyObs();
	}
	
	/**
	 * mise a jour du couple etat-valeur (e,a) apres chaque interaction <etat e,action a, etatsuivant esuivant, recompense reward>
	 * la mise a jour s'effectue lorsque l'agent est notifie par l'environnement apres avoir realise une action.
	 * @param e
	 * @param a
	 * @param esuivant
	 * @param reward
	 */
	@Override
	public void endStep(Etat e, Action a, Etat esuivant, double reward) {
		if (RLAgent.DISPRL)
			System.out.println("QL mise a jour etat=" + e + ", action=" + a + ", etat'=" + esuivant + ", r=" + reward);
		
		// VOTRE CODE
		setQValeur(e, a, (1 - getAlpha()) * getQValeur(e, a) + getAlpha() * (reward + getGamma() * getValeur(esuivant)));
		
		//System.out.println("Nombre d'état: " + qvaleurs.keySet().size());
	}
	
	@Override
	public Action getAction(Etat e) {
		this.actionChoisie = this.stratExplorationCourante.getAction(e);
		return this.actionChoisie;
	}
	
	@Override
	public void reset() {
		super.reset();
		// VOTRE CODE
		
		qvaleurs.clear();
		
		this.episodeNb = 0;
		this.notifyObs();
	}
}
