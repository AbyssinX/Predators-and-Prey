package NEAT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Animals.Predator;
import Animals.Prey;
import NEAT.NodeGene.Type;

/*
 * TODO
 * * Finish mutatation of weights
 * * Add speciation
 * * Elitism 
 * * 
 */


class Activation {
    public double sigmoid(double x){
        return 1 / (1 + Math.pow(Math.E, -x));
    }
}



class Configuration {
    double init_mean = 0;    
    double init_stdev = 1;   
    double min = -20;
    double max = 20;
    double mutation_rate = 0.2;
    double mutate_power = 1.2;
    double replace_rate = 0.05;


}


// class Individual {
//     Genome genome;
//     double fitness;
// }



public class Genome {
    // CONFIGURATIONS
    private static double mean = 0;    
    private static double stdev = 1;   
    private static double min = -20;
    private static double max = 20;
    private static double mutation_rate = 0.2;
    private static double mutate_power = 1.2;
    private static double replace_rate = 0.05;              // ? 
    private static double survival_threshold = 0.1;


    private static Random random = new Random();                   
    private final int genomeId;
    // private int globalInnovationNumber = 0;


    public List<NodeGene> nodes;
    public List<ConnectionGene> connections;

    private final int number_of_inputs;             // make static?
    private int number_of_hidden = 0;
    private final int number_of_outputs;

    public double fitness;



    public Genome(int genomeId, int inputs, int outputs){
        this.nodes = new ArrayList<>();             
        this.connections = new ArrayList<>();
        this.genomeId = genomeId;
        this.number_of_inputs = inputs;
        this.number_of_outputs = outputs;
    }

    // public Genome(int genomeId, List<NodeGene> nodes_given, List<ConnectionGene> connections_given){
    //     this.nodes = nodes_given;             
    //     this.connections = connections_given;
    //     this.genomeId = genomeId;
    //     int[] count_types = count_neurons();
    //     number_of_inputs = count_types[0];
    //     number_of_outputs = count_types[1];
    // }

















    
    public static void evaluateFitnessPredator(Predator predator) {
        // Build neural net from this genome                    // ???               
        // Run simulation
        // Return fitness (e.g., prey caught or survival time)
















        


        // return 0.0;
    }


    public static void evaluateFitnessPrey(Prey prey) {
        // Build neural net from this genome                    // ???               
        // Run simulation
        // Return fitness (e.g., prey caught or survival time)
















        


        // return 0.0;
    }



    /* --------------------------------------------------------------------------
    *  |                             MUTATION: STRUCTURAL                       |
    *  --------------------------------------------------------------------------
    */ 

    private static void mutate(Genome genome) {
        // Randomly add node, add connection, or perturb weights
        // What probabilities should I set for each mutation?
        double chance = random.nextDouble(); 
        if (chance < 0.5){
            add_link(genome);
        } else if (chance < 0.6){
            remove_link(genome);
        } else if (chance < 0.75){
            add_neuron(genome);
        } else {
            remove_neuron(genome);
        }

        if (random.nextDouble() < 0.6){
            mutate_weights(genome);           // ?
        }
        
    }


    
    private static void add_link(Genome genome){
        int input =  random.nextInt(genome.nodes.size() + 1);
        int output = random.nextInt(genome.nodes.size() - genome.number_of_inputs + 1) + genome.number_of_inputs;

  
        if (create_a_cycle(input, output, genome)) return;
  
        ConnectionGene possible_connection = connected(input, output, genome);  
        if (possible_connection.exists){ 
            possible_connection.enabled = true;           // what about links that got disabled during the addition of a neuron?
            return;
        }

        ConnectionGene new_connection = new ConnectionGene(input, output, 0 , true); // what weight should there be? 
        genome.connections.add(new_connection);


    }


    // ---------------- Helper Functions

    private static ConnectionGene connected(int input, int output, Genome genome){
        for (ConnectionGene connection : genome.connections){
            if (connection.inpNode == input && connection.outNode == output){
                return connection;
            }
        }
        return new ConnectionGene(0,0, 0, false); // none existent connection
    }

    private List<ConnectionGene> connectedToHidden(int neuronId){
        List<ConnectionGene> connected = new ArrayList<>();
        for (ConnectionGene connection : this.connections){
            if (connection.inpNode == neuronId){
                connected.add(connection);
            } else if (connection.outNode == neuronId){
                connected.add(connection);
            }
        }
        return connected; // none existent connection
    }

    private static boolean create_a_cycle(int input, int output, Genome genome){      // TODO: i need to improve this. The function checks only very basic cycles.
        for (ConnectionGene connection : genome.connections){
            if (connection.inpNode == output && connection.outNode == input){
                return true;
            }
        }
        return false;
    }


    // -----------------

    private static void remove_link(Genome genome){
        if (genome.connections.isEmpty()) return;

        int connection_remove = random.nextInt(genome.connections.size());
        genome.connections.remove(connection_remove); 
    }


    private static void add_neuron(Genome genome){
        if (genome.connections.isEmpty()) return;

        ConnectionGene old_connection = genome.connections.get(random.nextInt(genome.connections.size()));
        old_connection.enabled = false;

        NodeGene new_neuron = new NodeGene(genome.nodes.size()+1, Type.HIDDEN, 0, 0); // activation ? bias?
        genome.nodes.add(new_neuron);
        genome.number_of_hidden++;

        // genome.globalInnovationNumber += 1;
        ConnectionGene incoming = new ConnectionGene(old_connection.inpNode, new_neuron.id, 1, true);               
        // genome.globalInnovationNumber += 1;
        ConnectionGene outgoing = new ConnectionGene(new_neuron.id, old_connection.outNode, old_connection.weight, true);  
        genome.connections.add(incoming);
        genome.connections.add(outgoing);
    }

    private static void remove_neuron(Genome genome){
        if (genome.number_of_hidden == 0) return;

        NodeGene neuron = genome.nodes.get(random.nextInt(genome.nodes.size() - genome.number_of_inputs - genome.number_of_outputs) + genome.number_of_inputs + genome.number_of_outputs);

        for (ConnectionGene connection : genome.connectedToHidden(neuron.id)){
            genome.connections.remove(connection);                           
        }

        genome.nodes.remove(neuron);
        genome.number_of_hidden--;
        
    }



    

    /* --------------------------------------------------------------------------
    *  |                             MUTATION: NON-STRUCTURAL                   |
    *  --------------------------------------------------------------------------
    */ 

    private static void mutate_weights(Genome genome){     // How to combine lower functions into one?
        for (ConnectionGene connection : genome.connections){
            if (random.nextDouble() < 0.2){
                connection.weight = mutate_delta(connection.weight);
            }
        }
        // do i update bias?
    }


    private static double new_value(){
        return clamp(random.nextGaussian(mean, stdev));
    }

    private static double mutate_delta(double value){
        double delta = clamp(random.nextGaussian(0, mutate_power));
        return clamp(value + delta);
    }
    // there is also a chance that a value will be replaced by a new random value.

    private static double clamp(double x){
        return Math.min(max, Math.max(min, x));
    }



 

    /* --------------------------------------------------------------------------
    *  |                             CROSSOVER                                  |
    *  --------------------------------------------------------------------------
    */ 

    private static Genome crossover(Genome dominant, Genome recessive) {
        // Align genes by id and combine
        Genome offspring = new Genome(next_genome_id(), dominant.number_of_inputs, dominant.number_of_outputs);

        // Add neurons
        for (NodeGene dominant_neuron : dominant.nodes){
            int id = dominant_neuron.id;
            NodeGene recessive_neuron = recessive.findNeuron(id);
            if (recessive_neuron.exists){
                offspring.nodes.add(crossoverNeuron(dominant_neuron, recessive_neuron));
            } else {
                offspring.nodes.add(dominant_neuron);
            }
        }

        // Add connections
        for (ConnectionGene dominant_connection : dominant.connections){
            int input = dominant_connection.inpNode;
            int output = dominant_connection.outNode;
            ConnectionGene recessive_connection = recessive.findConnection(input, output);
            if (recessive_connection.exists){
                offspring.connections.add(crossoverConnection(dominant_connection, recessive_connection));
            } else {
                offspring.connections.add(dominant_connection);
            }
        }


        return offspring;
    }

    // ----------------- Helper Functions

    public NodeGene findNeuron(int x){
        for (NodeGene neuron : this.nodes){
            if (neuron.id == x){
                return neuron;
            }
        }
        return new NodeGene(-1, Type.INPUT, 0, 0); // none existent neuron
    }


    private ConnectionGene findConnection(int input, int output){
        for (ConnectionGene connection : this.connections){
            if (connection.inpNode == input && connection.outNode == output){
                return connection;
            }
        }
        return new ConnectionGene(0, 0, 0, false); // none existent connection
    }


    // --------------------------   







    private static NodeGene crossoverNeuron(NodeGene a, NodeGene b) {
        assert(a.id == b.id);
        int new_id = a.id;
        double bias = (random.nextDouble() > 0.5) ? a.bias : b.bias;                    
        double activation = (random.nextDouble() > 0.5) ? a.activation : b.activation;
        
        return new NodeGene(new_id, a.type, bias, activation);
    }

    private static ConnectionGene crossoverConnection(ConnectionGene a, ConnectionGene b) {
        assert(a.inpNode == b.inpNode);
        assert(a.outNode == b.outNode);
        double weight = (random.nextDouble() > 0.5) ? a.weight : b.weight;
        boolean enabled = (random.nextDouble() > 0.5) ? a.enabled : b.enabled;    // TODO: set a chance that an inherited gene is disabled if it is disabld in either parent
                                           
        return new ConnectionGene(a.inpNode, a.outNode, weight, enabled);
    }








    // private static final double compatibilityThreshold;
    // I need an ordered list of species too.

    private static double compatibilityDistance(Genome genome1, Genome genome2){  // required to speciate and allow for the preservation of innovation

        return 0.0; // (c1 * E + c2 * D) / N + c3 * W      where E is the number of excess genes, 
                    //                                           D is the number of disjoin genes,
                    //                                           W is the average weight distances of matching genes, including disables genes,
                    //                                           N is the number of genes in the bigger genome (can be one if there is less than 20 genes in both genomes),
                    //                                           c1,c2,c3 are parameters to adjust the imporance of the three factors.
    }







    private int[] count_neurons(){
        int counter_input = 0;
        int counter_output = 0;
        for (NodeGene neuron : this.nodes){
            if (neuron.type == Type.INPUT){
                counter_input++;
            } else if (neuron.type == Type.OUTPUT){
                counter_output++;
            } 
        }
        return new int[] {counter_input, counter_output};
    }

    


    private static int nextGenomeID = 0;
    public static int next_genome_id(){
        nextGenomeID += 1;
        return nextGenomeID;
    }








    /* --------------------------------------------------------------------------
    *  |                             REPRODUCTION                               |
    *  --------------------------------------------------------------------------
    */ 

    // How do i combine the following two functions?
    public static void reproducePredator(List<Predator> predators){
        Predator.sortByFitness(predators);

        int reproduction_cutoff = (int) Math.ceil(survival_threshold * predators.size());

        // List<Predator> new_generation = new ArrayList<>();
        int modified_population_size = predators.size() - reproduction_cutoff;

        for (int i = 0; i < modified_population_size; i++){
            Predator parent1 = predators.get(random.nextInt(reproduction_cutoff));
            Predator parent2 = predators.get(random.nextInt(reproduction_cutoff));
            Genome offspring_genome = crossover(parent1.genome, parent2.genome);
            mutate(offspring_genome);
            predators.get(i + reproduction_cutoff).genome = offspring_genome;

        }

    }

    public static void reproducePrey(List<Prey> preys){
        Prey.sortByFitness(preys);

        int reproduction_cutoff = (int) Math.ceil(survival_threshold * preys.size());

        int modified_population_size = preys.size() - reproduction_cutoff;

        for (int i = 0; i < modified_population_size; i++){
            Prey parent1 = preys.get(random.nextInt(reproduction_cutoff));
            Prey parent2 = preys.get(random.nextInt(reproduction_cutoff));
            Genome offspring_genome = crossover(parent1.genome, parent2.genome);
            mutate(offspring_genome);
            preys.get(i + reproduction_cutoff).genome = offspring_genome;

        }

    }


    /* --------------------------------------------------------------------------
    *  |                             For Neural Network                         |
    *  --------------------------------------------------------------------------
    */ 

    public List<Integer> make_input_ids(){
        List<Integer> output = new ArrayList<>();

        for (NodeGene gene : this.nodes){
            if (gene.type == Type.INPUT) output.add(gene.id);
        }

        return output;
    }


    public List<Integer> make_output_ids(){
        List<Integer> output = new ArrayList<>();

        for (NodeGene gene : this.nodes){
            if (gene.type == Type.OUTPUT) output.add(gene.id);
        }

        return output;
    }


    public List<List<Integer>> createLayers(List<Integer> inputs, Genome genome){ // not sure this is gonna work
        List<List<Integer>> layers = new ArrayList<>();
        List<Integer> layer = new ArrayList<>();
        layers.add(inputs);

        for (int i = 0; i < layers.size(); i++){                       
            for (int id : layers.get(i)){
                List<Integer> connectedTo = connectedTo(id, genome);

                if (!connectedTo.isEmpty()){
                    for (int hidden_id : connectedTo){
                        if (hidden_id >= number_of_inputs && hidden_id <= (number_of_inputs + number_of_outputs)) { // does this properly work?
                            continue;
                        }
                        layer.add(hidden_id);
                    }
                }

            }
            if (!layer.isEmpty()){
                layers.add(layer);
                layer.clear();
            }

        }
        layers.remove(0);

        return layers;


    }

    private List<Integer> connectedTo(int neuronId, Genome genome){

        List<Integer> connectedTo = new ArrayList<>();

        for (ConnectionGene connection : genome.connections){
            if (connection.inpNode == neuronId){
                connectedTo.add(connection.outNode);
            } 
        }

        return connectedTo; 
    }
}


