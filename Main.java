import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

/*
 * I want to implement and see the difference between the perfomance of Reinforcement Learning and Lotka-Volterra equations.
 */

public class Main {

    private static Timer timer;
    // private static WindowManager window;
    private static Simulation simulation;

    public static void main(String[] args){
        simulation = new Simulation();

        WindowManager.createWindow(simulation);

        timer = new Timer(70, simulation);
        timer.start();

    }
    
}
