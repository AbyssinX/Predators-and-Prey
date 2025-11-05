package World;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.JPanel;

public class Graph extends JPanel{

    // private final static int X1 = 600;
    private final int width = 150;
    private final int height = 20;
    public Stack<Double> fitness_record;

    public Graph(){
        fitness_record = new Stack<>();
        setPreferredSize(new Dimension(width, height));
    }

    public void registerData(double fitness){
        this.fitness_record.add(fitness);

        if (this.fitness_record.size() > width / 2){
            this.fitness_record.remove(0);
        }
        repaint();
    }


    public void draw(Graphics g){
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        // g.drawRect(0, 0, width, height);

        if (!fitness_record.isEmpty()){
            g.drawString("Max fitness: " + fitness_record.peek().intValue(), 5, 15);
        }

        // g.setColor(Color.GREEN);
        // for (int i = 1; i < fitness_record.size(); i++){
        //     g.drawLine(i - 1, 400 - fitness_record.get(i-1).intValue() / 10, i, 400 - fitness_record.get(i).intValue() / 10);
        // }


    }

    @Override 
    protected void paintComponent(Graphics g){
        super.paintComponent(g);  
        draw(g);                  // will this work?
    }




    
}
