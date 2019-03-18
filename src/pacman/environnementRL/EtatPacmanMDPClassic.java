package pacman.environnementRL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pacman.elements.MazePacman;
import pacman.elements.StateAgentPacman;
import pacman.elements.StateGamePacman;
import environnement.Etat;
/**
 * Classe pour définir un etat du MDP pour l'environnement pacman avec QLearning tabulaire

 */
public class EtatPacmanMDPClassic implements Etat, Cloneable {
	
	/** TODO: Minimiser le plus possible les attributs de cette classe, en plundering les attrbiuts des classes
	 * MazePacman et StateAgentPacman s'il le faut.
	 **/
	private MazePacman maze;
	private StateAgentPacman pacmansState;
	private StateAgentPacman ghostsState;
	private int foodEaten;
	private int capsulesEaten;
	private int ghostsEaten;
	private int score;
	
	public EtatPacmanMDPClassic(@NotNull final StateGamePacman state){
		// VOTRE CODE
		setMaze(state.getMaze());
		setPacmansState(state.getNumberOfPacmans() > 0 ? state.getPacmanState(state.getNumberOfPacmans() - 1) : null);
		setGhostsState(state.getNumberOfGhosts() > 0 ? state.getGhostState(state.getNumberOfGhosts() - 1) : null);
		setFoodEaten(state.getFoodEaten());
		setCapsulesEaten(state.getCapsulesEaten());
		setGhostsEaten(state.getGhostsEaten());
		setScore(state.getScore());
	}
	
	/* GETTERS & SETTERS */
	
	public MazePacman getMaze() {
		return maze;
	}
	
	public void setMaze(MazePacman maze) {
		this.maze = maze;
	}
	
	public StateAgentPacman getPacmansState() {
		return pacmansState;
	}
	
	public void setPacmansState(StateAgentPacman pacmansState) {
		this.pacmansState = pacmansState;
	}
	
	public StateAgentPacman getGhostsState() {
		return ghostsState;
	}
	
	public void setGhostsState(StateAgentPacman ghostsState) {
		this.ghostsState = ghostsState;
	}
	
	public int getFoodEaten() {
		return foodEaten;
	}
	
	public void setFoodEaten(int foodEaten) {
		this.foodEaten = foodEaten;
	}
	
	public int getCapsulesEaten() {
		return capsulesEaten;
	}
	
	public void setCapsulesEaten(int capsulesEaten) {
		this.capsulesEaten = capsulesEaten;
	}
	
	public int getGhostsEaten() {
		return ghostsEaten;
	}
	
	public void setGhostsEaten(int ghostsEaten) {
		this.ghostsEaten = ghostsEaten;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	/* OVERRIDES */
	
	@Override
	public Object clone() {
		EtatPacmanMDPClassic clone = null;
		try {
			// On recupere l'instance a renvoyer par l'appel de la 
			// methode super.clone()
			clone = (EtatPacmanMDPClassic)super.clone();
		} catch(CloneNotSupportedException cnse) {
			// Ne devrait jamais arriver car nous implementons 
			// l'interface Cloneable
			cnse.printStackTrace(System.err);
		}
		
		// on renvoie le clone
		return clone;
	}
	
	@Override
	@Contract(value = "null -> false", pure = true)
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EtatPacmanMDPClassic)) return false;
		EtatPacmanMDPClassic that = (EtatPacmanMDPClassic) o;
		return getFoodEaten() == that.getFoodEaten() &&
				getCapsulesEaten() == that.getCapsulesEaten() &&
				getGhostsEaten() == that.getGhostsEaten() &&
				getScore() == that.getScore() &&
				Objects.equals(getMaze(), that.getMaze()) &&
				Objects.equals(getPacmansState(), that.getPacmansState()) &&
				Objects.equals(getGhostsState(), that.getGhostsState());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getMaze(), getPacmansState(), getGhostsState(), getFoodEaten(), getCapsulesEaten(),
				getGhostsEaten(), getScore());
	}
	
	@Override
	public String toString() {
		return "EtatPacmanMDPClassic{" +
				"maze=" + maze +
				", pacmansState=" + pacmansState +
				", ghostsState=" + ghostsState +
				", foodEaten=" + foodEaten +
				", capsulesEaten=" + capsulesEaten +
				", ghostsEaten=" + ghostsEaten +
				", score=" + score +
				'}';
	}
}
