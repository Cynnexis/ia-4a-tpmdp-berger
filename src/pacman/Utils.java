package pacman;

import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pacman.elements.MazePacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;
import pacman.environnementRL.Distance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Utils {
	
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
	 * Find the closest agent in the list `othersAgent` to `reference`, using the euclidean distance.
	 * @param reference The reference agent.
	 * @param othersAgent The others agent that must be parsed. `reference` must NOT be in this list.
	 * @return Return the closest agent.
	 */
	@NotNull
	@Contract("_, _ -> new")
	public static Pair<StateAgentPacman, Double> getClosestAgent(@NotNull StateAgentPacman reference, @NotNull StateAgentPacman... othersAgent) {
		return getClosestAgent(reference, Arrays.asList(othersAgent), Distance.EUCLIDEAN);
	}
	
	/**
	 * Find the agents in the list `othersAgent` that are around `reference` in a specific radius.
	 * @param reference The reference agent.
	 * @param othersAgent The others agent that must be parsed. `reference` must NOT be in this list.
	 * @param tileRadius The radius (in tile) for the search.
	 * @param distanceType The type of distance to use. By default, the euclidean distance is used.
	 * @return Return the list of all agents in the radius with their distance to `reference`. Use a stream to extract
	 *         the desire element(s).
	 */
	public static ArrayList<Pair<StateAgentPacman, Double>> getAgentsInRadius(@NotNull StateAgentPacman reference, @NotNull Collection<StateAgentPacman> othersAgent, double tileRadius, @NotNull Distance distanceType) {
		ArrayList<Pair<StateAgentPacman, Double>> agents = new ArrayList<>();
		
		double distance;
		for (StateAgentPacman a : othersAgent) {
			if ((distance = computeDistance(reference, a, distanceType)) <= tileRadius) {
				agents.add(new Pair<>(a, distance));
			}
		}
		
		return agents;
	}
	/**
	 * Find the agents in the list `othersAgent` that are around `reference` in a specific radius. The euclidean distance is used.
	 * @param reference The reference agent.
	 * @param othersAgent The others agent that must be parsed. `reference` must NOT be in this list.
	 * @param tileRadius The radius (in tile) for the search.
	 * @return Return the list of all agents in the radius with their distance to `reference`. Use a stream to extract
	 *         the desire element(s).
	 */
	public static ArrayList<Pair<StateAgentPacman, Double>> getAgentsInRadius(@NotNull StateAgentPacman reference, @NotNull Collection<StateAgentPacman> othersAgent, double tileRadius) {
		return getAgentsInRadius(reference, othersAgent, tileRadius, Distance.EUCLIDEAN);
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
	
	public static ArrayList<StateAgentPacman> getGhosts(@NotNull final StateGamePacman state) {
		return new ArrayList<StateAgentPacman>(state.getNumberOfGhosts()) {
			{
				for (int i = 0; i < state.getNumberOfGhosts(); i++) {
					add(state.getGhostState(i));
				}
			}
		};
	}
	
	public static boolean[][] getFoods(@NotNull final StateGamePacman state) {
		try {
			return getBooleanArray(state, "isFood");
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean[][] getWalls(@NotNull final StateGamePacman state) {
		try {
			return getBooleanArray(state, "isWall");
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean[][] getBooleanArray(@NotNull final StateGamePacman state, @NotNull final String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method isThere = state.getMaze().getClass().getMethod(methodName, int.class, int.class);
		
		boolean[][] f = new boolean[state.getMaze().getSizeX()][state.getMaze().getSizeY()];
		for (int x = 0; x < state.getMaze().getSizeX(); x++) {
			for (int y = 0; y < state.getMaze().getSizeY(); y++) {
				f[x][y] = (boolean) isThere.invoke(state.getMaze(), x, y);
			}
		}
		
		return f;
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
	
	public static <T> T pickRandomly(@NotNull List<T> elements) {
		return elements.get(new Random().nextInt(elements.size()));
	}
	public static  <T> T pickRandomly(T... elements) {
		return pickRandomly(Arrays.asList(elements));
	}
}
