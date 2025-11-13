import javax.swing.Timer;
import World.Simulation;

/*
 * I want to implement and see the difference between the perfomance of Reinforcement Learning and Lotka-Volterra equations.
 */

public class Main {

    private static Timer timer;
    private static Simulation simulation;

    public static void main(String[] args){

        simulation = new Simulation();
        WindowManager.createWindow(simulation);

        timer = new Timer(5, simulation);
        timer.start();

    }
    
}
