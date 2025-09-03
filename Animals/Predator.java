package Animals;
import java.awt.Color;

public class Predator extends Cell{
    
    public final static int MAX_FOOD_BAR_VALUE = 100;
    public final static int MAX_WATER_BAR_VALUE = 100;
    private static final int LIFETIME = 100;

    public Predator(Color color, int speed){  // I need to change the speed functionality 
        super(color, "predator");
        super.speed = speed;
    }




}
