package World;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// import javax.swing.Action;
import javax.swing.JPanel;

import Animals.Cell;
import Animals.Predator;
import Animals.Prey;
import NEAT.AIController;
import NEAT.Action;
import NEAT.Genome;
import NEAT.Species;



public class Simulation extends JPanel implements ActionListener {

    private World world;
    private Random random;

    public static int step = 0;
    private static int reproduction_time = 10;              // If this term is constant, in the beginning the program will take forever to evolve.
                                                           // I need to allow this to change in the accordance to the best fitness score. 
    private static int birth_time = 20;
    // public static Set<Double> fitness_record = new HashSet<>();    // create a graph to represent the rise in fitness
    private boolean paused = false;
    public static Graph graph;


    public Simulation(){
        random = new Random();
        this.world = new World(random);

        this.setPreferredSize(new Dimension(World.WORLD_SIZE, World.WORLD_SIZE));
        graph = new Graph();
        
    }







    private Set<Prey> preys = new HashSet<>();  
    private Set<Predator> predators = new HashSet<>();                      // make it a set instead (unique elements)
    // private List<Prey> offsprings_preys = new ArrayList<>();
    public List<Predator> offsprings_predators = new ArrayList<>();         // should be a set?
    List<Predator> new_gen = new ArrayList<>();

    List<Predator> regeneration = new ArrayList<>();

    public List<Species> all_species = new ArrayList<>();
    public List<Species> offsprings_species = new ArrayList<>();

    @Override
    public void actionPerformed(ActionEvent e){


        // ---------------------------------------------- Button Handling ------------------------
        Object action = e.getActionCommand();
        
        if ("START".equals(action)) { 
            paused = false;
        } else if ("PAUSE".equals(action)) {
            paused = true;
        }

        if (paused) return;

        // -----------------------------------------------------------------------------------------


        World newWorld = new World();
        step++;

        Cell[][] animal_grid = world.getAnimalGrid();
        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){

                if (animal_grid[i][j].isPredator()){
                    Predator predator = (Predator) animal_grid[i][j];
                    predators.add(predator);
            
                    predatorAction(i, j, newWorld); 

                    predator.FOOD_BAR--;
                    if (predator.FOOD_BAR <= 0) predator.alive = false;

                } else if (animal_grid[i][j].isPrey()){

                    preys.add((Prey) animal_grid[i][j]);        // i need to create seperate grids for predators and preys
                                                                // Do i though? Can i make it work with only one grid?
                    
                    preyAction(i, j, newWorld);

                }
            }
        }
        

        if (allDeadPredators(predators) && !regeneration.isEmpty()){
            generate_new_predators(newWorld);
        }

        if (remainingPreys(preys, 15)){           
            generate_new_preys(newWorld, 15);
        }

        if (step % reproduction_time == 0 && !predators.isEmpty()){
            // I compare fittness of all predators/preys. Perform the generation of offsprings. 
            // I want this to seem smooth. So I will update genomes for half of the animals and half will remain unchanged. 
            // But no animal actually disappear from a screen.
            
            // List<Predator> forReproduction = new ArrayList<>();
            // forReproduction.addAll(predators);
            // offsprings_predators = Genome.reproducePredator(forReproduction);  

            // Genome.reproducePrey(preys);



            List<Predator> forReproduction = new ArrayList<>();
            forReproduction.addAll(predators);
            // List<Predator> predatorsList = predators.;
            Genome.match_species(forReproduction, all_species);
            offsprings_species = Genome.reproducePredatorSpecies(all_species, predators.size()); 
            Graph.n_species = offsprings_species.size() ;

            for (Species s : offsprings_species){
                for (Predator predator : s.predators) {
                    new_gen.add(predator);
                    regeneration.add(predator);
                }

            }

            Genome.adjustThreshold(offsprings_species);

            predators.clear();                           // clear this after the reproduction step?
            preys.clear();

            // Are those effects on reproduction time even useful?
            if (reproduction_time != 5 && Graph.max_fitness < 1000) reproduction_time -= 1;

            if ((Graph.max_fitness >= 1000 || step >= 5000) && reproduction_time < 20) reproduction_time += 1;

        }

        this.world = newWorld;
    }










    
    public void predatorAction(int x, int y, World copy){
        Predator predator = (Predator) this.world.getAnimalGrid()[x][y];

        // if (predator.FOOD_BAR == 0){
        //     predator.alive = false;
        //     // copy.set(x, y, new Cell(World.MARK_BACKGROUND, "background"));
        // }


        if (predator.alive){
            predator.staying_alive += 1;
            predator.genome.fitness = Genome.evaluateFitnessPredator(predator);

            // ---------------- Fitness data collection ----------------------
            if (graph.fitness_record.empty()){
                graph.registerData(predator.genome.fitness);
            }
            else if (graph.fitness_record.peek() >= predator.genome.fitness){
                graph.registerData(graph.fitness_record.peek());
                
            } else graph.registerData(predator.genome.fitness);
            // ---------------------------------------------------------------


            
            AIController controller = new AIController();
            
            Action action = controller.getActionPredator(predator.NeuralNetwork, x, y, this.world, copy); 
            predator.direction = action;

            int new_x;
            int new_y;

            predator.FOOD_BAR--;
    
            // if ((step % birth_time == 0) && !offsprings_predators.isEmpty() && random.nextDouble() > 0.7){ 
            //     predator.genome = offsprings_predators.get(random.nextInt(offsprings_predators.size())).genome;

            // }

            if ((step % birth_time == 0) && !new_gen.isEmpty() && random.nextDouble() > 0.7){ 
                Predator offspring = new_gen.get(random.nextInt(new_gen.size()));
                predator.genome = offspring.genome;
                // new_gen.remove(offspring);
            }
                    

        
            new_x = x + action.getXDirection();
            new_y = y + action.getYDirection();

            if (new_x < 0 || new_x >= World.SIZE || new_y < 0 || new_y >= World.SIZE) {
                // hit border → don't move
                copy.set(x, y, predator);
                return;
            }

            world.set(x, y, new Cell(World.MARK_BACKGROUND, "background"));
            
            // Handling movement
            if ((world.getAnimalGrid()[new_x][new_y].alive || copy.getAnimalGrid()[new_x][new_y].alive) && (world.getAnimalGrid()[new_x][new_y].isPredator() || copy.getAnimalGrid()[new_x][new_y].isPredator())) { 
                Predator clone = predator.cloneDeep(true);
                copy.set(x, y, clone);

            } else {

                // Handling eating
                if (preyIsEaten(new_x, new_y, this.world, copy)){
                    // world.getAnimalGrid()[new_x][new_y].alive = false;                         // i need to make sure i record died preys here
                    // world.set(new_x, new_y, new Cell(World.MARK_BACKGROUND, "background"));
                    predator.setFoodBar(Predator.MAX_FOOD_BAR_VALUE);
                    predator.preysEaten++;
                } 

                world.set(new_x, new_y, new Cell(World.MARK_BACKGROUND, "background"));
                copy.set(new_x, new_y, predator);


            }

                

        } else {
            world.set(x, y, new Cell(World.MARK_BACKGROUND, "background"));
            copy.set(x, y, predator);    // do i need to set background for copy too? I record died predators. Maybe I shouldn't
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
                    
                    Cell new_predator = new Predator(World.PREDATOR_COLOR, 1);

                    Predator predator = regeneration.get(random.nextInt(regeneration.size()));
                    new_predator.genome = predator.genome.cloneDeep();
                    regeneration.remove(predator);

                    newWorld.set(i, j, new_predator);
                    count++;
                }
            }
        }

    }


    private void generate_new_preys(World newWorld, int m){             
        Cell[][] animal_grid = newWorld.getAnimalGrid();
        int count = 0;
        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){
                if ((animal_grid[i][j].isEmpty() || !animal_grid[i][j].alive) && count <= (World.MAXIMUM_ANIMAL_COUNT / 2) && random.nextDouble() > 0.996){  // - m
                    
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
    private boolean allDeadPredators(Set<Predator> predators){
        boolean output = true;
        for (Predator predator : predators){
            if (predator.alive){
                output = false;
            }
        }
        return output;
    }


    private boolean remainingPreys(Set<Prey> preys, int n){
        return (preys.size() <= n);
    }



    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g); // why do i need this?
        this.world.draw(g);
        // this.graph.draw(g);
        super.repaint();
    }



    
}
