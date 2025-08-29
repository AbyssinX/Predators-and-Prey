import java.awt.Color;

public class HelpFunctions {
    
    public static int sum(double[] values){
        int sum = 0;
        for (double value : values){
            sum += value;
        }
        return sum;
    }

    public static int sum(int[] values){
        int sum = 0;
        for (double value : values){
            sum += value;
        }
        return sum;
    }

    
    public static double easingSinFunc(double x){
        return 0.5 * (Math.sin( 3 * (x - Math.PI / 6) ) + 1);
    }


    public static double[] findMinMax(double[][] world){
        double[] MinMax = new double[2];
        double min_value = 0;
        double max_value = 0;
        for (int i = 0; i < world.length; i++){
            for (int j = 0; j < world[0].length; j++){
                if (world[i][j] > max_value){
                    max_value = world[i][j];
                } else if (world[i][j] < min_value){
                    min_value = world[i][j];
                }

            }
        }
        MinMax[0] = min_value;
        MinMax[1] = max_value;
        return MinMax;
    }

    
    public static boolean contains(int[] array, int value){ // check whether it works
        for (int element : array){
            if (element == value){
                return true;
            }
        }
        return false;
    }

    private static double[][] convolution_matrix = {{0.0113, 0.0838, 0.0113}, {0.0838, 0.6193, 0.0838}, {0.0113, 0.0838, 0.0113}};
    // private static double[][] convolution_matrix = {{0,0,0}, {0,1,0}, {0,0,0}};
    // private static double[][] convolution_matrix = {{0.25, 0.25}, {0.25, 0.25}};
    
    public static Color[][] convolution(Color[][] map){

        int h = map.length, w = map[0].length;
        Color[][] updated_map = new Color[h][w];

        for (int i = 0; i < map.length; i++){
            for (int j = 0; j < map[0].length; j++){

                int red = 0, green = 0, blue = 0; 

                for (int x = 0; x < convolution_matrix.length; x++){
                    for (int y = 0; y < convolution_matrix[0].length; y++){
                        int xi = Math.max(i + x - (convolution_matrix.length - 1), 0);                    // This clamps the indecies so when the index tries to go beyond the image I ensure that it is at least 0.
                        int yj = Math.max(j + y - (convolution_matrix.length - 1), 0);                    // Thus, I avoid colour artifacts at the border.

      
                        red += (int) (map[xi][yj].getRed() * convolution_matrix[x][y]);
                        green += (int) (map[xi][yj].getGreen() * convolution_matrix[x][y]);
                        blue += (int) (map[xi][yj].getBlue() * convolution_matrix[x][y]);
                    }
                }

                updated_map[i][j] = new Color(red, green, blue);
            }
        }

        return updated_map;
    }
}
