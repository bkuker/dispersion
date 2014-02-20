package com.billkuker.rocketry.dispersion.core;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.billkuker.rocketry.dispersion.core.mutators.Mutator;
import com.billkuker.rocketry.dispersion.core.mutators.RocketMutator;
import com.billkuker.rocketry.dispersion.core.mutators.SimulationOptionsMutator;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.exception.SimulationException;

public class Engine {
	final OpenRocketDocument doc;
	final int simulationNumber;
	final List<Mutator> mutators;
	final List<SimListener> simListeners = new Vector<SimListener>();

	Random r = new Random(0);

	public class Sim {
		private final Simulation s;

		Sim(final Simulation s) {
			this.s = s;
		}

		public Simulation getSimulation() {
			return s;
		}
	}

	public interface SimListener {
		public void simComplete(Sim s);
	}

	public Engine(final OpenRocketDocument doc, final int simulationNumber, final List<Mutator> mutators) {
		this.doc = doc.copy();
		this.simulationNumber = simulationNumber;
		this.mutators = mutators;
	}

	public void addSimListener(final SimListener l) {
		simListeners.add(l);
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

			Sim sim = new Sim(s);

			for (SimListener l : simListeners)
				l.simComplete(sim);
		}
	}
}
