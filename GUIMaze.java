package maze;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import maze.AbstractMaze.Direction;
import maze.AbstractMaze.Maze;
import maze.Options.ViewType;


/* Nathan Merrill
 * A01204314
 * CS2410
 */

public class GUIMaze extends JFrame{
	private static final long serialVersionUID = -1300699734173008548L;
	
	private MazePanel mazePanel;
	private OptionsPanel optionsPanel;
	
	
	JButton undo;
	JButton start;
	JMenuItem quit;
	JMenuItem save;
	JMenuItem load;
	
	
	
	public GUIMaze(){
		mazePanel = new MazePanel();
		optionsPanel = new OptionsPanel();
		setLayout(new BorderLayout());
		add(mazePanel, BorderLayout.EAST);
		add(optionsPanel, BorderLayout.WEST);
		pack();
		validate();
		addListeners();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Maze Game");
		addMenu();
		setLocationRelativeTo(null);
	}
	
	private void addListeners(){//Adds the listeners for the keypresses
		ActionMap actionMap = getRootPane().getActionMap();
		InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		Map<Direction, Integer> keyPresses = new HashMap<Direction, Integer>();
		keyPresses.put(Direction.Up, KeyEvent.VK_UP);
		keyPresses.put(Direction.Down, KeyEvent.VK_DOWN);
		keyPresses.put(Direction.Left, KeyEvent.VK_LEFT);
		keyPresses.put(Direction.Right, KeyEvent.VK_RIGHT);
		for (Direction d: keyPresses.keySet()){
			inputMap.put(KeyStroke.getKeyStroke(keyPresses.get(d),0), "key"+d);
			actionMap.put("key"+d, new ArrowKey(d));
		}
		
		getRootPane().setActionMap(actionMap);
		getRootPane().setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, inputMap);
	}
	
	private void addMenu(){
		final JMenuBar mb = new JMenuBar();
		JMenu m = new JMenu("File");
		mb.add(m);
		
		start = new JButton("Start");
		start.addActionListener(new Start());
		mb.add(start);
		
		undo = new JButton("Undo");
		undo.setEnabled(false);
		undo.addActionListener(new Undo());
		mb.add(undo);

		save = new JMenuItem("Save");
		save.addActionListener(new Save());
		m.add(save);
		
		load = new JMenuItem("Load");
		load.addActionListener(new Load());
		m.add(load);
		
		quit = new JMenuItem("Quit");
		quit.addActionListener(new Quit());
		m.add(quit);
		
		setJMenuBar(mb);
	}
	
	
	/**A class that will generate the maze on a seperate thread
	 * It uses a seperate thread so that the updating of the JFrame
	 * doesn't slow down the generation of the maze.
	 * It can, however, be manually slowed from the generation delay.
	 *
	 */
	class MazeGenerator extends SwingWorker<Integer, Integer> {
		
		Maze maze;
		
		public MazeGenerator(Maze maze){
			this.maze = maze;
		}

		@Override
		protected Integer doInBackground() throws Exception {
			maze.generateStart();
			GUIMaze.this.setTitle("Loading...");
			while (maze.generatePart()){
				if (optionsPanel.getOptions().getViewType().equals(ViewType.Normal)){
					publish(0);
					Thread.sleep(optionsPanel.getOptions().getGenerationDelay());
				}
			}
			publish(0);
			GUIMaze.this.setTitle("Finished!");
			return 0;
		}
		@Override
		protected void process(List<Integer> parts){
			repaint();
		}
		
	}
	
	private class Quit implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			finish(false);
		}
	}
	private class Save implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser saveFile = new JFileChooser();
            if (saveFile.showOpenDialog(GUIMaze.this)==JFileChooser.APPROVE_OPTION){
            	try{
            		mazePanel.saveToFile(saveFile.getSelectedFile());
            	} catch (IOException e1){
            		showMessage("Unable to save to file");
            	}
            }
		}
	}
	private class Load implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				MazePanel mp = MazePanel.readFromFile(new MouseClicks());
				if (mp==null)
					return;
				setMazePanel(mp);
			} catch (ClassNotFoundException | IOException e1) {
				showMessage("Bad file");
			}
		}
	}
	private class Undo implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			mazePanel.getMaze().undo();
			undo.setEnabled(mazePanel.getMaze().canUndo());
			repaint();
		}
	}
	private class Start implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			
			Options options = new Options(optionsPanel.getOptions());//Use the copy constructor for the options, so updating the Option panel doesn't change the current display
			Queue<Point> history = new ArrayBlockingQueue<Point>(options.getHistorySize());
			MazePanel mp = new MazePanel(options, history, options.getMaze(), new MouseClicks());
			start.setText("Restart");
			setMazePanel(mp);
			new MazeGenerator(mazePanel.getMaze()).execute();//Starts a thread 			
			undo.setEnabled(mazePanel.getMaze().canUndo());
			save.setEnabled(true);
			
		}
	}
	
	private class ArrowKey extends AbstractAction {
		private Direction d;
		public ArrowKey(Direction d){
			this.d = d;
		}
		
		private static final long serialVersionUID = 4183490777699871094L;
		@Override
		public void actionPerformed(ActionEvent e) {
			mazePanel.getMaze().movePlayer(d);
			undo.setEnabled(mazePanel.getMaze().canUndo());
			repaint();
			if (mazePanel.getMaze().hasFinished())
				finish(true);
		}
		
	}

	private class MouseClicks extends MouseAdapter{
		@Override
		public void mouseReleased(MouseEvent e){
			mazePanel.movePlayer(new Point(e.getX(),e.getY()));
			undo.setEnabled(mazePanel.getMaze().canUndo());
			repaint();
			if (mazePanel.getMaze().hasFinished())
				finish(true);
		}
		@Override
		public void mouseEntered(MouseEvent e){
			e.getComponent().requestFocus();
		}
	}
	
	/**
	 * Removes the current maze panel (if it exists)
	 * and replaces it with the panel passed in.
	 * Additionally, it packs and validates the JFrame.
	 * @param mp
	 */
	private void setMazePanel(MazePanel mp){
		if (mazePanel!=null)
			remove(mazePanel);
		mazePanel = mp;
		add(mazePanel, BorderLayout.EAST);
		validate();
		pack();
		repaint();
	}
	
	private void finish(boolean win){//True if it was a win, false if it was a lose
		mazePanel.showEverything();
		repaint();
		if (win){
			showMessage("You win! It took you "+mazePanel.getMaze().getNumMoves() +" moves. Fastest was:"+mazePanel.getMaze().getFastestPathSize());
		} else {
			showMessage("Better luck next time!");
		}
		dispose();
		
	}
	
	private void showMessage(String message){
		JOptionPane.showMessageDialog(this, message);
	}

}
