package com.billkuker.rocketry.dispersion.core.mutators;

import net.sf.openrocket.simulation.SimulationOptions;

public interface SimulationOptionsMutator extends Mutator {
	public void mutate(SimulationOptions op);
}
