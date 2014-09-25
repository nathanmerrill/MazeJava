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
 * Recursive Division takes an open maze,
 * draws a line to split it in two, with a single point
 * open.  It then calls itself on the two halves.
 *
 */
public class DividingMaze extends AbstractMaze {
	private static final long serialVersionUID = 2251055024084483008L;
	List<Rectangle> rectangles;
	
	private class Rectangle{
		private Point topLeft;
		private Point bottomRight;
		private Rectangle(Point topLeft, Point bottomRight){
			this.topLeft = topLeft;
			this.bottomRight = bottomRight;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((bottomRight == null) ? 0 : bottomRight.hashCode());
			result = prime * result
					+ ((topLeft == null) ? 0 : topLeft.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Rectangle other = (Rectangle) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (bottomRight == null) {
				if (other.bottomRight != null)
					return false;
			} else if (!bottomRight.equals(other.bottomRight))
				return false;
			if (topLeft == null) {
				if (other.topLeft != null)
					return false;
			} else if (!topLeft.equals(other.topLeft))
				return false;
			return true;
		}
		private DividingMaze getOuterType() {
			return DividingMaze.this;
		}
		public int getTop(){
			return topLeft.y;
		}
		public int getBottom(){
			return bottomRight.y;
		}
		public int getLeft(){
			return topLeft.x;
		}
		private int getRight(){
			return bottomRight.x;
		}
	}
	
	@Override
	protected void generateStart() {
		rectangles = new ArrayList<Rectangle>();
		fillMaze(State.Open);
		rectangles.add(new Rectangle(new Point(0,0),
				new Point(getSize().width-1,getSize().height-1)));
	}
	@Override
	protected boolean generatePart() {
		return divide();
	}
	//This function will take the a random rectangle, and divide it in half, with a single point open
	private boolean divide(){
		if (rectangles.isEmpty())
			return false;
		Rectangle rect = rectangles.remove(r.nextInt(rectangles.size()));//Get random rectangle
		
		if (rect.getBottom()==rect.getTop())//If rectangle has 0 width or height, try again
			return divide();
		if (rect.getRight()==rect.getLeft())
			return divide();
		
		if ((rect.getBottom()-rect.getTop())>(rect.getRight()-rect.getLeft())){//If its taller than it is wide
			
			int yLine = r.nextInt((rect.getBottom()-rect.getTop())/2)*2+1+rect.getTop();//get random horizontal line
			
			drawHorizontalLine(yLine, rect.getLeft(), rect.getRight());
			
			rectangles.add(new Rectangle(new Point(rect.getLeft(),rect.getTop())//create two new rectangles
						,new Point(rect.getRight(),yLine-1)));
			rectangles.add(new Rectangle(new Point(rect.getLeft(),yLine+1)
						,new Point(rect.getRight(), rect.getBottom()))); 
		} else {
			int xLine = r.nextInt((rect.getRight()-rect.getLeft())/2)*2+1+rect.getLeft();//get random vertical line
			
			drawVerticalLine(xLine, rect.getTop(), rect.getBottom());
			
			rectangles.add(new Rectangle(new Point(rect.getLeft(), rect.getTop())//create two new rectangles
						,new Point(xLine-1, rect.getBottom())));
			rectangles.add(new Rectangle(new Point(xLine+1,rect.getTop())
						,new Point(rect.getRight(), rect.getBottom())));
		}
		return true;
		
	}
	private void drawVerticalLine(int xLine, int top, int bottom){
		int numChances = (bottom-top)/2+1;
		int xToSkip = r.nextInt(numChances)*2+top;
		for (int i = top; i<=bottom; i++){
			if (i == xToSkip)
				continue;
			setState(new Point(xLine,i),State.Wall);
		}
	}
	private void drawHorizontalLine(int rLine, int left, int right){
		int numChances = (right-left)/2+1;
		int yToSkip = r.nextInt(numChances)*2+left;
		for (int i = left; i<=right; i++){
			if (i == yToSkip)
				continue;
			setState(new Point(i,rLine),State.Wall);
		}
	}

}
