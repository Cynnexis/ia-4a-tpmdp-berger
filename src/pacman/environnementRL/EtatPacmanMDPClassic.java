package pacman.environnementRL;

import environnement.Etat;
import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pacman.elements.ActionPacman;
import pacman.elements.MazePacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe pour définir un etat du MDP pour l'environnement pacman avec QLearning tabulaire
 */
public class EtatPacmanMDPClassic implements Etat, Cloneable {
	
	private Integer distancePacmanGhostX;
	private Integer distancePacmanGhostY;
	private int distancePacmanFood;
	private Integer directionToClosestFood;
	
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
		if (computeDistance(pacman, ghostDistance.getKey(), Distance.MANHATTAN) <= 4) {
			setDistancePacmanGhostX(pacman.getX() - ghostDistance.getKey().getX());
			setDistancePacmanGhostY(pacman.getY() - ghostDistance.getKey().getY());
		}
		else {
			setDistancePacmanGhostX(null);
			setDistancePacmanGhostY(null);
		}
		
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
			int direction = getDirection(pacman, Objects.requireNonNull(getClosestAgent(pacman, foods)).getKey());
			if (state.isLegalMove(new ActionPacman(direction), pacman))
				setDirectionToClosestFood(getDirection(pacman, Objects.requireNonNull(getClosestAgent(pacman, foods)).getKey()));
			else
				setDirectionToClosestFood(null);
		}
		else {
			setDistancePacmanFood(0);
			setDirectionToClosestFood(MazePacman.STOP);
		}
		
		System.out.println(this);
	}
	public EtatPacmanMDPClassic(@NotNull EtatPacmanMDPClassic etat) {
		setDistancePacmanGhostX(etat.getDistancePacmanGhostX());
		setDistancePacmanGhostY(etat.getDistancePacmanGhostY());
		setDistancePacmanFood(etat.getDistancePacmanFood());
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
	public static double computeDistance(@NotNull StateAgentPacman a, @NotNull StateAgentPacman b, @NotNull Distance distance) {
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
	@NotNull
	@Contract("_, _, _ -> new")
	public static Pair<StateAgentPacman, Double> getClosestAgent(@NotNull StateAgentPacman reference, @NotNull List<StateAgentPacman> othersAgent, @NotNull Distance distanceType) {
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
	@NotNull
	@Contract("_, _ -> new")
	public static Pair<StateAgentPacman, Double> getClosestAgent(@NotNull StateAgentPacman reference, @NotNull List<StateAgentPacman> othersAgent) {
		return getClosestAgent(reference, othersAgent, Distance.EUCLIDEAN);
	}
	
	/**
	 * Get the direction (NORTH, SOUTH, EAST, WEST) towards `agent` from `reference`.
	 * @param reference The reference.
	 * @param agent The agent where `reference` wants to reach.
	 * @return Return the direction.
	 */
	public static int getDirection(@NotNull StateAgentPacman reference, @NotNull StateAgentPacman agent) {
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
	public static ArrayList<StateAgentPacman> convertBooleanArrayToStates(@NotNull boolean[][] map) {
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
	@Contract(pure = true)
	public static String directionCodeToString(int direction) {
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
	
	/**
	 * Invert the direction.
	 *
	 * Example:
	 * <pre>
	 * invertDirection(NORTH) = SOUTH
	 * invertDirection(SOUTH) = NORTH
	 * invertDirection(EAST) = WEST
	 * invertDirection(WEST) = EAST
	 * invertDirection(STOP) = STOP
	 * invertDirection(36) = 36
	 * </pre>
	 * @param direction The direction to invert.
	 * @return Return the invert.
	 */
	@Contract(pure = true)
	public static int invertDirection(int direction) {
		switch (direction) {
			case MazePacman.NORTH:
				return MazePacman.SOUTH;
			case MazePacman.EAST:
				return MazePacman.WEST;
			case MazePacman.SOUTH:
				return MazePacman.NORTH;
			case MazePacman.WEST:
				return MazePacman.EAST;
			default:
				return direction;
		}
	}
	
	/**
	 * Find the closest agent in the given direction.
	 * @param reference The reference.
	 * @param otherAgents The other agents. `reference` must not be in this list.
	 * @param direction The direction where to search.
	 * @return Return the closest agent in the given direction and its distance. If there is no such agent, or the direction is wrong, return null as key.
	 */
	@NotNull
	@Contract("_, _, _, _ -> new")
	public static Pair<StateAgentPacman, Double> getClosestAgentInDirection(@NotNull StateAgentPacman reference, @NotNull List<StateAgentPacman> otherAgents, int direction, @NotNull Distance distanceType) {
		StateAgentPacman closestAgent = null;
		double minDistance = Double.MAX_VALUE;
		
		for (StateAgentPacman agent : otherAgents) {
			if (getDirection(reference, agent) == direction) {
				double distance = computeDistance(reference, agent, distanceType);
				
				if (distance < minDistance) {
					minDistance = distance;
					closestAgent = agent;
				}
			}
		}
		
		return new Pair<>(closestAgent, minDistance);
	}
	
	/* GETTERS & SETTERS */
	
	@Nullable
	public Integer getDistancePacmanGhostX() {
		return distancePacmanGhostX;
	}
	
	public void setDistancePacmanGhostX(@Nullable Integer distancePacmanGhostX) {
		this.distancePacmanGhostX = distancePacmanGhostX;
	}
	
	@Nullable
	public Integer getDistancePacmanGhostY() {
		return distancePacmanGhostY;
	}
	
	public void setDistancePacmanGhostY(@Nullable Integer distancePacmanGhostY) {
		this.distancePacmanGhostY = distancePacmanGhostY;
	}
	
	public int getDistancePacmanFood() {
		return distancePacmanFood;
	}
	
	public void setDistancePacmanFood(int distancePacmanFood) {
		this.distancePacmanFood = distancePacmanFood;
	}
	
	@Nullable
	public Integer getDirectionToClosestFood() {
		return directionToClosestFood;
	}
	
	public void setDirectionToClosestFood(@Nullable Integer directionToClosestFood) {
		this.directionToClosestFood = directionToClosestFood;
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
	@Contract(value = "null -> false", pure = true)
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EtatPacmanMDPClassic)) return false;
		EtatPacmanMDPClassic that = (EtatPacmanMDPClassic) o;
		return Objects.equals(getDistancePacmanGhostX(), that.getDistancePacmanGhostX()) &&
				Objects.equals(getDistancePacmanGhostY(), that.getDistancePacmanGhostY()) &&
				getDistancePacmanFood() == that.getDistancePacmanFood() &&
				Objects.equals(getDirectionToClosestFood(), that.getDirectionToClosestFood());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getDistancePacmanGhostX(), getDistancePacmanGhostY(), getDistancePacmanFood(), getDirectionToClosestFood());
	}
	
	
	@Override
	public String toString() {
		return "ghost: (" + getDistancePacmanGhostX() + " ; " + getDistancePacmanGhostY() + "), " +
				"food: " + (getDirectionToClosestFood() != null ? directionCodeToString(getDirectionToClosestFood()) : "(null)") + getDistancePacmanFood();
	}
}
