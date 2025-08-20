import java.awt.Color;

public class Predator extends Cell{
    
    private int speed;
    private int FOOD_BAR;
    private int WATER_BAR;
    private static final int LIFETIME = 100;

    public Predator(Color color, int speed){
        super(color, "predator");
        this.speed = speed;
    }

    @Override
    public int getSpeed(){
        return this.speed;
    }
}
