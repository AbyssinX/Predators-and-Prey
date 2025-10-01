
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import NEAT.Genome;
import World.Simulation;

/*
 * I want to implement and see the difference between the perfomance of Reinforcement Learning and Lotka-Volterra equations.
 */

public class Main {

    private static Timer timer;
    // private static WindowManager window;
    private static Simulation simulation;
    // public static List<Double> fitness_record = new ArrayList<>();

    public static void main(String[] args){


        simulation = new Simulation();
        // Genome.evaluateFitnessPredator(simulation);

        WindowManager.createWindow(simulation);

        timer = new Timer(0, simulation);
        timer.start();
        // System.out.println(Simulation.fitness_record);
    }
    
}
