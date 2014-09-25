package maze;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import maze.Options.MazeType;
import maze.Options.StartLocations;
import maze.Options.ViewType;

/* Nathan Merrill
 * A01204314
 * CS2410
 */

/**
 * This panel is an interface to change all of the settings for maze generation
 *
 */
public class OptionsPanel extends JPanel{
	
	private static final long serialVersionUID = -3320643302438797849L;
	
	private JLabel sizeOfIcon;
	private JSpinner iconSize;
	
	private JLabel sizeOfMaze;
	private JSpinner mazeSize;

	private ButtonGroup playStyles;
	private JRadioButton normalPlay;
	private JRadioButton limitedViewPlay;
	
	private ButtonGroup startingLocations;
	private JRadioButton corners;
	private JRadioButton randomized;
	
	private JComboBox<String> mazeTypes;
	private ButtonGroup mazeType;
	private JRadioButton perfect;
	private JRadioButton braid;
	
	private JLabel delayOfGeneration;
	private JSpinner generationDelay;
	
	private JButton defaults;
	private JButton randomize;
	
	private JLabel sizeOfHistory;
	private JSpinner historySize;
	
	private JLabel rangeOfView;
	private JSpinner viewRange;
	
	
	private List<JPanel> panels;
	
	private Options options;//The settings are stored in a seperate object so it can be passed around
	
	/**
	 * Updates all of the GUI components to match options
	 */
	private void updateOptions(){
		iconSize.setValue(options.getIconSize());
		mazeSize.setValue(options.getMazeSize());
		if (options.getMazeType().equals(MazeType.Perfect))
			perfect.setSelected(true);
		else
			braid.setSelected(true);
		
		if (options.getStartLocations().equals(StartLocations.Corners))
			corners.setSelected(true);
		else
			randomized.setSelected(true);
		
		if (options.getViewType().equals(ViewType.Normal))
			normalPlay.setSelected(true);
		else
			limitedViewPlay.setSelected(true);
		
		mazeTypes.setSelectedItem(options.getMazeSelection());
		generationDelay.setValue(options.getGenerationDelay());
		generationDelay.setEnabled(options.getViewType().equals(ViewType.Normal));
		
		historySize.setValue(options.getHistorySize());
		historySize.setEnabled(limitedViewPlay.isSelected());
		viewRange.setEnabled(limitedViewPlay.isSelected());
		
		viewRange.setValue(options.getViewRange());
	}
	
	private JPanel createIconPanel(){
		sizeOfIcon = new JLabel("Icon size:");
		iconSize = new JSpinner(new SpinnerNumberModel());
		iconSize.setPreferredSize(new Dimension(50,20));
		iconSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				options.setIconSize(((int)iconSize.getValue()));
				updateOptions();
			}
		});
		JPanel panel = new JPanel();
		panel.add(sizeOfIcon);
		panel.add(iconSize);
		return panel;
	}
	
	private JPanel createDimensionPanel(){
		sizeOfMaze = new JLabel("Maze size:");
		mazeSize = new JSpinner(new SpinnerNumberModel());
		mazeSize.setPreferredSize(new Dimension(50,20));
		mazeSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				options.setMazeSize((int)mazeSize.getValue());
				updateOptions();
			}
		});
		JPanel panel = new JPanel();
		panel.add(sizeOfMaze);
		panel.add(mazeSize);
		return panel;
	}
	
	private JPanel createViewPanel(){
		normalPlay = new JRadioButton("Full View");
		normalPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setViewType(ViewType.Normal);
				updateOptions();
			}
		});
		limitedViewPlay = new JRadioButton("Limited View");
		limitedViewPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setViewType(ViewType.Limited);
				updateOptions();
			}
		});
		playStyles = new ButtonGroup();
		playStyles.add(normalPlay);
		playStyles.add(limitedViewPlay);
		
		JPanel panel = new JPanel();
		panel.add(normalPlay);
		panel.add(limitedViewPlay);
		return panel;
	}
	
	private JPanel createStartsPanel(){
		corners = new JRadioButton("Corner ends");
		corners.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setStartLocation(StartLocations.Corners);
				updateOptions();
			}
		});
		randomized = new JRadioButton("Randomized");
		randomized.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setStartLocation(StartLocations.Randomized);
				updateOptions();
			}
		});
		startingLocations = new ButtonGroup();
		startingLocations.add(corners);
		startingLocations.add(randomized);
		
		JPanel panel = new JPanel();
		panel.add(corners);
		panel.add(randomized);
		return panel;
		
	}
	
	private JPanel createGenerationPanel(){
		mazeTypes = new JComboBox<String>();
		for (String mazeString:options.getMazeVarieties()){
			mazeTypes.addItem(mazeString);
		}
		mazeTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setMazeSelection((String)mazeTypes.getSelectedItem());
				updateOptions();
			}
		});
		
		JPanel panel = new JPanel();
		panel.add(mazeTypes);
		return panel;
		
	}
	
	private JPanel createMazeTypePanel(){
		perfect = new JRadioButton("Perfect");
		perfect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setMazeType(MazeType.Perfect);
				updateOptions();
			}
		});
		braid = new JRadioButton("Braid");
		braid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.setMazeType(MazeType.Braid);
				updateOptions();
			}
		});
		mazeType = new ButtonGroup();
		mazeType.add(perfect);
		mazeType.add(braid);
		
		JPanel panel = new JPanel();
		panel.add(perfect);
		panel.add(braid);
		return panel;
	}
	
	private JPanel createGenerationDelayPanel(){
		delayOfGeneration = new JLabel("Generation delay:");
		generationDelay = new JSpinner();
		generationDelay.setPreferredSize(new Dimension(50,20));
		generationDelay.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				options.setGenerationDelay((int)generationDelay.getValue());
				updateOptions();
			}
		});
		
		JPanel panel = new JPanel();
		panel.add(delayOfGeneration);
		panel.add(generationDelay);
		return panel;
		
	}
	
	private JPanel createButtonPanel(){
		defaults = new JButton("Default");
		defaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options = new Options();
				updateOptions();
			}
		});
		randomize = new JButton("Randomize");
		randomize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				options.randomize();
				updateOptions();
			}
		});
		
		
		JPanel panel = new JPanel();
		panel.add(defaults);
		panel.add(randomize);
		return panel;
	}
	
	private JPanel createHistoryPanel(){
		sizeOfHistory = new JLabel("History size:");
		historySize = new JSpinner();
		historySize.setPreferredSize(new Dimension(50,20));
		historySize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				options.setHistorySize((int)historySize.getValue());
				updateOptions();
			}
		});
		JPanel panel = new JPanel();
		panel.add(sizeOfHistory);
		panel.add(historySize);
		return panel;
	}
	
	private JPanel createViewRangePanel(){
		rangeOfView = new JLabel("View range:");
		viewRange = new JSpinner(new SpinnerNumberModel());
		viewRange.setPreferredSize(new Dimension(50,20));
		viewRange.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				options.setViewRange((int)viewRange.getValue());
				updateOptions();
			}
		});
		JPanel panel = new JPanel();
		panel.add(rangeOfView);
		panel.add(viewRange);
		return panel;
	}
	public Options getOptions(){
		return options;
	}
	public OptionsPanel(){
		options = new Options();
		panels= new ArrayList<JPanel>();
		panels.add(createGenerationPanel());
		panels.add(createIconPanel());
		panels.add(createDimensionPanel());
		panels.add(createStartsPanel());
		panels.add(createMazeTypePanel());
		panels.add(createViewPanel());
		panels.add(createGenerationDelayPanel());
		panels.add(createHistoryPanel());
		panels.add(createViewRangePanel());
		panels.add(createButtonPanel());
		
		for (JPanel panel:panels){
			add(panel);
		}

		setLayout(new GridLayout(panels.size(),1));
		setPreferredSize(new Dimension(220, panels.size()*35));
		updateOptions();
	}
}
