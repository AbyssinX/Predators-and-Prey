

// import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;

import javax.swing.JPanel;

public class Simulation extends JPanel implements ActionListener {

    private World world;
    private Random random;

    public static int[] the_worlds_I_like = {15, 17, 100};

    public Simulation(){
        random = new Random();
        this.world = new World(random);

        this.setPreferredSize(new Dimension(World.WORLD_SIZE, World.WORLD_SIZE));
    }









    @Override
    public void actionPerformed(ActionEvent e){

        World newWorld = new World();

        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){
                if (world.getAnimalGrid()[i][j].isPredator()){
                    predatorAction(i, j, newWorld);
                } else if (world.getAnimalGrid()[i][j].isPrey()){
                    preyAction(i, j, newWorld);
                }
            }
        }

        this.world = newWorld;
    }


















    
    public void predatorAction(int x, int y, World copy){
        // int dx = (random.nextInt(3) - 1) * world.getAnimalGrid()[x][y].getSpeed();
        // int dy = (random.nextInt(3) - 1) * world.getAnimalGrid()[x][y].getSpeed();

        int speed = this.world.getAnimalGrid()[x][y].getSpeed();
        copy.set(x, y, new Predator(this.world.getAnimalGrid()[x][y].getColor(), speed));

    //     try{
    //         if (this.world.getAnimalGrid()[x + dx][y + dy].isEmpty() && copy.getAnimalGrid()[x + dx][y + dy].isEmpty()){
    //             int speed = this.world.getAnimalGrid()[x][y].getSpeed();
    //             copy.set(x+dx, y+dy, new Predator(this.world.getAnimalGrid()[x][y].getColor(), speed));
    //         } else {
    //             int speed = this.world.getAnimalGrid()[x][y].getSpeed();
    //             copy.set(x, y, new Predator(this.world.getAnimalGrid()[x][y].getColor(), speed));
    //         }
    //     } catch(IndexOutOfBoundsException e){
    //         int speed = this.world.getAnimalGrid()[x][y].getSpeed();
    //         copy.set(x, y, new Predator(this.world.getAnimalGrid()[x][y].getColor(), speed));
    //     }
        
        if (preyIsEaten(x,y)){
            this.world.getAnimalGrid()[x][y].setFoodBar(Predator.MAX_FOOD_BAR_VALUE);
        }

    }







    public void preyAction(int x, int y, World copy){
        // int dx = (random.nextInt(3) - 1) * world.getAnimalGrid()[x][y].getSpeed();
        // int dy = (random.nextInt(3) - 1) * world.getAnimalGrid()[x][y].getSpeed();

        int speed = this.world.getAnimalGrid()[x][y].getSpeed();
        copy.set(x, y, new Prey(this.world.getAnimalGrid()[x][y].getColor(), speed));

    //     try {
    //         if (this.world.getAnimalGrid()[x + dx][y + dy].isEmpty() && copy.getAnimalGrid()[x + dx][y + dy].isEmpty()){
    //             int speed = this.world.getAnimalGrid()[x][y].getSpeed();
    //             copy.set(x+dx, y+dy, new Prey(this.world.getAnimalGrid()[x][y].getColor(), speed));
    //         } else {
    //             int speed = this.world.getAnimalGrid()[x][y].getSpeed();
    //             copy.set(x, y, new Prey(this.world.getAnimalGrid()[x][y].getColor(), speed));
    //         }
    //     } catch (IndexOutOfBoundsException e) {
    //         int speed = this.world.getAnimalGrid()[x][y].getSpeed();
    //         copy.set(x, y, new Prey(this.world.getAnimalGrid()[x][y].getColor(), speed));
    //     }
        if (foodIsEaten(x, y)) {
            this.world.getAnimalGrid()[x][y].setFoodBar(Prey.MAX_FOOD_BAR_VALUE);
        }
    }


    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g); // why do i need this?
        this.world.draw(g);
        super.repaint();
    }

    private boolean preyIsEaten(int x, int y){
        return true;
    }

    private boolean foodIsEaten(int x, int y){
        return true;
    }

    
}
