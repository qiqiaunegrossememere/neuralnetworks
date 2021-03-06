package com.github.neuralnetworks.samples.xor;

import com.github.neuralnetworks.architecture.Matrix;
import com.github.neuralnetworks.training.TrainingInputData;
import com.github.neuralnetworks.training.TrainingInputDataImpl;
import com.github.neuralnetworks.training.TrainingInputProvider;

/**
 * XOR input provider
 */
public class XorInputProvider implements TrainingInputProvider {

    private static final long serialVersionUID = 1L;

    private int inputSize;
    private int currentInput;
    private TrainingInputData input;

    public XorInputProvider(int inputSize) {
	super();
	this.inputSize = inputSize;
	this.input = new TrainingInputDataImpl(new Matrix(2, 1), new Matrix(1, 1));
    }

    @Override
    public TrainingInputData getNextInput() {
	if (currentInput < inputSize) {
	    switch (currentInput % 4) {
	    case 0:
		input.getInput().set(0, 0, 0);
		input.getInput().set(1, 0, 0);
		input.getTarget().set(0, 0, 0);
		break;
	    case 1:
		input.getInput().set(0, 0, 0);
		input.getInput().set(1, 0, 1);
		input.getTarget().set(0, 0, 1);
		break;
	    case 2:
		input.getInput().set(0, 0, 1);
		input.getInput().set(1, 0, 0);
		input.getTarget().set(0, 0, 1);
		break;
	    case 3:
		input.getInput().set(0, 0, 1);
		input.getInput().set(1, 0, 1);
		input.getTarget().set(0, 0, 0);
		break;
	    }

	    currentInput++;

	    return input;
	}

	return null;
    }

    @Override
    public void reset() {
	currentInput = 0;
    }

    @Override
    public int getInputSize() {
	return inputSize;
    }
}
