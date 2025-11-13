package NEAT;

public class ConnectionGene {
    int inpNode;
    int outNode;
    double weight;
    boolean enabled;
    boolean exists = false;
    int innovationNumber; // allows to track the historical origin. 

    public ConnectionGene(int inpNode, int outNode, double weight, boolean enabled, int innovationNumber){
        if (inpNode == 0 && outNode == 0){
            exists = false;
        } else {
            exists = true;
        }

        this.inpNode = inpNode;
        this.outNode = outNode;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationNumber = innovationNumber;
    }


}
