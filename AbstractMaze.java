package maze;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import maze.Options.MazeType;
import maze.Options.StartLocations;


/* Nathan Merrill
 * A01204314
 * CS2410
 */

/**
 * AbstractMaze is to be extended by all maze generators
 * It has two functions of note, generateStart and generatePart
 * generateStart is to be used for the generator to instantiate its generation-needed variables and for general setup
 * generatePart should generate a single part of the maze.  It will be called repeatedly, and should return false only when the entire maze is generated
 * 
 */


public abstract class AbstractMaze implements Serializable{
	private static final long serialVersionUID = 3582962809093999443L;

	public enum State{Wall, Open, Finish, OutOfBounds};
	public enum Direction{Up, Left, Right, Down;
		public Point shift(Point p){
			return shift(p,1);
		}
		public Point shift(Point p, int amount){//Takes a point and shifts it the direction of the enum
			switch(this){
			case Down:
				return new Point(p.x, p.y+amount);
			case Left:
				return new Point(p.x-amount, p.y);
			case Right:
				return new Point(p.x+amount, p.y);
			case Up:
				return new Point(p.x, p.y-amount);
			default:
				return null;
			}
		}
		public Direction getOpposite(){
			return values()[3-this.ordinal()];
		}
		public static Direction[] getRandomValues(){
			Direction[] values = values();
			Collections.shuffle(Arrays.asList(values), r);
			return values;
		}
		public static Direction getRandomValue(){
			return values()[r.nextInt(values().length)];
		}
	}
	

	
	/**
	 * This is the public interface to the Maze.
	 * Prior to use, the user must call generateStart, 
	 * and then call generatePart until generatePart returns true;
	 * Alternativly, if you don't want such fine control, you can call
	 * generateAll
	 *
	 */
	public class Maze implements Serializable{
		private static final long serialVersionUID = 7329409782031846480L;
		
		private int numMoves;//Keeps track of number of moves.  history.size() will not work, because undoing a move still adds to this variable
		private Set<Point> fastestPath;//Stores the fastest path
		private Point player;//Stores the position of the player
		protected Point start;//Stores the position of the start
		protected Point finish;//Stores the position of the finish
		private Stack<Point> history;//A history of the player's moves, for undo and for the finish
		private Options options;//The options chosen for the maze
		private Set<Point> all;//A set of points that contains each point of the maze.  It is global so it doesn't have to be generated over and over
		
		
		
		private Maze(Options options){
			maze = new State[options.getMazeSize()][options.getMazeSize()];
			this.options = options;
			all = new HashSet<Point>();
			history = new Stack<Point>();
			for (int i = 0; i < maze.length; i++){
				for (int a = 0; a < maze[i].length; a++){
					all.add(new Point(i,a));
				}
			}
		}
		
		public void generateAll(){
			generateStart();
			while (generatePart());
		}
		public void generateStart(){
			currentPosition = new Point(-2,0);//Set to -2, so that the incremental counting starts at 0
			player = null;
			AbstractMaze.this.generateStart();
			numMoves = 1;
		}
		public boolean generatePart(){//
			if (AbstractMaze.this.generatePart())
				return true;
			if (options.getMazeType().equals(MazeType.Braid))
				if (clearDeadEnds())
					return true;
			if (player!=null)
				return false;
			setStartLocations();
			player = new Point(start);
			setState(finish, State.Finish);
			return true;
		}
		
		private Point currentPosition;
		private boolean clearDeadEnds(){//Clears all of the dead ends in the maze, preferably by connecting an adjacent dead ends
			int x = currentPosition.x+2;
			int y = currentPosition.y;
			if (x > maze.length){
				x = 0;
				y += 2;
				if (y > maze[0].length)
					return false;
			}
			currentPosition = new Point(x, y);
			if (isDeadEnd(currentPosition)){
				for (Direction d:Direction.getRandomValues()){
					if (isDeadEnd(d.shift(currentPosition,2))){
						setState(d.shift(currentPosition), State.Open);
						return true;
					}
				}
				for (Direction d:Direction.getRandomValues()){
					if (getState(d.shift(currentPosition)).equals(State.Wall)){
						setState(d.shift(currentPosition), State.Open);
						return true;
					}
				}
				
			}
			return clearDeadEnds();
		}
		
		private boolean isDeadEnd(Point p){//Returns true if the point has less than 2 connections, and if its not out of bounds
			if (getState(p)==State.OutOfBounds)
				return false;
			int numConnections = 0;
			for (Direction d: Direction.values()){
				if (getState(d.shift(p))==State.Open){
					numConnections++;
				}
			}
			return numConnections<2;
		}

		
		private void setStartLocations(){//Will set the start locations, and generate the fastest path
			if (options.getStartLocations().equals(StartLocations.Corners)){
				start = new Point(0,0);
				finish = new Point(maze.length-1,maze[0].length-1);
				fastestPath = generateFastestPath();
			} else {
				while (true){
					start = getRandomPoint();
					finish = getRandomPoint();
					fastestPath = generateFastestPath();
					if (fastestPath.size()>=Math.sqrt(maze.length*maze[0].length))
						break;
				}
			}
		}
		
		private Set<Point> generateFastestPath(){//Floods the maze, following each path until it finds the fastest route
			Set<Point> visitedPoints = new HashSet<Point>();
			List<Finder> recentPoints = new ArrayList<Finder>();
			recentPoints.add(new Finder(null, start));
			visitedPoints.add(start);
			while (!recentPoints.isEmpty()){
				ListIterator<Finder> iter = recentPoints.listIterator();
				while (iter.hasNext()){
					Finder next = iter.next();
					if (next.location.equals(finish))
						return buildFastestPath(next);
					iter.remove();
					for (Direction d:Direction.values()){
						Point midPoint = d.shift(next.location);
						Point newLocation = d.shift(next.location, 2);
						if (getState(midPoint)!=State.Open)
							continue;
						if (visitedPoints.contains(newLocation))
							continue;
						Finder midFinder = new Finder(next, midPoint);
						iter.add(new Finder(midFinder, newLocation));
						visitedPoints.add(newLocation);
					}
				}
			}
			return null;
		}
		private Set<Point> buildFastestPath(Finder f){
			Set<Point> stack = new HashSet<Point>();
			while (f!=null){
				stack.add(f.location);
				f = f.last;
			}
			return stack;
		}
		private class Finder{//A basic linked list that remembers previous points
			Finder last;
			Point location;
			public Finder(Finder last, Point location){
				this.last = last;
				this.location = location;
			}
		}

		public void movePlayer(Direction d){
			movePlayer(d.shift(player));
		}
		public void movePlayer(Point p){//This function moves the player, and then returns the state of the square attempted
			if (p.equals(player))
				return;	
			if (Math.abs(player.x-p.x)+Math.abs(player.y-p.y)>1){//If the point is more than one square away
				return;
			}
			State newState = getState(p);//This function takes care of array bounds checking
			if (newState==State.Open||newState==State.Finish){//If it's an open square, or the finish line
				history.add(player);
				player = p;//update player variable
				numMoves++;//Add to number of moves
			}
		}
		
		public void undo(){
			try{
				player = history.pop();
				numMoves++;//Add to number of moves
			}catch (EmptyStackException e){}
		}
	
		public Set<Point> getAll(){//Returns a set of points that points to every point in the maze
			return all;
		}
		public int getFastestPathSize(){
			return fastestPath.size();
		}
		public int getNumMoves(){
			return numMoves;
		}
		public Point getPlayer(){
			return player;
		}
		public State getState(Point p){
			return AbstractMaze.this.getState(p);
		}
		public Dimension getSize(){
			return new Dimension(maze.length, maze[0].length);
		}
		public Stack<Point> getHistory(){
			return history;
		}
		public Set<Point> getFastestPath(){
			return fastestPath;
		}
		
		
		public boolean canUndo(){
			return history==null?false:!history.isEmpty();
		}
		public boolean hasFinished(){
			return player.equals(finish);
		}
		
	}

	protected static Random r;
	protected Point getRandomPoint(){
		return new Point(r.nextInt(maze.length/2)*2,r.nextInt(maze[0].length/2)*2);
	}
	private State[][] maze;//A double array of states (representing the squares of the maze)
	
	protected abstract boolean generatePart();
	protected abstract void generateStart();

	protected void fillMaze(State state){
		for (int i = 0; i< maze.length; i++){
			for (int a = 0; a < maze[i].length; a++){
				setState(new Point(i,a), state);
			}
		}
	}
	protected void fillMaze(){
		fillMaze(State.Wall);
	}
	public Maze create(Options options){
		return new Maze(options){
		private static final long serialVersionUID = -1915647990804001624L;};
	}
	public AbstractMaze(){
		r = new Random();
		r.setSeed(System.currentTimeMillis());
		
	}
	protected Dimension getSize(){
		return new Dimension(maze.length, maze[0].length);
	}
	protected void setState(Point p, State state){
		maze[p.x][p.y]=state;
	}
	
	protected State getState(Point p){
		if (p.x<0)
			return State.OutOfBounds;
		if (p.y<0)
			return State.OutOfBounds;
		if (p.x>=maze.length)
			return State.OutOfBounds;
		if (p.y>=maze[p.x].length)
			return State.OutOfBounds;
		return maze[p.x][p.y];
	}
	
 	
	


}
