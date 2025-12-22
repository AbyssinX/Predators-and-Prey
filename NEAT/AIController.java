package NEAT;

import java.util.ArrayList;
// import java.util.Comparator;
import java.util.List;
import java.util.Random;

// import Animals.Cell;
import Animals.Predator;
import World.Graph;
import World.World;

public class AIController {
    


    public AIController(){

    }


    public Action getActionPredator(FeedForwardNeuralNetwork nn, int x, int y, World world, World copy){     // what do I base this function on?
        Predator predator = (Predator) world.getAnimalGrid()[x][y];
        List<Double> inputs = extract_inputs_predator(x, y, world, copy);
        List<Double> outputs = nn.activate(inputs);

        Random random = new Random();
        double sum = 0.0;

        double temperature = predator.genome.temperature; 

        for (double o : outputs) {
            sum += Math.exp(o / temperature);
        }

        // Softmax 
        double r = random.nextDouble();                              
        double cumulative = 0;
        for (int i = 0; i < outputs.size(); i++) {
            cumulative += Math.exp(outputs.get(i) / temperature) / sum;
            if (r < cumulative) return Action.values()[i];
        }
        return Action.values()[outputs.size()-1];


    }



    /*
     * Here i get the valuable information from the simulation.
     */

    private List<Double> extract_inputs_predator(int x, int y, World world, World copy){       
        List<Double> inputs = new ArrayList<>();

        // I'm gonna implement rectangular vision first with the width of 3 square and the length given by the parameter sight;
        int sight = 5;                              //world.getAnimalGrid()[x][y].observable_distance;
 

        // Compute nearest prey in a scanned rectangle
        double bestDistPrey = Double.POSITIVE_INFINITY, bestDistPredator = Double.POSITIVE_INFINITY;
        int bestDxPrey = 0, bestDyPrey = 0;
        int bestDxPredator = 0, bestDyPredator = 0;

        int countPreys = 0; 
        int countPredators = 0; 


        for (int i = x - sight; i <= x + sight; i++){

            for (int j = y - sight; j <= y + sight; j++){

                if (i < 0 || j < 0 || i >= World.SIZE || j >= World.SIZE) continue;

                if (world.getAnimalGrid()[i][j].isPrey() || copy.getAnimalGrid()[i][j].isPrey()){
                    int dxPrey = i - x, dyPrey = j - y;
                    double distance = Math.sqrt(dxPrey*dxPrey + dyPrey*dyPrey);
                    
                    countPreys++;
                    if (distance < bestDistPrey) { 
                        bestDistPrey = distance; 
                        bestDxPrey = dxPrey; 
                        bestDyPrey = dyPrey; 
                    }
                }

                if ((world.getAnimalGrid()[i][j].isPredator() || copy.getAnimalGrid()[i][j].isPredator()) && (i != x && j != y)){
                    int dxPredator = i - x, dyPredator = j - y;
                    double distance = Math.sqrt(dxPredator*dxPredator + dyPredator*dyPredator);          // consider the smallest distance, not the one seen last

                    countPredators++;
                    if (distance < bestDistPredator) { 
                        bestDistPredator = distance; 
                        bestDxPredator = dxPredator; 
                        bestDyPredator = dyPredator; 
                    }
                }


                
            }
        }
        
        Predator predator = (Predator) world.getAnimalGrid()[x][y];

        if (bestDistPrey < Double.POSITIVE_INFINITY) {                             
            double maxSight = Math.sqrt(2) * sight;
            double normDist = Math.min(1.0, bestDistPrey / maxSight);           // 0..1
            double angle = Math.atan2(bestDyPrey, bestDxPrey) / Math.PI;           // -1..1

            Genome g = predator.genome;
            g.current_dist = bestDistPrey;
            double progress = g.previous_dist - g.current_dist;
            inputs.add(progress);       
            g.progress = progress;  
            g.previous_dist = g.current_dist;                        

            g.distanceToEnemy += normDist;
            g.countEnemies += 1;
            g.averageDistanceToNearestEnemy = g.distanceToEnemy / g.countEnemies;

            if (angle == 0) g.facingEnemyCount += 1;

            inputs.add(1.0 - normDist);    // closer -> larger
            inputs.add(angle);             // direction

            // facing: dot product with current facing vector
            Action dir = world.getAnimalGrid()[x][y].direction;

            int fx = dir.getXDirection(), fy = dir.getYDirection();

            double dot = (bestDxPrey*fx + bestDyPrey*fy) / (Math.max(1.0, bestDistPrey)); // approx cos
            inputs.add(dot); // -1..1

            inputs.add(countPreys / Math.pow(2,2*sight + 1));

            double relativeVelocity = (bestDxPrey * fx + bestDyPrey * fy) / (bestDistPrey + 1e-6);
            inputs.add(relativeVelocity);
        } else {
        // no prey seen: push zeros
            inputs.add(0.0); inputs.add(0.0); inputs.add(0.0); inputs.add(0.0); inputs.add(0.0); inputs.add(0.0);
        }


        // check if predator is seen
        if (bestDistPredator < Double.POSITIVE_INFINITY) {                             
            double maxSight = Math.sqrt(2) * sight;
            double normDist = Math.min(1.0, bestDistPredator / maxSight);           // 0..1
            double angle = Math.atan2(bestDyPredator, bestDxPredator) / Math.PI;           // -1..1

            inputs.add(1.0 - normDist);    // closer -> larger
            inputs.add(angle);             // direction

            // facing: dot product with current facing vector
            Action dir = world.getAnimalGrid()[x][y].direction;

            int fx = dir.getXDirection(), fy = dir.getYDirection();

            double dot = (bestDxPredator*fx + bestDyPredator*fy) / (Math.max(1.0, bestDistPredator)); // approx cos
            inputs.add(dot); // -1..1
            inputs.add(countPredators / Math.pow(2,2*sight + 1));
        } else {
        // no prey seen: push zeros
            inputs.add(0.0); inputs.add(0.0); inputs.add(0.0); inputs.add(0.0);
        }

        inputs.add((double) predator.getFoodBar() / Predator.MAX_FOOD_BAR_VALUE);
        // inputs.add(predator.age / Predator.MAX_AGE);    
        inputs.add(Math.random() * 0.05);                        // add a little bit of noise
        inputs.add((double) predator.direction.getXDirection());
        inputs.add((double) predator.direction.getYDirection());

        // add border proximity 4 values (normalize by world size)
        inputs.add((double) x / World.SIZE);           // north proximity
        inputs.add((double) (World.SIZE-1-x) / World.SIZE); // south
        inputs.add((double) y / World.SIZE);           // west
        inputs.add((double) (World.SIZE-1-y) / World.SIZE); // east

        


        return inputs;
    }


    // private int dot_product(int x1, int y1, int x2, int y2){
    //     return x1*x2 + y1 * y2;
    // }

    // private double magnitude(int x, int y){
    //     return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    // }


}