package com.billkuker.rocketry.models.wind;

import net.sf.openrocket.models.wind.WindModel;
import net.sf.openrocket.util.Coordinate;

/**
 * Simple, non-variable, wind model.
 * 
 * @author bkuker
 *
 */
public class VectorWindModel implements WindModel {

	private final Coordinate v;

	public VectorWindModel(final Coordinate v) {
		this.v = v.clone();
	}

	@Override
	public int getModID() {
		return 0;
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitude) {
		return v;
	}

}
