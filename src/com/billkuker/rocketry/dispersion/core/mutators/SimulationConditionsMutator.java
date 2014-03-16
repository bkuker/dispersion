package com.billkuker.rocketry.dispersion.core.mutators;

import net.sf.openrocket.simulation.SimulationConditions;

public interface SimulationConditionsMutator extends Mutator {
	public void mutate(SimulationConditions sc);
}
