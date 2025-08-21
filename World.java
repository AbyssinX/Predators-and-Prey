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
    private double[][] noise_grid = new double[SIZE * CELL_SIZE][SIZE * CELL_SIZE];
    private double FREQUENCY = 0.01;

    private PerlinNoise noise;

    public World(){
        noise = new PerlinNoise();


        // this.grid = new Cell[SIZE][SIZE];

        // for (int i = 0; i < SIZE; i++){
        //     for (int j = 0; j < SIZE; j++){
        //         this.grid[i][j] = new Cell(BACKGROUND_COLOR, "background");
        //     }
        // }

        for (int x = 0; x < SIZE * CELL_SIZE; x++){
            for (int y = 0; y < SIZE * CELL_SIZE; y++){
                double sampleX = x * FREQUENCY;
                double sampleY = y * FREQUENCY;
                this.noise_grid[x][y] = noise.perlin(sampleX, sampleY);
              
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
        for (int i = 0; i < SIZE  * CELL_SIZE; i++){
            for (int j = 0; j < SIZE * CELL_SIZE; j++){
                g.setColor(new Color(0, (int) (noise_grid[i][j] * 255), 0));
                g.fillRect(i, j, 1, 1);
            }
        }


        // for (int i = 0; i < SIZE ; i++){
        //     for (int j = 0; j < SIZE; j++){
                // if (grid[i][j].isEmpty()){
                //     for (int x = i * CELL_SIZE; x < i * CELL_SIZE + CELL_SIZE; i++){
                //         for (int y = j * CELL_SIZE; y < j * CELL_SIZE + CELL_SIZE; j++){
                //             g.setColor(new Color(0, (int) (noise_grid[x][y] * 255), 0));
                //             g.fillRect(x, y, 1, 1);
                //         }
                //     }
                // }


        //         g.setColor(grid[i][j].getColor());
        //         g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE); 
        //     }       
        // }

    }

    public void prepareData(boolean[][] ngrid, int frequency){

    }


}
