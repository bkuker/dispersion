package com.billkuker.rocketry.dispersion.core.mutators;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Quaternion;

import com.billkuker.rocketry.dispersion.core.variables.Uniform2PI;

public class Util {

	static Coordinate perturb(final Coordinate c, final Uniform2PI direction, final Number angle) {
		// Create an arbitrary normal perpendicular vector
		final Coordinate p = perp(c);

		// Turn it into a random perpendicular vector
		// Rotate it randomly around c, uniform 360 degrees
		final Quaternion q = Quaternion.rotation(c, direction.doubleValue());
		final Coordinate rp = q.rotate(p);

		// Now rotate the input vector a small amount around the random
		// perpendicular vector
		final Quaternion q2 = Quaternion.rotation(rp, angle.doubleValue());
		return q2.rotate(c);
	}

	/*
	 * Some not-good methods public static Coordinate perturb2(final Coordinate
	 * c, final double dev) { Random r = new Random(); double a1 =
	 * r.nextGaussian() * dev; double a2 = r.nextGaussian() * dev; Coordinate p1
	 * = perp(c); Coordinate p2 = p1.cross(c); Quaternion q1 =
	 * Quaternion.rotation(p1, a1); Quaternion q2 = Quaternion.rotation(p2, a2);
	 * return q1.rotate(q2.rotate(c)); }
	 * 
	 * public static Coordinate perturb(final Coordinate c, final double dev) {
	 * Random r = new Random(); double angle = r.nextDouble() * 2 * Math.PI;
	 * double length = r.nextGaussian() * dev; Coordinate p =
	 * perp(c).multiply(length); Quaternion q = Quaternion.rotation(c, angle);
	 * Coordinate d = q.rotate(p); return
	 * c.normalize().add(d).normalize().multiply(c.length()); }
	 */

	static Coordinate perp(final Coordinate c) {
		Coordinate u = c.normalize();
		if (Math.abs(u.y) < 0.99)
			return new Coordinate(-u.z, 0, u.x).normalize();
		else
			return new Coordinate(0, u.z, -u.y).normalize();
	}
}
