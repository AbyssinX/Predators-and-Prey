import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import Animals.Predator;
import Animals.Prey;
import World.World;
import NEAT.Genome;

public class Simulation extends JPanel implements ActionListener {

    private World world;
    private Random random;

    private static int step = 0;
    private static int time_update_fittness = 100;              // If this term is constant, in the beginning the program will take forever to evolve.
                                                                      // I need to allow this to change in the accordance to the best fitness score. 

    // public static int[] the_worlds_I_like = {15, 17, 100};

    public Simulation(){
        random = new Random();
        this.world = new World(random);

        this.setPreferredSize(new Dimension(World.WORLD_SIZE, World.WORLD_SIZE));
    }







    private List<Prey> preys = new ArrayList<>();
    private List<Predator> predators = new ArrayList<>();
    private List<Prey> offsprings_preys = new ArrayList<>();
    private List<Predator> offsprings_predators = new ArrayList<>();

    @Override
    public void actionPerformed(ActionEvent e){

        World newWorld = new World();
        step++;

        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){
                if (world.getAnimalGrid()[i][j].isPredator()){
                    

                    if (step == time_update_fittness){ 
                        Predator predator = (Predator) world.getAnimalGrid()[i][j];
                        Genome.evaluateFitnessPredator(predator);                    // should i keep it out of the loop? 
                        predators.add(predator);
                        newWorld.set(i,j, world.getAnimalGrid()[i][j]);

                    } else predatorAction(i, j, newWorld);

                } else if (world.getAnimalGrid()[i][j].isPrey()){

                    if (step == time_update_fittness){
                        Prey prey = (Prey) world.getAnimalGrid()[i][j];
                        Genome.evaluateFitnessPrey(prey);
                        preys.add(prey);
                        newWorld.set(i,j, world.getAnimalGrid()[i][j]);
                        

                    } else preyAction(i, j, newWorld);

                }
            }
        }

        if (step == time_update_fittness){
            // I compare fittness of all predators/preys. Perform the generation of offsprings. 
            // I want this to seem smooth. So I will update genomes for half of the animals and half will remain unchanged. 
            // But no animal actually disappear from a screen.

            // Predator bestPredator = sort_predator_(predators);
            // Prey bestPrey = findTheBestPrey(preys);

            Genome.reproducePredator(predators);
            
            Genome.reproducePrey(preys);
            
            predators.clear();
            preys.clear();
        }

        
        this.world = newWorld;
    }


















    
    public void predatorAction(int x, int y, World copy){
        Predator predator = (Predator) this.world.getAnimalGrid()[x][y];
        int speed = predator.getSpeed();
        copy.set(x, y, new Predator(predator.getColor(), speed));


        if (preyIsEaten(x,y)){
            predator.setFoodBar(Predator.MAX_FOOD_BAR_VALUE);
        }



    }







    public void preyAction(int x, int y, World copy){
        Prey prey = (Prey) this.world.getAnimalGrid()[x][y];
        int speed = prey.getSpeed();
        copy.set(x, y, new Prey(prey.getColor(), speed));


        if (foodIsEaten(x, y)) {
            prey.setFoodBar(Prey.MAX_FOOD_BAR_VALUE);
        }

        
    }










    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g); // why do i need this?
        this.world.draw(g);
        super.repaint();
    }

    private boolean preyIsEaten(int x, int y){
        return true;
    }

    private boolean foodIsEaten(int x, int y){
        return true;
    }

    
}
