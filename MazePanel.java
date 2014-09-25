package maze;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import maze.AbstractMaze.Maze;
import maze.AbstractMaze.State;
import maze.Options.ViewType;

/* Nathan Merrill
 * A01204314
 * CS2410
 */

/**
 * This panel is designed to display the current maze.
 * It will change its display based on view type chosen by the player
 * It also contains the save to file and load from file functions
 * becase it has all of the data needed (options, maze, and history) 
 *
 */
public class MazePanel extends JPanel {
	private static final long serialVersionUID = 8545625588582082978L;
	
	private boolean showPathTaken;
	private boolean showFastestPath;
	private final Options options;
	private final Maze maze;
	private Queue<Point> recentHistory;
	
	
	public void showEverything(){//To be called at the end.  Will display the full maze, plus path taken, plus fastest path
		showPathTaken(true);
		showFastestPath(true);
		options.setViewType(ViewType.Normal);
	}
	public void showPathTaken(boolean show){
		showPathTaken = show;
	}
	public void showFastestPath(boolean show){
		showFastestPath = show;
	}
	
	public void saveToFile(File f) throws IOException{//Will save current state to a file.
		OutputStream stream = new FileOutputStream(f);
		ObjectOutputStream o = new ObjectOutputStream(stream);
		o.writeObject(options);
		o.writeObject(maze);
		o.writeObject(recentHistory);
		o.close();
		       
	}
	
	@SuppressWarnings("unchecked")
	public static MazePanel readFromFile(MouseListener clicks) throws IOException, ClassNotFoundException{ //Will create a new MazePanel from file
		JFileChooser c = new JFileChooser();
		int rVal = c.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION){
			Options options = null;
			Maze maze = null;
			Queue<Point> recentHistory = null;
			InputStream stream = new FileInputStream(c.getSelectedFile());
			ObjectInputStream o = new ObjectInputStream(stream);
			options = (Options)o.readObject();
			maze = (Maze)o.readObject();
			recentHistory = (Queue<Point>)o.readObject();
			o.close();
			return new MazePanel(options, recentHistory, maze, clicks);
		}
		return null;
	}
	
	
	public MazePanel(){//Constructor for a new blank frame.  Will not have any functionality, but will reserve the GUI space
		setPreferredSize(new Dimension(1000,1027));
		maze = null;
		options = null;
	}
	public MazePanel(Options options, Queue<Point> recentHistory, Maze maze, MouseListener clicks){
		this.recentHistory = recentHistory;
		showPathTaken = false;
		showFastestPath = false;
		this.options = options;
		this.maze = maze;
		setPreferredSize(new Dimension(1000,1027));
		setDoubleBuffered(true);
		addMouseListener(clicks);
	}
	
	private void paintPoint(Graphics g, Point p){
		State s = maze.getState(p);
		
		if (s==null)
			return;
		switch(s){//Then set the color based on the square
		case Finish:
			g.setColor(Color.GREEN);
			break;
		case Open:
			g.setColor(Color.WHITE);
			break;
		case OutOfBounds:
			g.setColor(Color.GRAY);
			break;
		case Wall:
			g.setColor(Color.BLACK);
			break;
		}
		if (showFastestPath && maze.getFastestPath().contains(p)){//If we are showing the fastest route, and the point is in the fastest route, then change color to green
			g.setColor(Color.GREEN);
		}
		
		int x = p.x*options.getIconSize();//Position the squares
		int y = p.y*options.getIconSize();
		g.fillRect(x, y,options.getIconSize(),options.getIconSize());
		
		if (p.equals(maze.getPlayer())//If the position is the player
				||showPathTaken&&maze.getHistory().contains(p)){//Or if we are showing the path taken, and the square is in that path
			g.setColor(Color.BLUE);
			g.drawOval(x, y, options.getIconSize()-1,options.getIconSize()-1);//Draw a circle
			g.fillOval(x+options.getIconSize()*2/5, y+options.getIconSize()*2/5, options.getIconSize()*1/5, options.getIconSize()*1/5);//And an eye
			g.fillOval(x+options.getIconSize()*3/5, y+options.getIconSize()*2/5, options.getIconSize()*1/5, options.getIconSize()*1/5);//And another eye
			g.drawArc(x+options.getIconSize()*1/5, y, options.getIconSize()*4/5,options.getIconSize()*4/5, -180,90);//And a smile
		}
		
				
		
	}

	private void paintPoints(Graphics g, Collection<Point> ps){
		for (Point p: ps){
			paintPoint(g,p);
		}
	}
	
	@Override
	public void paintComponent(Graphics g){//Decides what points should be visible
		super.paintComponent(g);
		if (options==null)
			return;
		switch (options.getViewType()){
		case Limited:
			if (maze.getPlayer()==null)
				return;
			addPointsInView(maze.getPlayer());
			paintPoints(g,recentHistory);
			break;
		case Normal:
			paintPoints(g, maze.getAll());	
			break;
		}
	}
	
	private void addPointsInView(Point p){//This function will calculate what squares are in view by shooting out lines at different angles, and adding every square it hits until it hits a wall
		int iconSize = options.getIconSize();
		double centerX = (p.x+.5)*iconSize;//We calculate the point of view from the center of the square
		double centerY = (p.y+.5)*iconSize;
		Set<Double> angles = new HashSet<Double>();//A set of angles (so that if we hit a wall, we remove that angle from the set)
		Stack<Point> pointsToAdd = new Stack<Point>();//A current stack of points to add  
						//(We use a stack, so that when we push onto the history, if the history is smaller than the points seen, it will keep the closest points)
		pointsToAdd.add(maze.getPlayer());
		for (int a = 0; a < 360; a+=15){
			angles.add(Math.toRadians(a));
		}
		for (double length = .4; length <= options.getViewRange(); length+=.1){//We start at .4, to prevent any rounding errors, and then move outward in increments of .1
			Iterator<Double> iter = angles.iterator();
			while (iter.hasNext()){
				double angle = iter.next();
				double newX = length*Math.cos(angle)*iconSize+centerX;
				double newY = length*Math.sin(angle)*iconSize+centerY;
				Point toCheck = new Point((int)Math.round(newX/iconSize),(int)Math.round(newY/iconSize));
				
				if (maze.getState(toCheck)!=State.Open){
					iter.remove();
				}
				
				if (maze.getState(toCheck)!=State.OutOfBounds){//We do this check, so that we don't fill up the history with points that aren't visible
					pointsToAdd.add(toCheck);		
				}
				
			}
		}
		addAllToRecentHistory(pointsToAdd);// Will add the stack to the history (which, when beyond its max size, will keep only the newest items)
	}
	private void addAllToRecentHistory(Collection<Point> ps){
		for (Point p: ps){
			addToRecentHistory(p);
		}
	}
	private void addToRecentHistory(Point p){
		if (recentHistory.contains(p)){//If the queue already has the point
			recentHistory.remove(p);//Remove then add it again, so it is back at the top
			recentHistory.add(p);
			return;
		}
		if (!recentHistory.offer(p)){//If the stack is full
			recentHistory.poll();//remove the bottom of the queue
			recentHistory.offer(p);//And then put the new item
		}
	}

	public void movePlayer(Point p){//Converts the point on the frame to the applicable point in the maze
		int x = p.x/options.getIconSize();
		int y = p.y/options.getIconSize();
		maze.movePlayer(new Point(x,y));
	}
	
	public Maze getMaze(){
		return maze;
	}
	
}
