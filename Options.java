package maze;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import maze.AbstractMaze.Maze;
import maze.generators.DividingMaze;
import maze.generators.EllersMaze;
import maze.generators.GrowingMaze;
import maze.generators.PrimsMaze;
import maze.generators.RecursiveBacktrackerMaze;


/* Nathan Merrill
 * A01204314
 * CS2410
 */
/**
 * This class stores and validates all options pertaining to the Maze
 * After calling any setter, any other variable may be changed, and
 * therefore the GUI should update using all of the getters
 *
 */
public class Options implements Serializable{
	private static final long serialVersionUID = -6853834005340201438L;

	public enum StartLocations {Corners, Randomized};
	public enum MazeType {Perfect, Braid};
	public enum ViewType {Normal,Limited};
	private Map<String, AbstractMaze> mazeMap;
	
	private StartLocations startLocation;
	private MazeType mazeType;
	private ViewType viewType;
	private int iconSize;
	private int mazeSize;
	private int generationDelay;
	private int historySize;
	private int viewRange;
	private String mazeSelection;
	private Random r;
	
	private <T extends Enum<T>> T randomEnum(T[] enums){
		return enums[r.nextInt(enums.length)];
	}

	/**
	 * This returns a number between 0 (inclusive) and max (exclusive).
	 * 
	 * It is weighted towards 0.
	 */
	public int getWeightedRandomInt(int max){ 
		return (int)Math.pow(r.nextDouble()*Math.pow(max, .5), 2);
	}
	public void randomize(){
		setStartLocation(randomEnum(StartLocations.values()));
		setMazeType(randomEnum(MazeType.values()));
		setViewType(randomEnum(ViewType.values()));
		setIconSize(50);
		setMazeSize(getWeightedRandomInt(1000));

		if (viewType==ViewType.Limited){
			setViewRange(r.nextInt(10));
			setHistorySize(getViewRange()*getWeightedRandomInt(20)+3);
		} else{
			setGenerationDelay(getWeightedRandomInt(100));
		}
		int a = r.nextInt(mazeMap.keySet().size());
		for (String mazeType: mazeMap.keySet()){
			if (a==0)
				setMazeSelection(mazeType);
			a--;
		}
	}
	
	
	public Options(){
		r = new Random();
		r.setSeed(System.currentTimeMillis());
		startLocation = StartLocations.Corners;
		mazeType = MazeType.Perfect;
		viewType = ViewType.Normal;
		iconSize = 20;
		mazeSize = 15;
		generationDelay = 50;
		historySize = 50;
		viewRange = 5;
		

		mazeSelection = "Recursive Backtracker";
		mazeMap = new HashMap<String, AbstractMaze>();
		mazeMap.put(mazeSelection, new RecursiveBacktrackerMaze());
		mazeMap.put("Eller's Maze", new EllersMaze());
		mazeMap.put("Prim's Maze", new PrimsMaze());
		mazeMap.put("Recursive Division", new DividingMaze());
		mazeMap.put("Growing Maze", new GrowingMaze());
	}
	public Options(Options o){
		r = new Random();
		r.setSeed(System.currentTimeMillis());
		startLocation =o.startLocation;
		mazeType = o.mazeType;
		viewType = o.viewType;
		mazeSize = o.mazeSize;
		generationDelay = o.generationDelay;
		historySize = o.historySize;
		viewRange =o.viewRange;
		iconSize = o.iconSize;

		mazeSelection = o.mazeSelection;
		mazeMap = o.mazeMap;
	}
	
	public void update(Options o){
		startLocation =o.startLocation;
		mazeType = o.mazeType;
		viewType = o.viewType;
		mazeSize = o.mazeSize;
		generationDelay = o.generationDelay;
		historySize = o.historySize;
		viewRange =o.viewRange;
		iconSize = o.iconSize;

		mazeSelection = o.mazeSelection;
		mazeMap = o.mazeMap;
	}
	
	
	
	public void setGenerationDelay(int delay){
		if (delay>100)
			delay = 100;
		if (delay< 0)
			delay = 0;
		if (delay!=0)
			viewType = ViewType.Normal;
		generationDelay = delay;
	}
	public int getGenerationDelay(){
		return generationDelay;
	}
	
	public int getViewRange(){
		return viewRange;
	}
	public void setViewRange(int viewRange){
		if (viewRange > 10)
			viewRange = 10;
		if (viewRange < 1)
			viewRange = 1;
		if (historySize < 4*viewRange)
			historySize = 4*viewRange;
		this.viewRange = viewRange;
	}
	
	public Maze getMaze(){
		return mazeMap.get(mazeSelection).create(this);
	}
	public Set<String> getMazeVarieties(){
		return mazeMap.keySet();
	}
	public void setMazeSelection(String selection){
		this.mazeSelection = selection;
	}
	public String getMazeSelection(){
		return mazeSelection;
	}

	public StartLocations getStartLocations() {
		return startLocation;
	}

	public void setStartLocation(StartLocations startLocation) {
		this.startLocation = startLocation;
	}

	public MazeType getMazeType() {
		return mazeType;
	}

	public void setMazeType(MazeType mazeType) {
		this.mazeType = mazeType;
	}

	public ViewType getViewType() {
		return viewType;
	}

	public void setViewType(ViewType viewType) {
		this.viewType = viewType;
	}

	public int getIconSize() {
		return iconSize;
	}

	public void setIconSize(int iconSize) {
		if (iconSize<1)
			iconSize = 1;
		else if (iconSize>50)
			iconSize = 50;
		while (iconSize*mazeSize>1000){
			mazeSize-=2;
		}
		this.iconSize = iconSize;
	}

	public int getMazeSize() {
		return mazeSize;
	}

	public void setMazeSize(int mazeSize) {
		if (mazeSize%2==0){
			if (mazeSize>this.mazeSize)
				mazeSize++;
			else
				mazeSize--;
		}
		if (mazeSize>999)
			mazeSize = 999;
		else if (mazeSize<5)
			mazeSize = 5;
		while (mazeSize*iconSize>1000){
			iconSize--;
		}
		this.mazeSize = mazeSize;
	}

	
	public int getHistorySize() {
		return historySize;
	}
	

	public void setHistorySize(int historySize) {
		if (historySize < 4*viewRange)
			historySize = 4*viewRange;
		if (historySize > 10000)
			historySize = 10000;
		this.historySize = historySize;
	}

	
	
}
