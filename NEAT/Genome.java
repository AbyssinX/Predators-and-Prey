package NEAT;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import NEAT.NodeGene.Type;
import World.World;

/*
 * TODO
 * * Add speciation
 * * 
 * * 
 */




// class Activation {
//     public double sigmoid(double x){
//         return 1 / (1 + Math.pow(Math.E, -x));
//     }
// }

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
    double mean = 0;    
    double stdev = 1;   
    double min = -20;
    double max = 20;
    double mutation_rate = 0.2;
    double mutate_power = 1.2;
    double replace_rate = 0.05;


    private static Random random = new Random();                   
    private final int genomeId;
    // private int globalInnovationNumber = 0;


    public List<NodeGene> nodes;
    public List<ConnectionGene> connections;

    private final int number_of_inputs;
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

    public Genome(int genomeId, List<NodeGene> nodes_given, List<ConnectionGene> connections_given){
        this.nodes = nodes_given;             
        this.connections = connections_given;
        this.genomeId = genomeId;
        int[] count_types = count_neurons();
        number_of_inputs = count_types[0];
        number_of_outputs = count_types[1];
    }

















    
    public double evaluateFitness(World env) {
        // Build neural net from this genome                    // ???               
        // Run simulation
        // Return fitness (e.g., prey caught or survival time)
















        


        return 0.0;
    }



    /* --------------------------------------------------------------------------
    *  |                             MUTATION: STRUCTURAL                       |
    *  --------------------------------------------------------------------------
    */ 

    private void mutate(Genome genome) {
        // Randomly add node, add connection, or perturb weights
        // What probabilities should I set for each mutation?
        if (random.nextDouble() < 0.2){
            add_link(genome);
        }
        if (random.nextDouble() < 0.1){
            remove_link(genome);
        }
        if (random.nextDouble() < 0.1){
            add_neuron(genome);
        }
        if (random.nextDouble() < 0.1){
            remove_neuron(genome);
        }
        if (random.nextDouble() < 0.1){
            mutate_weights(genome);           // ?
        }
        
    }


    
    private void add_link(Genome genome){
        int input =  random.nextInt(genome.nodes.size() + 1);
        int output = random.nextInt(genome.nodes.size() - genome.number_of_inputs + 1) + genome.number_of_inputs;

  
        if (create_a_cycle(input, output)) return;
  
        ConnectionGene possible_connection = connected(input, output);  
        if (possible_connection.exists){ 
            possible_connection.enabled = true;           // what about links that got disabled during the addition of a neuron?
            return;
        }

        // genome.globalInnovationNumber += 1;
        // boolean enabled = (random.nextDouble() > 0.5) ? true : false;  // should i do this?
        ConnectionGene new_connection = new ConnectionGene(input, output, 0 , true); // what weight should there be? 
        genome.connections.add(new_connection);


    }


    // ---------------- Helper Functions

    private ConnectionGene connected(int input, int output){
        for (ConnectionGene connection : this.connections){
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

    private boolean create_a_cycle(int input, int output){      // TODO: i need to improve this. The function checks only very basic cycles.
        for (ConnectionGene connection : this.connections){
            if (connection.inpNode == output && connection.outNode == input){
                return true;
            }
        }
        return false;
    }


    // -----------------

    private void remove_link(Genome genome){
        if (genome.connections.isEmpty()) return;

        int connection_remove = random.nextInt(connections.size());
        genome.connections.remove(connection_remove); 
    }


    private void add_neuron(Genome genome){
        if (genome.connections.isEmpty()) return;

        ConnectionGene old_connection = genome.connections.get(random.nextInt(connections.size()));
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

    private void remove_neuron(Genome genome){
        if (genome.number_of_hidden == 0) return;

        NodeGene neuron = genome.nodes.get(random.nextInt(nodes.size() - genome.number_of_inputs - number_of_outputs) + genome.number_of_inputs + number_of_outputs);

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

    private void mutate_weights(Genome genome){     // How to combine lower functions into one?
        
    }


    private double new_value(){
        return clamp(random.nextGaussian(this.mean, this.stdev));
    }

    private double mutate_delta(double value){
        double delta = clamp(random.nextGaussian(0, this.mutate_power));
        return clamp(value + delta);
    }
    // there is also a chance that a value will be replaced by a new random value.

    private double clamp(double x){
        return Math.min(this.max, Math.max(this.min, x));
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

    private NodeGene findNeuron(int x){
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


}





