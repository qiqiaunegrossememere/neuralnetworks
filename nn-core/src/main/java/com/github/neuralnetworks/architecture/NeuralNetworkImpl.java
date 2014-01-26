package com.github.neuralnetworks.architecture;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.neuralnetworks.calculation.LayerCalculator;
import com.github.neuralnetworks.util.UniqueList;

/**
 * Base class for all types of neural networks.
 * A neural network is defined only by the layers it contains. The layers themselves contain the connections with the other layers.
 */
public class NeuralNetworkImpl implements NeuralNetwork {

    private Set<Layer> layers;
    private LayerCalculator layerCalculator;

    public NeuralNetworkImpl() {
	super();
	this.layers = new UniqueList<Layer>();
    }

    public NeuralNetworkImpl(List<Layer> layers) {
	super();
	this.layers = new UniqueList<Layer>(layers);
    }

    @Override
    public LayerCalculator getLayerCalculator() {
        return layerCalculator;
    }

    public void setLayerCalculator(LayerCalculator layerCalculator) {
        this.layerCalculator = layerCalculator;
    }

    @Override
    public Set<Layer> getLayers() {
	return layers;
    }

    public void setLayers(Set<Layer> layers) {
        this.layers = layers;
    }

    /* (non-Javadoc)
     * @see com.github.neuralnetworks.architecture.NeuralNetwork#getInputLayer()
     * Default implementation - the input layer is that layer, which doesn't have any inbound connections
     */
    @Override
    public Layer getInputLayer() {
	hasInboundConnections:
	for (Layer l : layers) {
	    for (Connections c : l.getConnections(this)) {
		if (l == c.getOutputLayer() && !(c.getInputLayer() instanceof BiasLayer) && !(c.getOutputLayer() instanceof BiasLayer)) {
		    continue hasInboundConnections;
		}
	    }

	    return l;
	}

	return null;
    }

    @Override
    public Layer getOutputLayer() {
	return getNoOutboundConnectionsLayer();
    }

    protected Layer getNoOutboundConnectionsLayer() {
	hasOutboundConnections:
	for (Layer l : layers) {
	    for (Connections c : l.getConnections(this)) {
		if (l == c.getInputLayer()) {
		    continue hasOutboundConnections;
		}
	    }

	    return l;
	}

	return null;
    }

    /* (non-Javadoc)
     * @see com.github.neuralnetworks.architecture.NeuralNetwork#getConnections()
     * Returns list of all the connections within the network.
     * The list is retrieved by iterating over all the layers. Only connections that have both layers in this network are returned.
     */
    @Override
    public List<Connections> getConnections() {
	List<Connections> result = new UniqueList<>();
	if (layers != null) {
	    for (Layer l : layers) {
		result.addAll(l.getConnections(this));
	    }
	}

	return result;
    }

    /**
     * Add layer to the network
     * @param layer
     * @return whether the layer was added successfully
     */
    public boolean addLayer(Layer layer) {
	if (layer != null) {
	    if (layers == null) {
		layers = new UniqueList<>();
	    }
	    
	    if (!layers.contains(layer)) {
		layers.add(layer);
		return true;
	    }
	}

	return false;
    }


    /**
     * Add layers to the network
     * @param newLayers
     */
    public void addLayers(Collection<Layer> newLayers) {
	if (newLayers != null) {
	    if (layers == null) {
		layers = new UniqueList<>();
	    }

	    for (Layer layer : newLayers) {
		if (!layers.contains(layer)) {
		    layers.add(layer);
		}
	    }
	}
    }

    /**
     * Add connection to the network - this means adding both input and output layers to the network
     * @param connection
     */
    public void addConnection(Connections connection) {
	if (connection != null) {
	    addLayer(connection.getInputLayer());
	    addLayer(connection.getOutputLayer());
	}
    }
}
