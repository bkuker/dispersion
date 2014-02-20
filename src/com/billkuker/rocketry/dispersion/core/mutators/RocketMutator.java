package com.billkuker.rocketry.dispersion.core.mutators;

import net.sf.openrocket.rocketcomponent.Rocket;

public interface RocketMutator extends Mutator {
	public void mutate(Rocket r);
}
