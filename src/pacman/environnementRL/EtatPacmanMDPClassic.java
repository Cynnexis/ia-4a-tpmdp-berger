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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Classe pour définir un etat du MDP pour l'environnement pacman avec QLearning tabulaire
 */
public class EtatPacmanMDPClassic implements Etat, Cloneable {
	
	public static final int TILE_RADIUS_GHOST_DETECTOR = 4;
	
	/**
	 * The pacman's position
	 */
	private Pair<Integer, Integer> pacmanPos;
	
	/**
	 * List of all the distances from pacman to close ghosts. Each item of the list is a pair (x ; y) that corresponds
	 * to the distance on x-axis and y-axis.
	 */
	@NotNull
	private ArrayList<Pair<Integer, Integer>> distancePacmanGhosts;
	
	/**
	 * Direction from pacman to the close ghosts. The index is the same as `distancePacmanGhosts`.
	 */
	private ArrayList<Integer> directionToGhosts;
	
	/**
	 * Distance from pacman to closest dot food.
	 */
	private int distancePacmanFood;
	
	/**
	 * Direction from pacman to the closest food.
	 */
	private int directionToClosestFood;
	
	public EtatPacmanMDPClassic(@NotNull final StateGamePacman state){
		// VOTRE CODE
		
		if (state.getNumberOfPacmans() != 1)
			System.err.println("EtatPacmanMDPClassic> Strange number of pacman: " + state.getNumberOfPacmans());
		
		// Get pacman
		StateAgentPacman pacman = state.getPacmanState(0);
		
		// Get pacman's position
		pacmanPos = new Pair<>(pacman.getX(), pacman.getY());
		
		// Get the closest ghosts and compute their distances from pacman, and the direction
		distancePacmanGhosts = new ArrayList<>();
		directionToGhosts = new ArrayList<>();
		
		for (int i = 0; i < state.getNumberOfGhosts(); i++) {
			StateAgentPacman ghost = state.getGhostState(i);
			// If the distance between pacman and the ghost is less or equal to 4, add it to the list
			if (computeDistance(pacman, ghost, Distance.MANHATTAN) <= TILE_RADIUS_GHOST_DETECTOR) {
				distancePacmanGhosts.add(new Pair<>(
						pacman.getX() - ghost.getX(),
						pacman.getY() - ghost.getY()
				));
				
				int direction = getDirection(pacman, ghost);
				if (state.isLegalMove(new ActionPacman(direction), pacman))
					directionToGhosts.add(direction);
				else
					directionToGhosts.add(MazePacman.STOP);
			}
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
				setDirectionToClosestFood(direction);
			else
				setDirectionToClosestFood(MazePacman.STOP);
		}
		else {
			setDistancePacmanFood(0);
			setDirectionToClosestFood(MazePacman.STOP);
		}
		
		//System.out.println(this);
	}
	public EtatPacmanMDPClassic(@NotNull EtatPacmanMDPClassic etat) {
		setDistancePacmanGhosts(etat.getDistancePacmanGhosts());
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
	
	@NotNull
	public Pair<Integer, Integer> getPacmanPos() {
		return pacmanPos;
	}
	
	public void setPacmanPos(@NotNull Pair<Integer, Integer> pacmanPos) {
		this.pacmanPos = pacmanPos;
	}
	
	@NotNull
	public ArrayList<Pair<Integer, Integer>> getDistancePacmanGhosts() {
		return distancePacmanGhosts;
	}
	
	public void setDistancePacmanGhosts(@NotNull ArrayList<Pair<Integer, Integer>> distancePacmanGhosts) {
		this.distancePacmanGhosts = distancePacmanGhosts;
	}
	
	public int getDistancePacmanFood() {
		return distancePacmanFood;
	}
	
	@NotNull
	public ArrayList<Integer> getDirectionToGhosts() {
		return directionToGhosts;
	}
	
	public void setDirectionToGhosts(@NotNull ArrayList<Integer> directionToGhosts) {
		this.directionToGhosts = directionToGhosts;
	}
	
	public void setDistancePacmanFood(int distancePacmanFood) {
		this.distancePacmanFood = distancePacmanFood;
	}
	
	public int getDirectionToClosestFood() {
		return directionToClosestFood;
	}
	
	public void setDirectionToClosestFood(int directionToClosestFood) {
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
		return getPacmanPos().equals(that.getPacmanPos()) &&
				getDistancePacmanGhosts().equals(that.getDistancePacmanGhosts()) &&
				getDirectionToGhosts().equals(that.getDirectionToGhosts()) &&
				getDistancePacmanFood() == that.getDistancePacmanFood() &&
				Objects.equals(getDirectionToClosestFood(), that.getDirectionToClosestFood());
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		
		result = prime * result + (getPacmanPos().getKey() != null ? getPacmanPos().getKey() : 0);
		result = prime * result + (getPacmanPos().getValue() != null ? getPacmanPos().getValue() : 0);
		
		if (!getDistancePacmanGhosts().isEmpty()) {
			for (Pair<Integer, Integer> ghost : getDistancePacmanGhosts()) {
				if (ghost != null) {
					result = prime * result + (ghost.getKey() != null ? ghost.getKey() : 0);
					result = prime * result + (ghost.getValue() != null ? ghost.getValue() : 0);
				}
			}
		}
		
		if (!getDirectionToGhosts().isEmpty())
			for (Integer directionToGhost : getDirectionToGhosts())
				result = prime * result + (directionToGhost != null ? directionToGhost : 0);
		
		result = prime * result + getDistancePacmanFood();
		result = prime * result + getDirectionToClosestFood();
		return result;
		//return Objects.hash(getPacmanPos(), getDistancePacmanGhosts(), getDirectionToGhosts(), getDistancePacmanFood(), getDirectionToClosestFood());
	}
	
	@Override
	public String toString() {
		assert getDistancePacmanGhosts().size() == getDirectionToGhosts().size();
		
		StringBuilder strb = new StringBuilder();
		strb.append("ghost: ");
		
		if ((getDistancePacmanGhosts().isEmpty()))
			strb.append("(null)");
		else {
			for (int i = 0, maxi = getDistancePacmanGhosts().size(); i < maxi; i++) {
				strb.append("(")
					.append(getDistancePacmanGhosts().get(i).getKey())
					.append(" ; ")
					.append(getDistancePacmanGhosts().get(i).getValue())
					.append(")")
					.append(directionCodeToString(getDirectionToGhosts().get(i)));
				
				if (i + 1 < maxi)
					strb.append(" ");
			}
		}
		
		strb.append(", ");
		
		strb.append("food: ")
			.append(directionCodeToString(getDirectionToClosestFood()))
			.append(getDistancePacmanFood());
		
		return strb.toString();
	}
}
