package maze.generators;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import maze.AbstractMaze;


/* Nathan Merrill
 * A01204314
 * CS2410
 */

/**
 * Eller's Maze iterates line by line.
 * It maintains a list of states, where a state is defined
 * as non-connected portions of the maze.  Two points that
 * have the same state number have already been connected,
 * and vice versa.
 * 
 * The algorythm will connect random points on a line, and update
 * their states (but never two points with the same state).
 * Then, for each set of points in a single state, it will
 * connect at least 1 random point to the next line down.
 */
public class EllersMaze extends AbstractMaze{
	private static final long serialVersionUID = 639090495466278148L;
	int[] states;//Array of states (different numbers have not been connected)
	
	int y;//Current position
	int x;
	
	@Override
	protected void generateStart() {
		fillMaze();
		states = new int[getSize().width/2+1];
		for (int i = 0; i<states.length; i++){
			states[i]=i;
		}
		y = 0;
		x = 0;
	}
	
	@Override
	protected boolean generatePart() {
		if (y==getSize().height){//If just finished last line
			if (allHaveSameState())//And all of the states have been connected
				return false;
			y--;//Otherwise, retry last line
		}
		if (y%2==0){//If on line with openings
			mergeStates();//Merge random openings
		} else {
			dropDownStates();//Otherwise, dropdown random states
			y++;
			return true;
		}
		x++;
		if (x==getSize().width){
			x=0;
			y++;
		}
		return true;
	}
	private boolean allHaveSameState(){
		for (int i = 0; i < states.length; i++){
			if (states[i]!=0)//If every state has been merged, they all will have 0, because we set it to the lowest nubmer
				return false;
		}
		return true;
	}
	private void mergeStates(){
		if (x%2==0){//If on vertex
			setState(new Point(x,y), State.Open);
			return;
		}
		if (states[x/2]==states[(x+1)/2]){//If current state is the same as the next state
			return;//Don't do anything
		}
		if(r.nextBoolean()){//Randomly decide if you should connect states
			setStates(states[(x+1)/2],states[x/2]);
			setState(new Point(x,y),State.Open);
		}
	}
	private void setStates(int from, int to){
		if (from < to) {//Make sure to always set the states to the lowest number
			int a = from;
			from = to;
			to = a;
		}
		for (int i = 0; i< states.length; i++){
			if (states[i]==from)
				states[i]=to;
		}
	}
	
	private void dropDownStates(){//Moves down a random set of states, but at least 1 of each state
		Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
		int max = states[0];
		for (int state: states){
			Integer count = occurrences.get(state);
			if (count==null)
				occurrences.put(state, 1);
			else
				occurrences.put(state, count+1);
			if (max<state)
				max = state;
		}
		max++;
		List<Integer> list = new ArrayList<Integer>(); 
		for (int i = 0; i < states.length; i++){
			list.add(i);
		}
		Collections.shuffle(list);
		for (Integer item: list){
			int state = states[item];
			int count = occurrences.remove(state);
			if (count>1){
				occurrences.put(state, count-1);
				if (r.nextBoolean()){
					states[item] = max++;
					continue;
				}
			}
			setState(new Point(item*2,y),State.Open);
		}
	}
	

}
