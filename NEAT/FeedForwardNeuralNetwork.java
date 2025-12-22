package NEAT;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class NeuronInput {
    int input_id;
    double weight;

    public NeuronInput(int input_id, double weight){
        this.input_id = input_id;
        this.weight = weight;
    }
}

class Neuron {
    int neuron_id;
    double bias;
    List<NeuronInput> inputs;
    int activation;

    public Neuron(int neuron_id, double bias, List<NeuronInput> inputs, int activation){
        this.neuron_id = neuron_id;
        this.bias = bias;
        this.inputs = inputs;
        this.activation = activation;
    }
}

class Activation {
    public double tanh(double x) { return Math.tanh(x); }
    public double elu(double x) { return x >= 0 ? x : Math.exp(x) - 1; }
    public double leakyReLU(double x) { return x >= 0 ? x : 0.01 * x; }
    public double sigmoid(double x) { return 1.0 / (1.0 + Math.exp(-x)); }

    public double activate(double x, int type) {
        return switch (type) {
            case 0 -> tanh(x);
            case 1 -> leakyReLU(x);
            case 2 -> elu(x);
            default -> sigmoid(x);
        };
    }
}




public class FeedForwardNeuralNetwork {

    Activation activation = new Activation();
    public List<Integer> input_ids;
    public List<Integer> output_ids;
    private List<Neuron> neurons;
    

    public FeedForwardNeuralNetwork(List<Integer> input_ids, List<Integer> output_ids, List<Neuron> neurons){
        this.input_ids = input_ids;
        this.output_ids = output_ids;
        this.neurons = neurons;
    }




    public List<Double> activate(List<Double> inputs){      
        assert(input_ids.size() == inputs.size());          // inputs' order needs to match input_ids' order

        Map<Integer, Double> values = new HashMap<>();
        for (int i = 0; i < inputs.size(); i++){
            int input_id = input_ids.get(i);
            values.put(input_id, inputs.get(i));
        }

   

        for (Neuron neuron : neurons){

            double value = 0;

            for (NeuronInput input : neuron.inputs){

                value += values.get(input.input_id) * input.weight;

            }
            value += neuron.bias;
            value = activation.activate(value, neuron.activation);
            values.put(neuron.neuron_id, value);
        }

        List<Double> outputs = new ArrayList<>();
        for (int output_id : output_ids){
            assert(values.containsKey(output_id));
            outputs.add(values.get(output_id));
        }

        return outputs;

    }


    
    public static FeedForwardNeuralNetwork createFromGenome(Genome genome){
        List<Integer> inputs =  genome.make_input_ids();
        List<Integer> outputs = genome.make_output_ids();
        List<List<Integer>> layers = genome.createLayers(inputs, outputs, genome);

        List<Neuron> neurons = new ArrayList<>();
        for (List<Integer> layer : layers){
            for (int neuron_id : layer){
                List<NeuronInput> neuronInputs = new ArrayList<>();

                for (ConnectionGene connection : genome.connections){

                    if (neuron_id == connection.outNode && connection.enabled){
                        neuronInputs.add(new NeuronInput(connection.inpNode, connection.weight));
                    }
                }
        
                NodeGene neuron = genome.findNeuron(neuron_id);

                neurons.add(new Neuron(neuron_id, neuron.bias, neuronInputs, neuron.type_activation));
            }
        }


        return new FeedForwardNeuralNetwork(inputs, outputs, neurons);

    }


}
