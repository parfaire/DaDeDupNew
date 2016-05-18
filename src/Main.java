import controller.Controller;
import ui.MainWindow;

import javax.swing.*;
import java.io.*;

/**
 *  Main Class is where we set the parameters, the receptor of args command in order to bulk wiritng or reading
 *  These are the args parameters should be:
 *  args[0] = String mode => "WRITE", "WRITEKEEP", "READ", "CLEAN" (see below for more explanations)
 *  args[1] = int blockSize (in bits) => 4096
 *  args[2,4,6...even] = String hashFunction => "SHA-256", "SHA-1", "MD5"
 *  args[3,5,7..odd] = String fileName => "asd.txt", "abc.txt"
 *
 *  WRITE -> write bulk files, but erase it after each file gets written (for statistic purpose of individual files)
 *  WRITEKEEP -> write bulk files, and keep it.
 *  READ -> read bulk files.
 *  CLEAN -> clear all the files data.
 *  *All of the modes will automatically trigger the user interface of deduplication system
 *  where user can write and read file
 *
 *  *Default parameter is : 8192 bits blocksize and SHA-256 hashFunction
 */

public class Main {
    public static void main(String[] args) {
        //create the UI and get the controller
        MainWindow mw = new MainWindow();
        Controller c = mw.getController();
        //set default parameter
        c.setParameter(8192, "SHA-256");
        //get the arguments
        try{
            if (args.length > 0) {
                //WRITE mode
                if (args[0].equals("WRITE")) {
                    for (int i = 1; i < args.length; i+=3) {
                        c.setParameter(Integer.parseInt(args[i]), args[i+1]);
                        File f = new File(args[i+2]);
                        c.write(f.getName(), f);
                        c.clearData();
                    }
                    JOptionPane.showMessageDialog(null,"Writing done!");
                }
                //WRITEKEEP mode
                if (args[0].equals("WRITEKEEP")) {
                    for (int i = 1; i < args.length; i+=3) {
                        c.setParameter(Integer.parseInt(args[i]), args[i+1]);
                        File f = new File(args[i+2]);
                        c.write(f.getName(), f);
                    }
                    JOptionPane.showMessageDialog(null,"Writing done!");
                }
                //READ mode
                else if (args[0].equals("READ")) {
                    for (int i = 3; i < args.length; i+=2)
                        c.read(args[i],args[i+1]);
                    JOptionPane.showMessageDialog(null,"Reading done!");
                }
                //CLEAN mode
                else if (args[0].equals("CLEAN")) {
                    c.clearData();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
