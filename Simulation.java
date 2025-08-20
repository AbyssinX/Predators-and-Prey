import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Random;

import javax.swing.JPanel;

public class Simulation extends JPanel implements ActionListener {

    private World world;
    private Random random;

    public Simulation(){
        this.setPreferredSize(new Dimension(World.SIZE * World.CELL_SIZE, World.SIZE * World.CELL_SIZE));
        random = new Random(1234);
        this.world = new World(random);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        World newWorld = new World();

        for (int i = 0; i < World.SIZE; i++){
            for (int j = 0; j < World.SIZE; j++){
                if (world.getGrid()[i][j].isPredator()){
                    predatorMovement(i, j, newWorld);
                } else if (world.getGrid()[i][j].isPrey()){
                    preyMovement(i, j, newWorld);
                }
            }
        }

        this.world = newWorld;
    }



    
    public void predatorMovement(int x, int y, World copy){
        int dx = (random.nextInt(3) - 1) * world.getGrid()[x][y].getSpeed();
        int dy = (random.nextInt(3) - 1) * world.getGrid()[x][y].getSpeed();

        try{
            if (this.world.getGrid()[x + dx][y + dy].isEmpty()){
                int speed = this.world.getGrid()[x][y].getSpeed();
                copy.set(x+dx, y+dy, new Predator(Color.RED, speed));
            } else {
                int speed = this.world.getGrid()[x][y].getSpeed();
                copy.set(x, y, new Predator(Color.RED, speed));
            }
        } catch(IndexOutOfBoundsException e){
            int speed = this.world.getGrid()[x][y].getSpeed();
            copy.set(x, y, new Predator(Color.RED, speed));
        }

    }




    public void preyMovement(int x, int y, World copy){
        int dx = (random.nextInt(3) - 1) * world.getGrid()[x][y].getSpeed();
        int dy = (random.nextInt(3) - 1) * world.getGrid()[x][y].getSpeed();

        try {
            if (this.world.getGrid()[x + dx][y + dy].isEmpty()){
                int speed = this.world.getGrid()[x][y].getSpeed();
                copy.set(x+dx, y+dy, new Prey(Color.GREEN, speed));
            } else {
                int speed = this.world.getGrid()[x][y].getSpeed();
                copy.set(x, y, new Prey(Color.GREEN, speed));
            }
        } catch (IndexOutOfBoundsException e) {
            int speed = this.world.getGrid()[x][y].getSpeed();
            copy.set(x, y, new Prey(Color.GREEN, speed));
        }

    }


    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g); // why do i need this?
        this.world.draw(g);
        super.repaint();
    }
    
}
