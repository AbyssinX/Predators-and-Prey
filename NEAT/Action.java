package NEAT;

public enum Action {        // should I split actions into move ans rotate?

    North(0,-1),            
    East(1,0),        
    South(0,1),       
    West(-1,0),          

    // GoNorthEast,
    // GoSouthEast,
    // GoSouthWest,
    // GoNorthWest

    DoNothing(0,0);
    

    private final int x;
    private final int y;

    Action(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getXDirection(){
        return this.x;
    }

    public int getYDirection(){
        return this.y;
    }
}