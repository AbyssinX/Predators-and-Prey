
import javax.swing.JFrame;

import World.Simulation;

public class WindowManager {

    private static JFrame frame;

    public static void createWindow(Simulation simulation){

        frame = new JFrame("Prey and Predator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setContentPane(simulation);
        // frame.setSize(450,450);
        frame.setVisible(true);
        frame.pack();

    }
}
