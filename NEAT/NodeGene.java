package NEAT;

public class NodeGene {
    public enum Type { INPUT, HIDDEN, OUTPUT }
    int id;
    Type type;
    double bias;
    double activation;
    int type_activation;
    boolean exists;
    double layer;

    public NodeGene(int id, Type type, double bias, double activation, int type_activation, double layer){
        if (id != -1){
            exists = true;
        } 
        if (type == Type.HIDDEN){
            this.bias = bias;
            this.activation = activation;
            this.type_activation = type_activation;
        }

        this.id = id;
        this.type = type;
        this.layer = layer;

    }
}