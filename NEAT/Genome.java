package NEAT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Animals.Predator;
import Animals.Prey;
import NEAT.NodeGene.Type;
import World.World;


/*
 * TODO
 * * Finish mutatation of weights - DONE
 * * Add speciation
 * * Elitism 
 * * Improve reproduction functionality ???
 * * Make the graph connected from the very beginning - DONE
 * * Check the power of mutation - DONE
 * * During mutation i initiate connections with no weights - fix this - DONE
 * * Increase number of inputs for predators
 *      Make they recognise borders (nes), other predators (nes), terrain (not nes)
 * * 
 */

 
class Activation {
    public double sigmoid(double x){
        return 1 / (1 + Math.pow(Math.E, -x));
    }
}




public class Genome {
    // CONFIGURATIONS
    private static double mean = 0;    
    private static double stdev = 1;   
    private static double min = -20;
    private static double max = 20;
    // private static double mutation_weights_rate = 0.2;
    private static double mutate_power = 1.2;
    // private static double add_neuron_rate = 1.2;
    // private static double add_link_rate = 0.05;              
    // private static double weight_replace_rate = 1.2;
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




    
    public static int evaluateFitnessPredator(Predator predator) {  
        // Build neural net from this genome                              
        // Run simulation                                       
        // Return fitness (e.g., prey caught or survival time)

        return predator.staying_alive + predator.preysEaten;
    }



    public static void evaluateFitnessPrey(Prey prey) {         
        // Build neural net from this genome                              
        // Run simulation                                       
        // Return fitness (e.g., survival time)

        // return prey.staying_alive + prey.foodEaten;
    }



    /* --------------------------------------------------------------------------
    *  |                             MUTATION: STRUCTURAL                       |
    *  --------------------------------------------------------------------------
    */ 

    private static void mutate(Genome genome) {
        // Randomly add node, add connection, or perturb weights
        // What probabilities should I set for each mutation?

        double perturb_chance = 0.9;
        double replace_chance = 0.1;
        double chance = random.nextDouble(); 
        if (chance < 0.05){                            // 5% chance
            add_link(genome);      
        } else if (chance < 0.06){                     // 1% chance
            remove_link(genome);
        } else if (chance < 0.09){                     // 3% chance
            add_neuron(genome);           
        } else if (chance < 0.11){                     // 2% chance
            remove_neuron(genome);
        } else if (chance < 1){                        // 89% chance
            mutate_weights(genome, perturb_chance, replace_chance);          
        } 
        
    }


    
    private static void add_link(Genome genome){
        int input =  random.nextInt(genome.nodes.size()); 
        int output = random.nextInt(genome.nodes.size() - genome.number_of_inputs) + genome.number_of_inputs; // ensures I don't pick input as a target

        List<Integer> existent_output = genome.make_output_ids();

        if (existent_output.contains(input)) return; // ensures I don't pick output as a source
        if (input == output) return;

        ConnectionGene possible_connection = connected(input, output, genome);  
        if (possible_connection.exists) return;
        
        // IMPORTANT: check reachability in the *correct* direction:
        // if target can already reach source, adding source->target would create a cycle
        if (isReachable(output, input, genome)) return;

        ConnectionGene new_connection = new ConnectionGene(input, output, new_value() , true); // what weight should there be? 
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
    



    private static boolean isReachable(int input, int output, Genome genome){
        Set<Integer> visited = new HashSet<>();
        return creates_a_cycle(input, output, visited, genome);
    }


    private static boolean creates_a_cycle(int current, int finish, Set<Integer> visited, Genome genome){   
        
        if (current == finish) return true;
        if (visited.contains(current)) return false;  // already explored -> no path found along this branch
        visited.add(current);

        for (ConnectionGene connection : genome.connections){
            if (connection.inpNode == current){   // connection.enabled &&               // need to check all connections
                if (creates_a_cycle(connection.outNode, finish, visited, genome)) {
                    return true;
                }
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

        NodeGene new_neuron = new NodeGene(genome.nodes.size(), Type.HIDDEN, new_value(), new_value()); // activation ? bias?
        genome.nodes.add(new_neuron);
        genome.number_of_hidden++;


        ConnectionGene incoming = new ConnectionGene(old_connection.inpNode, new_neuron.id, 1, true);               
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

    private static void mutate_weights(Genome genome, double perturb_chance, double replace_chance){     // How to combine lower functions into one?
        for (ConnectionGene connection : genome.connections){
            if (random.nextDouble() < perturb_chance + replace_chance){
                if (random.nextDouble() < replace_chance / (perturb_chance + replace_chance)){
                    connection.weight = new_value();
                } else connection.weight = mutate_delta(connection.weight);
            }
        }

        for (NodeGene node : genome.nodes){
            if (random.nextDouble() < perturb_chance + replace_chance){
                if (random.nextDouble() < replace_chance / (perturb_chance + replace_chance)){
                    node.bias = new_value();
                } else node.bias = mutate_delta(node.bias);
            }
        }
    }


    // private static void replace_weights(Genome genome){     // How to combine lower functions into one?
    //     for (ConnectionGene connection : genome.connections){
    //         if (random.nextDouble() < 0.1){
    //             connection.weight = new_value();
    //         }
    //     }
    // }


    public static double new_value(){
        return clamp(random.nextGaussian(mean, stdev));
    }

    private static double mutate_delta(double value){
        double delta = clamp(random.nextGaussian(0, mutate_power));
        return clamp(value + delta);
    }
    // TODO: there is also a chance that a value will be replaced by a new random value.

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
        boolean enabled;
        
        if (a.enabled ^ b.enabled){                           // ^ - XOR
            enabled = (random.nextDouble() < 0.25 ) ? true : false;   
        } else {
            enabled = a.enabled;
        }
                                           
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







    // private int[] count_neurons(){
    //     int counter_input = 0;
    //     int counter_output = 0;
    //     for (NodeGene neuron : this.nodes){
    //         if (neuron.type == Type.INPUT){
    //             counter_input++;
    //         } else if (neuron.type == Type.OUTPUT){
    //             counter_output++;
    //         } 
    //     }
    //     return new int[] {counter_input, counter_output};
    // }

    


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
    public static List<Predator> reproducePredator(List<Predator> predators){        // reproduce only if alive ???
        Predator.sortByFitness(predators);

        int reproduction_cutoff = (int) Math.ceil(survival_threshold * predators.size());

        List<Predator> new_generation = new ArrayList<>();
        int modified_population_size = predators.size() - reproduction_cutoff;

        for (int i = 0; i < modified_population_size; i++){
            Predator parent1 = predators.get(random.nextInt(reproduction_cutoff));
            Predator parent2 = predators.get(random.nextInt(reproduction_cutoff));
            Genome offspring_genome = crossover(parent1.genome, parent2.genome);
            mutate(offspring_genome);

            Predator offspring = new Predator(World.PREDATOR_COLOR, 1);
            offspring.genome = offspring_genome;
            
            new_generation.add(offspring);
            // predators.get(i + reproduction_cutoff).genome = offspring_genome;   
            // predators.get(i + reproduction_cutoff).alive = true;              // make them alive once they reproduce ???
                                                                              // I'll need to make it better in the future
            // predators.get(i + reproduction_cutoff).FOOD_BAR = Predator.MAX_FOOD_BAR_VALUE; // refill their stomachs



        }
        return new_generation;

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




    // REPEAT THIS BIT!
    public List<List<Integer>> createLayers(List<Integer> inputs, List<Integer> outputs, Genome genome){ // not sure this is gonna work
        List<List<Integer>> layers = new ArrayList<>();
        Set<Integer> assigned = new HashSet<>(inputs);

        layers.add(new ArrayList<>(inputs));

        while (assigned.size() < genome.nodes.size() - outputs.size()){     // less than or equal to ? 
            List<Integer> newLayer = new ArrayList<>();

            for (NodeGene node : genome.nodes){
                if (assigned.contains(node.id) || inputs.contains(node.id) || outputs.contains(node.id)) {
                    continue;  // already placed or in I/O
                }

                boolean ready = true;
                for (ConnectionGene connection : genome.connections){
                    if (connection.outNode == node.id && connection.enabled){
                        if (!assigned.contains(connection.inpNode)){
                            ready = false;
                            break;
                        }
                    }
                }
                if (ready){
                    newLayer.add(node.id);
                }
            }



            if (newLayer.isEmpty()) {


                // System.out.println("Genome: ");
                // for (NodeGene node : genome.nodes){
                //     System.out.print(node.id + " ");
                // }
                // System.out.println();

                // System.out.println("Enabled connections: ");
                // for (ConnectionGene connection : genome.connections){
                //     if (connection.enabled){
                //         System.out.print(connection.inpNode + " ");
                //         System.out.println(connection.outNode);
                //     }
                // }
                

                // for (NodeGene node : genome.nodes){
                    

                //     if (assigned.contains(node.id) || inputs.contains(node.id) || outputs.contains(node.id)) {
                //         continue;  // already placed or in I/O
                //     }

                    
                //     for (ConnectionGene connection : genome.connections){
                //         if (connection.outNode == node.id && connection.enabled){
                //             if (!assigned.contains(connection.inpNode)){

                //                 System.out.println("Input to this node: " + connection.inpNode);
                //                 System.out.println("This node: " + node.id);
                //                 System.out.println(isReachable(node.id, connection.inpNode, genome));
                //                 System.out.println("----------------------");

                                
                //                 break;
                //             }
                //         }
                //     }

                // }

                throw new RuntimeException("Cycle detected in genome (recurrent connection)!");      // TODO: This still happens sometimes. Figure out why?


            }


            layers.add(newLayer);
            assigned.addAll(newLayer);
        }

        layers.add(new ArrayList<>(outputs));

        for (NodeGene node : genome.nodes){

            List<Boolean> check = new ArrayList<>();
            for (List<Integer> layer : layers){
                check.add(layer.contains(node.id));
            }
            if (allFalse(check)) {
                System.out.println(node.id + "is not present in layers: " + layers);
            }
        }

    
        return layers;


    }

    private boolean allFalse(List<Boolean> check){
        boolean result = true;
        for (boolean value : check){
            if (value) result = false;
        }
        return result;
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


