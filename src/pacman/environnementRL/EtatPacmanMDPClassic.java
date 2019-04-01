package pacman.environnementRL;

import java.text.SimpleDateFormat;
import java.util.*;

import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pacman.elements.MazePacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;
import environnement.Etat;
/**
 * Classe pour définir un etat du MDP pour l'environnement pacman avec QLearning tabulaire
 */
public class EtatPacmanMDPClassic implements Etat, Cloneable {
	
	private int distancePacmanGhost;
	private int distancePacmanFood;
	
	public EtatPacmanMDPClassic(@NotNull final StateGamePacman state){
		// VOTRE CODE
		
		if (state.getNumberOfPacmans() != 1)
			System.err.println("EtatPacmanMDPClassic> Strange number of pacman: " + state.getNumberOfPacmans());
		
		// Compute distance between pacman and the closest ghost
		setDistancePacmanGhost((int)
				// Cast from double to integer through round()
				Math.round(
						// Get the closest ghost from pacman
						Objects.requireNonNull(getClosestAgent(
								state.getPacmanState(0),
								// Create anonymously the list of ghosts
								new ArrayList<StateAgentPacman>() {
									{
										for (int i = 0, maxi = state.getNumberOfGhosts(); i < maxi; i++)
											add(state.getGhostState(i));
									}
								}
						)).getValue()
				)
		);
		
		// Compute the distance between pacman and the closest food
		setDistancePacmanGhost(state.getClosestDot(state.getPacmanState(0)));
	}
	public EtatPacmanMDPClassic(@NotNull EtatPacmanMDPClassic etat) {
		setDistancePacmanGhost(etat.getDistancePacmanGhost());
	}
	
	/* UTILITY METHODS */
	
	/**
	 * Compute the distance between two agent.
	 * @param a First agent.
	 * @param b Second agent.
	 * @param distance The type of distance to compute.
	 * @return Return the distance between `a` and `b`.
	 */
	public double computeDistance(@NotNull StateAgentPacman a, @NotNull StateAgentPacman b, @NotNull Distance distance) {
		switch (distance) {
			case EUCLIDEAN:
				return Math.sqrt(
						Math.pow(b.getX() - a.getX(), 2) +
								Math.pow(b.getY() - a.getY(), 2)
				);
			case MANHATTAN:
				return Math.abs(
						b.getX() - a.getX()
				) + Math.abs(
						b.getY() - a.getY()
				);
		}
		
		return 0.;
	}
	
	/**
	 * Find the closest agent in the list `othersAgent` to `reference`.
	 * @param reference The reference agent.
	 * @param othersAgent The others agent that must be parsed. `reference` must NOT be in this list.
	 * @param distanceType The type of distance to use. By default, the euclidean distance is used.
	 * @return Return the closest agent.
	 */
	@Nullable
	public Pair<StateAgentPacman, Double> getClosestAgent(@NotNull StateAgentPacman reference, @NotNull List<StateAgentPacman> othersAgent, @NotNull Distance distanceType) {
		StateAgentPacman closestAgent = null;
		double minDistance = Double.MAX_VALUE;
		
		for (StateAgentPacman agent : othersAgent) {
			double distance = computeDistance(reference, agent, distanceType);
			
			if (distance < minDistance) {
				minDistance = distance;
				closestAgent = agent;
			}
		}
		
		return new Pair<>(closestAgent, minDistance);
	}
	/**
	 * Find the closest agent in the list `othersAgent` to `reference`, using the euclidean distance.
	 * @param reference The reference agent.
	 * @param othersAgent The others agent that must be parsed. `reference` must NOT be in this list.
	 * @return Return the closest agent.
	 */
	@Nullable
	public Pair<StateAgentPacman, Double> getClosestAgent(@NotNull StateAgentPacman reference, @NotNull List<StateAgentPacman> othersAgent) {
		return getClosestAgent(reference, othersAgent, Distance.EUCLIDEAN);
	}
	
	/* GETTERS & SETTERS */
	
	public int getDistancePacmanGhost() {
		return distancePacmanGhost;
	}
	
	public void setDistancePacmanGhost(int distancePacmanGhost) {
		this.distancePacmanGhost = distancePacmanGhost;
	}
	
	public int getDistancePacmanFood() {
		return distancePacmanFood;
	}
	
	public void setDistancePacmanFood(int distancePacmanFood) {
		this.distancePacmanFood = distancePacmanFood;
	}
	
	/* OVERRIDES */
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch(CloneNotSupportedException ex) {
			ex.printStackTrace();
			return new EtatPacmanMDPClassic(this);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EtatPacmanMDPClassic)) return false;
		EtatPacmanMDPClassic that = (EtatPacmanMDPClassic) o;
		return getDistancePacmanGhost() == that.getDistancePacmanGhost() &&
				getDistancePacmanFood() == that.getDistancePacmanFood();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getDistancePacmanGhost(), getDistancePacmanFood());
	}
}
