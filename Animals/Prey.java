package Animals;
import java.awt.Color;
import java.util.List;
import java.util.Random;

import NEAT.Action;
import NEAT.FeedForwardNeuralNetwork;
import NEAT.Genome;
import NEAT.NodeGene;

public class Prey extends Cell{
    
    public final static int MAX_FOOD_BAR_VALUE = 100;
    public final static int MAX_WATER_BAR_VALUE = 100;
    // private static final int LIFETIME = 120;

    

    public Prey(Color color, int speed){ // I need to change the speed functionality 
        super(color, "prey");
        super.speed = speed;

        super.genome = new Genome(Genome.next_genome_id(), inputs, outputs);
        for (int id = 0; id < inputs; id++){
            NodeGene input_neuron = new NodeGene(id, NodeGene.Type.INPUT, 0, 0, 0, 0);
            super.genome.nodes.add(input_neuron);
        }
        for (int id = inputs; id < inputs + outputs; id++){
            NodeGene output_neuron = new NodeGene(id, NodeGene.Type.OUTPUT, 0, 0, 0, 1000);
            super.genome.nodes.add(output_neuron);
        }

        Random random = new Random();
        super.direction = Action.values()[random.nextInt(Action.values().length)];
        super.NeuralNetwork = FeedForwardNeuralNetwork.createFromGenome(genome);    // maybe i should leave it 

    }


    public static void sortByFitness(List<Prey> preys){
        
        preys.sort( (a, b) -> { return Double.compare(a.genome.fitness, b.genome.fitness); });
       
    }
    

    
}
