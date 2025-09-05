package Animals;
import java.awt.Color;
// import java.util.List;

import NEAT.Genome;
// import NEAT.NodeGene;


enum  Action{

    DoNothing,

    GoNorth,
    GoEast,
    GoSouth,
    GoWest,

    GoNorthEast,
    GoSouthEast,
    GoSouthWest,
    GoNorthWest
    
}



public class Cell {
    
    protected static final int inputs = 3;      // change later
    protected static final int outputs = 1;     // change later

    private Color color;
    private String tag;
    protected int speed;
    private int FOOD_BAR;
    private int WATER_BAR;

    public Genome genome;

    public Cell(Color color, String tag){
        this.color = color;
        this.tag = tag;

        if (isPredator()){
            this.FOOD_BAR = Predator.MAX_FOOD_BAR_VALUE;
            this.WATER_BAR = Predator.MAX_WATER_BAR_VALUE;


        } else if (isPrey()){

            this.FOOD_BAR = Prey.MAX_FOOD_BAR_VALUE;
            this.FOOD_BAR = Prey.MAX_WATER_BAR_VALUE;



        }



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




    
}
