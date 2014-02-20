package com.billkuker.rocketry.dispersion.core;

import net.sf.openrocket.document.Simulation;

public class Sample {
	private final Simulation s;

	Sample(final Simulation s) {
		this.s = s;
	}

	public Simulation getSimulation() {
		return s;
	}
}