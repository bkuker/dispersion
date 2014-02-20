package com.billkuker.rocketry.dispersion.core;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.exception.SimulationException;

import com.billkuker.rocketry.dispersion.core.mutators.Mutator;
import com.billkuker.rocketry.dispersion.core.mutators.RocketMutator;
import com.billkuker.rocketry.dispersion.core.mutators.SimulationOptionsMutator;

public class Engine {
	final OpenRocketDocument doc;
	final int simulationNumber;
	final List<Mutator> mutators;
	final List<SampleListener> sampleListeners = new Vector<SampleListener>();

	Random r = new Random(0);

	public interface SampleListener {
		public void sampleSimulationComplete(Sample s);
	}

	public Engine(final OpenRocketDocument doc, final int simulationNumber, final List<Mutator> mutators) {
		this.doc = doc.copy();
		this.simulationNumber = simulationNumber;
		this.mutators = mutators;
	}

	public void addSimListener(final SampleListener l) {
		sampleListeners.add(l);
	}

	public void run(final int iterations) throws SimulationException {
		for (int i = 0; i < iterations; i++) {
			OpenRocketDocument doc = this.doc.copy();
			Simulation s = doc.getSimulation(simulationNumber).copy();

			for (Mutator m : mutators) {
				if (m instanceof RocketMutator) {
					((RocketMutator) m).mutate(doc.getRocket());
				} else if (m instanceof SimulationOptionsMutator) {
					((SimulationOptionsMutator) m).mutate(s.getOptions());
				} else {
					throw new Error("Don't know about " + m);
				}
			}

			s.getOptions().setRandomSeed(r.nextInt());

			s.simulate();

			Sample sim = new Sample(s);

			for (SampleListener l : sampleListeners)
				l.sampleSimulationComplete(sim);
		}
	}
}
