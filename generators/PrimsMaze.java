package maze.generators;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import maze.AbstractMaze;

/* Nathan Merrill
 * A01204314
 * CS2410
 */

/**
 * Prim's maze starts at a random point, and
 * then puts all of its neighbors in a list.
 * 
 * It will then pick a random neighbor, and
 * connect it if it isn't already part of the
 * maze, and then add its neighbors to the list
 *
 */
public class PrimsMaze extends AbstractMaze{
	
	private static final long serialVersionUID = -8725674957504026722L;
	@Override
	protected void generateStart() {
		fillMaze();
		points = new ArrayList<Point>();
		Point start = getRandomPoint();
		setState(start, State.Open);
		addPointsAround(start);
	}

	@Override
	protected boolean generatePart(){
		return clearMaze();
	}
	
	
	List<Point> points;//A list of all of the points that need to be checked
	private boolean clearMaze(){
		if (points.isEmpty())
			return false;
		Point p = points.get(r.nextInt(points.size()));//Get a random point from the list
		points.remove(p);
		if (getState(p)!=State.Wall)
			return clearMaze();
		List<Point> openPoints = getNearbyOpenPoints(p);
		if (openPoints.size()>1)
			return clearMaze();
		Point last = openPoints.get(0);
		Point next = new Point(2*p.x-last.x,2*p.y-last.y);
		setState(p,State.Open);
		setState(next,State.Open);
		addPointsAround(next);
		return true;
	}
	
	private List<Point> getNearbyOpenPoints(Point p){
		List<Point> points = new ArrayList<Point>();
		for (Direction d: Direction.values()){
			Point newPoint = d.shift(p);
			if (getState(newPoint).equals(State.Open)){
				points.add(newPoint);
			}
		}
		return points;
	}
	private void addPointsAround(Point p){
		for (Direction d:Direction.values()){
			points.add(d.shift(p));
		}
	}


	

}
