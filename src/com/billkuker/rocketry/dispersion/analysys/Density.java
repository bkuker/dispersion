package com.billkuker.rocketry.dispersion.analysys;

import java.awt.geom.PathIterator;

import net.sf.openrocket.util.Coordinate;

public interface Density {
	public static double ONE_SIGMA = .68268949;
	public static double TWO_SIGMA = .954499736;
	public static double THREE_SIGMA = .997300204;

	public void add(Coordinate c);

	public Iterable<Path> getPaths(double mass);

	public static interface Path {
		public PathIterator getPathIterator();
	}
}
