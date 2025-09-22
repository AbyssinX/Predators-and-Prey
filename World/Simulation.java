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
    private static int reproduction_time = 3;              // If this term is constant, in the beginning the program will take forever to evolve.
                                                                      // I need to allow this to change in the accordance to the best fitness score. 
    private static int birth_time = 20;
    // public static int[] the_worlds_I_like = {15, 17, 100};

    public Simulation(){
        random = new Random();
        this.world = new World(random);

        this.setPreferredSize(new Dimension(World.WORLD_SIZE, World.WORLD_SIZE));
        
    }







    private List<Prey> preys = new ArrayList<>();
    private List<Predator> predators = new ArrayList<>();
    // private List<Prey> offsprings_preys = new ArrayList<>();
    public List<Predator> offsprings_predators = new ArrayList<>();

    @Override
    public void actionPerformed(ActionEvent e){

        World newWorld = new World();
        step++;

        Cell[][] animal_grid = world.getAnimalGrid();
        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){

                if (animal_grid[i][j].isPredator()){
                    
                    predators.add((Predator) animal_grid[i][j]);
            
                    predatorAction(i, j, newWorld); 

                } else if (animal_grid[i][j].isPrey()){

                    preys.add((Prey) animal_grid[i][j]);        // i need to create seperate grids for predators and preys
                    
                    preyAction(i, j, newWorld);

                }
            }
        }
        

        if (allDeadPredators(predators) && !offsprings_predators.isEmpty()){
            generate_new_predators(newWorld);
        }
      
        

        if (remainingPreys(preys, 5)){           
            generate_new_preys(newWorld);
        }

        if (step % reproduction_time == 0 && !predators.isEmpty()){
            
            // I compare fittness of all predators/preys. Perform the generation of offsprings. 
            // I want this to seem smooth. So I will update genomes for half of the animals and half will remain unchanged. 
            // But no animal actually disappear from a screen.

            offsprings_predators = Genome.reproducePredator(predators);               // need to work on this
            
            // Genome.reproducePrey(preys);

        }

        predators.clear();
        preys.clear();
        

        
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

                if (preyIsEaten(new_x, new_y, this.world, copy)){
                    world.getAnimalGrid()[new_x][new_y] = new Cell(World.MARK_BACKGROUND, "background");
                    copy.getAnimalGrid()[new_x][new_y] = new Cell(World.MARK_BACKGROUND, "background");
                    predator.setFoodBar(Predator.MAX_FOOD_BAR_VALUE);
                    predator.genome.fitness++;
                } else{
                    predator.FOOD_BAR--;
                } 




                if ((world.getAnimalGrid()[new_x][new_y].isPredator() || copy.getAnimalGrid()[new_x][new_y].isPredator()) && (world.getAnimalGrid()[new_x][new_y].alive || copy.getAnimalGrid()[new_x][new_y].alive)) {
                    copy.set(x, y, predator);
                } else {
                    copy.set(new_x, new_y, predator);

                    if ((step % birth_time == 0) && !offsprings_predators.isEmpty() && random.nextDouble() > 0.7){
                        copy.set(x,y, offsprings_predators.get(random.nextInt(offsprings_predators.size())));
                    }
                }

            } catch (IndexOutOfBoundsException e){
                new_x = x;
                new_y = y;
                copy.set(new_x, new_y, predator);
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















    private boolean preyIsEaten(int x, int y, World world, World copy){
        if (world.getAnimalGrid()[x][y].isPrey() || copy.getAnimalGrid()[x][y].isPrey()){
            return true;
        } else return false;
    }

    private boolean foodIsEaten(int x, int y){
        return true;
    }






    private void generate_new_predators(World newWorld){             
        Cell[][] animal_grid = newWorld.getAnimalGrid();
        int count = 0;
        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){
                if ((animal_grid[i][j].isEmpty() || !animal_grid[i][j].alive) && count <= (World.MAXIMUM_ANIMAL_COUNT / 2) && random.nextDouble() > 0.996){
                    
                    Cell new_predator = offsprings_predators.get(random.nextInt(offsprings_predators.size()));
                    new_predator.FOOD_BAR = Predator.MAX_FOOD_BAR_VALUE;
                    new_predator.alive = true;
                    newWorld.set(i, j, new_predator);
                    count++;
                }
            }
        }

    }


    private void generate_new_preys(World newWorld){             
        Cell[][] animal_grid = newWorld.getAnimalGrid();
        int count = 0;
        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){
                if ((animal_grid[i][j].isEmpty() || !animal_grid[i][j].alive) && count <= (World.MAXIMUM_ANIMAL_COUNT / 2) && random.nextDouble() > 0.996 ){ 
                    
                    Cell new_prey = new Prey(World.PREY_COLOR, 1); // offsprings_prey.get(random.nextInt(offsprings_predators.size()));
                    // new_prey.FOOD_BAR = Predator.MAX_FOOD_BAR_VALUE;
                    // new_prey.alive = true;
                    newWorld.set(i, j, new_prey);
                    count++;
                }
            }
        }

    }


    // TODO: Find a way to combine undentical methods below
    private boolean allDeadPredators(List<Predator> predators){
        boolean output = true;
        for (Predator predator : predators){
            if (predator.alive){
                output = false;
            }
        }
        return output;
    }


    private boolean remainingPreys(List<Prey> preys, int n){
        return (preys.size() <= n);
    }



    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g); // why do i need this?
        this.world.draw(g);
        super.repaint();
    }



    
}
