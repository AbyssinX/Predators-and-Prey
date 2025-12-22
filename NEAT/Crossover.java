package NEAT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import Animals.Predator;
import NEAT.NodeGene.Type;

public class Crossover {

    private static Random random = new Random();

    public static Genome crossover(Genome parent1, Genome parent2) {
        // Align genes by id and combine
        // Genome offspring = new Genome(nextGenomeID, nextGenomeID, currentInnovation)

        Genome fitter, weaker;
        if (parent1.fitness >= parent2.fitness) {
            fitter = parent1.cloneDeep();
            weaker = parent2.cloneDeep();
        } else {
            fitter = parent2.cloneDeep();
            weaker = parent1.cloneDeep();
        }

        // Create offspring (initially empty)
        Genome offspring = new Genome(Genome.next_genome_id(), fitter.number_of_inputs, fitter.number_of_outputs);


        // Sort parents genes by innovation number
        fitter.connections.sort(Comparator.comparingInt(c -> c.innovationNumber));
        weaker.connections.sort(Comparator.comparingInt(c -> c.innovationNumber));

        int i = 0, j = 0;

        // 1. ALIGN GENES
        while (i < fitter.connections.size() || j < weaker.connections.size()) {

            ConnectionGene fitter_connection = (i < fitter.connections.size()) ? fitter.connections.get(i) : null;
            ConnectionGene weaker_connection = (j < weaker.connections.size()) ? weaker.connections.get(j) : null;


            // --- CASE 1: Both exist and match
            if (fitter_connection != null && weaker_connection != null &&
                fitter_connection.innovationNumber == weaker_connection.innovationNumber) {

                ConnectionGene chosen = (Math.random() < 0.5) ? fitter_connection : weaker_connection;

                // Disabled genes have 25% chance of staying disabled (NEAT rule)
                boolean enabled = chosen.enabled;
                if (!fitter_connection.enabled || !weaker_connection.enabled) {
                    if (Math.random() < 0.75) enabled = false;
                }


                offspring.connections.add(new ConnectionGene(
                    chosen.inpNode, chosen.outNode, chosen.weight, enabled, chosen.innovationNumber
                ));
                

                i++; 
                j++;
                continue;
            }
            // --- CASE 2: Disjoint or excess gene from fitter
            if (fitter_connection != null &&  ( weaker_connection == null || fitter_connection.innovationNumber < weaker_connection.innovationNumber)) {
                // fitter gene only

                offspring.connections.add(new ConnectionGene(
                    fitter_connection.inpNode, fitter_connection.outNode, fitter_connection.weight, fitter_connection.enabled, fitter_connection.innovationNumber
                ));
                

                i++;
                continue;
            }

            // --- CASE 3: if there are only weak genes left, then just skip skip (NEAT rule)
            j++;
        }


        // 2. RECONSTRUCT NODE LIST FROM CONNECTIONS
        
        HashSet<Integer> seenNodes = new HashSet<>();

        for (ConnectionGene connection : offspring.connections) {
            seenNodes.add(connection.inpNode);
            seenNodes.add(connection.outNode);
        }
        
        
        List<NodeGene> new_nodes = new ArrayList<>();
        // Rebuild node list from fitter parent + weaker if needed
        for (int nodeId : seenNodes) {
            NodeGene node = (fitter.findNeuron(nodeId) != null) ? fitter.findNeuron(nodeId) : weaker.findNeuron(nodeId);

            new_nodes.add(new NodeGene(
                node.id, node.type, node.bias, node.activation,
                node.type_activation, node.layer
            ));
        }
        
        offspring.nodes.addAll(new_nodes);
        offspring.number_of_hidden = (int) offspring.nodes.stream().filter(n -> n.type == Type.HIDDEN).count();

        offspring.renormalizeLayers("crossover");

        return offspring;
    }









    // private static NodeGene crossoverNeuron(NodeGene a, NodeGene b) {
    //     assert(a.id == b.id);
    //     int new_id = a.id;
    //     double bias = (random.nextDouble() > 0.5) ? a.bias : b.bias;                    
    //     double activation = (random.nextDouble() > 0.5) ? a.activation : b.activation;
    //     int type_activation = (random.nextDouble() > 0.5) ? a.type_activation : b.type_activation;
        
    //     return new NodeGene(new_id, a.type, bias, activation, type_activation, a.layer);
    // }

    // private static ConnectionGene crossoverConnection(ConnectionGene a, ConnectionGene b) {
    //     assert(a.inpNode == b.inpNode);
    //     assert(a.outNode == b.outNode);
    //     double weight = (random.nextDouble() > 0.5) ? a.weight : b.weight;
    //     boolean enabled;
        
    //     if (a.enabled ^ b.enabled){                           // ^ - XOR
    //         enabled = (random.nextDouble() < 0.25 ) ? true : false;   
    //     } else {
    //         enabled = a.enabled;
    //     }
                                           
    //     return new ConnectionGene(a.inpNode, a.outNode, weight, enabled, a.innovationNumber); // am I using the right innovation number?
    // }





    


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
        if (species.size() < targetSpecies) compatibilityThreshold -= 0.01;

        else if (species.size() > targetSpecies) compatibilityThreshold += 0.01;

        compatibilityThreshold = Math.max(0.1, Math.min(5.0, compatibilityThreshold));
    }


    public static double relativeImprovementThreshold = 0.005; // 0.02
    public static int toleranceOfUnimprovement = 30; // 15
    public static List<Species> match_species(List<Predator> predators, List<Species> all_species){
        // List<Species> new_all_species = new ArrayList<>();
        Collections.shuffle(predators, random);
        List<Species> toRemove = new ArrayList<>();

        for (Species species : all_species) {
            // if (species.predators.isEmpty()){
            //     System.out.println("here");
            // }

            Predator.sortByFitness(species.predators);
            species.representative = species.predators.get(0); 
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
                newSpecies.representative = predator;
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

        // if (predators.size() <= 54) System.out.println(all_species.);

        return all_species;

    }

    


}
