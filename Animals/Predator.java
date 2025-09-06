package Animals;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import NEAT.Action;
import NEAT.ConnectionGene;
import NEAT.FeedForwardNeuralNetwork;
import NEAT.Genome;
import NEAT.NodeGene;

public class Predator extends Cell{
    
    public final static int MAX_FOOD_BAR_VALUE = 20;
    public final static int MAX_WATER_BAR_VALUE = 100;
    // private static final int LIFETIME = 100;
    
    public int staying_alive = 0;
    

    public Predator(Color color, int speed){  // I need to change the speed functionality 
        super(color, "predator");
        super.speed = speed;

        super.genome = new Genome(Genome.next_genome_id(), inputs, outputs);
        for (int id = 0; id < inputs; id++){
            NodeGene input_neuron = new NodeGene(id, NodeGene.Type.INPUT, 0, 0);
            super.genome.nodes.add(input_neuron);
        }
        for (int id = inputs; id < inputs + outputs; id++){
            NodeGene output_neuron = new NodeGene(id, NodeGene.Type.OUTPUT, 0, 0);
            super.genome.nodes.add(output_neuron);                                                       // potentially could add links from the very start
        }

        // Make a fully connected graph
        for (int input_id = 0; input_id < inputs; input_id++){
            for (int output_id = inputs; output_id < inputs + outputs; output_id++){
                genome.connections.add(new ConnectionGene(input_id, output_id, Genome.new_value(), true));
            }
        }




        Random random = new Random();
        super.direction = Action.values()[random.nextInt(Action.values().length)];

        // super.NeuralNetwork = FeedForwarNeuralNetwork.createFromGenome(genome);
    }

    
    public static void sortByFitness(List<Predator> predators){

        predators.sort( (a, b) -> { return Double.compare(a.genome.fitness, b.genome.fitness); });
        
    }




}
