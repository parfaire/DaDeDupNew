import controller.Controller;
import ui.MainWindow;

import javax.swing.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        MainWindow mw = new MainWindow();
        Controller c = mw.getController();
        c.setParameter(8192, "SHA-256");
        try{
            if (args.length > 0) {
                //args[0] = mode => WRITE | READ | CLEAN
                //args[1 i] = blockSize in bits |4096|
                //args[2 i+1] = hashFunc |SHA-256|
                //args[3 i+2] = fileName
                if (args[0].equals("WRITE")) {
                    for (int i = 1; i < args.length; i+=3) {
                        c.setParameter(Integer.parseInt(args[i]), args[i+1]);
                        File f = new File(args[i+2]);
                        mw.getController().write(f.getName(), f);
                        mw.getController().clearData();
                    }
                    JOptionPane.showMessageDialog(null,"Writing done!");
                } else if (args[0].equals("READ")) {
                    for (int i = 3; i < args.length; i+=2)
                        mw.getController().read(args[i],args[i+1]);
                    JOptionPane.showMessageDialog(null,"Reading done!");
                } else if (args[0].equals("CLEAN")) {
                    c.clearData();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
