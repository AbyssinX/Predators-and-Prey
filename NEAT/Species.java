package NEAT;

import java.util.ArrayList;
import java.util.List;
import Animals.Predator;

public class Species {
    
    public List<Predator> predators;
    public double new_fitness;
    public double old_fitness;
    public double unimproved_generation = 0;
    public Predator representative;


    public Species(List<Predator> predators){
        this.predators = predators;
        Predator.sortByFitness(predators);
        this.representative = predators.get(0);
        this.new_fitness = predators.get(0).genome.fitness;
    }

    public Species(){
        this.predators = new ArrayList<>();
    }



}
