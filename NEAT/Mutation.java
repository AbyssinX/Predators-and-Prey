package NEAT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Animals.Cell;
import NEAT.NodeGene.Type;

public class Mutation {
    // CONFIGURATIONS
    private static double mean = 0;    
    private static double stdev = 0.5;   
    private static double min = -20;
    private static double max = 20;
    private static double mutate_power = 1.5;

    private static Random random = new Random();

    public static void mutate(Genome genome) {
        // Randomly add node, add connection, or perturb weights
        // What probabilities should I set for each mutation?

        double perturb_chance = 0.8;
        double replace_chance = 0.1;
        double chance = random.nextDouble(); 
        if (chance < 0.05){                            // 5% chance
            add_link(genome);
        } else if (chance < 0.06){                     // 1% chance
            add_neuron(genome);  
        } else if (chance < 1){                        // 95% chance
            mutate_weights(genome, perturb_chance, replace_chance);          
        } 

    }


    
    private static void add_link(Genome genome){
        // int input =  random.nextInt(genome.nodes.size()); 
        // int output = random.nextInt(genome.nodes.size() - genome.number_of_inputs) + genome.number_of_inputs; // ensures I don't pick input as a target
        
        NodeGene inputN = genome.nodes.get(random.nextInt(genome.nodes.size()));
        NodeGene outputN = genome.nodes.get(random.nextInt(genome.nodes.size() - genome.number_of_inputs) + genome.number_of_inputs);


        List<Integer> existent_output = genome.make_output_ids();

        if (existent_output.contains(inputN.id)) return; // ensures I don't pick output as a source
        if (inputN.id == outputN.id) return;

        if (inputN.layer >= outputN.layer) return;

        ConnectionGene possible_connection = connected(inputN.id, outputN.id, genome);  
        if (possible_connection.exists) return;
        
        // IMPORTANT: check reachability in the *correct* direction:
        // if target can already reach source, adding source->target would create a cycle
        if (isReachable(outputN.id, inputN.id, genome)) return;

        int innovation = getInnovationConnection(inputN.id, outputN.id);
        ConnectionGene new_connection = new ConnectionGene(inputN.id, outputN.id, new_value() , true, innovation); // what weight should there be? 
        genome.connections.add(new_connection);

        genome.renormalizeLayers("add_link");
    }


    // ---------------- Helper Functions

    private static ConnectionGene connected(int input, int output, Genome genome){
        for (ConnectionGene connection : genome.connections){
            if (connection.inpNode == input && connection.outNode == output){
                return connection;
            }
        }
        return new ConnectionGene(0,0, 0, false, -1); // none existent connection
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
            if (connection.inpNode == current && connection.enabled){   // connection.enabled &&               // need to check all connections
                if (creates_a_cycle(connection.outNode, finish, visited, genome)) {
                    return true;
                }
            }
        }

        return false;
    }

   

    // -----------------

    private static void add_neuron(Genome genome){
        if (genome.connections.isEmpty()) return;

        List<ConnectionGene> enabledConns = genome.connections.stream().filter(c -> c.enabled).toList();
        if (enabledConns.isEmpty()) return;
        

        ConnectionGene old_connection = enabledConns.get(random.nextInt(enabledConns.size()));

        NodeGene inputN = genome.findNeuron(old_connection.inpNode);
        NodeGene outputN = genome.findNeuron(old_connection.outNode);
    


        int newNodeId = getInnovationNode(old_connection.innovationNumber);

        
        if (genome.findNeuron(newNodeId) == null) {
            // System.out.println("here");
        
            double layer = (inputN.layer + outputN.layer) / 2.0;
   
            int type_activation = random.nextInt(4);
            
            NodeGene new_neuron = new NodeGene(newNodeId, Type.HIDDEN, new_value(), new_value(), type_activation, layer); // activation ? bias?
            genome.nodes.add(new_neuron);
            genome.number_of_hidden++;
            
            // System.out.println(genome.nodes.size());
        }
        

        // old_connection.enabled = false;

        int innovation = getInnovationConnection(old_connection.inpNode, newNodeId);
        ConnectionGene incoming = new ConnectionGene(old_connection.inpNode, newNodeId, 1, true, innovation);

        int innovation2 = getInnovationConnection(newNodeId, old_connection.outNode);              
        ConnectionGene outgoing = new ConnectionGene(newNodeId, old_connection.outNode, old_connection.weight, true, innovation2);

        
        old_connection.enabled = false;
        genome.connections.add(incoming);
        genome.connections.add(outgoing);

        
        genome.renormalizeLayers("add_neuron");
    }






    private static int currentInnovation = 0;
    private static final Map<String, Integer> connectionMap = new HashMap<>();
    private static int currentInnovationNode = Cell.inputs + Cell.outputs;
    private static final Map<Integer, Integer> nodeMap = new HashMap<>();

    public static int getInnovationConnection(int inNode, int outNode) {
        String key = inNode + ":" + outNode;
        
        if (connectionMap.containsKey(key)) {
            return connectionMap.get(key);
        } else {
            currentInnovation++;
            connectionMap.put(key, currentInnovation);
            return currentInnovation;
        }
    }

    public static int getInnovationNode(int key) {
        
        if (nodeMap.containsKey(key)) {

            return nodeMap.get(key);
        } else {
            currentInnovationNode++;
            nodeMap.put(key, currentInnovationNode);
            return currentInnovationNode;
        }
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
                // if (random.nextDouble() < replace_chance / (perturb_chance + replace_chance)){
                //     node.bias = new_value();
                // } else node.bias = mutate_delta(node.bias);
                
                if (random.nextDouble() < 0.8) node.bias += random.nextGaussian() * 0.1;
                else node.bias = random.nextGaussian() * 0.5;
            }
        }
    }



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

}
