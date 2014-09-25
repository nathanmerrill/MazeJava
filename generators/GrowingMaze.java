package maze.generators;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import maze.AbstractMaze;

public class GrowingMaze extends AbstractMaze{
	private static final long serialVersionUID = -2608043105361258192L;
	
	List<Point> edges;
	@Override
	protected boolean generatePart(){
		if (edges.isEmpty())
			return false;
		Point p = edges.remove(r.nextInt(edges.size()));
		if (getState(p).equals(State.Wall))
			return generatePart();
		Point next = null;
		for (Direction d: Direction.values()){
			if (getState(d.shift(p)).equals(State.Wall) || getState(d.shift(p,2)).equals(State.OutOfBounds)){
				if (next!=null){
					return generatePart();
				}
				next = d.getOpposite().shift(p);
			}
		}
		setState(p,State.Wall);
		setState(next, State.Wall);
		for (Direction d: Direction.values()){
			if (d.shift(next).equals(State.Open))
				edges.add(d.shift(next));
		}
		return true;	
	}

	@Override
	protected void generateStart() {
		fillMaze(State.Open);
		edges = new ArrayList<Point>();
		for (int x = 1; x < getSize().width; x+=2){
			edges.add(new Point(x,0));
			edges.add(new Point(x,getSize().height-1));			
		}
		for (int y = 1; y < getSize().height; y+=2){
			edges.add(new Point(0,y));
			edges.add(new Point(getSize().width-1,y));			
		}
	}

}
