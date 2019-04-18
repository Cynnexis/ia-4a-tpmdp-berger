package pacman.environnementRL;

import environnement.Etat;
import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pacman.elements.ActionPacman;
import pacman.elements.MazePacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;

import java.util.ArrayList;
import java.util.Objects;

import static pacman.Utils.*;

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
		ArrayList<StateAgentPacman> foods = convertBooleanArrayToStates(getFoods(state));
		
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
						setDirectionToClosestFood(pickRandomly(MazePacman.EAST, MazePacman.WEST));
					else
						setDirectionToClosestFood(pickRandomly(MazePacman.NORTH, MazePacman.SOUTH));
					
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
	
	/* METHODS */
	
	public boolean isLegalMove(int direction, @NotNull StateAgentPacman reference) {
		return state.isLegalMove(new ActionPacman(direction), reference);
	}
	public boolean isLegalMove(int direction) {
		return isLegalMove(direction, pacman);
	}
	
	public int getDimensions() {
		return state.getNumberOfPacmans() * (((int) Math.pow(2 * TILE_RADIUS_GHOST_DETECTOR + 1, 3)) * state.getNumberOfGhosts() + 5);
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
		return Objects.hash(getDistancePacmanGhosts(), getDirectionToClosestFood());
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
