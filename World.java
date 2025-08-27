import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;


public class World {

    private static int LAKE1 = 0;
    private static int LAKE2 = 1;
    private static int BEACH = 2;
    private static int GRASS = 3;
    private static int BUSH =  4;
    
    private static Color[] COLOURS_TERRAIN =   {new Color(54,100,190), new Color(59,132,147), new Color(192,178,97), new Color(127,170,73), new Color(70,131,30)   };
    private static int[] ALL_TERRAIN = {                              LAKE1,                       LAKE2,                        BEACH,                      GRASS,                       BUSH   };
    private static double[] weights_terrain =  {                          1,                           1,                          0.3,                          2,                          1   };
    private static double[] heights = new double[ALL_TERRAIN.length];
    private static int[][] map_terrain;
    private static Color[][] map_terrain_colours;

    private static int NOT_FOOD = 0;
    private static int FOOD = 1;
    private static Color FOOD_COLOUR =                      new Color(92,59, 156);
    private static int[] ALL_FOOD =        {    NOT_FOOD,                         FOOD};
    private static double[] weights_food = {           5,                            1};
    private static int[][] map_food;

    // public static int WORLD_SIZE = 700;
    // public static int TILE_SIZE = 16;
    


    public static int SIZE = 140;
    public static int CELL_SIZE = 5;
    public static int WORLD_SIZE = SIZE * CELL_SIZE;
    // private static int BUSH_SIZE = 5;
    // public static Color BUSH_COLOR = new Color(0,100,0);
    public static Color BACKGROUND_COLOR = new Color(0, 163, 108);



    private Cell[][] grid;
    private double[][] noise_grid = new double[SIZE * CELL_SIZE][SIZE * CELL_SIZE];
    private double[][] noise_food = new double[SIZE * CELL_SIZE][SIZE * CELL_SIZE];

    private double FREQUENCY = 0.03;
    private PerlinNoise noise;


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

                this.noise_grid[x][y] = HelpFunctions.easingSinFunc(layer1 + layer2 + layer3 + layer4 + layer5); // adding different rescaled noise layers generates Fractal Perlin Noise.
                
                /* 
                 * Here, I will generate the second noise grid that will determine the diustribuition of berries on the bushes.
                 */
            
                double food_layer = noise.OctavePerlin(sampleX, sampleY, 9, 1);
                this.noise_food[x][y] = HelpFunctions.easingSinFunc(food_layer);

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



    private static Color[] tile_corner_types = new Color[4];

    public void draw(Graphics g){
        for (int i = 0; i < SIZE; i++){
            for (int j = 0; j < SIZE; j++){


                int parameter = (int) (noise_food[i][j] * 255);

                if (map_terrain[i][j] == BUSH && map_food[i][j] == FOOD){
                    g.setColor(FOOD_COLOUR);
                } else g.setColor(map_terrain_colours[i][j]);
                // g.setColor(COLOURS_TERRAIN[map_terrain[i][j]]);
                
            
                // g.setColor(new Color(parameter, parameter, parameter));
                g.fillRect(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);

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
