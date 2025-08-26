import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;


public class World {

    private static int LAKE1 = 0;
    private static int LAKE2 = 1;
    private static int BEACH = 2;
    private static int GRASS = 3;
    private static int BUSH =  4;
    private static Color[] COLOURS =   {new Color(82,70,186), new Color(70,148,186), new Color(181,186,102), new Color(75,188,67), new Color(28,131,21)};
    private static int[] ALL_TERRAIN = {      LAKE1,        LAKE2,         BEACH,       GRASS,        BUSH};
    private static double[] weights =  {          1,            1,           0.5,           1,           1};
    private static double[] heights = new double[ALL_TERRAIN.length];
    private static int[][] map;
    


    public static int SIZE = 170;
    public static int CELL_SIZE = 4;
    private static int BUSH_SIZE = 5;
    public static Color BUSH_COLOR = new Color(0,100,0);
    public static Color BACKGROUND_COLOR = new Color(0, 163, 108);



    private Cell[][] grid;
    private double[][] noise_grid = new double[SIZE * CELL_SIZE][SIZE * CELL_SIZE];

    private double FREQUENCY = 0.007;
    private PerlinNoise noise;
    private static double min_value = 0;
    private static double max_value = 0;

    private static Random random;

    public World(){
        random = new Random();
        noise = new PerlinNoise(random);

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

                
                
                double layer1 = noise.OctavePerlin(sampleX, sampleY, 1, 2) / 2;
                double layer2 = noise.OctavePerlin(sampleX, sampleY, 2, 1) / 4;
                double layer3 = noise.OctavePerlin(sampleX, sampleY, 4, 0.5) / 6;
                double layer4 = noise.OctavePerlin(sampleX, sampleY, 6, 0.5) / 8;
                double layer5 = noise.OctavePerlin(sampleX, sampleY, 24, 0.5) / 10;

                this.noise_grid[x][y] = easingSinFunc(layer1 + layer2 + layer3 + layer4 + layer5);
                
            
            }
        }
        map = convertToTerrain(noise_grid, weights);
        
    }


    public double easingSinFunc(double x){
        return 0.5 * (Math.sin( 3 * (x - Math.PI / 6) ) + 1);
    }



    private void findMinMax(double[][] world){
        for (int i = 0; i < SIZE  * CELL_SIZE; i++){
            for (int j = 0; j < SIZE * CELL_SIZE; j++){
                if (world[i][j] > max_value){
                    max_value = world[i][j];
                } else if (world[i][j] < min_value){
                    min_value = world[i][j];
                }

            }
        }
    }


    private int[][] convertToTerrain(double[][] grid, double[] weights){
        int total_weights = sum(weights);
        findMinMax(grid);
        double range = max_value - min_value;
        double previous_height = min_value;
        for (int terrain : ALL_TERRAIN){
            double height = range * (weights[terrain] / total_weights) + previous_height;
            heights[terrain] = height;
            previous_height = height;
        }

        int[][] map = new int[grid.length][grid[0].length];
        for (int x = 0; x < grid.length; x++){
            for (int y = 0; y < grid[0].length; y++){
                for (int terrain : ALL_TERRAIN){
                    if (grid[x][y] <= heights[terrain]) {
                        map[x][y] = terrain;
                        break;
                    }
                }
            }
        }
        return map;
    }


    private static int sum(double[] values){
        int sum = 0;
        for (double value : values){
            sum += value;
        }
        return sum;
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
                
                // int parameter = (int) (noise_grid[i][j] * 255);

                g.setColor(COLOURS[map[i][j]]);
 
            
                // g.setColor(new Color(parameter, parameter, parameter));
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

 


}
