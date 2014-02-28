package com.billkuker.rocketry.models.wind;

import net.sf.openrocket.models.wind.PinkNoiseWindModel;
import net.sf.openrocket.util.Coordinate;

public class DirectionWindModel extends PinkNoiseWindModel {
	
	private PinkNoiseWindModel d;

	public DirectionWindModel(int seed) {
		super(seed);
		d = new PinkNoiseWindModel(seed * 2);
		d.setAverage(0);
	}

	public void setDirection(final double direction) {
		 d.setAverage(direction);
	}

	public void setDirectionalStandardDeviation(final double standardDeviation){
		d.setStandardDeviation(standardDeviation);
	}

	private double getDirection(double time, double altitude) {
		return  d.getWindVelocity(time, 0).x;
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitude) {
		final double speed = super.getWindVelocity(time, altitude).length();
		final double d = getDirection(time, altitude);
		System.err.println(d);
		return new Coordinate(Math.cos(d) * speed, Math.sin(d) * speed, 0);
	}
}
