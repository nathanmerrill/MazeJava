package maze.generators;

import java.awt.Point;
import java.util.Stack;

import maze.AbstractMaze;

/* Nathan Merrill
 * A01204314
 * CS2410
 */

/**
 * This maze will tunnel from a random point,
 * going random directions. When it reaches a point
 * that any other direction will create a loop
 * or will go out of bounds, it will backtrack
 * until it finds a point that it can tunnel from
 *
 */
public class RecursiveBacktrackerMaze extends AbstractMaze {
	private static final long serialVersionUID = -5226069795695114857L;
	
	Stack<Point> history;//History of previously visited cleared spot (to backtrack if you run into a dead end)
	

	@Override
	protected void generateStart() {
		fillMaze();
		Point p = getRandomPoint();
		history = new Stack<Point>();
		history.add(p);
		setState(p, State.Open);
	}
	
	@Override
	protected boolean generatePart() {
		if (history.isEmpty())//History will be empty when maze is generated
			return false;
		Direction[] directions = Direction.getRandomValues();//Get array of the directions in a random order
		for (Direction direction:directions){
			Point next = direction.shift(history.peek(),2);//Look at next intersection
			if (getState(next)!=State.Wall) //If its open, then it hasn't been dug to
				continue;
			Point inBetween = direction.shift(history.peek());
			setState(inBetween, State.Open);
			setState(next, State.Open);
			history.push(next);//Add point just dug to to the history
			return true;
		}
		history.pop();
		return generatePart();
	}
	

}
