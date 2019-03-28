package agent.strategy;

import agent.rlagent.RLAgent;
import environnement.Action;
import environnement.Etat;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

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
	
	public StrategyGreedy(RLAgent agent, double epsilon) {
		super(agent);
		this.epsilon = epsilon;
	}
	@SuppressWarnings("CopyConstructorMissesField") // Do not include `rand` as copyable variable.
	public StrategyGreedy(@NotNull StrategyGreedy strategyGreedy) {
		this((RLAgent) strategyGreedy.getAgent(), strategyGreedy.getEpsilon());
	}
	
	/**
	 * Return the action to execute if the agent is in the state `e`. As it is the greedy strategy, this method uses a
	 * stochastic system to compute the action. There is a probability `epsilon` that the action will be randomly picked.
	 * @param e The state where the agent is.
	 * @return The action to execute accordingly to `e`.
	 */
	@Override
	@Nullable
	@Contract(pure = true)
	public Action getAction(Etat e) {// renvoi null si e absorbant
		// Get the actions, sorted (the first is the best action)
		List<Action> actions = this.getAgent().getPolitique(e);
		List<Action> actionsLegales = this.agent.getActionsLegales(e);
		
		if (actionsLegales.isEmpty())
			return null;
		
		// VOTRE CODE
		
		// If the random number generator choose a value between [0 ; ε]
		if (rand.nextDouble() <= getEpsilon() || actions.isEmpty())
			return actionsLegales.get(rand.nextInt(actionsLegales.size()));
		// Else, choose the best action
		else
			return actions.get(0);
	}
	
	/**
	 * The probability that the agent have a random behaviour. The smaller this variable is, the more deterministic the
	 * behaviour will be.
	 * @return The epsilon value.
	 */
	public double getEpsilon() {
		return epsilon;
	}
	
	/**
	 * Set the probability that make the agent having a random behaviour. The smaller this variable is, the more
	 * deterministic the behaviour will be.
	 * @param epsilon The epsilon value.
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
		System.out.println("epsilon: " + epsilon);
	}
}
