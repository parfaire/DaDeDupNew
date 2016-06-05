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

/**
 * InterfacePanel provide user interface for WRITE and READ to/from Data Deduplication system.
 * It also provides the list information of the files that have been written.
 */
public class InterfacePanel extends JPanel {
	private final String INTERFACE_LABEL = "Deduplication";
	private final String READ_GROUP_LABEL = "Read";
    private final String READ_LABEL1 = "Path";
    private final String READ_LABEL2 = "Filename";
	private final String WRITE_GROUP_LABEL = "Write";
    private final String WRITE_LABEL1 = "Input";
	private final String READ_BUTTON_LABEL = ".....READ";
	private final String WRITE_BUTTON_LABEL = ".....WRITE";
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

    /**
     * Constructor. Assigning its main parent to itself and instantiate all the UI components.
     * @param mainWindow
     */
    public InterfacePanel(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		setComponents();
	}

    /**
     * A list of file/folder names that have been written to the system.
     * @return information list of the files that have been written.
     */
    public JList<String> getList(){
        return list;
    }

    /**
     * Instantiate  all the UI components and configure the layout.
     */
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

        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2,1));
        centerPanel.add(readPanel);
        centerPanel.add(writePanel);

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

    /**
     * An event listener of "CLICK" to handle buttons.
     */
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
            }
            updateUI();
        }
    }

    /**
     * To automatically fill the Read inputbox with existing filename chosen.
     * @param filename
     */
    private void setReadFilename(String filename){
        readFilename.setText(filename);
    }
}
