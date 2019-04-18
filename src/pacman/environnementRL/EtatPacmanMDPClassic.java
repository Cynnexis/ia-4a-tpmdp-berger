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

import java.util.*;

/**
 * Classe pour définir un etat du MDP pour l'environnement pacman avec QLearning tabulaire
 */
public class EtatPacmanMDPClassic implements Etat, Cloneable {
	
	public static final int TILE_RADIUS_GHOST_DETECTOR = 3;
	
	/**
	 * List of all the distances from pacman to close ghosts. Each item of the list is a pair (x ; y) that corresponds
	 * to the distance on x-axis and y-axis.
	 */
	private ArrayList<Pair<Integer, Integer>> distancePacmanGhosts;
	
	/**
	 * Direction from pacman to the closest food.
	 */
	private int directionToClosestFood;
	
	/**
	 * The current state game. It is NOT included in the hash code and the equals method.
	 */
	private StateGamePacman state;
	
	/**
	 * The coordinates of pacman in the grid. It is NOT included in the hash code and the equals method.
	 */
	private StateAgentPacman pacman;
	
	public EtatPacmanMDPClassic(@NotNull final StateGamePacman state){
		// VOTRE CODE
		this.state = state;
		
		if (state.getNumberOfPacmans() != 1)
			System.err.println("EtatPacmanMDPClassic> Strange number of pacman: " + state.getNumberOfPacmans());
		
		// Get pacman
		pacman = state.getPacmanState(0);
		
		// Get the closest ghosts and compute their distances from pacman, and the direction
		distancePacmanGhosts = new ArrayList<>();
		
		for (int i = 0; i < state.getNumberOfGhosts(); i++) {
			StateAgentPacman ghost = state.getGhostState(i);
			// If the distance between pacman and the ghost is less or equal to 4, add it to the list
			if (computeDistance(pacman, ghost, Distance.MANHATTAN) <= TILE_RADIUS_GHOST_DETECTOR) {
				// Compute the distance
				distancePacmanGhosts.add(new Pair<>(
						pacman.getX() - ghost.getX(),
						pacman.getY() - ghost.getY()
				));
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
			// Compute the direction from pacman to the nearest food
			int direction = getDirection(pacman, Objects.requireNonNull(getClosestAgent(pacman, foods)).getKey());
			if (isLegalMove(direction))
				setDirectionToClosestFood(direction);
			else {
				// If the direction is not legal, take another one randomly, and perpendicular to `direction`:
				int iter = 0;
				do {
					if (direction == MazePacman.NORTH || direction == MazePacman.SOUTH)
						setDirectionToClosestFood(new Random().nextBoolean() ? MazePacman.EAST : MazePacman.WEST);
					else
						setDirectionToClosestFood(new Random().nextBoolean() ? MazePacman.NORTH : MazePacman.SOUTH);
					
					iter++;
					if (iter >= 100) {
						setDirectionToClosestFood(MazePacman.STOP);
						break;
					}
				} while (!isLegalMove(getDirectionToClosestFood()));
			}
		}
		else
			setDirectionToClosestFood(MazePacman.STOP);
	}
	public EtatPacmanMDPClassic(@NotNull EtatPacmanMDPClassic etat) {
		setDistancePacmanGhosts(etat.getDistancePacmanGhosts());
		setDirectionToClosestFood(etat.getDirectionToClosestFood());
		setState(etat.getState());
		setPacman(etat.getPacman());
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
	
	public boolean isLegalMove(int direction, @NotNull StateAgentPacman reference) {
		return state.isLegalMove(new ActionPacman(direction), reference);
	}
	public boolean isLegalMove(int direction) {
		return isLegalMove(direction, pacman);
	}
	
	/* GETTERS & SETTERS */
	
	@NotNull
	public ArrayList<Pair<Integer, Integer>> getDistancePacmanGhosts() {
		return distancePacmanGhosts;
	}
	
	public void setDistancePacmanGhosts(@NotNull ArrayList<Pair<Integer, Integer>> distancePacmanGhosts) {
		this.distancePacmanGhosts = distancePacmanGhosts;
	}
	
	public int getDirectionToClosestFood() {
		return directionToClosestFood;
	}
	
	public void setDirectionToClosestFood(int directionToClosestFood) {
		this.directionToClosestFood = directionToClosestFood;
	}
	
	@NotNull
	public StateGamePacman getState() {
		return state;
	}
	
	public void setState(@NotNull StateGamePacman state) {
		this.state = state;
	}
	
	@NotNull
	public StateAgentPacman getPacman() {
		return pacman;
	}
	
	public void setPacman(@NotNull StateAgentPacman pacman) {
		this.pacman = pacman;
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
		return getDistancePacmanGhosts().equals(that.getDistancePacmanGhosts()) &&
				Objects.equals(getDirectionToClosestFood(), that.getDirectionToClosestFood());
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		
		if (!getDistancePacmanGhosts().isEmpty()) {
			for (Pair<Integer, Integer> ghost : getDistancePacmanGhosts()) {
				if (ghost != null) {
					result = prime * result + (ghost.getKey() != null ? ghost.getKey() : 0);
					result = prime * result + (ghost.getValue() != null ? ghost.getValue() : 0);
				}
			}
		}
		
		result = prime * result + getDirectionToClosestFood();
		return result;
		//return Objects.hash(getDistancePacmanGhosts(), getDirectionToClosestFood());
	}
	
	@Override
	public String toString() {
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
					.append(")");
				
				if (i + 1 < maxi)
					strb.append(" ");
			}
		}
		
		strb.append(", ");
		
		strb.append("food: ")
			.append(directionCodeToString(getDirectionToClosestFood()));
		
		return strb.toString();
	}
}
