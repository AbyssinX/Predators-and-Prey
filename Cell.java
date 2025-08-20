import java.awt.Color;

public class Cell {

    private Color color;
    private String tag;

    public Cell(Color color, String tag){
        this.color = color;
        this.tag = tag;
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
        return 0;
    }
    
}
