import java.awt.Color;

public class Prey extends Cell{
    
    private int speed;
    private int FOOD_BAR;
    private int WATER_BAR;
    private final int LIFETIME = 120;

    public Prey(Color color, int speed){
        super(color, "prey");
        this.speed = speed;
    }

    @Override
    public int getSpeed(){
        return this.speed;
    }

    
}
