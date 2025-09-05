package NEAT;

public class NodeGene {
    public enum Type { INPUT, HIDDEN, OUTPUT }
    int id;
    Type type;
    double bias;
    double activation;
    boolean exists;

    public NodeGene(int id, Type type, double bias, double activation){
        if (id != -1){
            exists = true;
        } 
        if (type == Type.HIDDEN){
            this.bias = bias;
            this.activation = activation;
        }

        this.id = id;
        this.type = type;

    }
}