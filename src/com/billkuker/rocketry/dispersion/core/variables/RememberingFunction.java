package com.billkuker.rocketry.dispersion.core.variables;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

public class RememberingFunction implements Function {
	private final Number value;
	private final double deltaT;

	private UnivariateFunction f;

	private final DoubleArray x = new ResizableDoubleArray();
	private final DoubleArray y = new ResizableDoubleArray();
	private double nextX = 0;

	public RememberingFunction(final Number value, final double deltaT) {
		this.value = value;
		this.deltaT = deltaT;
	}

	@Override
	public double doubleValue(final double x) {
		if (x < 0) {
			throw new IllegalArgumentException("Requesting function value at x=" + x);
		}
		if (x + deltaT >= nextX) {
			while (x + deltaT >= nextX) {
				this.x.addElement(nextX);
				this.y.addElement(value.doubleValue());
				nextX += deltaT;
			}
			f = new LinearInterpolator().interpolate(this.x.getElements(), this.y.getElements());
		}
		return f.value(x);
	}

	public static void main(String args[]) {
		Function f = new RememberingFunction(new PinkNoise(), .5);
		for (double d = 0; d < 10; d += 0.1) {
			System.out.println(d + ", " + f.doubleValue(d));
		}
		for (double d = 0; d < 10; d += 0.1) {
			System.out.println(d + ", " + f.doubleValue(d));
		}
	}
}
