import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;


public class World {

    public static int SIZE = 170;
    public static int CELL_SIZE = 4;
    private static int BUSH_SIZE = 5;
    public static Color BUSH_COLOR = new Color(0,100,0);
    public static Color BACKGROUND_COLOR = new Color(0, 163, 108);
    private Cell[][] grid;

    public World(){
        this.grid = new Cell[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                this.grid[i][j] = new Cell(BACKGROUND_COLOR, "background");
            }
        }
    }

    public World(Random random){
        this.grid = new Cell[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                if (random.nextDouble() > 0.995){
                    this.grid[i][j] = random.nextBoolean() ? new Predator(Color.RED, 2) : new Prey(Color.GREEN, 1);
                } else {
                    this.grid[i][j] = new Cell(BACKGROUND_COLOR, "background");
                } 
                    
            }
        }
           
    }

    

    
    public Cell[][] getGrid(){
        return this.grid;
    }

    public void set(int x, int y, Cell cell){
        this.grid[x][y] = cell;
    }




    public void draw(Graphics g){
        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                g.setColor(grid[i][j].getColor());
                g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE); 

            }       
        }

    }


}
