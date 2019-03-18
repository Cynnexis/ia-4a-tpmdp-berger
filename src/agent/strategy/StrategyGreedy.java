package agent.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import agent.rlagent.RLAgent;
import environnement.Action;
import environnement.Etat;
/**
 * Strategie qui renvoit un choix aleatoire avec proba epsilon, un choix glouton (suit la politique de l'agent) sinon
 * @author lmatignon
 *
 */
public class StrategyGreedy extends StrategyExploration {
	/**
	 * parametre pour probabilite d'exploration
	 */
	protected double epsilon;
	private Random rand = new Random();
	
	public StrategyGreedy(RLAgent agent,double epsilon) {
		super(agent);
		this.epsilon = epsilon;
	}

	@Override
	public Action getAction(Etat e) {// renvoi null si e absorbant
		// Get the actions, sorted (the first is the best action)
		List<Action> actions = this.getAgent().getPolitique(e);
		if (this.agent.getActionsLegales(e).isEmpty() || actions.isEmpty())
			return null;
		
		// VOTRE CODE
		
		// If there is one action in the list, return it.
		if (actions.size() == 1)
			return actions.get(0);
		
		// If the random number generator choose a value between [0 ; ε]
		if (rand.nextDouble() <= this.epsilon) {
			actions.remove(0);
			return actions.get(rand.nextInt(actions.size()));
		}
		// Else, choose the best action
		else
			return actions.get(0);
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
		System.out.println("epsilon: " + epsilon);
	}
}
