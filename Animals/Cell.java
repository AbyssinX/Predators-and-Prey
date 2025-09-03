package Animals;
import java.awt.Color;

import NEAT.Genome;
import NEAT.NodeGene;

public class Cell {
    private static final int inputs = 3;      // change later
    private static final int outputs = 1;     // change later

    private Color color;
    private String tag;
    protected int speed;
    private int FOOD_BAR;
    private int WATER_BAR;

    public Cell(Color color, String tag){
        this.color = color;
        this.tag = tag;

        if (isPredator()){
            this.FOOD_BAR = Predator.MAX_FOOD_BAR_VALUE;
            this.WATER_BAR = Predator.MAX_WATER_BAR_VALUE;

            Genome genome = new Genome(Genome.next_genome_id(), inputs, outputs);
            for (int id = 0; id < inputs; id++){
                NodeGene input_neuron = new NodeGene(id, NodeGene.Type.INPUT, 0, 0);
                genome.nodes.add(input_neuron);
            }
            for (int id = inputs; id < inputs + outputs; id++){
                NodeGene output_neuron = new NodeGene(id, NodeGene.Type.OUTPUT, 0, 0);
                genome.nodes.add(output_neuron);                                                       // potentially could add links from the very start
            }

        } else if (isPrey()){

            this.FOOD_BAR = Prey.MAX_FOOD_BAR_VALUE;
            this.FOOD_BAR = Prey.MAX_WATER_BAR_VALUE;

            Genome genome = new Genome(Genome.next_genome_id(), inputs, outputs);
            for (int id = 0; id < inputs; id++){
                NodeGene input_neuron = new NodeGene(id, NodeGene.Type.INPUT, 0, 0);
                genome.nodes.add(input_neuron);
            }
            for (int id = inputs; id < inputs + outputs; id++){
                NodeGene output_neuron = new NodeGene(id, NodeGene.Type.OUTPUT, 0, 0);
                genome.nodes.add(output_neuron);
            }

        }



    }

    public Color getColor(){
        return color;
    }

    public Boolean isPredator(){
        return this.tag.equals("predator");
    }

    public Boolean isPrey(){
        return this.tag.equals("prey");
    }

    public Boolean isEmpty(){
        return this.tag.equals("background");
    }

    public int getSpeed(){
        return speed;
    }

    public void setFoodBar(int x){
        this.FOOD_BAR = x;
    }

    public void setWaterBar(int x){
        this.WATER_BAR = x;
    }
    
}
