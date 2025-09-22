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

    public Neuron(int neuron_id, double bias, List<NeuronInput> inputs){
        this.neuron_id = neuron_id;
        this.bias = bias;
        this.inputs = inputs;
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



   
    public List<Double> activate(List<Double> inputs){      // static or not static?
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
            value = activation.sigmoid(value);
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
              
                neurons.add(new Neuron(neuron_id, neuron.bias, neuronInputs));
            }
        }


        return new FeedForwardNeuralNetwork(inputs, outputs, neurons);

    }


}
