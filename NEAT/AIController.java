package NEAT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import World.World;

public class AIController {
    


    public AIController(){

    }

    public Action getActionPredator(FeedForwardNeuralNetwork nn, int x, int y, World world, World copy){     // what do I base this function on?

        List<Double> inputs = extract_inputs_predator(x, y, world, copy);
        List<Double> outputs = nn.activate(inputs);

        int max_idx = 0;
    
        for (int i = 0; i < inputs.size(); i++){
            if (outputs.get(max_idx) < outputs.get(i)){
                max_idx = i;
            }
            
        
        }
        
        
        return Action.values()[max_idx];
       
    }



    /*
     * Here i get the valuable information from the simulation.
     * What information do I need?
     */

    private List<Double> extract_inputs_predator(int x, int y, World world, World copy){       
        List<Double> inputs = new ArrayList<>();

        // I'm gonna implement rectangular vision first with the width of 3 square and the length given by the parameter sight;
        int sight = 5;                                             //world.getAnimalGrid()[x][y].observable_distance;
        Action direction =  world.getAnimalGrid()[x][y].direction;

        boolean foodFound = false;
        int[] foodPosition = new int[2];              // there will be a problem if multiple preys detected
        // Determine whether the food is visible
        switch(direction){
            case Action.North : 

            
            for (int i = x-1; i <= x+1; i++){
                for (int j = y; j <= y - sight; j--){      // ?
                    try{
                        if (world.getAnimalGrid()[i][j].isPrey() || copy.getAnimalGrid()[i][j].isPrey()){
                            foodFound = true;
                            foodPosition[0] = i;
                            foodPosition[1] = j;
                        }
                    } catch (IndexOutOfBoundsException e){
                        continue;
                    }
                }
            }
            break;

            case Action.East : 
            for (int i = x; i <= x + sight; i++){
                for (int j = y - 1; j <= y + 1; j++){
                    try{
                        if (world.getAnimalGrid()[i][j].isPrey() || copy.getAnimalGrid()[i][j].isPrey()){
                            foodFound = true;
                            foodPosition[0] = i;
                            foodPosition[1] = j;
                        }
                    } catch (IndexOutOfBoundsException e){
                        continue;
                    }
                }
            }
            break;

            case Action.South : 
            for (int i = x-1; i <= x+1; i++){
                for (int j = y; j <= y + sight; j++){     // ?

                    try{
                        if (world.getAnimalGrid()[i][j].isPrey() || copy.getAnimalGrid()[i][j].isPrey()){
                            foodFound = true;
                            foodPosition[0] = i;
                            foodPosition[1] = j;
                        }
                    } catch (IndexOutOfBoundsException e){
                        continue;
                    }
                }
            }
            break;

            case Action.West : 
            for (int i = x ; i <= x - sight; i--){   // ?
                for (int j = y - 1; j <= y + 1; j++){
                    try{
                        if (world.getAnimalGrid()[i][j].isPrey() || copy.getAnimalGrid()[i][j].isPrey()){
                            foodFound = true;
                            foodPosition[0] = i;
                            foodPosition[1] = j;
                        }
                    } catch (IndexOutOfBoundsException e){
                        continue;
                    }
                }
            }
            break;
        }


        // I decide which direction to go to based on how close the food source to a direction vector.
        if (foodFound){
            for (int i = 0; i < world.getAnimalGrid()[x][y].inputs; i++){
                Action vector = Action.values()[i];
                int dot_product = dot_product(foodPosition[0], foodPosition[1], vector.getXDirection(), vector.getYDirection());

                if (dot_product >= 0){
                    double angle = dot_product / magnitude(foodPosition[0], foodPosition[1]);
                    inputs.add(Math.cos(angle));
                } else inputs.add(0.0);
                
            }
        } else {
            for (int i = 0; i < world.getAnimalGrid()[x][y].inputs; i++){
                inputs.add(0.0);
            }
        }




        return inputs;
    }


    private int dot_product(int x1, int y1, int x2, int y2){
        return x1*x2 + y1 * y2;
    }

    private double magnitude(int x, int y){
        return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
    }


}