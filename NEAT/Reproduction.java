package NEAT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import Animals.Predator;
import Animals.Prey;
import World.World;

public class Reproduction {

    private static double survival_threshold = 0.2;
    private static Random random = new Random();


    private static Predator tournamentSelect(List<Predator> population, int k){
        Predator best = null;

        for (int i = 0; i < k; i++){
            Predator candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.genome.adjusted_fitness > best.genome.adjusted_fitness) best = candidate;
        }
        return best;
    }







    public static List<Species> reproducePredatorSpecies(List<Species> all_species, int totalPopulation){
    
    List<Species> new_gen_species = new ArrayList<>();

    // Step 1: Compute adjusted fitness for all individuals
    List<Double> speciesSumAdjusted = new ArrayList<>();
    double totalAdjustedFitness = 0;
    
    for (Species species : all_species) {
        double sumAdjusted = 0.0;

        // for (Predator predator : species.predators) {
        //     // double adjusted = predator.genome.fitness / species.predators.size();
        //     sumFitness += predator.genome.fitness;
        // }
;

        for (Predator predator : species.predators) {
            predator.genome.adjusted_fitness = predator.genome.fitness / species.predators.size();
            sumAdjusted += predator.genome.adjusted_fitness;

        }

        // double avgAdjusted = sumFitness / species.predators.size();
        speciesSumAdjusted.add(sumAdjusted);
        totalAdjustedFitness += sumAdjusted;
    }


    // Step 2: Allocate offspring per species
    List<Integer> offspringCounts = new ArrayList<>();
    for (int s = 0; s < all_species.size(); s++) {
        int count = (int) Math.round((speciesSumAdjusted.get(s) / totalAdjustedFitness) * totalPopulation);
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

        int eliteCount = Math.max(1, species.predators.size() / 10);  
        for (int i = 0; i < eliteCount; i++) {
            newSpecies.predators.add(species.predators.get(i).cloneDeep(false));
        }

        while (newSpecies.predators.size() < offspringCounts.get(s)) {
            Predator parent1 = tournamentSelect(species.predators, 3);
            Predator parent2 = tournamentSelect(species.predators, 3);
            Genome dominant = (parent1.genome.fitness > parent2.genome.fitness) ? parent1.genome : parent2.genome;
            Genome recessive = (parent1.genome.genomeId == dominant.genomeId) ? parent2.genome : parent1.genome;

            Genome child = Crossover.crossover(dominant, recessive);

            Mutation.mutate(child);

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
            Genome offspring_genome = Crossover.crossover(parent1.genome, parent2.genome);
            Mutation.mutate(offspring_genome);
            preys.get(i + reproduction_cutoff).genome = offspring_genome;

        }

    }

}
