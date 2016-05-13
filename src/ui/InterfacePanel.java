package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;


public class InterfacePanel extends JPanel {
	private final String INTERFACE_LABEL = "Deduplication";
	private final String READ_GROUP_LABEL = "Read";
    private final String COMPARE_LABEL = "Compare";
    private final String READ_LABEL1 = "Path";
    private final String READ_LABEL2 = "Filename";
	private final String WRITE_GROUP_LABEL = "Write";
    private final String WRITE_LABEL1 = "Input";
	private final String READ_BUTTON_LABEL = ".....READ";
	private final String WRITE_BUTTON_LABEL = ".....WRITE";
    private final String DIR1_BUTTON_LABEL = ".....DIR1";
    private final String DIR2_BUTTON_LABEL = ".....DIR2";
    private final String COMPARE_SUBMIT_LABEL = "Check the Difference!";
    private final String READ_SUBMIT_LABEL = "Get the file!";
    private final String WRITE_SUBMIT_LABEL = "Store the file!";
    private final String SERVER_LIST_LABEL = "List of Files";
	private final int TEXT_FIELD_WTDTH = 72;
	
	private MainWindow mainWindow;
    private JPanel interfacePanel;
    private JPanel centerPanel;

    private JPanel readPanel;
	private JTextField readPathText;
    private JTextField readFilename;
	private JButton readFileButton;
    private JLabel readLbl;
    private JLabel readLbl2;
    private JButton readSubmit;

    private JScrollPane scrollPane;
    private JList<String> list;

    private JPanel writePanel;
	private	JTextField writePathText;
	private JButton writePathButton;
    private JLabel writeLbl;
    private JButton writeSubmit;

    private JPanel comparePanel;
    private	JTextField dir1Text;
    private	JTextField dir2Text;
    private JButton dir1Browse;
    private JButton dir2Browse;
    private JLabel dir1Lbl;
    private JLabel dir2Lbl;
    private JButton btnCompare;
	
    public InterfacePanel(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		setComponents();
	}

    public JList<String> getList(){
        return list;
    }
	
	private void setComponents() {
        readFilename = new JTextField("", TEXT_FIELD_WTDTH);
		readPathText = new JTextField("", TEXT_FIELD_WTDTH);
		//readPathText.setEditable(false);
        readSubmit = new JButton(READ_SUBMIT_LABEL);
        readSubmit.addActionListener(new EventListener());
		readFileButton = new JButton(READ_BUTTON_LABEL);
		readFileButton.setPreferredSize(new Dimension(20, 20));
        readFileButton.addActionListener(new EventListener());
        readLbl = new JLabel(READ_LABEL1);
        readLbl2 = new JLabel(READ_LABEL2);
		readPanel = new JPanel();
        readPanel.add(readLbl);
		readPanel.add(readPathText);
		readPanel.add(readFileButton);
        readPanel.add(readLbl2);
        readPanel.add(readFilename);
        readPanel.add(readSubmit);
		TitledBorder inputTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), READ_GROUP_LABEL);
		readPanel.setBorder(inputTitle);
		
		writePathText = new JTextField("", TEXT_FIELD_WTDTH);
		writePathText.setEditable(false);
        writeSubmit = new JButton(WRITE_SUBMIT_LABEL);
        writeSubmit.addActionListener(new EventListener());
		writePathButton = new JButton(WRITE_BUTTON_LABEL);
		writePathButton.setPreferredSize(new Dimension(20, 20));
        writePathButton.addActionListener(new EventListener());
        writeLbl = new JLabel(WRITE_LABEL1);
		writePanel = new JPanel();
        writePanel.add(writeLbl);
		writePanel.add(writePathText);
		writePanel.add(writePathButton);
        writePanel.add(writeSubmit);
		TitledBorder outputTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), WRITE_GROUP_LABEL);
		writePanel.setBorder(outputTitle);


        list = new JList<>();
        scrollPane = new JScrollPane(list);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        TitledBorder listTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), SERVER_LIST_LABEL);
        scrollPane.setBorder(listTitle);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click detected
                    int idx = list.getSelectedValue().indexOf("(");
                    mainWindow.getInterfacePanel().setReadFilename(list.getSelectedValue().substring(0,idx-1));
                }
            }
        });

        comparePanel = new JPanel();
        dir1Text = new JTextField("", TEXT_FIELD_WTDTH);
        dir2Text = new JTextField("", TEXT_FIELD_WTDTH);
        dir1Browse  = new JButton(DIR1_BUTTON_LABEL);
        dir1Browse.setPreferredSize(new Dimension(20, 20));
        dir1Browse.addActionListener(new EventListener());
        dir2Browse = new JButton(DIR2_BUTTON_LABEL);
        dir2Browse.setPreferredSize(new Dimension(20, 20));
        dir2Browse.addActionListener(new EventListener());
        dir1Lbl = new JLabel("Dir 1");
        dir2Lbl = new JLabel("Dir 2");
        btnCompare = new JButton(COMPARE_SUBMIT_LABEL);
        btnCompare.addActionListener(new EventListener());
        comparePanel.add(dir1Lbl);
        comparePanel.add(dir1Text);
        comparePanel.add(dir1Browse);
        comparePanel.add(dir2Lbl);
        comparePanel.add(dir2Text);
        comparePanel.add(dir2Browse);
        comparePanel.add(btnCompare);
        TitledBorder compareTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), COMPARE_LABEL);
        comparePanel.setBorder(compareTitle);


        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3,1));
        centerPanel.add(readPanel);
        centerPanel.add(writePanel);
        centerPanel.add(comparePanel);

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
        interfacePanel.add(centerPanel, BorderLayout.CENTER);
        interfacePanel.add(scrollPane, BorderLayout.EAST);
        scrollPane.setPreferredSize(new Dimension(290,320));
		TitledBorder interfaceTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), INTERFACE_LABEL);
		interfacePanel.setBorder(interfaceTitle);

		
		setLayout(new BorderLayout());
		add(interfacePanel, BorderLayout.CENTER);
		
		setVisible(true);
	}

    private class EventListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            if(e.getActionCommand().equals(READ_BUTTON_LABEL)) {
                JFileChooser opener = new JFileChooser();
                opener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                opener.setAcceptAllFileFilterUsed(false);
                opener.setFileHidingEnabled(false);
                if(opener.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                    readPathText.setText(opener.getSelectedFile().toString());
                }
            }else if (e.getActionCommand().equals(WRITE_BUTTON_LABEL)) {
                JFileChooser opener = new JFileChooser();
                opener.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                opener.setFileHidingEnabled(false);
                if(opener.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                    writePathText.setText(opener.getSelectedFile().toString());
                }
            }else if (e.getActionCommand().equals(DIR1_BUTTON_LABEL)) {
                JFileChooser opener = new JFileChooser();
                opener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                opener.setFileHidingEnabled(false);
                if(opener.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                    dir1Text.setText(opener.getSelectedFile().toString());
                }
            }else if (e.getActionCommand().equals(DIR2_BUTTON_LABEL)) {
                JFileChooser opener = new JFileChooser();
                opener.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                opener.setFileHidingEnabled(false);
                if(opener.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                    dir2Text.setText(opener.getSelectedFile().toString());
                }
            }else if (e.getActionCommand().equals(READ_SUBMIT_LABEL)) {
                if(readPathText.getText().equals("") || readFilename.getText().equals("") ){
                    JOptionPane.showMessageDialog(null, "Please input the file.");
                }else {
                    long startTime = System.nanoTime();
                    mainWindow.getController().read(readPathText.getText(),readFilename.getText());
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
                    JOptionPane.showMessageDialog(null, "File is created..(within: "+duration+" ms)");
                    readFilename.setText("");
                    readPathText.setText("");
                }
            }else if (e.getActionCommand().equals(WRITE_SUBMIT_LABEL)) {
                if(writePathText.getText().equals("") ){
                    JOptionPane.showMessageDialog(null, "Please input the file.");
                }else{
                    long startTime = System.nanoTime();
                    File f = new File(writePathText.getText());
                    try {
                        mainWindow.getController().write(f.getName(),f);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
                    JOptionPane.showMessageDialog(null, "File is stored..(within: "+duration+" ms)");
                    writePathText.setText("");
                }
            }else if (e.getActionCommand().equals(COMPARE_SUBMIT_LABEL)) {
                if(dir1Text.getText().equals("") || dir2Text.getText().equals("")   ){
                    JOptionPane.showMessageDialog(null, "Please input the file.");
                }else{
                    mainWindow.getController().compareTwoDir(dir1Text.getText(),dir2Text.getText());
                }
            }
            updateUI();
        }
    }
    private void setReadFilename(String filename){
        readFilename.setText(filename);
    }
}
