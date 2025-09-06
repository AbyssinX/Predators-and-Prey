package World;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

// import javax.swing.Action;
import javax.swing.JPanel;

import Animals.Cell;
import Animals.Predator;
import Animals.Prey;
import NEAT.AIController;
import NEAT.Action;
import NEAT.FeedForwardNeuralNetwork;
import NEAT.Genome;

public class Simulation extends JPanel implements ActionListener {

    private World world;
    private Random random;

    private static int step = 0;
    private static int time_update_fittness = 3;              // If this term is constant, in the beginning the program will take forever to evolve.
                                                                      // I need to allow this to change in the accordance to the best fitness score. 

    // public static int[] the_worlds_I_like = {15, 17, 100};

    public Simulation(){
        random = new Random();
        this.world = new World(random);

        this.setPreferredSize(new Dimension(World.WORLD_SIZE, World.WORLD_SIZE));
        
    }







    private List<Prey> preys = new ArrayList<>();
    private List<Predator> predators = new ArrayList<>();
    // private List<Prey> offsprings_preys = new ArrayList<>();
    // private List<Predator> offsprings_predators = new ArrayList<>();

    @Override
    public void actionPerformed(ActionEvent e){

        World newWorld = new World();
        step++;

        Cell[][] animal_grid = world.getAnimalGrid();
        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){

                if (animal_grid[i][j].isPredator()){
                    

                    predators.add((Predator) animal_grid[i][j]);
                    // System.out.println(animal_grid[i][j].genome.fitness);

                    predatorAction(i, j, newWorld); 

                } else if (animal_grid[i][j].isPrey()){

                    preyAction(i, j, newWorld);

                }
            }
        }

        // if (step == time_update_fittness){
            // I compare fittness of all predators/preys. Perform the generation of offsprings. 
            // I want this to seem smooth. So I will update genomes for half of the animals and half will remain unchanged. 
            // But no animal actually disappear from a screen.


        Genome.reproducePredator(predators);               // need to work on this
        
        // Genome.reproducePrey(preys);
        
        predators.clear();
        // preys.clear();
        // }

        
        this.world = newWorld;
    }


















    
    public void predatorAction(int x, int y, World copy){
        Predator predator = (Predator) this.world.getAnimalGrid()[x][y];


        if (predator.FOOD_BAR == 0){
            predator.alive = false;
        }

        if (predator.alive){
            predator.staying_alive += 1;
            predator.genome.fitness = predator.staying_alive;

            predator.NeuralNetwork = FeedForwardNeuralNetwork.createFromGenome(predator.genome);
            AIController controller = new AIController();
            Action action = controller.getActionPredator(predator.NeuralNetwork, x, y, this.world, copy); // what's next?

            predator.direction = action;

            int new_x;
            int new_y;
            try{
                new_x = x + action.getXDirection();
                new_y = y + action.getYDirection();
                copy.set(new_x, new_y, predator);
            } catch (IndexOutOfBoundsException e){
                new_x = x;
                new_y = y;
                copy.set(new_x, new_y, predator);
            }

            if (preyIsEaten(new_x, new_y, this.world, copy)){
                predator.setFoodBar(Predator.MAX_FOOD_BAR_VALUE);
            } else{
                predator.FOOD_BAR--;
            } 
        } else copy.set(x, y, predator);




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




    private boolean preyIsEaten(int x, int y, World world, World copy){
        if (world.getAnimalGrid()[x][y].isPrey() || copy.getAnimalGrid()[x][y].isPrey()){
            return true;
        } else return false;
    }

    private boolean foodIsEaten(int x, int y){
        return true;
    }

    
}
