package variables;

import java.util.Random;

public class Gaussian extends Number {

	private static final Random r = new Random(0);
	private final Number mean;
	private final Number stddev;

	public Gaussian(final Number mean, final Number stddev) {
		this.mean = mean;
		this.stddev = stddev;
	}

	public Gaussian(final Number stddev) {
		this.mean = 0;
		this.stddev = stddev;
	}

	@Override
	public double doubleValue() {
		return mean.doubleValue() + r.nextGaussian() * stddev.doubleValue();
	}

	@Override
	public float floatValue() {
		return (float) doubleValue();
	}

	@Override
	public int intValue() {
		return (int) doubleValue();
	}

	@Override
	public long longValue() {
		return (long) doubleValue();
	}

}
