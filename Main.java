
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import NEAT.Genome;
import World.Graph;
import World.Simulation;

/*
 * I want to implement and see the difference between the perfomance of Reinforcement Learning and Lotka-Volterra equations.
 */

public class Main {

    private static Timer timer;
    private static Timer timer2;
    // private static WindowManager window;
    private static Simulation simulation;
    private static Graph graph;
    // public static List<Double> fitness_record = new ArrayList<>();

    public static void main(String[] args){


        simulation = new Simulation();
        graph = new Graph();
        // Genome.evaluateFitnessPredator(simulation);

        WindowManager.createWindow(simulation);

        timer = new Timer(0, simulation);
        // timer2 = new Timer(0, graph);
        timer.start();
        // timer2.start();
        // System.out.println(Simulation.fitness_record);
    }
    
}
