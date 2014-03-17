package com.billkuker.rocketry.models.wind;

import net.sf.openrocket.models.wind.PinkNoiseWindModel;
import net.sf.openrocket.util.Coordinate;

import com.billkuker.rocketry.dispersion.core.variables.Function;
import com.billkuker.rocketry.dispersion.core.variables.PinkNoise;
import com.billkuker.rocketry.dispersion.core.variables.RememberingFunction;

public class DirectionWindModel extends PinkNoiseWindModel {

	private Function d;
	private double directionAverage = 0;
	private double directionStdDev = 0;

	private static final double ALPHA = 5.0 / 3.0;
	private static final int POLES = 2;
	private static final double STDDEV = 2.252;
	private static final double DELTA_T = 0.05;

	public DirectionWindModel(int seed) {
		super(seed);
		d = new RememberingFunction(new PinkNoise(ALPHA, POLES), DELTA_T);
	}

	public void setDirection(final double direction) {
		this.directionAverage = direction;
	}

	public void setDirectionalStandardDeviation(final double standardDeviation) {
		this.directionStdDev = standardDeviation;
	}

	private double getDirection(double time, double altitude) {
		return directionAverage + d.doubleValue(time) * directionStdDev / STDDEV;
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitude) {
		final double speed = super.getWindVelocity(time, altitude).length();
		final double d = getDirection(time, altitude);
		return new Coordinate(Math.cos(d) * speed, Math.sin(d) * speed, 0);
	}
}
