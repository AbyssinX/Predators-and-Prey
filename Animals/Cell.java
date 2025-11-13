package Animals;
import java.awt.Color;
// import java.util.List;

import NEAT.FeedForwardNeuralNetwork;
import NEAT.Genome;
import NEAT.Action;




public class Cell {
    
    public static final int inputs = 16;      // change later
    protected static final int outputs = 4;     // change later

    protected Color color;
    protected String tag;
    protected int speed;
    public Action direction; // !
    public int FOOD_BAR;
    public int WATER_BAR;

    public boolean alive = true;

    public Genome genome;
    public FeedForwardNeuralNetwork NeuralNetwork;

    public Cell(Color color, String tag){
        this.color = color;
        this.tag = tag;

        if (isPredator()){
            this.FOOD_BAR = Predator.MAX_FOOD_BAR_VALUE;
            this.WATER_BAR = Predator.MAX_WATER_BAR_VALUE;


        } else if (isPrey()){

            this.FOOD_BAR = Prey.MAX_FOOD_BAR_VALUE;
            this.FOOD_BAR = Prey.MAX_WATER_BAR_VALUE;

        } else alive = false;



    }

    public Color getColor(){
        return color;
    }

    public Boolean isPredator(){
        return this.tag.equals("predator");
    }

    public Boolean isPrey(){
        return this.tag.equals("prey");
    }

    public Boolean isEmpty(){
        return this.tag.equals("background");
    }

    public int getSpeed(){
        return speed;
    }

    public void setFoodBar(int x){
        this.FOOD_BAR = x;
    }

    public void setWaterBar(int x){
        this.WATER_BAR = x;
    }

    public int getFoodBar(){
        return this.FOOD_BAR;
    }


    
}
