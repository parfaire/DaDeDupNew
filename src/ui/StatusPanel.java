package ui;

import java.awt.*;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * StatusPanel provide user interface with status information of the system. Such as its memory usage and space usage.
 */
public class StatusPanel extends JPanel {
    private long total;
    private long expectedTotal;

	private final String LABEL = "Status";
	private final int TEXT_FIELD_WTDTH = 40;

    private MainWindow mainWindow;

	private JPanel panelDdt;
	private JPanel panelRecord;
	private JPanel panelFolder;
    private JPanel panelStorage;
    private JPanel panelTotal;
    private JPanel panelConf;
    private JPanel panelLeft;

	private JTextField tfDdt;
	private JTextField tfRecord;
    private JTextField tfFolder;
	private JTextField tfStorage;

    private JLabel lblDdt;
    private JLabel lblRecord;
    private JLabel lblFolder;
    private JLabel lblStorage;
    private JLabel lblTotal;
    private JLabel lblExpectedTotal;
    private JLabel lblDedup;
    private JLabel lblMemory;

    private JButton btnClear;
    private NumberFormat format = NumberFormat.getInstance();


    //SETTER TO ALLOW OTHER CLASSES OUTSIDE THIS CLASS UPDATE THE STATUS
    public void setLblDedup() {
        double d = (double)expectedTotal/(double)total;
        String decimal4 = String.format("%.4f",d);
        this.lblDedup.setText("Deduplication ratio : "+ decimal4);
    }

    public void setLblExpectedTotal(long l) {
        expectedTotal = l;
        this.lblExpectedTotal.setText("Expected Total : " + format.format(l / 1024) +" KB");
    }

    public void setLblTotal(long l) {
        total = l;
        this.lblTotal.setText("Total : " + format.format(l / 1024) +" KB");
    }

    public void setLblStorage(long l) {
        this.lblStorage.setText(format.format(l / 1024) +" KB");
    }

    public void setLblDdt(long l) {
        this.lblDdt.setText(format.format(l / 1024) +" KB");
    }

    public void setLblFolder(long l) {
        this.lblFolder.setText(format.format(l / 1024) +" KB");
    }

    public void setLblRecord(long l) {
        this.lblRecord.setText(format.format(l / 1024) +" KB");
    }

    public void setLblMemory(String s) {
        this.lblMemory.setText(s);
    }

    public void setTfDdt(String s) {
        this.tfDdt.setText(s);
    }

    public void setTfRecord(String s) {
        this.tfRecord.setText(s);
    }

    public void setTfFolder(String s) {
        this.tfFolder.setText(s);
    }

    public void setTfStorage(String s) {
        this.tfStorage.setText(s);
    }

	//END OF SETTER

    /**
     * Constructor. Assigning its main parent to itself and instantiate all the UI components.
     * @param mainWindow
     */
	StatusPanel(MainWindow mainWindow) {
        this.mainWindow=mainWindow;
		this.setComponents();
	}

    /**
     * Instantiate  all the UI components and configure the layout.
     */
	private void setComponents() {
		//title
		TitledBorder joblistTitle = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.LABEL);
		this.setBorder(joblistTitle);

		//components
		tfDdt = new JTextField("", TEXT_FIELD_WTDTH);
        tfDdt.setEditable(false);
		tfRecord = new JTextField("", TEXT_FIELD_WTDTH);
        tfRecord.setEditable(false);
        tfFolder = new JTextField("", TEXT_FIELD_WTDTH);
        tfFolder.setEditable(false);
		tfStorage = new JTextField("", TEXT_FIELD_WTDTH);
        tfStorage.setEditable(false);
        lblDdt = new JLabel("");
        lblRecord = new JLabel("");
        lblFolder = new JLabel("");
        lblStorage = new JLabel("");
        lblTotal = new JLabel("");
        lblExpectedTotal = new JLabel("");
        lblDedup = new JLabel("");
        lblMemory = new JLabel("");

		panelDdt = new JPanel();
		panelDdt.setLayout(new FlowLayout());
		panelDdt.add(tfDdt);
        panelDdt.add(lblDdt);
        TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "DDT");
        panelDdt.setBorder(title);

		panelRecord = new JPanel();
        panelRecord.setLayout(new FlowLayout());
        panelRecord.add(tfRecord);
        panelRecord.add(lblRecord);
        title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Record");
        panelRecord.setBorder(title);

        panelFolder = new JPanel();
        panelFolder.setLayout(new FlowLayout());
        panelFolder.add(tfFolder);
        panelFolder.add(lblFolder);
        title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Folder");
        panelFolder.setBorder(title);

		panelStorage = new JPanel();
		panelStorage.setLayout(new FlowLayout());
        panelStorage.add(tfStorage);
        panelStorage.add(lblStorage);
        title= BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Storage");
        panelStorage.setBorder(title);

        panelTotal = new JPanel();
        panelTotal.setLayout(new GridLayout(3,1));
        panelTotal.add(lblExpectedTotal);
        panelTotal.add(lblTotal);
        panelTotal.add(lblDedup);

        panelConf = new JPanel();
        panelConf.setLayout(new GridLayout(2,1));
        btnClear = new JButton("Clear data");
        btnClear.addActionListener(e -> mainWindow.getController().clearData());
        //panelConf.add(btnSave);
        panelConf.add(btnClear);
        panelConf.add(panelTotal);

        panelLeft = new JPanel();
        panelLeft.setLayout(new GridLayout(4,1));
        panelLeft.add(panelDdt);
        panelLeft.add(panelRecord);
        panelLeft.add(panelFolder);
        panelLeft.add(panelStorage);

        add(panelLeft,BorderLayout.CENTER);
        add(panelConf,BorderLayout.EAST);
        add(lblMemory, BorderLayout.WEST);
		setVisible(true);
	}
}
