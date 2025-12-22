package NEAT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import Animals.Cell;
import Animals.Predator;
import Animals.Prey;
import NEAT.NodeGene.Type;
import World.World;


/*
 * TODO
 * * Number of species and the number of predators on the screen seems unreasonable sometimes
 * * Make sure the parameters are set well
 * * Penelise species that have't improved in 15 generations
 * * Allow only top 20% to reproduce
 * * 
 */





public class Genome {
    private static Random random = new Random();                   
    public final int genomeId;

    public List<NodeGene> nodes;
    public List<ConnectionGene> connections;

    public final int number_of_inputs;         
    public int number_of_hidden = 0;
    public final int number_of_outputs;
    public final double temperature;


    // Statistics
    public double fitness;
    public double adjusted_fitness;

    public double distanceToEnemy = 0;
    public int countEnemies = 0;
    public double averageDistanceToNearestEnemy = 0;
    public int facingEnemyCount = 0;
    public double progress = 0;

    public double current_dist = 0;
    public double previous_dist = 0;



    public Genome(int genomeId, int inputs, int outputs){
        this.nodes = new ArrayList<>();             
        this.connections = new ArrayList<>();
        this.genomeId = genomeId;
        this.number_of_inputs = inputs;
        this.number_of_outputs = outputs;
        this.temperature = random.nextDouble(0.8) + 0.6; 
    }




    
public static double evaluateFitnessPredator(Predator predator) {
    // Strong primary reward: eats
    double fitness = predator.preysEaten * 100.0;

    if (predator.preysEaten > 0) {
        fitness += predator.staying_alive * 0.5;
    } else {
        // Penalize staying alive without eating
        fitness -= predator.staying_alive * 0.1;
    }


    // Reward reduction for being far from nearest detected prey (encourage pursuit).
    // ensure averageDistanceToNearestEnemy updated elsewhere (see AIController changes).
    double avgDist = predator.genome.averageDistanceToNearestEnemy;

    // Reward getting closer to prey (only if they detected prey)
    if (predator.genome.countEnemies > 0) {
        
        fitness += Math.max(0, 20.0 / (avgDist + 1.0)); // closer = better
        
        // Bonus for facing prey
        if (predator.genome.facingEnemyCount > 0) {
            fitness += predator.genome.facingEnemyCount * 2.0;
        }
    }
    
    // Strong penalty for idle survival
    if (predator.preysEaten == 0 && predator.staying_alive > 200) {
        fitness -= 50;
    }


    return fitness;
    }




    public static void evaluateFitnessPrey(Prey prey) {         
        // Build neural net from this genome                              
        // Run simulation                                       
        // Return fitness (e.g., survival time)

        // return prey.staying_alive + prey.foodEaten;
    }



    

    public void renormalizeLayers(String from){
        Map<Integer, List<Integer>> graph = new HashMap<>();
        Map<Integer, Integer> indegree = new HashMap<>();

        for (NodeGene node : nodes){
            graph.put(node.id, new ArrayList<>());
            indegree.put(node.id, 0);
        }
        for (ConnectionGene connection : connections){
            if (!connection.enabled) continue;

            graph.get(connection.inpNode).add(connection.outNode);
            indegree.put(connection.outNode, indegree.get(connection.outNode) + 1);
        }

        // Kahn's topological sort
        Queue<Integer> queue = new LinkedList<>();

        for (var entry : indegree.entrySet()){
            if (entry.getValue() == 0) queue.add(entry.getKey()); // put input nodes into the queue
        }

        List<Integer> sorted = new ArrayList<>();

        while (!queue.isEmpty()){
            int node = queue.remove();
            sorted.add(node);

            for (int neighbour : graph.get(node)){
                indegree.put(neighbour, indegree.get(neighbour) - 1);
                if (indegree.get(neighbour) == 0) queue.add(neighbour);
            }
        }



        // Cycle detection 
        if (sorted.size() != nodes.size()) {
            System.out.println(sorted.size());
            System.out.println(nodes.size());

            for (ConnectionGene c : connections){
                System.out.println(c.inpNode + " to " + c.outNode + ": " + findNeuron(c.inpNode).layer + " -> " + findNeuron(c.outNode).layer);
            }
            throw new IllegalStateException("Cycle detected: genome is not a DAG. Detected after running " + from);
        }
        
        // Assign layers
        Map<Integer, Integer> layerMap = new HashMap<>();
        for (int id : sorted) layerMap.put(id, 0);
        
        for (int id : sorted){
            int currentLayer = layerMap.get(id);
            for (int neighbour : graph.get(id)){
                layerMap.put(neighbour, Math.max(layerMap.get(neighbour), currentLayer + 1));
            }
        }

        for (NodeGene node : nodes) {
            if (node.type == NodeGene.Type.INPUT) node.layer = 0;  
            else if (node.type == NodeGene.Type.OUTPUT) node.layer = 1000;
            else node.layer = layerMap.get(node.id);
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



    public List<List<Integer>> createLayers(List<Integer> inputs, List<Integer> outputs, Genome genome){ // not sure this is gonna work
        List<List<Integer>> layers = new ArrayList<>();
        Set<Integer> assigned = new HashSet<>(inputs);

        layers.add(new ArrayList<>(inputs));

        
        while (assigned.size() < genome.nodes.size() - outputs.size()){     
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
                throw new RuntimeException("Cycle detected in genome (recurrent connection)!");     
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


    public Genome cloneDeep() {
        List<NodeGene> newNodes = new ArrayList<>();
        for (NodeGene n : this.nodes) {
            newNodes.add(new NodeGene(n.id, n.type, n.bias, n.activation, n.type_activation, n.layer));
        }

        List<ConnectionGene> newConns = new ArrayList<>();
        for (ConnectionGene c : this.connections) {
            newConns.add(new ConnectionGene(c.inpNode, c.outNode, c.weight, c.enabled, c.innovationNumber));
        }

        Genome copyGenome = new Genome(next_genome_id(), this.number_of_inputs, this.number_of_outputs);
        copyGenome.nodes = newNodes;
        copyGenome.connections = newConns;
        copyGenome.number_of_hidden = this.number_of_hidden;

        return copyGenome;
    }


    // ----------------- Helper Functions

    public NodeGene findNeuron(int x){
        for (NodeGene neuron : this.nodes){
            if (neuron.id == x){
                return neuron;
            }
        }
        return null; // none existent neuron
    }

    private static int nextGenomeID = 0;
    public static int next_genome_id(){
        nextGenomeID += 1;
        return nextGenomeID;
    }


    private boolean allFalse(List<Boolean> check){
        boolean result = true;
        for (boolean value : check){
            if (value) result = false;
        }
        return result;
    }


    // private ConnectionGene findConnection(int input, int output){
    //     for (ConnectionGene connection : this.connections){
    //         if (connection.inpNode == input && connection.outNode == output){
    //             return connection;
    //         }
    //     }
    //     return null; // none existent connection
    // }


    // -------------------------- 


}


