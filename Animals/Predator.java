package Animals;

import java.awt.Color;
import java.util.List;
import java.util.Random;

import NEAT.Action;
import NEAT.ConnectionGene;
import NEAT.Genome;
import NEAT.Mutation;
import NEAT.NodeGene;
import NEAT.FeedForwardNeuralNetwork;

public class Predator extends Cell{
    
    public final static int MAX_FOOD_BAR_VALUE = 1000;
    public final static int MAX_WATER_BAR_VALUE = 100;
    // private static final int LIFETIME = 100;

    public Action last_action;
    
    public int staying_alive = 0;
    public int preysEaten = 0;
    

    public Predator(Color color, int speed){  // I need to change the speed functionality 
        super(color, "predator");
        super.speed = speed;

        super.genome = new Genome(Genome.next_genome_id(), inputs, outputs);
        for (int id = 0; id < inputs; id++){
            NodeGene input_neuron = new NodeGene(id, NodeGene.Type.INPUT, 0, 0, 0, 0);
            super.genome.nodes.add(input_neuron);
        }
        for (int id = inputs; id < inputs + outputs; id++){
            NodeGene output_neuron = new NodeGene(id, NodeGene.Type.OUTPUT, 0, 0, 4, 1000);
            super.genome.nodes.add(output_neuron);                                                      
        }

        // Make a fully connected graph
        for (int input_id = 0; input_id < inputs; input_id++){
            for (int output_id = inputs; output_id < inputs + outputs; output_id++){
                int innovationNumber = Mutation.getInnovationConnection(input_id, output_id);
                genome.connections.add(new ConnectionGene(input_id, output_id, Mutation.new_value(), true, innovationNumber));
            }
        }




        Random random = new Random();
        super.direction = Action.values()[random.nextInt(Action.values().length)];

        super.NeuralNetwork = FeedForwardNeuralNetwork.createFromGenome(genome);
    }

    
    public static void sortByFitness(List<Predator> predators){

        predators.sort( (a, b) -> { return Double.compare(b.genome.fitness, a.genome.fitness); });
        
    }

    public Predator cloneDeep(boolean stats) {
        Predator clone = new Predator(this.color, this.speed);
        clone.genome = this.genome.cloneDeep(); // deep copy genes
        clone.NeuralNetwork = FeedForwardNeuralNetwork.createFromGenome(clone.genome);

        if (stats) {
            clone.staying_alive = this.staying_alive;
            clone.preysEaten = this.preysEaten;
            clone.genome.distanceToEnemy = this.genome.distanceToEnemy;
            clone.genome.countEnemies = this.genome.countEnemies;
            clone.genome.facingEnemyCount = this.genome.facingEnemyCount;
            clone.FOOD_BAR = this.FOOD_BAR;
            clone.direction = this.direction;
            
        }

        return clone;
    }


}
