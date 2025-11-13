package NEAT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Animals.Predator;
import Animals.Prey;
import NEAT.NodeGene.Type;
import World.World;


/*
 * TODO
 * * Add speciation
 * * Make sure the parameters are set well
 * * Make sure that neural network can accept networks with cycles. 
 * * Penelise species that have't improved in 15 generations
 * * Allow only top 20% to reproduce
 * * 
 */





public class Genome {
    // CONFIGURATIONS
    private static double mean = 0;    
    private static double stdev = 0.5;   
    private static double min = -20;
    private static double max = 20;
    private static double mutate_power = 1.5;
    private static double survival_threshold = 0.1;


    private static Random random = new Random();                   
    private final int genomeId;


    public List<NodeGene> nodes;
    public List<ConnectionGene> connections;

    private final int number_of_inputs;             // make static?
    private int number_of_hidden = 0;
    private final int number_of_outputs;

    public double fitness;

    public double distanceToEnemy = 0;
    public int countEnemies = 0;
    public double averageDistanceToNearestEnemy = 0;
    public int facingEnemyCount = 0;



    public Genome(int genomeId, int inputs, int outputs){
        this.nodes = new ArrayList<>();             
        this.connections = new ArrayList<>();
        this.genomeId = genomeId;
        this.number_of_inputs = inputs;
        this.number_of_outputs = outputs;
    }




    
public static double evaluateFitnessPredator(Predator predator) {
    // Strong primary reward: eats
    double fitness = predator.preysEaten * 100.0;

    // Small reward for surviving longer (encourages not starving)
    fitness += predator.staying_alive * 0.5;

    // Reward reduction for being far from nearest detected prey (encourage pursuit).
    // ensure averageDistanceToNearestEnemy updated elsewhere (see AIController changes).
    double avgDist = predator.genome.averageDistanceToNearestEnemy;
    if (avgDist > 0) {
        fitness += Math.max(0, 100 / (avgDist + 1)); // closer -> more reward, scale to taste
    }

    // Minor reward for facing prey (you can track a boolean or add to genome)
    if (predator.genome.facingEnemyCount > 0) fitness += 5.0 * predator.genome.facingEnemyCount;

    if (predator.preysEaten == 0 && predator.staying_alive > 500) fitness -= 50; // discourage idle survival

    predator.genome.fitness = fitness; // store back for selection
    return fitness;

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
        double replace_chance = 0.15;
        double chance = random.nextDouble(); 
        if (chance < 0.06){                            // 6% chance
            add_link(genome);
                  
        } else if (chance < 0.08){                     // 2% chance
            remove_link(genome);
        } else if (chance < 0.16){                     // 8% chance
            add_neuron(genome);          
        } else if (chance < 0.22){                     // 6% chance
            remove_neuron(genome);
        } else if (chance < 1){                        // 78% chance
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

        int innovation = getInnovation(input, output);
        ConnectionGene new_connection = new ConnectionGene(input, output, new_value() , true, innovation); // what weight should there be? 
        genome.connections.add(new_connection);


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
        // System.out.println(genome.nodes.size());
        if (genome.connections.isEmpty()) return;

        List<ConnectionGene> enabledConns = genome.connections.stream()
            .filter(c -> c.enabled)
            .toList();

        if (enabledConns.isEmpty()) return;

        ConnectionGene old_connection = enabledConns.get(random.nextInt(enabledConns.size()));


        // ConnectionGene old_connection = genome.connections.get(random.nextInt(genome.connections.size()));

        old_connection.enabled = false;

        int type_activation = random.nextInt(4);
        NodeGene new_neuron = new NodeGene(genome.nodes.size(), Type.HIDDEN, new_value(), new_value(), type_activation); // activation ? bias?
        genome.nodes.add(new_neuron);
        genome.number_of_hidden++;

        int innovation = getInnovation(old_connection.inpNode, new_neuron.id);
        ConnectionGene incoming = new ConnectionGene(old_connection.inpNode, new_neuron.id, 1, true, innovation); 
        int innovation2 = getInnovation(new_neuron.id, old_connection.outNode);              
        ConnectionGene outgoing = new ConnectionGene(new_neuron.id, old_connection.outNode, old_connection.weight, true, innovation2);  

        genome.connections.add(incoming);
        genome.connections.add(outgoing);
        // System.out.println(genome.nodes.size());
        // System.out.println("-----------------");
        // System.out.println(genome.number_of_hidden);

    }

    private static void remove_neuron(Genome genome){
        
        if (genome.number_of_hidden == 0) return;

        // NodeGene hidden_neuron = genome.nodes.get(random.nextInt(genome.nodes.size() - genome.number_of_inputs - genome.number_of_outputs) + genome.number_of_inputs + genome.number_of_outputs);
     

        List<NodeGene> hidden_neurons = new ArrayList<>();
        for (NodeGene neuron : genome.nodes){
            if (neuron.type == Type.HIDDEN) {

                hidden_neurons.add(neuron);
            }
        }
        
        NodeGene hidden_neuron = hidden_neurons.get(random.nextInt(hidden_neurons.size()));

        List<ConnectionGene> toRemove = genome.connectedToHidden(hidden_neuron.id);
        genome.connections.removeAll(toRemove);

        genome.nodes.remove(hidden_neuron);
        genome.number_of_hidden--;
        
    }



    private static int currentInnovation = 0;
    private static final Map<String, Integer> connectionMap = new HashMap<>();

    public static int getInnovation(int inNode, int outNode) {
        String key = inNode + ":" + outNode;
        if (connectionMap.containsKey(key)) {
            return connectionMap.get(key);
        } else {
            currentInnovation++;
            connectionMap.put(key, currentInnovation);
            return currentInnovation;
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
                offspring.connections.add(new ConnectionGene(
                    dominant_connection.inpNode,
                    dominant_connection.outNode,
                    dominant_connection.weight,
                    dominant_connection.enabled,
                    dominant_connection.innovationNumber
                ));
            }
        }
        offspring.number_of_hidden = dominant.number_of_hidden;


        return offspring;
    }

    // ----------------- Helper Functions

    public NodeGene findNeuron(int x){
        for (NodeGene neuron : this.nodes){
            if (neuron.id == x){
                return neuron;
            }
        }
        return new NodeGene(-1, Type.INPUT, 0, 0, 0); // none existent neuron
    }


    private ConnectionGene findConnection(int input, int output){
        for (ConnectionGene connection : this.connections){
            if (connection.inpNode == input && connection.outNode == output){
                return connection;
            }
        }
        return new ConnectionGene(0, 0, 0, false, -1); // none existent connection
    }


    // --------------------------   







    private static NodeGene crossoverNeuron(NodeGene a, NodeGene b) {
        assert(a.id == b.id);
        int new_id = a.id;
        double bias = (random.nextDouble() > 0.5) ? a.bias : b.bias;                    
        double activation = (random.nextDouble() > 0.5) ? a.activation : b.activation;
        
        return new NodeGene(new_id, a.type, bias, activation, 0);
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
                                           
        return new ConnectionGene(a.inpNode, a.outNode, weight, enabled, a.innovationNumber); // am I using the right innovation number?
    }





    


    public static double compatibilityThreshold = 1.5; // make adaptive
    public static double compatibilityDistance(Genome genome1, Genome genome2) {
        double c1 = 1.0;
        double c2 = 1.0;
        double c3 = 0.4;

        genome1.connections.sort(Comparator.comparingInt(c -> c.innovationNumber));
        genome2.connections.sort(Comparator.comparingInt(c -> c.innovationNumber));

        int i = 0;
        int j = 0;
        int excess = 0;
        int disjoint = 0;
        int matching = 0;
        double weight_sum = 0.0;

        while (i < genome1.connections.size() && j < genome2.connections.size()) {
            ConnectionGene connection1 = genome1.connections.get(i);
            ConnectionGene connection2 = genome2.connections.get(j);

            if (connection1.innovationNumber == connection2.innovationNumber) {
                matching++;
                weight_sum += Math.abs(connection1.weight - connection2.weight);
                i++; 
                j++;

            } else if (connection1.innovationNumber < connection2.innovationNumber) {
                disjoint++;
                i++;
            } else {
                disjoint++;
                j++;
            }
        }

        // Remaining genes are excess
        excess += (genome1.connections.size() - i) + (genome2.connections.size() - j);

        int N = Math.max(genome1.connections.size(), genome2.connections.size());
        if (N < 20) N = 1;

        double W = (matching > 0) ? weight_sum / matching : 0.0;

        double distance = (c1 * excess + c2 * disjoint) / N + c3 * W;
        // System.out.println(distance);

        return distance; // (c1 * E + c2 * D) / N + c3 * W      where E is the number of excess genes, 
                    //                                           D is the number of disjoin genes,
                    //                                           W is the average weight distances of matching genes, including disabled genes,
                    //                                           N is the number of genes in the bigger genome (can be one if there is less than 20 genes in both genomes),
                    //                                           c1,c2,c3 are parameters to adjust the imporance of the three factors.
    }

    










    public static final double targetSpecies = 15;

    public static void adjustThreshold(List<Species> species) {
        if (species.size() < targetSpecies) Genome.compatibilityThreshold -= 0.01;

        else if (species.size() > targetSpecies) Genome.compatibilityThreshold += 0.01;

        Genome.compatibilityThreshold = Math.max(0.1, Math.min(5.0, Genome.compatibilityThreshold));
    }


    public static double relativeImprovementThreshold = 0.02;
    public static int toleranceOfUnimprovement = 15;
    public static List<Species> match_species(List<Predator> predators, List<Species> all_species){
        // List<Species> new_all_species = new ArrayList<>();
        Collections.shuffle(predators, random);
        List<Species> toRemove = new ArrayList<>();

        for (Species species : all_species) {
            Predator.sortByFitness(species.predators);
            species.representative = species.predators.get(0).cloneDeep(false);
            species.predators.clear();
        }

        for (Predator predator : predators) {
            boolean assigned = false;

            for (Species species : all_species) {

                double distance = compatibilityDistance(predator.genome, species.representative.genome);
                if (distance <= compatibilityThreshold) {
                    species.predators.add(predator);  
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                Species newSpecies = new Species();
                newSpecies.predators.add(predator);
                newSpecies.representative = predator.cloneDeep(false);
                all_species.add(newSpecies);
            }
        }


        for (Species species : all_species) {
            if (species.unimproved_generation >= toleranceOfUnimprovement || species.predators.isEmpty()) {
                toRemove.add(species);
                continue;
            }

            Predator.sortByFitness(species.predators);
            species.old_fitness = species.new_fitness;
            species.new_fitness = species.predators.get(0).genome.fitness;

            if (Math.abs(species.new_fitness - species.old_fitness) / species.old_fitness < relativeImprovementThreshold) species.unimproved_generation++;  
            else species.unimproved_generation = 0;



        }

        all_species.removeAll(toRemove);

        return all_species;

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


    // public static List<Predator> reproducePredator(List<Predator> predators){
    //     Predator.sortByFitness(predators);
    //     int population = predators.size();
    //     int elitism = Math.max(1, population / 10); // keep top 10%
    //     List<Predator> newGen = new ArrayList<>();

    //     // 1) Elitism: clone top N
    //     for (int i = 0; i < elitism; i++){
    //         Predator p = predators.get(i);
    //         Predator clone = p.cloneDeep(false); // implement clone to copy genome, NN
    //         if (random.nextDouble() < 0.3) mutate(clone.genome);
    //         newGen.add(clone);
    //     }

    //     int best_ones = Math.max(1, population / 20); // keep top 20%
    //     List<Predator> best_predators = new ArrayList<>();
    //     for (int i = 0; i < best_ones; i++){
    //         Predator p = predators.get(i);
    //         best_predators.add(p);
    //     }


    //     // 2) Fill rest with crossover + mutation using tournament selection
    //     while (newGen.size() < population){
    //         Predator parent1 = tournamentSelect(best_predators, 3);
    //         Predator parent2 = tournamentSelect(best_predators, 3);
    //         Genome child = crossover(parent1.genome, parent2.genome);
    //         mutate(child);
    //         Predator childPred = new Predator(World.PREDATOR_COLOR, 1);

    //         childPred.genome = child;
    //         childPred.NeuralNetwork = FeedForwardNeuralNetwork.createFromGenome(child);
    //         newGen.add(childPred);
    //     }
    //     return newGen;
    // }

    private static Predator tournamentSelect(List<Predator> population, int k){
        Random random = new Random();
        Predator best = null;

        for (int i = 0; i < k; i++){
            Predator candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.genome.fitness > best.genome.fitness) best = candidate;
        }
        return best;
    }







    public static List<Species> reproducePredatorSpecies(List<Species> all_species, int totalPopulation){
    
    List<Species> new_gen_species = new ArrayList<>();

    // Step 1: Compute adjusted fitness for all individuals
    List<Double> speciesAvgFitness = new ArrayList<>();
    double totalAdjustedFitness = 0;
    
    for (Species species : all_species) {
        double sumAdjusted = 0.0;

        for (Predator predator : species.predators) {
            double adjusted = predator.genome.fitness / species.predators.size();
            sumAdjusted += adjusted;
        }

        double avgAdjusted = sumAdjusted / species.predators.size();
        speciesAvgFitness.add(avgAdjusted);
        totalAdjustedFitness += avgAdjusted;
    }


    // Step 2: Allocate offspring per species
    List<Integer> offspringCounts = new ArrayList<>();
    for (double avgFit : speciesAvgFitness) {
        int count = (int) Math.round((avgFit / totalAdjustedFitness) * totalPopulation);
        offspringCounts.add(count);
    }

    // Step 3: Create next generation

    for (int s = 0; s < all_species.size(); s++) {
        Species species = all_species.get(s);
        Species newSpecies = new Species();
        newSpecies.old_fitness = species.old_fitness;
        newSpecies.new_fitness = species.new_fitness;
        newSpecies.unimproved_generation = species.unimproved_generation;

        Predator.sortByFitness(species.predators);

        int eliteCount = Math.max(1, species.predators.size() / 8);  // top 8% survive with no changes
        for (int i = 0; i < eliteCount; i++) {
            newSpecies.predators.add(species.predators.get(i).cloneDeep(false));
        }

        while (newSpecies.predators.size() < offspringCounts.get(s)) {
            Predator parent1 = tournamentSelect(species.predators, 3);
            Predator parent2 = tournamentSelect(species.predators, 3);
            Genome dominant = (parent1.genome.fitness > parent2.genome.fitness) ? parent1.genome : parent2.genome;
            Genome recessive = (parent1.genome.genomeId == dominant.genomeId) ? parent2.genome : parent1.genome;

            Genome child = crossover(dominant, recessive);
            mutate(child);
            Predator childPred = new Predator(World.PREDATOR_COLOR, 1);
            childPred.genome = child;
            childPred.NeuralNetwork = FeedForwardNeuralNetwork.createFromGenome(child);
            newSpecies.predators.add(childPred);
        }

        new_gen_species.add(newSpecies);
    }


        return new_gen_species;
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






    public Genome cloneDeep() {
        List<NodeGene> newNodes = new ArrayList<>();
        for (NodeGene n : this.nodes) {
            newNodes.add(new NodeGene(n.id, n.type, n.bias, n.activation, n.type_activation));
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
}


