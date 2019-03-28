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
	
	private int pacmanX;
	private int pacmanY;
	private int pacmanDirection;
	private int ghostX;
	private int ghostY;
	private int ghostDirection;
	private boolean[][] walls;
	private boolean[][] food;
	
	public EtatPacmanMDPClassic(@NotNull final StateGamePacman state){
		// VOTRE CODE
		setPacmanX(state.getPacmanState(0).getX());
		setPacmanY(state.getPacmanState(0).getY());
		setPacmanDirection(state.getPacmanState(0).getDirection());
		setGhostX(state.getGhostState(0).getX());
		setGhostY(state.getGhostState(0).getY());
		setGhostDirection(state.getGhostState(0).getDirection());
		setWalls(new boolean[state.getMaze().getSizeX()][state.getMaze().getSizeY()]);
		setFood(new boolean[state.getMaze().getSizeX()][state.getMaze().getSizeY()]);
		
		for (int x = 0; x < state.getMaze().getSizeX(); x++) {
			for (int y = 0; y < state.getMaze().getSizeY(); y++) {
				walls[x][y] = state.getMaze().isWall(x, y);
				food[x][y] = state.getMaze().isFood(x, y);
			}
		}
	}
	public EtatPacmanMDPClassic(@NotNull EtatPacmanMDPClassic etat) {
		setPacmanX(etat.getPacmanX());
		setPacmanY(etat.getPacmanY());
		setPacmanDirection(etat.getPacmanDirection());
		setGhostX(etat.getGhostX());
		setGhostY(etat.getGhostY());
		setGhostDirection(etat.getGhostDirection());
		setWalls(etat.getWalls());
		setFood(etat.getFood());
	}
	
	/* GETTERS & SETTERS */
	
	public int getPacmanX() {
		return pacmanX;
	}
	
	public void setPacmanX(int pacmanX) {
		this.pacmanX = pacmanX;
	}
	
	public int getPacmanY() {
		return pacmanY;
	}
	
	public void setPacmanY(int pacmanY) {
		this.pacmanY = pacmanY;
	}
	
	public int getPacmanDirection() {
		return pacmanDirection;
	}
	
	public void setPacmanDirection(int pacmanDirection) {
		this.pacmanDirection = pacmanDirection;
	}
	
	public int getGhostX() {
		return ghostX;
	}
	
	public void setGhostX(int ghostX) {
		this.ghostX = ghostX;
	}
	
	public int getGhostY() {
		return ghostY;
	}
	
	public void setGhostY(int ghostY) {
		this.ghostY = ghostY;
	}
	
	public int getGhostDirection() {
		return ghostDirection;
	}
	
	public void setGhostDirection(int ghostDirection) {
		this.ghostDirection = ghostDirection;
	}
	
	public boolean[][] getWalls() {
		return walls;
	}
	
	public void setWalls(boolean[][] walls) {
		this.walls = walls;
	}
	
	public boolean[][] getFood() {
		return food;
	}
	
	public void setFood(boolean[][] food) {
		this.food = food;
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
		return getPacmanX() == that.getPacmanX() &&
				getPacmanY() == that.getPacmanY() &&
				getPacmanDirection() == that.getPacmanDirection() &&
				getGhostX() == that.getGhostX() &&
				getGhostY() == that.getGhostY() &&
				getGhostDirection() == that.getGhostDirection() &&
				Arrays.equals(getWalls(), that.getWalls()) &&
				Arrays.equals(getFood(), that.getFood());
	}
	
	@Override
	public int hashCode() {
		int result = Objects.hash(getPacmanX(), getPacmanY(), getPacmanDirection(), getGhostX(), getGhostY(), getGhostDirection());
		result = 31 * result + Arrays.hashCode(getWalls());
		result = 31 * result + Arrays.hashCode(getFood());
		return result;
	}
	
	@Override
	public String toString() {
		return "EtatPacmanMDPClassic{" +
				"pacmanX=" + pacmanX +
				", pacmanY=" + pacmanY +
				", pacmanDirection=" + pacmanDirection +
				", ghostX=" + ghostX +
				", ghostY=" + ghostY +
				", ghostDirection=" + ghostDirection +
				", walls=" + Arrays.toString(walls) +
				", food=" + Arrays.toString(food) +
				'}';
	}
}
