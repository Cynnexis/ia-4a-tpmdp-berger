package pacman.environnementRL;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.stage.Stage;
import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pacman.elements.MazePacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;
import environnement.Etat;

import javax.swing.plaf.nimbus.State;

/**
 * Classe pour définir un etat du MDP pour l'environnement pacman avec QLearning tabulaire
 */
public class EtatPacmanMDPClassic implements Etat, Cloneable {
	
	private int distancePacmanGhost;
	private int distancePacmanFood;
	private int directionToClosestGhost;
	private int directionToClosestFood;
	private int directionPacman;
	
	public EtatPacmanMDPClassic(@NotNull final StateGamePacman state){
		// VOTRE CODE
		
		if (state.getNumberOfPacmans() != 1)
			System.err.println("EtatPacmanMDPClassic> Strange number of pacman: " + state.getNumberOfPacmans());
		
		// Get pacman
		StateAgentPacman pacman = state.getPacmanState(0);
		
		// Get the closest ghost
		Pair<StateAgentPacman, Double> ghostDistance = getClosestAgent(
				pacman,
				// Create anonymously the list of ghosts
				new ArrayList<StateAgentPacman>() {
					{
						for (int i = 0, maxi = state.getNumberOfGhosts(); i < maxi; i++)
							add(state.getGhostState(i));
					}
				},
				Distance.MANHATTAN
		);
		
		// Compute distance between pacman and the closest ghost
		setDistancePacmanGhost((int)
				// Cast from double to integer through round()
				Math.round(
						// Get the closest ghost from pacman
						Objects.requireNonNull(ghostDistance).getValue()
				)
		);
		
		// Compute the direction from pacman to the nearest ghost
		setDirectionToClosestGhost(getDirection(pacman, ghostDistance.getKey()));
		
		// Compute the distance between pacman and the closest food
		boolean[][] f = new boolean[state.getMaze().getSizeX()][state.getMaze().getSizeY()];
		for (int x = 0; x < state.getMaze().getSizeX(); x++) {
			for (int y = 0; y < state.getMaze().getSizeY(); y++) {
				f[x][y] = state.getMaze().isFood(x, y);
			}
		}
		ArrayList<StateAgentPacman> foods = convertBooleanArrayToStates(f);
		
		if (!foods.isEmpty()) {
			setDistancePacmanFood((int) Math.round(
					Objects.requireNonNull(getClosestAgent(
							state.getPacmanState(0),
							foods,
							Distance.MANHATTAN))
							.getValue()
			));
			
			// Compute the direction from pacman to the nearest food
			setDirectionToClosestFood(getDirection(pacman, Objects.requireNonNull(getClosestAgent(pacman, foods)).getKey()));
		}
		else {
			setDistancePacmanFood(0);
			setDirectionToClosestFood(MazePacman.STOP);
		}
		
		// Get pacman direction
		setDirectionPacman(pacman.getDirection());
		
		System.out.println(this);
	}
	public EtatPacmanMDPClassic(@NotNull EtatPacmanMDPClassic etat) {
		setDistancePacmanGhost(etat.getDistancePacmanGhost());
		setDistancePacmanFood(etat.getDistancePacmanFood());
		setDirectionToClosestGhost(etat.getDirectionToClosestGhost());
		setDirectionToClosestFood(etat.getDirectionToClosestFood());
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
	
	/**
	 * Get the direction (NORTH, SOUTH, EAST, WEST) towards `agent` from `reference`.
	 * @param reference The reference.
	 * @param agent The agent where `reference` wants to reach.
	 * @return Return the direction.
	 */
	@NotNull
	public int getDirection(@NotNull StateAgentPacman reference, @NotNull StateAgentPacman agent) {
		int deltaX = reference.getX() - agent.getX();
		int deltaY = reference.getY() - agent.getY();
		
		// If the reference is ON the agent, return "STOP"
		if (deltaX == 0 && deltaY == 0)
			return MazePacman.STOP;
		
		// If the X-difference is greater than the Y-difference, choose either EAST or WEST
		if (Math.abs(deltaX) > Math.abs(deltaY)) {
			if (deltaX > 0)
				return MazePacman.WEST;
			else
				return MazePacman.EAST;
		}
		// If the Y-difference is greater, choose either NORTH or SOUTH
		else {
			if (deltaY > 0)
				return MazePacman.NORTH;
			else
				return MazePacman.SOUTH;
		}
	}
	
	/**
	 * Convert a boolean map to a list of state.
	 * @param map The boolean map.
	 * @return A list of state.
	 */
	@NotNull
	public ArrayList<StateAgentPacman> convertBooleanArrayToStates(@NotNull boolean[][] map) {
		ArrayList<StateAgentPacman> states = new ArrayList<>();
		
		for (int x = 0; x < map.length; x++)
			for (int y = 0; y < map[x].length; y++)
				if (map[x][y])
					states.add(new StateAgentPacman(x, y));
		
		return states;
	}
	
	/**
	 * Convert the direction (integer) to its representation as string.
	 * @param direction The direction.
	 * @return Return the string representation of the direction. If the direction is invalid, return null.
	 */
	@Nullable
	public String directionCodeToString(int direction) {
		switch (direction) {
			case MazePacman.NORTH:
				return "▲";
			case MazePacman.EAST:
				return "▶";
			case MazePacman.SOUTH:
				return "▼";
			case MazePacman.WEST:
				return "◀";
			case MazePacman.STOP:
				return "⛔";
			default:
				return null;
		}
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
	
	public int getDirectionToClosestGhost() {
		return directionToClosestGhost;
	}
	
	public void setDirectionToClosestGhost(int directionToClosestGhost) {
		this.directionToClosestGhost = directionToClosestGhost;
	}
	
	public int getDirectionToClosestFood() {
		return directionToClosestFood;
	}
	
	public void setDirectionToClosestFood(int directionToClosestFood) {
		this.directionToClosestFood = directionToClosestFood;
	}
	
	public int getDirectionPacman() {
		return directionPacman;
	}
	
	public void setDirectionPacman(int directionPacman) {
		this.directionPacman = directionPacman;
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
				getDistancePacmanFood() == that.getDistancePacmanFood() &&
				getDirectionToClosestGhost() == that.getDirectionToClosestGhost() &&
				getDirectionToClosestFood() == that.getDirectionToClosestFood() &&
				getDirectionPacman() == that.getDirectionPacman();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getDistancePacmanGhost(), getDistancePacmanFood(), getDirectionToClosestGhost(), getDirectionToClosestFood(), getDirectionPacman());
	}
	
	@Override
	public String toString() {
		return "pac: " + directionCodeToString(getDirectionPacman()) + ", " +
				"ghost: " + directionCodeToString(getDirectionToClosestGhost()) + getDistancePacmanGhost() + ", " +
				"food: " + directionCodeToString(getDirectionToClosestFood()) + getDistancePacmanFood();
	}
}
