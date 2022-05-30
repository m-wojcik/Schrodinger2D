package schrodinger2D;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class SchFrame extends JFrame {
	
	JPanel panelEast;
	JPanel panel0, panel1, panel1_1, panel1_2, panel2, panel3, panel4;
	SchPanel panelCenter;
	JCheckBox drawingCheckBox;
	JButton runButton, showButton;
	JRadioButton potentialButton, waveButton;
	ButtonGroup bg;
	JTextField velXTextField, velYTextField, framesTextField, nameTextField; 
	JLabel framesLabel, titleLabel, nameLabel, velXLabel, velYLabel;
	JSlider velXSlider, velYSlider;
	
	Boolean draw, drawPotential, drawWave;
	
	String simulationName;
	
	public SchFrame() {
		
		// FRAME ***************************************************************************
		
		this.setTitle("Schrodinger wave simulator");
		this.setSize(716, 438);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		this.setResizable(false);
		
		// VARIABLES **********************************************************************
		
		draw = false;
		drawPotential = true;
		drawWave = false;
		
		// PANEL CENTER *******************************************************************
		
		
		panelCenter = new SchPanel();
		panelCenter.setPreferredSize(new Dimension(400,400));
		this.add(panelCenter);
		
		
		// PANEL EAST *********************************************************************
		
		panelEast = new JPanel();
		panelEast.setBackground(Color.white);
		panelEast.setPreferredSize(new Dimension(300,500));
		panelEast.setLayout(new GridLayout(5, 1));
		this.add(panelEast, BorderLayout.EAST);
		
		//panel 0
		
		panel0 = new JPanel();
		panel0.setLayout(new BorderLayout());
		panel0.setBorder(BorderFactory.createLineBorder(Color.black));
		panelEast.add(panel0);
		
		titleLabel = new JLabel("Schrodinger Wave Simulator");
		titleLabel.setFont(new Font("Calibri", Font.BOLD, 23));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		panel0.add(titleLabel, BorderLayout.CENTER);
		
		//panel 1
		
		panel1 = new JPanel();
		panel1.setLayout(new GridLayout(1,2));
		panel1.setBorder(BorderFactory.createLineBorder(Color.black));
		panelEast.add(panel1);
		
		panel1_1 = new JPanel();
		panel1_1.setLayout(new GridLayout(2,1));
		nameLabel = new JLabel("Simulation name:");
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		panel1_1.add(nameLabel);
		nameTextField = new JTextField("Kwanty");
		nameTextField.setHorizontalAlignment(JTextField.CENTER);
		nameTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				panelCenter.setAnimationTitle(nameTextField.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				panelCenter.setAnimationTitle(nameTextField.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				panelCenter.setAnimationTitle(nameTextField.getText());
			}
		});
		panel1_1.add(nameTextField);
		panel1.add(panel1_1);
		
		panel1_2 = new JPanel();
		panel1.add(panel1_2);
		
		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panelCenter.repositionWaves();
				panelCenter.evolve(Double.parseDouble(framesTextField.getText()));
				JOptionPane.showMessageDialog(null, "Simulation done.");
			}
		});
		panel1_2.add(runButton);
		
		// panel 2
		
		panel2 = new JPanel();
		panel2.setBorder(BorderFactory.createLineBorder(Color.black));
		panel2.setLayout(new GridLayout(3,1));
		panelEast.add(panel2);
		
		drawingCheckBox = new JCheckBox("Interaction:");
		drawingCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
		drawingCheckBox.setHorizontalTextPosition(JCheckBox.LEFT);
		drawingCheckBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				 panelCenter.setDraw(!panelCenter.getDraw());
				 
				 potentialButton.setEnabled(panelCenter.getDraw());
				 waveButton.setEnabled(panelCenter.getDraw());
			}
		});
		panel2.add(drawingCheckBox);
		
		potentialButton = new JRadioButton("Draw potential");
		potentialButton.setHorizontalAlignment(JButton.CENTER);
		potentialButton.setSelected(true);
		potentialButton.setEnabled(false);
		potentialButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				 panelCenter.setDrawPotential(!panelCenter.getDrawPotential());	
			}
		});
		panel2.add(potentialButton);
		
		waveButton = new JRadioButton("Move wave function");
		waveButton.setHorizontalAlignment(JButton.CENTER);
		waveButton.setEnabled(false);
		waveButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				 panelCenter.setDrawWave(!panelCenter.getDrawWave());	
			}
		});
		panel2.add(waveButton);
		
		bg = new ButtonGroup();
		bg.add(potentialButton);
		bg.add(waveButton);
		
		// panel 3
		
		panel3 = new JPanel();
		panel3.setBorder(BorderFactory.createLineBorder(Color.black));
		panel3.setLayout(new GridLayout(2,2));
		panelEast.add(panel3);
		
		velXLabel = new JLabel("X velocity: 0");
		velXLabel.setHorizontalAlignment(JLabel.CENTER);
		panel3.add(velXLabel);
		
		velYLabel = new JLabel("Y velocity: 0");
		velYLabel.setHorizontalAlignment(JLabel.CENTER);
		panel3.add(velYLabel);
		
		velXSlider = new JSlider(JSlider.HORIZONTAL,-20,20,0);
		velXSlider.setMajorTickSpacing(10);
		velXSlider.setPaintTicks(true);
		velXSlider.setPaintLabels(true);
		velXSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				velXLabel.setText(String.format("X velocity: %d", velXSlider.getValue()));
				panelCenter.setWaveVelX(velXSlider.getValue());
				panelCenter.repositionWaves();
			}
		});
		panel3.add(velXSlider);
		
		velYSlider = new JSlider(JSlider.HORIZONTAL,-20,20,0);
		velYSlider.setMajorTickSpacing(10);
		velYSlider.setPaintTicks(true);
		velYSlider.setPaintLabels(true);
		velYSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				velYLabel.setText(String.format("Y velocity: %d", velYSlider.getValue()));
				panelCenter.setWaveVelY(-velYSlider.getValue());
				panelCenter.repositionWaves();
			}
		});
		panel3.add(velYSlider);
		
		// panel 4
		
		panel4 = new JPanel();
		panel4.setBorder(BorderFactory.createLineBorder(Color.black));
		panel4.setLayout(new GridLayout(1, 2));
		panelEast.add(panel4);
		
		framesLabel = new JLabel("Number of frames: ");
		framesLabel.setHorizontalAlignment(JLabel.CENTER);
		panel4.add(framesLabel);
		
		framesTextField = new JTextField("100");
		framesTextField.setHorizontalAlignment(JTextField.CENTER);
		panel4.add(framesTextField);
		
	}
	
	public static void main(String[] args) {
		SchFrame frame = new SchFrame();
		frame.setVisible(true);
		}
	}

