package com.github.neuralnetworks.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.neuralnetworks.architecture.Matrix;
import com.github.neuralnetworks.architecture.NeuralNetwork;
import com.github.neuralnetworks.architecture.types.DBN;
import com.github.neuralnetworks.architecture.types.NNFactory;
import com.github.neuralnetworks.architecture.types.RBM;
import com.github.neuralnetworks.architecture.types.StackedAutoencoder;
import com.github.neuralnetworks.calculation.neuronfunctions.AparapiSigmoid;
import com.github.neuralnetworks.training.DNNLayerTrainer;
import com.github.neuralnetworks.training.OneStepTrainer;
import com.github.neuralnetworks.training.TrainerFactory;
import com.github.neuralnetworks.training.rbm.PCDAparapiTrainer;
import com.github.neuralnetworks.util.Constants;

public class DNNTest {

    @Test
    public void testDBNConstruction() {
	DBN dbn = NNFactory.dbn(new int[] { 4, 4, 4 }, false);
	assertEquals(3, dbn.getLayers().size(), 0);
	assertEquals(2, dbn.getNeuralNetworks().size(), 0);
	assertEquals(2, dbn.getFirstNeuralNetwork().getLayers().size(), 0);
	assertEquals(2, dbn.getLastNeuralNetwork().getLayers().size(), 0);

	dbn = NNFactory.dbn(new int[] { 4, 4, 4 }, true);
	assertEquals(5, dbn.getLayers().size(), 0);
	assertEquals(2, dbn.getNeuralNetworks().size(), 0);
	assertEquals(4, dbn.getFirstNeuralNetwork().getLayers().size(), 0);
	assertEquals(4, dbn.getLastNeuralNetwork().getLayers().size(), 0);

	assertEquals(true, dbn.getFirstNeuralNetwork().getHiddenBiasConnections() != null);
	assertEquals(true, dbn.getFirstNeuralNetwork().getVisibleBiasConnections() != null);
	assertEquals(true, dbn.getLastNeuralNetwork().getHiddenBiasConnections() != null);
	assertEquals(true, dbn.getLastNeuralNetwork().getVisibleBiasConnections() != null);

	assertEquals(false, dbn.getLayers().contains(dbn.getFirstNeuralNetwork().getVisibleBiasConnections().getInputLayer()));
	assertEquals(false, dbn.getLayers().contains(dbn.getLastNeuralNetwork().getVisibleBiasConnections().getInputLayer()));

	assertEquals(true, dbn.getFirstNeuralNetwork().getHiddenLayer() == dbn.getLastNeuralNetwork().getVisibleLayer());

	assertEquals(true, dbn.getOutputLayer().equals(dbn.getLastNeuralNetwork().getHiddenLayer()));
    }

    @Test
    public void testStackedAutoencoderConstruction() {
	StackedAutoencoder sae = NNFactory.sae(new int[] { 5, 4, 3 }, false);
	assertEquals(3, sae.getLayers().size(), 0);
	assertEquals(2, sae.getNeuralNetworks().size(), 0);
	assertEquals(3, sae.getFirstNeuralNetwork().getLayers().size(), 0);
	assertEquals(3, sae.getLastNeuralNetwork().getLayers().size(), 0);

	sae = NNFactory.sae(new int[] { 5, 4, 3 }, true);
	assertEquals(5, sae.getLayers().size(), 0);
	assertEquals(2, sae.getNeuralNetworks().size(), 0);
	assertEquals(5, sae.getFirstNeuralNetwork().getLayers().size(), 0);
	assertEquals(5, sae.getLastNeuralNetwork().getLayers().size(), 0);

	assertEquals(false, sae.getLayers().contains(sae.getFirstNeuralNetwork().getOutputLayer()));
	assertEquals(false, sae.getLayers().contains(sae.getLastNeuralNetwork().getOutputLayer()));
	assertEquals(true, sae.getOutputLayer().equals(sae.getLastNeuralNetwork().getHiddenLayer()));

	assertEquals(true, sae.getFirstNeuralNetwork().getHiddenLayer() == sae.getLastNeuralNetwork().getInputLayer());
    }

    @Test
    public void testDNNLayerTrainer() {
	DBN dbn = NNFactory.dbn(new int [] {3, 2, 2}, true);
	NNFactory.nnSigmoid(dbn, null);

	RBM firstRBM = dbn.getFirstNeuralNetwork();

	Matrix cg1 = firstRBM.getMainConnections().getConnectionGraph();
	cg1.set(0, 0, 0.2f);
	cg1.set(0, 1, 0.4f);
	cg1.set(0, 2, -0.5f);
	cg1.set(1, 0, -0.3f);
	cg1.set(1, 1, 0.1f);
	cg1.set(1, 2, 0.2f);

	Matrix cgb1 = firstRBM.getVisibleBiasConnections().getConnectionGraph();
	cgb1.set(0, 0, 0f);
	cgb1.set(1, 0, 0f);
	cgb1.set(2, 0, 0f);

	Matrix cgb2 = firstRBM.getHiddenBiasConnections().getConnectionGraph();
	cgb2.set(0, 0, -0.4f);
	cgb2.set(1, 0, 0.2f);

	SimpleInputProvider inputProvider = new SimpleInputProvider(new float[][] { { 1, 0, 1 } }, null, 1, 1);

	PCDAparapiTrainer firstTrainer = TrainerFactory.pcdTrainer(firstRBM, null, null, null, null, 1f, 0f, 0f, 1);
	firstTrainer.getProperties().setParameter(Constants.HIDDEN_CONNECTION_CALCULATOR, new AparapiSigmoid());
	firstTrainer.getProperties().setParameter(Constants.VISIBLE_CONNECTION_CALCULATOR, new AparapiSigmoid());

	RBM secondRBM = dbn.getLastNeuralNetwork();

	PCDAparapiTrainer secondTrainer = TrainerFactory.pcdTrainer(secondRBM, null, null, null, null, 1f, 0f, 0f, 1);
	secondTrainer.getProperties().setParameter(Constants.HIDDEN_CONNECTION_CALCULATOR, new AparapiSigmoid());
	secondTrainer.getProperties().setParameter(Constants.VISIBLE_CONNECTION_CALCULATOR, new AparapiSigmoid());

	Map<NeuralNetwork, OneStepTrainer<?>> layerTrainers = new HashMap<>();
	layerTrainers.put(firstRBM, firstTrainer);
	layerTrainers.put(secondRBM, secondTrainer);

	DNNLayerTrainer trainer = TrainerFactory.dnnLayerTrainer(dbn, layerTrainers, inputProvider, null, null);
	trainer.train();
	
	assertEquals(0.2 + 0.13203661, cg1.get(0, 0), 0.00001);
	assertEquals(0.4 - 0.22863509,  cg1.get(0, 1), 0.00001);
	assertEquals(-0.5 + 0.12887852, cg1.get(0, 2), 0.00001);
	assertEquals(-0.3 + 0.26158813, cg1.get(1, 0), 0.00001);
	assertEquals(0.1 - 0.3014404,  cg1.get(1, 1), 0.00001);
	assertEquals(0.2 + 0.25742438, cg1.get(1, 2), 0.00001);

	assertEquals(0.52276707, cgb1.get(0, 0), 0.00001);
	assertEquals(- 0.54617375, cgb1.get(1, 0), 0.00001);
	assertEquals(0.51522285, cgb1.get(2, 0), 0.00001);

	assertEquals(-0.4 - 0.08680013, cgb2.get(0, 0), 0.00001);
	assertEquals(0.2 - 0.02693379, cgb2.get(1, 0), 0.00001);
    }
}
