import java.util.Random;

public class PerlinNoise {

    private static final int[] permutation = { 151,160,137,91,90,15,                      // Hash lookup table as defined by Ken Perlin.  This is a randomly
    131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,          // arranged array of all numbers from 0-255 inclusive.
    190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
    88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
    77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
    102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
    135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
    5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
    223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
    129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
    251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
    49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
    138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
    };

    private static int[] p; // The p[] array is used in a hash function that will determine what gradient vector to use later on. 
    
    public PerlinNoise(){
        p = new int[512];
        for(int x = 0; x < 512; x++) {
            p[x] = permutation[x%256];
        }
    }

    public PerlinNoise(Random random){
        p = new int[512];
        for(int x = 0; x < 512; x++) {
            p[x] = permutation[(x + random.nextInt(x+1)) % 256];
        }
    }
    
    public double perlin(double x, double y){
        int xi = (int) x & 255; // same as x mod 255. Will be applied later to generate gradient vectors
        int yi = (int) y & 255;
        
        double xf = x - (int) x; // the fractional part tells us where inside the cube the point lies
        double yf = y - (int) y;

        double u = fade(xf);
        double v = fade(yf);

        

        int left_bottom, left_top, right_bottom, right_top;
        left_bottom =  p[p[    xi ]+     yi ]; // This is the hash function that Perlin Noise uses. Mathematically cheap addition and look-ups.
        left_top =     p[p[    xi ]+ 1 + yi ];
        right_bottom = p[p[1 + xi ]+     yi ];
        right_top =    p[p[1 + xi ]+ 1 + yi ];
  
        double x1, x2, y1;
        x1 = lerp(grad (left_bottom, xf, yf),         // The gradient function calculates the dot product between a pseudorandom
                  grad (right_bottom, xf-1, yf), u);            // gradient vector and the vector from the input coordinate to the 4
                                                      // surrounding points in its unit square.
        x2 = lerp(grad (left_top, xf  , yf-1),              // This is all then lerped together as a sort of weighted average based on the faded (u,v)
                  grad (right_top, xf-1, yf-1),  u);         // values.
                  
        y1 = lerp (x1, x2, v);

        return (y1+1) / 2;        // this way the final value is between [0,1]
    }



    public static double fade(double t) {
                                                        // Fade function as defined by Ken Perlin.  This eases coordinate values
                                                        // so that they will ease towards integral values.  This ends up smoothing
                                                        // the final output.
    return t * t * t * (t * (t * 6 - 15) + 10);         // 6t^5 - 15t^4 + 10t^3
    }


    public static double grad(int hash, double x, double y){
        switch(hash & 0x3)
        {
            case 0x0: return  x + y;  // ( 1,  1) * (x,y)
            case 0x1: return -x + y;  // (-1,  1) * (x,y)
            case 0x2: return  x - y;  // ( 1, -1) * (x,y)
            case 0x3: return -x - y;  // (-1, -1) * (x,y)
            default: return 0; // never happens
        }

    }

    // Linear Interpolate
    public static double lerp(double a, double b, double x) {
        return a + x * (b - a);
    }


    public double OctavePerlin(double x, double y, int octaves, double persistence) {
    double total = 0;
    double frequency = 1;
    double amplitude = 1;
    double maxValue = 0;  // Used for normalizing result to 0.0 - 1.0
    for(int i=0;i<octaves;i++) {
        total += perlin(x * frequency, y * frequency) * amplitude;
        
        maxValue += amplitude;
        
        amplitude *= persistence;
        frequency *= 2;
    }
    
    return total/maxValue;
}

}
