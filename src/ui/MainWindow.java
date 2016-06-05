package ui;

import controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Main Frame/Window of the User Interface that contains everything.
 */
public class MainWindow extends JFrame implements WindowListener{

	private final Dimension MIN_FRAME_SIZE = new Dimension(1280, 640);
	
	private InterfacePanel interfacePanel;
	private StatusPanel statusPanel;
	private Controller controller;

	public InterfacePanel getInterfacePanel() { return interfacePanel; }

	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

	public Controller getController() {
		return controller;
	}
	
	/**
	 * Constructor of mainwindow, assigning the controller and setting up all components.
	 */
	public MainWindow() {
		// controller
		this.setupComponents();
		controller = new Controller(this);
		setVisible(true);
		addWindowListener(this);
	}

	/**
	 * Preparing to attach two main Panels in itself (InterfacePanel and StatusPanel) and configure the layout.
	 */
	private void setupComponents() {
		this.setMinimumSize(MIN_FRAME_SIZE);
		
		// Panels
		this.interfacePanel = new InterfacePanel(this);
		this.statusPanel = new StatusPanel(this);
		
		// Layout
		this.setLayout(new BorderLayout());
		this.getContentPane().add(this.interfacePanel, BorderLayout.PAGE_START);
		this.interfacePanel.setPreferredSize(new Dimension(this.getWidth(), (int)(this.getHeight()*0.6)));
		this.getContentPane().add(this.statusPanel, BorderLayout.CENTER);
		this.statusPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
		
		
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		System.exit(0);
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
