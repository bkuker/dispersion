package com.billkuker.rocketry.dispersion.core.mutators;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.billkuker.rocketry.dispersion.core.variables.Odds;



public class ParachuteFailure extends AbstractComponentMutator {
	private static final Logger log = LoggerFactory.getLogger(ParachuteFailure.class);
	private final Odds o;

	public ParachuteFailure(final Odds o) {
		this.o = o;
	}

	protected void mutate(final RocketComponent c) {
		if (c instanceof Parachute) {
			if (o.occurs()) {
				log.info("Parachute Failure");
				((Parachute) c).setCD(0);
			}
		}
	}
}
