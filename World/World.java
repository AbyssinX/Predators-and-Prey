package World;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import Animals.Cell;
import Animals.Predator;
import Animals.Prey;


public class World {

    // Configuration for the terrain
    private static int LAKE1 = 0;
    private static int LAKE2 = 1;
    private static int BEACH = 2;
    private static int GRASS = 3;
    private static int BUSH =  4;
    
    private static Color[] COLOURS_TERRAIN =   {new Color(54,100,190), new Color(59,132,147), new Color(192,178,97), new Color(127,170,73), new Color(70,131,30)   };
    private static int[] ALL_TERRAIN = {                              LAKE1,                       LAKE2,                        BEACH,                      GRASS,                       BUSH   };
    private static double[] weights_terrain =  {                          11,                           11,                          3,                         16,                         12   };    // more bushes
    // private static double[] weights_terrain2 =  {                         1,                           1,                          0.3,                          2,                        1.5   };  // grass is dominating, yet interesting rivers form
    // private static double[] weights_terrain3 =  {                        0.8,                         0.8,                          0.3,                          1.8,                          1 }; // water world
    private static double[] heights = new double[ALL_TERRAIN.length];
    private static int[][] map_terrain;
    private static Color[][] map_terrain_colours;

    private static int NOT_FOOD = 0;
    private static int FOOD = 1;
    private static Color FOOD_COLOUR =                      new Color(92,59, 156);
    private static int[] ALL_FOOD =        {    NOT_FOOD,                         FOOD};
    private static double[] weights_food = {           5,                            1};
    private static int[][] map_food;



    // Configuration for the size of the world.
    public static int SIZE = 135;
    public static int CELL_SIZE = 5;
    public static int WORLD_SIZE = SIZE * CELL_SIZE;
    public static Color MARK_BACKGROUND = new Color(0, 0, 0); // I need this for the functionality of the cells.
    public static Color PREY_COLOR = new Color(124,252,0);
    public static Color PREDATOR_COLOR = new Color(183,53,79);



    private Cell[][] animal_grid;
    public final static int MAXIMUM_ANIMAL_COUNT = 150;            // allows me to control how many actors are spawned
    private double[][] noise_grid = new double[SIZE][SIZE];
    private double[][] noise_food = new double[SIZE][SIZE];

    private double FREQUENCY = 0.03;
    private PerlinNoise noise;














    public World(){

        this.animal_grid = new Cell[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                this.animal_grid[i][j] = new Cell(MARK_BACKGROUND, "background");
            }
        }
    
    }




    public World(Random random){

        this.animal_grid = new Cell[SIZE][SIZE];
        int animal_count = 0;

        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){
                if (random.nextDouble() > 0.992 && animal_count < MAXIMUM_ANIMAL_COUNT){ 
                    this.animal_grid[i][j] = random.nextBoolean() ? new Predator(PREDATOR_COLOR, 1) : new Prey(PREY_COLOR, 1);
                    animal_count++;
                } else {
                    this.animal_grid[i][j] = new Cell(MARK_BACKGROUND, "background");
                } 
                    
            }
        }

        noise = new PerlinNoise(random);

        for (int x = 0; x < SIZE; x++){
            for (int y = 0; y < SIZE; y++){
                double sampleX = x * FREQUENCY;
                double sampleY = y * FREQUENCY;

                
                /*
                 * This will generate the main features of a terrain.
                 */
                
                double layer1 = noise.OctavePerlin(sampleX, sampleY, 1, 2) / 2;
                double layer2 = noise.OctavePerlin(sampleX, sampleY, 2, 1) / 4;
                double layer3 = noise.OctavePerlin(sampleX, sampleY, 4, 0.5) / 6;
                double layer4 = noise.OctavePerlin(sampleX, sampleY, 6, 0.5) / 8;
                double layer5 = noise.OctavePerlin(sampleX, sampleY, 24, 0.8) / 10; // a slight increase in the persistence of this layer will result in the more fractal nature

                this.noise_grid[x][y] = layer1 + layer2 + layer3 + layer4 + layer5; // adding different rescaled noise layers generates Fractal Perlin Noise.
                
                /* 
                 * Here, I will generate the second noise grid that will determine the diustribuition of berries on the bushes.
                 */
            
                double food_layer = noise.OctavePerlin(sampleX, sampleY, 9, 1);
                this.noise_food[x][y] = food_layer;

            }
        }
        map_terrain = convertToTerrain(noise_grid, weights_terrain, ALL_TERRAIN);
        map_terrain_colours = convertToColour(map_terrain, COLOURS_TERRAIN);
        map_terrain_colours = HelpFunctions.convolution(map_terrain_colours);
        map_food = convertToTerrain(noise_food, weights_food, ALL_FOOD);
    
    
           
    }





    private Color[][] convertToColour(int[][] grid, Color[] colors){
        Color[][] updated_map = new Color[grid.length][grid[0].length];

        for (int i = 0; i < grid.length; i++){
            for (int j = 0; j < grid[0].length; j++){
                updated_map[i][j] = colors[grid[i][j]];
            }
        }

        return updated_map;
    }



  


    private int[][] convertToTerrain(double[][] grid, double[] weights, int[] ALL_VARIATIONS){
        int total_weights = HelpFunctions.sum(weights);
        double[] MinMax = HelpFunctions.findMinMax(grid);
        double max_value = MinMax[1], min_value = MinMax[0];

        double range = max_value - min_value;
        double previous_height = min_value;
        for (int type : ALL_VARIATIONS){
            double height = range * (weights[type] / total_weights) + previous_height;
            heights[type] = height;
            previous_height = height;
        }

        int[][] map = new int[grid.length][grid[0].length];
        for (int x = 0; x < grid.length; x++){
            for (int y = 0; y < grid[0].length; y++){
                for (int type : ALL_VARIATIONS){
                    if (grid[x][y] <= heights[type]) {
                        map[x][y] = type;
                        break;
                    }
                }
            }
        }
        return map;
    }



    




    
    public Cell[][] getAnimalGrid(){
        return this.animal_grid;
    }


    public void set(int x, int y, Cell cell){
        this.animal_grid[x][y] = cell;
    }







    public void draw(Graphics g){

        for (int i = 0; i < SIZE ; i++){
            for (int j = 0; j < SIZE; j++){
    
                if (animal_grid[i][j].isEmpty() || !animal_grid[i][j].alive){
                    if (map_terrain[i][j] == BUSH && map_food[i][j] == FOOD){
                        g.setColor(FOOD_COLOUR);
                    } else g.setColor(map_terrain_colours[i][j]);
                
                    g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else {
                    // System.out.println(animal_grid[i][j].alive);
                    g.setColor(animal_grid[i][j].getColor());
                    g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE); 
                }
            }       
        }
    }

 


}
